package com.mtaas.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.mtaas.bean.AutDetails;
import com.mtaas.bean.Device;
import com.mtaas.bean.DeviceConfig;
import com.mtaas.util.Util;

public class AUTDetailDAO {
	
	private static Logger log = Logger.getLogger(AUTDetailDAO.class.getName());
	
//	private static String insertAUTDetail = "insert into MTAAS.AUTDetails(`appActivity`,`packageName`) values(?,?)";
	
	private static String insertAUTDetail = "insert IGNORE into MTAAS.AUTDetails(`appActivity`,`packageName`,`appName`) values(?,?,?)";
	
	private static String getAUTDetailId = "select id from MTAAS.AUTDetails where appActivity = ? and packageName = ? and appName = ?";
	
	private static final String getAutDetailFromRequestId = "select appActivity,packageName,appName from MTAAS.AUTDetails ad "
			+ "inner join MTAAS.Request r on r.AUTDetailId = ad.id where r.id =%d";
	
	public static int insertAUTDetail(AutDetails autDetail){
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
//		String hubUrl = Util.buildHubUrl(device.getHubIp(), device.getHubPort());
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			String appActivity = autDetail.getAppActivity();
			String packageName = autDetail.getPackageName();
			String appName = autDetail.getAppName();
			
//			String getAUTDetailsIdQuery = String.format(getAUTDetailId,appActivity,packageName,appName);
			preparedStatement = conn.prepareStatement(getAUTDetailId);
			preparedStatement.setString(1, appActivity);
			preparedStatement.setString(2, packageName);
			preparedStatement.setString(3, appName);
			
			log.info("getAUTDetailsIdQuery query: " + preparedStatement);
			rs = preparedStatement.executeQuery();
			
			if (!rs.next()){
			
//				if (null != appName){
					preparedStatement = conn.prepareStatement(insertAUTDetail, Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setString(3, appName);
/*				}
				else{
					preparedStatement = conn.prepareStatement(insertAUTDetail, Statement.RETURN_GENERATED_KEYS);
				}*/
				
				preparedStatement.setString(1, appActivity);
				preparedStatement.setString(2, packageName);
				
				log.info("insertAUTDetail query: " + preparedStatement);
				int affectedRows = preparedStatement.executeUpdate();
				
				if (affectedRows == 1){
					rs = preparedStatement.getGeneratedKeys();
				    rs.next();
				    int autId = rs.getInt(1);
				    log.info("Aut detailed inserted with id : " + autId);
				    return autId;
				}
				else if (affectedRows == 0){
					log.info("Inserted device rows affected is 0");
					preparedStatement = conn.prepareStatement(getAUTDetailId);
					preparedStatement.setString(1, appActivity);
					preparedStatement.setString(2, packageName);
					preparedStatement.setString(3, appName);
					
					log.info("getAUTDetailsIdQuery query: " + preparedStatement);
					rs = preparedStatement.executeQuery();
					
					rs.next();
					
					int autId = rs.getInt(1);
					log.info("AutId received from query : " + autId);
					return autId;
				}
				else{
					log.error("AUTDetail.insertAUTDetail(). This is not expected");
				}
			}
			else{
				log.info("Aut Detail is already present in table, so returning the existing id");
				return rs.getInt(1);
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
	
	public static AutDetails getAutDetailsFromRequestId(int requestId) {
		AutDetails autDetails = null;
		
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			String getAutDetailFromRequestIdQuery = String.format(getAutDetailFromRequestId, requestId);

			log.info("getAutDetailFromRequestIdQuery query: " + getAutDetailFromRequestIdQuery);
			
			st = conn.createStatement();

			res = st.executeQuery(getAutDetailFromRequestIdQuery);
			if (res.next()){
				autDetails = new AutDetails();
				
				autDetails.setAppActivity(res.getString(1));
				autDetails.setPackageName(res.getString(2));
				autDetails.setAppName(res.getString(3));
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
		return autDetails;
	}



}
