package com.mtaas.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.apache.commons.lang3.StringUtils;

import com.mtaas.bean.AutDetails;
import com.mtaas.bean.DeviceConfig;
import com.mtaas.bean.TestExecutionRequest;
import com.mtaas.bean.TestSuite;
import com.mtaas.bean.TestSuiteExecutionResponse;
import com.mtaas.db.AUTDetailDAO;
import com.mtaas.db.DeviceInfoDAO;
import com.mtaas.db.HubInfoDAO;
import com.mtaas.db.TestExecutionDAO;
import com.mtaas.db.TestExecutionResultDAO;
import com.mtaas.db.TestInfoDAO;
import com.mtaas.db.TestSuiteExecutionResultDAO;
import com.mtaas.util.MtaasConstants.FailedTestRerunRequestType;
import com.mtaas.util.MtaasConstants.HubOrDeviceStatus;
import com.mtaas.util.MtaasConstants.TestResult;

public class Util {
	
	private static final String PACKAGE = "package ";
	private static final String IMPORT = "import ";
	private static final String JAVA_REGEXP = ".java";
	private static Logger log = Logger.getLogger(Util.class.getName());
	
	public static boolean isNonEmptyString(String str){
		return str != null && str.length() > 0;
	}
	
	public static String getSuiteName(TestExecutionRequest request){
		return request.getTestSuite().getSuiteName();
	}
	
	public static List<String> getClassesName(TestExecutionRequest request){
		List<String> classesName = new ArrayList<String>();
		for (String className : request.getTestSuite().getClassName()){
			log.info("Added classes for a suite. Class name: " + className);
			classesName.add(className);
		}
		return classesName;
	}
	
	public static String getTestName(String className, Map<String, String> params){
		
		String isEmulator = params.get(MtaasConstants.IS_EMULATOR);
    	boolean isEmulatorBoolean = Boolean.valueOf(isEmulator);
		int isEmulatorInt = Util.getIntEquivalentOfBoolean(isEmulatorBoolean);
		
		String testClassNameKey = className
									+ MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + params.get(MtaasConstants.DEVICE) 
									+ MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + params.get(MtaasConstants.VERSION)
									+ MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + isEmulatorInt
									+ MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + params.get(MtaasConstants.MANUFACTURER)
									+ MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + params.get(MtaasConstants.MODEL);
		
		return testClassNameKey;
	}
	
	public static String getTestName(String className, DeviceConfig deviceConfig){
		return String.format("%s%s%s%s%s", className,MtaasConstants.MTAAS_TEST_NAME_SEPERATOR, 
				deviceConfig.getDevice(),MtaasConstants.MTAAS_TEST_NAME_SEPERATOR, deviceConfig.getVersion());
	}
	
	public static String getActualTestName(String testName,String separator){
		StringBuilder testNameBuilder = new StringBuilder();
		
		log.info("Test Name received : " + testName);
		String testNameParts[] = testName.split(separator);
		int count = testNameParts.length;
		
		//here 5 represent the number of additional values that has been appended by _ to the original test name
		for (int i=0; i < (count-5); i++){
			//here 6 represent the number of number of addional values + 1 that has bee appended by _ to the original test name. 
			//This method is to identify the _ character and append it if the original test name has _.
			if (i < (count-6))
				testNameBuilder.append(testNameParts[i] + separator);
			else
				testNameBuilder.append(testNameParts[i]);
		}
		
		String originalTestName = testNameBuilder.toString();
		log.info("original test name to be sent : " + originalTestName);
		return originalTestName;
	}
	
	public static Timestamp getSQLCurrentTimeStamp(long currentTime){
		return new java.sql.Timestamp(currentTime); 
	}
	
	public static Timestamp getSQLCurrentTimeStamp(){
		Date date= new Date();
    	long executionStartTime = date.getTime();
		return new java.sql.Timestamp(executionStartTime); 
	}

