# THIS README IS OUTDATED, PLEASE USE THE DOCUMENT ON https://www.spigotmc.org/resources/discord-economy-bridge-1-7-1-16.90290/

# DiscordEconomyBridge

 - This is a plugin for having shortcut commands inside discord to control economy
 - Note that this plugin won't work with offline users if the server is in offline environment (but should work with bungee online mode)
#### TODO:
 - Tell me something todo

#### Please suggest features

## Dependencies
 - Vault
 - An economy plugin that supports Vault

## Features
 - Add/Remove money from player trough discord
 - Check player's balance trough discord (offline checking only works for online mode servers!)
 - Custom command creation with placeholders
 - All inbuilt command responses are customizable (with a custom embed builder in config.yml)

## Bot commands
 - help - Shows all the available commands
 - bal, balance <username or uuid> - Shows the balance of a player.
 - addmoney <amount> <username or uuid> - Adds money to the specified player's balance
 - removemoney, remmoney <amount> <username or uuid> - Removes money from the specified player's balance
 - leaderboard, lb, top - Shows the top 10 richest players on the server!

## Installation

### Making a discord bot account
- Go to https://discordapp.com/developers/applications/me
- Create a new application
- Name your application whatever you want and click create
- Now go to Bot settings and click Add Bot button and click Yes, do it!
- Now if you want you can change the profile picture of the bot here but what we are here for is the token
- So click copy to get the token, we will need this for later

### Setting up the plugin
- Load up the server with the plugin, this should generate config.yml in the plugins/DiscordEconomyBridge folder
- In config.yml in TOKEN field change BOT_TOKEN to the token you just copied
- Now restart the server, and the bot should be online