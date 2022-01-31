package me.pliexe.discordeconomybridge.discord.commands

import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.UUIDUtils
import me.pliexe.discordeconomybridge.discord.*
import me.pliexe.discordeconomybridge.formatMoney
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.util.*

class Balance(main: DiscordEconomyBridge): Command(main) {
    override val usage: String
        get() = "@user"

    override val name: String
        get() = "balance"

    override fun getSlashCommandData(): CommandData {
        return CommandData(name, "Show your or someones current balance!").addOption(OptionType.USER, "user", "The user you want to check!", false)
    }

    override fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        if(args.isEmpty())
            return fail(event, "No username or uuid given to search for!")

        val usernameOrUUID = event.message.contentRaw.substring(commandName.length + prefix.length + 1)

        fun getEmbed(player: Player): MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return LegacyGetYmlEmbed({
                val form = setCommandPlaceholders(it, prefix, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
                    .replace("%custom_vault_eco_balance%", formatMoney(main.getEconomy().getBalance(player), config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "balanceCommandEmbed", main.discordMessagesConfig, {
                it == "ifOnline"
            }).build()
        }

        fun getEmbed(player: OfflinePlayer): MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return LegacyGetYmlEmbed({
                val form = setCommandPlaceholders(it, prefix, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
                    .replace("%custom_vault_eco_balance%", formatMoney(main.getEconomy().getBalance(player), config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "balanceCommandEmbed", main.discordMessagesConfig, {
                it == "ifOnline"
            }).build()
        }

        fun sendMsg(embed: MessageEmbed) {
            event.channel.sendMessageEmbeds(embed).queue()
        }

        val uuid = UUIDUtils.getUUIDFromString(usernameOrUUID)

        if(uuid != null) {
            val player = server.getPlayer(uuid)

            if(player == null) {
                val offlinePlayer = server.getOfflinePlayer(uuid)
                if(!offlinePlayer.hasPlayedBefore())
                    return fail(event, "Player not found!")

                if(!main.getEconomy().hasAccount(offlinePlayer))
                    return fail(event, "This player does not have an account")

                sendMsg(getEmbed(offlinePlayer))
            } else {
                if(!main.getEconomy().hasAccount(player))
                    return fail(event, "This player does not have an account")

                sendMsg(getEmbed(player))
            }
        } else {
            val player = server.getPlayer(usernameOrUUID)

            if(player == null) {
                val uuid2 = main.usersManager.GetPlayerUUID(usernameOrUUID)
                if(uuid2 == null)
                    return fail(event, "Player not found or has not played before!")

                val offlinePlayer = server.getOfflinePlayer(uuid2)
                if(!offlinePlayer.hasPlayedBefore())
                    return fail(event, "Player not found!")

                if(!main.getEconomy().hasAccount(offlinePlayer))
                    return fail(event, "This player does not have an account")

                sendMsg(getEmbed(offlinePlayer))
            } else {
                if(!main.getEconomy().hasAccount(player))
                    return fail(event, "This player does not have an account")

                sendMsg(getEmbed(player))
            }
        }
    }

    override fun run(
        event: github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent,
        commandName: String,
        prefix: String,
        args: List<String>
    ) {
        val uuid: UUID? = if(event.message.mentionedUsers.isEmpty())
            DiscordSRV.getPlugin().accountLinkManager.getUuid(event.author.id)
        else DiscordSRV.getPlugin().accountLinkManager.getUuid(event.message.mentionedUsers.first().id)

        if(uuid == null)
            return fail(event, "This user does not have his account linked!")

        fun getEmbed(player: Player): github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return GetYmlEmbed( {
                val form = setCommandPlaceholders(it, prefix, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
                    .replace("%custom_vault_eco_balance%", formatMoney(main.getEconomy().getBalance(player), config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "balanceCommandEmbed", main.discordMessagesConfig, {
                it == "ifOnline"
            }).build()
        }

        fun getEmbed(player: OfflinePlayer): github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return GetYmlEmbed( {
                val form = setCommandPlaceholders(it, prefix, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
                    .replace("%custom_vault_eco_balance%", formatMoney(main.getEconomy().getBalance(player), config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "balanceCommandEmbed", main.discordMessagesConfig, {
                false
            }).build()
        }

        fun sendMsg(embed: github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed) {
            event.channel.sendMessageEmbeds(embed).queue()
        }

        val player = server.getPlayer(uuid)

        if(player == null) {
            val offlinePlayer = server.getOfflinePlayer(uuid)

            if(!main.getEconomy().hasAccount(offlinePlayer))
                return fail(event, "This player does not have an account")

            sendMsg(getEmbed(offlinePlayer))
        } else {
            if(!main.getEconomy().hasAccount(player))
                return fail(event, "This player does not have an account")

            sendMsg(getEmbed(player))
        }
    }

    override fun run(event: SlashCommandEvent) {
        val user = event.options.firstOrNull()?.asUser ?: event.user

        val uuid: UUID = DiscordSRV.getPlugin().accountLinkManager.getUuid(user.id)
            ?: return fail(event, "This user does not have his account linked!")

        fun getEmbed(player: Player): github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return GetYmlEmbed( {
                val form = setCommandPlaceholders(it, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
                    .replace("%custom_vault_eco_balance%", formatMoney(main.getEconomy().getBalance(player), config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "balanceCommandEmbed", main.discordMessagesConfig, {
                it == "ifOnline"
            }).build()
        }

        fun getEmbed(player: OfflinePlayer): github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return GetYmlEmbed( {
                val form = setCommandPlaceholders(it, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
                    .replace("%custom_vault_eco_balance%", formatMoney(main.getEconomy().getBalance(player), config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "balanceCommandEmbed", main.discordMessagesConfig, {
                false
            }).build()
        }

        fun sendMsg(embed: github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed) {
            event.replyEmbeds(embed).queue()
        }

        val player = server.getPlayer(uuid)

        if(player == null) {
            val offlinePlayer = server.getOfflinePlayer(uuid)

            if(!main.getEconomy().hasAccount(offlinePlayer))
                return fail(event, "This player does not have an account")

            sendMsg(getEmbed(offlinePlayer))
        } else {
            if(!main.getEconomy().hasAccount(player))
                return fail(event, "This player does not have an account")

            sendMsg(getEmbed(player))
        }
    }
}