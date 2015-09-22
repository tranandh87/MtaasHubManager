package com.mtaas.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

import org.apache.log4j.Logger;

import com.mtaas.util.Util;

public class RequestDAO {
	public static final String insertStmt = "Insert into MTAAS.Request(requestTime,AUTDetailId,OriginalRequestId) values(?,?,?)";
	private static Logger log = Logger.getLogger(RequestDAO.class.getName());
	public static int insertRequest(Timestamp time, int autDetailsId,int OriginalRequestId){
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int requestId = -1;
		try {
			con = DatabaseConnection.getInstance().getDbConnectionFromPool();
			stmt = con.prepareStatement(insertStmt, Statement.RETURN_GENERATED_KEYS);
			stmt.setTimestamp(1, time);
			stmt.setInt(2, autDetailsId);
			
			if (OriginalRequestId > 0){
				log.info("insertRequest. This is the request to run failed Test case.");
				stmt.setInt(3, OriginalRequestId);
			}
			else{
				stmt.setNull(3, Types.NULL);
			}
			
			if (stmt.executeUpdate() == 1){
				rs = stmt.getGeneratedKeys();
				while (rs.next()){
					requestId = rs.getInt(1);
					log.info("auto-generated requestId=" + requestId);
				}
			}
		} catch (Exception e){
			log.error("exception caught in insertRequest", e);
		} finally {
			Util.closeDBResources(rs, null, stmt, con);
		}
		
		return requestId;
	}
}
