package me.pliexe.discordeconomybridge.discord

import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.bukkit.Server
import org.bukkit.configuration.file.FileConfiguration

abstract class Command(protected val main: DiscordEconomyBridge) {

    protected val config: FileConfiguration = main.config
    protected val server: Server = main.server

    public abstract val name: String
    public abstract val usage: String

    abstract fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>)
    abstract fun run(event: github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>)
    abstract fun run(event: SlashCommandEvent)

    abstract fun getSlashCommandData(): CommandData
    
    open val adminCommand = false

    fun fail(event: GuildMessageReceivedEvent, message: String)
    {
        val embed = LegacyGetYmlEmbed({
            val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), name, usage)
                .replace("{message}", message)

            if(event.member == null)
                setDiscordPlaceholders(event.author, form)
            else
                setDiscordPlaceholders(event.member!!, form)
        }, "failMessage", main.discordMessagesConfig)

        event.channel.sendMessageEmbeds(embed.build()).queue()
    }

    fun fail(event: github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent, message: String)
    {
        val embed = GetYmlEmbed( {
            val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), name, usage)
                .replace("{message}", message)

            if(event.member == null)
                setDiscordPlaceholders(event.author, form)
            else
                setDiscordPlaceholders(event.member!!, form)
        }, "failMessage", main.discordMessagesConfig)

        event.channel.sendMessageEmbeds(embed.build()).queue()
    }

    fun fail(event: SlashCommandEvent, message: String)
    {
        val embed = GetYmlEmbed( {
            val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), name, usage)
                .replace("{message}", message)

            if(event.member == null)
                setDiscordPlaceholders(event.user, form)
            else
                setDiscordPlaceholders(event.member!!, form)
        }, "failMessage", main.discordMessagesConfig)

        event.replyEmbeds(embed.build()).queue()
    }

    fun noPermission(event: GuildMessageReceivedEvent)
    {
        val embed = LegacyGetYmlEmbed({
            val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), name, usage)

            if(event.member == null)
                setDiscordPlaceholders(event.author, form)
            else
                setDiscordPlaceholders(event.member!!, form)
        }, "noPermissionMessage", main.discordMessagesConfig)

        event.channel.sendMessageEmbeds(embed.build()).queue()
    }

    fun noPermission(event: github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent)
    {
        val embed = GetYmlEmbed( {
            val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), name, usage)

            if(event.member == null)
                setDiscordPlaceholders(event.author, form)
            else
                setDiscordPlaceholders(event.member!!, form)
        }, "noPermissionMessage", main.discordMessagesConfig)

        event.channel.sendMessageEmbeds(embed.build()).queue()
    }

    fun noPermission(event: SlashCommandEvent)
    {
        val embed = GetYmlEmbed( {
            val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), name, usage)

            if(event.member == null)
                setDiscordPlaceholders(event.user, form)
            else
                setDiscordPlaceholders(event.member!!, form)
        }, "noPermissionMessage", main.discordMessagesConfig)

        event.replyEmbeds(embed.build()).queue()
    }
}