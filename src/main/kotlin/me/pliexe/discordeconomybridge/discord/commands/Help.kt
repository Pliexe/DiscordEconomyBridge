package me.pliexe.discordeconomybridge.discord.commands

import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class Help(main: DiscordEconomyBridge): Command(main) {

    override val usage: String
        get() = ""

    override val name: String
        get() = "help"

    override fun getSlashCommandData(): CommandData {
        return CommandData(name, "Show basics of bot")
    }

    override fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        event.channel.sendMessageEmbeds(LegacyGetYmlEmbed({
            val form = setCommandPlaceholders(it, prefix, name, usage)
//                .replace("{commands}")
            if(event.member == null)
                setDiscordPlaceholders(event.author, form)
            else setDiscordPlaceholders(event.member!!, form)
        }, "helpCommandEmbed", main.discordMessagesConfig).build()).queue()
    }

    override fun run(event: github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        event.channel.sendMessageEmbeds(GetYmlEmbed( {
            val form = setCommandPlaceholders(it, prefix, name, usage)
//                .replace("{commands}")
            if(event.member == null)
                setDiscordPlaceholders(event.author, form)
            else setDiscordPlaceholders(event.member!!, form)
        }, "helpCommandEmbed", main.discordMessagesConfig).build()).queue()
    }

    override fun run(event: SlashCommandEvent) {
        event.channel.sendMessageEmbeds(GetYmlEmbed( {
            val form = setCommandPlaceholders(it, name, usage)
            if(event.member == null)
                setDiscordPlaceholders(event.user, form)
            else setDiscordPlaceholders(event.member!!, form)
        }, "helpCommandEmbed", main.discordMessagesConfig).build()).queue()
    }
}