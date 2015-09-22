package com.mtaas.util;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.mtaas.db.DatabaseConnection;

public class ClearAllTableUtil {
	private static final String dbName = "MTAAS.";
	
	/**
	 * Tables are given in the order so that the child table with FK constraint are listed before the parent tables.
	 */
	private static final String[] allTables = {"Tracking", "TestMethodExecutionResult", "TestExecutionResult", "TestSuiteExecutionResult", "Request", "TestMethod", "Test", "TestSuite", "Device", "Hub","AUTDetails"};
	
	public static List<String> createDeleteQueries(){
		List<String> queries = new ArrayList<String>();
		for (int i = 0; i < allTables.length; ++i){
			StringBuilder sb = new StringBuilder();
			sb.append("Delete from " + dbName + allTables[i]);
			queries.add(sb.toString());
		}
		
		return queries;
	}
	
	public static void clearAllTables(){
		Connection con = null;
		Statement stmt = null;
		try {
			con = DatabaseConnection.getInstance().getDbConnection();
			stmt = con.createStatement();
			List<String> queries = createDeleteQueries();
			for (int i = 0; i < queries.size(); ++i){
				stmt.addBatch(queries.get(i));
			}
			
			stmt.executeBatch();
			System.out.println("Deleted all tables: " + allTables);
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			Util.closeDBResources(null, stmt, null, con);
		}
	}
	
	public static void main(String[] a){
		clearAllTables();
	}
}
