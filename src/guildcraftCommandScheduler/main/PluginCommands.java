package guildcraftCommandScheduler.main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import guildcraftCommandScheduler.testing.GCCSTest;

/**
 * Commands for the GCCS plugin
 * @author Jonodonozym
 *
 */
public class PluginCommands implements CommandExecutor {

	Main plugin;
	
	public PluginCommands(Main plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		PluginDescriptionFile pdfile = plugin.getDescription();
		
		if (args.length == 0)
			plugin.getServer().dispatchCommand(sender, "help gccs");
		else{
			switch(args[0].toLowerCase()){
				case "about":
					sender.sendMessage(ChatColor.GOLD+"## GuildCraft Command Scheduler ##");
					sender.sendMessage(ChatColor.GREEN+"A plugin that runs scheduled commands from an SQL database");
					sender.sendMessage(ChatColor.GREEN+"Version: "+ChatColor.YELLOW+pdfile.getVersion());
					sender.sendMessage(ChatColor.GREEN+"Author: "+ChatColor.YELLOW+pdfile.getAuthors());
					break;
				case "reloadconfig":
				case "reloadblacklist":
				case "reload":
				case "r":
					if (sender.hasPermission("gccs.access")){
						plugin.reloadConfig();
						plugin.reloadDatabase();
						sender.sendMessage(ChatColor.GREEN+"GCCS config and blacklist reloaded");
					}
					else
						sender.sendMessage(ChatColor.RED+"You don't have the permissions to do that!");
					break;
				case "update":
				case "u":
					if (sender.hasPermission("gccs.access"))
						plugin.database.executePendingCommands();
					else
						sender.sendMessage(ChatColor.RED+"You don't have the permissions to do that!");
					break;
				case "test":
				case "t":
					if (sender.hasPermission("gccs.access"))
						GCCSTest.runTests(sender, plugin.database.getConnection(), plugin.database.getTable(), plugin.database.getServer());
					else
						sender.sendMessage(ChatColor.RED+"You don't have the permissions to do that!");
					break;
				default: plugin.getServer().dispatchCommand(sender, "help gccs");
			}
		}
		
		return true;
	}
}
