package me.pliexe.discordeconomybridge

import github.scarsz.discordsrv.DiscordSRV
import me.pliexe.discordeconomybridge.discord.Listener
import me.pliexe.discordeconomybridge.discord.registerClient
import me.pliexe.discordeconomybridge.discordsrv.DiscordSRVListener
import me.pliexe.discordeconomybridge.filemanager.ConfigManager
import me.pliexe.discordeconomybridge.filemanager.DataConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import javax.security.auth.login.LoginException

class DiscordEconomyBridge : JavaPlugin() {

    companion object {
        var placeholderApiEnabled = false
        var discordSrvEnabled = false
    }

    private var jda: JDA? = null
    private var econ: Economy? = null
    val usersManager: UsersManager = UsersManager()
    val moderatorManager = ModeratorManager(this)
    val discordMessagesConfig = ConfigManager.getConfig("discord_messages.yml", this, "discord_messages.yml")
    val defaultConfig = ConfigManager.getConfig("config.yml", this, "config.yml")
    private val discordSrvListener = DiscordSRVListener(this)

    /*fun getJda(): JDA { return jda!! }*/
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

    override fun onEnable() {

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            placeholderApiEnabled = true

//        logger.info("TOKEN IS ${defaultConfig.getString("TOKEN")} : EXISTS: ${defaultConfig.isSet("TOKEN")}")


        if((if(defaultConfig.isBoolean("independent")) !defaultConfig.getBoolean("independent") else true) && Bukkit.getPluginManager().getPlugin("DiscordSRV") != null)
        {
            discordSrvEnabled = true

            if(defaultConfig.isSet("TOKEN") && defaultConfig.isString("TOKEN") && (defaultConfig.getString("TOKEN") != DiscordSRV.config().getString("BotToken")))
                logger.info("Found DiscordSRV. If you want to run this plugin independently then enable \"independent\" in config.yml")
        } else if(!TokenCheck(this)) {
            server.pluginManager.disablePlugin(this)
            return
        }

        if(!CheckForConfigurations(this)) {
            logger.severe("FALSE TRIGGER")
            server.pluginManager.disablePlugin(this)
            return
        }

//        if(!discordSrvEnabled)
//        {
            DataConfig.setup()
            DataConfig.get().options().copyDefaults(true)
            DataConfig.save()
//        }

        usersManager.LoadFromConfig()
        moderatorManager.LoadFromConfig()

        if(!setupEconomy()) {
            server.pluginManager.disablePlugin(this)
            return
        }

        server.pluginManager.registerEvents(Listener(this), this)

        if(discordSrvEnabled)
        {
            DiscordSRV.api.subscribe(discordSrvListener)
        } else {
            if(!defaultConfig.isString("TOKEN")) {
                server.consoleSender.sendMessage("${ChatColor.RED}${ChatColor.BOLD}TOKEN was not found! Disabling plugin!")
                pluginLoader.disablePlugin(this)
                return
            }

            val token = defaultConfig.getString("TOKEN")

            if(token == "BOT_TOKEN")
            {
                server.consoleSender.sendMessage("${ChatColor.RED}${ChatColor.BOLD}TOKEN was not changed! Please changed the field TOKEN with the bot's token! Disabling...")
                pluginLoader.disablePlugin(this)
                return
            }

            jda = registerClient(this, defaultConfig, token)
            if(jda == null) return
        }
    }

    override fun onDisable() {
        if (discordSrvEnabled)
            DiscordSRV.api.unsubscribe(discordSrvListener)
    }

}