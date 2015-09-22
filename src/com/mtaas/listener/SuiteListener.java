package com.mtaas.listener;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlTest;

import com.mtaas.bean.AutDetails;
import com.mtaas.db.AUTDetailDAO;
import com.mtaas.db.RequestDAO;
import com.mtaas.db.TestSuiteExecutionResultUpdater;
import com.mtaas.db.TestSuiteExecutionUpdater;
import com.mtaas.testRunner.TestNGRunner;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.Util;

public class SuiteListener implements ISuiteListener {
	
	private static Logger log = Logger.getLogger(TestNGRunner.class.getName());
	
//	public static ConcurrentMap<String,Integer> testSuiteMapper = new ConcurrentHashMap<String,Integer>();
	
	BlockingQueue<Boolean> suiteResultUpdaterQueue = null;
	BlockingQueue<Map<String,Integer>> testSuiteMapperNotifier = null;
	
	private int requestId = 0;
	
	public SuiteListener(int requestId){
//		suiteResultUpdaterQueue = new LinkedBlockingQueue<Boolean>();
    	this.suiteResultUpdaterQueue = new LinkedBlockingQueue<Boolean>();
    	log.info("end of SuiteListener constructor" );
    	this.requestId = requestId;
    }
    
    public BlockingQueue<Boolean> getSuiteResultUpdaterQueue(){
    	return suiteResultUpdaterQueue;
    }
    
//    BlockingQueue<ConcurrentMap<String,Integer>> testSuiteMapperNotifier 
    public void setSuiteMapperNotifierQueue(BlockingQueue<Map<String,Integer>> testSuiteMapperNotifier){
    	log.info("setSuiteMapperNotifierQueue. setting testSuiteMapperNotifier" );
    	this.testSuiteMapperNotifier = testSuiteMapperNotifier;
    }
    
	@Override
    public void onStart(ISuite suite){
    	log.info("On suite Start");
    
		Timestamp executionStartTime = Util.getSQLCurrentTimeStamp();
		//insert into AUTDetail table
		AutDetails autDetail = buildAutDetail(suite);
		int autDetailId = 0;
		if (autDetail != null){
			autDetailId = AUTDetailDAO.insertAUTDetail(autDetail);
		}
		else
			log.error("Error in parsing the suite parameters to get the autDetails value. See log above and method buildAutDetail()");
			
		//insert into the Request table, and get the id
		int reqId = 0;
		if (requestId > 0){
			reqId = RequestDAO.insertRequest(executionStartTime, autDetailId,requestId);
		}
		else{
			reqId = RequestDAO.insertRequest(executionStartTime, autDetailId,0);
		}
    	
    	TestSuiteExecutionUpdater testSuiteExecutionUpdater = new TestSuiteExecutionUpdater(null);
    	testSuiteExecutionUpdater.setSuiteMapperNotifierQueue(testSuiteMapperNotifier);
    	testSuiteExecutionUpdater.updateTestSuiteExecution(suite,executionStartTime, reqId);
    }
    
	/*This method returns Application Under Test (AUT) object. Through we get all the test inside a suite, all test will have
	 * the same aut parameters passed during testNG xml construction. This is why we return AutDeatils object in 
	 * the first value of suite.getXmlSuite().getTests()
	 *
	 */
    private AutDetails buildAutDetail(ISuite suite) {
    	log.info("In suitelistener.buildAutDetail()");
    	AutDetails autDetail = new AutDetails();
    	for (XmlTest test: suite.getXmlSuite().getTests() ){
    		Map<String, String> testParams = test.getAllParameters();
    		String appActivity = testParams.get(MtaasConstants.APP_ACTIVITY);
    		String packageName = testParams.get(MtaasConstants.PACKAGE_NAME);
    		String appName = testParams.get(MtaasConstants.APP_NAME);
    		
    		log.info("App activity: " + appActivity);
    		log.info("Package Name: " + packageName);
    		log.info("App Name :" + appName);
    		
    		autDetail.setAppActivity(appActivity);
    		autDetail.setPackageName(packageName);
    		autDetail.setAppName(appName);
    		
    		return autDetail;
    	}
    	return null;
	}

	@Override
    public void onFinish(ISuite suite){
    	log.info("On suite end");
    	
    	Timestamp executionEndTime = Util.getSQLCurrentTimeStamp();
    	
    	boolean canSuiteResultUpdate = false;
		try {
			log.info("OnFinish of suite. Going into loop of queue take");
			canSuiteResultUpdate = suiteResultUpdaterQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (canSuiteResultUpdate){
			Map<String, Integer> testSuiteMap = null;
			try {
				log.info("taking from testSuiteMapperNotifier, this should not cause any delay since this queue is already populated in OnStart() method");
				testSuiteMap = testSuiteMapperNotifier.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	TestSuiteExecutionResultUpdater testSuiteExecutionResultUpdater = new TestSuiteExecutionResultUpdater(suite, 
	    			executionEndTime,testSuiteMap);
	    	testSuiteExecutionResultUpdater.run();
		}
    }
}
