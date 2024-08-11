package me.pliexe.discordeconomybridge

import de.leonhard.storage.Config
import de.leonhard.storage.Json
import de.leonhard.storage.LightningBuilder
import de.leonhard.storage.internal.settings.ConfigSettings
import de.leonhard.storage.internal.settings.DataType
import de.leonhard.storage.internal.settings.ReloadSettings
import github.scarsz.discordsrv.DiscordSRV
import me.clip.placeholderapi.PlaceholderAPI
import me.pliexe.discordeconomybridge.checkers.checkForUpdates
import me.pliexe.discordeconomybridge.commands.*
import me.pliexe.discordeconomybridge.discord.LinkHandler
import me.pliexe.discordeconomybridge.discord.handlers.CommandHandler
import me.pliexe.discordeconomybridge.discord.registerClient
import me.pliexe.discordeconomybridge.discordsrv.DiscordSRVListener
import net.dv8tion.jda.api.JDA
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.FileUtil
import java.io.File
import java.lang.NumberFormatException
import java.util.logging.Logger

class DiscordEconomyBridge : JavaPlugin() {

    companion object {
        var placeholderApiEnabled = false
        var discordSrvEnabled = false
        lateinit var logger: Logger

        lateinit var userCache: Json
    }

    private var jda: JDA? = null
    private var econ: Economy? = null
//    val usersManager: UsersManager = UsersManager()
    val moderatorManager = ModeratorManager(this)
//    val discordMessagesConfig = ConfigManager.getConfig("discord_messages.yml", this, "discord_messages.yml")

//    lateinit var giveawaysDb: Json
    lateinit var pluginMessagesConfig: Config
    lateinit var discordMessagesConfig: Config
    lateinit var customCommandsConfig: Config
    lateinit var defaultConfig: Config

    var shutingDown = false

//    val pluginMessagesConfig = ConfigManager.getConfig("plugin_messages.yml", this, "plugin_messages.yml")

//    val defaultConfig = ConfigManager.getConfig("config.yml", this, "config.yml")
    private val discordSrvListener = DiscordSRVListener(this)
    val commandHandler = CommandHandler(this)
    val linkHandler = LinkHandler(this)
    val pluginMessages = PluginMessages(this)
    val pluginConfig = pluginConfig(this)

    var started = false

    var discordSRVActive: Boolean = false
    val botTag: String
        get() {
            return if(jda == null)
                DiscordSRV.getPlugin().jda.selfUser.asTag
            else jda!!.selfUser.asTag
        }

    /*fun getJda(): JDA { return jda!! }*/
    fun getEconomy(): Economy { return econ!! }
    fun getJda(): JDA? { return jda }

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

        DiscordEconomyBridge.logger = this.logger

        userCache = LightningBuilder
            .fromPath("users", dataFolder.path)
            .createJson()

        defaultConfig = LightningBuilder
            .fromPath("config", dataFolder.path)
            .setReloadSettings(ReloadSettings.MANUALLY)
            .setDataType(DataType.SORTED)
            .addInputStream(getResource("config.yml"))
            .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
            .createConfig()

        discordMessagesConfig = LightningBuilder
            .fromPath("discord_messages", dataFolder.path)
//            .setReloadSettings(ReloadSettings.MANUALLY)
            .setDataType(DataType.SORTED)
            .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
            .addInputStream(getResource("discord_messages.yml"))
            .createConfig()

        pluginMessagesConfig = LightningBuilder
            .fromPath("plugin_messages", dataFolder.path)
//            .setReloadSettings(ReloadSettings.MANUALLY)
            .setDataType(DataType.SORTED)
            .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
            .addInputStream(getResource("plugin_messages.yml"))
            .createConfig()

        customCommandsConfig = LightningBuilder
            .fromPath("custom_commands", dataFolder.path)
//            .setReloadSettings(ReloadSettings.MANUALLY)
            .setDataType(DataType.SORTED)
            .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
            .addInputStream(getResource("custom_commands.yml"))
            .createConfig()

