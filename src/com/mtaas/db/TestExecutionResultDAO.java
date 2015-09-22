package com.mtaas.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mtaas.bean.DeviceConfig;
import com.mtaas.bean.TestInfo;
import com.mtaas.bean.TestExecutionDetailResponse;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.Util;
import com.mtaas.util.MtaasConstants.TestResult;

public class TestExecutionResultDAO {
	private static Logger log = Logger.getLogger(TestExecutionResultDAO.class.getName());
	
	private static String updateTestResultQuery = "UPDATE MTAAS.%s SET status='%s' , executionEndTime = '%s' WHERE id = %d";
	
	private static String getTestExecutionResult = "select status,id from MTAAS.TestExecutionResult WHERE testSuiteExecutionId = %d";
	
	private static String getDeviceIdFromTestMethodExecIdQuery = "select appiumIp from MTAAS.Device d inner join MTAAS.TestMethodExecutionResult tm" 
								+ " on tm.deviceId = d.deviceId where tm.id =";
	
	private static String getFailedTestClassesNameFromRequestId = "select distinct t.name from MTAAS.Test t " +
															"inner join MTAAS.TestExecutionResult te on te.testId = t.id " +
															"inner join MTAAS.TestSuiteExecutionResult tse on tse.id = te.testSuiteExecutionId " +
															"inner join MTAAS.Request r on tse.requestId = r.id " +
															"where tse.status != 'pass' and te.status != 'pass' and r.id = %d";
	
	private static String getAllTestClassesNameFromRequestId = "select distinct t.name from MTAAS.Test t " +
			"inner join MTAAS.TestExecutionResult te on te.testId = t.id " +
			"inner join MTAAS.TestSuiteExecutionResult tse on tse.id = te.testSuiteExecutionId " +
			"inner join MTAAS.Request r on tse.requestId = r.id " +
			"where tse.status != 'pass' and r.id = %d";

	public static int updateExecutionResult(String tableName,
			int id, Timestamp testExecutionSqlEndTime, TestResult testResult) {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			String updateTestResult = String.format(updateTestResultQuery,
					tableName,testResult.name(),testExecutionSqlEndTime, id);
			preparedStatement = conn.prepareStatement(updateTestResult);

			log.info("updateExecutionResult query: " + preparedStatement);
			
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

		// TODO Auto-generated method stub
		
	}

	public static TestResult getSuiteResult(int suiteExecutionId) {
		// TODO Auto-generated method stub
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			
			boolean isGetTestExecStatusFailed = true;
			Set<String> statusSet = new HashSet<String>();
			/**while loop to iterate through the TestExecutionResult table even if it throws null pointer exception. And also
			 *to iterate until TestExecuiontResult table status is updated to the final result to avoid reading stautus
			 *"INPROGRESS" which is set during the test start 
			 */
//			while(isGetTestExecStatusFailed){
				/*try{
					.info("INSIDE while loop of getSuiteResult() method to get the stats of suiteId");*/
					
					String getTestExecutionResultQuery = String.format(getTestExecutionResult,
							suiteExecutionId);
					preparedStatement = conn.prepareStatement(getTestExecutionResultQuery);
		
					log.info("getSuiteResult query: " + preparedStatement);
					
					rs = preparedStatement.executeQuery();
					
					while(rs.next()){
						String status = rs.getString(1);
						/**
						 * Checking for inprogress. Sometimes TestSuiteExecutionResultUpdater is executed before 
						 * TestExecitionUpdater. In this case the old status of TestExecutionResult table (set during test start as "INPROGRESS") 
						 * is read which is not desired. If this is not address then this method return NULL from the logic follows after 
						 * this while loop and it give null-pointer exception in updateExecutionResult() when called from TestSuiteExecutionResultUpdater.
						 *  
						 */
						/*if (status.equalsIgnoreCase("inprogress")){
							continue;
						}*/
						log.info(String.format("In getSuiteResult(). TestexecutionResult Id:%d & Status:%s", rs.getInt(2),rs.getString(1)));
						statusSet.add(status);
					}
					/*}
					//isGetTestExecStatusFailed = false;
				}
				catch (NullPointerException e){
					log.error("Caught in null pointer exception of getSuiteResult. Not throwing exception." + e.getMessage());
					//isGetTestExecStatusFailed = true;
				}*/
//			}
			
			if (statusSet.contains(MtaasConstants.TestResult.FAIL.name())){
				log.info(String.format("Suite Execution id: %s. Result = %s", suiteExecutionId,"FAIL"));
				return MtaasConstants.TestResult.FAIL;
			}
			else if ((statusSet.contains(MtaasConstants.TestResult.SKIP.name()))){
				log.info(String.format("Suite Execution id: %s. Result = %s", suiteExecutionId,"SKIP"));
				return MtaasConstants.TestResult.SKIP;
			}
			else if ((statusSet.contains(MtaasConstants.TestResult.PASS.name()))){
				log.info(String.format("Suite Execution id: %s. Result = %s", suiteExecutionId,"PASS"));
				return MtaasConstants.TestResult.PASS;
			}
			else if ((statusSet.contains(MtaasConstants.TestResult.INPROGRESS.name()))){
				log.info(String.format("Suite Execution id: %s. Result = %s", suiteExecutionId,"INPROGRESS"));
				return MtaasConstants.TestResult.INPROGRESS;
			}
			else{
				log.info(String.format("Suite Execution id: %s. Result = %s", suiteExecutionId,"Not defined"));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(rs, null, preparedStatement, conn);
		}
		return MtaasConstants.TestResult.getValue(MtaasConstants.NO_TEST_RESULT);
	}
	
