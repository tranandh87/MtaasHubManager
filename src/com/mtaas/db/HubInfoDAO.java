package com.mtaas.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.mtaas.bean.Device;
import com.mtaas.bean.DeviceConfig;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.MtaasConstants.HubOrDeviceStatus;
import com.mtaas.util.Util;

public class HubInfoDAO {
	
	private static Logger log = Logger.getLogger(HubInfoDAO.class.getName());
	
//	private static final String getHubId = "select hubId,status from MTAAS.Hub where platformSupported='%s' and hubUrl='%s' FOR UPDATE";
	private static final String getHubId = "select hubId,status from MTAAS.Hub where platformSupported = ? and hubUrl = ?";
	
	private static final String insertHubInfo = "INSERT IGNORE INTO MTAAS.Hub(`platformSupported`,`status`,`hubUrl`) VALUES (?,?,?)";
	
	private static String getDeviceIdQuery = "SELECT hubId from MTAAS.Hub where `platformSupported` = '%s' and `hubUrl` ='%s'";
	
	/*private static String getGridUrlQuery = "SELECT hubUrl from MTAAS.Hub H inner join MTAAS.Device D on H.hubId = D.hubId "
			+ "where D.platform='%s' and D.osVersion='%s' and D.status !='unavailable' and H.status='%s'";*/
	private static String getGridUrlQuery = "SELECT H.hubUrl from MTAAS.Hub H inner join MTAAS.Device D on H.hubId = D.hubId "
			+ "where D.platform=? and D.osVersion=? and H.status=? and isEmulator=? and manufacturer=? and model=? and D.status=?";
	
	private static final String getAllHubUrl = "Select hubUrl,status from MTAAS.Hub";
	
	private static final String updateHubStatusWithHubUrl = "update MTAAS.Hub set status = ? WHERE hubUrl like ?";
	
	private static final String updateHubStatusWithHubId = "update MTAAS.Hub set status = ? WHERE hubId = ?";
	
	private static final String updateHubStatus = "update MTAAS.Hub set status=? WHERE hubId=?";
	
