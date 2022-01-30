package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.Command
import me.pliexe.discordeconomybridge.discord.GetYmlEmbed
import me.pliexe.discordeconomybridge.discord.setDiscordPlaceholders
import me.pliexe.discordeconomybridge.discord.setPlaceholdersForDiscordMessage
import me.pliexe.discordeconomybridge.formatMoney
import me.pliexe.discordeconomybridge.getEmbedFromYml
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.text.DecimalFormat

class Leaderboard(main: DiscordEconomyBridge): Command(main) {
    override val usage: String
        get() = ""

    override fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        if(main.server.offlinePlayers.isEmpty() && main.server.onlinePlayers.isEmpty())
            return fail(event, "There is no one to show on the leaderboard!")

        val formatter = DecimalFormat("#,###.##")
        val currency = config.getString("Currency")
        val leftSided = config.getBoolean("CurrencyLeftSide")

        val players = main.server.offlinePlayers.filter { main.getEconomy().hasAccount(it) }.map { User(main.getEconomy().getBalance(it), it) }.sortedByDescending { it.money }



//                getEmbedFromYml(config, "leaderboardCommandEmbed", text -> "hi", )

        val embedNameTemplate = if(config.isString("leaderboardCommandEmbed.fieldRepeatName")) config.getString("leaderboardCommandEmbed.fieldRepeatName") else null
        val embedValueTemplate = if(config.isString("leaderboardCommandEmbed.fieldRepeatValue")) config.getString("leaderboardCommandEmbed.fieldRepeatValue") else null
        val inline = if(config.isBoolean("leaderboardCommandEmbed.fieldRepeatInline")) config.getBoolean("leaderboardCommandEmbed.fieldRepeatInline") else false

        val embedCanBeSet = embedNameTemplate != null && embedValueTemplate != null

        val descCanBeSet = config.isString("leaderboardCommandEmbed.descriptionRepeat")

        if(!embedCanBeSet && !descCanBeSet) {
            event.channel.sendMessage("Invalid yaml configuration. Either description or embed field must be set!")
            return
        }

        var text: MutableList<String>? = null

        var leaderboardLimit = if (config.isInt("leaderboardSlots")) config.getInt("leaderboardSlots") else 10

        if(leaderboardLimit > players.size) leaderboardLimit = players.size

        val embed2 = getEmbedFromYml(config, "leaderboardCommandEmbed", { text2 -> text2 }, null, !descCanBeSet)

        val embed = GetYmlEmbed(event.channel, {
            setDiscordPlaceholders(event.member!!, it)
        }, "leaderboardCommandEmbed", main.discordMessagesConfig)

        for(index in 0 until leaderboardLimit)
        {
            if(descCanBeSet)
            {
                if(text == null)
                    text = ArrayList()

                text.add(
                    setPlaceholdersForDiscordMessage(event.member!!, players[index].player, config.getString("leaderboardCommandEmbed.descriptionRepeat"))
                    .replace("%custom_vault_eco_balance%", formatMoney(players[index].money, currency, leftSided, formatter))
                    .replace("{index}", (index+1).toString()))
            }

            if(embedCanBeSet) {
                embed.addField(
                    setPlaceholdersForDiscordMessage(event.member!!, players[index].player, embedNameTemplate!!)
                        .replace("%custom_vault_eco_balance%", formatMoney(players[index].money, currency, leftSided, formatter))
                        .replace("{index}", (index+1).toString()),
                    setPlaceholdersForDiscordMessage(event.member!!, players[index].player, embedNameTemplate!!)
                        .replace("%custom_vault_eco_balance%", formatMoney(players[index].money, currency, leftSided, formatter))
                        .replace("{index}", (index+1).toString()),
                    inline
                )
            }
        }

        if(!text.isNullOrEmpty()) embed.setDescription(text.joinToString("\n"))

        event.channel.sendMessageEmbeds(embed.build()).queue()
    }
}

private class User (
    val money: Double,
    val player: OfflinePlayer
)