	public static String getAppiumIpFromTestMethodExecutionResult(int testMethodExecId){
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
					
			preparedStatement = conn.prepareStatement(getDeviceIdFromTestMethodExecIdQuery + testMethodExecId);
		
			log.info("getAppiumIpFromTestMethodExecutionResult query: " + preparedStatement);
					
			rs = preparedStatement.executeQuery();
					
			if(rs.next()){
				String appiumIp = rs.getString(1);
				return appiumIp;
			}
			else{
				log.info("getAppiumIpFromTestMethodExecutionResult(). Data inconsistency or serious problem in db. Please check db");
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			Util.closeDBResources(rs, null, preparedStatement, conn);
		}
		return null;
	}

	public static TestExecutionDetailResponse getTestExecutionIdDetail(
			int testExecId) {
		TestExecutionDetailResponse testExecutionDetailResponse = new TestExecutionDetailResponse();
		
		DeviceConfig deviceConfig = DeviceInfoDAO.getDeviceConfigFromTestExecId(testExecId);
		TestInfo testConfig = TestInfoDAO.getTestConfigFromTestExecutionId(testExecId);
		List<TestInfo> testMethodsConfig = TestMethodExecutionDAO.getTestMethodsConfigFromTestExecutionId(testExecId);
		
		testExecutionDetailResponse.setDeviceConfig(deviceConfig);
		testExecutionDetailResponse.setTestInfo(testConfig);
		testExecutionDetailResponse.setTestMethodInfo(testMethodsConfig);
		
		return testExecutionDetailResponse;
	}

	public static List<String> getFailedTestClassesNameFromRequestId(int requestId) {
		List<String> classesName = null;
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			String getFailedTestClassesNameFromRequestIdQuery = String.format(
					getFailedTestClassesNameFromRequestId, requestId);
			
			log.info("getFailedTestClassesNameFromRequestIdQuery query: " + getFailedTestClassesNameFromRequestIdQuery);
			
			st = conn.createStatement();

			classesName = new ArrayList<String>();
			
			res = st.executeQuery(getFailedTestClassesNameFromRequestIdQuery);
			while (res.next()){
				classesName.add(res.getString(1));
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
		return classesName;
	}

	public static List<String> getAllTestClassesNameFromRequestId(int requestId) {
		List<String> classesName = null;
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			String getAllTestClassesNameFromRequestIdQuery = String.format(
					getAllTestClassesNameFromRequestId, requestId);
			
			log.info("getAllTestClassesNameFromRequestIdQuery query: " + getAllTestClassesNameFromRequestIdQuery);
			
			st = conn.createStatement();

			classesName = new ArrayList<String>();
			
			res = st.executeQuery(getAllTestClassesNameFromRequestIdQuery);
			while (res.next()){
				classesName.add(res.getString(1));
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
		return classesName;

	}
}
