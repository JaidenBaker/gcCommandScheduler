package guildcraftCommandScheduler.utils;

import java.io.File;
import java.sql.Connection;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import guildcraftCommandScheduler.main.Main;


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
	
	private String server;
	
	private Connection connection;

	private int CommandPointer = 0;
	
	public CommandDatabase(Main plugin){
		this.plugin = plugin;
		
		// loading config file
		FileConfiguration configFile = plugin.getConfig();
		
		// loading data from config
        host = configFile.getString("host");
        dBName = configFile.getString("database");
        dBUsername = configFile.getString("username");
        dBPassword = configFile.getString("password");
		dBTable = configFile.getString("table"); 

		server = configFile.getString("server");
	}
	
	public void openConnection(){
		connection = ServerConnection.open(plugin.getLogger(), host, dBName, dBUsername, dBPassword);
	}
	
	public void closeConnection(){
		ServerConnection.close(connection);
	}
	
	
	public void executePendingCommands(){
		//plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "broadcast test");
	}
}
