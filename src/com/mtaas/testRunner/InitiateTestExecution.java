package com.mtaas.testRunner;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.mtaas.bean.AutDetails;
import com.mtaas.bean.DeviceConfig;
import com.mtaas.bean.TestExecutionRequest;
import com.mtaas.bean.TestSuiteExecutionResponse;
import com.mtaas.db.RequestSuiteExecutionDAO;
import com.mtaas.db.TestSuiteExecutionUpdater;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.Util;
import com.mtaas.util.MtaasConstants.FailedTestRerunRequestType;

public class InitiateTestExecution implements ITestResponseListener {
	private static Logger log = Logger.getLogger(InitiateTestExecution.class.getName());
	TestExecutionRequest request = null;
//	ISuiteExecution suiteExecution = null;
	private BlockingQueue<TestSuiteExecutionResponse> suiteUpdaterQueue;
	
	public InitiateTestExecution(TestExecutionRequest request, BlockingQueue<TestSuiteExecutionResponse> suiteUpdaterQueue){
		log.info("In Initiate Test Execution");
		
		//registering listener for suite updater to call onInsertSuiteExecution() method
		new TestSuiteExecutionUpdater(this);
		
		//registering listener for TestNGRunner to call noGridAvailable() method
		new TestNGRunner(this);
		this.request = request;
		this.suiteUpdaterQueue = suiteUpdaterQueue;
		log.info("finishing Initiate Test Execution");
	}
	
	public void onInsertSuiteExecution(int suiteId, Set<Integer> suiteExecutionIds, int reqId){
		log.info("onInsertSuiteExecution. Insert to suite table listener. suiteId: " + suiteId);
		for (Integer suiteExecId: suiteExecutionIds){
			log.info("onInsertSuiteExecution. Suite Execution Ids:" + suiteExecId);
		}
		
		try {
			TestSuiteExecutionResponse testSuiteExecutionResponse = new TestSuiteExecutionResponse();
			testSuiteExecutionResponse.setIsRequestPassed(true);
			testSuiteExecutionResponse.setExecutionRequestId(reqId);
			testSuiteExecutionResponse.setSuiteId(suiteId);
			testSuiteExecutionResponse.setMessage("Test is build successfully");
			
			suiteUpdaterQueue.put(testSuiteExecutionResponse);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void noGridAvailable(String message){
		log.info("no grid available returned from TestNGRunner");
		TestSuiteExecutionResponse testSuiteExecutionResponse = new TestSuiteExecutionResponse();
		testSuiteExecutionResponse.setIsRequestPassed(false);
		testSuiteExecutionResponse.setMessage(message);
		
		try {
			suiteUpdaterQueue.put(testSuiteExecutionResponse);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String runTest(int requestId, FailedTestRerunRequestType failedTestRerunRequestType){
		if (validateRequest()){
			BackgroundTestExecutor testRunner = new BackgroundTestExecutor(request,requestId,failedTestRerunRequestType);
			testRunner.start();
			return request.toString();
		}
		else{
			log.info("request body contains invalid data");
			TestSuiteExecutionResponse testSuiteExecutionResponse = new TestSuiteExecutionResponse();
			testSuiteExecutionResponse.setIsRequestPassed(false);
			testSuiteExecutionResponse.setMessage("request body contains invalid data");
			
			try {
				suiteUpdaterQueue.put(testSuiteExecutionResponse);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "request body contains invalid data";
			
		}
	}
	
	public boolean validateRequest(){
		if (checkDeviceConfig() &&
			checkAutDetails() &&
			checTestSuite())
			return true;
		else{
			return false;
		}
	}
	
	private boolean checkDeviceConfig(){
		List<DeviceConfig> reqDeviceConfig = request.getDeviceConfig();
		if (!(reqDeviceConfig.size() > 0)){
			log.info("there is not Device config in request so exiting the test run");
			return false;
		}
		for (DeviceConfig deviceConfig: reqDeviceConfig){
			if (!MtaasConstants.SUPPORTED_DEVICES.contains(deviceConfig.getDevice())){
				return false;
			}
			/*if (!MtaasConstants.SUPPORTED_VERSIONS.contains(deviceConfig.getVersion())){
				return false;
			}*/
		}
		return true; 
	}
	
	private boolean checkAutDetails(){
		AutDetails reqAutDetails = request.getAutDetails();
		if (Util.isNonEmptyString(reqAutDetails .getAppActivity()) &&
			Util.isNonEmptyString(reqAutDetails.getPackageName())) //&&
//			Util.isNonEmptyString(reqAutDetails.getAppName()))
			return true;
		else
			return false;
	}
	
	private boolean checTestSuite(){
		if (!(request.getTestSuite().getClassName().size() > 0))
			return false;
		else
			return true;
		
	}
}


