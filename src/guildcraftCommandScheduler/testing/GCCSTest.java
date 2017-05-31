package guildcraftCommandScheduler.testing;

import java.sql.Connection;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import guildcraftCommandScheduler.main.Main;
import guildcraftCommandScheduler.utils.CustomSqlApi;

public class GCCSTest {
	public static void runTests(CommandSender sender, Connection connection, String table, String server){	
		if (sender instanceof Player){
			sender.sendMessage(ChatColor.GRAY+"Testing the GCCS plugin. This may take a while...");
			sender.sendMessage(ChatColor.GRAY+"If everything works correctly, exactly 2 messages saying 'command recieved' should pop up.");
			sender.sendMessage(ChatColor.GRAY+"One command will be delayed, so wait at least a minute before panicking");
		}

		if (sender instanceof ConsoleCommandSender){
			Main.getPluginLogger().info("Testing the GCCS plugin. This may take a while...");
			Main.getPluginLogger().info("If everything works correctly, exactly 2 messages saying 'player 'CONSOLE' not found' and exactly 2 saying 'command blocked' should pop up.");
		}
		
		CustomSqlApi.createTable(connection, "test_table");
		if (!CustomSqlApi.checkColumns(connection, "test_table")){
				sender.sendMessage(ChatColor.RED+"Test table doesn't have the right columns! Contact the developer to fix this issue!");
				CustomSqlApi.dropTable(connection, "test_table");
				return;
		}
		CustomSqlApi.dropTable(connection, "test_table");
		
		CustomSqlApi.addCommand(connection, table, "tell "+sender.getName()+" First server command recieved!", server, true);
		
		// these two should be blocked
		CustomSqlApi.addCommand(connection, table, "op aXed", server, true);
		CustomSqlApi.addCommand(connection, table, "op", server, true);

		CustomSqlApi.addCommand(connection, table, "tell "+sender.getName()+" This should not be displayed! (Error code 0)", server, false);
		CustomSqlApi.addCommand(connection, table, "tell "+sender.getName()+" This should not be displayed! (Error code 1)", server+"_TESTING", true);
		
		new java.util.Timer().schedule( 
		        new java.util.TimerTask() {
		            @Override
		            public void run() {
		        		CustomSqlApi.addCommand(connection, table, "tell "+sender.getName()+" Second server command recieved!", server, true);
		            }
		        }, 
		        20000L 
		);
	}
}
