package com.mtaas.db;

import java.sql.Connection;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.testng.ITestResult;

import com.mtaas.util.MtaasConstants.TestResult;
import com.mtaas.util.Util;

public class TestMethodExecutionResultUpdater extends Thread {
	
	private static Logger log = Logger.getLogger(TestMethodExecutionResultUpdater.class.getName());
	
	private ITestResult testNGResult = null;
	
	private int testMethodExecutionId = 0;
	private Timestamp testExecutionSqlEndTime = null;
	
	public TestMethodExecutionResultUpdater(ITestResult testNGResult,int testMethodExecutionID,long endTime){
		this.testNGResult = testNGResult;
		this.testMethodExecutionId = testMethodExecutionID;
		this.testExecutionSqlEndTime = Util.getSQLCurrentTimeStamp(endTime);
	}
	
	@Override
	public void run(){
		updateTestMethodExecutionResult();
	}
	
	private void updateTestMethodExecutionResult(){
		TestResult testResult = TestResult.getValue(testNGResult.getStatus());
		int affectedRows = TestExecutionResultDAO.updateExecutionResult("TestMethodExecutionResult",testMethodExecutionId,testExecutionSqlEndTime,testResult);
		if (affectedRows == 1){
			log.info("Test Result updated Successfully: " + testNGResult.getName());
		}
	}
}
