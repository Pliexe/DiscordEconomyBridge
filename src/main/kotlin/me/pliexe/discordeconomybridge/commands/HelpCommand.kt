package me.pliexe.discordeconomybridge.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class HelpCommand(val main: DiscordEconomyBridge) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(main.getJda() == null)
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', main.pluginMessages.helpCommandFail))
        else
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', main.pluginMessages.helpCommand))
        return true
    }
}