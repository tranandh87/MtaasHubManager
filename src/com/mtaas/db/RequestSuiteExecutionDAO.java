package com.mtaas.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import org.apache.log4j.Logger;

public class RequestSuiteExecutionDAO {
	private static Logger log = Logger.getLogger(RequestSuiteExecutionDAO.class.getName());
	
	//only need to specify the suiteExecutionId since we'll use the autoIncrement for the requestId
	private static final String firstMappingInsert = "insert into MTAAS.Request_SuiteExecutionId(suiteExecutionId) values(?);";
	
	private static final String mappingInsert = "insert into MTAAS.Request_SuiteExecutionId('id', 'suiteExecutionId') values(?, ?);";
	
	public static int updateRequestExecutionIdMapping(Set<Integer> suiteExecutionIds) {
		
		int requestId = -1, i = 0;
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			con = DatabaseConnection.getInstance().getDbConnectionFromPool();
			for (int suiteExecutionId : suiteExecutionIds){
				if (i == 0){
					//first time, get the requestId to be used for subsequent mapping
					stmt = con.prepareStatement(firstMappingInsert, Statement.RETURN_GENERATED_KEYS);
					stmt.setInt(1, suiteExecutionId);
					log.info(stmt.toString());
					if (stmt.executeUpdate() == 1){
						rs = stmt.getGeneratedKeys();
						while (rs.next()){
							requestId = rs.getInt(1);
							log.info("auto-generated requestId=" + requestId);
						}
					}
				}
				else {
					stmt = con.prepareStatement(mappingInsert);
					stmt.setInt(1, requestId);
					stmt.setInt(2, suiteExecutionId);
					stmt.executeUpdate();
				}
				
				log.info(String.format("requestId=%d, suiteExecutionId=%d", requestId, suiteExecutionId));
			}
		} catch (Exception e){
			log.error("Exception caught in updateRequestExecutionIdMapping", e);
		}
		
		return requestId;
	}

}
