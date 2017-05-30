package guildcraftCommandScheduler.main;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import guildcraftCommandScheduler.utils.CommandDatabase;

public class Main extends JavaPlugin {
	
	private long intervalTicks = 0L;
	
	private CommandDatabase database = null;
	
	private BukkitRunnable loop;
	
	@Override
	public void onEnable(){
		// creates default config if one doesn't exist
		File file = new File(getDataFolder() + File.separator + "config.yml");
		if(!file.exists())
			saveDefaultConfig();
		
		// loads the config
		FileConfiguration configFile = getConfig();
		intervalTicks = configFile.getLong("interval")*20L;
		
		database = new CommandDatabase(this);
		database.openConnection();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				database.executePendingCommands();
			}
		}.runTaskTimer(this, 0, intervalTicks);
	}

	@Override
	public void onDisable(){
		database.closeConnection();
	}
}
