package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import me.pliexe.discordeconomybridge.formatMoney
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.text.DecimalFormat

class Balance(main: DiscordEconomyBridge): Command(main) {
    override val usage: String
        get() = "@user"

    override val name: String
        get() = "balance"

    override val description: String
        get() = "Show the current balance of user"

    override fun getCommandOptions(): CommandOptions {
        return CommandOptions()
            .addOption(OptionType.USER, "user", "The user you want to check!", false)
            .addOption(OptionType.STRING, "player", "The minecraft player you want to check!", false)
    }

    override fun run(event: CommandEventData) {
        val player: UniversalPlayer = if(event.isSlashCommand()) {
            if (main.defaultConfig.getBoolean("disableViewingOfOtherUsers") && !main.moderatorManager.isModerator(event.member!!)) {
                val uuid = main.linkHandler.getUuid(event.author.id)
                if (uuid == null) {
                    fail(event, "You are not linked to a minecraft account!")
                    return
                } else UniversalPlayer.getByUUID(uuid)
            } else {
                event.getOptionUser("user")?.let {
                    val uuid = main.linkHandler.getUuid(it.id)
                    if (uuid == null) {
                        fail(event, "User is not linked to a minecraft account!")
                        return
                    } else UniversalPlayer.getByUUID(uuid)
                } ?: run {
                    event.getOptionString("player")?.let {
                        UniversalPlayer.getByString(it)
                    } ?: run {
                        fail(event, "Player not found!")
                        return
                    }
                }
            }
        } else {
            if(event.args!!.isEmpty() || (main.defaultConfig.getBoolean("disableViewingOfOtherUsers") && !main.moderatorManager.isModerator(event.member!!))) {
                val uuid = main.linkHandler.getUuid(event.author.id)
                if (uuid == null) {
                    fail(event, "You are not linked to a minecraft account!")
                    return
                } else UniversalPlayer.getByUUID(uuid)
            } else {
                if(event.userMentionsSize() > 0) {
                    val uuid = main.linkHandler.getUuid(event.getUserMention(0).id)
                    if (uuid == null) {
                        fail(event, "User is not linked to a minecraft account!")
                        return
                    } else UniversalPlayer.getByUUID(uuid)
                } else {

                    UniversalPlayer.getByString(event.args[0]) ?: run {
                        return fail(event, "Player not found!")
                    }
                }
            }
        }
        val formatter = DecimalFormat("#,###.##")

        event.sendYMLEmbed("balanceCommandEmbed", {
            val form = setCommandPlaceholders(it, event.prefix, event.commandName, description, usage)
            (if(event.member == null)
                setPlaceholdersForDiscordMessage(event.author, player, form)
            else
                setPlaceholdersForDiscordMessage(event.member!!, player, form))
                .replace("%custom_vault_eco_balance%", formatMoney(player.getBalance(main), main.pluginConfig.currency, main.pluginConfig.currencyLeftSide, formatter))
                .replace("%custom_player_online%", if(player.isOnline) "Online" else "Offline")
        }, {
            if(it == "ifOnline") player.isOnline else false
        }).queue()

        main.commandHandler.commandComplete(this)
    }
}