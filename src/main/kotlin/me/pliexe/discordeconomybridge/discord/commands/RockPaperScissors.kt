package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.bukkit.Bukkit

//:rock: :page_with_curl: :scissors:

class RockPaperScissors(main: DiscordEconomyBridge): Command(main) {
    override val name: String
        get() = "rockpaperscissors"

    override val description: String
        get() = "Play a game of rock paper scissors against the bot or a player!"

    override val usage: String
        get() = "[user] <bet> [rounds](max 32)"

    override fun getCommandOptions(): CommandOptions {
        return CommandOptions()
            .addOption(OptionType.NUMBER, "bet", "Amount to bet")
            .addOption(OptionType.USER, "user", "The user to play against! (Not required)", false)
            .addOption(OptionType.INTEGER, "rounds", "Amount of rounds. If not set default will be 3!", false)
    }

    override fun run(event: CommandEventData) {
        if(!main.linkHandler.isLinked(event.author.id))
            return fail(event, "Your account is not linked to discord!")

        val bet: Double
        var opponent: DiscordMember? = null
        val rounds: Int

        if(event.isSlashCommand()) {
            opponent = event.getOptionMember("user")
            bet = event.getOptionDouble("bet")!!
            rounds = event.getOptionInt("rounds") ?: 3
        } else {
            if(event.args!!.isEmpty())
                return fail(event, "No argument given for bet or user. If you want to play against the bot just give the bet amount!")

            if(event.memberMentionsSize() > 0)
            {
                opponent = event.getMemberMention(0)
                if(event.args.size < 2)
                    return fail(event, "Bet argument is missing!")

                bet = event.args[1].toDoubleOrNull() ?: return fail(event, "Bet may only be a numeric value!")

                rounds = if(event.args.size > 2)
                    event.args[2].toIntOrNull() ?: return fail(event, "The amount of rounds may only be a natural number!")
                else 3
            } else {
                bet = event.args[0].toDoubleOrNull() ?: return fail(event, "Bet may only be a numeric value!")

                rounds = if(event.args.size > 1)
                    event.args[1].toIntOrNull() ?: return fail(event, "The amount of rounds may only be a natural number!")
                else 3
            }
        }

        if(rounds < 1) return fail(event, "The amount of rounds may not be lower than 1!")
        if(rounds > 32) return fail(event, "The amount of rounds may not be higher than 32!")

        val minBet = if(config.isDouble("minBet")) config.getDouble("minBet") else 100.0

        if(bet < minBet)
            return fail(event, "The wager may not be lower than $minBet")

        val maxBet = if(config.isDouble("maxBet")) config.getDouble("maxBet") else 100000000000000000.0

        if(bet > maxBet)
            return fail(event, "The wager may not be higher than $maxBet")

        var opponentPlayer: UniversalPlayer? = null

        val player = Bukkit.getOfflinePlayer(main.linkHandler.getUuid(event.author.id))

        if(opponent != null)
        {
            if(!main.linkHandler.isLinked(opponent.id))
                return fail(event, "The opponent you challenged does not have his account linked to discord!")
            opponentPlayer = UniversalPlayer(Bukkit.getPlayer(main.linkHandler.getUuid(opponent.id)) ?: Bukkit.getOfflinePlayer(main.linkHandler.getUuid(opponent.id))!!)
        }

        // Rock = 0, Paper = 1, Scissors = 2
        // draw - 0
        // p1 - win = 1
        // p2 - win = 2
        fun outcome(p1: Int, p2: Int): Int {
            return when(p2 - p1) {
                0 -> 0
                1 -> 2
                else -> 1
            }
        }

        val rcpPaper =  getString(main.discordMessagesConfig!!.get("rpsCommand.rpcPaper")) ?: ":page_with_curl:"
        val rcpRock = getString(main.discordMessagesConfig!!.get("rpsCommand.rpcRock")) ?: ":rock:"
        val rcpScissor = getString(main.discordMessagesConfig!!.get("rpsCommand.rpcScissor")) ?: ":scissors:"

        class Round(
            usedStr: String
        ) {
            val used: Int = when(usedStr) {
                "r" -> 0
                "p" -> 1
                else -> 2
            }

            var win: Boolean? = null

            // Rock = 0, Paper = 1, Scissors = 2
            fun toStr(): String {
                return when(used) {
                    0 -> rcpRock
                    1 -> rcpPaper
                    2 -> rcpScissor
                    else -> rcpScissor
                }
            }
        }

        val playerPlayed = mutableListOf<Round>()
        val opponentPlayed = mutableListOf<Round>()

        val noRoundsPlayedYet = getString(main.discordMessagesConfig.get("rpsCommand.noRoundsPlayed")) ?: "Not played yet!"
        val roundWin = getString(main.discordMessagesConfig.get("rpsCommand.roundWin")) ?: ":white_check_mark:"
        val roundText = getString(main.discordMessagesConfig.get("rpsCommand.roundText")) ?: "{picker} {result}"
        val joinedTextForStatus = getString(main.discordMessagesConfig.get("rpsCommand.joinedTextForStatus")) ?: " | "
        val roundLose = getString(main.discordMessagesConfig.get("rpsCommand.roundLose")) ?: ":x:"

        val rcpRockButtonLabel = getString(main.discordMessagesConfig.get("rpsCommand.rpcRockButtonLabel")) ?: "🪨"
        val rcpPaperButtonLabel = getString(main.discordMessagesConfig.get("rpsCommand.rpcPaperButtonLabel")) ?: "📃"
        val rcpScissorButtonLabel = getString(main.discordMessagesConfig.get("rpsCommand.rpcScissorButtonLabel")) ?: "✂️"

        val textLockedIn = getString(main.discordMessagesConfig.get("rpsCommand.textLockedIn")) ?: "Locked in!"
        val textWaitingForLockIn = getString(main.discordMessagesConfig.get("rpsCommand.textWaitingForLockIn")) ?: "Waiting for play..."

        val textAfterLockIn = getString(main.discordMessagesConfig.get("rpsCommand.textAfterLockIn")) ?: "You have already picked your answer"

        var round = 1

        var lockedInP1 = false
        var lockedInP2 = false

        fun formatRounds(rounds: List<Round>): String {
            return if(rounds.isEmpty())
                noRoundsPlayedYet
            else rounds.subList(0, round - 1).joinToString(joinedTextForStatus) {
                roundText
                    .replace("{picked}", it.toStr())
                    .replace("{result}", if (it.win!!) roundWin else roundLose)
            }
        }

        fun calculatePoints(rounds: List<Round>): Int {
            return if(rounds.isEmpty()) 0
            else rounds.subList(0, round - 1).sumBy { if(it.win!!) 1 else 0 }
        }

        fun showMessage(interaction: ComponentInteractionEvent? = null, init: Boolean = false) {
            val embed = if(opponent == null) event.getYMLEmbed("rpsCommand.messages.gameBot", {
                val form = setCommandPlaceholders(
                    it
                        .replace("{rounds_1}", formatRounds(opponentPlayed))
                        .replace("{rounds_2}", formatRounds(playerPlayed))
                        .replace("{round}", round.toString())
                        .replace("{points_1}", calculatePoints(opponentPlayed).toString())
                        .replace("{points_2}", calculatePoints(playerPlayed).toString()),
                    event.prefix, event.commandName, description, name
                )
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }) else event.getYMLEmbed("rpsCommand.messages.game", {
                val form = setCommandPlaceholders(
                    it
                        .replace("{rounds_1}", formatRounds(playerPlayed))
                        .replace("{rounds_2}", formatRounds(opponentPlayed))
                        .replace("{p1_locked_in}", if(lockedInP1) textLockedIn else textWaitingForLockIn)
                        .replace("{p2_locked_in}", if(lockedInP2) textLockedIn else textWaitingForLockIn)
                        .replace("{round}", round.toString())
                        .replace("{points_1}", calculatePoints(playerPlayed).toString())
                        .replace("{points_2}", calculatePoints(opponentPlayed).toString())
                    ,
                    event.prefix, event.commandName, description, name
                )
                setPlaceholdersForDiscordMessage(event.member!!, opponent, UniversalPlayer(player), opponentPlayer!!, form)
            })

            if(init) {
                (interaction?.editMessage(embed)?.setActionRow(mutableListOf(
                    Button.primary("r",rcpRockButtonLabel),
                    Button.primary("p", rcpPaperButtonLabel),
                    Button.primary("s", rcpScissorButtonLabel)
                ))
                    ?: event.sendMessage(embed)
                        .setActionRow(mutableListOf(
                            Button.primary("r",rcpRockButtonLabel),
                            Button.primary("p", rcpPaperButtonLabel),
                            Button.primary("s", rcpScissorButtonLabel)
                        ))).queue { message ->
                            val collector = message.createInteractionCollector(300000, true)

                            collector.onClick = { interactionEvent ->
                                if(opponent == null) {

                                } else {
                                    fun picked() {
                                        if(lockedInP1 && lockedInP2) {
                                            when(outcome(playerPlayed.last().used, opponentPlayed.last().used)) {
                                                0 -> {
                                                    playerPlayed.last().win = true
                                                    opponentPlayed.last().win = true
                                                }
                                                1 -> {
                                                    playerPlayed.last().win = true
                                                    opponentPlayed.last().win = false
                                                }
                                                2 -> {
                                                    playerPlayed.last().win = false
                                                    opponentPlayed.last().win = true
                                                }
                                            }

                                            lockedInP1 = false
                                            lockedInP2 = false
                                            round++
                                        }
                                        showMessage(interactionEvent)
                                    }

                                    when(interactionEvent.user.id) {
                                        event.author.id -> {
                                            if(lockedInP1) {
                                                interactionEvent.replyEphemeral(textAfterLockIn).queue()
                                            } else {
                                                playerPlayed.add(Round(interactionEvent.componentId))
                                                lockedInP1 = true
                                                picked()
                                            }

                                        }
                                        opponent.id -> {
                                            if(lockedInP2)
                                                interactionEvent.replyEphemeral(textAfterLockIn).queue()
                                            else {
                                                opponentPlayed.add(Round(interactionEvent.componentId))
                                                lockedInP2 = true
                                                picked()
                                            }
                                        }
                                        else -> {
                                            sendWrongUserInteractionMessage(interactionEvent)
                                        }
                                    }
                                }
                            }
                        }
            } else {
                interaction!!.editMessage(embed).setActionRow(mutableListOf(
                    Button.primary("r",rcpRockButtonLabel),
                    Button.primary("p", rcpPaperButtonLabel),
                    Button.primary("s", rcpScissorButtonLabel)
                )).queue()
            }
        }

        if(opponent == null) {
            showMessage(null, true)
        } else {
            event.sendYMLEmbed("rpsCommand.messages.challenge", {
                val form = setCommandPlaceholders(it, event.prefix, event.commandName, description, usage)
                setPlaceholdersForDiscordMessage(event.member!!, opponent, UniversalPlayer(player), opponentPlayer!!, form)
                    .replace("{rounds}", rounds.toString())
            }).setYesOrNoButtons(getString(main.discordMessagesConfig.get("rpsCommand.buttonAcceptLabel")) ?: "Accept", getString(main.discordMessagesConfig!!.get("rpsCommand.buttonDeclineLabel")) ?: "Decline")
                .queue { message ->
                    message.awaitYesOrNo(300000, { it.user.id == opponent.id }) { outcome, interaction, deleted ->
                        if(outcome) {
                            showMessage(interaction, true)
                        }
                        else if(!deleted) {
                            if(interaction == null) {
                                message.editMessage(event.getYMLEmbed("rpsCommand.messages.declined", {
                                    val form = setCommandPlaceholders(it, event.prefix, event.commandName, description, usage)
                                    setPlaceholdersForDiscordMessage(event.member!!, opponent, UniversalPlayer(player), opponentPlayer!!, form)
                                })).removeActinRows().queue()
                            } else {
                                interaction.editMessage(event.getYMLEmbed("rpsCommand.messages.declined", {
                                    val form = setCommandPlaceholders(it, event.prefix, event.commandName, description, usage)
                                    setPlaceholdersForDiscordMessage(event.member!!, opponent, UniversalPlayer(player), opponentPlayer!!, form)
                                })).removeActinRows().queue()
                            }

                        }
                    }
            }
        }
    }
}