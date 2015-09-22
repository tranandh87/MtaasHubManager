package com.mtaas.factory;

import com.mtaas.bean.TestExecutionRequest;
import com.mtaas.testRunner.TestNGRunner;
import com.mtaas.testRunner.MtaasTestRunner;
import com.mtaas.util.MtaasConstants.FailedTestRerunRequestType;

public class TestRunnerFactory {
	public static MtaasTestRunner getTestRunner(TestExecutionRequest request,int reqeustId,FailedTestRerunRequestType failedTestRerunRequestType){
		
		//only use TestNGRunner for now, may expand to more later
		return new TestNGRunner(request,reqeustId,failedTestRerunRequestType);
	}
}
