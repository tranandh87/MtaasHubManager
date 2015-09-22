package com.mtaas.testRunner;

import com.mtaas.bean.TestExecutionRequest;
import com.mtaas.factory.TestRunnerFactory;
import com.mtaas.util.MtaasConstants.FailedTestRerunRequestType;

public class BackgroundTestExecutor extends Thread {
	TestExecutionRequest request = null;
	int requestId = 0;
	FailedTestRerunRequestType failedTestRerunRequestTyp = null;
	
	BackgroundTestExecutor(TestExecutionRequest request,int requestId,FailedTestRerunRequestType failedTestRerunRequestType){
		this.request = request;
		this.requestId = requestId;
		this.failedTestRerunRequestTyp = failedTestRerunRequestType;
	}
	
	@Override
	public void run(){
		System.out.println("BackgroundTestExecutor starting");
		MtaasTestRunner runner = TestRunnerFactory.getTestRunner(request,requestId,failedTestRerunRequestTyp);
		runner.startTest();
	}
}
