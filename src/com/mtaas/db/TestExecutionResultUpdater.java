package com.mtaas.db;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.mtaas.bean.TestExecution;
import com.mtaas.bean.TestMethodExecution;
import com.mtaas.bean.TestSuiteExecution;
import com.mtaas.testRunner.TestNGRunner;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.MtaasConstants.TestResult;
import com.mtaas.util.Util;

public class TestExecutionResultUpdater extends Thread {
	
	private static Logger log = Logger.getLogger(TestExecutionResultUpdater.class.getName());
	
	private ITestContext testContext = null;
	private int testExecutionId = 0;
	private Timestamp testExecutionSqlEndTime = null;
	
	BlockingQueue<Boolean> suiteResultUpdaterQueue = null;
    
	public TestExecutionResultUpdater(ITestContext testContext,int tesExecutionID,Timestamp testExecutionEndTime, BlockingQueue<Boolean> suiteResultUpdaterQueue){
		this.testContext = testContext;
		this.testExecutionId = tesExecutionID;
		this.testExecutionSqlEndTime = testExecutionEndTime;
		this.suiteResultUpdaterQueue = suiteResultUpdaterQueue;
	}
	
	@Override
	public void run(){
		updateTestExecutionResult();
	}
	
	private void updateTestExecutionResult(){
		TestResult testResult = Util.getTestResult(testContext);
		int affectedRows = TestExecutionResultDAO.updateExecutionResult("TestExecutionResult",testExecutionId,testExecutionSqlEndTime,testResult);
		if (affectedRows == 1){
			log.info("Test Result updated Successfully: " + testContext.getName());
			try {
				log.info("In updateTestExecutionResult. Putting true to queue that will be taken by suite listerner by queue take method");
				suiteResultUpdaterQueue.put(true);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
