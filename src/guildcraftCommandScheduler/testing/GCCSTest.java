package guildcraftCommandScheduler.testing;

import java.sql.Connection;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import guildcraftCommandScheduler.main.CommandDatabase;
import guildcraftCommandScheduler.utils.CustomSqlApi;

/**
 * Testing class with a static runTests method
 * @author Jonodonozym
 */
public class GCCSTest {
	/**
	 * Tests to see if the GCCommandSheduler plugin and SQL server are working correctly
	 */
	public static void runTests(CommandDatabase database, CommandSender sender, Connection connection, String table, String server){	
		sender.sendMessage(ChatColor.GRAY+"[GCCommandSheduler] Testing the plugin. This may take a while...");

		testValidCommands(database, sender, connection, table, server);
		testInvalidServer(database, sender, connection, table, server);
		testInvalidStatus(database, sender, connection, table, server);
		testBlockedCommands(database, sender, connection, table, server);
		testTableCreation(connection, sender);
		
		sender.sendMessage(ChatColor.GRAY+"[GCCommandSheduler] Plugin testing complete! Read the above lines and check that they are all correct.");
	}
	
	/**
	 * Tests to see that valid commands (in this case, the 'tell' command) can be sent to and received from the sql server
	 */
	private static void testValidCommands(CommandDatabase database, CommandSender sender, Connection connection, String table, String server){
		if (sender instanceof Player)
			sender.sendMessage(ChatColor.GRAY+"[GCCommandSheduler] Testing valid commands. 2 messages saying 'command recieved' should pop up.");
		else
			sender.sendMessage("[GCCommandSheduler] Now testing valid commands. 2 messages saying 'command run from SQL server' should pop up.");

		CustomSqlApi.addCommand(connection, table, "tell "+sender.getName()+" First server command recieved!", server, sender.getName(), true);
		database.executePendingCommands();
		CustomSqlApi.addCommand(connection, table, "tell "+sender.getName()+" Second server command recieved!", server, sender.getName(), true);
    	database.executePendingCommands();
    	
    	sender.sendMessage(ChatColor.GRAY+"[GCCommandSheduler] Testing valid commands complete");
	}
	
	/**
	 * Makes sure that the plugin doesn't run commands intended for a different server
	 */
	private static void testInvalidServer(CommandDatabase database, CommandSender sender, Connection connection, String table, String server){
    	sender.sendMessage(ChatColor.GRAY+"[GCCommandSheduler] Testing which server the command gets sent to. You should see nothing.");
    	
		CustomSqlApi.addCommand(connection, table, "tell "+sender.getName()+" This should not be displayed! This command is for another server", sender.getName(), "anlsdfasdgzxckjojdsv", true);
		database.executePendingCommands();
		
    	sender.sendMessage(ChatColor.GRAY+"[GCCommandSheduler] Testing which server complete");
	}
	
	/**
	 * Makes sure that the plugin doesn't run commands that are already executed
	 */
	private static void testInvalidStatus(CommandDatabase database, CommandSender sender, Connection connection, String table, String server){
    	sender.sendMessage(ChatColor.GRAY+"[GCCommandSheduler] Testing already executed commands. You should see nothing");
    	
		CustomSqlApi.addCommand(connection, table, "tell "+sender.getName()+" This should not be displayed! This command is already executed", sender.getName(), server, false);
		database.executePendingCommands();
		
    	sender.sendMessage(ChatColor.GRAY+"[GCCommandSheduler] Testing already executed commands complete");
	}
	
	/**
	 * Checks to make sure that the plugin blocks commands correctly
	 */
	private static void testBlockedCommands(CommandDatabase database, CommandSender sender, Connection connection, String table, String server){
		if (sender instanceof Player)
			sender.sendMessage(ChatColor.GRAY+"[GCCommandSheduler] Now testing blocked commands. If everything works, you should see nothing");
		else
			sender.sendMessage(ChatColor.GRAY+"[GCCommandSheduler] Now testing blocked commands. If everything works, you should see 2 'command blocked' messages");

		CustomSqlApi.addCommand(connection, table, "gccs", server, sender.getName(), true);
		CustomSqlApi.addCommand(connection, table, "gccs reload", server, sender.getName(), true);
		database.executePendingCommands();
		
		sender.sendMessage(ChatColor.GRAY+"[GCCommandSheduler] Blocked commands done testing.");
	}
	
	/**
	 * Creates a test table and ensures that the required columns are present
	 */
	private static void testTableCreation(Connection connection, CommandSender sender){
		CustomSqlApi.createTable(connection, "test_table");
		if (!CustomSqlApi.checkColumns(connection, "test_table"))
				sender.sendMessage(ChatColor.RED+"[GCCommandSheduler] Test table doesn't have the right columns! Contact the developer to fix this issue!");
		CustomSqlApi.dropTable(connection, "test_table");
	}
}
