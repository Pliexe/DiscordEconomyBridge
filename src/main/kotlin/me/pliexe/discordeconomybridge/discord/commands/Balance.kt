package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import me.pliexe.discordeconomybridge.formatMoney
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.text.DecimalFormat
import java.util.*

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
    }

    override fun run(event: CommandEventData) {
        val user = if(event.isSlashCommand()) {
            event.getOptionUser("user") ?: event.author
        } else {
            if(event.userMentionsSize() > 0) event.getUserMention(0) else event.author
        }

        if(user.id != event.author.id && (if(config.isBoolean("disableViewingOfOtherUsers")) config.getBoolean("disableViewingOfOtherUsers") else false)) {
            if(event.member == null)
                return fail(event, "You may not look at other peoples balance!")
            else if(!main.moderatorManager.isModerator(event.member!!))
                return fail(event, "You don't have permission to view the balance of other users!")
        }

        val uuid: UUID = main.linkHandler.getUuid(user.id)
            ?: return fail(event, "This user does not have his account linked!")

        fun sendMsg(player: UniversalPlayer) {
            val formatter = DecimalFormat("#,###.##")

            event.sendYMLEmbed("balanceCommandEmbed", {
                val form = setCommandPlaceholders(it, event.prefix, event.commandName, description, usage)
                (if(event.member == null)
                     setPlaceholdersForDiscordMessage(event.author, player, form)
                else
                    setPlaceholdersForDiscordMessage(event.member!!, player, form))
                    .replace("%custom_vault_eco_balance%", formatMoney(player.getBalance(main), config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
                    .replace("%custom_player_online%", if(player.isOnline) "Online" else "Offline")
            }, {
                if(it == "ifOnline") player.isOnline else false
            }).queue()
        }

        val offlinePlayer = server.getOfflinePlayer(uuid)

        if(!main.getEconomy().hasAccount(offlinePlayer))
            return fail(event, "This player does not have an account")

        sendMsg(UniversalPlayer(offlinePlayer))
    }
}