package com.mtaas.db;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.xml.XmlTest;

import com.mtaas.bean.TestExecution;
import com.mtaas.testRunner.TestNGRunner;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.MtaasConstants.TestResult;
import com.mtaas.util.Util;

public class TestExecutionUpdater extends Thread {
	
	private static Logger log = Logger.getLogger(TestNGRunner.class.getName());
	Map<String,Integer> testSuiteMap = null;
	
	public TestExecutionUpdater(Map<String,Integer> testSuiteMap){
		this.testSuiteMap = testSuiteMap;
	}
	
	public int updateTestExecution(ITestContext testContext, Timestamp executionStartTime){
		
    	String actualTestName = Util.getActualTestName(testContext.getName(), MtaasConstants.MTAAS_TEST_NAME_SEPERATOR);
		int testId = TestExecutionDAO.getUniqueId("Test", "id", "name", actualTestName);
		
		if (testId > 0){
			XmlTest test = testContext.getCurrentXmlTest();

	    	Map<String, String> testParams = test.getAllParameters();
	    	String deviceName = testParams.get(MtaasConstants.DEVICE);
	    	String version = testParams.get(MtaasConstants.VERSION);
	    	
	    	//getting the Custom capability
    		String isEmulator = testParams.get(MtaasConstants.IS_EMULATOR);
    		boolean isEmulatorBoolean = Boolean.valueOf(isEmulator);
    		int isEmulatorInt = Util.getIntEquivalentOfBoolean(isEmulatorBoolean);
    		
    		String manufacturer = testParams.get(MtaasConstants.MANUFACTURER);
    		String model = testParams.get(MtaasConstants.MODEL);
	    	
	    	String gridUrl = testParams.get(MtaasConstants.HUB_URL);
	    	int hubId = HubInfoDAO.getGridId(deviceName,gridUrl);
	    	
	    	int deviceId = TestExecutionDAO.getDeviceId(deviceName, version, hubId,isEmulatorInt,manufacturer,model);
	    	
//	    	String suitekey = testContext.getSuite().getName() + MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + deviceId;
	    	String suiteName = testContext.getSuite().getName();
	    	String suiteKey = Util.buildSuiteMapperKey(suiteName, deviceId);
	    	log.info("Key of suiteListernerMap : " + suiteKey );
//	    	int suiteExecutionId = SuiteListener.testSuiteMapper.get(suitekey);
	    	int suiteExecutionId = testSuiteMap.get(suiteKey);
	    	
	    	log.info("value of suiteListernerMap : " + suiteExecutionId );
	    	
	    	if (suiteExecutionId > 0){
		    	log.info("suiteId from suiteListenerMap is:" + suiteExecutionId);
		    	
		    	TestResult inProgress = TestResult.getValue(16);
		    	
				if(deviceId > 0){
					TestExecution testExecution = buildTestExecution(testId,suiteExecutionId,deviceId,executionStartTime,inProgress);
					return TestExecutionDAO.insertTestExecution(testExecution);
				}
				else{
					log.info("TestExecutionUpdater. Deviceid not present in device table.Device-Version" + deviceName + "-" + version);
				}
		    }
	    	else{
	    		log.info("TestExecutionUpdater. suiteExecutionId not present in suitelister map. suiteKey:" + suiteKey);
	    	}
		}
		else{
			log.info("TestExecutionUpdater. Test name not found in table Test. Test = " + testContext.getName());
		}
		
		return -1;
	}
	
	private TestExecution buildTestExecution(int testId, int testSuiteId, int deviceId, Timestamp executionStartTime, TestResult testResult){
		TestExecution testExecution = new TestExecution();
		
		testExecution.setTestId(testId);
		testExecution.setDeviceId(deviceId);
		testExecution.setTestSuiteId(testSuiteId);
		testExecution.setExecutionStartTime(executionStartTime);
		testExecution.setStatus(testResult);
		
		return testExecution;
	}
	
	
}
