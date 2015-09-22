package com.mtaas.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mtaas.bean.DeviceConfig;
import com.mtaas.bean.ExecutionStatus;
import com.mtaas.bean.ExecutionSummary;
import com.mtaas.bean.TestClassExecutionStatus;
import com.mtaas.bean.SuiteExecutionIdSummary;
import com.mtaas.bean.SuiteIdExecutionSummaryResponse;
import com.mtaas.bean.TestInfo;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.Util;
import com.mtaas.util.MtaasConstants.TestResult;

public class TestSuiteExecutionResultDAO {
	private static Logger log = Logger.getLogger(TestSuiteExecutionResultDAO.class.getName());
	private static final String deviceConfigSelectStmt = "select deviceId, platform, osVersion, manufacturer, isEmulator, model from MTAAS.Device ";
	private static final String deviceConfigForRequestIdQuery = deviceConfigSelectStmt + "where deviceId IN (select distinct deviceId from MTAAS.TestSuiteExecutionResult result where result.requestId = %d);";
	private static final String deviceConfigForSuiteIdQuery = deviceConfigSelectStmt + "where deviceId IN (select distinct deviceId from MTAAS.TestSuiteExecutionResult result where result.testSuiteId = %d);";
	
	private static final String suiteExecutionSelectStmt = "select id, deviceId, status, executionStartTime, executionEndTime from MTAAS.TestSuiteExecutionResult testSuiteResult";
	private static final String bySuiteIdCondition = " where testSuiteResult.testSuiteId = %d ";
	private static final String byRequestIdCondition = " where testSuiteResult.requestId = %d ";
	private static final String orderByStmt = "order by executionStartTime desc;";
	
	//this query returns the test suite execution id, deviceId, status for the specified testSuiteId
	private static String suiteExecutionBySuiteId = suiteExecutionSelectStmt + bySuiteIdCondition + orderByStmt;
	private static String suiteExecutionByRequestId = suiteExecutionSelectStmt + byRequestIdCondition + orderByStmt;
	
	private static String suiteNameBySuiteId = "select name from MTAAS.TestSuite where id=%d";
	private static String suiteNameByRequestId = "select name from MTAAS.TestSuite ts join MTAAS.TestSuiteExecutionResult tser on ts.id = tser.testSuiteId where tser.requestId=%d;";
	
	//returns the testExecution id and status for the specified testSuiteExecutionId
	private static String testExecutionInfoForSuiteExecutionIdQuery = "SELECT testResult.id, testResult.status, test.name "
			+ "FROM MTAAS.TestExecutionResult testResult join MTAAS.Test test on testResult.testId = test.id "
			+ "where testSuiteExecutionId = %d;";
	
	private static String getFailedSuiteDevicesAndSuiteIdFromRequestId = "select deviceId,testSuiteId from MTAAS.TestSuiteExecutionResult tse " +
															"inner join MTAAS.Request r on tse.requestId = r.id " +
															"where tse.status != 'PASS' and r.id =%d";
	

	/**
	 * build the TestSuiteExecutionResultSummaryResponse
	 * 1. populate the deviceConfigs
	 * 2. populate the result summary, which cross-reference the deviceConfig
	 * @param suiteId
	 * @return
	 */
	public static SuiteIdExecutionSummaryResponse getSuiteIdExecutionSummary(int suiteId){
		SuiteIdExecutionSummaryResponse resp = new SuiteIdExecutionSummaryResponse();
		
		String query = String.format(deviceConfigForSuiteIdQuery, suiteId);
		populateDeviceConfigs(resp, query);
		
		String executionSummaryQuery = String.format(suiteExecutionBySuiteId, suiteId);
		populateSummaryResult(resp, executionSummaryQuery);
		
		String suiteNameQuery = String.format(suiteNameBySuiteId, suiteId);
		setSuiteName(resp, suiteNameQuery);
		
		return resp;
	}

