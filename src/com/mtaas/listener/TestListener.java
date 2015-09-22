package com.mtaas.listener;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.mtaas.db.HubInfoDAO;
import com.mtaas.db.TestExecutionResultUpdater;
import com.mtaas.db.TestExecutionUpdater;
import com.mtaas.db.TestInfoDAO;
import com.mtaas.db.TestMethodExecutionResultUpdater;
import com.mtaas.db.TestMethodExecutionUpdater;
import com.mtaas.testRunner.TestNGRunner;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.Util;

public class TestListener extends TestListenerAdapter{
	
	
    private int m_count = 0;
    private static Logger log = Logger.getLogger(TestNGRunner.class.getName());
    
    private ConcurrentMap<String,TestMethodInfo> testMethodExecutionMapper = new ConcurrentHashMap<String,TestMethodInfo>();
    private ConcurrentMap<String,Integer> testExecutionMapper = new ConcurrentHashMap<String,Integer>();

    BlockingQueue<Boolean> suiteResultUpdaterQueue = null;
    BlockingQueue<Map<String,Integer>> testSuiteMapperNotifier = null;
    
    public TestListener(BlockingQueue<Boolean> suiteResultUpdaterQueue){
    	this.suiteResultUpdaterQueue = suiteResultUpdaterQueue;
    	this.testSuiteMapperNotifier = new LinkedBlockingQueue<Map<String,Integer>>();
    	log.info("end of TestListener constructor" );
    }
    
    public BlockingQueue<Map<String,Integer>> getTestSuiteMapperNotifier(){
    	return testSuiteMapperNotifier;
    }
    
    boolean initialSetUpDone = false;
    
   // @Override
    /**
     * This method is called twice for each test method (one before setUp(), one before tearDown())
     */
    /*public void beforeConfiguration(ITestResult itr){
    	super.beforeConfiguration(itr);
    	boolean isFailedTest = !itr.getTestContext().getFailedTests().getAllMethods().isEmpty();
    	boolean isSkippedTest = !itr.getTestContext().getSkippedTests().getAllMethods().isEmpty();
    	boolean isPassedTest = !itr.getTestContext().getPassedTests().getAllMethods().isEmpty();
    	
    	boolean isTestRan = isFailedTest || isSkippedTest || isPassedTest;
    	log.info("Before Configuration: isTestRan=" + isTestRan);
    
		if (!isTestRan) {
			//we only want to get into here if this is the first time the configuration is done.
			String deviceName = (String) itr.getParameters()[0];
			String version = (String) itr.getParameters()[2];

			log.info("Device : " + deviceName);
			log.info("Version : " + version);
		}
    }*/
    
    @Override
    public void onConfigurationSuccess(ITestResult itr){
    	super.onConfigurationSuccess(itr);
    	log.info("configuration success");
    	
    	boolean isFailedTest = !itr.getTestContext().getFailedTests().getAllMethods().isEmpty();
    	boolean isSkippedTest = !itr.getTestContext().getSkippedTests().getAllMethods().isEmpty();
    	boolean isPassedTest = !itr.getTestContext().getPassedTests().getAllMethods().isEmpty();
    	
    	boolean isTestRan = isFailedTest || isSkippedTest || isPassedTest;
    	log.info("On Configuration Success: isTestRan=" + isTestRan);
    	
    	if (!isTestRan)
    		updateTestMethods(itr);
    }
    
    @Override
    public void onConfigurationFailure(ITestResult itr){
    	super.onConfigurationFailure(itr);
    	log.error("testMethod:" + itr.getName());
    	log.error("error", itr.getThrowable());
    	
    }

