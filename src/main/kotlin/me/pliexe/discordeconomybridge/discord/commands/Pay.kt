package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import me.pliexe.discordeconomybridge.formatMoney
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.bukkit.Bukkit
import java.text.DecimalFormat

class Pay(main: DiscordEconomyBridge): Command(main) {
    override val name: String
        get() = "pay"

    override val usage: String
        get() = "@user amount"

    override val description: String
        get() = "Pay/Give an specified amount of money to another player/user"

    override fun getCommandOptions(): CommandOptions {
        return CommandOptions()
            .addOption(OptionType.USER, "user", "The user to pay/give money to!")
            .addOption(OptionType.NUMBER, "amount", "The amount to pay/give/send")
    }

    override fun run(event: CommandEventData) {
        if(!main.linkHandler.isLinked(event.author.id))
            return fail(event, "Your discord account is not linked to minecraft!")

        val reciever: DiscordMember
        val amount: Double

        if(event.isSlashCommand()) {
            reciever = event.getOptionMember("user") ?: return fail(event, "Unable to fetch member. Report bug to developer!")
            amount = event.getOptionDouble("amount")!!
        } else {
            if(event.args!!.isEmpty())
                return fail(event, "No argument given for property @user!")

            if(event.args.size < 2)
                return fail(event, "No argument given for property amount!")

            if(event.memberMentionsSize() <= 0)
                return fail(event, "User not found!")

            reciever = event.getMemberMention(0)

            amount = event.args[1].toDoubleOrNull() ?: return fail(event, "Amount may only be an numeric value!")
        }

        if(amount <= 0) return fail(event, "You may not send 0 or less money!")
        if(reciever.id == event.author.id)
            return fail(event, "You may not send money to yourself!")

        val senderPlayer = Bukkit.getOfflinePlayer(main.linkHandler.getUuid(event.author.id))

        if(!main.getEconomy().hasAccount(senderPlayer))
            return fail(event, "You don't have money to send!")

        val senderBalance = main.getEconomy().getBalance(senderPlayer)

        if(senderBalance < amount)
            return fail(event, "You don't have enough money to send that amount of cash")

        val recieverPlayer = main.linkHandler.getUuid(reciever.id)?.let { Bukkit.getOfflinePlayer(it) } ?: return fail(event, "The player you are trying to send money does not have their account linked!")

        if(!main.getEconomy().hasAccount(recieverPlayer))
            main.getEconomy().createPlayerAccount(recieverPlayer)

        main.getEconomy().depositPlayer(recieverPlayer, amount)
        main.getEconomy().withdrawPlayer(senderPlayer, amount)

        val formatter = DecimalFormat("#,###.##")

        event.sendYMLEmbed("payMessage", {
            val form = setCommandPlaceholders(it, event.prefix, event.commandName, description, usage)
            setPlaceholdersForDiscordMessage(event.member!!, reciever, UniversalPlayer(senderPlayer), UniversalPlayer(recieverPlayer), form)
                .replace("{amount}", formatMoney(amount, main.pluginConfig.currency, main.pluginConfig.currencyLeftSide, formatter))
        }).queue()

        main.commandHandler.commandComplete(this)
    }
}