package com.mtaas.testRunner;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.mtaas.bean.AutDetails;
import com.mtaas.bean.DeviceConfig;
import com.mtaas.bean.TestExecutionRequest;
import com.mtaas.db.HubInfoDAO;
import com.mtaas.db.TestInfoUpdater;
import com.mtaas.db.TestMethodExecutionDAO;
import com.mtaas.listener.SuiteListener;
import com.mtaas.listener.TestListener;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.Util;
import com.mtaas.util.MtaasConstants.FailedTestRerunRequestType;
 
public class TestNGRunner extends MtaasTestRunner {
	
	
	/**
	 * Perform validation by:
	 * 	parse the list of DeviceConfig
	 * 	parse the testSuite to get the list of test class names
	 * 	parse the autDetails to get the packageName, appName, activity
	 * 
	 * if validation succeeds, creates the TestNG instance & runs the test in a separate thread and returns the
	 * response to user. (might implement a request queue later)
	 * @param req
	 */
	private static Logger log = Logger.getLogger(TestNGRunner.class.getName());
	
	public TestNGRunner(TestExecutionRequest request,int requestId,FailedTestRerunRequestType failedTestRerunRequestType){
		super(request,requestId,failedTestRerunRequestType);
	}
	
	private static ITestResponseListener noGridAvailableEventReceiver;
	public TestNGRunner(ITestResponseListener noGridAvailableEventReceiverParam){
		super(null,0,null);
		log.info("Setting noGridAvailableEventReceiver ");
		noGridAvailableEventReceiver = noGridAvailableEventReceiverParam;
	}
	
	public void runTest(){
		log.info("TestNGRunner.runTest() started");
		
		TestNG myTestNG = buildTestNG();
		
		if (null != myTestNG){
			
//			BlockingQueue<Boolean> suiteResultUpdaterQueue = null;
//			BlockingQueue<Map<String,Integer>> testSuiteMapperNotifier = null;
			
			SuiteListener suiteListener = new SuiteListener(requestId);
			BlockingQueue<Boolean> suiteResultUpdaterQueue = suiteListener.getSuiteResultUpdaterQueue();
			
			TestListener testListener = new TestListener(suiteResultUpdaterQueue);
			BlockingQueue<Map<String,Integer>> testSuiteMapperNotifier = testListener.getTestSuiteMapperNotifier();
			
			suiteListener.setSuiteMapperNotifierQueue(testSuiteMapperNotifier);
			
			log.info("checking whether suiteResultUpdaterQueue & testSuiteMapperNotifier is not NULL" );
			log.info("suiteResultUpdaterQueue : " + suiteResultUpdaterQueue);
			log.info("testSuiteMapperNotifier : " + testSuiteMapperNotifier);
			log.info("finish checking that suiteResultUpdaterQueue & testSuiteMapperNotifier is not NULL");

			myTestNG.addListener(suiteListener);
			myTestNG.addListener(testListener);
	
			myTestNG.run();
		}
//		listenTest(tla);
		
		/*Map<String, String> params = buildTestParams(request.getDeviceConfig().get(0));
		String className = request.getTestSuite().getClassName().get(0);
		runTestNGTest(params, className);*/
		
		log.info("TestNGRunner.runTest() finished");
	}
 
	private TestNG buildTestNG() {
		TestNG myTestNG = new TestNG();
		
		XmlSuite mySuite = new XmlSuite();
		mySuite.setParallel("tests");
		mySuite.setName(request.getTestSuite().getSuiteName());
		
		List<XmlTest> allTest = buildAllTests(mySuite);
		mySuite.setTests(allTest);
		
		if (allTest.size() <= 0)
			return null;
		
		List<XmlSuite> mySuites = new ArrayList<XmlSuite>();
		mySuites.add(mySuite);
		myTestNG.setXmlSuites(mySuites);
		log.info("buildTestNG. testNG built");

		return myTestNG;
	}

