package me.pliexe.discordeconomybridge.discordsrv

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.ButtonClickEvent
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent
import github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.privileges.CommandPrivilege
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.handlers.CommandHandler

class JDAListener(private val main: DiscordEconomyBridge): ListenerAdapter() {
    private val logger = main.logger
    private val commandHandler = CommandHandler(main)

    init {
        logger.info("[Discord Economy Bridge Bot] Loading Commands!")
        commandHandler.loadCommands()

        logger.info("[Discord Economy Bridge Bot] Loading command Aliases!")
        commandHandler.loadAliases()

        if(main.defaultConfig.isList("slashCommandServers"))
            main.defaultConfig.getStringList("slashCommandServers").forEach { guildID ->
                val guild = DiscordSRV.getPlugin().jda.getGuildById(guildID)
                if(guild != null) registerSlashCommands(guild)
            }
    }

    private fun registerSlashCommands(guild: Guild) {
        logger.info("Registering slash commands for the guild \"${guild.name}\" : ${guild.id}")
        guild.retrieveCommands().queue { retrievedCommands ->
            var updated = 0
            val updateCommands = guild.updateCommands()
            commandHandler.getCommands().forEach { (key, value) ->
                if(!retrievedCommands.any {it.name == key}) {
                    updateCommands.addCommands(value.getSlashCommandData())
                    updated++
                }
            }
            logger.info("Registering command permissions")
            if(updated > 0) updateCommands.queue { commands ->

                val modRoles = main.moderatorManager.getRoles()

                if(modRoles.isNotEmpty()) {
                    commands.forEach { command ->
                        if(commandHandler.getCommand(command.name)!!.adminCommand)
                        {
                            command.updatePrivileges(guild, modRoles.map { CommandPrivilege.enableRole(it) }).queue()
                        }
                    }
                }
            } else {
                val modRoles = main.moderatorManager.getRoles()

                if(modRoles.isNotEmpty()) {
                    retrievedCommands.forEach { command ->
                        if(commandHandler.getCommand(command.name)!!.adminCommand)
                        {
                            command.updatePrivileges(guild, modRoles.map { CommandPrivilege.enableRole(it) }).queue()
                        }
                    }
                }
            }
        }
        logger.info("Done registering slash commands for \"${guild.name}\" : ${guild.id}")
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        commandHandler.runCommand(event)
    }

    override fun onButtonClick(event: ButtonClickEvent) {
        if(event.isFromGuild)
            commandHandler.getEvents()[event.interaction.messageId]?.let { it(event) }
    }

    override fun onSlashCommand(event: SlashCommandEvent) {
        commandHandler.runCommand(event)
    }
}