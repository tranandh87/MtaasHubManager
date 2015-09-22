package com.mtaas.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class DatabaseConnection {
	protected static AtomicReference<DatabaseConnection> instance = new AtomicReference<DatabaseConnection>();
	private static Logger log = Logger.getLogger(DatabaseConnection.class.getName());
	
	String url = "jdbc:mysql://localhost:3306/";
    String dbName = "MTAAS";
    String driver = "com.mysql.jdbc.Driver";
    
    private static final String userName = "root";
    private static final String pw = "root";
    
    private DataSource dataSource;
    
    public static DatabaseConnection getInstance() {
		instance.compareAndSet(null, new DatabaseConnection());
		return instance.get();
	}
    
	public Connection getDbConnection() {
		Connection conn = null;
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url + dbName, userName, pw);
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    
    public Connection getDbConnectionFromPool(){
    	//return getDbConnection(userName, pw);
    	Connection conn = null;
    	try
        {
              // Get DataSource
              Context initContext  = new InitialContext();
              Context envContext  = (Context)initContext.lookup("java:/comp/env");
              dataSource = (DataSource)envContext.lookup("jdbc/mtaas-hubmanager-db");
              conn = dataSource.getConnection();
        } 
        catch (NamingException e)
        {
              e.printStackTrace();
        }
        catch (SQLException ex)
        {
             log.error("SQLException in getDbConnection() method" + ex.getStackTrace());
             log.info("Could not connect in ConnectionDAO");
        } 
        return conn; 
    }

}
