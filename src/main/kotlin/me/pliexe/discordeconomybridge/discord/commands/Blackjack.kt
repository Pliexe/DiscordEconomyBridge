package me.pliexe.discordeconomybridge.discord.commands
import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message
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
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule
import kotlin.random.Random
import kotlin.random.nextInt

class Card (
    val value: Int,
    val emote: String
)

private val template = arrayOf(
    Card(2, "2:spades:"), Card(3, "3:spades:"), Card(4, "4:spades:"), Card(5, "5:spades:"), Card(6, "6:spades:"), Card(7, "7:spades:"), Card(8, "8:spades:"), Card(9, "9:spades:"), Card(10, "10:spades:"), Card(11, "A:spades:"), Card(10, "j:spades:"), Card(10, "q:spades:"), Card(10, "k:spades:"),
    Card(2, "2:clubs:"), Card(3, "3:clubs:"), Card(4, "4:clubs:"), Card(5, "5:clubs:"), Card(6, "6:clubs:"), Card(7, "7:clubs:"), Card(8, "8:clubs:"), Card(9, "9:clubs:"), Card(10, "10:clubs:"), Card(11, "A:clubs:"), Card(10, "j:clubs:"), Card(10, "q:clubs:"), Card(10, "k:clubs:"),
    Card(2, "2:diamonds:"), Card(3, "3:diamonds:"), Card(4, "4:diamonds:"), Card(5, "5:diamonds:"), Card(6, "6:diamonds:"), Card(7, "7:diamonds:"), Card(8, "8:diamonds:"), Card(9, "9:diamonds:"), Card(10, "10:diamonds:"), Card(11, "A:diamonds:"), Card(10, "j:diamonds:"), Card(10, "q:diamonds:"), Card(10, "k:diamonds:"),
    Card(2, "2:hearts:"), Card(3, "3:hearts:"), Card(4, "4:hearts:"), Card(5, "5:hearts:"), Card(6, "6:hearts:"), Card(7, "7:hearts:"), Card(8, "8:hearts:"), Card(9, "9:hearts:"), Card(10, "10:hearts:"), Card(11, "A:hearts:"), Card(10, "j:hearts:"), Card(10, "q:hearts:"), Card(10, "k:hearts:")
)

class Blackjack(main: DiscordEconomyBridge) : Command(main) {
    override val name: String
        get() = "blackjack"

    override val usage: String
        get() = "bet"

    val buttonEvents = HashMap<String, ((e: ButtonClickEvent) -> Unit)>()

    override fun getSlashCommandData(): CommandData {
        return CommandData(name, "Play a game of blackjack!")
            .addOption(OptionType.NUMBER, "bet", "Amount to bet", true)
    }

    override fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        if(args.isEmpty())
            return fail(event, "No bet amount was given!")

        var bet = args[0].toDoubleOrNull() ?: return fail(event, "Bet may only be an numeric value!")

        val minBet = if(config.isDouble("minBet")) config.getDouble("minBet") else 100.0

        if(bet < minBet)
            return fail(event, "The wager may not be lower than $minBet")

        val maxBet = if(config.isDouble("maxBet")) config.getDouble("maxBet") else 100000000000000000.0

        if(bet > maxBet)
            return fail(event, "The wager may not be higher than $maxBet")

        val uuid = DiscordSRV.getPlugin().accountLinkManager.getUuid(event.author.id)
            ?: return fail(event, "Your account is not linked!")

        val player = server.getOfflinePlayer(uuid)

        if(!main.getEconomy().hasAccount(player))
            main.getEconomy().createPlayerAccount(player)

        val currentBalance = main.getEconomy().getBalance(player)

        if(bet > currentBalance)
            return fail(event, "You don't have enough money to bet the amount specified!")

        val currentDeck = template.toMutableList()
        currentDeck.shuffle()

        val yourCards = mutableListOf(currentDeck.removeAt(0), currentDeck.removeAt(0))
        val houseCards =  mutableListOf(currentDeck.removeAt(0), currentDeck.removeAt(0))

