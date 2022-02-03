package me.pliexe.discordeconomybridge.discord.commands

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.ButtonClickEvent
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent
import github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.Command
import me.pliexe.discordeconomybridge.discord.GetYmlEmbed
import me.pliexe.discordeconomybridge.discord.setCommandPlaceholders
import me.pliexe.discordeconomybridge.discord.setPlaceholdersForDiscordMessage
import me.pliexe.discordeconomybridge.formatMoney
import org.bukkit.OfflinePlayer
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule
import kotlin.random.Random

class Coinflip(main: DiscordEconomyBridge) : Command(main) {

    override val name: String
        get() = "coinflip"

    override val usage: String
        get() = "@user(The user to challenge) amount(The amount to wager)"

    val buttonEvents = HashMap<String, ((e: ButtonClickEvent) -> Unit)>()

    override fun getSlashCommandData(): CommandData {
        return CommandData(name, "Flip a coin. You win if you get head.")
            .addOption(OptionType.USER, "user", "User to challenge", true)
            .addOption(OptionType.NUMBER, "wager", "The amount of money to wager", true)
    }

    override fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        val wagerUuid = DiscordSRV.getPlugin().accountLinkManager.getUuid(event.author.id)
            ?: return fail(event, "Your account is not linked!")

        val wagerPlayer = server.getOfflinePlayer(wagerUuid)
        if(event.message.mentionedMembers.isEmpty())
            return fail(event, "User not found!")

        if(args.size < 2)
            return fail(event, "The amount to wager was not specified!")

        val wager = args[1].toDoubleOrNull() ?: return fail(event, "The wager must be an numeric value!")

        val minBet = if(config.isDouble("minBet")) config.getDouble("minBet") else 100.0

        if(wager < minBet)
            return fail(event, "The wager may not be lower than $minBet")

        val maxBet = if(config.isDouble("maxBet")) config.getDouble("maxBet") else 100000000000000000.0

        if(wager > maxBet)
            return fail(event, "The wager may not be higher than $maxBet")

        if(!main.getEconomy().hasAccount(wagerPlayer))
            main.getEconomy().createPlayerAccount(wagerPlayer)

        val balance = main.getEconomy().getBalance(wagerPlayer)

        if(wager > balance)
            return fail(event, "You don't have this much money to wager!")

        val challenger = event.message.mentionedMembers.first()

        val challengerUuid = DiscordSRV.getPlugin().accountLinkManager.getUuid(challenger.id)
            ?: return fail(event, "The user that you're trying to challenge does not have his account linked!")

        val challengerPlayer = server.getOfflinePlayer(challengerUuid)

        if(!main.getEconomy().hasAccount(challengerPlayer))
            main.getEconomy().createPlayerAccount(challengerPlayer)

        val challengerBalance = main.getEconomy().getBalance(challengerPlayer)

        if(wager > challengerBalance)
            return fail(event, "The opponent does not have enough money to accept!")

        val formatter = DecimalFormat("#,###.##")

