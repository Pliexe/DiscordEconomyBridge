package me.pliexe.discordeconomybridge.discord.commands
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import me.pliexe.discordeconomybridge.formatMoney
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.text.DecimalFormat

class Card (
    val value: Int,
    val emote: String
)

private val template = arrayOf(
    Card(1, "A:spades:"), Card(2, "2:spades:"), Card(3, "3:spades:"), Card(4, "4:spades:"), Card(5, "5:spades:"), Card(6, "6:spades:"), Card(7, "7:spades:"), Card(8, "8:spades:"), Card(9, "9:spades:"), Card(10, "10:spades:"), Card(10, "j:spades:"), Card(10, "q:spades:"), Card(10, "k:spades:"),
    Card(1, "A:clubs:"), Card(2, "2:clubs:"), Card(3, "3:clubs:"), Card(4, "4:clubs:"), Card(5, "5:clubs:"), Card(6, "6:clubs:"), Card(7, "7:clubs:"), Card(8, "8:clubs:"), Card(9, "9:clubs:"), Card(10, "10:clubs:"), Card(10, "j:clubs:"), Card(10, "q:clubs:"), Card(10, "k:clubs:"),
    Card(1, "A:diamonds:"), Card(2, "2:diamonds:"), Card(3, "3:diamonds:"), Card(4, "4:diamonds:"), Card(5, "5:diamonds:"), Card(6, "6:diamonds:"), Card(7, "7:diamonds:"), Card(8, "8:diamonds:"), Card(9, "9:diamonds:"), Card(10, "10:diamonds:"), Card(10, "j:diamonds:"), Card(10, "q:diamonds:"), Card(10, "k:diamonds:"),
    Card(1, "A:hearts:"), Card(2, "2:hearts:"), Card(3, "3:hearts:"), Card(4, "4:hearts:"), Card(5, "5:hearts:"), Card(6, "6:hearts:"), Card(7, "7:hearts:"), Card(8, "8:hearts:"), Card(9, "9:hearts:"), Card(10, "10:hearts:"), Card(10, "j:hearts:"), Card(10, "q:hearts:"), Card(10, "k:hearts:")
)

class Blackjack(main: DiscordEconomyBridge) : Command(main) {
    override val name: String
        get() = "blackjack"

    override val usage: String
        get() = "bet"

    override val description: String
        get() = "Play a game of blackjack!"

    override fun getCommandOptions(): CommandOptions {
        return CommandOptions()
            .addOption(OptionType.NUMBER, "bet", "Amount to bet")
    }

    override val isGame = true