    @Override
    public void onStart(ITestContext testContext){

    	super.onStart(testContext);
    	log.info("On test start: TestName: " + testContext.getName());
    	
    	Timestamp executionStartTime = Util.getSQLCurrentTimeStamp();

    	Map<String,Integer> testSuiteMap = null;
//		try {
			log.info("OnStart of Test in TestListerner. Going into loop of queue take. This should wait if this is "
					+ "executing before testSuiteExecutionUpdater.updateTestSuiteExecution else this should not"
					+ "have any delay. Most likely NO DELAY");
//			testSuiteMap = testSuiteMapperNotifier.take();
			testSuiteMap = testSuiteMapperNotifier.peek();
		/*} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
    	TestExecutionUpdater testExecutionUpdater = new TestExecutionUpdater(testSuiteMap);
    	log.info("Test start: Clearing suiteResultUpdaterQueue");
    	suiteResultUpdaterQueue.clear();
    	int testExecutionId = testExecutionUpdater.updateTestExecution(testContext,executionStartTime);
    	
    	log.info("TestExecutionMapper. Key(testName): " + testContext.getName() + ". Value (testExecutionId): " + testExecutionId); 
    	testExecutionMapper.put(testContext.getName(), testExecutionId);
		
    }
    
    @Override
    public void onFinish(ITestContext testContext){
    	log.info("on finish of test class. Test class: " + testContext.getName());
    	Timestamp executionEndTime = Util.getSQLCurrentTimeStamp();
    	int testExecutionId = testExecutionMapper.get(testContext.getName());
    	
    	if (testExecutionId > 0){
	    	TestExecutionResultUpdater testExecutionResultUpdater = new TestExecutionResultUpdater(testContext,testExecutionId,
	    			executionEndTime,suiteResultUpdaterQueue);
	    	testExecutionResultUpdater.start();
    	}
    	else{
    		log.error(String.format("onFinish: Not able to find value for key: %s in temporary hashmap for test table",testContext.getName()));
    	}
    }
    
    @Override
    public void onTestFailure(ITestResult tr) {
    	super.onTestFailure(tr);
        log(tr.getName()+ "--Test method failed\n");
        
        updateTestResult(tr);
    }
	 
    @Override
    public void onTestSkipped(ITestResult tr) {
    	super.onTestSkipped(tr);
        log(tr.getName()+ "--Test method skipped\n");
        
        updateTestResult(tr);
    }
	 
    @Override
    public void onTestSuccess(ITestResult tr) {
    	super.onTestSuccess(tr);
        log(tr.getName()+ "--Test method success\n");
        
        updateTestResult(tr);
    }
    
    
    @Override
    public void onTestStart(ITestResult result) {
    	
    	log.info("In test method OnStart Method");
    	
    	super.onTestStart(result);
    	Timestamp executionStartTime = Util.getSQLCurrentTimeStamp();
    	
    	String testMethodName = result.getName();
		log.info("On Test Start: MethodName " + testMethodName);

    	String testName = result.getTestContext().getName();
    	log.info("On Test start: TestName " + testName);
    	
    	String key = Util.concatinateByMtaasTestNameSeperator(testName, testMethodName);
    	TestMethodInfo testMethodInfo = testMethodExecutionMapper.get(key);
    	
    	int testExecutionId = testExecutionMapper.get(testName);
    	
    	if (null != testMethodInfo){
			String deviceName = testMethodInfo.getDevice();
	    	String version = testMethodInfo.getVersion();
	    	
	    	//getting custom capability
	    	int isEmulator = testMethodInfo.getIsEmulator();
	    	String manufacturer = testMethodInfo.getManufacturer();
	    	String model = testMethodInfo.getModel();
	    	
	    	String gridUrl = testMethodInfo.getHubUrl();
	    	int hubId = HubInfoDAO.getGridId(deviceName,gridUrl);
	    	
	    	TestMethodExecutionUpdater testMethodExecutionUpdater= new TestMethodExecutionUpdater();
	    	int testMethodExecutionId = testMethodExecutionUpdater.updateTestMethodExecution(result, testExecutionId, 
	    			deviceName, version,hubId, isEmulator,manufacturer,model,executionStartTime);

	    	testMethodInfo.setTestMethodExecutionId(testMethodExecutionId);
	    	
//	    	testExecutionUpdater.start();
    	}
    	else{
    		log.error(String.format("onTestStart: Not able to find value for key: %s in temporary hashmap",testName));
    	}
    }
    
    private void log(String string) {
        System.out.print(string);
        if (++m_count % 40 == 0) {
	    System.out.println("");
        }
    }
    
    private class TestMethodInfo{
    	String device = null;
    	String version = null;
    	String hubUrl = null;
    	int isEmulator = 0;
    	String manufacturer = null;
    	String model = null;
		int testMethodExecutionId = 0;

		public String getHubUrl() {
			return hubUrl;
		}
		public void setHubUrl(String gridUrl) {
			this.hubUrl = gridUrl;
		}
		
    	public String getDevice() {
			return device;
		}
		public void setDevice(String device) {
			this.device = device;
		}
		
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		
		public int getTestMethodExecutionId() {
			return testMethodExecutionId;
		}
		public void setTestMethodExecutionId(int testMethodExecutionId) {
			this.testMethodExecutionId = testMethodExecutionId;
		}
		
		public int getIsEmulator() {
			return isEmulator;
		}
		public void setIsEmulator(int isEmulator) {
			this.isEmulator = isEmulator;
		}
		
		public String getManufacturer() {
			return manufacturer;
		}
		public void setManufacturer(String manufacturer) {
			this.manufacturer = manufacturer;
		}
		
		public String getModel() {
			return model;
		}
		public void setModel(String model) {
			this.model = model;
		}
    }
    
    private void updateTestMethods(ITestResult testResult){
    	
    	String testName = testResult.getTestContext().getName();
    	
    	String actualTestName = (Util.getActualTestName(testName, MtaasConstants.MTAAS_TEST_NAME_SEPERATOR)); 
    	for (ITestNGMethod method: testResult.getTestContext().getAllTestMethods()){
	    	String testMethodName = method.getMethodName();
			
			TestInfoDAO.updateTestMethod(actualTestName,testMethodName);
			populateTestMethodExecutionMapper(testResult,testName,testMethodName);
    	}	
    }
    
    private void populateTestMethodExecutionMapper(ITestResult testResult,String testName, String testMethodName){
    	String key = Util.concatinateByMtaasTestNameSeperator(testName,testMethodName);
    	
    	/**Expect during the first method call in a particular test class, the configuration parameter is null
    	 * 
    	 */
    	if (null == testMethodExecutionMapper.get(key)){
	    	TestMethodInfo testMethodInfo = buildTestMethodInfo(testResult);
	    	testMethodExecutionMapper.put(key, testMethodInfo);
	    	log.info(String.format("testMethodExecutionMapper. Key = %s, value = %s ",key,testMethodInfo.toString() + ". See before log for key."));
    	}
    }
    
