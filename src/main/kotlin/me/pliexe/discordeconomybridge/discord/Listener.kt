package me.pliexe.discordeconomybridge.discord

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.UUIDUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Server
import org.bukkit.configuration.file.FileConfiguration
import java.awt.Color
import java.util.*

class Listener(private  val main: DiscordEconomyBridge,private val server: Server, private val config: FileConfiguration) : ListenerAdapter() {
    private val logger = server.logger

    override fun onReady(event: ReadyEvent) {
        logger.info("[Discord Economy Bridge Bot] Bot online!")
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if(event.author.isBot) return
        val prefix = config.getString("PREFIX")
        if(!event.message.contentRaw.startsWith(prefix)) return

        val rawArgs = event.message.contentRaw.split(" +".toRegex())
        val command = rawArgs[0].substring(prefix.length)
        val args = rawArgs.subList(1, rawArgs.size)

        when(command) {
            "help" -> {
                val embed = EmbedBuilder()
                    .setTitle("These are the avaible commands")
                    .addField("${prefix}balance", "See a player's current balance\nUsage: ${prefix}balance <username or uuid>\nAlias: ${prefix}bal", false)
                    .addField("${prefix}addmoney", "Add balance to a player\nUsage: ${prefix}addmoney <amount> <username or uuid>", false)
                    .addField("${prefix}removemoney", "Remove balance from a player\nUsage: ${prefix}removemoney <amount> <username or uuid>\nAlias: ${prefix}remmoney", false)

                event.channel.sendMessage(embed.build()).queue()
            }
            "addmoney" -> {
                if(event.member == null) {
                    val embed = EmbedBuilder()
                        .setDescription("Couldn't get member!")
                        .setColor(Color(0xb72d0e))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }
                if(!main.moderatorManager.isModerator(event.member!!.roles)) {
                    val embed = EmbedBuilder()
                        .setDescription(config.getString("noPermissionMessage"))
                        .setColor(Color(0xb72d0e))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                if(args.isEmpty()) {
                    val embed = EmbedBuilder()
                        .setDescription("No arguemnts given. Usage: ${prefix}addmoney <amount> <user>")
                        .setColor(Color(0xb72d0e))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                val amount = args[0].toDoubleOrNull()

                if(amount == null) {
                    val embed = EmbedBuilder()
                        .setDescription("Amount not given! Usage: ${prefix}addmoney **<amount>** <user>")
                        .setColor(Color(0xb72d0e))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                if(event.message.contentRaw.length < command.length + prefix.length + args[0].length + 2) {
                    val embed = EmbedBuilder()
                        .setDescription("Missing user parameter! Usage: ${prefix}addmoney <amount> **<user>**")
                        .setColor(Color(0xb72d0e))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                val uuidOrUsername = event.message.contentRaw.substring(command.length + prefix.length + args[0].length + 2)

                fun sendMsg(removed: Double, username: String) {
                    val embed = EmbedBuilder()
                        .setDescription("Added ${if(config.getBoolean("CurrencyLeftSide")) "${config.getString("Currency")}$removed" else "$removed${config.getString("Currency")}"} to $username's balance")
                        .setColor(Color(0xe0c308))
                        .build()

                    event.channel.sendMessage(embed).queue()
                }

                val uuid = UUIDUtils.getUUIDFromString(uuidOrUsername)

                if(uuid == null) {
                    val player = server.getPlayer(uuidOrUsername)

                    if(player == null) {
                        val uuid2 = main.usersManager.GetPlayerUUID(uuidOrUsername)

                        if(uuid2 == null) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found or has not played before!")
                                .setColor(Color(0xb72d0e))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        val offlinePlayer = server.getOfflinePlayer(uuid2)

                        if(!offlinePlayer.hasPlayedBefore()) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found!")
                                .setColor(Color(0xb72d0e))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        if(!main.getEconomy().hasAccount(offlinePlayer)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(0xb72d0e))
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
                                .setColor(Color(0xb72d0e))
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
                                .setColor(Color(0xb72d0e))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        if(!main.getEconomy().hasAccount(offlinePlayer)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(0xb72d0e))
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
                                .setColor(Color(0xb72d0e))
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
                        .setColor(Color(0xb72d0e))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }
                if(!main.moderatorManager.isModerator(event.member!!.roles)) {
                    val embed = EmbedBuilder()
                        .setDescription(config.getString("noPermissionMessage"))
                        .setColor(Color(0xb72d0e))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                if(args.isEmpty()) {
                    val embed = EmbedBuilder()
                        .setDescription("No arguemnts given. Usage: ${prefix}removemoney <amount> <user>")
                        .setColor(Color(0xb72d0e))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                val amount = args[0].toDoubleOrNull()

                if(amount == null) {
                    val embed = EmbedBuilder()
                        .setDescription("Amount not given! Usage: ${prefix}removemoney **<amount>** <user>")
                        .setColor(Color(0xb72d0e))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                if(event.message.contentRaw.length < command.length + prefix.length + args[0].length + 2) {
                    val embed = EmbedBuilder()
                        .setDescription("Missing user parameter! Usage: ${prefix}addmoney <amount> **<user>**")
                        .setColor(Color(0xb72d0e))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }

                val uuidOrUsername = event.message.contentRaw.substring(command.length + prefix.length + args[0].length + 2)

                fun sendMsg(removed: Double, username: String) {
                    val embed = EmbedBuilder()
                        .setDescription("Removed ${if(config.getBoolean("CurrencyLeftSide")) "${config.getString("Currency")}$removed" else "$removed${config.getString("Currency")}"} from $username's balance")
                        .setColor(Color(0xe0c308))
                        .build()

                    event.channel.sendMessage(embed).queue()
                }

                val uuid = UUIDUtils.getUUIDFromString(uuidOrUsername)

                if(uuid == null) {
                    val player = server.getPlayer(uuidOrUsername)

                    if(player == null) {
                        val uuid2 = main.usersManager.GetPlayerUUID(uuidOrUsername)

                        if(uuid2 == null) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found or has not played before!")
                                .setColor(Color(0xb72d0e))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        val offlinePlayer = server.getOfflinePlayer(uuid2)

                        if(!offlinePlayer.hasPlayedBefore()) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found!")
                                .setColor(Color(0xb72d0e))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        if(!main.getEconomy().hasAccount(offlinePlayer)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(0xb72d0e))
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
                                .setColor(Color(0xb72d0e))
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
                                .setColor(Color(0xb72d0e))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }

                        if(!main.getEconomy().hasAccount(offlinePlayer)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(0xb72d0e))
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
                                .setColor(Color(0xb72d0e))
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
                        .setColor(Color(0xb72d0e))
                        .build()

                    event.channel.sendMessage(embed).queue()
                    return
                }
                val usernameOrUUID = event.message.contentRaw.substring(command.length + prefix.length + 1)

                fun sendMsg(username: String, balance: Number, online: Boolean) {
                    val embed = EmbedBuilder()
                        .addField("Username", username, true)
                        .addField("Status", if(online) "Online" else "Offline", true)
                        .addField("Balance", if(config.getBoolean("CurrencyLeftSide")) "${config.getString("Currency")}$balance" else "$balance${config.getString("Currency")}", true)
                        .setColor(Color(if(online) 0x26b207 else 0xb72d0e ))
                        .build()

                    event.channel.sendMessage(embed).queue()
                }

                val uuid = UUIDUtils.getUUIDFromString(usernameOrUUID)

                if(uuid != null) {
                    val player = server.getPlayer(uuid)

                    if(player == null) {
                        val offlinePlayer = server.getOfflinePlayer(uuid)
                        if(!offlinePlayer.hasPlayedBefore()) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found!")
                                .setColor(Color(0xb72d0e))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        if(!main.getEconomy().hasAccount(offlinePlayer)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(0xb72d0e))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        sendMsg(offlinePlayer.name, main.getEconomy().getBalance(offlinePlayer), false)
                    } else {
                        if(!main.getEconomy().hasAccount(player)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(0xb72d0e))
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
                                .setColor(Color(0xb72d0e))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        val offlinePlayer = server.getOfflinePlayer(uuid2)
                        if(!offlinePlayer.hasPlayedBefore()) {
                            val embed = EmbedBuilder()
                                .setDescription("Player not found!")
                                .setColor(Color(0xb72d0e))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        if(!main.getEconomy().hasAccount(offlinePlayer)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(0xb72d0e))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        sendMsg(usernameOrUUID, main.getEconomy().getBalance(offlinePlayer), false)
                    } else {
                        if(!main.getEconomy().hasAccount(player)) {
                            val embed = EmbedBuilder()
                                .setDescription("This player does not have an account")
                                .setColor(Color(0xb72d0e))
                                .build()

                            event.channel.sendMessage(embed).queue()
                            return
                        }
                        sendMsg(usernameOrUUID, main.getEconomy().getBalance(player), true)
                    }
                }
            }
        }
    }
}