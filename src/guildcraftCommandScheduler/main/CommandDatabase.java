package guildcraftCommandScheduler.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;

import guildcraftCommandScheduler.utils.CustomSqlApi;


/**
 * The Database class interfaces with the command database by opening / closing the connection,
 * fetching all un-executed commands and executing commands.
 * @author Jonodonozym
 *
 */
public class CommandDatabase {

	private final Main plugin;
	
	private final String host;
	private final String dBName;
	private final String dBUsername;
	private final String dBPassword;
	private final String dBTable;
	private final String server;
	private final int port;

	private Connection connection;
	
	private int commandPointer = 0;
	
	private Set<String> commandBlacklist;
	
	
	public CommandDatabase(Main plugin){
		this.plugin = plugin;
		
		// loading config file
		FileConfiguration configFile = plugin.getConfig();
		
		// loading data from config
        host = configFile.getString("host");
        port = configFile.getInt("port");
        dBName = configFile.getString("database");
        dBUsername = configFile.getString("username");
        dBPassword = configFile.getString("password");
		dBTable = configFile.getString("table"); 
		server = configFile.getString("server");
		
		// load the blacklist file
		commandBlacklist = new HashSet<String>();
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
	
	public void openConnection(){
		if (connection != null){
			plugin.getLogger().info("Closing existing connection...");
			closeConnection();
		}
		connection = CustomSqlApi.open(plugin.getLogger(), host, port, dBName, dBUsername, dBPassword);
		
		// ensures the table exists (
		if (!CustomSqlApi.hasTable(connection, dBTable)){
			plugin.getLogger().info("WARNING: No table found named "+dBTable+". The table has been created, "
					+ "but double-check that the table name you have entered is correct to prevent further errors.");
			CustomSqlApi.createTable(connection, dBTable);
		}
	}
	
	public void closeConnection(){
		plugin.getLogger().info("Connection successfully closed.");
		CustomSqlApi.close(connection);
	}
	
	
	/**
	 * Queries the server for all records after the last accessed record
	 * if the status is pending and the which_server matches the server in the config
	 * runs the command is not blacklisted
	 */
	public void executePendingCommands(){
		int maxID = CustomSqlApi.getMaxID(connection, dBTable);
		
		// no commands found
		if (maxID == -1)
			return;
		
	    // fetches all commands that are pending for this server
	    List<String[]> rows = CustomSqlApi.fetchPendingRowsBetween(
	    		connection, dBTable, server, commandPointer, maxID);
	    
	    // executes all non-blocked commands
	    Server s = plugin.getServer();
	    for (String[] row: rows){
	    	int id = Integer.parseInt(row[0]);
	    	String command = row[1];
	    	
	    	if (isBlocked(command)){
	    		plugin.getLogger().info("'"+command+"' scheduled, but was blocked due to being in commandBlacklist.txt");
			    CustomSqlApi.setStatus(connection, dBTable, id, "blocked");
	    	}
	    	else {
	    		s.dispatchCommand(Bukkit.getConsoleSender(), command);
			    CustomSqlApi.setStatus(connection, dBTable, id, "executed");
	    	}
	    }
	    
	    commandPointer = maxID+1;
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
	
	public Connection getConnection(){
		return connection;
	}
	
	public String getServer(){
		return server;
	}
	
	public String getTable(){
		return dBTable;
	}
}
