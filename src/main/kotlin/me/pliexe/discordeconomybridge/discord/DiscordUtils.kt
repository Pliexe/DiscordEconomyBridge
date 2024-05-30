package me.pliexe.discordeconomybridge.discord

import de.leonhard.storage.Config
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.clip.placeholderapi.PlaceholderAPI
import me.pliexe.discordeconomybridge.isConfigSection
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.awt.Color
import java.util.*
import java.util.logging.Logger

// Matches any patter that is: condition ? word : word
val regexScriptValidate = Regex("^[^?]+\\?[^:]+:[^:]+\$")
// I had to sacrifice 80 babies to the blood god to make these regex patterns
//val regexScriptValidateString = Regex("^([^?]+)\\?( +|)\"[^\"]+\"( +|):( +|)\"[^\"]+(\"( +|))\$")
// Matches any patter that is: condition ? "word" : "word"

fun sendWrongUserInteractionMessage(interactionEvent: ComponentInteractionEvent) {
    val embed = interactionEvent.getYMLEmbed("onWrongClickMessage", {
        if(interactionEvent.member == null)
            setDiscordPlaceholders(interactionEvent.user, it)
        else setDiscordPlaceholders(interactionEvent.member!!, it)
    })

    val content = embed.content?.let {
        if(interactionEvent.member == null)
            setDiscordPlaceholders(interactionEvent.user, it)
        else setDiscordPlaceholders(interactionEvent.member!!, it)
    }

    if(embed.isEmpty)
        interactionEvent.replyEphemeral(content ?: "Message not configured! Path: onWrongClickMessage").queue()
    else interactionEvent.replyEphemeral(embed).setContent(content).queue()
}

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

        fun getByUUIDorNull(uuid: UUID): UniversalPlayer? {
            val player = Bukkit.getPlayer(uuid)
            return if(player == null) {
                val offlinePlayer = Bukkit.getOfflinePlayer(uuid) ?: return null
                UniversalPlayer(offlinePlayer)
            } else UniversalPlayer(player)
        }

        fun getByUsername(username: String): UniversalPlayer? {
            return Bukkit.getPlayer(username)?.let { UniversalPlayer(it) } ?: run {
                DiscordEconomyBridge.userCache.getString(username)?.let { uuid ->
                    try {
                        Bukkit.getOfflinePlayer(UUID.fromString(uuid))?.let { UniversalPlayer(it) }
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
            }
        }

        fun getByString(getter: String): UniversalPlayer? {
            return try {
                getByUsername(getter) ?: getByUUIDorNull(UUID.fromString(getter))
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    override fun toString(): String {
        return name
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
        return if(onlinePlayer == null){
            if(main.getEconomy().hasAccount(offlinePlayer)) main.getEconomy().getBalance(offlinePlayer!!) else 0.0
        }
        else if(main.getEconomy().hasAccount(onlinePlayer)) main.getEconomy().getBalance(onlinePlayer) else 0.0
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
        val res: EconomyResponse

        if(onlinePlayer == null)
            res = main.getEconomy().depositPlayer(offlinePlayer!!, amount)
        else res = main.getEconomy().depositPlayer(onlinePlayer, amount)

        if (res.type == EconomyResponse.ResponseType.FAILURE) {
            throw Error("Failed to withdraw player! ${res.errorMessage}")
        }
    }

    fun withdrawPlayer(main: DiscordEconomyBridge, amount: Double) {
        val res: EconomyResponse
        if(onlinePlayer == null)
            res = main.getEconomy().withdrawPlayer(offlinePlayer!!, amount)
        else res = main.getEconomy().withdrawPlayer(onlinePlayer, amount)

        if (res.type == EconomyResponse.ResponseType.FAILURE) {
            throw Error("Failed to withdraw player! ${res.errorMessage}")
        }
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
        .replace("%player_uuid%", player.uniqueId.toString())
        .replace("%player_online%", if(player.isOnline) "online" else "offline")
}

fun setPlaceholdersOther(player: UniversalPlayer, text: String): String {
    return text
        .replace("%player_other_name%", player.name)
        .replace("%player_other_uuid%", player.uniqueId.toString())
        .replace("%player_other_online%", if(player.isOnline) "online" else "offline")
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

fun setPlaceholdersForDiscordMessage(member: DiscordMember, other: DiscordMember, player: UniversalPlayer, text: String): String {
    return if(DiscordEconomyBridge.placeholderApiEnabled)
        player.setPlaceholderAPI(setDiscordPlaceholders(member, other, text))
    else setPlaceholdersAlternative(player, setDiscordPlaceholders(member, text))
}

fun setPlaceholdersForDiscordMessage(member: DiscordMember, other: DiscordMember, player: UniversalPlayer, otherPlayer: UniversalPlayer, text: String): String {
    return setPlaceholdersOther(otherPlayer, if(DiscordEconomyBridge.placeholderApiEnabled)
        player.setPlaceholderAPI(setDiscordPlaceholders(member, other, text))
    else setPlaceholdersAlternative(player, setDiscordPlaceholders(member, other, text)))
}

fun setPlaceholdersForDiscordMessage(user: DiscordUser, other: DiscordUser, player: UniversalPlayer, text: String): String {
    return if(DiscordEconomyBridge.placeholderApiEnabled)
        player.setPlaceholderAPI(setDiscordPlaceholders(user, other, text))
    else setPlaceholdersAlternative(player, setDiscordPlaceholders(user, other, text))
}

fun setDiscordPlaceholders(member: DiscordMember, text: String): String {
    return setDiscordPlaceholders(member.user, text)
        .replace("%discord_member_nickname%", member.nickname ?: member.user.name)
}

fun setDiscordPlaceholders(member: DiscordMember, other: DiscordMember, text: String): String {
    return setDiscordPlaceholders(member.user, other.user, text)
        .replace("%discord_other_member_nickname%", other.nickname ?: other.user.name)
}

fun setDiscordPlaceholders(user: DiscordUser, text: String): String {
    return text
        .replace("%discord_user_username%", user.name)
        .replace("%discord_user_discriminator%", user.discriminator)
        .replace("%discord_user_tag%", user.asTag)
        .replace("%discord_user_avatar_url%", user.avatarUrl)
        .replace("%discord_user_id%", user.id)
}

fun setDiscordPlaceholders(user: DiscordUser, other: DiscordUser, text: String): String {
    return setDiscordPlaceholders(user, text)
        .replace("%discord_other_user_username%", other.name)
        .replace("%discord_other_user_discriminator%", other.discriminator)
        .replace("%discord_other_user_tag%", other.asTag)
        .replace("%discord_other_user_avatar_url%", other.avatarUrl)
        .replace("%discord_other_user_id%", other.id)
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

fun getStringOrStringList(path: String, config: de.leonhard.storage.Config): String? {
    return getStringOrStringList(config.get(path))
}

fun getStringOrStringList(value: Any?): String? {
    return when (value) {
        is String -> return value
        is List<*> -> return value.joinToString("\n")
        else -> null
    }
}

fun getString(value: Any?): String? {
    return when(value) {
        is String -> return value
        else -> null
    }
}

fun getInt(value: Any?): Int? {
    return if(value is Int) value
    else null
}

fun getBool(value: Any?): Boolean? {
    return if(value is Boolean) return value
    else null
}

fun getYMLEmbed(main: DiscordEconomyBridge, embed: DiscordEmbed, path: String, filter: ((text: String) -> String), resolveScript: ((command: String) -> Boolean)? = null, ignoreDescription: Boolean = false): DiscordEmbed {
    return getYMLEmbed(main.discordMessagesConfig, main.logger, embed, path, filter, resolveScript, ignoreDescription)
}

fun getYMLEmbed(config: Config, logger: Logger, embed: DiscordEmbed, path: String, filter: ((text: String) -> String), resolveScript: ((command: String) -> Boolean)? = null, ignoreDescription: Boolean = false): DiscordEmbed {
    if(!config.contains(path))
        throw Error("Missing path configuration ($path) in discord_messages.yml!")

    if(!isConfigSection(config.get(path)))
        throw Error("Invalid configuration type ($path) in discord_messages.yml!")

    getStringOrStringList("$path.title", config)?.let { embed.setTitle(filter(it)) }

    if(isConfigSection(config.get("$path.fields")))
    {
        val embedFields = config.singleLayerKeySet("$path.fields")

        embedFields.forEach { fieldName ->
            try {
                getStringOrStringList("$path.fields.$fieldName.text", config)?.let {
                    embed.addField(
                        filter(fieldName),
                        filter(it),
                        config.get("$path.fields.$fieldName.inline", false)
                    )
                }
            } catch (e: ClassCastException) {
                logger.severe("Invalid embed field configuration at $path.fields.$fieldName.text")
                throw Error("InvalidConfig")
            }
        }
    }

    getInt(config.get("$path.color"))?.let { embed.setColor(it) } ?: run {
        try {
            val value = config.getString("$path.color")

            if(value != null && value.isNotEmpty()) {
                if(resolveScript != null && regexScriptValidate.matches(value)) {
                    val endOfCond = value.indexOf("?")
                    val condition = value.substring(0, endOfCond).trim()

                    if(resolveScript(condition))
                        embed.setColor(Color.decode(value.substring(endOfCond + 1, value.indexOf(':')).trim()))
                    else embed.setColor(Color.decode(value.substring(value.indexOf(':') + 1).trim()))
                } else embed.setColor(Color.decode(value))
            }
        } catch (e: ClassCastException) {
            logger.severe("Invalid embed color configuration at $path.color!")
            throw Error("InvalidConfig")
        }
    }

    if(!ignoreDescription)
        getStringOrStringList("$path.description", config)?.let {
            embed.setDescription(filter(it))
        }

    try {
        if(isConfigSection(config.get("$path.footer"))) {
            getStringOrStringList("$path.footer.text", config)?.let {
                embed.setFooter(filter(it), getString(config.get("$path.footer.icon_url")))
            }
        }
    } catch (e: ClassCastException) {
        logger.severe("Invalid embed footer configuration! $path.footer")
        throw Error("InvalidConfig")
    }

    getString(config.get("$path.image"))?.let {
        embed.setImage(filter(it))
    }

    getString(config.get("$path.thumbnail"))?.let {
        embed.setThumbnail(filter(it))
    }

    try {
        if(isConfigSection(config.get("$path.author"))) {
            getStringOrStringList("$path.author.name", config)?.let {
                embed.setFooter(filter(it), config.getString("$path.author.icon_url"))
            }
        }
    } catch (e: ClassCastException) {
        logger.severe("Invalid embed author configuration! $path.author")
        throw Error("InvalidConfig")
    }

    getStringOrStringList("$path.content", config)?.let {
        embed.content = filter(it)
    }

    return embed
}