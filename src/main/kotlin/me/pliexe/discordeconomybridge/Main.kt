package me.pliexe.discordeconomybridge

import me.pliexe.discordeconomybridge.discord.Listener
import me.pliexe.discordeconomybridge.files.DataConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.milkbowl.vault.economy.Economy
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import javax.security.auth.login.LoginException

class DiscordEconomyBridge : JavaPlugin() {

    private var jda: JDA? = null
    private var econ: Economy? = null
    val usersManager: UsersManager = UsersManager(this)
    val moderatorManager = ModeratorManager(this)

    fun getJda(): JDA { return jda!! }
    fun getEconomy(): Economy { return econ!! }

    private fun setupEconomy(): Boolean {
        if(server.pluginManager.getPlugin("Vault") == null) {
            logger.severe("Disabled due to no Vault dependency found!")
            return false
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java)
        if(rsp == null) {
            logger.severe("Disabled due to no Economy plugin!")
            return false
        }
        econ = rsp.provider
        return econ != null
    }

    private fun hasDefaults(): Boolean {
        if(!config.isSet("TOKEN")) {
            logger.severe("TOKEN field missing from config.yml, disabling plugin...")
            return false
        } else if(!config.isString("TOKEN")) {
            logger.severe("TOKEN field is invalid type. It must be a text(string) in config.yml, disabling plugin...")
            return false
        }

        if(!config.isString("PREFIX")) {
            logger.severe("PREFIX field missing from config.yml, disabling plugin...")
            return false
        } else if(!config.isString("PREFIX")) {
            logger.severe("PREFIX field is invalid type. It must be a text(string) in config.yml, disabling plugin...")
            return false
        }

        if(!config.isString("Currency")) {
            logger.severe("Currency field missing from config.yml, disabling plugin...")
            return false
        } else if(!config.isString("Currency")) {
            logger.severe("Currency field is invalid type. It must be a text(string) in config.yml, disabling plugin...")
            return false
        }

        if(!config.isSet("CurrencyLeftSide")) {
            logger.severe("CurrencyLeftSide field missing from config.yml, disabling plugin...")
            return false
        } else if(!config.isBoolean("CurrencyLeftSide")) {
            logger.severe("CurrencyLeftSide field is invalid type. It must be a true or false (boolean) in config.yml, disabling plugin...")
            return false
        }

        if(!config.isSet("noPermissionMessage")) {
            logger.severe("noPermissionMessage field missing from config.yml, disabling plugin...")
            return false
        } else if(!config.isString("noPermissionMessage")) {
            logger.severe("noPermissionMessage field is invalid type. It must be a text(string) in config.yml, disabling plugin...")
            return false
        }

        return true
    }

    override fun onEnable() {
        saveDefaultConfig()
        if(!hasDefaults()) {
            server.pluginManager.disablePlugin(this)
            return
        }

        DataConfig.setup()
        DataConfig.get().options().copyDefaults(true)
        DataConfig.save()

        usersManager.LoadFromConfig()
        moderatorManager.LoadFromConfig()

        if(!setupEconomy()) {
            server.pluginManager.disablePlugin(this)
            return
        }

        val token = config.getString("TOKEN")

        if(token == "BOT_TOKEN")
        {
            server.consoleSender.sendMessage("${ChatColor.RED}${ChatColor.BOLD}TOKEN was not changed! Please changed the field TOKEN with the bot's token! Disabling...")
            pluginLoader.disablePlugin(this)
            return
        }

        if(token == null) {
            server.consoleSender.sendMessage("${ChatColor.RED}${ChatColor.BOLD}TOKEN was not found! Disabling plugin!")
            pluginLoader.disablePlugin(this)
            return
        }

        server.pluginManager.registerEvents(me.pliexe.discordeconomybridge.Listener(this), this)

        try {
            jda = JDABuilder.createDefault(token)
                .setAutoReconnect(true)
                .addEventListeners(Listener(this, server, config))
                .setActivity(if(config.isString("statusType") && config.isString("statusMessage"))
                    when(config.getString("statusType")) {
                        "Playing", "playing" -> Activity.playing(config.getString("statusMessage"))
                        "Watching", "watching" -> Activity.watching(config.getString("statusMessage"))
                        "Streaming", "streaming" -> Activity.streaming(config.getString("statusMessage"),config.getString("statusStreamURL"))
                        "Listening", "listening" -> Activity.listening(config.getString("statusMessage"))
                        else -> null
                    }
                    else null)
                .build()

            jda!!.awaitReady()
        } catch (e: LoginException) {
            logger.severe("Failed connecting to discord! Disabling plugin!")
            pluginLoader.disablePlugin(this)
            return
        } catch (e: Exception) {
            if(e is IllegalStateException && e.message.equals("Was shutdown trying to await status")) {
                pluginLoader.disablePlugin(this)
                return
            }
            logger.warning("An unknown error occurred building JDA...")
            pluginLoader.disablePlugin(this)
            return
        }
    }


}