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

    private val disabledCommands = if(config.isList("disabledCommands")) config.getStringList("disabledCommands") else null
    private val customCommands = if(config.isConfigurationSection("customCommands")) config.getConfigurationSection("customCommands").getKeys(false) else null
    private val customCommandAliases = HashMap<String, String>()
    private val commandAliases = HashMap<String, String>()

    private val componentInteractionEvents = HashMap<String, (msg: ComponentInteractionEvent) -> Unit>()
    private val messageDeleteEvents = HashMap<String, (msg: MessageDeleteEvent) -> Unit>()

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

    fun getEvents(): HashMap<String, (msg: ComponentInteractionEvent) -> Unit> {
        return componentInteractionEvents
    }

    fun getMessageDeleteEvents(): HashMap<String, (MessageDeleteEvent) -> Unit> {
        return messageDeleteEvents
    }

    private fun loadCommand(command: Command)
    {
        commands[command.name] = command
    }

    fun loadCommands()
    {
        loadCommand(Pay(main))
        loadCommand(Help(main))
        loadCommand(AddMoney(main))
        loadCommand(RemoveMoney(main))
        loadCommand(Balance(main))
        loadCommand(Leaderboard(main))
        loadCommand(Coinflip(main))
        loadCommand(Blackjack(main))
        loadCommand(RockPaperScissors(main))
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
                val eventData = CommandEventData(
                    main,
                    event,
                    cmd,
                    prefix,
                    args
                )
                customCommand(eventData)
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

        val eventData = CommandEventData(
            main,
            event,
            command,
            prefix,
            args
        )

        if(cmd.adminCommand && !main.moderatorManager.isModerator(eventData.member!!))
            return cmd.noPermission(eventData)


        cmd.run(eventData)
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
                val eventData = CommandEventData(
                    main,
                    event,
                    cmd,
                    prefix,
                    args
                )
                customCommand(eventData)
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

        val eventData = CommandEventData(
            main,
            event,
            command,
            prefix,
            args
        )

        if(cmd.adminCommand && !main.moderatorManager.isModerator(eventData.member!!))
            return cmd.noPermission(eventData)


        cmd.run(eventData)
    }

    fun runCommand(event: SlashCommandEvent)
    {
        if(disabledCommands != null)
            if(disabledCommands.contains(event.name)) return

        val cmd = commands[event.name] ?: return

        val eventData = CommandEventData(
            main,
            event,
            cmd.name,
            "/"
        )

        if(cmd.adminCommand && !main.moderatorManager.isModerator(eventData.member!!))
            return cmd.noPermission(eventData)

        cmd.run(eventData)
    }

    fun runCommand(event: net.dv8tion.jda.api.events.interaction.SlashCommandEvent)
    {
        if(disabledCommands != null)
            if(disabledCommands.contains(event.name)) return

        val cmd = commands[event.name] ?: return

        val eventData = CommandEventData(
            main,
            event,
            cmd.name,
            "/"
        )

        if(cmd.adminCommand && !main.moderatorManager.isModerator(eventData.member!!))
            return cmd.noPermission(eventData)

        cmd.run(eventData)
    }

    private fun customCommand(event: CommandEventData)
    {
        val path = "customCommands.${event.commandName}"

        if(config.isBoolean("$path.adminCommand"))
            if(config.getBoolean("$path.adminCommand"))
                if(!main.moderatorManager.isModerator(event.member!!)) {
                    event.sendYMLEmbed("noPermissionMessage", {
                        val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), event.commandName, "Custom command", "unknown")

                        if(event.member == null)
                            setDiscordPlaceholders(event.author, form)
                        else setDiscordPlaceholders(event.member!!, form)
                    }).queue()
                    return
                }

        if(config.isBoolean("$path.requiresInput"))
            if(config.getBoolean("$path.requiresInput"))
                if(event.args!!.isEmpty()) {
                    event.sendYMLEmbed("failMessage", {
                        val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), event.commandName, "Custom command", "")
                            .replace("{message}", "This command requires any kind of input!")

                        if(event.member == null)
                            setDiscordPlaceholders(event.author, form)
                        else
                            setDiscordPlaceholders(event.member!!, form)
                    }).queue()
                    return
                }

        val embedExists = config.isConfigurationSection("$path.embed")

        val content = if(embedExists) null else getMultilineableString(config, "$path.content")
            ?.replace("{messageContent}", event.message!!.content)
            ?.replace("{messageContentWithoutCommand}", event.message!!.content.substring(event.prefix.length+event.commandName.length+1))

        if(content == null && !embedExists) {
            event.sendYMLEmbed("failMessage", {
                val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), event.commandName, "Custom command", "")
                    .replace("{message}", "Invalid yaml configuration. Embed or Content must be present!")

                if(event.member == null)
                    setDiscordPlaceholders(event.author, form)
                else
                    setDiscordPlaceholders(event.member!!, form)
            }).queue()
            return
        }

        var player: Player? = null
        var inputs: List<String>? = null

        if(config.isList("$path.inputs")) {
            inputs = config.getStringList("$path.inputs").filterNotNull()


            if(event.args!!.isEmpty() && inputs.isNotEmpty())
            {
                event.sendYMLEmbed("failMessage", {
                    val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), event.commandName, "Custom command", "")
                        .replace("{message}", "No arguments provided!\nUsage: ${event.prefix}${event.commandName} ${inputs.joinToString(" ")}")

                    if(event.member == null)
                        setDiscordPlaceholders(event.author, form)
                    else
                        setDiscordPlaceholders(event.member!!, form)
                }).queue()
                return
            }

            inputs.forEachIndexed { index, input ->
                when(input) {
                    "OnlinePlayer" -> {
                        if(event.args.size <= index) {
                            event.sendYMLEmbed("failMessage", {
                                val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), event.commandName, "Custom command", "")
                                    .replace("{message}", "No ${index + 1}. parameter provided\nUsage: ${event.prefix}${event.commandName} ${inputs.joinToString(" ")}")

                                if(event.member == null)
                                    setDiscordPlaceholders(event.author, form)
                                else
                                    setDiscordPlaceholders(event.member!!, form)
                            }).queue()
                            return
                        }

                        player = server.getPlayer(event.args[index])

                        if(player == null) {
                            event.sendYMLEmbed("failMessage", {
                                val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), event.commandName, "Custom command", "")
                                    .replace("{message}", "Player not found!\nUsage: ${event.prefix}${event.commandName} ${inputs.joinToString(" ")}")

                                if(event.member == null)
                                    setDiscordPlaceholders(event.author, form)
                                else
                                    setDiscordPlaceholders(event.member!!, form)
                            }).queue()
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

            event.sendYMLEmbed("$path.embed", {
                var form = setCommandPlaceholders(it, event.prefix, event.commandName, inputs?.joinToString(" ") ?: "", "")

                if(player == null)
                    form = setDiscordPlaceholders(event.member!!, form)
                else
                    form = setPlaceholdersForDiscordMessage(event.member!!, player!!, form)

                form
                    .replace("{messageContent}", event.message!!.content)
                    .replace("{messageContentWithoutCommand}", event.message!!.content.substring(event.prefix.length+event.commandName.length+1))

                if(player != null) {
                    form
                        .replace("{username}", player!!.name)
                        .replace("{joinDate}", sdf.format(Date(player!!.firstPlayed)))
                        .replace("{PlayerStatus}", "Online")
                        .replace("{playerStatus}", "online")
                } else form
            }).queue()
        } else event.sendMessage(content).queue()

    }
}