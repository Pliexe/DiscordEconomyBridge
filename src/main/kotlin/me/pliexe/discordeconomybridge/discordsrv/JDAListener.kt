package me.pliexe.discordeconomybridge.discordsrv

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild
import github.scarsz.discordsrv.dependencies.jda.api.events.ReadyEvent
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.GenericComponentInteractionCreateEvent
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent
import github.scarsz.discordsrv.dependencies.jda.api.events.message.MessageDeleteEvent
import github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.privileges.CommandPrivilege
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.ComponentInteractionEvent
import net.dv8tion.jda.api.exceptions.ContextException
import java.util.Timer
import java.util.TimerTask

class JDAListener(private val main: DiscordEconomyBridge): ListenerAdapter() {
    private val logger = main.logger

    init {
        logger.info("[Bot] Loading Commands!")
        main.commandHandler.loadCommands()

        logger.info("[Bot] Loading command Aliases!")
        main.commandHandler.loadAliases()

        logger.info("[Bot] Loading command cooldowns!")
        main.commandHandler.loadCooldowns()

        if(main.defaultConfig.contains("slashCommandServers")) {
            try {
                main.defaultConfig.getStringList("slashCommandServers").forEach { guildID ->
                    val guild = DiscordSRV.getPlugin().jda.getGuildById(guildID)
                    if(guild != null) {
                        main.logger.info("Trying to registering slash commands in 1s")
                        Timer().schedule(object : TimerTask() {
                            override fun run() {
                                registerSlashCommands(guild)
                            }
                        }, 1000)
                    }
                }
            } catch (e: ClassCastException) {
                main.logger.severe("Field \"slashCommandServers\" is of an invalid type, it must be an list of strings. The plugin will continue and ignore this configuration.")
            }
        }
    }

    private fun registerSlashCommands(guild: Guild) {
        try {
            logger.info("Registering slash commands for the guild \"${guild.name}\" : ${guild.id}")

            val updateCommands = guild.updateCommands()

            main.commandHandler.getCommands().forEach { (_, value) ->
                updateCommands.addCommands(value.getSlashCommandDataSRV())
            }
            updateCommands.queue { commands ->
                logger.info("Registered slash commands: [${commands.joinToString(", ")}]")
            }


            logger.info("Done registering slash commands for \"${guild.name}\" : ${guild.id}")
        } catch (e: ContextException){
            main.logger.severe("Did you invite your bot with applications.commands scope?")
            e.printStackTrace()
        }
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        main.commandHandler.runCommand(event)
    }

    override fun onGenericComponentInteractionCreate(event: GenericComponentInteractionCreateEvent) {
        main.commandHandler.getEvents()[event.interaction.messageId]?.let { it(ComponentInteractionEvent(main, event)) }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        main.commandHandler.getMessageDeleteEvents()[event.messageId]?.let { it(me.pliexe.discordeconomybridge.discord.MessageDeleteEvent(event)) }
    }

    override fun onSlashCommand(event: SlashCommandEvent) {
        main.commandHandler.runCommand(event)
    }

    override fun onReady(event: ReadyEvent) {
        main.logger.info("DISCORD SRV READY EVENT")
    }
}