package me.pliexe.discordeconomybridge.discord

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.awt.Color
import java.util.*

// Matches any patter that is: condition ? word : word
val regexScriptValidate = Regex("^[^?]+\\?[^:]+:[^:]+\$")
// I had to sacrifice 80 babies to the blood god to make these regex patterns
//val regexScriptValidateString = Regex("^([^?]+)\\?( +|)\"[^\"]+\"( +|):( +|)\"[^\"]+(\"( +|))\$")
// Matches any patter that is: condition ? "word" : "word"

class UniversalPlayer(
    val onlinePlayer: Player?,
    val offlinePlayer: OfflinePlayer?
) {
    constructor(player: Player) : this(player, null)
    constructor(player: OfflinePlayer) : this(null, player)

    companion object {
        fun getByUUID(uuid: UUID): UniversalPlayer {
            val player = Bukkit.getPlayer(uuid)
            return if(player == null) {
                val offlinePlayer = Bukkit.getOfflinePlayer(uuid) ?: throw Error("The player couldn't be gotten!")
                UniversalPlayer(offlinePlayer)
            } else UniversalPlayer(player)
        }
    }

    val name: String
        get() = onlinePlayer?.name ?: offlinePlayer!!.name

    val uniqueId: UUID
        get() = onlinePlayer?.uniqueId ?: offlinePlayer!!.uniqueId

    val isOnline: Boolean
        get() = onlinePlayer?.isOnline ?: offlinePlayer!!.isOnline

    fun setPlaceholderAPI(text: String): String {
        return if(onlinePlayer == null)
            PlaceholderAPI.setPlaceholders(offlinePlayer, text)
        else PlaceholderAPI.setPlaceholders(onlinePlayer, text)
    }

    fun getBalance(main: DiscordEconomyBridge): Double {
        return if(onlinePlayer == null)
            main.getEconomy().getBalance(offlinePlayer!!)
        else main.getEconomy().getBalance(onlinePlayer)
    }

    fun createEconomyAccountIfNotPresent(main: DiscordEconomyBridge) {
        if(onlinePlayer == null)
        {
            if (!main.getEconomy().hasAccount(offlinePlayer!!))
                main.getEconomy().createPlayerAccount(offlinePlayer)
        } else {
            if(!main.getEconomy().hasAccount(onlinePlayer))
                main.getEconomy().createPlayerAccount(offlinePlayer)
        }
    }

    fun depositPlayer(main: DiscordEconomyBridge, amount: Double) {
        if(onlinePlayer == null)
            main.getEconomy().depositPlayer(offlinePlayer!!, amount)
        else main.getEconomy().depositPlayer(onlinePlayer, amount)
    }

    fun withdrawPlayer(main: DiscordEconomyBridge, amount: Double) {
        if(onlinePlayer == null)
            main.getEconomy().withdrawPlayer(offlinePlayer!!, amount)
        else main.getEconomy().withdrawPlayer(onlinePlayer, amount)
    }
}

fun setPlaceholdersAlternative(player: Player, text: String): String {
    return setPlaceholdersAlternative(UniversalPlayer(player), text)
}

fun setPlaceholdersAlternative(player: OfflinePlayer, text: String): String {
    return setPlaceholdersAlternative(UniversalPlayer(player), text)
}

fun setPlaceholdersAlternative(player: UniversalPlayer, text: String): String {
    return text
        .replace("%player_name%", player.name)
        .replace("%player_uuid", player.uniqueId.toString())
        .replace("%player_online%", if(player.isOnline) "online" else "offline")
}

fun setPlaceholdersForDiscordMessage(member: DiscordMember, player: OfflinePlayer, text: String): String {
    return setPlaceholdersForDiscordMessage(member, UniversalPlayer(player), text)
}

fun setPlaceholdersForDiscordMessage(member: DiscordMember, player: Player, text: String): String {
    return setPlaceholdersForDiscordMessage(member, UniversalPlayer(player), text)
}

fun setPlaceholdersForDiscordMessage(member: DiscordMember, player: UniversalPlayer, text: String): String {
    return if(DiscordEconomyBridge.placeholderApiEnabled)
        player.setPlaceholderAPI(setDiscordPlaceholders(member, text))
    else setPlaceholdersAlternative(player, setDiscordPlaceholders(member, text))
}

fun setPlaceholdersForDiscordMessage(user: DiscordUser, player: UniversalPlayer, text: String): String {
    return if(DiscordEconomyBridge.placeholderApiEnabled)
        player.setPlaceholderAPI(setDiscordPlaceholders(user, text))
    else setPlaceholdersAlternative(player, setDiscordPlaceholders(user, text))
}

fun setDiscordPlaceholders(member: DiscordMember, text: String): String {
    return setDiscordPlaceholders(member.user, text)
        .replace("%discord_member_nickname", member.nickname ?: member.user.name)
}

