package me.pliexe.discordeconomybridge

import net.dv8tion.jda.api.EmbedBuilder
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import java.awt.Color

fun getEmbedFromYml (config: FileConfiguration, path: String, filter: (text: String) -> String, resolveScript: ((text: String) -> String)? = null): EmbedBuilder {
    val embed = EmbedBuilder()

    if(!config.isConfigurationSection(path)) throw Error("missing configuration in yaml: $path")

    if(config.isString("$path.title")) embed.setTitle(filter(config.getString("$path.title")))

    Bukkit.getServer().logger.info("IS CONFIG FIELDS: ${config.isConfigurationSection("$path.fields")}")
    if(config.isConfigurationSection("$path.fields"))
    {
        val embedsFields = config.getConfigurationSection("$path.fields").getKeys(false)

        Bukkit.getServer().logger.info("FIELDS: ${config.isConfigurationSection("$path.fields")}")

        embedsFields.forEach { fieldName ->
            if(config.isSet("$path.fields.$fieldName.text")) {
                embed.addField(filter(fieldName),
                    if (config.isString("$path.fields.$fieldName.text")) filter(config.getString("$path.fields.$fieldName.text")) else if(config.isList("$path.fields.$fieldName.text")) config.getStringList("$path.fields.$fieldName.text").joinToString("\n") { filter(it) } else "Invalid yaml configuration!",
                    config.getBoolean("$path.fields.$fieldName.inline")
                )
            }
        }
    }

    if(config.isInt("$path.color"))
    {
        embed.setColor(config.getInt("$path.color"))
    } else if(config.isString("$path.color")) {
        val value = config.getString("$path.color")
        if(value.startsWith("$"))
        {
            if(resolveScript != null) embed.setColor(Color.decode(resolveScript(value.substring(1))))
        } else
            embed.setColor(Color.decode(value))
    }

    if(config.isString("$path.description")) embed.setDescription(filter(config.getString("$path.description")))
    else if(config.isList("$path.description")) embed.setDescription(config.getStringList("$path.description").joinToString("\n") { filter(it) })

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