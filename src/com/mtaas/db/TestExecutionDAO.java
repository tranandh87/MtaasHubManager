package com.mtaas.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.mtaas.bean.TestExecution;
import com.mtaas.bean.TestMethodExecution;
import com.mtaas.bean.TestSuiteExecution;
import com.mtaas.util.Util;

public class TestExecutionDAO {
	private static Logger log = Logger.getLogger(TestExecution.class.getName());
	
	private static String getUniqueIdWithIntQuery = "SELECT %s from MTAAS.%s where %s = %d";
	
	private static String getUniqueIdWithStringQuery = "SELECT %s from MTAAS.%s where %s = '%s'";
	
	private static String getDeviceIdQuery = "SELECT deviceId from MTAAS.Device "
			+ "where platform = ? and osVersion =? and hubId=? and isEmulator =? and manufacturer =? and model =? and status='Available'";
	
	private static String updateTestMethodExecution = "INSERT INTO MTAAS.TestMethodExecutionResult(`testMethodId`,`testExecutionId`,`deviceId`,`status`,`executionStartTime`) VALUES (?,?,?,?,?)";
	
	private static String updateTestSuiteExecution = "INSERT INTO MTAAS.TestSuiteExecutionResult(`testSuiteId`,`deviceId`,`status`,`executionStartTime`, requestId) VALUES (?,?,?,?,?)";
	
	private static String updateTestExecution = "INSERT INTO MTAAS.TestExecutionResult(`testId`,`testSuiteExecutionId`,`deviceId`,`status`, `executionStartTime`) VALUES (?,?,?,?,?)";
	
	/**use this method to get the unique id of executionresult table by passing column name "name" and value
	 * @param conn
	 * @param tableName = TestExecutionResult, TestMethodExecutionResult, SuiteExecutionResult
	 * @param conditionColumnName = name, id
	 * @param value - pass either Integer or String
	 * @return
	 */
	public static int getUniqueId(String tableName, String retrieveColumnValue, String conditionColumnName, Object value){
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		try {
			conn = new DatabaseConnection().getDbConnectionFromPool();
			String getUniqueId = null;
			
			if (value instanceof Integer){
				int intValue = ((Integer)value).intValue();
				getUniqueId = String.format(getUniqueIdWithIntQuery, retrieveColumnValue, 
						tableName, conditionColumnName, intValue);
			}
			else if (value instanceof String){
				String stringValue = (String)value;
				getUniqueId = String.format(getUniqueIdWithStringQuery, retrieveColumnValue, 
						tableName, conditionColumnName, stringValue);
			}
			
			log.info("getUniqueId query: " + getUniqueId);
			
			st = conn.createStatement();

			res = st.executeQuery(getUniqueId);
			if (res.next()){
				int uniqueId= res.getInt(1);
				return uniqueId;
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
		return -1;
		
	}

	public static int getDeviceId (String platform, String version, int hubId, int isEmulator,String manufacturer,String model){
		PreparedStatement st = null;
		ResultSet res = null;
		Connection conn = null;
		try {
			conn = new DatabaseConnection().getDbConnectionFromPool();
			st = conn.prepareStatement(getDeviceIdQuery);
			
			/*
			 * "where platform = ? and osVersion =? and hubId=? and isEmulator =? and manufacturer =? and model =? and status='Available'";
			 */
			st.setString(1, platform);
			st.setString(2, version);
			st.setInt(3, hubId);
			st.setInt(4, isEmulator);
			st.setString(5, manufacturer);
			st.setString(6, model);
			
			log.info("getDeviceIdQuery: " + st);

			res = st.executeQuery();
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
	
	public static int insertTestMethodExecution(TestMethodExecution methodExecutionBean){
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = new DatabaseConnection().getDbConnectionFromPool();
			preparedStatement = conn.prepareStatement(updateTestMethodExecution, Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setInt(1, methodExecutionBean.getTestMethodId());
			preparedStatement.setInt(2, methodExecutionBean.getTestExecutionId());
			preparedStatement.setInt(3, methodExecutionBean.getDeviceId());
			preparedStatement.setObject(4, methodExecutionBean.getStatus().name());
			preparedStatement.setTimestamp(5, methodExecutionBean.getExecutionStartTime());
			
			log.info("insertTestMethodExecution query: " + preparedStatement);
			if (preparedStatement.executeUpdate() == 1){
				rs = preparedStatement.getGeneratedKeys();
			    rs.next();
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
	
	public static int insertTestExecution(TestExecution testExecution){
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = new DatabaseConnection().getDbConnectionFromPool();
			preparedStatement = conn.prepareStatement(updateTestExecution, Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setInt(1, testExecution.getTestId());
			preparedStatement.setInt(2, testExecution.getTestSuiteId());
			preparedStatement.setInt(3, testExecution.getDeviceId());
			preparedStatement.setString(4, testExecution.getStatus().name());
			preparedStatement.setTimestamp(5, testExecution.getExecutionStartTime());
			
			log.info("insertTestExecution query: " + preparedStatement);
			if (preparedStatement.executeUpdate() == 1){
				rs = preparedStatement.getGeneratedKeys();
			    rs.next();
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
	
	public static int insertTestSuiteExecution(TestSuiteExecution testSuiteExecution, int reqId){
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = new DatabaseConnection().getDbConnectionFromPool();
			preparedStatement = conn.prepareStatement(updateTestSuiteExecution, Statement.RETURN_GENERATED_KEYS);
			
			preparedStatement.setInt(1, testSuiteExecution.getTestSuiteId());
			preparedStatement.setInt(2, testSuiteExecution.getDeviceId());
			preparedStatement.setString(3, testSuiteExecution.getStatus().name());
			preparedStatement.setTimestamp(4, testSuiteExecution.getExecutionStartTime());
			preparedStatement.setInt(5, reqId);
			
			log.info("insertTestSuiteExecution query: " + preparedStatement);
			if (preparedStatement.executeUpdate() == 1){
				rs = preparedStatement.getGeneratedKeys();
			    rs.next();
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


}