    override fun run(event: CommandEventData) {
        var bet = if(event.isSlashCommand()) {
            event.getOptionDouble("bet")!!
        } else {
            if(event.args!!.isEmpty())
                return fail(event, "Bet amount was not given!")

            event.args[0].toDoubleOrNull() ?: return fail(event, "Bet may only be an numeric value!")
        }

        val minBet = main.pluginConfig.minBet

        if(bet < minBet)
            return fail(event, "The wager may not be lower than $minBet")

        val maxBet = main.pluginConfig.maxBet

        if(bet > maxBet)
            return fail(event, "The wager may not be higher than $maxBet")

        val uuid = main.linkHandler.getUuid(event.author.id)
            ?: return fail(event, "Your account is not linked!")

        val player = server.getOfflinePlayer(uuid)

        if(!main.getEconomy().hasAccount(player))
            main.getEconomy().createPlayerAccount(player)

        val currentBalance = main.getEconomy().getBalance(player)

        if(bet > currentBalance)
            return fail(event, "You don't have enough money to bet the amount specified!")

        event.addCooldowns(event.author.id)
        event.addBets(bet, player)

        val currentDeck = template.toMutableList()
        currentDeck.shuffle()

        val yourCards = mutableListOf(currentDeck.removeAt(0), currentDeck.removeAt(0))
        val houseCards =  mutableListOf(currentDeck.removeAt(0), currentDeck.removeAt(0))

        fun calculateValue(array: List<Card>): Int {
            var soft = false
            var value = 0
            array.forEach { card ->
                if(card.value == 1 && !soft)
                {
                    value += 11
                    soft = true
                } else value += card.value
            }
            if(soft && value > 21) value -= 10
            return value
        }


        val formatter = DecimalFormat("#,###.##")

        var msg: Message? = null

        fun doTransaction(won: Boolean) {
            if(main.shutingDown) return
            if(won)
                main.getEconomy().depositPlayer(player, bet * 2)
            event.removeBets()
            event.resetCooldowns()
        }

        fun blackjackOutcome(enemy: Boolean, bEvent: ComponentInteractionEvent? = null) {
            doTransaction(!enemy)

            val embed = event.getYMLEmbed(if(enemy) "blackjackCommandBlackjackOutcomeDealerEmbed" else "blackjackCommandBlackjackOutcomePlayerEmbed", { text ->


                val form = setCommandPlaceholders(text, event.prefix, event.commandName, description, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())
                    .replace(if(enemy) "{lose_amount}" else "{win_amount}", formatMoney(bet, main.pluginConfig.currency, main.pluginConfig.currencyLeftSide, formatter))

                if(event.member == null)
                    setPlaceholdersForDiscordMessage(event.user, UniversalPlayer(player), form)
                else setPlaceholdersForDiscordMessage(event.member!!, player, form)
            })

            if(bEvent == null)
                event.sendMessage(embed).queue()
            else
                bEvent.editMessage(embed).removeActinRows().queue()
        }

        fun draw(blackjack: Boolean, bEvent: ComponentInteractionEvent? = null) {
            if(main.shutingDown) return

            event.restoreBets()

            val embed = event.getYMLEmbed(if(blackjack) "blackjackCommandDrawBlackjackOutcomeEmbed" else "blackjackCommandDrawOutcomeEmbed", { text ->

                val form = setCommandPlaceholders(text, event.prefix, event.commandName, description, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())

                if(event.member == null)
                    setPlaceholdersForDiscordMessage(event.user, UniversalPlayer(player), form)
                else
                    setPlaceholdersForDiscordMessage(event.member!!, player, form)
            })

            if(bEvent == null)
            {
                if(msg == null)
                    event.sendMessage(embed).queue()
                else msg!!.editMessage(embed).removeActinRows().queue()
            }
            else
                bEvent.editMessage(embed).removeActinRows().queue()

            event.resetCooldowns()
        }

        fun bust(enemy: Boolean, bEvent: ComponentInteractionEvent? = null) {

            doTransaction(enemy)

            val embed = event.getYMLEmbed( if(enemy) "blackjackCommandBustDealerEmbed" else "blackjackCommandBustPlayerEmbed", { text ->

                val form = setCommandPlaceholders(text, event.prefix, event.commandName, description, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())
                    .replace(if(enemy) "{win_amount}" else "{lose_amount}", formatMoney(bet, main.pluginConfig.currency, main.pluginConfig.currencyLeftSide, formatter))

                if(event.member == null)
                    setPlaceholdersForDiscordMessage(event.user, UniversalPlayer(player), form)
                else
                    setPlaceholdersForDiscordMessage(event.member!!, player, form)
            })

            if(bEvent == null)
                msg!!.editMessage(embed).removeActinRows().queue()
            else bEvent.editMessage(embed).removeActinRows().queue()
        }

        fun otherOutcomes(enemy: Boolean, bEvent: ComponentInteractionEvent? = null) {

            doTransaction(!enemy)

            val embed = event.getYMLEmbed(if(enemy) "blackjackCommandDealerWinEmbed" else "blackjackCommandPlayerWinEmbed", { text ->

                val form = setCommandPlaceholders(text, event.prefix, event.commandName, description, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())
                    .replace(if(enemy) "{lose_amount}" else "{win_amount}", formatMoney(bet, main.pluginConfig.currency, main.pluginConfig.currencyLeftSide, formatter))

                if(event.member == null)
                    setPlaceholdersForDiscordMessage(event.user, UniversalPlayer(player), form)
                else
                    setPlaceholdersForDiscordMessage(event.member!!, player, form)
            })

            if(bEvent == null)
                msg!!.editMessage(embed).removeActinRows().queue()
            else bEvent.editMessage(embed).removeActinRows().queue()
        }

        fun resolveDealerActions(): Int {
            var cardsValue: Int

            while(calculateValue(houseCards).also { cardsValue = it } < 17) {
                houseCards.add(currentDeck.removeAt(0))
            }
            return cardsValue
        }

        fun stand(bEvent: ComponentInteractionEvent? = null) {
            val dealerValue = resolveDealerActions()

            if(dealerValue > 21) return bust(true, bEvent)
            else if(dealerValue == 21) return blackjackOutcome(true, bEvent)

            val yourScore = calculateValue(yourCards)
            val dealerScore = calculateValue(houseCards)

            if(yourScore == dealerScore) draw(false, bEvent)
            else if(yourScore > dealerScore) otherOutcomes(false, bEvent)
            else otherOutcomes(true, bEvent)
        }

        val begCalc = calculateValue(yourCards)
        val begEnCalc = calculateValue(houseCards)

        if(begCalc == 21 && begEnCalc == 21) return draw(true)
        if(begCalc == 21) return blackjackOutcome(false)
        if(begEnCalc == 21) return blackjackOutcome(true)

        fun ShowHand(bEvent: ComponentInteractionEvent? = null) {
            val embed = event.getYMLEmbed("blackjackCommandShowEmbed", {
                val form = setCommandPlaceholders(it, event.prefix, event.commandName, description, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.slice(1 until houseCards.size).joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards.subList(1, houseCards.size)).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())

                if(event.member == null)
                    setPlaceholdersForDiscordMessage(event.user, UniversalPlayer(player), form)
                else
                    setPlaceholdersForDiscordMessage(event.member!!, player, form)
            })

            if(bEvent == null)
            {
                event.sendMessage(embed)
                    .setActionRow(mutableListOf(
                        Button.primary("hit", getStringOrStringList("blackjackButtonHitLabel", main.discordMessagesConfig) ?: "Hit"),
                        Button.primary("stand", getStringOrStringList("blackjackButtonStandLabel", main.discordMessagesConfig) ?: "Stand"),
                        Button.primary("double", getStringOrStringList("blackjackButtonDoubleDownLabel", main.discordMessagesConfig) ?: "Double Down", currentBalance - bet * 2 <= 0)
                    )).queue { message ->
                        msg = message

                        val collector = message.createInteractionCollector(main.pluginConfig.gameTimeout, true)

                        fun hit(bEvent: ComponentInteractionEvent) {
                            yourCards.add(currentDeck.removeAt(0))
                            val score = calculateValue(yourCards)

                            if(score == 21) {
                                collector.stop()
                                blackjackOutcome(false, bEvent)
                            } else if(score > 21) {
                                collector.stop()
                                bust(false, bEvent)
                            } else ShowHand(bEvent)
                        }

                        collector.onClick = { interaction ->
                            if(interaction.user.id != event.author.id) {
                                interaction.replyEphemeral("You may not interact with this menu!").queue()
                            } else {
                                when (interaction.componentId) {
                                    "hit" -> {
                                        hit(interaction)
                                    }
                                    "stand" -> {
                                        collector.stop()
                                        stand(interaction)
                                    }
                                    "double" -> {
                                        collector.stop()
                                        bet += bet
                                        yourCards.add(currentDeck.removeAt(0))
                                        val score = calculateValue(yourCards)
                                        if (score == 21) blackjackOutcome(false, interaction)
                                        else if (score > 21) bust(false, interaction)
                                        else stand(interaction)
                                    }
                                }
                            }
                        }

                        collector.onDone = { doneType, _ ->
                            if(doneType == InteractionCollector.DoneType.Expired)
                                stand()
                        }
                    }
            } else {
                bEvent.editMessage(embed)
                    .setActionRow(mutableListOf(
                        Button.primary("hit", "Hit"),
                        Button.primary("stand", "Stand"),
                        Button.primary("double", "Double Down", currentBalance - bet * 2 <= 0)
                    )).queue()
            }
        }

        ShowHand()
    }
}