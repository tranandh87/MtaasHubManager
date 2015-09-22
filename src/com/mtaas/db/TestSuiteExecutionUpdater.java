package com.mtaas.db;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.testng.ISuite;
import org.testng.xml.XmlTest;

import com.mtaas.bean.TestSuiteExecution;
import com.mtaas.listener.SuiteListener;
import com.mtaas.testRunner.ITestResponseListener;
import com.mtaas.testRunner.TestNGRunner;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.MtaasConstants.TestResult;
import com.mtaas.util.Util;

public class TestSuiteExecutionUpdater{
	
	private static Logger log = Logger.getLogger(TestNGRunner.class.getName());
	
	private static ITestResponseListener suiteExecution;
	
	BlockingQueue<Map<String,Integer>> testSuiteMapperNotifier = null;
	
	public TestSuiteExecutionUpdater(ITestResponseListener suiteExecutionIdEventReceiver){
		if (suiteExecutionIdEventReceiver != null){
			log.info("TestSuiteExecutionUpdater.TestSuiteExecutionUpdater. setting suiteExecution for responding suiteId");
			suiteExecution = suiteExecutionIdEventReceiver;
		}
		else
			log.info("ISuiteExecution is null");
	}
	
	public void setSuiteMapperNotifierQueue(BlockingQueue<Map<String,Integer>> testSuiteMapperNotifier){
		log.info("Setting testSuiteMapperNotifier in TestSuiteExecutionUpdater");
    	this.testSuiteMapperNotifier = testSuiteMapperNotifier;
    }
	
	public void updateTestSuiteExecution(ISuite suite, Timestamp executionStartTime, int reqId){
		
		String suiteName = suite.getName();
		
		int testSuiteId = TestExecutionDAO.getUniqueId("TestSuite", "id", "name", suiteName);
		
    	TestResult inProgress = TestResult.getValue(16);
    	if (testSuiteId > 0){
    		Set<Integer> suiteIds = new HashSet<Integer>();
    		
    		ConcurrentMap<String,Integer> testSuiteMap = new ConcurrentHashMap<String,Integer>();
    		
	    	for (Map.Entry<String, Integer> entry: getUniqueDeviceIdOfSuite(suite).entrySet()){
	    		log.info("updateTestSuiteExecution. Device map key -> value: " + entry.getKey() + " -> " + entry.getValue() );
	    		int deviceId = entry.getValue();
	    		
	    		TestSuiteExecution testSuiteExecution = buildTestSuiteExecution(testSuiteId,deviceId,executionStartTime,inProgress);
	    		int suiteExecutionId = TestExecutionDAO.insertTestSuiteExecution(testSuiteExecution, reqId);
	    		
//	    		SuiteListener.testSuiteMapper.put(suiteName + MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + deviceId , suiteExecutionId);
	    		String suiteMapperKey = Util.buildSuiteMapperKey(suiteName,deviceId);
				testSuiteMap.put(suiteMapperKey, suiteExecutionId);
	    		
	    		log.info("updateTestSuiteExecution. testSuiteMap's Key----Value :" + suiteMapperKey + "---" + suiteExecutionId);
	    		
	    		suiteIds.add(suiteExecutionId);
	    	}
	    	if (suiteExecution != null)
	    		suiteExecution.onInsertSuiteExecution(testSuiteId, suiteIds, reqId);
	    	else
	    		log.error("suite execution is null. Need to quit");
	    	
	    	try {
	    		log.info("updateTestSuiteExecution. putting testSuiteMap into testSuiteMapperNotifier. Map:" + testSuiteMap);
				testSuiteMapperNotifier.put(testSuiteMap);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else{
    		log.info("suiteId not present in suite table. suiteName:" + suiteName);
    	}
	}
	
	/**
	 * This method iterates through all testclass build for a suite and returns the distinct deviceName_version as key and deviceId 
	 * in Device table as value.
	 * @param suite
	 * @return
	 */
	public static Map<String,Integer> getUniqueDeviceIdOfSuite(ISuite suite){
		Map<String,Integer> suiteUniqueDevice = new HashMap<String,Integer>();
		
		for (XmlTest test: suite.getXmlSuite().getTests() ){
			Map<String, String> testParams = test.getAllParameters();
    		String deviceName = testParams.get(MtaasConstants.DEVICE);
    		String version = testParams.get(MtaasConstants.VERSION);
    		
    		//getting the Custom capability
    		String isEmulator = testParams.get(MtaasConstants.IS_EMULATOR);
    		boolean isEmulatorBoolean = Boolean.valueOf(isEmulator);
    		int isEmulatorInt = Util.getIntEquivalentOfBoolean(isEmulatorBoolean);
    		
    		String manufacturer = testParams.get(MtaasConstants.MANUFACTURER);
    		String model = testParams.get(MtaasConstants.MODEL);
    		
    		String hubUrl = testParams.get(MtaasConstants.HUB_URL);
    		int hubId = HubInfoDAO.getGridId(deviceName,hubUrl);
    		
    		int deviceId = TestExecutionDAO.getDeviceId(deviceName,version, hubId,isEmulatorInt,manufacturer,model);
    		
    		if (deviceId > 0){
    			String uniqueDeviceKey = Util.buildUniqueDeviceKey(deviceName,version,isEmulatorInt,manufacturer,model);
    			suiteUniqueDevice.put(uniqueDeviceKey, deviceId);
    			log.info(String.format("available devices on a suite tag, Device map key = %s and value(deviceId) = %s",uniqueDeviceKey,deviceId));
    		}
    		else{
    			log.error("TestSuiteExecutionUpdater.getUniqueDeviceIdOfSuite(). DeviceId for above query is lesser than 0 so not populating this device."
    					+ " This would result in suiteExecuitonId is not included for this particular devie. Solution: check db and above query. Serious error");
    		}
    		
		}
		return suiteUniqueDevice;
	}
	
	private TestSuiteExecution buildTestSuiteExecution(int suiteId, int deviceId, Timestamp executionStartTime, TestResult testResult){
		TestSuiteExecution testSuiteExecution = new TestSuiteExecution();
		
		testSuiteExecution.setTestSuiteId(suiteId);
		testSuiteExecution.setDeviceId(deviceId);
		testSuiteExecution.setStatus(testResult);
		testSuiteExecution.setExecutionStartTime(executionStartTime);
		
		return testSuiteExecution;
	}
}
