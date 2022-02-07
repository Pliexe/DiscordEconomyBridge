package me.pliexe.discordeconomybridge

import org.bukkit.Bukkit

class pluginConfig(val main: DiscordEconomyBridge) {
    val minBet: Double
        get() {
            return try {
                main.defaultConfig.getOrDefault("minBet", 100.0)
            } catch (e: ClassCastException) {
                Bukkit.getLogger().severe("Field \"minBet\" is invalid type, it must be a number.  The plugin will continue with default value.")
                100.0
            }
        }

    val maxBet: Double
        get() {
            return try {
                main.defaultConfig.getOrDefault("maxBet", 100.0)
            } catch (e: ClassCastException) {
                Bukkit.getLogger().severe("Field \"maxBet\" is invalid type, it must be a number.  The plugin will continue with default value.")
                100000000000000000.0
            }
        }

    val currency: String
        get() {
            return try {
                main.defaultConfig.getOrDefault("Currency", "$")
            } catch (e: ClassCastException) {
                Bukkit.getLogger().severe("Field \"Currency\" is of an invalid type, it must be an string (text). The plugin will continue with default value.")
                "$"
            }
        }

    val currencyLeftSide: Boolean
        get() {
            return try {
                main.defaultConfig.getOrDefault("CurrencyLeftSide", false)
            } catch (e: ClassCastException) {
                Bukkit.getLogger().severe("Field \"CurrencyLeftSide\" is of an invalid type, it must be an Boolean (true or false). The plugin will continue with default value.")
                false
            }
        }
}