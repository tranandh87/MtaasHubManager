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
import com.mtaas.db.HubInfoDAO;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.MtaasConstants.HubOrDeviceStatus;


public class GridUrlChecker {
	
	private static Logger log = Logger.getLogger(GridUrlChecker.class.getName());
	
	private void scheduleGridHealthCheck(){
	new java.util.Timer().schedule( 
	        new java.util.TimerTask() {
	            @Override
	            public void run() {
	            	Map<String,HubOrDeviceStatus> hubUrls = HubInfoDAO.getAllHubUrl();
	        		
	            	hubUrls = getProtocolAndAuthorityFromUrl(hubUrls);
	        		
	            	URL url;
	            	String hubUrl = null;
	            	for (Map.Entry<String, HubOrDeviceStatus> entry : hubUrls.entrySet()) {
						try {
							hubUrl = entry.getKey();
							url = new URL(hubUrl);
							
							HttpURLConnection huc = (HttpURLConnection) url.openConnection();
							huc.setRequestMethod("HEAD");
							huc.connect();
							int responseCode = huc.getResponseCode();
							
							if (responseCode == 200) {
								if (!(compareHubStatus(entry.getValue(),HubOrDeviceStatus.AVAILABLE))){
									//TODO: update local map and db table for available
									log.info("Updating the current hub status to Available");
									hubUrls.put(hubUrl, HubOrDeviceStatus.AVAILABLE);

									updateHubStatus(hubUrl,HubOrDeviceStatus.AVAILABLE);
								}
			            		log.info("GOOD url : " + hubUrl);
			            	} else {
			            		log.info("something weird happened. Was expecting to go to ConnectException but not");
			            	}
						}
						catch(Exception connException){
							log.info("Checking for connection resutl in \"java.net.ConnectException\". HubURL : " + hubUrl);
							log.info("Exception message : " + connException.getMessage());
							
							if (!(compareHubStatus(entry.getValue(),HubOrDeviceStatus.UNAVAILABLE))){
								//TODO: update local map and db table for available
		            			log.info("Updating the current hub status to unavailable");
		            			hubUrls.put(entry.getKey(), HubOrDeviceStatus.UNAVAILABLE);
		            			
		            			updateHubStatus(hubUrl,HubOrDeviceStatus.UNAVAILABLE);
							}
		            		log.info("BAD url : " + hubUrl);
						}
						/*catch (IOException e) {
							// TODO Auto-generated catch block
//							e.printStackTrace();
							log.info("The url is not opened successfully. URL : " + hubUrl);
							log.info(e.getMessage());
						}*/
	            	}
	            }

				private void updateHubStatus(String hubUrl, HubOrDeviceStatus hubStatus) {
					// TODO Auto-generated method stub
					log.info("in updateHubStatus. HubUrl: " + hubUrl + ". Status: " + hubStatus.name());
					if (HubInfoDAO.updateHubStatusWithHubUrl(hubUrl,hubStatus) == 1)
						log.info("Hub is updated with new status. HubUrl like:" + hubUrl + ". Status: " + hubStatus.toString());
					else
						log.error("Hub table is not updated correctly. check db. HubUr like:" + hubUrl);
				}
	        }, 
	        0,
	        MtaasConstants.HUB_URL_CHECKER_INTERVAL
			);
	
		log.info("After timer scheduled");
	}
	
	public static void main(String a[]){
		
		GridUrlChecker gridhealthChecker = new GridUrlChecker();
//		Map<String,HubOrDeviceStatus> hubUrls = HubInfoDAO.getAllHubUrl();
//		Connection conn = new DatabaseConnection().getDbConnection();
		gridhealthChecker.scheduleGridHealthCheck();
	
	}
	
	private static Map<String,HubOrDeviceStatus> getProtocolAndAuthorityFromUrl(Map<String,HubOrDeviceStatus> hubUrls){
		Map<String,HubOrDeviceStatus> parsedHubUrls = new HashMap<String,HubOrDeviceStatus>();
		
		for (Map.Entry<String, HubOrDeviceStatus> entry : hubUrls.entrySet()) {
		    String hubUrl = entry.getKey();
		    
		    log.info("HubUrl before parsing: " + hubUrl + ". Status =" + entry.getValue().name());
			try {
				URL url = new URL(hubUrl);
				
				String protocol = url.getProtocol();
				log.info("protocol : " + protocol);
				String authority = url.getAuthority();
				log.info("Authority : " + authority);
				
				parsedHubUrls.put(protocol + "://" + authority,entry.getValue()); 
				
				log.info(String.format("After parsing. Hub url: %s. Status: %s", protocol + "://" + authority , entry.getValue().name()));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return parsedHubUrls;
	}
	
	private static boolean compareHubStatus(HubOrDeviceStatus availableHubStatus, HubOrDeviceStatus currentHubStatus){
		if (availableHubStatus == currentHubStatus)
			return true;
		else
			return false;
	}

}
