package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.UUIDUtils
import me.pliexe.discordeconomybridge.discord.Command
import me.pliexe.discordeconomybridge.discord.GetYmlEmbed
import me.pliexe.discordeconomybridge.discord.setPlaceholdersForDiscordMessage
import me.pliexe.discordeconomybridge.formatMoney
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.text.DecimalFormat

class Balance(main: DiscordEconomyBridge): Command(main) {
    override val usage: String
        get() = "username/uuid"

    override fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        if(args.isEmpty())
            return fail(event, "No username or uuid given to search for!")

        val usernameOrUUID = event.message.contentRaw.substring(commandName.length + prefix.length + 1)

        fun getEmbed(player: Player): MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return GetYmlEmbed(event.channel, {
                setPlaceholdersForDiscordMessage(event.member!!, player, it)
                    .replace("%custom_vault_eco_balance%", formatMoney(main.getEconomy().getBalance(player), config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "balanceCommandEmbed", main.discordMessagesConfig, {
                it == "ifOnline"
            }).build()
        }

        fun getEmbed(player: OfflinePlayer): MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return GetYmlEmbed(event.channel, {
                setPlaceholdersForDiscordMessage(event.member!!, player, it)
                    .replace("%custom_vault_eco_balance%", formatMoney(main.getEconomy().getBalance(player), config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "balanceCommandEmbed", main.discordMessagesConfig, {
                it == "ifOnline"
            }).build()
        }

        fun sendMsg(embed: MessageEmbed) {
            event.channel.sendMessageEmbeds(embed).queue()
        }

        val uuid = UUIDUtils.getUUIDFromString(usernameOrUUID)

        if(uuid != null) {
            val player = server.getPlayer(uuid)

            if(player == null) {
                val offlinePlayer = server.getOfflinePlayer(uuid)
                if(!offlinePlayer.hasPlayedBefore())
                    return fail(event, "Player not found!")

                if(!main.getEconomy().hasAccount(offlinePlayer))
                    return fail(event, "This player does not have an account")

                sendMsg(getEmbed(offlinePlayer))
            } else {
                if(!main.getEconomy().hasAccount(player))
                    return fail(event, "This player does not have an account")

                sendMsg(getEmbed(player))
            }
        } else {
            val player = server.getPlayer(usernameOrUUID)

            if(player == null) {
                val uuid2 = main.usersManager.GetPlayerUUID(usernameOrUUID)
                if(uuid2 == null)
                    return fail(event, "Player not found or has not played before!")

                val offlinePlayer = server.getOfflinePlayer(uuid2)
                if(!offlinePlayer.hasPlayedBefore())
                    return fail(event, "Player not found!")

                if(!main.getEconomy().hasAccount(offlinePlayer))
                    return fail(event, "This player does not have an account")

                sendMsg(getEmbed(offlinePlayer))
            } else {
                if(!main.getEconomy().hasAccount(player))
                    return fail(event, "This player does not have an account")

                sendMsg(getEmbed(player))
            }
        }
    }
}