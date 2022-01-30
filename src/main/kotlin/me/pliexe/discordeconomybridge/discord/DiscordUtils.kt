package me.pliexe.discordeconomybridge.discord

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.filemanager.Config
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.awt.Color

// Matches any patter that is: condition ? word : word
val regexScriptValidate = Regex("^[^?]+\\?[^:]+:[^:]+\$")
// I had to sacrifice 80 babies to the blood god to make these regex patterns
//val regexScriptValidateString = Regex("^([^?]+)\\?( +|)\"[^\"]+\"( +|):( +|)\"[^\"]+(\"( +|))\$")
// Matches any patter that is: condition ? "word" : "word"

fun setPlaceholdersAlternative(player: OfflinePlayer, text: String): String {
    return text
        .replace("%player_name%", player.name)
        .replace("%player_uuid", player.uniqueId.toString())
        .replace("%player_online%", "offline")
}

fun setPlaceholdersAlternative(player: Player, text: String): String {
    return text
        .replace("%player_name%", player.name)
        .replace("%player_uuid", player.uniqueId.toString())
        .replace("%player_online%", "online")
}

fun setPlaceholdersForDiscordMessage(member: Member, player: OfflinePlayer, text: String): String {
    return if(DiscordEconomyBridge.placeholderApiEnabled)
        PlaceholderAPI.setPlaceholders(player, setDiscordPlaceholders(member, text))
    else setPlaceholdersAlternative(player, setDiscordPlaceholders(member, text))
}

fun setPlaceholdersForDiscordMessage(member: Member, player: Player, text: String): String {
    return if(DiscordEconomyBridge.placeholderApiEnabled)
        PlaceholderAPI.setPlaceholders(player, setDiscordPlaceholders(member, text))
    else setPlaceholdersAlternative(player, setDiscordPlaceholders(member, text))
}

fun setDiscordPlaceholders(member: Member, text: String): String {
    return setDiscordPlaceholders(member.user, text)
        .replace("%discord_member_nickname", member.nickname ?: member.user.name)
}

fun setDiscordPlaceholders(user: User, text: String): String {
    return text
        .replace("%discord_user_username%", user.name)
        .replace("%discord_user_avatar_url", user.avatarUrl ?: user.defaultAvatarUrl)
}

fun setCommandPlaceholders(text: String, prefix: String, commandName: String, usage: String): String {
    return text
        .replace("%discord_command_name%", commandName)
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

fun resolveScript(text: String, ) {

}

fun GetYmlEmbed(channel: TextChannel, filter: (String) -> String, path: String, config: Config, resolveScript: ((condition: String) -> Boolean)? = null, ignoreDescription: Boolean = false): EmbedBuilder {
    val embed = EmbedBuilder()

    if(!config.isConfigurationSection(path)) throw Error("missing configuration in discord_messages.yml: $path")

    if(config.isString("$path.title")) embed.setTitle((config.getString("$path.title")))

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
        if(value.startsWith("$"))
        {
            if(resolveScript == null) throw Error("This command does not support custom color scripts")
            if(regexScriptValidate.matches(value.substring(1)))
            {
                val endOfCond = value.indexOf("?")
                val condition = value.substring(1, endOfCond).trim()

                if(resolveScript(condition))
                    embed.setColor(Color.decode(value.substring(endOfCond + 1, value.indexOf(':')).trim()))
                else embed.setColor(Color.decode(value.substring(value.indexOf(':') + 1).trim()))

            } else throw Error("Invalid script in color!")
        }
    }

    if(!ignoreDescription && config.isString("$path.description")) embed.setDescription(filter(config.getString("$path.description")))
    else if(!ignoreDescription && config.isList("$path.description")) embed.setDescription(config.getStringList("$path.description").joinToString("\n") { filter(it) })

    if(config.isString("$path.footer.text")) {
        if(config.isString("$path.footer.icon_url")) embed.setFooter(filter(config.getString("$path.footer.text")), config.getString("$path.footer.icon_url"))
        else embed.setFooter(filter(config.getString("$path.footer.text")))
    }

    if(config.isString("$path.title")) {
        embed.setTitle(filter(config.getString("$path.title")), if (config.isString("$path.icon_url")) config.getString("$path.icon_url") else null)
    }

    if(config.isString("$path.image")) {
        embed.setImage(config.getString("$path.image"))
    }

    if(config.isString("$path.thumnail")) {
        embed.setThumbnail(config.getString("$path.thumnail"))
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