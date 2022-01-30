package me.pliexe.discordeconomybridge.discord

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.getEmbedFromYml
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.bukkit.Server
import org.bukkit.configuration.file.FileConfiguration
import java.awt.Color

abstract class Command(protected val main: DiscordEconomyBridge) {

    protected val config: FileConfiguration = main.config
    protected val server: Server = main.server

    protected abstract val usage: String
    abstract fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>)

    val adminCommand = false

    fun fail(event: GuildMessageReceivedEvent, message: String)
    {
        val embed = GetYmlEmbed(event.channel, {
            if(event.member == null)
                setDiscordPlaceholders(event.author, it)
            else
                setDiscordPlaceholders(event.member!!, it)
        }, "failMessage", main.discordMessagesConfig)

        event.channel.sendMessageEmbeds(embed.build()).queue()
    }

    fun noPermission(event: GuildMessageReceivedEvent)
    {
        val embed = GetYmlEmbed(event.channel, {
            if(event.member == null)
                setDiscordPlaceholders(event.author, it)
            else
                setDiscordPlaceholders(event.member!!, it)
        }, "noPermissionMessage", main.discordMessagesConfig)

        event.channel.sendMessageEmbeds(embed.build()).queue()
    }
}