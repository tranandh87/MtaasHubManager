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

import org.apache.log4j.Logger;

import com.mtaas.bean.Device;
import com.mtaas.bean.DeviceConfig;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.MtaasConstants.HubOrDeviceStatus;
import com.mtaas.util.Util;

public class DeviceInfoDAO {

	private static Logger log = Logger.getLogger(DeviceInfoDAO.class.getName());
	
	private static final String getAllDevices = "Select deviceId, platform, osVersion, isEmulator, manufacturer, model from MTAAS.Device where status <> 'Unavailable';";
	
	//This query is used to check for updating device table with a only 1 kind of device (platform and osVersion) in a single hub.
	private static final String getDeviceInfo = "select deviceId,status,appiumIp "
			+ "from MTAAS.Device "
			+ "where platform = ? and osVersion = ? and `hubId` = ? "
			+ "and `manufacturer` = ? and `appiumIp` = ? and `isEmulator` = ? and `model` = ?" ;
	
	//private static final String getDeviceInfo = "select deviceId from MTAAS.Device where platform='%s' and osVersion='%s' and hubId=%d and appiumIp='%s'";
	
	private static final String insertDeviceInfo = "INSERT IGNORE INTO MTAAS.Device(`platform`,`osVersion`,`hubId`,"
			+ "`manufacturer`,`appiumIp`,`isEmulator`,`status`,`model`) "
			+ "VALUES (?,?,?,?,?,?,?,?)";
	
	private static final String getDeviceConfigFromTestMethodExecId = "select d.deviceId,platform,osVersion,manufacturer,isEmulator,model "
			+ "from MTAAS.Device d "
			+ "inner join MTAAS.TestMethodExecutionResult tm on tm.deviceId = d.deviceId where tm.id =%d";
	
	private static final String getDeviceConfigFromTestExecId = "select d.deviceId,platform,osVersion,manufacturer,isEmulator,model "
			+ "from MTAAS.Device d "
			+ "inner join MTAAS.TestExecutionResult t on t.deviceId = d.deviceId where t.id =%d";
	
	private static final String getDeviceConfigFromDeviceId = "select platform,osVersion,isEmulator,manufacturer,model "
																+ "from MTAAS.Device " +
																" where deviceId =%d";
	
	private static final String updateDeviceStatus = "update MTAAS.Device set status=? WHERE deviceId=?";
	
	private static final String updateAppiumNodeIp = "update MTAAS.Device set appiumIp=? WHERE deviceId=?";
	
	private static final String getHubIdFromDeviceId = "select hubId from MTAAS.Device where deviceId=%d";
	
	private static final String getAllAppiumIpFromDevice = "Select appiumIp,status from MTAAS.Device";
	
	private static final String updateDeviceStatusWithAppiumIp = "update MTAAS.Device set status = ? WHERE appiumIp like ?";
	
	private static final String getAppiumIpFromDeviceId = "select appiumIp from MTAAS.Device where deviceId=%d";
	