	private static void runTestNGTest(Map<String, String> params, String className) {
	 
	
	//Create an instance on TestNG
	 TestNG myTestNG = new TestNG();
	 
	//Create an instance of XML Suite and assign a name for it.
	 XmlSuite mySuite = new XmlSuite();
	 mySuite.setName(MtaasConstants.TEST_SUITE);
	 mySuite.setParallel("tests");
	 
	//Create an instance of XmlTest and assign a name for it.
	 XmlTest myTest = new XmlTest(mySuite);
//	 myTest.setName(params.get(MtaasConstants.TEST_NAME));
	 myTest.setName("sampleTest");
	
	//Add any parameters that you want to set to the Test.
	 myTest.setParameters(params);
	 
	//Create a list which can contain the classes that you want to run.
	 List<XmlClass> myClasses = new ArrayList<XmlClass> ();
	 myClasses.add(new XmlClass(MtaasConstants.TEST_LOCATION + className));
//	 myClasses.add(new XmlClass(className));
	 
	//Assign that to the XmlTest Object created earlier.
	 myTest.setXmlClasses(myClasses);
	 
	//Create a list of XmlTests and add the Xmltest you created earlier to it.
	 List<XmlTest> myTests = new ArrayList<XmlTest>();
	 myTests.add(myTest);
	 
	//add the list of tests to your Suite.
	 mySuite.setTests(myTests);
	 
	//Add the suite to the list of suites.
	 List<XmlSuite> mySuites = new ArrayList<XmlSuite>();
	 mySuites.add(mySuite);
	 
	//Set the list of Suites to the testNG object you created earlier.
	 myTestNG.setXmlSuites(mySuites);
	 
	//invoke run() - this will run your class.
	 myTestNG.run();
	 
	}
	
	private List<XmlTest> buildAllTests(XmlSuite xmlSuite){
		List<XmlTest> allTests = new ArrayList<XmlTest>();
		
		for (String className : request.getTestSuite().getClassName()){
			for (DeviceConfig deviceConfig: request.getDeviceConfig()){
				Map<String, String> testngParams = buildTestParams(deviceConfig);
				if (null != testngParams){
					XmlTest myTest = buildTest(testngParams,className, xmlSuite,testngParams); 
					log.info("DeviceConfig for current test  = " + deviceConfig);
					if (myTest != null){
						allTests.add(myTest);
						log.info("Test is build. XmlTest: " + myTest);
					}
					else {
						log.info("not added to allTest since myTest is null for className=" + className + ", deviceConfig=" + deviceConfig);
					}
				}
			}
		}
		
		return allTests;
	}
	
	private Map<String,String> buildTestParams(DeviceConfig deviceConfig){
		AutDetails autDetails = request.getAutDetails();
		Map<String, String> testngParams = new HashMap<String,String>();
		
		testngParams.put(MtaasConstants.DEVICE, deviceConfig.getDevice());
		testngParams.put(MtaasConstants.BROWSER, MtaasConstants.EMPTY_BROWSER_NAME);
		testngParams.put(MtaasConstants.VERSION, deviceConfig.getVersion());
		testngParams.put(MtaasConstants.PLATFORM, MtaasConstants.DEVICE_PLATFORM);
		testngParams.put(MtaasConstants.PACKAGE_NAME, autDetails.getPackageName());
		testngParams.put(MtaasConstants.APP_ACTIVITY, autDetails.getAppActivity());
		testngParams.put(MtaasConstants.APP_NAME, autDetails.getAppName());
		
		//custom capability
		boolean isEmulator = deviceConfig.getIsEmulator();
		if (isEmulator){
			testngParams.put(MtaasConstants.IS_EMULATOR, String.valueOf(isEmulator));
			testngParams.put(MtaasConstants.MANUFACTURER, MtaasConstants.EMULATOR_MANUFACTURER);
			testngParams.put(MtaasConstants.MODEL, MtaasConstants.EMULATOR_MODEL);
		}
		else{
			testngParams.put(MtaasConstants.IS_EMULATOR, String.valueOf(isEmulator));
			
			if (Util.isNonEmptyString(deviceConfig.getManufacturer())){
				testngParams.put(MtaasConstants.MANUFACTURER, deviceConfig.getManufacturer());
			}
			else{
				log.error("MANDATORY MANUFACTURER INFO FOR REAL DEVICE.For request with not a emulator device, manufacturer should be mentioined."
						+ "This request would error out with missing mandatory parameter in test class");
//				testngParams.put(MtaasConstants.MANUFACTURER, "");
			}
			
			if (Util.isNonEmptyString(deviceConfig.getModel())){
				testngParams.put(MtaasConstants.MODEL, deviceConfig.getModel());
			}
			else{
				log.error("MANDATORY MODEL INFO FOR REAL DEVICE.For request with not a emulator device, model should be mentioined."
						+ "This request would error out with missing mandatory parameter in test class");
//				testngParams.put(MtaasConstants.MODEL, "");
			}
		}
		
		String gridUrl = HubInfoDAO.selectHub(deviceConfig);
		
//		String gridUrl = GridInfoDAO.getGridUrl(conn, deviceConfig.getDevice(), deviceConfig.getVersion(), MtaasConstants.HubOrDeviceStatus.AVAILABLE);
		if (null != gridUrl){
			testngParams.put(MtaasConstants.HUB_URL, gridUrl);
			 
			updateSuiteAndTestTable();
			
			return testngParams; 
		}
		else{
			log.info(String.format("buildTestParams(). There is no hub matching the device configuration requested. DeviceName= %s Version= %s",
					deviceConfig.getDevice(),deviceConfig.getVersion()));
			if (null != noGridAvailableEventReceiver){
				String noGridMessage = String.format("There is no hub matching the device configuration requested. DeviceConfig = %s",
						deviceConfig);
				noGridAvailableEventReceiver.noGridAvailable(noGridMessage);
			}
			else
				log.info("neTestNGRunner.buildTestParams: noGridAvailableEventReceiver is null");
			return null;
		}
	}
	
