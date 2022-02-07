package me.pliexe.discordeconomybridge.discord

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.filemanager.Config
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import javax.security.auth.login.LoginException

fun registerClient(main: DiscordEconomyBridge, defaultConfig: Config, token: String): JDA? {

    try {
        val jda = JDABuilder.createDefault(token)
            .setAutoReconnect(true)
            .addEventListeners(Listener(main, main.server, defaultConfig))
            .setActivity(if(defaultConfig.isString("statusType") && defaultConfig.isString("statusMessage"))
                when(defaultConfig.getString("statusType")) {
                    "Playing", "playing" -> Activity.playing(defaultConfig.getString("statusMessage"))
                    "Watching", "watching" -> Activity.watching(defaultConfig.getString("statusMessage"))
                    "Streaming", "streaming" -> Activity.streaming(defaultConfig.getString("statusMessage"), defaultConfig.getString("statusStreamURL"))
                    "Listening", "listening" -> Activity.listening(defaultConfig.getString("statusMessage"))
                    else -> null
                }
            else null)
            .enableIntents(mutableListOf(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES))
            .build()

        return jda
    } catch (e: LoginException) {
        main.logger.severe("Failed connecting to discord! Disabling plugin!\nReason: ${e.message}")
        main.pluginLoader.disablePlugin(main)
        return null
    } catch (e: Exception) {
        if(e is IllegalStateException && e.message.equals("Was shutdown trying to await status")) {
            main.pluginLoader.disablePlugin(main)
            return null
        }
        main.logger.warning("An unknown error occurred building JDA...\nError:${e.message}\n${e.stackTraceToString()}")
        main.pluginLoader.disablePlugin(main)
        return null
    }
}