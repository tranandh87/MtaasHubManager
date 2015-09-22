package com.mtaas.testware.manager;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mtaas.client.FileUploadClient;
import com.mtaas.db.DeviceInfoDAO;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.MtaasConstants.HubOrDeviceStatus;
import com.mtaas.util.Util;
import com.sun.jersey.api.client.ClientResponse;

public class TestwareManager {
	private static Logger log = Logger.getLogger(TestwareManager.class.getName());
	public static enum Status {SUCCESS, FAIL_DUPLICATE, FAIL_GENERAL}; 

	public static Status addTestClassToRepository(InputStream contentStream, String fileName){
		Status status = Status.FAIL_GENERAL;
		try {
			
			String currentWorkingDir = System.getProperty("user.dir");
			log.info("current working directory: " + currentWorkingDir);
			
			String testClassPath = Util.getConfigFileProperty(MtaasConstants.CONFIG_TEST_CLASS_KEY); 
			
			if (testClassPath == null){
				Util.createConfigPropFile();
				log.error(MtaasConstants.CONFIG_FILE_NOT_FOUND_RESPONSE + currentWorkingDir);
			}
			
			String uploadedFileLocation = testClassPath + File.separator + fileName;
			
			log.info("Uploaded file location: " + uploadedFileLocation);
			
			// save it
			Util.writeToFile(contentStream, testClassPath, fileName);
			Util.changeFilePackageName(testClassPath, fileName, false, null,null);
			
			status = Status.SUCCESS;
		} catch (Exception e){
			log.error("exception caught in addTestToRepository", e);
		}
		
		return status;
	}
	
	
	public static Status addApkToRepository(InputStream contentStream, String apkName){
		Status status = Status.FAIL_GENERAL;
		
		try {
			
			String testDir = Util.getConfigFileProperty(MtaasConstants.CONFIG_TEST_CLASS_KEY);
			
			//save apk file
			Util.writeToFile(contentStream, testDir, apkName);
			
			sendApkToNodes(testDir + File.separator + apkName);
			
			status = Status.SUCCESS;
		} catch (Exception e){
			log.error(e);
		}
		
		return status;
	}
	
	/**
	 * send the apk to all connected nodes
	 * @param apkPath
	 */
	private static void sendApkToNodes(String apkPath) {
		Map<String, HubOrDeviceStatus> ipVsStatusMap = DeviceInfoDAO.getAllAppiumNodeIp();
		
		if (ipVsStatusMap != null && !ipVsStatusMap.isEmpty()){
			for (String appiumIp : ipVsStatusMap.keySet()){
				if (ipVsStatusMap.get(appiumIp) != HubOrDeviceStatus.UNAVAILABLE){
					String restEndPoint = appiumIp + MtaasConstants.APPIUM_NODE_APK_UPLOAD;
					ClientResponse cr = FileUploadClient.uploadFile(restEndPoint, apkPath);
					
					log.info(restEndPoint + ": statusCode=" + cr.getStatus() + "; response=" + cr.getClientResponseStatus());
				}
			}
		}
		else {
			log.warn("no available appium node");
		}
	}


	public static Status addTestJarToRepository(InputStream contentStream, String fileName){
		Status status = Status.FAIL_GENERAL;
		try {
			String testDir = Util.getConfigFileProperty(MtaasConstants.CONFIG_TEST_CLASS_KEY);
			
			//save jar file
			Util.writeToFile(contentStream, testDir, fileName);
			String jarFile = testDir + File.separator + fileName;
			
			Set<String> jarDirectory = new HashSet<String>();
			Set<String> extractedFileNames = Util.extractJarToDest(jarFile, testDir);
			
			jarDirectory = Util.getDirectoryNames(extractedFileNames);
			for (String dir : jarDirectory){
				log.info("Directory created to extract jar : " + dir);
			}
			
			Util.changeAllFilesPackageName(testDir, testDir, extractedFileNames,jarDirectory);
			
			new File(jarFile).delete();
			log.debug("jar file deleted after extracting");
			status = Status.SUCCESS;
		} catch (Exception e){
			log.error(e);
		}
		return status;
	}
}
