package me.pliexe.discordeconomybridge.discord.commands

import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.*
import me.pliexe.discordeconomybridge.formatMoney
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.bukkit.OfflinePlayer
import java.text.DecimalFormat

class Leaderboard(main: DiscordEconomyBridge): Command(main) {
    override val usage: String
        get() = ""

    override val name: String
        get() = "leaderboard"

    override fun getSlashCommandData(): CommandData {
        return CommandData(name, "Shows the top ${if (config.isInt("leaderboardSlots")) config.getInt("leaderboardSlots") else 10} players")
    }

    override fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        if(main.server.offlinePlayers.isEmpty() && main.server.onlinePlayers.isEmpty())
            return fail(event, "There is no one to show on the leaderboard!")

        val formatter = DecimalFormat("#,###.##")
        val currency = config.getString("Currency")
        val leftSided = config.getBoolean("CurrencyLeftSide")

        val players = main.server.offlinePlayers.filter { main.getEconomy().hasAccount(it) }.map { User(main.getEconomy().getBalance(it), it) }.sortedByDescending { it.money }



//                getEmbedFromYml(config, "leaderboardCommandEmbed", text -> "hi", )

        val embedNameTemplate = if(main.discordMessagesConfig.isString("leaderboardCommandEmbed.fieldRepeatName")) config.getString("leaderboardCommandEmbed.fieldRepeatName") else null
        val embedValueTemplate = if(main.discordMessagesConfig.isString("leaderboardCommandEmbed.fieldRepeatValue")) config.getString("leaderboardCommandEmbed.fieldRepeatValue") else null
        val inline = if(main.discordMessagesConfig.isBoolean("leaderboardCommandEmbed.fieldRepeatInline")) config.getBoolean("leaderboardCommandEmbed.fieldRepeatInline") else false

        val embedCanBeSet = embedNameTemplate != null && embedValueTemplate != null

        val descCanBeSet = main.discordMessagesConfig.isString("leaderboardCommandEmbed.descriptionRepeat")

        if(!embedCanBeSet && !descCanBeSet) {
            event.channel.sendMessage("Invalid yaml configuration. Either description or embed field must be set!")
            return
        }

        var text: MutableList<String>? = null

        var leaderboardLimit = if (config.isInt("leaderboardSlots")) config.getInt("leaderboardSlots") else 10

        if(leaderboardLimit > players.size) leaderboardLimit = players.size

        val embed = LegacyGetYmlEmbed({
            setDiscordPlaceholders(event.member!!, setCommandPlaceholders(it, prefix, name, usage))
        }, "leaderboardCommandEmbed", main.discordMessagesConfig)

        for(index in 0 until leaderboardLimit)
        {
            if(descCanBeSet)
            {
                if(text == null)
                    text = ArrayList()

                text.add(
                    setPlaceholdersForDiscordMessage(event.member!!, players[index].player, setCommandPlaceholders(main.discordMessagesConfig.getString("leaderboardCommandEmbed.descriptionRepeat"), prefix, name, usage))
                    .replace("%custom_vault_eco_balance%", formatMoney(players[index].money, currency, leftSided, formatter))
                    .replace("{index}", (index+1).toString()))
            }

            if(embedCanBeSet) {
                embed.addField(
                    setPlaceholdersForDiscordMessage(event.member!!, players[index].player, setCommandPlaceholders(embedNameTemplate!!, prefix, name, usage))
                        .replace("%custom_vault_eco_balance%", formatMoney(players[index].money, currency, leftSided, formatter))
                        .replace("{index}", (index+1).toString()),
                    setPlaceholdersForDiscordMessage(event.member!!, players[index].player, setCommandPlaceholders(embedValueTemplate!!, prefix, name, usage))
                        .replace("%custom_vault_eco_balance%", formatMoney(players[index].money, currency, leftSided, formatter))
                        .replace("{index}", (index+1).toString()),
                    inline
                )
            }
        }

        if(!text.isNullOrEmpty()) embed.setDescription(text.joinToString("\n"))

