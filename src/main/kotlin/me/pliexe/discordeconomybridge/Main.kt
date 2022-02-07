package me.pliexe.discordeconomybridge

import de.leonhard.storage.Config
import de.leonhard.storage.LightningBuilder
import de.leonhard.storage.internal.settings.ConfigSettings
import de.leonhard.storage.internal.settings.ReloadSettings
import github.scarsz.discordsrv.DiscordSRV
import me.pliexe.discordeconomybridge.commands.ClearSlashCommands
import me.pliexe.discordeconomybridge.commands.HelpCommand
import me.pliexe.discordeconomybridge.commands.LinkCommand
import me.pliexe.discordeconomybridge.commands.UnlinkCommand
import me.pliexe.discordeconomybridge.discord.LinkHandler
import me.pliexe.discordeconomybridge.discord.handlers.CommandHandler
import me.pliexe.discordeconomybridge.discord.registerClient
import me.pliexe.discordeconomybridge.discordsrv.DiscordSRVListener
import net.dv8tion.jda.api.JDA
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

class DiscordEconomyBridge : JavaPlugin() {

    companion object {
        var placeholderApiEnabled = false
        var discordSrvEnabled = false
    }

    private var jda: JDA? = null
    private var econ: Economy? = null
//    val usersManager: UsersManager = UsersManager()
    val moderatorManager = ModeratorManager(this)
//    val discordMessagesConfig = ConfigManager.getConfig("discord_messages.yml", this, "discord_messages.yml")

    val pluginMessagesConfig: Config = LightningBuilder
        .fromPath("plugin_messages", dataFolder.path)
        .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
        .setReloadSettings(ReloadSettings.MANUALLY)
        .addInputStream(getResource("plugin_messages.yml"))
        .createConfig()

    val discordMessagesConfig: Config = LightningBuilder
        .fromPath("discord_messages", dataFolder.path)
        .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
//        .setReloadSettings(ReloadSettings.MANUALLY)
        .addInputStream(getResource("discord_messages.yml"))
        .createConfig()

    val defaultConfig: Config = LightningBuilder
        .fromPath("config", dataFolder.path)
        .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
        .setReloadSettings(ReloadSettings.MANUALLY)
        .addInputStream(getResource("config.yml"))
        .createConfig()

//    val pluginMessagesConfig = ConfigManager.getConfig("plugin_messages.yml", this, "plugin_messages.yml")

//    val defaultConfig = ConfigManager.getConfig("config.yml", this, "config.yml")
    private val discordSrvListener = DiscordSRVListener(this)
    val commandHandler = CommandHandler(this)
    val linkHandler = LinkHandler(this)
    val pluginMessages = PluginMessages(this)
    val pluginConfig = pluginConfig(this)

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

        if(!checkForConfigurations(this)) {
            server.pluginManager.disablePlugin(this)
            return
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            placeholderApiEnabled = true

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
            getCommand("clearslashcommands").executor = ClearSlashCommands(this)
        }

        getCommand("discordeconomybridge").executor = HelpCommand(this)
    }

    override fun onDisable() {
        if (discordSrvEnabled)
            DiscordSRV.api.unsubscribe(discordSrvListener)
    }

}