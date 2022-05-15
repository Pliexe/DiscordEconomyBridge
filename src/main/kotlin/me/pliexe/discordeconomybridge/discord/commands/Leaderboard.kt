package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import me.pliexe.discordeconomybridge.formatMoney
import org.bukkit.OfflinePlayer
import java.text.DecimalFormat

class Leaderboard(main: DiscordEconomyBridge): Command(main) {
    override val usage: String
        get() = ""

    override val name: String
        get() = "leaderboard"

    override val description: String
        get() = "Shows the top ${if (config.isInt("leaderboardSlots")) config.getInt("leaderboardSlots") else 10} players"

    override fun getCommandOptions(): CommandOptions {
        return CommandOptions()
    }


    override fun run(event: CommandEventData) {
        if(main.server.offlinePlayers.isEmpty() && main.server.onlinePlayers.isEmpty())
            return fail(event, "There is no one to show on the leaderboard!")

        val formatter = DecimalFormat("#,###.##")
        val currency = main.pluginConfig.currency
        val leftSided = main.pluginConfig.currencyLeftSide

        val players = main.server.offlinePlayers.filter { main.getEconomy().hasAccount(it) }.map { User(main.getEconomy().getBalance(it), it) }.sortedByDescending { it.money }



//                getEmbedFromYml(config, "leaderboardCommandEmbed", text -> "hi", )

        val embedNameTemplate = getString(main.discordMessagesConfig.get("leaderboardCommandEmbed.fieldRepeatName"))
        val embedValueTemplate = getStringOrStringList("leaderboardCommandEmbed.fieldRepeatValue", main.discordMessagesConfig)
        val inline = getBool("leaderboardCommandEmbed.fieldRepeatInline") ?: false

        val embedCanBeSet = embedNameTemplate != null && embedValueTemplate != null

        val descCanBeSet = getStringOrStringList("leaderboardCommandEmbed.descriptionRepeat", main.discordMessagesConfig) != null

        if(!embedCanBeSet && !descCanBeSet)
            return fail(event,"Invalid yaml configuration. Either description or embed field must be set!")

        var text: MutableList<String>? = null

        var leaderboardLimit = if (config.isInt("leaderboardSlots")) config.getInt("leaderboardSlots") else 10

        if(leaderboardLimit > players.size) leaderboardLimit = players.size

        val embed = event.getYMLEmbed("leaderboardCommandEmbed", {
            setDiscordPlaceholders(event.member!!, setCommandPlaceholders(it, event.prefix, event.commandName, description, usage))
        })

        fun Placeholders(text: String, index: Int, player: OfflinePlayer): String {
            return setPlaceholdersForDiscordMessage(event.member!!, player, setCommandPlaceholders(text, event.prefix, event.commandName, description, usage))
                .replace("%custom_vault_eco_balance%", formatMoney(main.getEconomy().getBalance(player), currency, leftSided, formatter))
                .replace("{index}", (index+1).toString())
        }

        for(index in 0 until leaderboardLimit)
        {
            if(descCanBeSet)
            {
                if(text == null)
                    text = ArrayList()

                text.add(Placeholders(main.discordMessagesConfig.getString("leaderboardCommandEmbed.descriptionRepeat"), index, players[index].player))
            }

            if(embedCanBeSet) {
                embed.addField(
                    Placeholders(embedNameTemplate!!, index, players[index].player),
                    Placeholders(embedValueTemplate!!, index, players[index].player),
                    inline
                )
            }
        }

        if(!text.isNullOrEmpty()) embed.setDescription(text.joinToString("\n"))

        event.sendMessage(embed).queue()

        main.commandHandler.commandComplete(this, event)
    }
}

private class User (
    val money: Double,
    val player: OfflinePlayer
)