    private TestMethodInfo buildTestMethodInfo(ITestResult testResult){
    	TestMethodInfo testMethodInfo = new TestMethodInfo();
    	
    	Map<String, String> testParams = testResult.getTestClass().getXmlTest().getAllParameters();
		String deviceName = testParams.get(MtaasConstants.DEVICE);
		String version = testParams.get(MtaasConstants.VERSION);
		
		//getting the Custom capability
		String isEmulator = testParams.get(MtaasConstants.IS_EMULATOR);
		boolean isEmulatorBoolean = Boolean.valueOf(isEmulator);
		int isEmulatorInt = Util.getIntEquivalentOfBoolean(isEmulatorBoolean);
		
		String manufacturer = testParams.get(MtaasConstants.MANUFACTURER);
		String model = testParams.get(MtaasConstants.MODEL);
		
		String hubUrl = testParams.get(MtaasConstants.HUB_URL);
		
    	testMethodInfo.setDevice(deviceName);
    	testMethodInfo.setVersion(version);
    	testMethodInfo.setHubUrl(hubUrl);
    	
    	//setting custom capabilities
    	testMethodInfo.setIsEmulator(isEmulatorInt);
    	testMethodInfo.setManufacturer(manufacturer);
    	testMethodInfo.setModel(model);
    	
    	log.info(String.format("Current Device config in buildTestMethodInfo. Device = %s, version = %s, isEmulatorInt = %d, manufacturer = %s, model = %s", 
				deviceName,version,isEmulatorInt,manufacturer,model));
    	
    	return testMethodInfo;
    }
    
    private void updateTestResult(ITestResult testResult){
    	
        String testMethodName = testResult.getName();
		log.info("On Test Result: MethodName " + testMethodName);

    	String testName = testResult.getTestContext().getName();
    	log.info("On Test Result: TestName " + testName);
    	
    	String key = Util.concatinateByMtaasTestNameSeperator(testName, testMethodName);
    	TestMethodInfo testMethodInfo = testMethodExecutionMapper.get(key);
    	
    	if (null != testMethodInfo){
	    	Date date= new Date();
	    	
	    	long executionEndTime = date.getTime();
	    	int testMethodExecutionId = testMethodInfo.getTestMethodExecutionId();
	    	
	    	TestMethodExecutionResultUpdater testMethodExecutionResultUpdater = new TestMethodExecutionResultUpdater(testResult,testMethodExecutionId,executionEndTime);
	    	testMethodExecutionResultUpdater.start();
	    	
//	    	log.info("In Operational Tracking");
	    	
	    	/*TrackingTestMethodUpdater trackingTestMethodUpdater = new TrackingTestMethodUpdater(testMethodExecutionId);
//	    	trackingTestMethodUpdater.start();
	    	trackingTestMethodUpdater.updateTestMethodTracking();*/
    	}
    	else{
    		log.error(String.format("updateTestResult: Not able to find value for key: %s in temporary hashmap",testName));
    	}
    }
}