	public static int insertDeviceInfo(Device device, int hubId){
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			int isEmulator = Util.getIntEquivalentOfBoolean(device.getIsEmulator());
			
			/*String queryDeviceInfo = String.format(getDeviceInfo,device.getDevicePlatform(),device.getDeviceVersion(),
					hubId,device.getManufacturer(),device.getAppiumNodeIp(),isEmulator,device.getModel());*/
			preparedStatement = conn.prepareStatement(getDeviceInfo);
			
			String devicePlatform = device.getDevicePlatform();
			String model = device.getModel();
			String deviceVersion = device.getDeviceVersion();
			String manufacturer = device.getManufacturer();
			String appiumNodeIp = device.getAppiumNodeIp();
			
			preparedStatement.setString(1, devicePlatform);
			preparedStatement.setString(2, deviceVersion);
			preparedStatement.setInt(3, hubId);
			preparedStatement.setString(4,manufacturer);
			preparedStatement.setString(5, appiumNodeIp);
			preparedStatement.setInt(6, isEmulator);
			preparedStatement.setString(7, model);
			
			log.info("queryDeviceInfo query: " + preparedStatement);
			rs = preparedStatement.executeQuery();
			
			if (!rs.next()){
			
				preparedStatement = conn.prepareStatement(insertDeviceInfo, Statement.RETURN_GENERATED_KEYS);
				
				int deviceStatus = device.getDeviceStatus();
				String sqlDeviceStatus = null;
				log.info("calling Util.getStringEquivalentOfStatus for deviceStatus to insert new row in Device table");
				sqlDeviceStatus = Util.getStringEquivalentOfStatus(deviceStatus);
				log.info("Received Status : " + sqlDeviceStatus);
				if (sqlDeviceStatus == null)
					return -1;
				
				preparedStatement.setString(1, devicePlatform);
				preparedStatement.setString(2, deviceVersion);
				preparedStatement.setInt(3, hubId);
				preparedStatement.setString(4, manufacturer);
				preparedStatement.setString(5, appiumNodeIp);
				preparedStatement.setInt(6, isEmulator);
				preparedStatement.setString(7, sqlDeviceStatus);
				preparedStatement.setString(8, model);
				
				log.info("insertDeviceInfo query: " + preparedStatement);
				int deviceRowAffected = preparedStatement.executeUpdate();
				
				if (deviceRowAffected == 1){
					rs = preparedStatement.getGeneratedKeys();
				    rs.next();
				    int deviceId = rs.getInt(1);
				    log.info("Inserted deviced id : " + deviceId);
				    return deviceId;
				}
				else if (deviceRowAffected == 0){
					log.info("Inserted device rows affected is 0");
					preparedStatement = conn.prepareStatement(getDeviceInfo);
					
					preparedStatement.setString(1, devicePlatform);
					preparedStatement.setString(2, deviceVersion);
					preparedStatement.setInt(3, hubId);
					preparedStatement.setString(4,manufacturer);
					preparedStatement.setString(5, appiumNodeIp);
					preparedStatement.setInt(6, isEmulator);
					preparedStatement.setString(7, model);
					
					log.info("queryDeviceInfo query: " + preparedStatement);
					rs = preparedStatement.executeQuery();
					
					rs.next();
					
					int hubIdQueried = rs.getInt(1);
					log.info("No rows inserted so query recevied hubid : " + hubIdQueried);
					return hubIdQueried;
				}
				else{
					log.error("HubInfoDAO.insertHubInfo(). This is not expected");
				}
			}
			else{
				int deviceId = rs.getInt(1);
				String deviceStatusInTable = rs.getString(2);
				String appiumIpInTable = rs.getString(3);
				
				log.info("device already present in Device table. deviceId : " + rs.getInt(1));
				log.info("Device Status in table = " + deviceStatusInTable);
				
				int deviceStatusInRequest = device.getDeviceStatus();
				String sqlDeviceStatusInRequest = null;
				log.info("calling Util.getStringEquivalentOfStatus for deviceStatus to update device table");
				sqlDeviceStatusInRequest = Util.getStringEquivalentOfStatus(deviceStatusInRequest);
				if (sqlDeviceStatusInRequest == null){
					return -1;
				}
				log.info("Device Status in request (Device object)  = " + sqlDeviceStatusInRequest + ". This should be available if from appium node");
				updateDeviceStatusIfDifferent(deviceId, deviceStatusInTable, sqlDeviceStatusInRequest);
				
				updateAppiumNodeIpIfDifferent(deviceId,appiumIpInTable,appiumNodeIp);
				
				return deviceId;
				
				/*int currentHubId = rs.getInt("hubId");
				if (currentHubId != hubId){
					log.info("hubId is different, so going to update hubid");
					int currentDeviceId = rs.getInt("deviceId");
					String updateDeviceInfo = String.format(updateDeviceHubId, currentHubId,currentDeviceId);
					
					preparedStatement = conn.prepareStatement(updateDeviceInfo);

					log.debug("updateDeviceInfo query: " + preparedStatement);
					
					if (preparedStatement.executeUpdate() == 1){
						return rs.getInt(1);
					}
				}
				else{
					log.info("hub id is same so not performing any action");
					return rs.getInt(1);
				}*/
				
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(rs, null, preparedStatement, null);
		}
		return -1;
		
	}

