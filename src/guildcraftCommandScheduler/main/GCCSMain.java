package guildcraftCommandScheduler.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.mysql.jdbc.exceptions.MySQLTimeoutException;

/**
 * Main class for the GuildCraft Command Scheduler plugin (GCCS)
 * 
 * This plugin connects to an SQL server which stores a list of pending commands. The plugin
 * then checks the server for commands at a regular interval, and will dispatch them to the server
 * if the command's status is pending, the player that the command relates to is online and if the
 * server matches the one in the config.yml file
 * 
 * The SQL server can be modified by external sources while the plugin is running, although issues will
 * arise if pending commands are deleted while they are being executed.
 * 
 * @author Jonodonozym
 * @version 1.0 initial release
 */
public class GCCSMain extends JavaPlugin {
	
	private long intervalTicks = 0L;
	
	private CommandDatabase database = null;
	private BukkitTask loop = null;
	
	private static GCCSMain plugin;
	
	@Override
	public void onEnable(){
		plugin = this;
		loadDefaultConfig();

		
		reloadDatabase();
		
		getCommand("gccs").setExecutor(new PluginCommands(this));
	}
	
	@Override
	public void onDisable(){
		stop();
	}
	
	/**
	 * Re-loads the database and re-connects to the SQL server
	 * Includes any changes made to the config and blacklist files
	 */
	public void reloadDatabase(){
		stop();
		database = new CommandDatabase(this);
		
		Thread connectionThread = new Thread(){
			@Override public void run() {
				if (database.openConnection()){
					loop = startLoopTask(database);
				}
			}
		};
		
		connectionThread.run();
	}
	
	/**
	 * Creates default config and blacklist files if the don't exist in the plugin's directory
	 */
	private void loadDefaultConfig(){
		// creates default config.yml if one doesn't exist
		File file = new File(getDataFolder() + File.separator + "config.yml");
		if(!file.exists())
			saveDefaultConfig();

		// creates default commandBlacklist.txt if one doesn't exist
		file = new File(getDataFolder() + File.separator + "commandBlacklist.txt");
		if(!file.exists()){
			InputStream link = (getClass().getResourceAsStream("/commandBlacklist.txt"));
		    try { Files.copy(link, file.getAbsoluteFile().toPath()); }
		    catch (IOException e) { writeErrorLogFile(e);; }
		}
		
		reloadConfig();
	}
	
	/**
	 * Stops the plugin by disconnecting from the database and halting the update loop
	 */
	public void stop(){
		if (database != null){
			database.closeConnection();
			database = null;
		}
		if (loop != null){
			loop.cancel();
			loop = null;
		}
	}
	
	/**
	 * Starts the update loop, which will check the database at a regular interval
	 * @param database
	 * @return
	 */
	private BukkitTask startLoopTask(CommandDatabase database){
		if (database != null)
			return new BukkitRunnable() {
				@Override
				public void run() {
					database.executePendingCommands();
				}
			}.runTaskTimer(this, 0, intervalTicks);
		return null;
	}
	

	public static void writeErrorLogFile(Exception e){
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();
		writeErrorLogFile(exceptionAsString);
	}
	/**
	 * Writes an error log to a file, given an exception
	 * @param e
	 */
	public static void writeErrorLogFile(String s){
		plugin.getLogger().info("An error occurred. Check the Error log file for details. Once the issue is resolved, "
				+ "run the /gccs reload command");
		plugin.stop();
		String fileName = "Error log "+new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + ".txt";
		File file = new File(plugin.getDataFolder() + File.separator + fileName);
		try {
			BufferedWriter bfw = new BufferedWriter(new FileWriter(file));
			bfw.write("An error occurred in the GCCS plugin. If you can't work out the issue\n"
					+ "from this file, send this file to the plugin developer.");
			bfw.newLine();
			bfw.newLine();
			bfw.write(s);
			bfw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public void reloadConfig(){
		super.reloadConfig();
		TimeZone.setDefault(TimeZone.getTimeZone(getConfig().getString("timezone")));
		intervalTicks = getConfig().getLong("interval")*20L;
	}
	
	public static CommandDatabase getCommandDatabase(){ return plugin.database; }
}
