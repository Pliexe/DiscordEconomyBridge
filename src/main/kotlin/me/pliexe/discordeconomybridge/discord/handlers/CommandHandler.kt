package me.pliexe.discordeconomybridge.discord.handlers

import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.ButtonClickEvent
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import me.pliexe.discordeconomybridge.discord.commands.*
import me.pliexe.discordeconomybridge.getMultilineableString
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CommandHandler(private val main: DiscordEconomyBridge) {
    private val commands: HashMap<String, Command> = HashMap()

    private val config = main.config
    private val server = main.server

    private val failColor = if(config.isInt("onFailEmbedColor")) config.getInt("onFailEmbedColor") else 0xb72d0e
    private val disabledCommands = if(config.isList("disabledCommands")) config.getStringList("disabledCommands") else null
    private val customCommands = if(config.isConfigurationSection("customCommands")) config.getConfigurationSection("customCommands").getKeys(false) else null
    private val customCommandAliases = HashMap<String, String>()
    private val commandAliases = HashMap<String, String>()

    init {
        customCommands?.forEach { commandName ->
            if(config.isList("customCommands.$commandName.aliases")) {
                config.getStringList("customCommands.$commandName.aliases").forEach { alias ->
                    customCommandAliases[alias] = commandName
                }
            }
        }
    }

    fun getCommand(name: String): Command? {
        return commands[name]
    }

    fun getCommands(): HashMap<String, Command> {
        return commands
    }

    fun getEvents(key: String): ((ButtonClickEvent) -> Unit)? {
        return (commands["blackjack"] as Blackjack).buttonEvents[key]
            ?: (commands["coinflip"] as Coinflip).buttonEvents[key]
    }

    fun loadCommand(command: Command)
    {
        commands[command.name] = command
    }

    fun loadCommands()
    {
        loadCommand(Help(main))
        loadCommand(AddMoney(main))
        loadCommand(RemoveMoney(main))
        loadCommand(Balance(main))
        loadCommand(Leaderboard(main))
        loadCommand(Coinflip(main))
        loadCommand(Blackjack(main))
    }

    fun loadAliases()
    {
        if(config.isConfigurationSection("aliases"))
            config.getConfigurationSection("aliases").getKeys(false).forEach { command ->
                if(commands[command] != null)
                {
                    if(config.isList("aliases.$command"))
                        config.getStringList("aliases.$command").forEach { alias ->
                            commandAliases[alias] = command
                        }
                } else
                    server.logger.warning("${ChatColor.YELLOW}Alias for command: $command was not loaded because $command is not a command!")
            }
    }

    fun runCommand(event: GuildMessageReceivedEvent)
    {
        if(event.author.isBot) return
        val prefix = config.getString("PREFIX")
        if(!event.message.contentRaw.startsWith(prefix)) return

        val rawArgs = event.message.contentRaw.split(" +".toRegex())
        val command = rawArgs[0].substring(prefix.length).toLowerCase()

        if(disabledCommands != null)
            if(disabledCommands.contains(command)) return

        val args = rawArgs.subList(1, rawArgs.size)

        if(customCommands != null)
        {
            var cmd: String? = if(customCommands.contains(command)) command else null
            if(cmd == null) {
                cmd = customCommandAliases[command]
            }

            if(cmd != null)
            {
                customCommand(event, cmd, prefix, args)
                return
            }
        }

        var cmd = commands[command]

        if(cmd == null)
        {
            val alias = commandAliases[command]
            if(alias.isNullOrEmpty()) return
            cmd = commands[alias]
            if(cmd == null) return
        }

        if(cmd.adminCommand && !main.moderatorManager.isModerator(event.member!!))
            return cmd.noPermission(event)


        cmd.run(event, command, prefix, args)
    }

    fun runCommand(event: github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent)
    {
        if(event.author.isBot) return
        val prefix = config.getString("PREFIX")
        if(!event.message.contentRaw.startsWith(prefix)) return

        val rawArgs = event.message.contentRaw.split(" +".toRegex())
        val command = rawArgs[0].substring(prefix.length).toLowerCase()

        if(disabledCommands != null)
            if(disabledCommands.contains(command)) return

        val args = rawArgs.subList(1, rawArgs.size)

        if(customCommands != null)
        {
            var cmd: String? = if(customCommands.contains(command)) command else null
            if(cmd == null) {
                cmd = customCommandAliases[command]
            }

            if(cmd != null)
            {
                customCommand(event, cmd, prefix, args)
                return
            }
        }

        var cmd = commands[command]

        if(cmd == null)
        {
            val alias = commandAliases[command]
            if(alias.isNullOrEmpty()) return
            cmd = commands[alias]
            if(cmd == null) return
        }

        if(cmd.adminCommand && !main.moderatorManager.isModerator(event.member!!))
            return cmd.noPermission(event)

        cmd.run(event, command, prefix, args)
    }

    fun runCommand(event: SlashCommandEvent)
    {
        if(disabledCommands != null)
            if(disabledCommands.contains(event.name)) return

        val cmd = commands[event.name] ?: return

        if(cmd.adminCommand && !main.moderatorManager.isModerator(event.member!!))
            return cmd.noPermission(event)

        cmd.run(event)
    }

    private fun customCommand(event: GuildMessageReceivedEvent, cmd: String, prefix: String, args: List<String>)
    {
        val path = "customCommands.$cmd"

        if(config.isBoolean("$path.adminCommand"))
            if(config.getBoolean("$path.adminCommand"))
                if(!main.moderatorManager.isModerator(event.member!!)) {
                    val embed = EmbedBuilder()
                        .setDescription(config.getString("noPermissionMessage"))
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessageEmbeds(embed).queue()
                    return
                }

        if(config.isBoolean("$path.requiresInput"))
            if(config.getBoolean("$path.requiresInput"))
                if(args.isEmpty()) {
                    val embed = EmbedBuilder()
                        .setColor(failColor)
                        .setDescription("This command requires any kind of input!")

                    event.channel.sendMessageEmbeds(embed.build()).queue()
                    return
                }

        val embedExists = config.isConfigurationSection("$path.embed")

        val content = if(embedExists) null else getMultilineableString(config, "$path.content")
            ?.replace("{messageContent}", event.message.contentRaw)
            ?.replace("{messageContentWithoutCommand}", event.message.contentRaw.substring(prefix.length+cmd.length+1))

        if(content == null && !embedExists) {
            event.channel.sendMessage("Invalid yaml configuration. Embed or Content must be present!").queue()
            return
        }

        var player: Player? = null
        var inputs: List<String>? = null

        if(config.isList("$path.inputs")) {
            inputs = config.getStringList("$path.inputs").filterNotNull()


            if(args.isEmpty() && inputs.isNotEmpty())
            {
                val embed = EmbedBuilder()
                    .setColor(failColor)
                    .setDescription("No arguments provided!\nUsage: $prefix$cmd ${inputs.joinToString(" ")}")

                event.channel.sendMessageEmbeds(embed.build()).queue()
                return
            }

            inputs.forEachIndexed { index, input ->
                when(input) {
                    "OnlinePlayer" -> {
                        if(args.size <= index) {
                            val embed = EmbedBuilder()
                                .setColor(failColor)
                                .setDescription("No ${index + 1}. parameter provided\nUsage: $prefix$cmd ${inputs.joinToString(" ")}")

                            event.channel.sendMessageEmbeds(embed.build()).queue()
                            return
                        }

                        player = server.getPlayer(args[index])

                        if(player == null) {
                            val embed = EmbedBuilder()
                                .setColor(failColor)
                                .setDescription("Player not found!\nUsage: $prefix$cmd ${inputs.joinToString(" ")}")

                            event.channel.sendMessageEmbeds(embed.build()).queue()
                            return
                        }
                    }
                }
            }
        }

        val sdf = SimpleDateFormat("dd.MM.yyyy")

        if(player != null) {
            content?.replace("{username}", player!!.name)?.replace("{joinDate}", sdf.format(Date(player!!.firstPlayed)))
                ?.replace("{PlayerStatus}", "Online")?.replace("{playerStatus}", "online")
        }


        if(content == null) {
            val embed = LegacyGetYmlEmbed({
                var form = setCommandPlaceholders(it, prefix, cmd, inputs?.joinToString(" ") ?: "")

                if(player == null)
                    form = setDiscordPlaceholders(event.member!!, form)
                else
                    form = setPlaceholdersForDiscordMessage(event.member!!, player!!, form)

                form
                    .replace("{messageContent}", event.message.contentRaw)
                    .replace("{messageContentWithoutCommand}", event.message.contentRaw.substring(prefix.length+cmd.length+1))

                if(player != null) {
                    form
                        .replace("{username}", player!!.name)
                        .replace("{joinDate}", sdf.format(Date(player!!.firstPlayed)))
                        .replace("{PlayerStatus}", "Online")
                        .replace("{playerStatus}", "online")
                } else form
            }, "$path.embed", main.defaultConfig).build()

            event.channel.sendMessageEmbeds(embed).queue()
        } else event.channel.sendMessage(content).queue()
    }

    private fun customCommand(event: github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent, cmd: String, prefix: String, args: List<String>)
    {
        val path = "customCommands.$cmd"

        if(config.isBoolean("$path.adminCommand"))
            if(config.getBoolean("$path.adminCommand"))
                if(!main.moderatorManager.isModerator(event.member!!)) {
                    val embed = github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder()
                        .setDescription(config.getString("noPermissionMessage"))
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessageEmbeds(embed).queue()
                    return
                }

        if(config.isBoolean("$path.requiresInput"))
            if(config.getBoolean("$path.requiresInput"))
                if(args.isEmpty()) {
                    val embed = github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder()
                        .setColor(failColor)
                        .setDescription("This command requires any kind of input!")

                    event.channel.sendMessageEmbeds(embed.build()).queue()
                    return
                }

        val embedExists = config.isConfigurationSection("$path.embed")

        val content = if(embedExists) null else getMultilineableString(config, "$path.content")
            ?.replace("{messageContent}", event.message.contentRaw)
            ?.replace("{messageContentWithoutCommand}", event.message.contentRaw.substring(prefix.length+cmd.length+1))

        if(content == null && !embedExists) {
            event.channel.sendMessage("Invalid yaml configuration. Embed or Content must be present!").queue()
            return
        }

        var player: Player? = null
        var inputs: List<String>? = null

        if(config.isList("$path.inputs")) {
            inputs = config.getStringList("$path.inputs").filterNotNull()


            if(args.isEmpty() && inputs.isNotEmpty())
            {
                val embed = github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder()
                    .setColor(failColor)
                    .setDescription("No arguments provided!\nUsage: $prefix$cmd ${inputs.joinToString(" ")}")

                event.channel.sendMessageEmbeds(embed.build()).queue()
                return
            }

            inputs.forEachIndexed { index, input ->
                when(input) {
                    "OnlinePlayer" -> {
                        if(args.size <= index) {
                            val embed = github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder()
                                .setColor(failColor)
                                .setDescription("No ${index + 1}. parameter provided\nUsage: $prefix$cmd ${inputs.joinToString(" ")}")

                            event.channel.sendMessageEmbeds(embed.build()).queue()
                            return
                        }

                        player = server.getPlayer(args[index])

                        if(player == null) {
                            val embed = github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder()
                                .setColor(failColor)
                                .setDescription("Player not found!\nUsage: $prefix$cmd ${inputs.joinToString(" ")}")

                            event.channel.sendMessageEmbeds(embed.build()).queue()
                            return
                        }
                    }
                }
            }
        }

        val sdf = SimpleDateFormat("dd.MM.yyyy")

        if(player != null) {
            content?.replace("{username}", player!!.name)?.replace("{joinDate}", sdf.format(Date(player!!.firstPlayed)))
                ?.replace("{PlayerStatus}", "Online")?.replace("{playerStatus}", "online")
        }


        if(content == null) {
            val embed = GetYmlEmbed( {
                var form = setCommandPlaceholders(it, prefix, cmd, inputs?.joinToString(" ") ?: "")

                if(player == null)
                    form = setDiscordPlaceholders(event.member!!, form)
                else
                    form = setPlaceholdersForDiscordMessage(event.member!!, player!!, form)

                form
                    .replace("{messageContent}", event.message.contentRaw)
                    .replace("{messageContentWithoutCommand}", event.message.contentRaw.substring(prefix.length+cmd.length+1))

                if(player != null) {
                    form
                        .replace("{username}", player!!.name)
                        .replace("{joinDate}", sdf.format(Date(player!!.firstPlayed)))
                        .replace("{PlayerStatus}", "Online")
                        .replace("{playerStatus}", "online")
                } else form
            }, "$path.embed", main.defaultConfig).build()

            event.channel.sendMessageEmbeds(embed).queue()
        } else event.channel.sendMessage(content).queue()
    }
}