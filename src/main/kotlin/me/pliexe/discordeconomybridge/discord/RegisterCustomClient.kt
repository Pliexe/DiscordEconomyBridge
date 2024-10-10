package me.pliexe.discordeconomybridge.discord

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import javax.security.auth.login.LoginException

fun registerClient(main: DiscordEconomyBridge, defaultConfig: de.leonhard.storage.Config, token: String): JDA? {

    try {

        return JDABuilder.createDefault(token)
            .setAutoReconnect(true)
            .addEventListeners(Listener(main, main.server))
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .setActivity(getActivity(defaultConfig))
            .enableIntents(
                mutableListOf(
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.DIRECT_MESSAGES
                )
            )
            .build()
    } catch (e: LoginException) {
        main.logger.severe("Failed connecting to discord! Disabling plugin!\nReason: ${e.message}")
        main.pluginLoader.disablePlugin(main)
        return null
    } catch (e: Exception) {
        if (e is IllegalStateException && e.message.equals("Was shutdown trying to await status")) {
            main.pluginLoader.disablePlugin(main)
            return null
        }
        main.logger.warning("An unknown error occurred building JDA...\nError:${e.message}\n${e.stackTraceToString()}")
        main.pluginLoader.disablePlugin(main)
        return null
    }
}

fun getActivity(defaultConfig: de.leonhard.storage.Config): Activity? {
    return try {
        val message = defaultConfig.getString("statusMessage")
        if (message != null) {
            when(defaultConfig.getString("statusType")) {
                "Playing", "playing" -> Activity.playing(message)
                "Watching", "watching" -> Activity.watching(message)
                "Streaming", "streaming" -> Activity.streaming(message, defaultConfig.getString("statusStreamURL"))
                "Listening", "listening" -> Activity.listening(message)
                else -> null
            }
        } else null
    } catch (e: ClassCastException) {
        DiscordEconomyBridge.logger.severe("Type statusType or statusMessage is not a text (string). The plugin will continue to load.")
        null
    }
}