	public static String buildHubUrl(String hubIp, int hubPort) {
		if (isNonEmptyString(hubIp) && (hubPort > 0))
			return (MtaasConstants.PROTOCOL + hubIp + MtaasConstants.IP_PORT_SEPERATOR + hubPort + MtaasConstants.HUB_RESOURCE);
		return null;
		// TODO Auto-generated method stub
	}

	public static String concatinateByMtaasTestNameSeperator(String testName,
			String testMethodName) {
		return testName + MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + testMethodName; 
		// TODO Auto-generated method stub
	}
	
	public static TestResult getTestResult(ITestContext testContext){
	
//		testContext.getAllTestMethods()[0].
		boolean failedTest = !testContext.getFailedTests().getAllMethods().isEmpty();
		log.info("Failed Test:" + failedTest);
		boolean skippedTest = !testContext.getSkippedTests().getAllMethods().isEmpty();
		log.info("Skipped Test:" + skippedTest);
		boolean passedTest = !testContext.getPassedTests().getAllMethods().isEmpty();
		log.info("Passed Test:" + passedTest);
		
		//no fail or skip test and all pass
		if ((!(failedTest || skippedTest)) && passedTest){
			return TestResult.PASS;
		}
		//no fail and any skip
		else if ((!failedTest) && skippedTest){
			return TestResult.SKIP;
		}
		else if (failedTest){
			return TestResult.FAIL;
		}
		else{
			log.error("Test result is not either pass, fail and skipped. Not expected");
			return TestResult.getValue(MtaasConstants.NO_TEST_RESULT);
		}
		
	}
	
	public static void closeDBResources(ResultSet rs, Statement stmt,
			PreparedStatement preparedStatement, Connection conn) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
		

	public static String getStatusFromTestMethodTrackingStep(String step) {
		// TODO Auto-generated method stub
		int beginIndex = step.indexOf("\"status\":");
		log.info("Sting is : " + step);
		log.info("First index is : " + beginIndex);
		
		return step.substring(beginIndex, (beginIndex+10));
	}

	public static String getStringEquivalentOfStatus(int deviceStatus) {
		if (MtaasConstants.HUB_OR_DEVICE_STATUS.contains(deviceStatus))
			return HubOrDeviceStatus.getValue(deviceStatus).name();
		else {
			log.error("Status received in device object is not matching constants specified in MtaasConstants.HUB_OR_DEVICE_STATUS. " +
					"Ref: 0->busy,1->available,8-unavailable. Status received = " + deviceStatus);
			return null;
		}
	}

	public static boolean isFileExistInEclipseDir(String fileName) {
		
		File file = new File(fileName);
		if (file.exists())
			return true;
		else
			return false;
		
	}