        fun calculateValue(array: MutableList<Card>): Int {
            val values = array.map { it.value }
            val originalOutcome: Int = values.reduce { acc, curr ->
                if (curr == 11 && acc > 10)
                    acc + 1
                else
                    curr + acc
            }

            return if(originalOutcome > 21) values.reduce { acc, curr ->
                if(curr == 11)
                    acc + 1
                else curr + acc
            } else originalOutcome
        }

        val formatter = DecimalFormat("#,###.##")

        var msg: Message? = null

        fun blackjackOutcome(enemy: Boolean, bEvent: ButtonClickEvent? = null) {
            if(enemy)
                main.getEconomy().withdrawPlayer(player, bet)
            else
                main.getEconomy().depositPlayer(player, bet)

            val embed = GetYmlEmbed( { text ->


                val form = setCommandPlaceholders(text, prefix, name, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())
                    .replace(if(enemy) "{lose_amount}" else "{win_amount}", formatMoney(bet, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))

                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }, if(enemy) "blackjackCommandBlackjackOutcomeDealerEmbed" else "blackjackCommandBlackjackOutcomePlayerEmbed", main.discordMessagesConfig)

            if(bEvent == null)
                event.channel.sendMessageEmbeds(embed.build()).queue()
            else
                bEvent.editMessageEmbeds(embed.build()).setActionRows().queue()
        }

        fun draw(blackjack: Boolean, bEvent: ButtonClickEvent? = null) {
            val embed = GetYmlEmbed( { text ->

                val form = setCommandPlaceholders(text, prefix, name, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())

                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }, if(blackjack) "blackjackCommandDrawBlackjackOutcomeEmbed" else "blackjackCommandDrawOutcomeEmbed", main.discordMessagesConfig)

            if(bEvent == null)
            {
                if(msg == null)
                    event.channel.sendMessageEmbeds(embed.build()).queue()
                else msg!!.editMessageEmbeds(embed.build()).setActionRows().queue()
            }
            else
                bEvent.editMessageEmbeds(embed.build()).setActionRows().queue()
        }

        fun bust(enemy: Boolean, bEvent: ButtonClickEvent? = null) {

            if(enemy)
                main.getEconomy().depositPlayer(player, bet)
            else
                main.getEconomy().withdrawPlayer(player, bet)

            val embed = GetYmlEmbed( { text ->

                val form = setCommandPlaceholders(text, prefix, name, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())
                    .replace(if(enemy) "{win_amount}" else "{lose_amount}", formatMoney(bet, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))

                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }, if(enemy) "blackjackCommandBustDealerEmbed" else "blackjackCommandBustPlayerEmbed", main.discordMessagesConfig)

            if(bEvent == null)
                msg!!.editMessageEmbeds(embed.build()).setActionRows().queue()
            else bEvent.editMessageEmbeds(embed.build()).setActionRows().queue()
        }

        fun otherOutcomes(enemy: Boolean, bEvent: ButtonClickEvent? = null) {
            if(enemy)
                main.getEconomy().withdrawPlayer(player, bet)
            else
                main.getEconomy().depositPlayer(player, bet)

            val embed = GetYmlEmbed( { text ->

                val form = setCommandPlaceholders(text, prefix, name, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())
                    .replace(if(enemy) "{lose_amount}" else "{win_amount}", formatMoney(bet, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))

                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }, if(enemy) "blackjackCommandDealerWinEmbed" else "blackjackCommandPlayerWinEmbed", main.discordMessagesConfig)

            if(bEvent == null)
                msg!!.editMessageEmbeds(embed.build()).setActionRows().queue()
            else bEvent.editMessageEmbeds(embed.build()).setActionRows().queue()
        }

        var hit: ((bEvent: ButtonClickEvent, messageId: String) -> Unit)? = null

        fun resolveDealerActions(bEvent: ButtonClickEvent? = null): Boolean {
            var failed = false

            do {
                val value = calculateValue(houseCards)
                if(value > 21) {
                    failed = true
                    bust(true, bEvent)
                    break
                }

                if(value == 21) {
                    failed = true
                    blackjackOutcome(true, bEvent)
                    break
                }


                val chance = Random.nextInt(4 .. 21)

                if(chance > value)
                    houseCards.add(currentDeck.removeAt(0))
                else break
            } while (true)

            return failed
        }