	/**
	 * build a summary list of:
	 * 1. deviceConfigId
	 * 2. test suite execution status
	 * 3. test class execution stat
	 * 4. test class execution info
	 * @param resp
	 * @param query
	 */
	private static void populateSummaryResult(
			SuiteIdExecutionSummaryResponse resp, String query) {
		
		List<SuiteExecutionIdSummary> summaryList = new ArrayList<SuiteExecutionIdSummary>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			stmt = conn.createStatement();
			log.info("executionQuery: " + query);
			rs = stmt.executeQuery(query);
			
			while (rs.next()){
				SuiteExecutionIdSummary suiteExecution = new SuiteExecutionIdSummary();
				int suiteExecutionId = rs.getInt(1);
				suiteExecution.setTestSuiteExecutionId(suiteExecutionId);
				suiteExecution.setDeviceConfigId(rs.getInt(2));
				suiteExecution.setSuiteExecutionStatus(rs.getString(3));
				
				suiteExecution.setExecutionStartTime(rs.getString(4));
				suiteExecution.setExecutionEndTime(rs.getString(5));
				
				populateSuiteExecutionStat(suiteExecutionId, suiteExecution);
				summaryList.add(suiteExecution);
			}
			
			resp.setSuiteIdExecutionSummary(summaryList);
		} catch (Exception e){
			log.error("Exception in populateSummaryResult", e);
		} finally {
			Util.closeDBResources(rs, stmt, null, conn);
		}
	}

	private static void populateSuiteExecutionStat(int suiteExecutionId,
			SuiteExecutionIdSummary suiteExecution) {
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			stmt = conn.createStatement();
			String query = String.format(testExecutionInfoForSuiteExecutionIdQuery, suiteExecutionId);
			log.info("query: " + query);
			rs = stmt.executeQuery(query);
			
			List<ExecutionStatus> testExecutionStatusList = new ArrayList<ExecutionStatus>();
			int total = 0, failed = 0, passed= 0, skipped = 0;
			
			while (rs.next()){
				++total;
				
				TestClassExecutionStatus exStatus = new TestClassExecutionStatus();
				exStatus.setTestClassExecutionId(rs.getInt(1));
				String status = rs.getString(2);
				MtaasConstants.TestResult testResult = TestResult.valueOf(status);
				
				exStatus.setStatus(status);
				exStatus.setTestClassName(rs.getString(3));
				
				switch (testResult){
				case PASS:
					++passed;
					break;
					
				case FAIL:
					++failed;
					break;
					
				case SKIP:
					++skipped;
					break;
				}
				
				testExecutionStatusList.add(exStatus);
			}
			
			ExecutionSummary exSummary = new ExecutionSummary();
			exSummary.setTotal(total);
			exSummary.setFailed(failed);
			exSummary.setPassed(passed);
			exSummary.setSkipped(skipped);
			
			suiteExecution.setTestClassSummary(exSummary);
			suiteExecution.setTestClassExecutionStatus(testExecutionStatusList);
		} catch (Exception e){
			log.error("Exception caught in populateSuiteExecutionStat", e);
		} finally {
			Util.closeDBResources(rs, stmt, null, conn);
		}
	}
	
	private static void setSuiteName(SuiteIdExecutionSummaryResponse resp, String query){
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			
			while (rs.next()){
				resp.setSuiteName(rs.getString(1));
			}
			
			
		} catch (Exception e){
			log.error("Exception caught in populateDeviceConfigs", e);
		} finally {
			Util.closeDBResources(rs, stmt, null, conn);
		}
	}

	private static void populateDeviceConfigs(
			SuiteIdExecutionSummaryResponse resp, String query) {

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<DeviceConfig> configs = new ArrayList<DeviceConfig>();
		
		try {
			conn = DatabaseConnection.getInstance().getDbConnectionFromPool();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			
			while (rs.next()){
				DeviceConfig devConfig = new DeviceConfig();
				devConfig.setDeviceConfigId(rs.getInt(1));
				devConfig.setPlatform(rs.getString(2));
				devConfig.setVersion(rs.getString(3));
				devConfig.setManufacturer(rs.getString(4));
				
				boolean isEmulator = Util.getBooleanEquivalentOfInt(rs.getInt(5));
				devConfig.setIsEmulator(isEmulator);
				
				devConfig.setModel(rs.getString(6));
				
				configs.add(devConfig);
			}
			
			
		} catch (Exception e){
			log.error("Exception caught in populateDeviceConfigs", e);
		} finally {
			Util.closeDBResources(rs, stmt, null, conn);
		}
		
		resp.setDeviceConfigs(configs);
	}

	/**
	 * get the execution summary for this requestId by retrieving all the suite executions associated with this reqId
	 * @param executionRequestId
	 * @return
	 */
	public static SuiteIdExecutionSummaryResponse getExecutionSummary(
			int executionRequestId) {
		
		SuiteIdExecutionSummaryResponse resp = new SuiteIdExecutionSummaryResponse();
		String deviceConfigQuery = String.format(deviceConfigForRequestIdQuery, executionRequestId);
		populateDeviceConfigs(resp, deviceConfigQuery);
		
		String executionResultQuery = String.format(suiteExecutionByRequestId, executionRequestId);
		populateSummaryResult(resp, executionResultQuery);
		
		String suiteNameQuery = String.format(suiteNameByRequestId, executionRequestId);
		setSuiteName(resp, suiteNameQuery);
		
		return resp;
	}
	
	public static Map<Integer,Integer> getDevicesAndSuiteIdFromRequestId(int requestId){
		
		Map<Integer,Integer> devicesIdAndSuiteIdMap = null; 
		
		Connection conn = null;
		Statement st = null;
		ResultSet res = null;
		
		try {
			conn = new DatabaseConnection().getDbConnectionFromPool();
			String getDeviceAndSuiteIdFromRequestIdQuery = String.format(
					getFailedSuiteDevicesAndSuiteIdFromRequestId, requestId);
			
			log.info("getDeviceAndSuiteIdFromRequestIdQuery query: " + getDeviceAndSuiteIdFromRequestIdQuery);
			
			st = conn.createStatement();
			
			res = st.executeQuery(getDeviceAndSuiteIdFromRequestIdQuery);
			
			devicesIdAndSuiteIdMap = new HashMap<Integer,Integer>();
			while (res.next()){
				devicesIdAndSuiteIdMap.put(res.getInt(1),res.getInt(2));
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
		return devicesIdAndSuiteIdMap;
	}

}
