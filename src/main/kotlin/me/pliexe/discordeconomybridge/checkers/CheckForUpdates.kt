package me.pliexe.discordeconomybridge.checkers

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import java.io.IOException
import java.io.InputStreamReader
import java.lang.NumberFormatException
import java.net.URL

fun checkForUpdates(version: String) {
    val api = URL("https://api.github.com/repos/Pliexe/DiscordEconomyBridge/releases/latest")
    val con = api.openConnection()
    con.connectTimeout = 15000
    con.readTimeout = 15000

    val tagName: String

    try {
        val json = Gson().fromJson(InputStreamReader(con.getInputStream()).readLines().joinToString(), JsonObject::class.java)
        tagName = json.get("tag_name").asString.replace(".", "")
    } catch (e: JsonParseException) {
        DiscordEconomyBridge.logger.severe("Failed to check for updates. Code 1.")
        return
    } catch (e: IOException) {
        DiscordEconomyBridge.logger.severe("Failed to check for updates. Code 2.")
        e.printStackTrace()
        return
    } catch (e: ClassCastException) {
        DiscordEconomyBridge.logger.severe("Failed to check for updates. Code 3.")
        e.printStackTrace()
        return
    }

    try {
        val latestVersion = tagName.substring(1, tagName.length).toInt()

        if(latestVersion > version.replace(".", "").toInt()) {
            DiscordEconomyBridge.logger.info("The plugin is out of date. A new version is available: $tagName")
            DiscordEconomyBridge.logger.info("https://www.spigotmc.org/resources/discord-economy-bridge-1-7-1-18-slashcommands-minigames-in-discord-supports-discordsrv.90290/")
        } else {
            DiscordEconomyBridge.logger.info("DiscordEconomyBridge is up to date!")
        }
    } catch (e: NumberFormatException) {
        DiscordEconomyBridge.logger.severe("Failed to check for updates. Code 4.")
        return
    }
}