        fun sendMsg(winner: Member, loser: Member, winnerServer: OfflinePlayer, landSide: String, bevent: ButtonClickEvent) {
            bevent.editMessageEmbeds(GetYmlEmbed( {
                val form = setCommandPlaceholders(it, prefix, name, usage)
                setPlaceholdersForDiscordMessage(winner, winnerServer, form)
                    .replace("%discord_other_username%", loser.user.name)
                    .replace("%discord_other_tag%", loser.user.asTag)
                    .replace("%discord_other_discriminator%", loser.user.discriminator)
                    .replace("{land_side}", landSide)
                    .replace("{amount_wagered}", formatMoney(wager, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "coinflipCommandEmbed", main.discordMessagesConfig).build()).setActionRows().setContent("** **").queue()
        }

        fun decline(bevent: ButtonClickEvent? = null) {
            val embed = GetYmlEmbed( {
                val form = setCommandPlaceholders(it, prefix, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, wagerPlayer, form)
                    .replace("%discord_other_username%", challenger.user.name)
                    .replace("%discord_other_tag%", challenger.user.asTag)
                    .replace("%discord_other_discriminator%", challenger.user.discriminator)
                    .replace("{amount_wagered}", formatMoney(wager, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "coinflipCommandDeclineEmbed", main.discordMessagesConfig).build()

            if(bevent == null)
                event.channel.sendMessageEmbeds(embed).queue()
            else bevent.editMessageEmbeds(embed).setActionRows().setContent("** **").queue()
        }

        event.channel.sendMessageEmbeds(GetYmlEmbed({
            val form = setCommandPlaceholders(it, prefix, name, usage)
            setPlaceholdersForDiscordMessage(event.member!!, wagerPlayer, form)
                .replace("%discord_other_username%", challenger.user.name)
                .replace("%discord_other_tag%", challenger.user.asTag)
                .replace("%discord_other_discriminator%", challenger.user.discriminator)
                .replace("{amount_wagered}", formatMoney(wager, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
        },"coinflipCommandConfirmEmbed", main.discordMessagesConfig).build())
            .setActionRow(mutableListOf(
                Button.success("accept", "Accept"),
                Button.danger("decline", "Decline")
            ))
            .content(if(main.discordMessagesConfig.isBoolean("coinflipCommandDeclineEmbed.ping")) (if(main.discordMessagesConfig.getBoolean("coinflipCommandDeclineEmbed.ping")) "<@${challenger.id}>" else null) else null)
            .queue {message ->
                val tmr = Timer("CFTMOT", false).schedule(300000) {
                    decline()
                }
                buttonEvents[message.id] = { bevent ->
                    if(bevent.user.id != challenger.id)
                        bevent.reply("You may not interact with this menu!").setEphemeral(true).queue()
                    else
                    {
                        tmr.cancel()
                        buttonEvents.remove(message.id)
                        when(bevent.componentId)
                        {
                            "accept" -> {
                                if(Random.nextInt(0, 2) > 0)
                                {
                                    if(!main.getEconomy().hasAccount(challengerPlayer))
                                        main.getEconomy().createPlayerAccount(challengerPlayer)

                                    main.getEconomy().depositPlayer(challengerPlayer, wager)
                                    main.getEconomy().withdrawPlayer(wagerPlayer, wager)

                                    sendMsg(challenger, event.member!!, challengerPlayer, "tail", bevent)
                                } else {
                                    if(!main.getEconomy().hasAccount(challengerPlayer))
                                        main.getEconomy().createPlayerAccount(challengerPlayer)

                                    main.getEconomy().depositPlayer(wagerPlayer, wager)
                                    main.getEconomy().withdrawPlayer(challengerPlayer, wager)

                                    sendMsg(event.member!!, challenger, wagerPlayer, "head", bevent)
                                }
                            }
                            "decline" -> {
                                decline(bevent)
                            }
                        }
                    }
                }
            }
    }

    override fun run(event: SlashCommandEvent) {


        val wagerUuid = DiscordSRV.getPlugin().accountLinkManager.getUuid(event.user.id)
            ?: return fail(event, "Your account is not linked!")

        val wagerPlayer = server.getOfflinePlayer(wagerUuid)

        val wager = event.options[1].asDouble

        val minBet = if(config.isDouble("minBet")) config.getDouble("minBet") else 100.0

        if(wager < minBet)
            return fail(event, "The wager may not be lower than $minBet")

        val maxBet = if(config.isDouble("maxBet")) config.getDouble("maxBet") else 100000000000000000.0

        if(wager > maxBet)
            return fail(event, "The wager may not be higher than $maxBet")

        if(!main.getEconomy().hasAccount(wagerPlayer))
            main.getEconomy().createPlayerAccount(wagerPlayer)

        val balance = main.getEconomy().getBalance(wagerPlayer)

        if(wager > balance)
            return fail(event, "You don't have this much money to wager!")

        val challenger = event.options.first().asMember

        if(challenger == null)
            return fail(event, "Unable to get member for somer reason!")

        val challengerUuid = DiscordSRV.getPlugin().accountLinkManager.getUuid(challenger.id)
            ?: return fail(event, "The user that you're trying to challenge does not have his account linked!")

        val challengerPlayer = server.getOfflinePlayer(challengerUuid)

        if(!main.getEconomy().hasAccount(challengerPlayer))
            main.getEconomy().createPlayerAccount(challengerPlayer)

        val challengerBalance = main.getEconomy().getBalance(challengerPlayer)

        if(wager > challengerBalance)
            return fail(event, "The opponent does not have enough money to accept!")

        val formatter = DecimalFormat("#,###.##")

        fun sendMsg(winner: Member, loser: Member, winnerServer: OfflinePlayer, landSide: String, bevent: ButtonClickEvent) {
            bevent.editMessageEmbeds(GetYmlEmbed( {
                val form = setCommandPlaceholders(it, name, usage)
                setPlaceholdersForDiscordMessage(winner, winnerServer, form)
                    .replace("%discord_other_username%", loser.user.name)
                    .replace("%discord_other_tag%", loser.user.asTag)
                    .replace("%discord_other_discriminator%", loser.user.discriminator)
                    .replace("{land_side}", landSide)
                    .replace("{amount_wagered}", formatMoney(wager, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "coinflipCommandEmbed", main.discordMessagesConfig).build()).setActionRows().setContent("** **").queue()
        }

        fun decline(bevent: ButtonClickEvent? = null) {
            val embed = GetYmlEmbed( {
                val form = setCommandPlaceholders(it, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, wagerPlayer, form)
                    .replace("%discord_other_username%", challenger.user.name)
                    .replace("%discord_other_tag%", challenger.user.asTag)
                    .replace("%discord_other_discriminator%", challenger.user.discriminator)
                    .replace("{amount_wagered}", formatMoney(wager, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "coinflipCommandDeclineEmbed", main.discordMessagesConfig).build()

            if(bevent == null)
                event.replyEmbeds(embed).queue()
            else bevent.editMessageEmbeds(embed).setActionRows().setContent("** **").queue()
        }

        event.replyEmbeds(GetYmlEmbed({
            val form = setCommandPlaceholders(it, name, usage)
            setPlaceholdersForDiscordMessage(event.member!!, wagerPlayer, form)
                .replace("%discord_other_username%", challenger.user.name)
                .replace("%discord_other_tag%", challenger.user.asTag)
                .replace("%discord_other_discriminator%", challenger.user.discriminator)
                .replace("{amount_wagered}", formatMoney(wager, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
        },"coinflipCommandConfirmEmbed", main.discordMessagesConfig).build())
            .addActionRow(mutableListOf(
                Button.success("accept", "Accept"),
                Button.danger("decline", "Decline")
            ))
            .setContent(if(main.discordMessagesConfig.isBoolean("coinflipCommandDeclineEmbed.ping")) (if(main.discordMessagesConfig.getBoolean("coinflipCommandDeclineEmbed.ping")) "<@${challenger.id}>" else null) else null)
            .queue {messageInteraction ->
                messageInteraction.retrieveOriginal().queue { message ->
                    val tmr = Timer("CFTMOT", false).schedule(300000) {
                        decline()
                    }
                    buttonEvents[message.id] = { bevent ->
                        if(bevent.user.id != challenger.id)
                            bevent.reply("You may not interact with this menu!").setEphemeral(true).queue()
                        else
                        {
                            tmr.cancel()
                            buttonEvents.remove(message.id)
                            when(bevent.componentId)
                            {
                                "accept" -> {
                                    if(Random.nextInt(0, 2) > 0)
                                    {
                                        if(!main.getEconomy().hasAccount(challengerPlayer))
                                            main.getEconomy().createPlayerAccount(challengerPlayer)

                                        main.getEconomy().depositPlayer(challengerPlayer, wager)
                                        main.getEconomy().withdrawPlayer(wagerPlayer, wager)

                                        sendMsg(challenger, event.member!!, challengerPlayer, "tail", bevent)
                                    } else {
                                        if(!main.getEconomy().hasAccount(challengerPlayer))
                                            main.getEconomy().createPlayerAccount(challengerPlayer)

                                        main.getEconomy().depositPlayer(wagerPlayer, wager)
                                        main.getEconomy().withdrawPlayer(challengerPlayer, wager)

                                        sendMsg(event.member!!, challenger, wagerPlayer, "head", bevent)
                                    }
                                }
                                "decline" -> {
                                    decline(bevent)
                                }
                            }
                        }
                }
            }
            }
    }


    override fun run(
        event: net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent,
        commandName: String,
        prefix: String,
        args: List<String>
    ) {
        if(config.isBoolean("noDiscordSRVwarn") && config.getBoolean("noDiscordSRVwarn"))
            fail(event, "This command is not supported without DiscordSRV! You may automatically disable this messages in config.yml with noDiscordSRVwarn field")
    }
}