package me.pliexe.discordeconomybridge.files

import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

class DataConfig {
    companion object {
        private var file: File? = null
        private var customFile: FileConfiguration? = null

        fun setup() {
            file = File(Bukkit.getServer().pluginManager.getPlugin("DiscordEconomyBridge").dataFolder, "data.yml")

            if(!file!!.exists()) {
                try {
                    file!!.createNewFile()
                } catch (e: IOException)
                {
                    Bukkit.getLogger().severe("Unable to create data.yml. IO Exception $e")
                }
            }

            customFile = YamlConfiguration.loadConfiguration(file!!)
        }

        fun get(): FileConfiguration {
            return customFile!!
        }

        fun save() {
            try {
                customFile!!.save(file)
            } catch (e: IOException) {
                Bukkit.getLogger().severe("Couldn't save data.yml!")
            }
        }

        fun reload() {
            customFile = YamlConfiguration.loadConfiguration(file)
        }
    }


}