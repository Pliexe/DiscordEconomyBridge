package me.pliexe.discordeconomybridge.discord

import me.pliexe.discordeconomybridge.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ContextException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege
import org.bukkit.Bukkit
import org.bukkit.Server

class Listener(val main: DiscordEconomyBridge, server: Server) : ListenerAdapter() {
    private val logger = server.logger

    fun init() {
        logger.info("[Bot] Loading Commands!")
        main.commandHandler.loadCommands()

        logger.info("[Bot] Loading command Aliases!")
        main.commandHandler.loadAliases()

        if(main.defaultConfig.contains("slashCommandServers")) {
            try {
                main.defaultConfig.getStringList("slashCommandServers").forEach { guildID ->
                    val guild = main.getJda()!!.getGuildById(guildID)
                    if(guild != null) registerSlashCommands(guild)
                }
            } catch (e: ClassCastException) {
                main.logger.severe("Field \"slashCommandServers\" is of an invalid type, it must be an list of strings. The plugin will continue and ignore this configuration.")
            }
        }
    }

    private fun registerSlashCommands(guild: Guild) {
        try {
            logger.info("Registering slash commands for the guild \"${guild.name}\" : ${guild.id}")
            guild.retrieveCommands().queue { retrievedCommands ->
                var updated = 0
                val updateCommands = guild.updateCommands()
                main.commandHandler.getCommands().forEach { (key, value) ->
                    if(!main.getClearCmds() && !retrievedCommands.any {it.name == key}) {
                        updateCommands.addCommands(value.getSlashCommandDataNative())
                        updated++
                    }
                }
                logger.info("Registering command permissions")
                if(updated > 0) updateCommands.queue { commands ->

                    val modRoles = main.moderatorManager.getRoles()

                    if(modRoles.isNotEmpty()) {
                        commands.forEach { command ->
                            if(main.commandHandler.getCommand(command.name)!!.adminCommand)
                            {
                                command.updatePrivileges(guild, modRoles.map { CommandPrivilege.enableRole(it) }).queue()
                            }
                        }
                    }
                } else {
                    val modRoles = main.moderatorManager.getRoles()

                    if(modRoles.isNotEmpty()) {
                        retrievedCommands.forEach { command ->
                            if(main.commandHandler.getCommand(command.name)?.adminCommand == true)
                            {
                                command.updatePrivileges(guild, modRoles.map { CommandPrivilege.enableRole(it) }).queue()
                            }
                        }
                    }
                }
            }
            logger.info("Done registering slash commands for \"${guild.name}\" : ${guild.id}")
        } catch (e: ContextException) {
            main.logger.severe("Did you invite your bot with applications.commands scope?")
            e.printStackTrace()
        }
    }

    override fun onReady(event: ReadyEvent) {
        init()
        logger.info("[Discord Economy Bridge Bot] Bot online!")
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        main.commandHandler.runCommand(event)
    }

    override fun onGenericComponentInteractionCreate(event: GenericComponentInteractionCreateEvent) {
        main.commandHandler.getEvents()[event.interaction.messageId]?.let { it(ComponentInteractionEvent(main, event)) }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        main.commandHandler.getMessageDeleteEvents()[event.messageId]?.let { it(MessageDeleteEvent(event)) }
    }

    override fun onSlashCommand(event: SlashCommandEvent) {
        main.commandHandler.runCommand(event)
    }

    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        if(event.author.isBot) return

        if(event.message.contentRaw.length == 4) {
            if(main.linkHandler.tryLink(event.message.contentRaw, event.author.id)) {
                val player = Bukkit.getOfflinePlayer(main.linkHandler.getUuid(event.author.id))

                val embed = getYMLEmbed(main, DiscordEmbed(EmbedBuilder(), null), "accountLinkSuccessful", {
                    setPlaceholdersForDiscordMessage(DiscordUser(event.author), UniversalPlayer(player), it)
                })

                val content = if(embed.content != null) setPlaceholdersForDiscordMessage(DiscordUser(event.author), UniversalPlayer(player), embed.content!!).also { embed.content = it } else null

                if(embed.isEmpty)
                {
                    if(embed.content == null || embed.content!!.isEmpty())
                    {
                        val embed2 = DiscordEmbed(EmbedBuilder(), null)
                            .setColor(0x00b806)
                            .setTitle("Account successfully linked to Discord!")
                            .setDescription("Your account has been linked to **${player.name}** (${player.uniqueId}).")

                        event.channel.sendMessageEmbeds(embed2.getNative().build()).queue()
                    }
                    else
                        event.channel.sendMessage(content!!).queue()
                } else
                    event.channel.sendMessageEmbeds(embed.getNative().build()).content(content).queue()
            } else {
                if(main.linkHandler.isLinked(event.author.id)) {
                    val player = Bukkit.getOfflinePlayer(main.linkHandler.getUuid(event.author.id))
                    val embed = getYMLEmbed(main, DiscordEmbed(EmbedBuilder(), null), "accountAlreadyLinked", {
                        setPlaceholdersForDiscordMessage(DiscordUser(event.author), UniversalPlayer(player), it)
                    })

                    val content = if(embed.content != null) setPlaceholdersForDiscordMessage(DiscordUser(event.author), UniversalPlayer(player), embed.content!!).also { embed.content = it } else null

                    if(embed.isEmpty)
                        event.channel.sendMessage(content ?: "Your account is already linked to **${player.name}** (${player.uniqueId}).").queue()
                    else
                        event.channel.sendMessageEmbeds(embed.getNative().build()).content(content).queue()
                } else {
                    val embed = getYMLEmbed(main, DiscordEmbed(EmbedBuilder(), null), "accountLinkUnknown", {
                        setDiscordPlaceholders(DiscordUser(event.author), it)
                    })

                    val content = if(embed.content != null) setDiscordPlaceholders(DiscordUser(event.author), embed.content!!).also { embed.content = it } else null

                    if(embed.isEmpty)
                        event.channel.sendMessage(content ?: "I don't know of such code, try again.").queue()
                    else
                        event.channel.sendMessageEmbeds(embed.getNative().build()).content(content).queue()
                }
            }
        } else {
            val embed = getYMLEmbed(main, DiscordEmbed(EmbedBuilder(), null), "accountLinkInvalid", {
                setDiscordPlaceholders(DiscordUser(event.author), it)
            })

            val content = if(embed.content != null) setDiscordPlaceholders(DiscordUser(event.author), embed.content!!).also { embed.content = it } else null

            if(embed.isEmpty)
                event.channel.sendMessage(content ?: "Are you sure this is a code? A code only contains 4 characters and numbers.").queue()
            else
                event.channel.sendMessageEmbeds(embed.getNative().build()).content(content).queue()
        }
    }

}