package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*

class Help(main: DiscordEconomyBridge): Command(main) {

    override val usage: String
        get() = ""

    override val name: String
        get() = "help"

    override val description: String
        get() = "Show what all commands do!"

    override fun getCommandOptions(): CommandOptions {
        return CommandOptions()
    }

    override fun run(event: CommandEventData) {
        event.sendYMLEmbed("helpCommandEmbed", {
            val form = setCommandPlaceholders(it, event.prefix, event.commandName, description, usage)

            if(event.member == null)
                setDiscordPlaceholders(event.author, form)
            else setDiscordPlaceholders(event.member!!, form)
        }).queue()

        main.commandHandler.commandComplete(this, event)
    }
}