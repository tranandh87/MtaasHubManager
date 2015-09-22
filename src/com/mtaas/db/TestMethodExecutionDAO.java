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

import com.mtaas.bean.TestInfo;
import com.mtaas.util.Util;

public class TestMethodExecutionDAO {
	private static Logger log = Logger.getLogger(TestMethodExecutionDAO.class.getName());
	
	private static String getTestMethodsConfigFromTestExecId = "select tme.id,tme.status, tm.name from MTAAS.TestMethod tm " 
			+ "INNER JOIN MTAAS.TestMethodExecutionResult tme on tme.testMethodId = tm.id "
			+ "where tme.testExecutionId = %d";
	
	private static String getFailedTestMethodsNameFromReqeustId = "select distinct tm.name from MTAAS.TestMethod tm " +
							"inner join MTAAS.TestMethodExecutionResult tme on tm.id = tme.testMethodId " +
							"inner join MTAAS.TestExecutionResult te on te.Id = tme.testExecutionId " +
							"inner join MTAAS.Test t on te.testId = t.id " +
							"inner join MTAAS.TestSuiteExecutionResult tse on tse.id = te.testSuiteExecutionId " +
							"inner join MTAAS.Request r on tse.requestId = r.id " +
							"where tse.status != 'pass' and te.status != 'pass' and "
							+ "tme.status != 'pass' and r.id =? and tme.deviceId =? and t.name = ?";

	public static List<TestInfo> getTestMethodsConfigFromTestExecutionId(int testExecId) {
		
		List<TestInfo> testMethodsConfig = new ArrayList<TestInfo>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			stmt = conn.createStatement();
			String query = String.format(getTestMethodsConfigFromTestExecId, testExecId);
			log.info("executionQuery: " + query);
			rs = stmt.executeQuery(query);
			
			while (rs.next()){
				TestInfo testMethodConfig = new TestInfo();
				testMethodConfig.setTestMethodExecutionId(rs.getInt(1));
				testMethodConfig.setTestMethodStatus(rs.getString(2));
				testMethodConfig.setTestMethodName(rs.getString(3));
				
				testMethodsConfig.add(testMethodConfig);
			}
		} catch (Exception e){
			log.error("Exception in getTestMethodsConfigFromTestExecutionId", e);
		} finally {
			Util.closeDBResources(rs, stmt, null, conn);
		}
		return testMethodsConfig;
	}

	public static List<String> getFailedTestMethodsNameFromRequestAndTestngParams(
			int requestId, Map<String, String> testngParams, String className) {
		List<String> methodsName = null; 
		
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet res = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			/*String getFailedTestMethodsNameFromReqeustIdQuery = String.format(
					getFailedTestMethodsNameFromReqeustId, requestId);*/
			preparedStatement = conn.prepareStatement(getFailedTestMethodsNameFromReqeustId);
			
			int deviceId = Util.getDeviceIdFromTestngParams(testngParams);
			
			preparedStatement.setInt(1, requestId);
			preparedStatement.setInt(2, deviceId);
			preparedStatement.setString(3, className);
			
			log.info("getFailedTestMethodsNameFromReqeustIdQuery query: " + preparedStatement);
			
			res = preparedStatement.executeQuery();
			
			methodsName = new ArrayList<String>();
			while (res.next()){
				methodsName.add(res.getString(1));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(res, null, preparedStatement, conn);
		}
		return methodsName;
	}

}
