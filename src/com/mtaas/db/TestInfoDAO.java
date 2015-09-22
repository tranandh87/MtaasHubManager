package com.mtaas.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.mtaas.bean.TestInfo;
import com.mtaas.bean.TestInfo;
import com.mtaas.util.Util;

public class TestInfoDAO {
	private static Logger log = Logger.getLogger(TestInfoDAO.class.getName());
	
	private static String suiteIdQuery = "select id from MTAAS.TestSuite where name='%s'";
	
//	private static String queryTestIdForUpdate = "select id from MTAAS.Test where name='%s' FOR UPDATE";
	private static String queryTestIdForUpdate = "select id from MTAAS.Test where name='%s'";
	
	private static String updateTestMethod = "INSERT IGNORE INTO MTAAS.TestMethod(`name`, `testId`) VALUES (?,?)";
	
	private static String queryTestId = "select id from MTAAS.Test where name='%s'";
	
	private static String updateTestName = "INSERT IGNORE INTO MTAAS.Test(`name`, `testSuiteId`) VALUES (?,?)";
	
	private static String updateSuiteName = "INSERT IGNORE INTO MTAAS.TestSuite(`name`) VALUES(?)";
	
	private static String queryMethodName = "select id from MTAAS.TestMethod where name='%s'";
	
	private static String getTestConfigFromTestMethodExecId = "select te.id, t.name,te.status from MTAAS.Test t " 
									+ "inner join MTAAS.TestExecutionResult te on t.id = te.testId "
									+ "inner join MTAAS.TestMethodExecutionResult tme on tme.testExecutionId = te.id "
									+ "where tme.id = %d";
	
	private static String getSuiteConfigFromTestMethodExecId = "select tse.id,ts.name,tse.status from MTAAS.TestSuite ts "
									+ "inner join MTAAS.TestSuiteExecutionResult tse on ts.id = tse.testSuiteId "
									+ "inner join MTAAS.TestExecutionResult te on tse.id = te.testSuiteExecutionId "
									+ "inner join MTAAS.TestMethodExecutionResult tme on te.id = tme.testExecutionId "
									+ "where tme.id = %d";
	
	private static String getTestMethodConfigFromTestMethodExecId = "select name,status from MTAAS.TestMethod tm "
									+ "inner join MTAAS.TestMethodExecutionResult tme on tme.testMethodId = tm.id "
									+ "where tme.id = %d";
	
	private static String getTestConfigFromTestExecId = "select t.name,te.status from MTAAS.Test t " 
									+ "inner join MTAAS.TestExecutionResult te on t.id = te.testId "
									+ "where te.id = %d";

	private static String getSuiteConfigFromTestExecId = "select tse.id,ts.name,tse.status from MTAAS.TestSuite ts "
									+ "inner join MTAAS.TestSuiteExecutionResult tse on ts.id = tse.testSuiteId "
									+ "inner join MTAAS.TestExecutionResult te on tse.id = te.testSuiteExecutionId "
									+ "where te.id = %d";
	
	private static String getSuiteNameFromSuiteId = "select name from MTAAS.TestSuite where id =%d";

	
	public static int updateSuiteName(String suiteName){
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			String getSuiteId = String.format(suiteIdQuery,suiteName);
			log.info("querySuiteId query: " + getSuiteId);
			preparedStatement = conn.prepareStatement(getSuiteId);
			
			rs = preparedStatement.executeQuery();
			
			if (!rs.next()){
//				String updateSuiteName = "INSERT INTO MTAAS.TestSuite(`name`) VALUES(?)";
				
				preparedStatement = conn.prepareStatement(updateSuiteName, Statement.RETURN_GENERATED_KEYS);
				preparedStatement.setString(1, suiteName);
				
				log.info("updateSuiteName query: " + preparedStatement);
				
				int insertSuiteNameAffectedRows = preparedStatement.executeUpdate();
				if (insertSuiteNameAffectedRows == 1){
					rs = preparedStatement.getGeneratedKeys();
				    rs.next();
				    int insertedSuiteNameId = rs.getInt(1);
				    return insertedSuiteNameId;
				}
				//This is used if mutiple threads try to insert at the same time
				else if (insertSuiteNameAffectedRows == 0){
					preparedStatement = conn.prepareStatement(getSuiteId);
					rs = preparedStatement.executeQuery();
					rs.next();
					return rs.getInt(1);
				}
				else{
					log.error("TestInfoDAO.updateTestName(). This is not expected");
				}
				
			}
			else{
				log.info("updateSuiteName: Suite already present in MTAAS.TestSuite table. SuiteName = " + suiteName );
			    return rs.getInt(1);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			Util.closeDBResources(rs, null, preparedStatement, conn);
		}
		return -1;
	}
	
