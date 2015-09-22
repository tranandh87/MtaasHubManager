package com.mtaas.checker;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mtaas.db.DatabaseConnection;
import com.mtaas.db.DeviceInfoDAO;
import com.mtaas.db.HubInfoDAO;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.MtaasConstants.HubOrDeviceStatus;


public class AppiumNodeUrlChecker {
	
	private static Logger log = Logger.getLogger(AppiumNodeUrlChecker.class.getName());
	
//	private void scheduleAppiumNodeIpHealthCheck(final Map<String,HubOrDeviceStatus> appiumIps){
	private void scheduleAppiumNodeIpHealthCheck(){
	new java.util.Timer().schedule( 
	        new java.util.TimerTask() {
	            @Override
	            public void run() {
	            	
	            	Map<String,HubOrDeviceStatus> appiumIps = DeviceInfoDAO.getAllAppiumNodeIp();
	            	
	            	URL url;
	            	String appiumIp = null;
	            	String appiumIpInTable = null;
	            	for (Map.Entry<String, HubOrDeviceStatus> entry : appiumIps.entrySet()) {
						try {
							appiumIpInTable = entry.getKey();
							appiumIp = appiumIpInTable + MtaasConstants.APPIUM_NODE_IP_CHECK;
							url = new URL(appiumIp);
							
							HttpURLConnection huc = (HttpURLConnection) url.openConnection();
//							huc.setRequestMethod("HEAD");
							huc.connect();
							int responseCode = huc.getResponseCode();
							
							if (responseCode == 200) {
								if (!(compareDeviceStatus(entry.getValue(),HubOrDeviceStatus.AVAILABLE))){
									//TODO: update local map and db table for available
									log.info("Updating the current device status to Available");
									appiumIps.put(appiumIpInTable, HubOrDeviceStatus.AVAILABLE);

									updateDeviceStatus(appiumIpInTable,HubOrDeviceStatus.AVAILABLE);
								}
			            		log.info("GOOD url : " + appiumIp);
			            	} else {
			            		log.info("something weird happened. Was expecting to go to ConnectException but not");
			            	}
						}
						catch(Exception connException){
							log.info("Checking for connection resutl in \"java.net.ConnectException\". HubURL : " + appiumIp);
							log.info("Exception message : " + connException.getMessage());
							
							if (!(compareDeviceStatus(entry.getValue(),HubOrDeviceStatus.UNAVAILABLE))){
								//TODO: update local map and db table for available
		            			log.info("Updating the current device status to unavailable");
		            			appiumIps.put(appiumIpInTable, HubOrDeviceStatus.UNAVAILABLE);
		            			
		            			updateDeviceStatus(appiumIpInTable,HubOrDeviceStatus.UNAVAILABLE);
							}
		            		log.info("BAD url : " + appiumIp);
						}
						/*catch (IOException e) {
							// TODO Auto-generated catch block
//							e.printStackTrace();
							log.info("The url is not opened successfully. URL : " + appiumIp);
							log.info(e.getMessage());
						}*/
	            	}
	            }

				private void updateDeviceStatus(String appiumIp, HubOrDeviceStatus deviceStatus) {
					// TODO Auto-generated method stub
					log.info("in updateHubStatus. HubUrl: " + appiumIp + ". Status: " + deviceStatus.name());
					if (DeviceInfoDAO.updateDeviceStatusWithAppiumIp(appiumIp,deviceStatus) == 1)
						log.info("device is updated with new status. appiumIp like:" + appiumIp + ". Status: " + deviceStatus.toString());
					else
						log.error("device table is not updated correctly. check db. appiumIp like:" + appiumIp);
				}
	        }, 
	        0,
	        MtaasConstants.APPIUM_NODE_URL_CHECKER_INTERVAL
			);
	
		log.info("After timer scheduled");
	}
	
	public static void main(String a[]){
		
		AppiumNodeUrlChecker gridhealthChecker = new AppiumNodeUrlChecker();
//		Connection conn = new DatabaseConnection().getDbConnection();
		gridhealthChecker.scheduleAppiumNodeIpHealthCheck();
	}
	
	private static boolean compareDeviceStatus(HubOrDeviceStatus availableDeviceStatus, HubOrDeviceStatus currentDeviceStatus){
		if (availableDeviceStatus == currentDeviceStatus)
			return true;
		else
			return false;
	}

}