        fun stand(bEvent: ButtonClickEvent? = null) {
            val outc = resolveDealerActions(bEvent)
            if(outc) return

            val yourScore = calculateValue(yourCards)
            val dealerScore = calculateValue(houseCards)

            if(yourScore == dealerScore) draw(false, bEvent)
            else if(yourScore > dealerScore) otherOutcomes(false, bEvent)
            else otherOutcomes(true, bEvent)
        }

        fun ShowHand(bEvent: ButtonClickEvent? = null) {
            val embed = GetYmlEmbed( { text ->
                val form = setCommandPlaceholders(text, prefix, name, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.slice(1 until houseCards.size).joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards.subList(1, houseCards.size)).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())

                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }, "blackjackCommandShowEmbed", main.discordMessagesConfig)
            if(bEvent == null)
            {
                event.channel.sendMessageEmbeds(embed.build())
                    .setActionRow(mutableListOf(
                        Button.primary("hit", "Hit"),
                        Button.primary("stand", "Stand"),
                        Button.primary("double", "Double down").withDisabled(currentBalance - bet * 2 <= 0)
                    )).queue { message ->
                        msg = message
                        val tmr = Timer("BLTMOT", false).schedule(300000) {
                            stand()
                        }
                        buttonEvents[message.id] = { bevent ->
                            if(bevent.user.id != event.author.id) {
                                bevent.reply("You may not interact with this menu!").setEphemeral(true).queue()
                            } else {
                                tmr.cancel()
                                when (bevent.componentId) {
                                    "hit" -> {
                                        hit!!(bevent, message.id)
                                    }
                                    "stand" -> {
                                        buttonEvents.remove(message.id)
                                        stand(bevent)
                                    }
                                    "double" -> {
                                        buttonEvents.remove(message.id)
                                        bet += bet
                                        yourCards.add(currentDeck.removeAt(0))
                                        val score = calculateValue(yourCards)
                                        if (score == 21) blackjackOutcome(false, bevent)
                                        else if (score > 21) bust(false, bevent)
                                        else stand(bevent)
                                    }
                                }
                            }
                        }
                    }
            } else {
                bEvent.editMessageEmbeds(embed.build())
                    .setActionRow(mutableListOf(
                        Button.primary("hit", "Hit"),
                        Button.primary("stand", "Stand"),
                        Button.primary("double", "Double down").withDisabled(currentBalance - bet * 2 <= 0)
                    )).queue()
            }
        }

        hit = { bEvent, msgId ->
            yourCards.add(currentDeck.removeAt(0))

            val score = calculateValue(yourCards)

            if(score == 21) {
                buttonEvents.remove(msgId)
                blackjackOutcome(false, bEvent)
            }
            else if(score > 21) {
                buttonEvents.remove(msgId)
                bust(false, bEvent)
            }
            else {
                ShowHand(bEvent)
            }
        }


        val begCalc = calculateValue(yourCards)
        val begEnCalc = calculateValue(houseCards)

        if(begCalc == 21 && begEnCalc == 21) return draw(true)
        if(begCalc == 21) return blackjackOutcome(false)
        if(begEnCalc == 21) return blackjackOutcome(true)

        ShowHand()

    }

    override fun run(event: SlashCommandEvent) {

        var bet = event.options.first().asDouble

        val minBet = if(config.isDouble("minBet")) config.getDouble("minBet") else 100.0

        if(bet < minBet)
            return fail(event, "The wager may not be lower than $minBet")

        val maxBet = if(config.isDouble("maxBet")) config.getDouble("maxBet") else 100000000000000000.0

        if(bet > maxBet)
            return fail(event, "The wager may not be higher than $maxBet")

        val uuid = DiscordSRV.getPlugin().accountLinkManager.getUuid(event.user.id)
            ?: return fail(event, "Your account is not linked!")

        val player = server.getOfflinePlayer(uuid)

        if(!main.getEconomy().hasAccount(player))
            main.getEconomy().createPlayerAccount(player)

        val currentBalance = main.getEconomy().getBalance(player)

        if(bet > currentBalance)
            return fail(event, "You don't have enough money to bet the amount specified!")

        val currentDeck = template.toMutableList()
        currentDeck.shuffle()

        val yourCards = mutableListOf(currentDeck.removeAt(0), currentDeck.removeAt(0))
        val houseCards =  mutableListOf(currentDeck.removeAt(0), currentDeck.removeAt(0))

        fun calculateValue(array: MutableList<Card>): Int {
            val values = array.map { it.value }
            val originalOutcome: Int = values.reduce { acc, curr ->
                if (curr == 11 && acc > 10)
                    acc + 1
                else
                    curr + acc
            }

            return if(originalOutcome > 21) values.reduce { acc, curr ->
                if(curr == 11)
                    acc + 1
                else curr + acc
            } else originalOutcome
        }

        val formatter = DecimalFormat("#,###.##")

        var msg: Message? = null

        fun blackjackOutcome(enemy: Boolean, bEvent: ButtonClickEvent? = null) {
            if(enemy)
                main.getEconomy().withdrawPlayer(player, bet)
            else
                main.getEconomy().depositPlayer(player, bet)

            val embed = GetYmlEmbed( { text ->


                val form = setCommandPlaceholders(text, name, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())
                    .replace(if(enemy) "{lose_amount}" else "{win_amount}", formatMoney(bet, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))

                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }, if(enemy) "blackjackCommandBlackjackOutcomeDealerEmbed" else "blackjackCommandBlackjackOutcomePlayerEmbed", main.discordMessagesConfig)

            if(bEvent == null)
                event.replyEmbeds(embed.build()).queue()
            else
                bEvent.editMessageEmbeds(embed.build()).setActionRows().queue()
        }

        fun draw(blackjack: Boolean, bEvent: ButtonClickEvent? = null) {
            val embed = GetYmlEmbed( { text ->

                val form = setCommandPlaceholders(text, name, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())

                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }, if(blackjack) "blackjackCommandDrawBlackjackOutcomeEmbed" else "blackjackCommandDrawOutcomeEmbed", main.discordMessagesConfig)

            if(bEvent == null)
            {
                if(msg == null)
                    event.replyEmbeds(embed.build()).queue()
                else msg!!.editMessageEmbeds(embed.build()).setActionRows().queue()
            }
            else
                bEvent.editMessageEmbeds(embed.build()).setActionRows().queue()
        }

        fun bust(enemy: Boolean, bEvent: ButtonClickEvent? = null) {

            if(enemy)
                main.getEconomy().depositPlayer(player, bet)
            else
                main.getEconomy().withdrawPlayer(player, bet)

            val embed = GetYmlEmbed( { text ->

                val form = setCommandPlaceholders(text, name, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())
                    .replace(if(enemy) "{win_amount}" else "{lose_amount}", formatMoney(bet, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))

                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }, if(enemy) "blackjackCommandBustDealerEmbed" else "blackjackCommandBustPlayerEmbed", main.discordMessagesConfig)

            if(bEvent == null)
                msg!!.editMessageEmbeds(embed.build()).setActionRows().queue()
            else bEvent.editMessageEmbeds(embed.build()).setActionRows().queue()
        }

        fun otherOutcomes(enemy: Boolean, bEvent: ButtonClickEvent? = null) {
            if(enemy)
                main.getEconomy().withdrawPlayer(player, bet)
            else
                main.getEconomy().depositPlayer(player, bet)

            val embed = GetYmlEmbed( { text ->

                val form = setCommandPlaceholders(text, name, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())
                    .replace(if(enemy) "{lose_amount}" else "{win_amount}", formatMoney(bet, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))

                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }, if(enemy) "blackjackCommandDealerWinEmbed" else "blackjackCommandPlayerWinEmbed", main.discordMessagesConfig)

            if(bEvent == null)
                msg!!.editMessageEmbeds(embed.build()).setActionRows().queue()
            else bEvent.editMessageEmbeds(embed.build()).setActionRows().queue()
        }

        var hit: ((bEvent: ButtonClickEvent, messageId: String) -> Unit)? = null

        fun resolveDealerActions(bEvent: ButtonClickEvent? = null): Boolean {
            var failed = false

            do {
                val value = calculateValue(houseCards)
                if(value > 21) {
                    failed = true
                    bust(true, bEvent)
                    break
                }

                if(value == 21) {
                    failed = true
                    blackjackOutcome(true, bEvent)
                    break
                }


                val chance = Random.nextInt(4 .. 21)

                if(chance > value)
                    houseCards.add(currentDeck.removeAt(0))
                else break
            } while (true)

            return failed
        }

        fun stand(bEvent: ButtonClickEvent? = null) {
            val outc = resolveDealerActions(bEvent)
            if(outc) return

            val yourScore = calculateValue(yourCards)
            val dealerScore = calculateValue(houseCards)

            if(yourScore == dealerScore) draw(false, bEvent)
            else if(yourScore > dealerScore) otherOutcomes(false, bEvent)
            else otherOutcomes(true, bEvent)
        }

        fun ShowHand(bEvent: ButtonClickEvent? = null) {
            val embed = GetYmlEmbed( { text ->
                val form = setCommandPlaceholders(text, name, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.slice(1 until houseCards.size).joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards.subList(1, houseCards.size)).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())

                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }, "blackjackCommandShowEmbed", main.discordMessagesConfig)
            if(bEvent == null)
            {
                event.replyEmbeds(embed.build())
                    .addActionRow(mutableListOf(
                        Button.primary("hit", "Hit"),
                        Button.primary("stand", "Stand"),
                        Button.primary("double", "Double down").withDisabled(currentBalance - bet * 2 <= 0)
                    )).queue { interaction ->
                        interaction.retrieveOriginal().queue { message ->
                            msg = message
                            val tmr = Timer("BLTMOT", false).schedule(240000) {
                                stand()
                            }

                            buttonEvents[message.id] = { bevent ->
                                if(bevent.user.id != event.user.id) {
                                    bevent.reply("You may not interact with this menu!").setEphemeral(true).queue()
                                } else {
                                    tmr.cancel()
                                    when (bevent.componentId) {
                                        "hit" -> {
                                            hit!!(bevent, message.id)
                                        }
                                        "stand" -> {
                                            buttonEvents.remove(message.id)
                                            stand(bevent)
                                        }
                                        "double" -> {
                                            buttonEvents.remove(message.id)
                                            bet += bet
                                            yourCards.add(currentDeck.removeAt(0))
                                            val score = calculateValue(yourCards)
                                            if (score == 21) blackjackOutcome(false, bevent)
                                            else if (score > 21) bust(false, bevent)
                                            else stand(bevent)
                                        }
                                    }
                                }
                            }
                        }


                    }
            } else {
                bEvent.editMessageEmbeds(embed.build())
                    .setActionRow(mutableListOf(
                        Button.primary("hit", "Hit"),
                        Button.primary("stand", "Stand"),
                        Button.primary("double", "Double down").withDisabled(currentBalance - bet * 2 <= 0)
                    )).queue()
            }
        }

        hit = { bEvent, msgId ->
            yourCards.add(currentDeck.removeAt(0))

            val score = calculateValue(yourCards)

            if(score == 21) {
                buttonEvents.remove(msgId)
                blackjackOutcome(false, bEvent)
            }
            else if(score > 21) {
                buttonEvents.remove(msgId)
                bust(false, bEvent)
            }
            else {
                ShowHand(bEvent)
            }
        }


        val begCalc = calculateValue(yourCards)
        val begEnCalc = calculateValue(houseCards)

        if(begCalc == 21 && begEnCalc == 21) return draw(true)
        if(begCalc == 21) return blackjackOutcome(false)
        if(begEnCalc == 21) return blackjackOutcome(true)

        ShowHand()
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