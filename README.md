# GuildCraft Command Scheduler Documentation #

## Installation ##
Just drop the GCCommandScheduler.jar file and the GCCommandScheduler folder in your server’s plugin folder. You’ll also want to set up a MySQL database and modify the config.yml file to direct the plugin to said database (read on for specifics)

## Overview ##
This Minecraft plugin is designed to connect to a MySQL database and run commands from the database at a regular interval. The MySQL database must have the following columns:

| id | to_execute | player_username | which_server | timestamp_created | timestamp_executed | status |
|----|------------|-----------------|--------------|-------------------|--------------------|--------|

The plugin checks the database at a regular interval, and gets all rows from the database that have a status of “pending” and a which_server that matches the plugin’s target server. It then checks that the player specified in player_username is either online or is the console. If so, the plugin runs the Minecraft command in to_execute, updates the timestamp_executed value to the plugin’s current time and sets the status to “executed”.

## Configuration ##
The plugin comes with a config.yml file under the GCCommandScheduler folder where you can change:
  * The database’s host and database config
  *	The interval between database checks in seconds
  *	The plugin’s specified server
  *	The time zone for generating the timestamps

Be careful when editing the config.yml file, as using invalid fields will result in the plugin failing to work.

You can also prevent certain commands being run through the plugin by adding them to the plugin’s commandBlacklist.txt file. By default, /op is blacklisted as an example. You can also blacklist certain commands only if they have certain arguments, for example blacklisting ‘/msg fred’ will prevent that command from being run, but other /msg commands like ‘/msg greg’ will work.

## Plugin Commands ##
The plugin also has the following commands:
   * **/gccs about** 	Shows a brief description of the plugin, its current version and its author
   * **/gccs reload** 	Reloads the plugin, including changes made to the config and commandBlacklist files
   * **/gccs update** 	Immediately runs any pending commands on the database.
   * **/gccs test**	runs various tests to ensure the plugin is working correctly. You won’t need this unless something goes wrong

All commands except /gccs about require the gccs.access permission, which by default is only given to op.

## Error Handling ##
If the plugin runs into an error, it will say that it ran into an error on the server’s console and stop running. It will then write the error to a log file in the plugin’s directory.

If the file contains something like “connection link error”, then it could not connect to the database. Check that the database settings are correct in the config file and that the database exists. If you run into a different SQL error, make sure that your database has the correct columns mentioned at the start of this document.

Once you have resolved the issue, run the /gccs reload command to restart the plugin. If you still cannot resolve the issue after this, contact Jonodonozym, the plugin developer at jonodonozymdev@gmail.com or through other means like Discord.
