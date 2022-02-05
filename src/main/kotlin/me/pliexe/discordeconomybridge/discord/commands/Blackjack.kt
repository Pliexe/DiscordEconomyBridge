package me.pliexe.discordeconomybridge.discord.commands
import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.ButtonClickEvent
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent
import github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import me.pliexe.discordeconomybridge.formatMoney
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.bukkit.entity.Player
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

    override val description: String
        get() = "Play a game of blackjack!"

    override fun getCommandOptions(): CommandOptions {
        return CommandOptions()
            .addOption(OptionType.NUMBER, "bet", "Amount to bet")
    }

    override fun run(event: CommandEventData) {
        var bet = if(event.isSlashCommand()) {
            event.getOptionDouble("bet")!!
        } else {
            if(event.args!!.isEmpty())
                return fail(event, "Bet amount was not given!")

            event.args[0].toDoubleOrNull() ?: return fail(event, "Bet may only be an numeric value!")
        }

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

        var msg: me.pliexe.discordeconomybridge.discord.Message? = null

        fun doTransaction(won: Boolean) {
            val onlinePlayer = main.server.getPlayer(uuid)
            if(onlinePlayer == null) {
                val offlinePlayer = main.server.getOfflinePlayer(uuid)
                if(won)
                    main.getEconomy().depositPlayer(offlinePlayer, bet)
                else main.getEconomy().withdrawPlayer(offlinePlayer, bet)
            } else {
                if(won)
                    main.getEconomy().depositPlayer(onlinePlayer, bet)
                else main.getEconomy().withdrawPlayer(onlinePlayer, bet)
            }
        }

        fun blackjackOutcome(enemy: Boolean, bEvent: ComponentInteractionEvent? = null) {
            doTransaction(!enemy)

            val embed = event.getYMLEmbed(if(enemy) "blackjackCommandBlackjackOutcomeDealerEmbed" else "blackjackCommandBlackjackOutcomePlayerEmbed", { text ->


                val form = setCommandPlaceholders(text, event.prefix, event.commandName, description, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())
                    .replace(if(enemy) "{lose_amount}" else "{win_amount}", formatMoney(bet, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))

                if(event.member == null)
                    setPlaceholdersForDiscordMessage(event.user, UniversalPlayer(player), form)
                else setPlaceholdersForDiscordMessage(event.member, player, form)
            })

            if(bEvent == null)
                event.sendEmbed(embed).queue()
            else
                bEvent.editEmbed(embed).removeActinRows().queue()
        }

        fun draw(blackjack: Boolean, bEvent: ComponentInteractionEvent? = null) {
            val embed = event.getYMLEmbed(if(blackjack) "blackjackCommandDrawBlackjackOutcomeEmbed" else "blackjackCommandDrawOutcomeEmbed", { text ->

                val form = setCommandPlaceholders(text, event.prefix, event.commandName, description, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())

                if(event.member == null)
                    setPlaceholdersForDiscordMessage(event.user, UniversalPlayer(player), form)
                else
                    setPlaceholdersForDiscordMessage(event.member, player, form)
            })

            if(bEvent == null)
            {
                if(msg == null)
                    event.sendEmbed(embed).queue()
                else msg!!.editEmbed(embed).removeActinRows().queue()
            }
            else
                bEvent.editEmbed(embed).removeActinRows().queue()
        }

        fun bust(enemy: Boolean, bEvent: ComponentInteractionEvent? = null) {

            doTransaction(enemy)

            val embed = event.getYMLEmbed( if(enemy) "blackjackCommandBustDealerEmbed" else "blackjackCommandBustPlayerEmbed", { text ->

                val form = setCommandPlaceholders(text, event.prefix, event.commandName, description, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())
                    .replace(if(enemy) "{win_amount}" else "{lose_amount}", formatMoney(bet, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))

                if(event.member == null)
                    setPlaceholdersForDiscordMessage(event.user, UniversalPlayer(player), form)
                else
                    setPlaceholdersForDiscordMessage(event.member, player, form)
            })

            if(bEvent == null)
                msg!!.editEmbed(embed).removeActinRows().queue()
            else bEvent.editEmbed(embed).removeActinRows().queue()
        }

        fun otherOutcomes(enemy: Boolean, bEvent: ComponentInteractionEvent? = null) {

            doTransaction(!enemy)

            val embed = event.getYMLEmbed(if(enemy) "blackjackCommandDealerWinEmbed" else "blackjackCommandPlayerWinEmbed", { text ->

                val form = setCommandPlaceholders(text, event.prefix, event.commandName, description, usage)
                    .replace("{your_cards}", yourCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards}", houseCards.joinToString(" ") { it.emote })
                    .replace("{enemy_cards_value}", calculateValue(houseCards).toString())
                    .replace("{your_cards_value}", calculateValue(yourCards).toString())
                    .replace(if(enemy) "{lose_amount}" else "{win_amount}", formatMoney(bet, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))

                if(event.member == null)
                    setPlaceholdersForDiscordMessage(event.user, UniversalPlayer(player), form)
                else
                    setPlaceholdersForDiscordMessage(event.member, player, form)
            })

            if(bEvent == null)
                msg!!.editEmbed(embed).removeActinRows().queue()
            else bEvent.editEmbed(embed).removeActinRows().queue()
        }

        fun resolveDealerActions(bEvent: ComponentInteractionEvent? = null): Boolean {
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

        fun stand(bEvent: ComponentInteractionEvent? = null) {
            val outc = resolveDealerActions(bEvent)
            if(outc) return

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
                    setPlaceholdersForDiscordMessage(event.member, player, form)
            })

            if(bEvent == null)
            {
                event.sendEmbed(embed)
                    .setActionRow(mutableListOf(
                        Button.primary("hit", "Hit"),
                        Button.primary("stand", "Stand"),
                        Button.primary("double", "Double Down", currentBalance - bet * 2 <= 0)
                    )).queue { message ->
                        msg = message

                        val collector = message.createInteractionCollector(300000, true)

                        fun hit(bEvent: ComponentInteractionEvent, messageId: String) {
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
                            when(interaction.componentId) {
                                "hit" -> {
                                    hit(interaction, message.id)
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
                                    if(score == 21) blackjackOutcome(false, interaction)
                                    else if(score > 21) bust(false, interaction)
                                    else stand(interaction)
                                }
                            }
                        }

                        collector.onDone = { doneType, _ ->
                            if(doneType == InteractionCollector.DoneType.Expired)
                                stand()
                        }
                    }
            } else {
                bEvent.editEmbed(embed)
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