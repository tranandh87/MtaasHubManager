package com.mtaas.tracking;

import java.util.List;

import org.apache.log4j.Logger;

import com.mtaas.client.RestClient;
import com.mtaas.db.TestExecutionResultDAO;
import com.mtaas.tracking.bean.OperationalTracking;
import com.mtaas.util.MtaasConstants;

public class TrackingTestMethodUpdater extends Thread {
	
	private static Logger log = Logger.getLogger(TrackingTestMethodUpdater.class.getName());
	
	private int testMethodExecutionId = 0;
	private OperationalTracking operationalTracking = null;
	
	public TrackingTestMethodUpdater(int testMethodExecutionID,OperationalTracking operationalTracking){
		log.info("In tracking test method constructor");
		this.testMethodExecutionId = testMethodExecutionID;
		this.operationalTracking = operationalTracking;
	}
	
	@Override
	public void run(){
		updateTestMethodTracking();
	}
	
	public void updateTestMethodTracking() {
		
//		operationalTracking = getOperationTrackingFromNode();
		if (null != operationalTracking){
			log.info("Operation Tracking : " + operationalTracking);
			
			List<Integer> operationTrackingId = TrackingTestMethodDAO.updateTestMethodTracking(testMethodExecutionId, operationalTracking);
			
			for (Integer trackingId : operationTrackingId){
				log.info("Tracking id inserted: " + trackingId);
			}
		}
		else
			log.error(String.format("Operational Tracking for test method of TestMethodExecutionID = %d "
					+ "failed since no file received after trying %d many times",testMethodExecutionId,MtaasConstants.LOOP_COUNTER_TEST_METHOD_TRACKING));
	}
	
	private OperationalTracking getOperationTrackingFromNode(){
		boolean isNodeReturnedFile = false;
		OperationalTracking operationalTracking = null;
		String appiumIp = TestExecutionResultDAO.getAppiumIpFromTestMethodExecutionResult(testMethodExecutionId);
		
		if (null != appiumIp){
			appiumIp += MtaasConstants.NODE_TEST_METHOD_TRACE_END_POINT;
			int loopCounterForTestMethodTracking = 0;
	    	while(!isNodeReturnedFile){
	    		try {
	    			loopCounterForTestMethodTracking++;
	//    			String hubUrl = "http://localhost:4723/wd/hub/fetchTestMethodOpTrace";
	    			Thread.sleep(1000);
	    			
	    			if (loopCounterForTestMethodTracking >= MtaasConstants.LOOP_COUNTER_TEST_METHOD_TRACKING){
	    				isNodeReturnedFile = true;
	    			}
	
	    			operationalTracking = RestClient.getOperationalTracking(appiumIp);
	    			
	    			if (null != operationalTracking){
	    				log.info("success...");
	    				isNodeReturnedFile = true;
	    			}
	    			else{
	    				//Thread.sleep(500);
	    				log.info("Failed...");
	    			}
	    		}
	    		catch (Exception e) {
	    			isNodeReturnedFile = true;
					e.printStackTrace();
			    }
	    	}
		}
    	else{
			log.error("no appium url found in db. Check query and db");
			isNodeReturnedFile = true;
		}
    	
    	return operationalTracking;
	}
	
}
