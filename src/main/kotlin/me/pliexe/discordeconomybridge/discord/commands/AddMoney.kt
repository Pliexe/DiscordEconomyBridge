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

class AddMoney(main: DiscordEconomyBridge): Command(main) {

    override val usage: String
        get() = "amount @user"

    override val name: String
        get() = "addmoney"

    override val adminCommand = true

    override fun getSlashCommandData(): CommandData {
        return CommandData(name, "Add money to a user")
            .addOption(OptionType.NUMBER, "amount", "The amount of money to add!", true)
            .addOption(OptionType.USER, "user", "The user that will receive the money!", true)
            .setDefaultEnabled(false)
    }

    override fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        if(event.member == null)
            return fail(event, "Couldn't get member!")

        if(args.isEmpty())
            return fail(event, "No arguments given.")

        val amount = args[0].toDoubleOrNull()
            ?: return fail(event, "Amount not given!")

        if(event.message.contentRaw.length < commandName.length + prefix.length + args[0].length + 2)
            return fail(event, "Missing user parameter!")

        val uuidOrUsername = event.message.contentRaw.substring(commandName.length + prefix.length + args[0].length + 2)

        fun getEmbed(removed: Double, player: Player): MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return LegacyGetYmlEmbed({
                val form = setCommandPlaceholders(it, prefix, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
                    .replace("{amount_increase}", formatMoney(removed, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "addmoneyCommandEmbed", main.discordMessagesConfig).build()
        }

        fun getEmbed(removed: Double, player: OfflinePlayer): MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return LegacyGetYmlEmbed({
                val form = setCommandPlaceholders(it, prefix, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
                    .replace("{amount_increase}", formatMoney(removed, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "addmoneyCommandEmbed", main.discordMessagesConfig).build()
        }

        fun sendMsg(embed: MessageEmbed) {
            event.channel.sendMessageEmbeds(embed).queue()
        }

        val uuid = UUIDUtils.getUUIDFromString(uuidOrUsername)

        if(uuid == null) {
            val player = server.getPlayer(uuidOrUsername)

            if(player == null) {
                val uuid2 = main.usersManager.GetPlayerUUID(uuidOrUsername)

                if(uuid2 == null)
                    return fail(event, "Player not found or has not played before!")

                val offlinePlayer = server.getOfflinePlayer(uuid2)

                if(!offlinePlayer.hasPlayedBefore())
                    return fail(event, "Player not found!")

                if(!main.getEconomy().hasAccount(offlinePlayer))
                    main.getEconomy().createPlayerAccount(offlinePlayer)

                main.getEconomy().depositPlayer(offlinePlayer, amount)

                sendMsg(getEmbed(amount, offlinePlayer))
            } else {
                if(!main.getEconomy().hasAccount(player))
                    main.getEconomy().createPlayerAccount(player)

                main.getEconomy().depositPlayer(player, amount)

                sendMsg(getEmbed(amount, player))
            }
        } else {
            val player = server.getPlayer(uuid)

            if(player == null) {
                val offlinePlayer = server.getOfflinePlayer(uuid)

                if(!offlinePlayer.hasPlayedBefore())
                    return fail(event, "Player not found!")

                if(!main.getEconomy().hasAccount(offlinePlayer))
                    main.getEconomy().createPlayerAccount(offlinePlayer)

                main.getEconomy().depositPlayer(offlinePlayer, amount)

                sendMsg(getEmbed(amount, offlinePlayer))
            } else {
                if(!main.getEconomy().hasAccount(player))
                    main.getEconomy().createPlayerAccount(player)

                main.getEconomy().depositPlayer(player, amount)

                sendMsg(getEmbed(amount, player))
            }
        }
    }

    override fun run(
        event: github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent,
        commandName: String,
        prefix: String,
        args: List<String>
    ) {
        if(event.member == null)
            return fail(event, "Couldn't get member!")

        if(args.isEmpty())
            return fail(event, "No arguments given.")

        val amount = args[0].toDoubleOrNull()
            ?: return fail(event, "Amount not given!")

        val uuid: UUID? = if(event.message.mentionedUsers.isEmpty())
            DiscordSRV.getPlugin().accountLinkManager.getUuid(event.author.id)
        else
            DiscordSRV.getPlugin().accountLinkManager.getUuid(event.message.mentionedUsers.first().id)

        if(uuid == null)
            return fail(event, "This user does not have his account linked!")

        fun getEmbed(removed: Double, player: Player): github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return GetYmlEmbed( {
                val form = setCommandPlaceholders(it, prefix, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
                    .replace("{amount_increase}", formatMoney(removed, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "addmoneyCommandEmbed", main.discordMessagesConfig).build()
        }

        fun getEmbed(removed: Double, player: OfflinePlayer): github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return GetYmlEmbed( {
                val form = setCommandPlaceholders(it, prefix, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
                    .replace("{amount_increase}", formatMoney(removed, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "addmoneyCommandEmbed", main.discordMessagesConfig).build()
        }

        fun sendMsg(embed: github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed) {
            event.channel.sendMessageEmbeds(embed).queue()
        }

        val player = server.getPlayer(uuid)

        if(player == null) {
            val offlinePlayer = server.getOfflinePlayer(uuid)

            if(!offlinePlayer.hasPlayedBefore())
                return fail(event, "Player not found!")

            if(!main.getEconomy().hasAccount(offlinePlayer))
                main.getEconomy().createPlayerAccount(offlinePlayer)

            main.getEconomy().depositPlayer(offlinePlayer, amount)

            sendMsg(getEmbed(amount, offlinePlayer))
        } else {
            if(!main.getEconomy().hasAccount(player))
                main.getEconomy().createPlayerAccount(player)

            main.getEconomy().depositPlayer(player, amount)

            sendMsg(getEmbed(amount, player))
        }
    }

    override fun run(event: SlashCommandEvent) {
        val amount = event.options.first().asDouble

        if(amount < 0)
            return fail(event, "You may not give less than 0 to a user")

        val user = event.options[1].asUser
        if(user.isBot)
            return fail(event, "You may not add money to a bot!")

        val uuid: UUID = DiscordSRV.getPlugin().accountLinkManager.getUuid(user.id)
            ?: return fail(event, "This user does not have his account linked!")

        fun getEmbed(removed: Double, player: Player): github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return GetYmlEmbed( {
                val form = setCommandPlaceholders(it, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
                    .replace("{amount_increase}", formatMoney(removed, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "addmoneyCommandEmbed", main.discordMessagesConfig).build()
        }

        fun getEmbed(removed: Double, player: OfflinePlayer): github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed {
            val formatter = DecimalFormat("#,###.##")

            return GetYmlEmbed( {
                val form = setCommandPlaceholders(it, name, usage)
                setPlaceholdersForDiscordMessage(event.member!!, player, form)
                    .replace("{amount_increase}", formatMoney(removed, config.getString("Currency"), config.getBoolean("CurrencyLeftSide"), formatter))
            }, "addmoneyCommandEmbed", main.discordMessagesConfig).build()
        }

        fun sendMsg(embed: github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed) {
            event.replyEmbeds(embed).queue()
        }

        val player = server.getPlayer(uuid)

        if(player == null) {
            val offlinePlayer = server.getOfflinePlayer(uuid)

            if(!offlinePlayer.hasPlayedBefore())
                return fail(event, "Player not found!")

            if(!main.getEconomy().hasAccount(offlinePlayer))
                main.getEconomy().createPlayerAccount(offlinePlayer)

            main.getEconomy().depositPlayer(offlinePlayer, amount)

            sendMsg(getEmbed(amount, offlinePlayer))
        } else {
            if(!main.getEconomy().hasAccount(player))
                main.getEconomy().createPlayerAccount(player)

            main.getEconomy().depositPlayer(player, amount)

            sendMsg(getEmbed(amount, player))
        }
    }
}