package guildcraftCommandScheduler.main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import guildcraftCommandScheduler.testing.GCCSTest;

/**
 * Commands for the GCCS plugin.
 * Commands are:
 * gccs about
 * gccs reload
 * gccs update
 * gccs stop
 * gccs test
 * See the plugin.yml for more details
 * @author Jonodonozym
 *
 */
public class PluginCommands implements CommandExecutor {

	GCCSMain plugin;
	
	public PluginCommands(GCCSMain plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		PluginDescriptionFile pdfile = plugin.getDescription();
		
		if (args.length == 0)
			plugin.getServer().dispatchCommand(sender, "help gccs");
		else{
			CommandDatabase cdb = GCCSMain.getCommandDatabase();
			switch(args[0].toLowerCase()){
			
			
				case "about":
					sender.sendMessage(ChatColor.GOLD+"## "+ChatColor.WHITE+"GuildCraft Command Scheduler");
					sender.sendMessage(ChatColor.GOLD+"## "+ChatColor.GREEN+"A plugin that runs scheduled commands from an SQL database");
					sender.sendMessage(ChatColor.GOLD+"## "+ChatColor.GREEN+"Version: "+ChatColor.YELLOW+pdfile.getVersion());
					sender.sendMessage(ChatColor.GOLD+"## "+ChatColor.GREEN+"Author:  "+ChatColor.YELLOW+pdfile.getAuthors().get(0));
					break;
					

				case "reload":
				case "restart":
				case "reloadconfig":
				case "reloadblacklist":
				case "r":
					if (sender.hasPermission("gccs.access")){
						plugin.reloadConfig();
						plugin.reloadDatabase();
						sender.sendMessage(ChatColor.GREEN+"[GCCommandSheduler] config and blacklist reloaded");
					}
					else
						sender.sendMessage(ChatColor.RED+"[GCCommandSheduler] You don't have the permissions to do that!");
					break;
					
					
					
				case "update":
				case "u":
					if (sender.hasPermission("gccs.access")){
						if (cdb == null)
								sender.sendMessage(ChatColor.RED+"[GCCommandSheduler] Error: gccs has been stopped. Type /gccs reload to start it again");
						else if(cdb.executePendingCommands())
							sender.sendMessage(ChatColor.GREEN+"[GCCommandSheduler] All pending commands were executed");
						else
							sender.sendMessage(ChatColor.RED+"[GCCommandSheduler] Error: not connected to SQL database. "
									+ "Check that the config.yml file is correct and type /gccs reload");
					}
					else
						sender.sendMessage(ChatColor.RED+"[GCCommandSheduler] You don't have the permissions to do that!");
					break;
					
					
					
				case "test":
				case "t":
					if (sender.hasPermission("gccs.access"))
						if (cdb == null)
							sender.sendMessage(ChatColor.RED+"[GCCommandSheduler] Error: gccs has been stopped. Type /gccs reload to start it again");
						else
							GCCSTest.runTests(cdb, sender, cdb.getConnection(), cdb.getTable(), cdb.getServer());
					else
						sender.sendMessage(ChatColor.RED+"[GCCommandSheduler] You don't have the permissions to do that!");
					break;
					
					
					
				case "stop":
				case "s":
					if (sender.hasPermission("gccs.access")){
						plugin.stop();
						sender.sendMessage(ChatColor.GREEN+"[GCCommandSheduler] The plugin was stopped. Use /gccs reload to re-start it.");
					}
					else
						sender.sendMessage(ChatColor.RED+"[GCCommandSheduler] You don't have the permissions to do that!");
					break;
					
					
					
				default: plugin.getServer().dispatchCommand(sender, "help gccs");
			}
		}
		return true;
	}
}
