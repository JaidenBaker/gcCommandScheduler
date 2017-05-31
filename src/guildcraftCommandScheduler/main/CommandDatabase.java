package guildcraftCommandScheduler.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import guildcraftCommandScheduler.utils.CustomSqlApi;


/**
 * The Database class interfaces with the command database by opening / closing the connection,
 * fetching all un-executed commands and executing commands.
 * @author Jonodonozym
 */
public class CommandDatabase {

	private final GCCSMain plugin;
	
	private final String host;
	private final String dbName;
	private final String dbUsername;
	private final String dbPassword;
	private final String dbTable;
	private final String server;
	private final int port;

	private Connection connection;
	
	private Set<String> commandBlacklist;
	
	public CommandDatabase(GCCSMain plugin){
		this.plugin = plugin;
		
		// loading config file
		FileConfiguration configFile = plugin.getConfig();
		
		// loading data from config
        host = configFile.getString("host");
        port = configFile.getInt("port");
        dbName = configFile.getString("database");
        dbUsername = configFile.getString("username");
        dbPassword = configFile.getString("password");
		dbTable = configFile.getString("table"); 
		server = configFile.getString("server");
		
		// load the blacklist file
		commandBlacklist = new HashSet<String>();
		commandBlacklist.add("gccs"); //to prevent infinite loops when calling 'gccs update' through the sql database
		BufferedReader bfr;
		try {
			bfr = new BufferedReader(new FileReader(new File(plugin.getDataFolder()+ File.separator +"commandBlacklist.txt")));
			String line = bfr.readLine();
			while(line != null){
				if (line.startsWith("#") || line.trim().equals("")){
					line = bfr.readLine();
					continue;
				}
				if (line.startsWith("/"))
					line = line.substring(1);
				commandBlacklist.add(line);
				line = bfr.readLine();
			}
			bfr.close();
		}
		catch (IOException e) { e.printStackTrace(); }
	}
	
	/**
	 * Opens a connection to the database specified in the config.yml file
	 * @return true if successful
	 */
	public boolean openConnection(){
		if (connection != null){
			plugin.getLogger().info("Closing existing connection...");
			closeConnection();
		}
		connection = CustomSqlApi.open(plugin.getLogger(), host, port, dbName, dbUsername, dbPassword);
		
		if (connection == null)
			return false;
		
		// ensures the table exists (
		if (!CustomSqlApi.hasTable(connection, dbTable)){
			plugin.getLogger().info("WARNING: No table found named "+dbTable+". The table has been created, "
					+ "but double-check that the table name you have entered is correct to prevent further errors.");
			CustomSqlApi.createTable(connection, dbTable);
		}
		
		return true;
	}
	
	/**
	 * Closes the connection if it is open
	 */
	public void closeConnection(){
		CustomSqlApi.close(connection);
		plugin.getLogger().info("Connection successfully closed.");
	}
	
	
	/**
	 * Queries the server for all pending commands for the server written in the config.yml file
	 * runs the pending commands if they are not blacklisted
	 */
	public boolean executePendingCommands(){
		if (connection != null) {
		    // fetches all commands that are pending for this server
		    List<String[]> rows = CustomSqlApi.fetchPendingRows(
		    		connection, dbTable, server);
		    
		    // executes all non-blocked commands
		    for (String[] row: rows){
		    	int id = Integer.parseInt(row[0]);
		    	String command = row[1];
		    	String username = row[2];
		    	
		    	if (isBlocked(command)){
		    		plugin.getLogger().info("'"+command+"' scheduled, but was blocked due to being in commandBlacklist.txt");
				    CustomSqlApi.setStatus(connection, dbTable, id, "blocked");
		    	}
		    	
		    	// checks if the player is online or is the console
		    	else if(username.equals("CONSOLE") ||
		    			username.equals("GCCS plugin") ||
		    			plugin.getServer().getPlayer(username) != null){
		    		plugin.getLogger().info("Command '"+command+"' run from SQL server for player "+username);
		    		plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
				    CustomSqlApi.setStatus(connection, dbTable, id, "executed");
		    	}
		    }
		    return true;
		}
		return false;
	}
	
	/**
	 * Returns whether or not a command is blocked
	 * @param command
	 * @return true if blocked, false otherwise
	 */
	public boolean isBlocked(String command){
		for (String s: commandBlacklist)
			if (command.startsWith(s+" ") || command.equals(s))
				return true;
		return false;
	}
	
	// getters, setters and checkers
	public Connection getConnection(){ return connection; }
	public String getServer(){ return server; }
	public String getTable(){ return dbTable; }

	public boolean containsBlockedCommand(String command){ return commandBlacklist.contains(command); }
	public void addBlockedCommand(String command){ commandBlacklist.add(command); }
	public void removeBlockedCommand(String command){ commandBlacklist.remove(command); }
}