        event.channel.sendMessageEmbeds(embed.build()).queue()
    }

    override fun run(event: github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        if(main.server.offlinePlayers.isEmpty())
            return fail(event, "There is no one to show on the leaderboard!")

        val formatter = DecimalFormat("#,###.##")
        val currency = config.getString("Currency")
        val leftSided = config.getBoolean("CurrencyLeftSide")

        val players = main.server.offlinePlayers.filter { main.getEconomy().hasAccount(it) }.map { User(main.getEconomy().getBalance(it), it) }.sortedByDescending { it.money }

//                getEmbedFromYml(config, "leaderboardCommandEmbed", text -> "hi", )

        val embedNameTemplate = if(main.discordMessagesConfig.isString("leaderboardCommandEmbed.fieldRepeatName")) config.getString("leaderboardCommandEmbed.fieldRepeatName") else null
        val embedValueTemplate = if(main.discordMessagesConfig.isString("leaderboardCommandEmbed.fieldRepeatValue")) config.getString("leaderboardCommandEmbed.fieldRepeatValue") else null
        val inline = if(main.discordMessagesConfig.isBoolean("leaderboardCommandEmbed.fieldRepeatInline")) config.getBoolean("leaderboardCommandEmbed.fieldRepeatInline") else false

        val embedCanBeSet = embedNameTemplate != null && embedValueTemplate != null

        val descCanBeSet = main.discordMessagesConfig.isString("leaderboardCommandEmbed.descriptionRepeat")

        if(!embedCanBeSet && !descCanBeSet) {
            event.channel.sendMessage("Invalid yaml configuration. Either description or embed field must be set!")
            return
        }

        var text: MutableList<String>? = null

        var leaderboardLimit = if (config.isInt("leaderboardSlots")) config.getInt("leaderboardSlots") else 10

        if(leaderboardLimit > players.size) leaderboardLimit = players.size

        val embed = GetYmlEmbed( {
            setDiscordPlaceholders(event.member!!, setCommandPlaceholders(it, prefix, name, usage))
        }, "leaderboardCommandEmbed", main.discordMessagesConfig)

        for(index in 0 until leaderboardLimit)
        {
            if(descCanBeSet)
            {
                if(text == null)
                    text = ArrayList()

                text.add(
                    setPlaceholdersForDiscordMessage(event.member!!, players[index].player, setCommandPlaceholders(main.discordMessagesConfig.getString("leaderboardCommandEmbed.descriptionRepeat"), prefix, name, usage))
                        .replace("%custom_vault_eco_balance%", formatMoney(players[index].money, currency, leftSided, formatter))
                        .replace("{index}", (index+1).toString()))
            }

            if(embedCanBeSet) {
                embed.addField(
                    setPlaceholdersForDiscordMessage(event.member!!, players[index].player, setCommandPlaceholders(embedNameTemplate!!, prefix, name, usage))
                        .replace("%custom_vault_eco_balance%", formatMoney(players[index].money, currency, leftSided, formatter))
                        .replace("{index}", (index+1).toString()),
                    setPlaceholdersForDiscordMessage(event.member!!, players[index].player, setCommandPlaceholders(embedValueTemplate!!, prefix, name, usage))
                        .replace("%custom_vault_eco_balance%", formatMoney(players[index].money, currency, leftSided, formatter))
                        .replace("{index}", (index+1).toString()),
                    inline
                )
            }
        }

        if(!text.isNullOrEmpty()) embed.setDescription(text.joinToString("\n"))

        event.channel.sendMessageEmbeds(embed.build()).queue()
    }

    override fun run(event: SlashCommandEvent) {
        if(main.server.offlinePlayers.isEmpty())
            return fail(event, "There is no one to show on the leaderboard!")

        val formatter = DecimalFormat("#,###.##")
        val currency = config.getString("Currency")
        val leftSided = config.getBoolean("CurrencyLeftSide")

        val players = main.server.offlinePlayers.filter { main.getEconomy().hasAccount(it) }.map { User(main.getEconomy().getBalance(it), it) }.sortedByDescending { it.money }

//                getEmbedFromYml(config, "leaderboardCommandEmbed", text -> "hi", )

        val embedNameTemplate = if(main.discordMessagesConfig.isString("leaderboardCommandEmbed.fieldRepeatName")) config.getString("leaderboardCommandEmbed.fieldRepeatName") else null
        val embedValueTemplate = if(main.discordMessagesConfig.isString("leaderboardCommandEmbed.fieldRepeatValue")) config.getString("leaderboardCommandEmbed.fieldRepeatValue") else null
        val inline = if(main.discordMessagesConfig.isBoolean("leaderboardCommandEmbed.fieldRepeatInline")) config.getBoolean("leaderboardCommandEmbed.fieldRepeatInline") else false

        val embedCanBeSet = embedNameTemplate != null && embedValueTemplate != null

        val descCanBeSet = main.discordMessagesConfig.isString("leaderboardCommandEmbed.descriptionRepeat")

        if(!embedCanBeSet && !descCanBeSet) {
            event.channel.sendMessage("Invalid yaml configuration. Either description or embed field must be set!")
            return
        }

        var text: MutableList<String>? = null

        var leaderboardLimit = if (config.isInt("leaderboardSlots")) config.getInt("leaderboardSlots") else 10

        if(leaderboardLimit > players.size) leaderboardLimit = players.size

        val embed = GetYmlEmbed( {
            setDiscordPlaceholders(event.member!!, setCommandPlaceholders(it, name, usage))
        }, "leaderboardCommandEmbed", main.discordMessagesConfig)

        for(index in 0 until leaderboardLimit)
        {
            if(descCanBeSet)
            {
                if(text == null)
                    text = ArrayList()

                text.add(
                    setPlaceholdersForDiscordMessage(event.member!!, players[index].player, setCommandPlaceholders(main.discordMessagesConfig.getString("leaderboardCommandEmbed.descriptionRepeat"), name, usage))
                        .replace("%custom_vault_eco_balance%", formatMoney(players[index].money, currency, leftSided, formatter))
                        .replace("{index}", (index+1).toString()))
            }

            if(embedCanBeSet) {
                embed.addField(
                    setPlaceholdersForDiscordMessage(event.member!!, players[index].player, setCommandPlaceholders(embedNameTemplate!!, name, usage))
                        .replace("%custom_vault_eco_balance%", formatMoney(players[index].money, currency, leftSided, formatter))
                        .replace("{index}", (index+1).toString()),
                    setPlaceholdersForDiscordMessage(event.member!!, players[index].player, setCommandPlaceholders(embedValueTemplate!!, name, usage))
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