package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.UUIDUtils
import me.pliexe.discordeconomybridge.discord.Command
import me.pliexe.discordeconomybridge.formatMoney
import me.pliexe.discordeconomybridge.getEmbedFromYml
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.text.DecimalFormat

class RemoveMoney(main: DiscordEconomyBridge): Command(main) {
    override val usage: String
        get() = "amount @user"

    override fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        if(event.member == null)
            return fail(event, "Couldn't get member!")

        if(!main.moderatorManager.isModerator(event.member!!))
            return fail(event, config.getString("noPermissionMessage"))

        if(args.isEmpty())
            return fail(event, "No arguemnts given. Usage: ${prefix}removemoney amount @user")

        val amount = args[0].toDoubleOrNull()
            ?: return fail(event, "Amount not given! Usage: ${prefix}removemoney **amount** @user")

        if(event.message.contentRaw.length < commandName.length + prefix.length + args[0].length + 2)
            return fail(event, "Missing user parameter! Usage: ${prefix}removemoney amount **@user**")

        val uuidOrUsername = event.message.contentRaw.substring(commandName.length + prefix.length + args[0].length + 2)

        fun sendMsg(removed: Double, username: String) {
            val formatter = DecimalFormat("#,###.##")

            val embed = getEmbedFromYml(config, "removemoneyCommandEmbed", {
                it
                    .replace("{moneyAmount}", formatMoney(removed, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
                    .replace("{username}", username)
            })

            event.channel.sendMessageEmbeds(embed.build()).queue()
        }

        val uuid = UUIDUtils.getUUIDFromString(uuidOrUsername)

        if(uuid == null) {
            val player = server.getPlayer(uuidOrUsername)

            if(player == null) {
                val uuid2 = main.usersManager.GetPlayerUUID(uuidOrUsername)
                    ?: return fail(event, "Player not found or has not played before!")

                val offlinePlayer = server.getOfflinePlayer(uuid2)

                if(!offlinePlayer.hasPlayedBefore())
                    return fail(event, "Player not found!")

                if(!main.getEconomy().hasAccount(offlinePlayer))
                    return fail(event, "This player does not have an account")

                main.getEconomy().withdrawPlayer(offlinePlayer, amount)

                sendMsg(amount, offlinePlayer.name)
            } else {
                if(!main.getEconomy().hasAccount(player))
                    return fail(event, "This player does not have an account")

                main.getEconomy().withdrawPlayer(player, amount)

                sendMsg(amount, player.name)
            }
        } else {
            val player = server.getPlayer(uuid)

            if(player == null) {
                val offlinePlayer = server.getOfflinePlayer(uuid)

                if(!offlinePlayer.hasPlayedBefore())
                    return fail(event, "Player not found!")

                if(!main.getEconomy().hasAccount(offlinePlayer))
                    return fail(event, "This player does not have an account")

                main.getEconomy().withdrawPlayer(offlinePlayer, amount)

                sendMsg(amount, offlinePlayer.name)
            } else {
                if(!main.getEconomy().hasAccount(player))
                    return fail(event, "This player does not have an account")

                main.getEconomy().withdrawPlayer(player, amount)

                sendMsg(amount, player.name)
            }
        }
    }
}