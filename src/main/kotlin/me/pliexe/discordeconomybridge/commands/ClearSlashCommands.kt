package me.pliexe.discordeconomybridge.commands

import github.scarsz.discordsrv.DiscordSRV
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import net.dv8tion.jda.api.utils.MiscUtil
import net.dv8tion.jda.internal.utils.Checks
import net.dv8tion.jda.internal.utils.Helpers
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ClearSlashCommands(val main: DiscordEconomyBridge) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(args.isEmpty())
        {
            sender.sendMessage("Please provide the ID of the guild you want to clear slash commands in!")
            return true
        } else {

            val guildID = args[0]

            if(guildID.toLongOrNull() == null)
            {
                sender.sendMessage("Invalid guild id!")
                return true
            }

            if(main.getJda() == null) {
                val guild = DiscordSRV.getPlugin().jda.getGuildById(guildID)

                if(guild != null) {
                    guild.retrieveCommands().queue { commandMutableList ->
                        commandMutableList.forEach { command ->
                            command.delete().queue()
                        }
                    }
                } else {
                    sender.sendMessage("Unable to find guild(server)")
                    return true
                }
            } else {

                val guild = main.getJda()!!.getGuildById(guildID)

                if(guild != null) {
                    guild.retrieveCommands().queue { commandMutableList ->
                        commandMutableList.forEach { command ->
                            command.delete().queue()
                        }
                    }
                } else {
                    sender.sendMessage("Unable to find guild(server)")
                    return true
                }
            }

            sender.sendMessage("Cleared all slash commands!")
        }

        return true
    }
}