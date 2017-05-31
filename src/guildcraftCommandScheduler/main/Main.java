package guildcraftCommandScheduler.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin {
	
	private long intervalTicks = 0L;
	
	public CommandDatabase database = null;
	
	private static Logger logger;
	
	@Override
	public void onEnable(){
		loadDefaultConfig();
		
		intervalTicks = getConfig().getLong("interval")*20L;
		logger = getLogger();
		
		reloadDatabase();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				database.executePendingCommands();
			}
		}.runTaskTimer(this, 0, intervalTicks);
		
		getCommand("gccs").setExecutor(new PluginCommands(this));
	}

	@Override
	public void onDisable(){
		database.closeConnection();
	}
	
	/**
	 * Re-loads the database and re-connects to the SQL server
	 * Includes any changes made to the config and blacklist files
	 */
	public void reloadDatabase(){
		if (database != null)
			database.closeConnection();
		database = new CommandDatabase(this);
		database.openConnection();
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
		    catch (IOException e) { getLogger().info("Error loading default blacklist: "+e); }
		}
	}
	
	public static Logger getPluginLogger(){
		return logger;
	}
}
