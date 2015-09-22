package com.mtaas.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MtaasConstants {
	public static final String TEST_NAME = "testName";
	public static final Object TEST_CLASS = "testClass";
	public static final String TEST_SUITE = "testSuite";
	public static final String TEST_LOCATION = "com.mtaas.test.";
	
//	public static final Set<String> SUPPORTED_DEVICES = new HashSet<String>(Arrays.asList("Android","ios"));
	//CustomStringList is used for case-insensitive contains method of ArrayList.
	public static final CustomStringList SUPPORTED_DEVICES = new CustomStringList(Arrays.asList("Android"));
//	public static final Set<String> SUPPORTED_VERSIONS = new HashSet<String>(Arrays.asList("4.3","4.4.2"));

	public static final String TEST_CASE_GENERIC_NAME = "testCase";
	public static final String DEVICE = "device";
	public static final String BROWSER = "browserName";
	public static final String VERSION = "version";
	public static final String PLATFORM = "platform";
	public static final String PACKAGE_NAME = "packageName";
	public static final String APP_ACTIVITY = "appActivity";
	public static final String HUB_URL = "gridURL";
	public static final String APP_NAME = "appName";
	
	//custom capability
	public static final String IS_EMULATOR = "isEmulator";
	public static final String MANUFACTURER = "manufacturer";
	public static final String MODEL = "model";
	
	//custom capability values
	public static final String EMULATOR_MANUFACTURER = "N/A";
	public static final String EMULATOR_MODEL = "N/A";
	
	public static final String DEVICE_PLATFORM = "Linux";
	public static final String EMPTY_BROWSER_NAME = "";
	
	public static final String MTAAS_TEST_NAME_SEPERATOR = "_";
	
	public static final Set<Integer> TEST_RESULT = new HashSet<Integer>(Arrays.asList(1, 2, 16, 3, 4));
	public enum TestResult {
		
		PASS(1),FAIL(2),INPROGRESS(16),SKIP(3),SUCCESS_PERCENTAGE_FAILURE(4);
		int status;
		TestResult(int status){
			this.status = status; 
		}
		
		public static TestResult getValue(int status){
			switch(status){
				case 1:
					return PASS;
				case 2:
					return FAIL;
				case 3:
					return SKIP;
				case 4:
					return SUCCESS_PERCENTAGE_FAILURE;
				case 16:
					return INPROGRESS;
				default:
					return null;
			}
		}
	};
	
	public enum TrackingResult {
		
		PASS(1),FAIL(2);
		int status;
		TrackingResult(int status){
			this.status = status; 
		}
		
		public static TestResult getValue(int status){
			switch(status){
				case 1:
					return TestResult.PASS;
				case 2:
					return TestResult.FAIL;
				default:
					return null;
			}
		}
	};
	
	public static final Set<Integer> HUB_OR_DEVICE_STATUS = new HashSet<Integer>(Arrays.asList(0, 1, 8));
	public enum HubOrDeviceStatus{
		AVAILABLE(1),BUSY(0),UNAVAILABLE(8);
		
		int hubOrDeviceStatus;
		HubOrDeviceStatus(int hubStatus){
			this.hubOrDeviceStatus = hubStatus; 
		}
		
		public static HubOrDeviceStatus getValue(int status){
			switch(status){
				case 1:
					return AVAILABLE;
				case 0:
					return BUSY;
				case 8:
					return UNAVAILABLE;
				default:
					return null;
			}
		}
	}
	
	public enum TestExecutionResult{
		PASS(1),FAIL(0);
		
		int testStatus;
		TestExecutionResult(int testStatus){
			this.testStatus = testStatus; 
		}
		
		public static TestExecutionResult getValue(int status){
			switch(status){
				case 1:
					return TestExecutionResult.PASS;
				case 0:
					return TestExecutionResult.FAIL;
				default:
					return null;
			}
		}
	}

	public static final String TEST_METHOD_ID = "testMethodId";
	public static final String TEST_ID = "testId";
	public static final String TEST_SUITE_ID = "testSuiteId";
	public static final String DEVICE_ID = "deviceId";
	
	public static final String PROTOCOL = "http://";
	public static final String HUB_RESOURCE = "/wd/hub";
	public static final String IP_PORT_SEPERATOR = ":";
	
	public static final String STEP_NAME = "Step ";
	
	public static final String SUITE_NAME = "suite";
	
	public static final int NO_TEST_RESULT = 100;
	
	/**
	 * ***ID: DB constants***values between ID is the db constants for column name
	 */
	public static final String IS_EMULATOR_COL = "isEmulator";

	public static final String OS_VERSION_COL = "osVersion";

	public static final String PLATFORM_COL = "platform";
	
	public static final String MANUFACTURER_COL = "manufacturer";
	
	public static final String MODEL_COL = "model";
	
	public static final String NO_GRID_RESPONSE_MESSAGE = "No grid with the requested info is available. Try different device config";
	
	//field matching the tracking table in db
	public static final String TRACKING_TEST_METHOD_STEP_NAME = "step";
	public static final String TRACKING_TEST_METHOD_DESCRIPTION = "stepDescription";
	public static final String TRACKING_TESTMETHOD_RESULT = "result";
	public static final boolean GRID_URL_CHECKER_ENABLED = true;
	public static final boolean APPIUM_IP_CHECKER_ENABLED = true;
	
	public static final long APPIUM_NODE_URL_CHECKER_INTERVAL = 10000;
	public static final long HUB_URL_CHECKER_INTERVAL = 10000;
	
	public static final int LOOP_COUNTER_TEST_METHOD_TRACKING = 15;
	public static final String PUT_TEST_METHOD_EXEC_ID = "/wd/hub/addTestMethodIdToList/";

	public static final Object CONFIG_FILE_NOT_FOUND_RESPONSE = "Config file not found. "
			+ "Config file Created but file path value should be entered correctly. Config file locaiton: ";
	public static final String CONFIG_TEST_CLASS_KEY = "testClassFilePath";
	public static final String CONFIG_FILE_NAME = "config.properties";
	public static final String CONFIG_PACKAGE_KEY = "package";
	public static final String NO_FAILED_TEST_FOUND = "Request doesnot contain any failed test. Request Id : ";
	
	public static String NODE_TEST_METHOD_TRACE_END_POINT = "/wd/hub/fetchTestMethodOpTrace";
	public static String APPIUM_NODE_IP_CHECK = "/wd/hub/deviceConfig";
	public static String APPIUM_NODE_APK_UPLOAD = "/wd/hub/apkUpload";
	
	public enum FailedTestRerunRequestType{
		TESTCLASS,TESTMETHOD,TESTSUITE
	}
	//field matching the Operation.java fields.
	
	/**
	 * ID: DB constants
	 */
	
}

