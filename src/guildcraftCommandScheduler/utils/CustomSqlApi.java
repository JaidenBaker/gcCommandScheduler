package guildcraftCommandScheduler.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Utility class with static methods to interact with the sql database
 * @author Jonodonozym
 */
public class CustomSqlApi {

	private static final long retryInterval = 5L;
	private static final int maxFails = 3;
	private static final String driver = "com.mysql.jdbc.Driver";
	private static int numFails = 0;
	
	/**
	 * Opens a new connection to a specified database
	 * @param logger
	 * @param host
	 * @param databaseName
	 * @param username
	 * @param password
	 * @return the opened connection, or null if one couldn't be created
	 */
	public static Connection open(Logger logger, String host, int port, String databaseName, String username, String password){
		// connecting to database server, with at most 3 failed attempts every 10 seconds before it gives up
		numFails = 0;
		while (numFails <= maxFails){
			try {
				try {
					Class.forName(driver).newInstance();
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String url = "jdbc:mysql://"+host+":"+port+"/"+databaseName;
				logger.info("Attempting to connect to "+url);
				Connection c = DriverManager.getConnection(url,username,password);
				logger.info("Successfully connected to the "+databaseName+" database at "+host);
				return c;
			}
			catch (SQLException e) {
				numFails++;
				logger.info("Failed to connect to the database: ");
				e.printStackTrace();
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
	public static boolean close(Connection connection){
		if (connection != null){
			try {
				connection.close();
				connection = null;
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Gets the maximum id from a given table
	 * @param connection
	 * @param Table
	 * @return the max id, or -1 if the table doesn't exist
	 */
	public static int getMaxID(Connection connection, String Table){
		int maxID = -1;
		Statement stmt = null;
	    String query = "SELECT MAX(id) as id FROM " + Table+";";
	    try {
	        stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	        while (rs.next()) {
	        	maxID = rs.getInt("id");
	        }
	    } catch (SQLException e ) {
	        e.printStackTrace();
	    } finally {
	        if (stmt != null) {
	        	try { stmt.close(); }
	        	catch (SQLException e) { e.printStackTrace(); }
	        }
	    }
	    return maxID;
	}
	
	/**
	 * Returns the ID and command of all pending rows between two ID's (inclusive)
	 * @param connection
	 * @param table
	 * @param server
	 * @param startID
	 * @param endID
	 * @return A list of the rows, where a row is [id,command,username]
	 */
	public static List<String[]> fetchPendingRowsBetween(Connection connection, String table, String server, int startID, int endID){
	    String query = "SELECT * FROM "+table+" "
	    		+ "WHERE id BETWEEN "+startID+" AND "+endID+" "
	    		+ "AND status = 'pending' "
	    		+ "AND which_server = '"+server+"';";
	    return fetchRows(connection, query);
	}
	
	/**
	 * Sets the status of a row in the table
	 * @param connection
	 * @param table
	 * @param id
	 * @param status
	 */
	public static void setStatus(Connection connection, String table, int id, String status){
	    String update = "UPDATE "+table+" "
	    		+ "SET status = '"+status+"', timestamp_executed = CURRENT_TIMESTAMP "
	    		+ "WHERE id = "+id+";";
	    executeUpdate(connection, update);
	}
	

	/**
	 * Adds a new command to the table
	 */
	public static void addCommand(Connection connection, String table, String command, String server, boolean isPending){
	    String update = "INSERT INTO "+table+" (id, to_execute, player_username, which_server, timestamp_created, status)"
	    		+ " VALUES (" + (getMaxID(connection, table)+1) + ", "
				+ "'"+command + "', "
				+ "'GCCS plugin', "
				+ "'"+server + "', "
				+ "CURRENT_TIMESTAMP, "
				+ (isPending ? "'pending'":"'executed'")
				+ ");";
	    executeUpdate(connection, update);
	}
	
	/**
	 * Creates a new table with a given name
	 * @param connection
	 * @param name
	 */
	public static void createTable(Connection connection, String name){
	    String update = "CREATE TABLE "+name+" ("
	    		+ "id int,"
	    		+ "to_execute varchar(1023),"
	    		+ "player_username varchar(255),"
	    		+ "which_server varchar(255),"
	    		+ "timestamp_created varchar(127),"
	    		+ "timestamp_executed varchar(127),"
	    		+ "status varchar(255));";
	    executeUpdate(connection, update);
	}
	
	
	/**
	 * Drops a table with the given name
	 * @param connection
	 * @param name
	 */
	public static void dropTable(Connection connection, String name){
	    String update = "DROP TABLE "+name+";";
	    executeUpdate(connection, update);
	}

	
	/**
	 * Executes a query, returning the rows if the database responds with them
	 * @param connection
	 * @param query
	 * @return
	 */
	private static List<String[]> fetchRows(Connection connection, String query){
		List<String[]> rows = new ArrayList<String[]>();
		Statement stmt = null;
	    try {
	        stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	        while (rs.next()) {
	        	String[] row = new String[2];
	        	row[0] = rs.getInt("id")+"";
	        	row[1] = rs.getString("to_execute");
	        	rows.add(row);
	        }
	    } catch (SQLException e ) {
	        e.printStackTrace();
	    } finally {
	        if (stmt != null) {
	        	try { stmt.close(); }
	        	catch (SQLException e) { e.printStackTrace(); }
	        }
	    }
	    return rows;
	}
	
	public static boolean hasTable(Connection connection, String Table){
		boolean returnValue = false;
		String query = "SHOW TABLES LIKE '"+Table+"';";
		Statement stmt = null;
	    try {
	        stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	        while (rs.next()) {
	        	returnValue = true;
	        }
	    } catch (SQLException e ) {
	        e.printStackTrace();
	    } finally {
	        if (stmt != null) {
	        	try { stmt.close(); }
	        	catch (SQLException e) { e.printStackTrace(); }
	        }
	    }
	    return returnValue;
	}
	
	public static boolean checkColumns(Connection connection, String table){
		boolean returnValue = true;
		Statement stmt = null;
	    try {
	        stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery("SELECT * FROM "+table);
	        ResultSetMetaData rsm = rs.getMetaData();
	        List<String> columnNames = new ArrayList<String>();
	        int columns = rsm.getColumnCount();
	        for (int i=1; i<=columns; i++)
	        	columnNames.add(rsm.getColumnName(i));
	        returnValue =
    				columnNames.contains("id") &&
    				columnNames.contains("to_execute") &&
	        		columnNames.contains("player_username") &&
	        		columnNames.contains("timestamp_created") &&
	        		columnNames.contains("timestamp_executed") &&
	        		columnNames.contains("which_server") &&
	        		columnNames.contains("status");
	    } catch (SQLException e ) {
	        e.printStackTrace();
	    } finally {
	        if (stmt != null) {
	        	try { stmt.close(); }
	        	catch (SQLException e) { e.printStackTrace(); }
	        }
	    }
	    return returnValue;
	}
	
	/**
	 * Executes an update
	 * @param connection
	 * @param update
	 */
	private static void executeUpdate(Connection connection, String update){
		Statement stmt = null;
	    try {
	        stmt = connection.createStatement();
	        stmt.executeUpdate(update);
	    } catch (SQLException e ) {
	        e.printStackTrace();
	    } finally {
	        if (stmt != null) {
	        	try { stmt.close(); }
	        	catch (SQLException e) { e.printStackTrace(); }
	        }
	    }
	}
}