        if(defaultConfig.contains("VERSION")) {
            try {
                val version = defaultConfig.getString("VERSION")
                if(version == "\${version}")
                    defaultConfig.set("VERSION", description.version)
                else if(description.version.replace(".", "").toInt() > version.replace(".", "").toInt()) {
                    logger.info("Updating Configurations to latest version!")

                    val backupFileConf = File(dataFolder.path, "config.yml.old")
                    if(FileUtil.copy(defaultConfig.file, backupFileConf)) {
                        defaultConfig.set("VERSION", description.version)
                        defaultConfig.addDefaultsFromInputStream(getResource("config.yml"))

//                        val backupFileDM = File(dataFolder.path, "discord_messages.yml.old")
//                        if(!File("discord_messages.yml").exists() && FileUtil.copy(discordMessagesConfig.file, backupFileDM)) {
//                            discordMessagesConfig.addDefaultsFromInputStream(getResource("discord_messages.yml"))
//                        } else {
//                            logger.severe("Failed to create backup for discord_messages.yml! Skipping to next configuration...")
//                        }
//
//                        val backupFilePM = File(dataFolder.path, "plugin_messages.yml.old")
//                        if(!File("plugin_messages.yml").exists() && FileUtil.copy(pluginMessagesConfig.file, backupFilePM)) {
//                            pluginMessagesConfig.addDefaultsFromInputStream(getResource("plugin_messages.yml"))
//                        } else {
//                            logger.severe("Failed to create backup for plugin_messages.yml!")
//                        }
//
//                        val backupFileC = File(dataFolder.path, "custom_commands.yml.old")
//                        if(!File("custom_commands.yml").exists() && FileUtil.copy(customCommandsConfig.file, backupFileC)) {
//                            customCommandsConfig.addDefaultsFromInputStream(getResource("custom_commands.yml"))
//                        } else {
//                            logger.severe("Failed to create backup for custom_commands.yml!")
//                        }

                        logger.info("Configurations updated. Old versions of them have been backed up as config_name.yml.old")
                    } else {
                        logger.severe("Failed to create backup for config.yml")
                    }
                }
            } catch (e: ClassCastException) {
                logger.severe("Field VERSION in config.yml is of invalid type! Setting it. Saving old config to config.yml.old")

                val backupFile = File(dataFolder.path, "config.yml.old")
                FileUtil.copy(defaultConfig.file, backupFile)
                defaultConfig.set("VERSION", description.version)
            } catch (e: NumberFormatException) {
                logger.severe("Field VERSION in config.yml is of invalid type! Setting it. Saving old config to config.yml.old")

                val backupFile = File(dataFolder.path, "config.yml.old")
                FileUtil.copy(defaultConfig.file, backupFile)
                defaultConfig.set("VERSION", description.version)
            }
        } else {
            logger.severe("Missing VERSION from config.yml! Setting it. Saving old config to config.yml.old")

            val backupFile = File(dataFolder.path, "config.yml.old")
            FileUtil.copy(defaultConfig.file, backupFile)
            defaultConfig.set("VERSION", description.version)
        }

        if(!checkForConfigurations(this)) {
            server.pluginManager.disablePlugin(this)
            return
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
        {
            placeholderApiEnabled = true
            if(!PlaceholderAPI.containsPlaceholders("%player_name%"))
                logger.severe("Player placeholders are not installed inside PlaceholderAPI! Please install them using: /papi ecloud download Player")
        }

//        logger.info("TOKEN IS ${defaultConfig.getString("TOKEN")} : EXISTS: ${defaultConfig.isSet("TOKEN")}")

        discordSRVActive = Bukkit.getPluginManager().getPlugin("DiscordSRV") != null


        try {
            if(!defaultConfig.getOrDefault("independent", false) && discordSRVActive) {
                discordSrvEnabled = true

                if(defaultConfig.contains("TOKEN") && defaultConfig.getString("TOKEN") != DiscordSRV.config().getString("BotToken"))
                    logger.info("Found DiscordSRV. If you want to run this plugin independently then enable \"independent\" in config.yml")
            } else if(!tokenCheck(this)) {
                server.pluginManager.disablePlugin(this)
                return
            }
        } catch (e: ClassCastException) {
            logger.severe("Config field: independent is of invalid type. The bot will continue to load.")
        }

        pluginConfig.resetCache()
        moderatorManager.LoadFromConfig()

        if(!setupEconomy()) {
            server.pluginManager.disablePlugin(this)
            return
        }

        server.pluginManager.registerEvents(Listener(), this)

        if(discordSrvEnabled)
        {
            DiscordSRV.api.subscribe(discordSrvListener)

            getCommand("unlinkdiscord").executor = LinkDisabledCommandResponse(this)
            getCommand("linkdiscord").executor = LinkDisabledCommandResponse(this)
        } else {
            val token = defaultConfig.getString("TOKEN")

            if(token == "BOT_TOKEN")
            {
                server.consoleSender.sendMessage("${ChatColor.RED}${ChatColor.BOLD}TOKEN was not changed! Please changed the field TOKEN with the bot's token! Disabling...")
                pluginLoader.disablePlugin(this)
                return
            }

            linkHandler.initNative()

            jda = registerClient(this, defaultConfig, token)
            if(jda == null) {
                server.consoleSender.sendMessage("Couldn't start discord client!")
                pluginLoader.disablePlugin(this)
                return
            }
            jda!!.awaitReady()

            getCommand("unlinkdiscord").executor = UnlinkCommand(this)
            getCommand("linkdiscord").executor = LinkCommand(this)
        }

        getCommand("discordeconomybridge").executor = Deb(this)
        getCommand("clearslashcommands").executor = ClearSlashCommands(this)

        checkForUpdates(description.version)

        started = true
    }

    override fun onDisable() {
        if(started) {
            shutingDown = false
            logger.info("Unregistering commands!")
            if (discordSrvEnabled)
                DiscordSRV.api.unsubscribe(discordSrvListener)

            commandHandler.getEvents().clear()
            commandHandler.getMessageDeleteEvents().clear()
            if(!discordSrvEnabled)
                getJda()!!.shutdown()

            if(commandHandler.getBets().size > 0)
            {
                logger.info("Restoring cancelled bets")
                commandHandler.getBets().forEach { (key, value) ->
                    getEconomy().depositPlayer(server.getOfflinePlayer(key), value)
                }
            }
            logger.info("Done.")
        }
    }

}