	private static void updateAppiumNodeIpIfDifferent(int deviceId,
			String appiumNodeIpInTable, String appiumNodeIpInRequest) {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			if (appiumNodeIpInTable.equalsIgnoreCase(appiumNodeIpInRequest)){
				log.info("appium node ip is equal to the request and already present in table. This means " +
							"the device was previously connected to the same system but registering again.");
			}
			else{
				log.info("updating the appium node ip in the table with the appium node ip in request. Table: " + appiumNodeIpInTable +
						". Request: " + appiumNodeIpInRequest);
				 
				preparedStatement = conn.prepareStatement(updateAppiumNodeIp);
				preparedStatement.setString(1, appiumNodeIpInRequest);
				preparedStatement.setInt(2, deviceId);
				
				if (preparedStatement.executeUpdate() == 1){
					log.info("device appiumNodeIp is updated successfully. From -> " + appiumNodeIpInTable + ". To -> " + appiumNodeIpInRequest);
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

	private static void updateDeviceStatusIfDifferent(int deviceId,
			String deviceStatusInTable, String sqlDeviceStatusInRequest) {
		
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			if (deviceStatusInTable.equalsIgnoreCase(sqlDeviceStatusInRequest)){
				log.info("device status is equal to the request and already present in table. This is not expected " +
							"if the auto checker for the node or device is enabled. Check the function of check if enabled");
			}
			else{
				log.info("updating the device status in the table with the status in reques. Table: " + deviceStatusInTable +
						"Request: " + sqlDeviceStatusInRequest);
				
				preparedStatement = conn.prepareStatement(updateDeviceStatus);
				preparedStatement.setString(1, sqlDeviceStatusInRequest);
				preparedStatement.setInt(2, deviceId);
				
				if (preparedStatement.executeUpdate() == 1){
					log.info("device status is updated successfully. From -> " + deviceStatusInTable + ". To -> " + sqlDeviceStatusInRequest);
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

	public List<DeviceConfig> getDevices(){
		List<DeviceConfig> devList = new ArrayList<DeviceConfig>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(getAllDevices);
			while (rs.next()){
				String platform = rs.getString(MtaasConstants.PLATFORM_COL);
				String version = rs.getString(MtaasConstants.OS_VERSION_COL);
				boolean isEmulator = rs.getInt(MtaasConstants.IS_EMULATOR_COL) == 1;
				
				DeviceConfig dev = new DeviceConfig();
				dev.setDevice(platform);
				dev.setVersion(version);
				dev.setIsEmulator(isEmulator);
				dev.setManufacturer(rs.getString(MtaasConstants.MANUFACTURER_COL));
				dev.setModel(rs.getString(MtaasConstants.MODEL_COL));
				dev.setDeviceConfigId(rs.getInt(MtaasConstants.DEVICE_ID));
				
				devList.add(dev);
			}
		} catch (Exception e){
			log.error("exception caught in getDevices", e);
		} finally {
//			closeDbResources(null, stmt, conn);
			Util.closeDBResources(rs, stmt, null, conn);
		}
		
		return devList;
	}

	public static DeviceConfig getDeviceConfigFromTestMethodExecId(int testMethodExecId) {
		// TODO Auto-generated method stub
		DeviceConfig deviceConfig = null;
		
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			String getDeviceConfigFromTestMethodIdQuery = String.format(getDeviceConfigFromTestMethodExecId, testMethodExecId);

			log.info("getDeviceConfigFromTestMethodExecId query: " + getDeviceConfigFromTestMethodIdQuery);
			
			st = conn.createStatement();

			res = st.executeQuery(getDeviceConfigFromTestMethodIdQuery);
			if (res.next()){
				deviceConfig = new DeviceConfig();
				
				deviceConfig.setDeviceConfigId(res.getInt(1));
				deviceConfig.setPlatform(res.getString(2));
				deviceConfig.setVersion(res.getString(3));
				deviceConfig.setManufacturer(res.getString(4));
				
				boolean isEmulator = Util.getBooleanEquivalentOfInt(res.getInt(5));
				deviceConfig.setIsEmulator(isEmulator);
				
				deviceConfig.setModel(res.getString(6));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(res, st, null, conn);
		}
		return deviceConfig;
	}
	
	public static DeviceConfig getDeviceConfigFromTestExecId(int testExecId) {
		// TODO Auto-generated method stub
		DeviceConfig deviceConfig = null;
		
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			String getDeviceConfigFromTestExecIdQuery = String.format(getDeviceConfigFromTestExecId, testExecId);

			log.info("getDeviceConfigFromTestExecIdQuery query: " + getDeviceConfigFromTestExecIdQuery);
			
			st = conn.createStatement();

			res = st.executeQuery(getDeviceConfigFromTestExecIdQuery);
			if (res.next()){
				deviceConfig = new DeviceConfig();
				
				deviceConfig.setDeviceConfigId(res.getInt(1));
				deviceConfig.setPlatform(res.getString(2));
				deviceConfig.setVersion(res.getString(3));
				deviceConfig.setManufacturer(res.getString(4));
				
				boolean isEmulator = Util.getBooleanEquivalentOfInt(res.getInt(5));
				deviceConfig.setIsEmulator(isEmulator);
				
				deviceConfig.setModel(res.getString(6));
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(res, st, null, conn);
		}
		return deviceConfig;
	}

	public static int getHubIdFromDeviceId(int deviceId) {
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		int hubId = -1;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			String hubIdFromDeviceIdQuery = String.format(getHubIdFromDeviceId, deviceId);

			log.info("getHubIdFromDeviceId query: " + hubIdFromDeviceIdQuery);
			
			st = conn.createStatement();

			res = st.executeQuery(hubIdFromDeviceIdQuery);
			if (res.next()){
				hubId = res.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(res, st, null, conn);
		}
		return hubId;
	}

	public static Map<String, HubOrDeviceStatus> getAllAppiumNodeIp() {
		Map<String,HubOrDeviceStatus> appiumIps = new HashMap<String,HubOrDeviceStatus>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			stmt = conn.createStatement();
			
			log.info("getAllAppiumNodeIp: " + getAllAppiumIpFromDevice);
			rs = stmt.executeQuery(getAllAppiumIpFromDevice);
			
			while (rs.next()){
				String appiumIp = rs.getString(1);
				String status = rs.getString(2);
				status = status.toUpperCase();
				
				HubOrDeviceStatus deviceStatus = HubOrDeviceStatus.valueOf(status);
				appiumIps.put(appiumIp,deviceStatus);
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
		
		return appiumIps;
	}

	public static int updateDeviceStatusWithAppiumIp(String appiumIp,
			HubOrDeviceStatus deviceStatus) {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			preparedStatement = conn.prepareStatement(updateDeviceStatusWithAppiumIp);
			preparedStatement.setString(1, deviceStatus.toString());
			preparedStatement.setString(2, appiumIp + "%");

			log.info("updateDeviceStatusWithAppiumIp query: " + preparedStatement);
			
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

	public static DeviceConfig getDeviceConfigFromDeviceId(int deviceId) {
		DeviceConfig deviceConfig = null;
		
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			String getDeviceConfigFromDeviceIdQuery = String.format(getDeviceConfigFromDeviceId, deviceId);

			log.info("getDeviceConfigFromDeviceIdQuery query: " + getDeviceConfigFromDeviceIdQuery);
			
			st = conn.createStatement();

			res = st.executeQuery(getDeviceConfigFromDeviceIdQuery);
			if (res.next()){
				deviceConfig = new DeviceConfig();
				
				deviceConfig.setDevice(res.getString(1));
				deviceConfig.setVersion(res.getString(2));
				
				int isEmulatorInt = res.getInt(3);
				boolean isEmulatorBoolean = Util.getBooleanEquivalentOfInt(isEmulatorInt);
				deviceConfig.setIsEmulator(isEmulatorBoolean);
				
				deviceConfig.setManufacturer(res.getString(4));
				deviceConfig.setModel(res.getString(5));
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(res, st, null, conn);
		}
		return deviceConfig;
	}
	
	public static String getAppiumIpFromDeviceId(int deviceId){
		
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			String getAppiumIpFromDeviceIdQuery = String.format(getAppiumIpFromDeviceId, deviceId);

			log.info("getAppiumIpFromDeviceIdQuery query: " + getAppiumIpFromDeviceIdQuery);
			
			st = conn.createStatement();

			res = st.executeQuery(getAppiumIpFromDeviceIdQuery);
			if (res.next()){
				return res.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(res, st, null, conn);
		}
		return null;
	}

	/*private void closeDbResources(ResultSet rs, Statement stmt, Connection conn){
			try {
				if (rs != null){
					rs.close();
				}
				if (stmt != null){
					stmt.close();
				}
				if (conn != null){
					conn.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}*/

}
