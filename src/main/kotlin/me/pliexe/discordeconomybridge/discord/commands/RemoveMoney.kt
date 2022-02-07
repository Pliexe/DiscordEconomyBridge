package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import me.pliexe.discordeconomybridge.formatMoney
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.text.DecimalFormat

class RemoveMoney(main: DiscordEconomyBridge): Command(main) {
    override val usage: String
        get() = "@user amount"

    override val name: String
        get() = "removemoney"

    override val description: String
        get() = "Remove money from a user's balance"

    override val adminCommand: Boolean
        get() = true

    override fun getCommandOptions(): CommandOptions {
        return CommandOptions()
            .addOption(OptionType.USER, "user", "The user that will lose the money!")
            .addOption(OptionType.NUMBER, "amount", "The amount of money to add!")
            .setDefaultEnabled(false)
    }

    override fun run(event: CommandEventData) {
        val amount: Double
        val user: DiscordUser
        if(event.isSlashCommand()) {
            user = event.getOptionUser("user")!!
            amount = event.getOptionDouble("amount")!!
        } else {
            if(event.args!!.isEmpty())
                return fail(event, "User was not specified!")

            if(event.userMentionsSize() <= 0)
                return fail(event, "Couldn't get user!")

            if(event.args.size < 2)
                return fail(event, "Amount to remove was not specified!")

            amount = event.args[1].toDoubleOrNull()
                ?: return fail(event, "Amount must be an numeric value!")

            user = event.getUserMention(0)
        }

        val uuid = main.linkHandler.getUuid(user.id)
            ?: return fail(event, "This user does not have his account linked!")

        val player = UniversalPlayer.getByUUID(uuid)

        player.createEconomyAccountIfNotPresent(main)

        player.withdrawPlayer(main, amount)

        val formatter = DecimalFormat("#,###.##")

        event.sendYMLEmbed("removemoneyCommandEmbed", {
            val form = setCommandPlaceholders(it, event.prefix, event.commandName, description, usage)
            (if(event.member == null)
                setPlaceholdersForDiscordMessage(event.author, player, form)
            else setPlaceholdersForDiscordMessage(event.member!!, player, form))
                .replace("{amount_decrease}", formatMoney(amount, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
        }).queue()
    }
}