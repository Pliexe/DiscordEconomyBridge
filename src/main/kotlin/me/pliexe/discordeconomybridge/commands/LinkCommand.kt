package me.pliexe.discordeconomybridge.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LinkCommand(val main: DiscordEconomyBridge) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender is Player) {
            if(main.linkHandler.underWaitList(sender.uniqueId)) {
                val code = main.linkHandler.getCode(sender.uniqueId)

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', main.pluginMessages.linkingInProcess
                    .replace("{code}", code ?: "Failed to retrieve code")
                ))
            } else {
                if(main.linkHandler.isLinked(sender.uniqueId))
                {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', main.pluginMessages.alreadyLinked))
                    return true
                }

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', main.pluginMessages.linkAccount
                    .replace("{code}", main.linkHandler.prepareLink(sender.uniqueId))
                    .replace("{bot}", main.botTag)
                ))
            }
        } else sender.sendMessage(main.pluginMessages.noConsole)
        return true
    }
}