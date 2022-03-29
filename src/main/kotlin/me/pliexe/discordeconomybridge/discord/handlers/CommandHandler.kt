package me.pliexe.discordeconomybridge.discord.handlers

import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import me.pliexe.discordeconomybridge.discord.commands.*
import me.pliexe.discordeconomybridge.isConfigSection
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.bukkit.ChatColor
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.HashMap

class CommandHandler(private val main: DiscordEconomyBridge) {
    private val commands: HashMap<String, Command> = HashMap()

    private val config = main.config
    private val server = main.server

    private val disabledCommands = if(config.isList("disabledCommands")) config.getStringList("disabledCommands") else null
    private val commandAliases = HashMap<String, String>()

    private val componentInteractionEvents = HashMap<String, (msg: ComponentInteractionEvent) -> Unit>()
    private val messageDeleteEvents = HashMap<String, (msg: MessageDeleteEvent) -> Unit>()

    private val playingGame = mutableSetOf<String>()
    private val bets = HashMap<UUID, Double>()

    fun add(bet: Double, playerUq: UUID) {
        if(bets.containsKey(playerUq)) bets[playerUq] = bets[playerUq]!!.plus(bet)
        else bets[playerUq] = bet
    }

    fun getBets(): HashMap<UUID, Double> {
        return bets
    }

//    fun removeBets(vararg bets: UUID) {
//        for(bet in bets)
//            this.bets.remove(bet)
//    }

    fun getPlaying(): MutableSet<String> { return playingGame }

    fun isPlaying(userId: String): Boolean {
        return playingGame.contains(userId)
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

        main.customCommandsConfig.get("commands").also {
            if(isConfigSection(it)) {
                main.customCommandsConfig.singleLayerKeySet("commands").forEach { name ->

                    val inputSec = main.customCommandsConfig.getSection("commands.$name.inputs")


                    val usage = StringBuilder()

                    fun addInputToUsage(nm: String, default: String) {
                        val req = inputSec.getOrDefault("$nm.required", true)
                        usage.append(if(req) " <" else " [")
                        usage.append(inputSec.getOrDefault("$nm.name", default))
                        usage.append(if(req) ">" else "]")
                    }

                    inputSec.singleLayerKeySet().forEach { nm ->
                        when(inputSec.getString("$nm.type")) {
                            "MinecraftPlayer" -> addInputToUsage(nm, "minecraftPlayer")
                            "Double", "Number" -> addInputToUsage(nm, "number")
                            "WholeNumber", "Integer", "Int" -> addInputToUsage(nm, "whole_number")
                            "DiscordUser" -> addInputToUsage(nm, "discord_user")
                            "DiscordMember" -> addInputToUsage(nm, "discord_member")
                        }
                    }

                    val cmd = CustomCommand(main, name, main.customCommandsConfig.getString("commands.$name.description") ?: "No description", usage.toString().trim())

                    cmd.loadArguments(main.customCommandsConfig.getSection("commands.$name.inputs"))

                    loadCommand(cmd)
                }
            }
        }
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

    private fun runCommand(event: CommandEventData, cmd: Command) {
        if(cmd.isGame && playingGame.contains(event.author.id))
            return cmd.fail(event, "You are already playing an game!")

        if(cmd.adminCommand && !main.moderatorManager.isModerator(event.member!!))
            return cmd.noPermission(event)

        try {
            cmd.run(event)
        } catch (e: Exception) {
            if(cmd.isGame && playingGame.contains(event.author.id))
                event.resetCooldowns()

            if(cmd.isGame)
                event.restoreBets()

            e.printStackTrace()
            event.sendYMLEmbed("errorMessage", {
                setDiscordPlaceholders(
                    event.member!!,
                    setCommandPlaceholders(it, event.prefix, event.commandName, cmd.description, cmd.usage)
                )
            }).queue()
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

        runCommand(eventData, cmd)
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

        runCommand(eventData, cmd)
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

        runCommand(eventData, cmd)
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

        runCommand(eventData, cmd)
    }
}