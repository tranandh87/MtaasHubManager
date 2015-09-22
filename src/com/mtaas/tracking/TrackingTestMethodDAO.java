package com.mtaas.tracking;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.mtaas.bean.DeviceConfig;
import com.mtaas.bean.TestInfo;
import com.mtaas.bean.TestInfo;
import com.mtaas.db.DatabaseConnection;
import com.mtaas.db.DeviceInfoDAO;
import com.mtaas.db.TestInfoDAO;
import com.mtaas.tracking.bean.Operation;
import com.mtaas.tracking.bean.OperationalTracking;
import com.mtaas.tracking.bean.TestMethodTrackingResponse;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.Util;

public class TrackingTestMethodDAO {
	private static Logger log = Logger.getLogger(TrackingTestMethodDAO.class.getName());

	private static String updateTestMethodTracking = "INSERT INTO MTAAS.Tracking(`step`, `stepDescription`,`result`,`testMethodExecutionId`) "
			+ "VALUES (?,?,?,?)";
	
	private static String getTestMethodTracking = "select step, stepDescription,result from MTAAS.Tracking where testMethodExecutionId = ";
	
	public static List<Integer> updateTestMethodTracking(int testMethodExecutionId,
														OperationalTracking tracking) {
		
		log.info("In updateTestMethodTracking");
		Connection conn = null;
		List<Integer> operationTrackingId = new ArrayList<Integer>();
		
		int counter = 0;
		for (Operation operation: tracking.getOperations()){
			conn = new DatabaseConnection().getDbConnectionFromPool();
			counter++;
			String stepName = MtaasConstants.STEP_NAME + counter; 
			log.info("TrackingTestMethodDAO. Step " + counter);
			
			PreparedStatement preparedStatement = null;
			ResultSet rs = null;
			
			try {
				preparedStatement = conn.prepareStatement(updateTestMethodTracking, Statement.RETURN_GENERATED_KEYS);
				
				log.info("Opearation Step : " + operation);
//				log.info("operation resutl : " + operation.getStatus());
				
				preparedStatement.setString(1,stepName );
				//TODO: Parse the Operation.toString and get the valuable information out of it.
				preparedStatement.setString(2, operation.toString());
//				preparedStatement.setString(2, step);
				
				preparedStatement.setString(3, operation.getStatus());
//				preparedStatement.setString(3, Util.getStatusFromTestMethodTrackingStep(step));
				
				preparedStatement.setInt(4, testMethodExecutionId);
				
				log.info("updateTestMethodTracking query: " + preparedStatement);
				if (preparedStatement.executeUpdate() == 1){
					rs = preparedStatement.getGeneratedKeys();
				    rs.next();
				    operationTrackingId.add(rs.getInt(1));
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				Util.closeDBResources(rs, null, preparedStatement, conn);
			}
		}
		return operationTrackingId;
	}

	public static TestMethodTrackingResponse getTestMethodTracking(int testMethodExecutionId) {

		log.info("In getTestMethodTracking");
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try{
			conn = new DatabaseConnection().getDbConnectionFromPool();
			stmt = conn.createStatement();
			
			String getTestMethodTrackingQuery = getTestMethodTracking + testMethodExecutionId;
			
			log.info("TestMethodTracking query : " + getTestMethodTrackingQuery);
			
			rs = stmt.executeQuery(getTestMethodTrackingQuery);
			
			return buildTestMethodTrackingResponse(rs,testMethodExecutionId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Util.closeDBResources(rs, stmt, null, conn);
		}
		return null;
	}


//	private static TestMethodTrackingResponse buildTestMethodTrackingResponse(ResultSet resultSet) throws SQLException {
	private static TestMethodTrackingResponse buildTestMethodTrackingResponse(ResultSet resultSet, int testMethodExecId) throws SQLException {
		// TODO Auto-generated method stub
		TestMethodTrackingResponse testMethodTrackingResponse = new TestMethodTrackingResponse();
		
		List<Operation> operations = new ArrayList<Operation>();
		while (resultSet.next()){
			log.info("step in buildTestMethodTrackingResponse");
//			String stepName = resultSet.getString(MtaasConstants.TRACKING_TEST_METHOD_STEP_NAME);
			String description = resultSet.getString(MtaasConstants.TRACKING_TEST_METHOD_DESCRIPTION);
//			String result = resultSet.getString(MtaasConstants.TRACKING_TESTMETHOD_RESULT);
			
//			methodSteps.add(buildTestMethodTrackingStep(stepName,description,result));
			operations.add(buildOperation(description));
		}
		
		DeviceConfig deviceConfig = DeviceInfoDAO.getDeviceConfigFromTestMethodExecId(testMethodExecId);
		TestInfo testConfig = TestInfoDAO.getTestConfigFromTestMethodExecId(testMethodExecId);
		
		testMethodTrackingResponse.setDeviceConfig(deviceConfig);
		
		testMethodTrackingResponse.setTestConfig(testConfig);
		
		testMethodTrackingResponse.setOperations(operations);
		return testMethodTrackingResponse;
	}

	private static Operation buildOperation(String description) {
		log.info("building Operation");
		Operation operation = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			operation = mapper.readValue(description, Operation.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Util.splitString
		return operation;
	}

}