	public static int updateTestName(String className, int suiteId){
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
//			conn.setAutoCommit(false);
			
			String getTestId = String.format(queryTestIdForUpdate,className) ;
			log.info("updateTestName. queryTestId query: " + getTestId);
			preparedStatement = conn.prepareStatement(getTestId);
			
			rs = preparedStatement.executeQuery();
			
			if (!rs.next()){
//				String updateTestName = "INSERT IGNORE INTO MTAAS.Test(`name`, `testSuiteId`) VALUES (?,?)";
				preparedStatement = conn.prepareStatement(updateTestName, Statement.RETURN_GENERATED_KEYS);
				preparedStatement.setString(1,className);
				preparedStatement.setInt(2,suiteId);
				
				log.info("updateTestName: " + preparedStatement);
				
				int insertTestAfectedRows = preparedStatement.executeUpdate();
				if (insertTestAfectedRows == 1){
					rs = preparedStatement.getGeneratedKeys();
				    rs.next();
				    return rs.getInt(1);
				}
				//This is used if mutiple threads try to insert at the same time
				else if (insertTestAfectedRows == 0){
					preparedStatement = conn.prepareStatement(getTestId);
					rs = preparedStatement.executeQuery();
					rs.next();
					return rs.getInt(1);
				}
				else{
					log.error("TestInfoDAO.updateTestName(). This is not expected");
				}
			}
			else{
				log.info("updateTestName: Test already present in MTAAS.Test table. ClassName = " + className );
			    return rs.getInt(1);
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
	
	public static int updateTestMethod(String testName, String methodName){
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			String getMethodName = String.format(queryMethodName,methodName);
			log.info("updateTestMethod query1: " + getMethodName);
			preparedStatement = conn.prepareStatement(getMethodName);
			
			rs = preparedStatement.executeQuery();
			
			if (!rs.next()){
				
				String getTestId = String.format(queryTestId,testName);
				log.info("updateTestMethod query2: " + getTestId);
				preparedStatement = conn.prepareStatement(getTestId);
				
				rs = preparedStatement.executeQuery();
				
				if(rs.next()){
					int testId = rs.getInt(1);
					preparedStatement = conn.prepareStatement(updateTestMethod, Statement.RETURN_GENERATED_KEYS);
					preparedStatement.setString(1, methodName);
					preparedStatement.setInt(2, testId);
					
					log.info("updateTestMethod query3: " + preparedStatement);
					
					int updateTestMethodAffectedRows = preparedStatement.executeUpdate();
					if (updateTestMethodAffectedRows == 1){
						rs = preparedStatement.getGeneratedKeys();
					    rs.next();
					    return rs.getInt(1);
					}
					//This is used if mutiple threads try to insert at the same time
					else if (updateTestMethodAffectedRows == 0){
						preparedStatement = conn.prepareStatement(getMethodName);
						rs = preparedStatement.executeQuery();
						rs.next();
						return rs.getInt(1);
					}
					else{
						log.error("TestInfoDAO.updateTestMethod(). This is not expected");
					}
				}
				else{
					log.info(String.format("updateTestMethod: Test = %s not found for the TestMethod = %s",testName,methodName));
					log.error(String.format("updateTestMethod() TEST NAME ID IS NOT CREATED FOR TEST METHOD %s and TestName %s",methodName,testName));
				}
			}
			else
				log.info(String.format("updateTestMethod(). TestMethod already created %s",methodName));

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(rs, null, preparedStatement, conn);
		}
		return -1;
		
	}

	public static TestInfo getTestConfigFromTestMethodExecId(int testMethodExecId) {
		TestInfo testConfig = null;
		
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			String getTestConfigFromTestMethodExecIdQuery = String.format(
					getTestConfigFromTestMethodExecId, testMethodExecId);
			
			String getSuiteConfigFromTestMethodExecIdQuery = String.format(
					getSuiteConfigFromTestMethodExecId, testMethodExecId);
			
			String getTestMethodNameFromTestMethodExecIdQuery = String.format(
					getTestMethodConfigFromTestMethodExecId, testMethodExecId);

			log.info("getExecIdAndNameOfTestFromTestMethodExecIdQ query: " + getTestConfigFromTestMethodExecIdQuery);
			
			log.info("getExecIdAndNameOfSuiteFromTestMethodExecIdQ query: " + getSuiteConfigFromTestMethodExecIdQuery);
			
			log.info("getTestMethodNameFromTestMethodExecIdQ query: " + getTestMethodNameFromTestMethodExecIdQuery);
			
			st = conn.createStatement();

			testConfig = new TestInfo();
			
			res = st.executeQuery(getTestConfigFromTestMethodExecIdQuery);
			if (res.next()){
				testConfig.setTestExecutionId(res.getInt(1));
				testConfig.setTestName(res.getString(2));
				testConfig.setTestStatus(res.getString(3));
			}
			
			res = st.executeQuery(getSuiteConfigFromTestMethodExecIdQuery);
			if (res.next()){
				testConfig.setSuiteExecutionId(res.getInt(1));
				testConfig.setSuiteName(res.getString(2));
				testConfig.setSuiteStatus(res.getString(3));
			}
			
			res = st.executeQuery(getTestMethodNameFromTestMethodExecIdQuery);
			if (res.next()){
				testConfig.setTestMethodExecutionId(testMethodExecId);
				testConfig.setTestMethodName(res.getString(1));
				testConfig.setTestMethodStatus(res.getString(2));
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
		return testConfig;

	}
	
	public static TestInfo getTestConfigFromTestExecutionId(int testExecutionId){
		TestInfo testConfig = null;
		
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			String getTestConfigFromTestExecIdQuery = String.format(
					getTestConfigFromTestExecId, testExecutionId);
			
			String getSuiteConfigFromTestExecIdQuery = String.format(
					getSuiteConfigFromTestExecId, testExecutionId);
			
			log.info("getExecIdAndNameOfTestFromTestExecIdQ query: " + getTestConfigFromTestExecIdQuery);
			
			log.info("getExecIdAndNameOfSuiteFromTestExecIdQ query: " + getSuiteConfigFromTestExecIdQuery);
			
			st = conn.createStatement();

			testConfig = new TestInfo();
			
			res = st.executeQuery(getTestConfigFromTestExecIdQuery);
			if (res.next()){
				testConfig.setTestExecutionId(testExecutionId);
				testConfig.setTestName(res.getString(1));
				testConfig.setTestStatus(res.getString(2));
			}
			
			res = st.executeQuery(getSuiteConfigFromTestExecIdQuery);
			if (res.next()){
				testConfig.setSuiteExecutionId(res.getInt(1));
				testConfig.setSuiteName(res.getString(2));
				testConfig.setSuiteStatus(res.getString(3));
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
		return testConfig;
	}

	public static String getSuiteNameFromSuiteId(int suiteId) {
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			String getSuiteNameFromSuiteIdQuery = String.format(getSuiteNameFromSuiteId,suiteId);
			log.info("getSuiteNameFromSuiteIdQuery query: " + getSuiteNameFromSuiteIdQuery);
			
			st = conn.createStatement();
			
			res = st.executeQuery(getSuiteNameFromSuiteIdQuery);
			
			if (res.next()){
				return res.getString(1);
			}
			else{
				log.info("suite id to get suite name not present in test sutie table. Suite Id: " + suiteId);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			Util.closeDBResources(res,st, null, conn);
		}
		return null;
	}


}
