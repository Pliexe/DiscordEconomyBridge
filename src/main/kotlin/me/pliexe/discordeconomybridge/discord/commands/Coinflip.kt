package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import me.pliexe.discordeconomybridge.formatMoney
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.bukkit.OfflinePlayer
import java.text.DecimalFormat
import kotlin.random.Random

class Coinflip(main: DiscordEconomyBridge) : Command(main) {

    override val name: String
        get() = "coinflip"

    override val usage: String
        get() = "@user(The user to challenge) amount(The amount to wager)"

    override val description: String
        get() = "Flip a coin. You win if you get head."

    override fun getCommandOptions(): CommandOptions {
        return CommandOptions()
            .addOption(OptionType.USER, "user", "User to challenge")
            .addOption(OptionType.NUMBER, "wager", "The amount of money to wager")
    }

    override val isGame = true

    override fun run(event: CommandEventData) {
        if(!event.inGuild())
            return fail(event, "This command may only be run in a discord server (Guild)")

        val wagerUuid = main.linkHandler.getUuid(event.author.id)
            ?: return fail(event, "Your account is not linked!")

        val wagerPlayer = server.getOfflinePlayer(wagerUuid)

        val challenger: DiscordMember
        val wager: Double
        if(event.isSlashCommand()) {
            wager = event.getOptionDouble("wager")!!
            challenger = event.getOptionMember("user")!!
        } else {
            if(event.args!!.isEmpty())
                return fail(event, "User was not specified!")

            if(event.memberMentionsSize() <= 0)
                return fail(event, "User not found!")

            if(event.args.size < 2)
                return fail(event, "The amount to wager was not specified!")

            challenger = event.getMemberMention(0)

            wager = event.args[1].toDoubleOrNull() ?: return fail(event, "The wager must be an numeric value!")
        }
        if(challenger.id == event.author.id)
            return fail(event, "You may not play coinflip with yourself!")

        val minBet = main.pluginConfig.minBet

        if(wager < minBet)
            return fail(event, "The wager may not be lower than $minBet")

        val maxBet = main.pluginConfig.maxBet

        if(wager > maxBet)
            return fail(event, "The wager may not be higher than $maxBet")

        if(!main.getEconomy().hasAccount(wagerPlayer))
            main.getEconomy().createPlayerAccount(wagerPlayer)

        val balance = main.getEconomy().getBalance(wagerPlayer)

        if(wager > balance)
            return fail(event, "You don't have this much money to wager!")

        val challengerUuid = main.linkHandler.getUuid(challenger.id)
            ?: return fail(event, "The user that you're trying to challenge does not have their account linked!")

        val challengerPlayer = server.getOfflinePlayer(challengerUuid)

        if(!main.getEconomy().hasAccount(challengerPlayer))
            main.getEconomy().createPlayerAccount(challengerPlayer)

        val challengerBalance = main.getEconomy().getBalance(challengerPlayer)

        if(wager > challengerBalance)
            return fail(event, "The opponent does not have enough money to accept!")

        val formatter = DecimalFormat("#,###.##")

        fun sendMsg(winner: DiscordMember, loser: DiscordMember, winnerServer: OfflinePlayer, landSide: String, bevent: ComponentInteractionEvent) {
            bevent.editMessage(bevent.getYMLEmbed("coinflipCommandEmbed", {
                val form = setCommandPlaceholders(it, event.prefix, event.commandName, description, usage)
                setPlaceholdersForDiscordMessage(winner, winnerServer, form)
                    .replace("%discord_other_username%", loser.user.name)
                    .replace("%discord_other_tag%", loser.user.asTag)
                    .replace("%discord_other_discriminator%", loser.user.discriminator)
                    .replace("{land_side}", landSide)
                    .replace("{amount_wagered}", formatMoney(wager, main.pluginConfig.currency, main.pluginConfig.currencyLeftSide, formatter))
            })).removeActinRows().queue()
        }

        fun decline(bevent: ComponentInteractionEvent? = null, msg: Message? = null) {
            main.commandHandler.removeCooldown(this, event)

            val embed = event.getYMLEmbed("coinflipCommandDeclineEmbed", {
                val form = setCommandPlaceholders(it, event.prefix, event.commandName, description, usage)
                setPlaceholdersForDiscordMessage(event.member!!, wagerPlayer, form)
                    .replace("%discord_other_username%", challenger.user.name)
                    .replace("%discord_other_tag%", challenger.user.asTag)
                    .replace("%discord_other_discriminator%", challenger.user.discriminator)
                    .replace("{amount_wagered}", formatMoney(wager, main.pluginConfig.currency, main.pluginConfig.currencyLeftSide, formatter))
            })

            if(bevent == null)
                msg!!.editMessage(embed).queue()
            else bevent.editMessage(embed).removeActinRows().queue()
        }

        main.commandHandler.commandComplete(this, event)

        event.sendYMLEmbed("coinflipCommandConfirmEmbed", {
            val form = setCommandPlaceholders(it, event.prefix, event.commandName, description, usage)
            setPlaceholdersForDiscordMessage(event.member!!, wagerPlayer, form)
                .replace("%discord_other_username%", challenger.user.name)
                .replace("%discord_other_tag%", challenger.user.asTag)
                .replace("%discord_other_discriminator%", challenger.user.discriminator)
                .replace("{amount_wagered}", formatMoney(wager, main.pluginConfig.currency, main.pluginConfig.currencyLeftSide, formatter))
        }).setYesOrNoButtons(
            getStringOrStringList("coinflipButtonAccept", main.discordMessagesConfig) ?: "Accept",
            getStringOrStringList("coinflipButtonDecline", main.discordMessagesConfig) ?: "Decline"
        ).queue { message ->

            message.awaitYesOrNo(main.pluginConfig.commandTimeout, { it.user.id ==  challenger.id}, { outcome, interaction, deleted ->
                if(outcome) {

                    val currentBalancePlayer = main.getEconomy().getBalance(wagerPlayer)

                    if(wager > currentBalancePlayer)
                        return@awaitYesOrNo fail(interaction!!, "You don't have enough money.")

                    val currentBalanceOpp = main.getEconomy().getBalance(challengerPlayer)

                    if(wager > currentBalanceOpp)
                        return@awaitYesOrNo fail(interaction!!, "The opponent does not have enough money.")

                    if(Random.nextInt(0, 2) > 0)
                    {
                        if(!main.getEconomy().hasAccount(challengerPlayer))
                            main.getEconomy().createPlayerAccount(challengerPlayer)

                        main.getEconomy().depositPlayer(challengerPlayer, wager)
                        main.getEconomy().withdrawPlayer(wagerPlayer, wager)

                        sendMsg(challenger, event.member!!, challengerPlayer, "tail", interaction!!)
                    } else {
                        if(!main.getEconomy().hasAccount(challengerPlayer))
                            main.getEconomy().createPlayerAccount(challengerPlayer)

                        main.getEconomy().depositPlayer(wagerPlayer, wager)
                        main.getEconomy().withdrawPlayer(challengerPlayer, wager)

                        sendMsg(event.member!!, challenger, wagerPlayer, "head", interaction!!)
                    }
                }
                else if(!deleted) {
                    if(interaction == null) {
                        decline(null, message)
                    } else {
                        decline(interaction)
                    }
                }
            })
        }
    }
}