	public static int insertHubInfo(Device device){
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
//		String hubUrl = Util.buildHubUrl(device.getHubIp(), device.getHubPort());
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
//			conn.setAutoCommit(false);
			
//			String queryHubId = String.format(getHubId,device.getHubPlatform(),device.getHubUrl());
			preparedStatement = conn.prepareStatement(getHubId);

			String hubPlatform = device.getHubPlatform();
			String hubUrl = device.getHubUrl();
			
			preparedStatement.setString(1, hubPlatform);
			preparedStatement.setString(2, hubUrl);
			
			log.info("queryHubId query: " + preparedStatement);
			
			rs = preparedStatement.executeQuery();
			
			if (!rs.next()){
			
				preparedStatement = conn.prepareStatement(insertHubInfo, Statement.RETURN_GENERATED_KEYS);
				
				int hubStatus = device.getHubStatus();
				
				String sqlHubStatus = null;
				log.info("calling Util.getStringEquivalentOfStatus for HubStatus");
				sqlHubStatus = Util.getStringEquivalentOfStatus(hubStatus);
				log.info("Status received : " + sqlHubStatus);
				if (sqlHubStatus == null)
					return -1;
				
				preparedStatement.setString(1, hubPlatform);
				preparedStatement.setString(2, sqlHubStatus);
				preparedStatement.setString(3, hubUrl);
				
				log.info("insertHubInfo query: " + preparedStatement);
				int insertHubAffectedRows = preparedStatement.executeUpdate();
				if (insertHubAffectedRows == 1){
					rs = preparedStatement.getGeneratedKeys();
				    rs.next();
				    int hubId = rs.getInt(1);
				    log.info("Inserted Hub Id : " + hubId);
				    return hubId;
				}
				//This is used if mutiple threads try to insert at the same time
				else if (insertHubAffectedRows == 0){
					log.info("Inserted rows affected is 0");
					preparedStatement = conn.prepareStatement(getHubId);
					preparedStatement.setString(1, hubPlatform);
					preparedStatement.setString(2, hubUrl);
					log.info("query for HubId: " + preparedStatement);
					
					rs = preparedStatement.executeQuery();
					rs.next();
					
					int hubId = rs.getInt(1);
					log.info("No rows inserted so query recevied hubid : " + rs.getInt(1));
					return hubId;
				}
				else{
					log.error("HubInfoDAO.insertHubInfo(). This is not expected");
				}
			}
			else{
				log.info("Hub entry is already present in table, so will check for status changed");

				int hubId = rs.getInt(1);
				String hubStatusInTable = rs.getString(2);
				
				int hubStatusInRequest = device.getHubStatus();
				String sqlHubStatusInRequest = null;
				log.info("calling Util.getStringEquivalentOfStatus for hubstatus to update Hub table");
				sqlHubStatusInRequest = Util.getStringEquivalentOfStatus(hubStatusInRequest);
				if (sqlHubStatusInRequest == null){
					return -1;
				}
				log.info("Hub Stbatus in request (Device object)  = " + sqlHubStatusInRequest + ". This should be available if from appium node");
				updateHubStatusIfDifferent(hubId, hubStatusInTable, sqlHubStatusInRequest);
				
				return hubId;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			/*if (conn != null){
				try {
					conn.commit();
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/
			Util.closeDBResources(rs, null, preparedStatement, conn);
		}
		return -1;
		
	}

	private static void updateHubStatusIfDifferent(int hubId,
			String hubStatusInTable, String sqlHubStatusInRequest) {
		
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			if (hubStatusInTable.equalsIgnoreCase(sqlHubStatusInRequest)){
				log.error("Hub status is equal to the request and already present in table. This is not expected " +
							"if the auto checker for the hub or grid if enabled. Check the function of hub or grid check if enabled");
			}
			else{
				log.info("updating the hub status in the table with the status in reques. Table: " + hubStatusInTable +
						"Request: " + sqlHubStatusInRequest);
				
				preparedStatement = conn.prepareStatement(updateHubStatus);
				preparedStatement.setString(1, sqlHubStatusInRequest);
				preparedStatement.setInt(2, hubId);
				
				if (preparedStatement.executeUpdate() == 1){
					log.info("device status is updated successfully. From -> " + hubStatusInTable + ". To -> " + sqlHubStatusInRequest);
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(rs, null, preparedStatement, null);
		}
	}

	//status should be passed as either AVAILABLE or BUSY
	public static String getGridUrl(DeviceConfig deviceConfig, HubOrDeviceStatus status){
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet res = null;
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			pstmt = conn.prepareStatement(getGridUrlQuery);
			
			String device = deviceConfig.getDevice();
			String version = deviceConfig.getVersion();
			
			boolean isEmulatorBoolean = deviceConfig.getIsEmulator();
			int isEmulatorInt = Util.getIntEquivalentOfBoolean(isEmulatorBoolean);
			
			/**
			 * "SELECT H.hubUrl,D.status from MTAAS.Hub H inner join MTAAS.Device D on H.hubId = D.hubId "
			+ "where D.platform=? and D.osVersion=? and H.status=? and isEmulator=? and manufacturer=? and model=?";
			 */
			pstmt.setString(1, device);
			pstmt.setString(2, version);
			pstmt.setString(3, status.name());
			pstmt.setInt(4, isEmulatorInt);

			if (isEmulatorBoolean){
				pstmt.setString(5, MtaasConstants.EMULATOR_MANUFACTURER);
				pstmt.setString(6, MtaasConstants.EMULATOR_MODEL);
			}
			else{
				pstmt.setString(5, deviceConfig.getManufacturer());
				pstmt.setString(6, deviceConfig.getModel());
				
			}
			
			pstmt.setString(7, HubOrDeviceStatus.AVAILABLE.name());
			log.info("getGridUrl query: " + pstmt);

			res = pstmt.executeQuery();

			if (status.name().equalsIgnoreCase(HubOrDeviceStatus.BUSY.name())){
				return getRandomBusyGridUrl(res);
			}
			else if (res.next()){
				String url = res.getString(1);
				log.info("Available grid url=" + url);
				return url;
			} 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(res, null, pstmt, conn);
		}
		return null;
	}

