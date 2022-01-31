package me.pliexe.discordeconomybridge.filemanager

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStreamReader

class Config(private val file: File, main: DiscordEconomyBridge) {

    val config: FileConfiguration

    init {
        try {
            config = YamlConfiguration.loadConfiguration(file)
        } catch (e: Exception) {
            throw Error("Invalid Config: \"$file\". Disabling plugin! Error: $e")
            main.pluginLoader.disablePlugin(main)
        }
    }

    fun reloadConfig() {
        YamlConfiguration.loadConfiguration(file)
    }

    fun saveConfig() {
        ConfigManager.saveConfig(config.saveToString(), file)
    }

    fun isConfigurationSection(path: String): Boolean {
        return config.isConfigurationSection(path)
    }

    fun isString(path: String): Boolean {
        return config.isString(path)
    }

    fun isInt(path: String): Boolean {
        return config.isInt(path)
    }

    fun isBoolean(path: String): Boolean {
        return config.isBoolean(path)
    }

    fun isList(path: String): Boolean {
        return config.isList(path)
    }

    fun isSet(path: String): Boolean {
        return config.isSet(path)
    }

    fun getString(path: String): String {
        return config.getString(path)
    }

    fun getInt(path: String): Int {
        return config.getInt(path)
    }

    fun getConfigurationSection(path: String): ConfigurationSection {
        return config.getConfigurationSection(path)
    }

    fun getStringList(path: String): MutableList<String> {
        return config.getStringList(path)
    }

    fun getBoolean(path: String): Boolean {
        return config.getBoolean(path)
    }

    fun set(path: String, value: Any) {
        return config.set(path, value)
    }
}