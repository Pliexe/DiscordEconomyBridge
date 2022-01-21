package me.pliexe.discordeconomybridge.discord

import me.pliexe.discordeconomybridge.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Server
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.awt.Color
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Listener(private  val main: DiscordEconomyBridge,private val server: Server, private val config: FileConfiguration) : ListenerAdapter() {
    private val logger = server.logger

    private val failColor = if(config.isInt("onFailEmbedColor")) config.getInt("onFailEmbedColor") else 0xb72d0e

    private val disabledCommands = if(config.isList("disabledCommands")) config.getStringList("disabledCommands") else null
    private val customCommands = if(config.isConfigurationSection("customCommands")) config.getConfigurationSection("customCommands").getKeys(false) else null
    private val customCommandAliases = HashMap<String, String>()

    init {
        customCommands?.forEach { commandName ->
            if(config.isList("customCommands.$commandName.aliases")) {
                config.getStringList("customCommands.$commandName.aliases").forEach { alias ->
                    customCommandAliases[alias] = commandName
                }
            }
        }
    }

    override fun onReady(event: ReadyEvent) {
        logger.info("[Discord Economy Bridge Bot] Bot online!")
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if(event.author.isBot) return
        val prefix = config.getString("PREFIX")
        if(!event.message.contentRaw.startsWith(prefix)) return

        val rawArgs = event.message.contentRaw.split(" +".toRegex())
        val command = rawArgs[0].substring(prefix.length)

        if(disabledCommands != null)
            if(disabledCommands.contains(command)) return

        val args = rawArgs.subList(1, rawArgs.size)

        if(customCommands != null)
        {
            var cmd: String? = if(customCommands.contains(command)) command else null
            if(cmd == null) {
                cmd = customCommandAliases[command]
            }

            if(cmd != null) {
                val path = "customCommands.$cmd"

                if(config.isBoolean("$path.adminCommand"))
                    if(config.getBoolean("$path.adminCommand"))
                        if(!main.moderatorManager.isModerator(event.member!!)) {
                            val embed = EmbedBuilder()
                                .setDescription(config.getString("noPermissionMessage"))
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                if(config.isBoolean("$path.requiresInput"))
                    if(config.getBoolean("$path.requiresInput"))
                        if(args.isEmpty()) {
                            val embed = EmbedBuilder()
                                .setColor(failColor)
                                .setDescription("This command requires any kind of input!")

                            event.channel.sendMessage(embed.build()).queue()
                            return
                        }

                val embedExists = config.isConfigurationSection("$path.embed")

                val content = if(embedExists) null else getMultilineableString(config, "$path.content")
                    ?.replace("{messageContent}", event.message.contentRaw)
                    ?.replace("{messageContentWithoutCommand}", event.message.contentRaw.substring(prefix.length+command.length+1))

                if(content == null && !embedExists) {
                    event.channel.sendMessage("Invalid yaml configuration. Embed or Content must be present!").queue()
                    return
                }

                var player: Player? = null

                if(config.isList("$path.inputs")) {
                    val inputs = config.getStringList("$path.inputs").filterNotNull()


                    if(args.isEmpty() && inputs.isNotEmpty())
                    {
                        val embed = EmbedBuilder()
                            .setColor(failColor)
                            .setDescription("No arguments provided!\nUsage: $prefix$cmd ${inputs.joinToString(" ")}")

                        event.channel.sendMessage(embed.build()).queue()
                        return
                    }

                    inputs.forEachIndexed { index, input ->
                        when(input) {
                            "OnlinePlayer" -> {
                                if(args.size <= index) {
                                    val embed = EmbedBuilder()
                                        .setColor(failColor)
                                        .setDescription("No ${index + 1}. parameter provided\nUsage: $prefix$cmd ${inputs.joinToString(" ")}")

                                    event.channel.sendMessage(embed.build()).queue()
                                    return
                                }

                                player = server.getPlayer(args[index])

                                if(player == null) {
                                    val embed = EmbedBuilder()
                                        .setColor(failColor)
                                        .setDescription("Player not found!\nUsage: $prefix$cmd ${inputs.joinToString(" ")}")

                                    event.channel.sendMessage(embed.build()).queue()
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
                    val embed = getEmbedFromYml(config, "$path.embed", { text ->
                        text
                            .replace("{messageContent}", event.message.contentRaw)
                            .replace("{messageContentWithoutCommand}", event.message.contentRaw.substring(prefix.length+command.length+1))

                        if(player != null) {
                            text
                                .replace("{username}", player!!.name)
                                .replace("{joinDate}", sdf.format(Date(player!!.firstPlayed)))
                                .replace("{PlayerStatus}", "Online")
                                .replace("{playerStatus}", "online")
                        } else text
                    }).build()

                    event.channel.sendMessage(embed).queue()
                } else event.channel.sendMessage(content).queue()

                return
            }
        }

        when(command) {
            "help" -> {
                val embed = getEmbedFromYml(config, "helpCommandEmbed",{ text ->
                    text.replace("{prefix}", prefix)
                })

                event.channel.sendMessage(embed.build()).queue()
            }
            "addmoney" -> {
                if(event.member == null) {
                    val embed = EmbedBuilder()
                        .setDescription("Couldn't get member!")
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }
                if(!main.moderatorManager.isModerator(event.member!!)) {
                    val embed = EmbedBuilder()
                        .setDescription(config.getString("noPermissionMessage"))
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                if(args.isEmpty()) {
                    val embed = EmbedBuilder()
                        .setDescription("No arguemnts given. Usage: ${prefix}addmoney <amount> <user>")
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                val amount = args[0].toDoubleOrNull()

                if(amount == null) {
                    val embed = EmbedBuilder()
                        .setDescription("Amount not given! Usage: ${prefix}addmoney **<amount>** <user>")
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                if(event.message.contentRaw.length < command.length + prefix.length + args[0].length + 2) {
                    val embed = EmbedBuilder()
                        .setDescription("Missing user parameter! Usage: ${prefix}addmoney <amount> **<user>**")
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                val uuidOrUsername = event.message.contentRaw.substring(command.length + prefix.length + args[0].length + 2)

                fun sendMsg(removed: Double, username: String) {
                    val formatter = DecimalFormat("#,###.##")

                    val embed = getEmbedFromYml(config, "addmoneyCommandEmbed",{
                        it
                            .replace("{moneyAmount}", formatMoney(removed, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
                            .replace("{username}", username)
                    })

                    event.channel.sendMessage(embed.build()).queue()
                }

                val uuid = UUIDUtils.getUUIDFromString(uuidOrUsername)

                if(uuid == null) {
                    val player = server.getPlayer(uuidOrUsername)

                    if(player == null) {
                        val uuid2 = main.usersManager.GetPlayerUUID(uuidOrUsername)

                        if(uuid2 == null) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found or has not played before!")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        val offlinePlayer = server.getOfflinePlayer(uuid2)

                        if(!offlinePlayer.hasPlayedBefore()) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found!")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        if(!main.getEconomy().hasAccount(offlinePlayer)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        main.getEconomy().depositPlayer(offlinePlayer, amount)

                        sendMsg(amount, offlinePlayer.name)
                    } else {
                        if(!main.getEconomy().hasAccount(player)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        main.getEconomy().depositPlayer(player, amount)

                        sendMsg(amount, player.name)
                    }
                } else {
                    val player = server.getPlayer(uuid)

                    if(player == null) {
                        val offlinePlayer = server.getOfflinePlayer(uuid)

                        if(!offlinePlayer.hasPlayedBefore()) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found!")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        if(!main.getEconomy().hasAccount(offlinePlayer)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        main.getEconomy().depositPlayer(offlinePlayer, amount)

                        sendMsg(amount, offlinePlayer.name)
                    } else {
                        if(!main.getEconomy().hasAccount(player)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        main.getEconomy().depositPlayer(player, amount)

                        sendMsg(amount, player.name)
                    }
                }
            }
            "removemoney", "remmoney" -> {
                if(event.member == null) {
                    val embed = EmbedBuilder()
                        .setDescription("Couldn't get member!")
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }
                if(!main.moderatorManager.isModerator(event.member!!)) {
                    val embed = EmbedBuilder()
                        .setDescription(config.getString("noPermissionMessage"))
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                if(args.isEmpty()) {
                    val embed = EmbedBuilder()
                        .setDescription("No arguemnts given. Usage: ${prefix}removemoney <amount> <user>")
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                val amount = args[0].toDoubleOrNull()

                if(amount == null) {
                    val embed = EmbedBuilder()
                        .setDescription("Amount not given! Usage: ${prefix}removemoney **<amount>** <user>")
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                if(event.message.contentRaw.length < command.length + prefix.length + args[0].length + 2) {
                    val embed = EmbedBuilder()
                        .setDescription("Missing user parameter! Usage: ${prefix}addmoney <amount> **<user>**")
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                val uuidOrUsername = event.message.contentRaw.substring(command.length + prefix.length + args[0].length + 2)

                fun sendMsg(removed: Double, username: String) {
                    val formatter = DecimalFormat("#,###.##")

                    val embed = getEmbedFromYml(config, "removemoneyCommandEmbed", {
                        it
                            .replace("{moneyAmount}", formatMoney(removed, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
                            .replace("{username}", username)
                    })
                            
                    event.channel.sendMessage(embed.build()).queue()
                }

                val uuid = UUIDUtils.getUUIDFromString(uuidOrUsername)

                if(uuid == null) {
                    val player = server.getPlayer(uuidOrUsername)

                    if(player == null) {
                        val uuid2 = main.usersManager.GetPlayerUUID(uuidOrUsername)

                        if(uuid2 == null) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found or has not played before!")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        val offlinePlayer = server.getOfflinePlayer(uuid2)

                        if(!offlinePlayer.hasPlayedBefore()) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found!")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        if(!main.getEconomy().hasAccount(offlinePlayer)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        main.getEconomy().withdrawPlayer(offlinePlayer, amount)

                        sendMsg(amount, offlinePlayer.name)
                    } else {
                        if(!main.getEconomy().hasAccount(player)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        main.getEconomy().withdrawPlayer(player, amount)

                        sendMsg(amount, player.name)
                    }
                } else {
                    val player = server.getPlayer(uuid)

                    if(player == null) {
                        val offlinePlayer = server.getOfflinePlayer(uuid)

                        if(!offlinePlayer.hasPlayedBefore()) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found!")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        if(!main.getEconomy().hasAccount(offlinePlayer)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        main.getEconomy().withdrawPlayer(offlinePlayer, amount)

                        sendMsg(amount, offlinePlayer.name)
                    } else {
                        if(!main.getEconomy().hasAccount(player)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        main.getEconomy().withdrawPlayer(player, amount)

                        sendMsg(amount, player.name)
                    }
                }
            }
            "balance", "bal" -> {
                if(args.isEmpty()) {
                    val embed = EmbedBuilder()
                        .setDescription("No username or uuid given to search for!")
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }
                val usernameOrUUID = event.message.contentRaw.substring(command.length + prefix.length + 1)

                fun sendMsg(username: String, balance: Double, online: Boolean) {
                    val formatter = DecimalFormat("#,###.##")

                    val embed = getEmbedFromYml(config, "balanceCommandEmbed", { text ->
                        text
                            .replace("{username}", username)
                            .replace("{PlayerStatus}", if(online) "Online" else "Offline")
                            .replace("{playerStatus}", if(online) "online" else "offline")
                            .replace("{moneyAmount}", formatMoney(balance, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
                    }, { text ->
                        if(text.startsWith("ifOnline")) {
                            if(online)
                                text.substring(9, text.lastIndexOf(":"))
                            else text.substring(text.lastIndexOf(":") + 1)
                        } else throw Error("Invalid custom script")
                    })

                    event.channel.sendMessage(embed.build()).queue()
                }

                val uuid = UUIDUtils.getUUIDFromString(usernameOrUUID)

                if(uuid != null) {
                    val player = server.getPlayer(uuid)

                    if(player == null) {
                        val offlinePlayer = server.getOfflinePlayer(uuid)
                        if(!offlinePlayer.hasPlayedBefore()) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found!")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        if(!main.getEconomy().hasAccount(offlinePlayer)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        sendMsg(offlinePlayer.name, main.getEconomy().getBalance(offlinePlayer), false)
                    } else {
                        if(!main.getEconomy().hasAccount(player)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        sendMsg(player.name, main.getEconomy().getBalance(player), true)
                    }
                } else {
                    val player = server.getPlayer(usernameOrUUID)

                    if(player == null) {
                        val uuid2 = main.usersManager.GetPlayerUUID(usernameOrUUID)
                        if(uuid2 == null) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found or has not played before!")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        val offlinePlayer = server.getOfflinePlayer(uuid2)
                        if(!offlinePlayer.hasPlayedBefore()) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found!")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        if(!main.getEconomy().hasAccount(offlinePlayer)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        sendMsg(usernameOrUUID, main.getEconomy().getBalance(offlinePlayer), false)
                    } else {
                        if(!main.getEconomy().hasAccount(player)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(failColor))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        sendMsg(usernameOrUUID, main.getEconomy().getBalance(player), true)
                    }
                }
            }
            "leaderboard", "top", "lb"-> {
                if(main.server.offlinePlayers.isEmpty()) {
                    val embed = EmbedBuilder()
                        .setDescription("There players to show on leaderboard!")
                        .setColor(Color(failColor))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                val formatter = DecimalFormat("#,###.##")
                val currency = config.getString("Currency")
                val leftSided = config.getBoolean("CurrencyLeftSide")

                val players = main.server.offlinePlayers.filter { main.getEconomy().hasAccount(it) }.map { User(it.name, main.getEconomy().getBalance(it)) }.sortedByDescending { it.money }



//                getEmbedFromYml(config, "leaderboardCommandEmbed", text -> "hi", )

                val embedNameTemplate = if(config.isString("leaderboardCommandEmbed.fieldRepeatName")) config.getString("leaderboardCommandEmbed.fieldRepeatName") else null
                val embedValueTemplate = if(config.isString("leaderboardCommandEmbed.fieldRepeatValue")) config.getString("leaderboardCommandEmbed.fieldRepeatValue") else null
                val inline = if(config.isBoolean("leaderboardCommandEmbed.fieldRepeatInline")) config.getBoolean("leaderboardCommandEmbed.fieldRepeatInline") else false

                val embedCanBeSet = embedNameTemplate != null && embedValueTemplate != null

                val descCanBeSet = config.isString("leaderboardCommandEmbed.descriptionRepeat")

                if(!embedCanBeSet && !descCanBeSet) {
                    event.channel.sendMessage("Invalid yaml configuration. Either description or embed field must be set!")
                    return
                }

                var text: MutableList<String>? = null

                var leaderboardLimit = if (config.isInt("leaderboardSlots")) config.getInt("leaderboardSlots") else 10

                if(leaderboardLimit > players.size) leaderboardLimit = players.size

                val embed = getEmbedFromYml(config, "leaderboardCommandEmbed", { text2 -> text2 }, null, true)

                for(index in 0 until leaderboardLimit)
                {
                    val formattedMoney = formatMoney(players[index].money, currency, leftSided, formatter)

                    if(descCanBeSet)
                    {
                        if(text == null)
                            text = ArrayList()

                        text.add(config.getString("leaderboardCommandEmbed.descriptionRepeat")
                            .replace("{username}", players[index].username)
                            .replace("{balance}", formattedMoney)
                            .replace("{index}", (index+1).toString()))
                    }

                    if(embedCanBeSet) {
                        embed.addField(
                            embedNameTemplate!!
                                .replace("{username}", players[index].username)
                                .replace("{balance}", formattedMoney)
                                .replace("{index}", (index+1).toString()),
                            embedValueTemplate!!
                                .replace("{username}", players[index].username)
                                .replace("{balance}", formattedMoney)
                                .replace("{index}", (index+1).toString()),
                            inline
                        )
                    }
                }

                if(!text.isNullOrEmpty()) embed.setDescription(text.joinToString("\n"))

                event.channel.sendMessage(embed.build()).queue()
            }
        }
    }
}

class User (
    val username: String,
    val money: Double
)