	public static void createConfigPropFile() {

		Properties prop = new Properties();
		OutputStream configFile = null;
	 
		try {
			log.info("Creating config file in eclipse directory");
			configFile = new FileOutputStream(MtaasConstants.CONFIG_FILE_NAME);
	 
			// set the properties value
			prop.setProperty(MtaasConstants.CONFIG_TEST_CLASS_KEY, "<specify the location where the test file should be stored>");
			prop.setProperty(MtaasConstants.CONFIG_PACKAGE_KEY,"<specify package name for test class>");
			
			// save properties to project root folder
			prop.store(configFile, null);
	 
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (configFile != null) {
				try {
					configFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	 
		}
	}

	public static String getConfigFileProperty(String testClassProp) {
		Properties prop = new Properties();
		InputStream configFile = null;
		String propValue = null;
	 
		try {
			configFile = new FileInputStream(MtaasConstants.CONFIG_FILE_NAME);
			prop.load(configFile);
			propValue = prop.getProperty(testClassProp);
	 
		} catch (FileNotFoundException io) {
			log.error("getConfigFileProperty. File not found exception");
			io.printStackTrace();
		} catch (IOException io){
			log.error("getConfigFileProperty. IO exception");
			io.printStackTrace();
		}
		finally {
			if (configFile != null) {
				try {
					configFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	 
		}
		return propValue;
	}

	public static TestExecutionRequest BuildRequestToRunFailedTestCase(int requestId, FailedTestRerunRequestType failedTestRerunRequestType) {
		
		TestExecutionRequest testExecutionResult = new TestExecutionRequest();
		
		//building Device config and suiteId for the request
		int suiteId = 0;
		Map<DeviceConfig,Integer> devicesConfigAndSuiteIdMap = Util.getFailedSuiteDevicesConfigAndSuiteIdFromRequestId(requestId);
		
		List<DeviceConfig> devicesConfig = new ArrayList<DeviceConfig>();
		for (Map.Entry<DeviceConfig,Integer> entry : devicesConfigAndSuiteIdMap.entrySet()) {
			devicesConfig.add(entry.getKey());
			suiteId = entry.getValue();
		}
		testExecutionResult.setDeviceConfig(devicesConfig);
		
		//build AUT details
		AutDetails autDetails = AUTDetailDAO.getAutDetailsFromRequestId(requestId);
		testExecutionResult.setAutDetails(autDetails);
		
		//build testSuite
		TestSuite testSuite = new TestSuite();
		List<String> classesName = null;
		if (failedTestRerunRequestType == FailedTestRerunRequestType.TESTSUITE){
			log.info("This is request to run failed test suite, so including all test class of any status");
			classesName = TestExecutionResultDAO.getAllTestClassesNameFromRequestId(requestId);
		}
		else if (failedTestRerunRequestType == FailedTestRerunRequestType.TESTCLASS || 
				failedTestRerunRequestType == FailedTestRerunRequestType.TESTMETHOD){
			log.info("This is request to run failed testclasses or testmethods, so including all test class of any status");
			classesName = TestExecutionResultDAO.getFailedTestClassesNameFromRequestId(requestId);
		}
		else{
			log.error("Util.BuildRequestToRunFailedTestCase. This is not expected as only three type of"
					+ " failedTestRerunRequestType is expected as testClass,testMethod and test suite.");
		}
		String suiteName = TestInfoDAO.getSuiteNameFromSuiteId(suiteId);
		
		testSuite.setClassName(classesName);
		testSuite.setSuiteName(suiteName);
		
		testExecutionResult.setTestSuite(testSuite);
		
		return testExecutionResult;
	}

	private static Map<DeviceConfig,Integer> getFailedSuiteDevicesConfigAndSuiteIdFromRequestId(int requestId) {
		
		Map<DeviceConfig,Integer> devicesConfigAndSuiteIdMap = new HashMap<DeviceConfig,Integer>();
		Map<Integer,Integer> devicesIdAndSuiteIdMap = TestSuiteExecutionResultDAO.getDevicesAndSuiteIdFromRequestId(requestId);
		
		for (Map.Entry<Integer,Integer> entry : devicesIdAndSuiteIdMap.entrySet()) {
			DeviceConfig deviceConfig= DeviceInfoDAO.getDeviceConfigFromDeviceId((entry.getKey()).intValue());
			devicesConfigAndSuiteIdMap.put(deviceConfig, entry.getValue());
		}
		return devicesConfigAndSuiteIdMap;
	}
	
	public static void writeToFile(InputStream uploadedInputStream,
			String filePath, String fileName) throws Exception {
		
		OutputStream out = null;
		try {
			String uploadFileLocation = filePath + File.separator + fileName;
			out = new FileOutputStream(new File(uploadFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	 
	}
		
	public static void changeFilePackageName(String filePath, String fileName, boolean isPrepend, 
			Set<String> extractedFileNames, Set<String> jarDirectories) throws Exception {

		String oldFile = filePath + File.separator + fileName;

		java.util.Date date= new java.util.Date();

		
		Timestamp timestamp = new Timestamp(date.getTime());
		String tempFile = timestamp + "_" + fileName;
		tempFile = filePath + File.separator + tempFile;

		log.info("FileName is: " + fileName + ". Temp File Name: " + tempFile);

		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(oldFile));
			bufferedWriter = new BufferedWriter(new FileWriter(tempFile));

			String line = null;
			boolean isPacakgeNameChanged = false;

			//changing the package name
			while (!isPacakgeNameChanged && (line = bufferedReader.readLine()) != null) {
				if (checkForPackageOrImportLine(line,PACKAGE) && !isPacakgeNameChanged){
					String packageName = Util.getConfigFileProperty(MtaasConstants.CONFIG_PACKAGE_KEY);
					if (isPrepend){
						//prepend our own package name to the existing one
						//for ex: input=package test;
						//output=package mtaas.test.test;
						line = line.replace(PACKAGE, PACKAGE + packageName + ".");
						log.info("changed package name" + line);
					}
					else {
						//if we dont need to prepend, just replace the existing package name
						line = PACKAGE + packageName + ";";
					}
					isPacakgeNameChanged = true;
				}
				bufferedWriter.write(line + "\n");
			}
			
			//changing the import name
			if (null != extractedFileNames){
				changeImportsIfNeeded(bufferedReader,bufferedWriter,extractedFileNames,jarDirectories);
			}
			
			/*while ((line = bufferedReader.readLine()) != null) {
				//log.info(line);
				bufferedWriter.write(line + "\n");
			}*/
		} finally {
			try {
				if(bufferedReader != null)
					bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(bufferedWriter != null)
					bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Once everything is complete, delete old file
		File oldFileToBeDeleted = new File(oldFile);
		oldFileToBeDeleted.delete();

		// And rename temp file's name to old file name
		File newFile = new File(tempFile);
		newFile.renameTo(oldFileToBeDeleted);
	}

	public static boolean checkForPackageOrImportLine(String line,String packageOrImport) {
		line = line.trim();
		String regEx = "^\\b" + packageOrImport + ".*";
		boolean matched = line.matches(regEx);
		return matched;
	}

	private static void changeImportsIfNeeded(BufferedReader bufferedReader,
			BufferedWriter bufferedWriter, Set<String> extractedFileNames,
			Set<String> jarDirectories) {

		String line = null;
		if (null != extractedFileNames){
			Set<String> extractedFileNamesWithoutExtension = new HashSet<String>();

			for (String extractedFileName : extractedFileNames){
				String extractedFileNameWithoutExtension = getFileNamesWithoutExtension(extractedFileName,JAVA_REGEXP);
				extractedFileNamesWithoutExtension.add(extractedFileNameWithoutExtension);
			}
			
			try {
				String packageName = Util.getConfigFileProperty(MtaasConstants.CONFIG_PACKAGE_KEY);
				while((line = bufferedReader.readLine()) != null){
					
					if (checkForPackageOrImportLine(line,IMPORT)){
						log.info("Contains import statement" + line);
						do{
							if (!line.contains("*")){
								String testClassNameWithSemicolon = getLastPart(line,"\\.");
								String testClassName = getClassNameWithoutSemicolon(testClassNameWithSemicolon);
								
								if (extractedFileNamesWithoutExtension.contains(testClassName)){
									line = line.replace(IMPORT, IMPORT + packageName + ".");
									log.info("Import statement is replaced :" +  line);
								}
							}
							else{
								log.warn("This is the case where the * is used in import "
										+ "and if its a import current jar then we are not handling it right now");
							}
							
							bufferedWriter.write(line + "\n");
							
							if (!((line = bufferedReader.readLine()) != null)){
								//this condition will mostly not come since there will atleast few lines of code after import statement
								break;
							}
							
						}while(line.contains(IMPORT));
						
						bufferedWriter.write(line + "\n");
					}
					else{
						bufferedWriter.write(line + "\n");
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				try {
					if(bufferedReader != null)
						bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if(bufferedWriter != null)
						bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static String getClassNameWithoutSemicolon(String testClassName) {
		String regEx = "[0-9a-zA-Z]*+";
		
		Pattern MY_PATTERN = Pattern.compile(regEx);
		
		Matcher m = MY_PATTERN.matcher(testClassName.trim());
		if (m.find())
			return m.group();
		else
			return null;
	}

	private static String getLastPart(String input, String regEx) {
		String partsOfInput[] = input.split(regEx);
		int lastPartIndex = partsOfInput.length - 1;
		return partsOfInput[lastPartIndex];
	}

	private static String getFileNamesWithoutExtension(
			String extractedFileName, String javaRegexp) {
		
		String fileNameWithoutExtension = null;
		int indexOfJavaFileExtension = extractedFileName.indexOf(javaRegexp);
		fileNameWithoutExtension = extractedFileName.substring(0, indexOfJavaFileExtension);
			
		if (fileNameWithoutExtension.contains(File.separator)){
			fileNameWithoutExtension = getLastPart(fileNameWithoutExtension, File.separator);
		}
		
//		log.info("FileName without .java extension: " + fileNameWithoutExtension);
		
		return fileNameWithoutExtension;
	}

	public static int getIntEquivalentOfBoolean(boolean isEmulator) {
		if (isEmulator)
			return 1;
		else
			return 0;
	}
	
	public static boolean getBooleanEquivalentOfInt(int isEmulator) {
		if (isEmulator == 0)
			return false;
		else if (isEmulator ==1)
			return true;
		else{
			log.error("int value passed for Util.getBooleanEquivalentOfInt() method should be either 0 or 1. This is not expected."
					+ "Possibly isEmulator column value in Device table at db is wrong or wrong value passed in program");
			return false;
		}
	}

	public static String buildUniqueDeviceKey(String deviceName, String version,
			int isEmulator, String manufacturer, String model) {
		String key = deviceName + MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + version
								+ MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + isEmulator
								+ MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + manufacturer
								+ MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + model;
		return key;
	}

	public static String buildSuiteMapperKey(String suiteName, int deviceId) {
		String key = suiteName + MtaasConstants.MTAAS_TEST_NAME_SEPERATOR + deviceId;
		return key;
	}

	public static TestSuiteExecutionResponse buildFailedTestExectionResponse(String message) {
		TestSuiteExecutionResponse testSuiteExecutionResponse = new TestSuiteExecutionResponse();
		testSuiteExecutionResponse.setIsRequestPassed(false);
		testSuiteExecutionResponse.setMessage(message);
		
		return testSuiteExecutionResponse;
	}

	//jarDirectory is call by reference to get the directory in the jar
	public static Set<String> extractJarToDest(String jarFile, String destDir) throws IOException{
		JarFile jar = new JarFile(jarFile);
		Set<String> extractedJavaFileNames = new HashSet<String>();
		try {
			
			Enumeration<JarEntry> jarEnum = jar.entries();
			while (jarEnum.hasMoreElements()) {
				JarEntry jEntry = jarEnum.nextElement();
				String jEntryName = jEntry.getName();
				log.info("JarEntry name: " + jEntryName);
				File destFile = new File(destDir + File.separator + jEntryName);
				
				log.info("destFile full path: " + destFile.getAbsolutePath());
				if (jEntry.isDirectory()) { // if its a directory, create it
					boolean b = destFile.mkdir();
					log.info("creating directory: " + destFile.getAbsolutePath() + ", success=" + b);
					continue;
				}
				else {
					//file, check for only .java files
					if (!jEntryName.endsWith(JAVA_REGEXP)){
						log.info(jEntryName + " is not a java class, skipping");
						continue;
					}
				}
				
				InputStream is = jar.getInputStream(jEntry); // get the input stream
				
				if (!destFile.exists()){
					log.info(destFile.getAbsolutePath() + " does not exist, creating");
					createFile(destFile);
				}
				
				FileOutputStream fos = new FileOutputStream(destFile);
				while (is.available() > 0) {  // write contents of 'is' to 'fos'
					fos.write(is.read());
				}
				log.info("finished creating " + destFile);
				fos.close();
				is.close();
				
				extractedJavaFileNames.add(jEntryName);
			}
		} finally {
			if (jar != null){
				jar.close();
			}
		}
		
		return extractedJavaFileNames;
	}

	/**
	 * create the file, also create the parent dir if not already existing
	 * @param destFile
	 */
	private static void createFile(File destFile) {
		File parentDir = destFile.getParentFile();
		if (!parentDir.exists()){
			parentDir.mkdirs();
		}
	}

	/**
	 * change all package names of all files under this testDir
	 * @param testDir
	 * @throws Exception 
	 */
	public static void changeAllFilesPackageName(String rootTestDirPrefix, String curTestDir, 
			Set<String> extractedFileNames, Set<String> jarDirectory) throws Exception {
		File dir = new File(curTestDir);
		log.debug("changeAllFilesPackageName for testDir=" + curTestDir);
		
		for (File f : dir.listFiles()){
			String fileName = f.getName();
			log.info("current file/dir=" + fileName);
			if (f.isDirectory()){
				//if directory, recursively rename all the child dir
				String childTestDir = curTestDir + File.separator + fileName;
				log.info("recursively changing package name for all files under dir=" + childTestDir);
				changeAllFilesPackageName(rootTestDirPrefix, childTestDir, extractedFileNames,jarDirectory);
			}
			else if (fileName.endsWith(JAVA_REGEXP) && isExtractedFile(extractedFileNames, rootTestDirPrefix, curTestDir, fileName)){
				//is a file, change the package name in this file
				log.info("changing package name for current file:" + curTestDir + "/" + fileName);
				changeFilePackageName(curTestDir, fileName, true, extractedFileNames,jarDirectory);
			}
		}
	}

	private static boolean isExtractedFile(Set<String> extractedFileNames, String rootTestDirPrefix, String curTestDir, String fileName) {
		curTestDir = curTestDir.replace(rootTestDirPrefix, "");
		String relativeFilePath = curTestDir + File.separator + fileName;
		if (relativeFilePath.startsWith(File.separator)){
			relativeFilePath = relativeFilePath.substring(1);
		}
		return extractedFileNames.contains(relativeFilePath);
	}

	public static Set<String> getDirectoryNames(Set<String> extractedFileNames) {
		Set<String> directoryNames = new HashSet<String>();
		String[] directories = null; 
		
		for (String fileNameWithPath : extractedFileNames){
			String[] directoriesWithClassName = StringUtils.split(fileNameWithPath, File.separator);
			directories = Arrays.copyOf(directoriesWithClassName, directoriesWithClassName.length - 1);
			for (String directory : directories){
				directoryNames.add(directory);
			}
		}
		
		return directoryNames;
	}
	
	public static Set<String> getCombinationOfPackagesFromDir(Set<String> directories){
		
		Set<String> posssiblePackages = new HashSet<String>();
		
		for(String directory:  directories){
			String packageTemp = directory;
			for (String dir : directories){
				if(!dir.equalsIgnoreCase(directory)){
					packageTemp = packageTemp.concat("." + dir); 
				}
			}
			posssiblePackages.add(packageTemp);
		}
		return posssiblePackages;
	}

	public static int getDeviceIdFromTestngParams(
			Map<String, String> testngParams) {
		
		String deviceName = testngParams.get(MtaasConstants.DEVICE);
		String version = testngParams.get(MtaasConstants.VERSION);
		
		//getting the Custom capability
		String isEmulator = testngParams.get(MtaasConstants.IS_EMULATOR);
		boolean isEmulatorBoolean = Boolean.valueOf(isEmulator);
		int isEmulatorInt = Util.getIntEquivalentOfBoolean(isEmulatorBoolean);
		
		String manufacturer = testngParams.get(MtaasConstants.MANUFACTURER);
		String model = testngParams.get(MtaasConstants.MODEL);
		
		String hubUrl = testngParams.get(MtaasConstants.HUB_URL);
		int hubId = HubInfoDAO.getGridId(deviceName,hubUrl);
		
		int deviceId = TestExecutionDAO.getDeviceId(deviceName,version, hubId,isEmulatorInt,manufacturer,model);
		return  deviceId;
	}
	
	
}


