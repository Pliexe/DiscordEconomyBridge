package me.pliexe.discordeconomybridge.discord

import me.pliexe.discordeconomybridge.*
import me.pliexe.discordeconomybridge.discord.handlers.CommandHandler
import me.pliexe.discordeconomybridge.filemanager.Config
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Server
import org.bukkit.configuration.file.FileConfiguration

class Listener(main: DiscordEconomyBridge, server: Server, config: Config) : ListenerAdapter() {
    private val logger = server.logger

    private val commandHandler = CommandHandler(main)

    init {
        logger.info("[Discord Economy Bridge Bot] Loading Commands!")
        commandHandler.loadCommands()

        logger.info("[Discord Economy Bridge Bot] Loading command Aliases!")
        commandHandler.loadAliases()
    }

    override fun onReady(event: ReadyEvent) {

        logger.info("[Discord Economy Bridge Bot] Bot online!")
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        commandHandler.runCommand(event)
    }


}