package org.ws.mts.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.cuba.log.Log;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DatabaseContext {
	private static final String TAG = DatabaseContext.class.getSimpleName();
	
	private ComboPooledDataSource dataSource;
	private Log logger;
	    
    public DatabaseContext(Log logger, String host, int port, String database, String user, String password) {
        this.logger = logger;
        
        logger.d(TAG, "Initialization started");
        logger.d(TAG, "Configuring PooledDataSource");
        System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
        
        dataSource = new ComboPooledDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database + "?ssl=false");
        dataSource.setUser(user);
        dataSource.setPassword(password);
        
        try {
        	String driverClass = Driver.class.getName();
            logger.d(TAG, "Applying driver class: " + driverClass);
            dataSource.setDriverClass(driverClass);
        } catch (Throwable t) {
            logger.e(TAG, "Failed to apply driver class", t);
            throw new RuntimeException(t);
        }
        
        dataSource.setMaxIdleTime(1 * 60 * 1000);
        dataSource.setMaxConnectionAge(60 * 60 * 1000);
    }
    
    public ComboPooledDataSource getDataSource() {
    	return dataSource;
    }
    
    public Connection getConnection() throws SQLException {
    	return dataSource.getConnection();
    }
    
    public <T> T fetchOne(CharSequence query, Mapper<T> mapper) throws SQLException {
    	logger.d(TAG, "Fetching one result by " + query);
    	try(Connection connection = getConnection();
			Statement statement = connection.createStatement()) {
    		ResultSet resultSet = statement.executeQuery(query.toString());
    		
    		if(resultSet.next()) {
            	logger.d(TAG, query + ": item found");
    			return mapper.map(resultSet);
    		}
    		
        	logger.d(TAG, query + ": item not found");    		
    		return null;
    	}
    }
    
    public <T> List<T> fetchAll(CharSequence query, Mapper<T> mapper) throws SQLException {
    	logger.d(TAG, "Fetching few results by " + query);
    	try(Connection connection = getConnection();
			Statement statement = connection.createStatement()) {
    		ArrayList<T> list = new ArrayList<>();
    		ResultSet resultSet = statement.executeQuery(query.toString());
    		
    		while(resultSet.next()) {
    			list.add(mapper.map(resultSet));
    		}
    		
        	logger.d(TAG, "Found " + list.size() + " items by " + query);
    		
    		return list;
    	}
    }
    
    public boolean execute(CharSequence query) throws SQLException {
    	logger.d(TAG, "Executing " + query);
    	try(Connection connection = getConnection();
			Statement statement = connection.createStatement()) {
    		boolean result = statement.execute(query.toString());
        	return result;
    	}
    }
    
}
