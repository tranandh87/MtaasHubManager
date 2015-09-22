package com.mtaas.testRunner;

import org.apache.log4j.Logger;

import com.mtaas.bean.TestExecutionRequest;
import com.mtaas.util.MtaasConstants.FailedTestRerunRequestType;

public abstract class MtaasTestRunner {
	private static Logger log = Logger.getLogger(MtaasTestRunner.class.getName());
	
	TestExecutionRequest request = null;
	int requestId = 0;
	FailedTestRerunRequestType failedTestRerunRequestType = null;
	public MtaasTestRunner(TestExecutionRequest request,int requestId,FailedTestRerunRequestType failedTestRerunRequestType){
		this.request = request;
		this.requestId = requestId;
		this.failedTestRerunRequestType = failedTestRerunRequestType;
	}
	
	/**
	 * use this template method since the database logging is the same regardless of the
	 * test runner type.
	 */
	public void startTest(){
		logTestExecutionStart();
		runTest();
		logTestExecutionEnd();
	}
	
	private void logTestExecutionEnd() {
		log.info("test suite execution ended");
	}

	private void logTestExecutionStart() {
		log.info("test suite execution started");
	}

	public abstract void runTest();
}
