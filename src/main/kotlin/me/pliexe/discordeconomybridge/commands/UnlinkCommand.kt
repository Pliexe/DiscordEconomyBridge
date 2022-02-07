package me.pliexe.discordeconomybridge.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UnlinkCommand(val main: DiscordEconomyBridge) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender is Player) {
            if(!main.linkHandler.isLinked(sender.uniqueId))
            {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', main.pluginMessages.accountNotLInked))
                return true
            }

            main.linkHandler.unLink(sender.uniqueId)

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', main.pluginMessages.unlinkAccount))
        } else sender.sendMessage(main.pluginMessages.noConsole)
        return true
    }
}