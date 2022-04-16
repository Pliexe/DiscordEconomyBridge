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

    override val isGame = true

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

        if(opponent != null && opponent.id == event.author.id)
            return fail(event, "You may not play against yourself!")

        if(rounds < 1) return fail(event, "The amount of rounds may not be lower than 1!")
        if(rounds > 32) return fail(event, "The amount of rounds may not be higher than 32!")

        val minBet = main.pluginConfig.minBet

        if(bet < minBet)
            return fail(event, "The wager may not be lower than $minBet")

        val maxBet = main.pluginConfig.maxBet

        if(bet > maxBet)
            return fail(event, "The wager may not be higher than $maxBet")

        var opponentPlayer: UniversalPlayer? = null

        val player = Bukkit.getOfflinePlayer(main.linkHandler.getUuid(event.author.id))

        if(!main.getEconomy().hasAccount(player))
            return fail(event, "You don't have any money in your balance")

        val balance = main.getEconomy().getBalance(player)

        if(bet > balance)
            return fail(event, "You may not bet more than you have money available in your balance!")

        if(opponent != null)
        {
            if(!main.linkHandler.isLinked(opponent.id))
                return fail(event, "The opponent you challenged does not have their account linked to discord!")
            opponentPlayer = UniversalPlayer(Bukkit.getPlayer(main.linkHandler.getUuid(opponent.id)) ?: Bukkit.getOfflinePlayer(main.linkHandler.getUuid(opponent.id))!!)
            if(bet > opponentPlayer.getBalance(main))
                return fail(event, "The opponent does not have enough money to proceed with the bet!")
        } else {
            event.addBets(bet, player)
        }

        event.addCooldowns(event.author.id)
        main.commandHandler.commandComplete(this)

        // Rock = 0, Paper = 1, Scissors = 2
        // draw - 0
        // p1 - win = 1
        // p2 - win = 2
        fun outcome(p1: Int, p2: Int): Int {
            return if((p1+1) % 3 == p2) 2
            else if(p1 == p2) 0
            else 1
        }

        val rcpPaper =  getString(main.discordMessagesConfig.get("rpsCommand.rpcPaper")) ?: ":page_with_curl:"
        val rcpRock = getString(main.discordMessagesConfig.get("rpsCommand.rpcRock")) ?: ":rock:"
        val rcpScissor = getString(main.discordMessagesConfig.get("rpsCommand.rpcScissor")) ?: ":scissors:"

        class Round(
            val used: Int
        ) {
            constructor(usedStr: String): this(when(usedStr) {
                "r" -> 0
                "p" -> 1
                else -> 2
            })

            var win: Boolean? = null

            // Rock = 0, Paper = 1, Scissors = 2
            fun toStr(): String {
                return when(used) {
                    0 -> rcpRock
                    1 -> rcpPaper
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

        fun gameOver(interactionEvent: ComponentInteractionEvent?, playerWins: Boolean, message: Message? = null) {
            if(main.shutingDown) return

            if(opponent == null) {
                if(playerWins) {
                    main.getEconomy().depositPlayer(player, bet * 2)
                }
            } else {
                if(playerWins) {
                    main.getEconomy().depositPlayer(player, bet * 2)
                } else {
                    opponentPlayer!!.depositPlayer(main, bet * 2)
                }
            }

            event.removeBets()

            val embed = if(opponent == null) event.getYMLEmbed(if(playerWins) "rpsCommand.messages.gameOverBotPlayerWin" else "rpsCommand.messages.gameOverBotPlayerLose", {
                val form = setCommandPlaceholders(
                    it
                        .replace("{rounds_1}", formatRounds(opponentPlayed))
                        .replace("{rounds_2}", formatRounds(playerPlayed))
                        .replace("{rounds}", rounds.toString())
                        .replace("{points_1}", calculatePoints(opponentPlayed).toString())
                        .replace("{points_2}", calculatePoints(playerPlayed).toString()),
                    event.prefix, event.commandName, description, name
                )
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }) else event.getYMLEmbed("rpsCommand.messages.gameOver", {
                val form = setCommandPlaceholders(
                    it
                        .replace("{rounds_1}", formatRounds(playerPlayed))
                        .replace("{rounds_2}", formatRounds(opponentPlayed))
                        .replace("{rounds}", rounds.toString())
                        .replace("{points_1}", calculatePoints(playerPlayed).toString())
                        .replace("{points_2}", calculatePoints(opponentPlayed).toString())
                    ,
                    event.prefix, event.commandName, description, name
                )
                if(playerWins)
                {
                    setPlaceholdersForDiscordMessage(opponent, event.member!!, opponentPlayer!!, UniversalPlayer(player),
                        setPlaceholdersForDiscordMessage(event.member!!, opponent, UniversalPlayer(player), opponentPlayer, form.replace("{p1}", "")).replace("{p2}", "")
                    )
                }
                else
                {
                    setPlaceholdersForDiscordMessage(event.member!!, opponent, UniversalPlayer(player), opponentPlayer!!,
                        setPlaceholdersForDiscordMessage(opponent, event.member!!, opponentPlayer, UniversalPlayer(player), form.replace("{p1}", "")).replace("{p2}", "")
                    )
                }
            })

            if(interactionEvent == null)
                message!!.editMessage(embed).removeActinRows().queue()
            else interactionEvent.editMessage(embed).removeActinRows().queue()

            event.resetCooldowns()
        }

        fun gameDraw(interactionEvent: ComponentInteractionEvent?, message: Message? = null) {
            if(main.shutingDown) return

            event.restoreBets()

            val embed = if(opponent == null) event.getYMLEmbed("rpsCommand.messages.drawBot", {
                val form = setCommandPlaceholders(
                    it
                        .replace("{rounds_1}", formatRounds(opponentPlayed))
                        .replace("{rounds_2}", formatRounds(playerPlayed))
                        .replace("{rounds}", rounds.toString())
                        .replace("{points_1}", calculatePoints(opponentPlayed).toString())
                        .replace("{points_2}", calculatePoints(playerPlayed).toString()),
                    event.prefix, event.commandName, description, name
                )
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
            }) else event.getYMLEmbed("rpsCommand.messages.draw", {
                val form = setCommandPlaceholders(
                    it
                        .replace("{rounds_1}", formatRounds(playerPlayed))
                        .replace("{rounds_2}", formatRounds(opponentPlayed))
                        .replace("{rounds}", rounds.toString())
                        .replace("{points_1}", calculatePoints(playerPlayed).toString())
                        .replace("{points_2}", calculatePoints(opponentPlayed).toString())
                    ,
                    event.prefix, event.commandName, description, name
                )
                setPlaceholdersForDiscordMessage(event.member!!, opponent, UniversalPlayer(player), opponentPlayer!!, form)
            })

            if(interactionEvent == null)
                message!!.editMessage(embed).removeActinRows().queue()
            else interactionEvent.editMessage(embed).removeActinRows().queue()

            event.resetCooldowns()
        }


        fun showMessage(interaction: ComponentInteractionEvent? = null, init: Boolean = false) {
            val embed = if(opponent == null) event.getYMLEmbed("rpsCommand.messages.gameBot", {
                val form = setCommandPlaceholders(
                    it
                        .replace("{rounds_1}", formatRounds(opponentPlayed))
                        .replace("{rounds_2}", formatRounds(playerPlayed))
                        .replace("{round}", round.toString())
                        .replace("{rounds}", rounds.toString())
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
                        .replace("{rounds}", rounds.toString())
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
                            val collector = message.createInteractionCollector(main.pluginConfig.gameTimeout, true)

                            fun setOutcome(outcome: Int) {
                                when (outcome) {
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
                            }

                            collector.onDone = { type, _ ->
                                if(type == InteractionCollector.DoneType.Expired) {

                                    if(opponent == null) {
                                        gameOver(null, false, message)
                                    } else {
                                        if(lockedInP1 || lockedInP2) {
                                            if(lockedInP1)
                                                gameOver(null, true, message)
                                            else gameOver(null, false, message)
                                        } else {
                                            val playerP = calculatePoints(playerPlayed)
                                            val opp = calculatePoints(opponentPlayed)

                                            if(playerP > opp)
                                                gameOver(null, true, message)
                                            else if(opp > playerP)
                                                gameOver(null, false, message)
                                            else gameDraw(null, message)
                                        }
                                    }
                                } else if(type == InteractionCollector.DoneType.MessageDeleted) {
                                    event.restoreBets()
                                    event.resetCooldowns()
                                }
                            }

                            collector.onClick = { interactionEvent ->
                                if(opponent == null) {
                                    if(interactionEvent.user.id != event.author.id) {
                                        sendWrongUserInteractionMessage(interactionEvent)
                                    } else {
                                        playerPlayed.add(Round(interactionEvent.componentId))
                                        opponentPlayed.add(Round((0..2).random()))

                                        setOutcome(outcome(playerPlayed.last().used, opponentPlayed.last().used))

                                        round++
                                        if(round > rounds) {
                                            collector.stop()

                                            val playerP = calculatePoints(playerPlayed)
                                            val bot = calculatePoints(opponentPlayed)

                                            if(playerP > bot)
                                                gameOver(interactionEvent, true)
                                            else if(bot > playerP)
                                                gameOver(interactionEvent, false)
                                            else gameDraw(interactionEvent)
                                        } else showMessage(interactionEvent)
                                    }
                                } else {
                                    fun picked() {
                                        if(lockedInP1 && lockedInP2) {
                                            setOutcome(outcome(playerPlayed.last().used, opponentPlayed.last().used))

                                            lockedInP1 = false
                                            lockedInP2 = false

                                            round++
                                            if(round > rounds)
                                            {
                                                collector.stop()

                                                val p1 = calculatePoints(playerPlayed)
                                                val p2 = calculatePoints(opponentPlayed)

                                                if(p1 > p2)
                                                    gameOver(interactionEvent, true)
                                                else if(p2 > p1)
                                                    gameOver(interactionEvent, false)
                                                else gameDraw(interactionEvent)
                                            } else {
                                                showMessage(interactionEvent)
                                            }

                                        } else showMessage(interactionEvent)
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
            }).setYesOrNoButtons(getString(main.discordMessagesConfig.get("rpsCommand.buttonAcceptLabel")) ?: "Accept", getString(main.discordMessagesConfig.get("rpsCommand.buttonDeclineLabel")) ?: "Decline")
                .queue { message ->
                    message.awaitYesOrNo(main.pluginConfig.commandTimeout, { it.user.id == opponent.id }) { outcome, interaction, deleted ->
                        if(outcome) {
                            if(main.commandHandler.isPlaying(opponent.id))
                            {
                                event.resetCooldowns()
                                return@awaitYesOrNo fail(interaction!!, "The user is already playing a game...")
                            }
                            else {
                                if(bet > main.getEconomy().getBalance(player))
                                    return@awaitYesOrNo fail(interaction!!, "${event.author.name} does not have enough money to start the game.")

                                if(bet > opponentPlayer!!.getBalance(main))
                                    return@awaitYesOrNo fail(interaction!!, "${event.author.name} does not have enough money to start the game.")

                                event.addCooldowns(opponent.id)
                                event.addBets(bet, UniversalPlayer(player), opponentPlayer)
                                showMessage(interaction, true)
                            }
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
                            main.commandHandler.commandFail(this)
                            event.resetCooldowns()
                        } else {
                            main.commandHandler.commandFail(this)
                            event.resetCooldowns()
                        }
                    }
            }
        }
    }
}