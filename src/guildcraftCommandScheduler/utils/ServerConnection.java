package guildcraftCommandScheduler.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Helper class with static methods to open and close connections
 * @author Jonodonozym
 */
public class ServerConnection {

	private static final long retryInterval = 5L;
	private static final int maxFails = 3;
	private static int numFails = 0;
	private static int port = 3306;
	
	/**
	 * Opens a new connection to a specified database
	 * @param logger
	 * @param host
	 * @param databaseName
	 * @param username
	 * @param password
	 * @return the opened connection, or null if one couldn't be created
	 */
	public static Connection open(Logger logger, String host, String databaseName, String username, String password){
		// connecting to database server, with at most 3 failed attempts every 10 seconds before it gives up
		while (numFails <= maxFails){
			try {
				Connection c = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+databaseName,username,password);
				logger.info("Successfully connected to the "+databaseName+" database at "+host);
				numFails = 0;
				return c;
			}
			catch (SQLException e) {
				numFails++;
				logger.info("Failed to connect to the database: "+e);
				if (numFails <= maxFails){
					logger.info("Attempting to reconnect in "+retryInterval
							+ " seconds... (attempt "+numFails+" of "+maxFails+")");
					try { Thread.sleep(retryInterval*1000L); }
					catch (InterruptedException e1) { e1.printStackTrace(); }
				}
				else
					logger.info("Failed to connect "+maxFails+" times. Referr to the previous error messages"
							+ " or contact the database host / plugin developer to help resolve the issue.");
			}
		}
		return null;
	}
	
	/**
	 * Closes a given connection, catching any errors
	 * @param connection
	 */
	public static void close(Connection connection){
		if (connection != null){
			try {
				connection.close();
				connection = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
