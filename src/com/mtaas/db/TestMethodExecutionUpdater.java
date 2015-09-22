package com.mtaas.db;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.testng.ITestResult;

import com.mtaas.bean.TestMethodExecution;
import com.mtaas.client.RestClient;
import com.mtaas.testRunner.TestNGRunner;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.MtaasConstants.TestResult;

public class TestMethodExecutionUpdater{
	
	private static Logger log = Logger.getLogger(TestNGRunner.class.getName());
	
	public int updateTestMethodExecution(ITestResult result, int testExecutionId, String deviceName, 
			String version,int hubId,int isEmulator,String manufacturer, String model, Timestamp ExecutionStartTime){
		int deviceId = TestExecutionDAO.getDeviceId(deviceName,version,hubId,isEmulator,manufacturer,model);
		
		if(deviceId > 0){
			int testMethodId = TestExecutionDAO.getUniqueId("TestMethod", "id", "name", result.getName());
			TestResult inProgress = TestResult.getValue(16);
			
			TestMethodExecution testMethodExecutionBean = buildTestExecution(testMethodId,testExecutionId, deviceId,ExecutionStartTime,inProgress);
			int testMethodExecutionId = TestExecutionDAO.insertTestMethodExecution(testMethodExecutionBean);
			
			String appiumIp = DeviceInfoDAO.getAppiumIpFromDeviceId(deviceId);
			if (appiumIp != null){
				log.info("Appium Ip got is : " + appiumIp);
				String endPoint = appiumIp + MtaasConstants.PUT_TEST_METHOD_EXEC_ID + testMethodExecutionId; 
//				String endPoint = "http://localhost:8080/RestfulAPIs/hubManager/testMethodExecId/" + testMethodExecutionId;
				log.info("End point to send testMethodExecId : " + endPoint);
				RestClient.sendMethodExecutioinIdForOperationTracking(endPoint);
			}
			else{
				log.error("TestMethodExecutionUpdater.updateTestMethodExecution(). AppiumIp return as null for deviceId: " + deviceId);
			}
			return testMethodExecutionId;
		}
		else{
			log.info(String.format("updateTestMethodExecution. Deviceid not found for device= %s and version= %s",deviceName,version));
		}
		
		return -1;
	}
	
	private TestMethodExecution buildTestExecution(int testMethodId, int testExecutionId, int deviceId, Timestamp executionStartTime, TestResult testResult ){
		TestMethodExecution testMethodExecution = new TestMethodExecution();
		
		testMethodExecution.setTestMethodId(testMethodId);
		testMethodExecution.setTestExecutionId(testExecutionId);
		testMethodExecution.setDeviceId(deviceId);
		testMethodExecution.setExecutionStartTime(executionStartTime);
		testMethodExecution.setStatus(testResult);
		
		return testMethodExecution;
	}
}
