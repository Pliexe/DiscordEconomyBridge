package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.UUIDUtils
import me.pliexe.discordeconomybridge.discord.Command
import me.pliexe.discordeconomybridge.discord.GetYmlEmbed
import me.pliexe.discordeconomybridge.discord.setPlaceholdersForDiscordMessage
import me.pliexe.discordeconomybridge.formatMoney
import me.pliexe.discordeconomybridge.getEmbedFromYml
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.awt.Color
import java.text.DecimalFormat

class AddMoney(main: DiscordEconomyBridge): Command(main) {

    override val usage: String
        get() = "amount @user"

    override fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        if(event.member == null)
            return fail(event, "Couldn't get member!")

        if(args.isEmpty())
            return fail(event, "No arguemnts given.")

        val amount = args[0].toDoubleOrNull()
            ?: return fail(event, "Amount not given!")

        if(event.message.contentRaw.length < commandName.length + prefix.length + args[0].length + 2)
            return fail(event, "Missing user parameter!")

        val uuidOrUsername = event.message.contentRaw.substring(commandName.length + prefix.length + args[0].length + 2)

        fun getEmbed(removed: Double, player: Player): MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return GetYmlEmbed(event.channel, {
                setPlaceholdersForDiscordMessage(event.member!!, player, it)
                    .replace("{amount_increase}", formatMoney(removed, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "addmoneyCommandEmbed", main.discordMessagesConfig).build()
        }

        fun getEmbed(removed: Double, player: OfflinePlayer): MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return GetYmlEmbed(event.channel, {
                setPlaceholdersForDiscordMessage(event.member!!, player, it)
                    .replace("{amount_increase}", formatMoney(removed, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "addmoneyCommandEmbed", main.discordMessagesConfig).build()
        }

        fun sendMsg(embed: MessageEmbed) {
            event.channel.sendMessageEmbeds(embed).queue()
        }

        val uuid = UUIDUtils.getUUIDFromString(uuidOrUsername)

        if(uuid == null) {
            val player = server.getPlayer(uuidOrUsername)

            if(player == null) {
                val uuid2 = main.usersManager.GetPlayerUUID(uuidOrUsername)

                if(uuid2 == null)
                    return fail(event, "Player not found or has not played before!")

                val offlinePlayer = server.getOfflinePlayer(uuid2)

                if(!offlinePlayer.hasPlayedBefore())
                    return fail(event, "Player not found!")

                if(!main.getEconomy().hasAccount(offlinePlayer))
                    return fail(event, "This player does not have an account")

                main.getEconomy().depositPlayer(offlinePlayer, amount)

                sendMsg(getEmbed(amount, offlinePlayer))
            } else {
                if(!main.getEconomy().hasAccount(player))
                    return fail(event, "This player does not have an account")

                main.getEconomy().depositPlayer(player, amount)

                sendMsg(getEmbed(amount, player))
            }
        } else {
            val player = server.getPlayer(uuid)

            if(player == null) {
                val offlinePlayer = server.getOfflinePlayer(uuid)

                if(!offlinePlayer.hasPlayedBefore())
                    return fail(event, "Player not found!")

                if(!main.getEconomy().hasAccount(offlinePlayer))
                    return fail(event, "This player does not have an account")

                main.getEconomy().depositPlayer(offlinePlayer, amount)

                sendMsg(getEmbed(amount, offlinePlayer))
            } else {
                if(!main.getEconomy().hasAccount(player))
                    return fail(event, "This player does not have an account")

                main.getEconomy().depositPlayer(player, amount)

                sendMsg(getEmbed(amount, player))
            }
        }
    }
}