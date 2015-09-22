package com.mtaas.db;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.mtaas.bean.TestExecution;
import com.mtaas.bean.TestMethodExecution;
import com.mtaas.bean.TestSuiteExecution;
import com.mtaas.listener.SuiteListener;
import com.mtaas.testRunner.TestNGRunner;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.MtaasConstants.TestResult;
import com.mtaas.util.Util;

public class TestSuiteExecutionResultUpdater extends Thread {
	
	private static Logger log = Logger.getLogger(TestSuiteExecutionResultUpdater.class.getName());
	
	private ISuite suite = null;
	
	private Timestamp testExecutionSqlEndTime = null;
	private Map<String,Integer> testSuiteMap = null;
	
	public TestSuiteExecutionResultUpdater(ISuite suite,Timestamp testExecutionEndTime,Map<String,Integer> testSuiteMap){
		this.suite = suite;
		this.testExecutionSqlEndTime = testExecutionEndTime;
		this.testSuiteMap = testSuiteMap;
	}
	
	@Override
	public void run(){
		updateTestSuiteExecutionResult();
	}
	
	/**
	 * Based on number of different device in a test suite, this method updates the  
	 * @param conn
	 */
	private void updateTestSuiteExecutionResult(){
		
		String suiteName = suite.getName();
		
		Set<Integer> hubIds = new HashSet<Integer>();
		
    	for (Map.Entry<String, Integer> entry: TestSuiteExecutionUpdater.getUniqueDeviceIdOfSuite(suite).entrySet()){
    		log.info("updateTestSuiteExecutionResult. Device map key -> value: " + entry.getKey() + " -> " + entry.getValue() );
    		int deviceId = entry.getValue();
    		
    		String suiteMapperKey = Util.buildSuiteMapperKey(suiteName, deviceId);
    		int suiteExecutionId = testSuiteMap.get(suiteMapperKey);
    		log.info(String.format("suiteExecutionId: %s for suite name %s in testSuiteMapper", suiteExecutionId,suiteMapperKey));

    		TestResult testResult = TestExecutionResultDAO.getSuiteResult(suiteExecutionId);
    		if (testResult.name() != null){
    			log.info("Test suite Result: " + testResult.name());
    		}
    		else{
    			log.error("TestExecutionResultDAO.getSuiteResult returned NULL. This means that either of the entry for" +
    					" TestExecutionResult for this specific suiteId is in not finished because of error");
    		}
    		
    		/*try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
    		int affectedRows = TestExecutionResultDAO.updateExecutionResult("TestSuiteExecutionResult",suiteExecutionId,testExecutionSqlEndTime,testResult);
    		if (affectedRows == 1){
    			log.info("Test suite updated Successfully: " + suite.getName() + ". Device id:" + deviceId);
    		}
    		
    		int hubId = DeviceInfoDAO.getHubIdFromDeviceId(deviceId);
    		if (hubId > 0)
    			hubIds.add(hubId);
    		else
    			log.error("updateTestSuiteExecutionResult. Error while returning hubId from Device table using deviceId. check db");
    	}
    	
    	for (Integer hubId: hubIds){
    		HubInfoDAO.updateHubStatusWithHubId(hubId, MtaasConstants.HubOrDeviceStatus.AVAILABLE);
    	}
    	
	}
}