	private void updateSuiteAndTestTable() {
		TestInfoUpdater testInfoUpdater = new TestInfoUpdater(request);
		testInfoUpdater.start();
	}

	private XmlTest buildTest(Map<String,String> params, String className, XmlSuite xmlSuite,Map<String, String> testngParams){
		/*XmlTest myTest = new XmlTest(xmlSuite);
		myTest.setName(Util.getTestName(className, params));
		myTest.setParameters(params);
		List<XmlClass> myClasses = new ArrayList<XmlClass> ();
		XmlClass xmlClass = new XmlClass(MtaasConstants.TEST_LOCATION + className);*/
		XmlTest myTest = null;
		XmlClass xmlClass = null;
		List<XmlClass> myClasses = null;
		
		//if request to run failed test case do here..
		List<XmlInclude> xmlsInclude = new ArrayList<XmlInclude>();
		if (requestId > 0){
			
			if (failedTestRerunRequestType == FailedTestRerunRequestType.TESTMETHOD){
				log.info("This request is to run the failed test methods for request id: " + requestId);
				List<String> failedTestMethodsName = TestMethodExecutionDAO.getFailedTestMethodsNameFromRequestAndTestngParams(requestId,
						testngParams,className);
				
				log.info("testClassName:" + className + ", failedTestMethodsName:" + failedTestMethodsName + ", testngParams:" + testngParams);
				
				if (failedTestMethodsName.size() > 0){
					myTest = new XmlTest(xmlSuite);
					myTest.setName(Util.getTestName(className, params));
					myTest.setParameters(params);
					myClasses = new ArrayList<XmlClass> ();
					xmlClass = new XmlClass(MtaasConstants.TEST_LOCATION + className);
					for (String methodName:failedTestMethodsName){
						log.info("Included test Methods: " + methodName);
						XmlInclude xmlInclude = new XmlInclude(methodName); 
						xmlsInclude.add(xmlInclude);
					}
					xmlClass.setIncludedMethods(xmlsInclude);
				}
				else{
					log.info(String.format("During failed test method exectuion, this class : %s "
							+ "doesnot contain any failed test method for testNgParams: %s.",className,testngParams));
				}
			} 
			else if (failedTestRerunRequestType == FailedTestRerunRequestType.TESTCLASS){
				log.info("This is the request to run failed test CLASS so running the entire request without including methods");
			}
			else if (failedTestRerunRequestType == FailedTestRerunRequestType.TESTSUITE){
				log.info("This is the request to run failed test SUITE so running the entire request without including methods");
			}
			else{
				log.error("TestNGRunner.buildTest(). This is not expected since only for " +
						"only for rerun test request with requestId > 0 is expected with three option" +
						" either testclass, testmethod or testsuite. Check request.");
			}
		}
		else{
			myTest = new XmlTest(xmlSuite);
			myTest.setName(Util.getTestName(className, params));
			myTest.setParameters(params);
			myClasses = new ArrayList<XmlClass> ();
			xmlClass = new XmlClass(MtaasConstants.TEST_LOCATION + className);
		}
		
		if (myClasses != null && myTest != null){
			myClasses.add(xmlClass);
			myTest.setXmlClasses(myClasses);
		}
		
		return myTest;
		
	}
	
	private void listenTest(TestListener tla){
		log.info("In listenTest method");
		for (ITestResult success: tla.getPassedTests()){
			log.info("Success Testcase Name" + success.getName());
		}
		
		for (ITestResult failure: tla.getFailedTests()){
			log.info("Failed Testcase Name" + failure.getName());
		}
		
		for (ITestResult skipped: tla.getSkippedTests()){
			log.info("Skipper Testcase Name" + skipped.getName());
		}
	}
	
	
	
}