	/**
	 * Method to get the available Hub having the status Available. Else if all hub is running then get the random hub. 
		Else if all the hub in unavailable then return null
	 * @param deviceConfig
	 * @return
	 */
	public static String selectHub(DeviceConfig deviceConfig) {
		
		/*String device = deviceConfig.getDevice();
		String version = deviceConfig.getVersion();*/
		
		log.info("Trying for first available grid");
		String firstAvailableGridUrl = getGridUrl(deviceConfig,HubOrDeviceStatus.AVAILABLE);
		
		if (null != firstAvailableGridUrl){
			log.info("found available Grid so returning. HubUrl:" + firstAvailableGridUrl);
			HubInfoDAO.updateHubStatusWithHubUrl(firstAvailableGridUrl, HubOrDeviceStatus.BUSY);
			return firstAvailableGridUrl;
		}
		else{
			log.info("trying for random available grid since no grid is available");
			String randomBusyGridUrl =  getGridUrl(deviceConfig,HubOrDeviceStatus.BUSY);
			
			if (null != randomBusyGridUrl){
				log.info("found busy Grid so returning. HubUrl:" + randomBusyGridUrl);
				return randomBusyGridUrl;
				
			}
			else{
				log.info("All grid are unavailable. See above log to find whether grid is unavialble or only device is unavailable");
				return null;
			}
		}
	}
	
	private static String getRandomBusyGridUrl(ResultSet res) throws SQLException {
		List<String> gridUrlList = new ArrayList<String>();

		while(res.next()){
			String gridUrl = res.getString(1);
			gridUrlList.add(gridUrl);
		}
		
		if (gridUrlList.isEmpty()){
			log.warn("gridUrlList is empty, returning null grid url");
			return null;
		}
		
		if (gridUrlList.size() == 1){
			return gridUrlList.get(0);
		}
		
		int randomInt = randInt(0, gridUrlList.size());
		return gridUrlList.get(randomInt);
		
	}
	
	public static int randInt(int min, int max) {

	    Random rand = new Random();
	    int randomNum = rand.nextInt(max - min) + min;

	    return randomNum;
	}

	public static int getGridId(String deviceName, String gridUrl) {
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			st = conn.createStatement();

			String getDeviceId = String.format(getDeviceIdQuery,deviceName,gridUrl);
			
			log.info("query: " + getDeviceId);

			res = st.executeQuery(getDeviceId);
			if (res.next()){
				int uniqueId= res.getInt(1);
				return uniqueId;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(res, st, null, conn);
		}
		return -1;
	}
	
	public static Map<String,HubOrDeviceStatus> getAllHubUrl() {
		Map<String,HubOrDeviceStatus> hubUrls = new HashMap<String,HubOrDeviceStatus>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			stmt = conn.createStatement();
			
			log.info("getAllHubUrl: " + getAllHubUrl);
			
			rs = stmt.executeQuery(getAllHubUrl);
			
			while (rs.next()){
				String hubUrl = rs.getString(1);
				String status = rs.getString(2);
				status = status.toUpperCase();
				
				HubOrDeviceStatus hubStatus = HubOrDeviceStatus.valueOf(status);
				hubUrls.put(hubUrl,hubStatus);
			}
			
		} catch (Exception e){
			log.error("exception caught in getGridAllHubUrl", e);
		} finally {
			Util.closeDBResources(rs, stmt, null, conn);
			/*try {
				stmt.close();
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		
		return hubUrls;
	}
	
	public static int updateHubStatusWithHubUrl(String hubUrl, HubOrDeviceStatus hubStatus) {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
//			String updateTestResult = String.format(updateHubStatusWithHubUrl,hubOrDeviceStatus.toString(),hubUrl);
			
			preparedStatement = conn.prepareStatement(updateHubStatusWithHubUrl);
			preparedStatement.setString(1, hubStatus.toString());
			preparedStatement.setString(2, hubUrl + "%");

			log.info("updateHubStatusWithHubUrl query: " + preparedStatement);
			
			if (preparedStatement.executeUpdate() == 1){
			    return 1;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(rs, null, preparedStatement, conn);
		}
		return -1;
		
	}
	
	public static int updateHubStatusWithHubId(int hubId, HubOrDeviceStatus hubStatus) {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
//			String updateTestResult = String.format(updateHubStatusWithHubUrl,hubOrDeviceStatus.toString(),hubUrl);
			
			preparedStatement = conn.prepareStatement(updateHubStatusWithHubId);
			preparedStatement.setString(1, hubStatus.toString());
			preparedStatement.setInt(2, hubId);

			log.info("updateHubStatusWithHubId query: " + preparedStatement);
			
			if (preparedStatement.executeUpdate() == 1){
			    return 1;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(rs, null, preparedStatement, conn);
		}
		return -1;
		
	}
	
}
