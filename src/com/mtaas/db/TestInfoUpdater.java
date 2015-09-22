package com.mtaas.db;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.seleniumhq.jetty7.util.log.Log;

import com.mtaas.bean.TestExecution;
import com.mtaas.bean.TestExecutionRequest;
import com.mtaas.util.Util;

public class TestInfoUpdater extends Thread {
	TestExecutionRequest request = null;
	private static Logger log = Logger.getLogger(TestInfoUpdater.class.getName());
	
	public TestInfoUpdater(TestExecutionRequest request){
		this.request = request;
	}
	
	@Override
	public void run(){
		log.info("TestInfoUpdater starting");
		if (null!=request){
			int suiteId = updateSuiteName();
			if (suiteId > 0)
				updateTestName(suiteId);
			else
				log.info("TestInfoUpdater.run(). Suite Id not created so test name is not created too");
		}	
	}
	
	private int updateSuiteName(){
		String suiteName = Util.getSuiteName(request); 
		
		return TestInfoDAO.updateSuiteName(suiteName);
	}
	
	private void updateTestName(int suiteId){
		for (String className: Util.getClassesName(request)){
			TestInfoDAO.updateTestName(className, suiteId);
		}
		
	}
}
