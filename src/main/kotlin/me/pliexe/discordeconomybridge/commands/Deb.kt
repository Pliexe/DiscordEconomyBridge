package me.pliexe.discordeconomybridge.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class Deb(val main: DiscordEconomyBridge) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(args.isEmpty() || args[0] != "reload") {
            if(main.getJda() == null)
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', main.pluginMessages.helpCommandFail))
            else
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', main.pluginMessages.helpCommand))
            return true
        } else {
            if(sender.isOp || sender.hasPermission("discordeconomybridge.reload"))
            {
                sender.sendMessage("Starting reload...")

                main.defaultConfig.forceReload()
                main.moderatorManager.resetRoles()
                main.moderatorManager.LoadFromConfig()
                main.pluginMessagesConfig.forceReload()
                main.discordMessagesConfig.forceReload()
//        main.customCommandsConfig.forceReload()

                sender.sendMessage("Done reloading!")

                return true
            } else {
                sender.sendMessage("You don't have permission to run this command!")
                return true
            }
        }
    }
}