fun setDiscordPlaceholders(user: DiscordUser, text: String): String {
    return text
        .replace("%discord_user_username%", user.name)
        .replace("%discord_user_discriminator%", user.discriminator)
        .replace("%discord_user_tag%", user.asTag)
        .replace("%discord_user_avatar_url", user.avatarUrl)
}

fun setCommandPlaceholders(text: String, prefix: String, commandName: String, description: String, usage: String): String {
    return text
        .replace("%discord_command_name%", commandName)
        .replace("%discord_command_description%", description)
        .replace("%discord_command_prefix%", prefix)
        .replace("%discord_command_usage%", usage)
}

//fun ResolveScriptLine(text: String, conditionCheck: (text: String) -> Boolean): String {
//
//    if(text.contains(regexScriptValidateString)) {
//        val conditionEnd = text.indexOf("?")
//        val condition = text.substring(0, conditionEnd).trim()
//
//        return if(conditionCheck(condition))
//            text.substring(conditionEnd + 1, text.indexOf(":")).trim()
//        else text.substring(text.indexOf(":")+ 1).trim()
//
//    } else if(text.contains(regexScriptValidate)) {
//        val conditionEnd = text.indexOf("?")
//        val condition = text.substring(0, conditionEnd).trim()
//
//        if(conditionCheck(condition))
//        {
//            var endIndex: Int = conditionEnd
//
//            for(i in conditionEnd+1..text.length)
//                if(text[i] == '\"' && text[i - 1] == '\\')
//                {
//                    endIndex = i
//                    break
//                }
//
//            return text.substring(endIndex + 1, text.indexOf('"'))
//        }
//            text.substring(conditionEnd + 1, text.lastIndexOf('"'))
//
//    } else text
//
//
//
//    GetYmlEmbed()
//}

fun getYMLEmbed(main: DiscordEconomyBridge, embed: DiscordEmbed, path: String, filter: ((text: String) -> String), resolveScript: ((command: String) -> Boolean)? = null, ignoreDescription: Boolean = false): DiscordEmbed {
    val config = main.discordMessagesConfig

    if(!config.isConfigurationSection(path))
        throw Error("Missing path configuration ($path) in discord_messages.yml!")

    if(config.isString("$path.title")) embed.setTitle(filter(config.getString("$path.title")))

    if(config.isConfigurationSection("$path.fields"))
    {
        val embedFields = config.getConfigurationSection("$path.fields").getKeys(false)

        embedFields.forEach { fieldName ->
            if(config.isSet("$path.fields.$fieldName.text")) {
                embed.addField(
                    filter(fieldName),
                    when {
                        config.isString("$path.fields.$fieldName.text") -> filter(config.getString("$path.fields.$fieldName.text"))
                        config.isList("$path.fields.$fieldName.text") -> config.getStringList("$path.fields.$fieldName.text").joinToString("\n") { filter(it) }
                        else -> "Invalid yaml configuration!"
                    }, config.getBoolean("$path.fields.$fieldName.inline"))
            }
        }
    }

    if(config.isInt("$path.color"))
        embed.setColor(config.getInt("$path.color"))
    else if(config.isString("$path.color")) {
        val value = config.getString("$path.color")
        if(resolveScript != null && regexScriptValidate.matches(value))
        {
            val endOfCond = value.indexOf("?")
            val condition = value.substring(0, endOfCond).trim()

            if(resolveScript(condition))
                embed.setColor(Color.decode(value.substring(endOfCond + 1, value.indexOf(':')).trim()))
            else embed.setColor(Color.decode(value.substring(value.indexOf(':') + 1).trim()))
        } else embed.setColor(Color.decode(config.getString("$path.color")))
    }

    if(!ignoreDescription && config.isString("$path.description")) embed.setDescription(filter(config.getString("$path.description")))
    else if(!ignoreDescription && config.isList("$path.description")) embed.setDescription(config.getStringList("$path.description").joinToString("\n") { filter(it) })

    if(config.isString("$path.footer.text")) {
        if(config.isString("$path.footer.icon_url")) embed.setFooter(filter(config.getString("$path.footer.text")), config.getString("$path.footer.icon_url"))
        else embed.setFooter(filter(config.getString("$path.footer.text")))
    }

    if(config.isString("$path.image")) {
        embed.setImage(config.getString("$path.image"))
    }

    if(config.isString("$path.thumbnail")) {
        embed.setThumbnail(config.getString("$path.thumbnail"))
    }

    if(config.isString("$path.author.name")) {
        embed.setAuthor(
            config.getString("$path.author.name"),
            if(config.isString("$path.author.url")) config.getString("$path.author.url") else null,
            if(config.isString("$path.author.icon_url")) config.getString("$path.author.icon_url") else null
        )
    }

    return embed
}