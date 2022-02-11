package me.pliexe.discordeconomybridge

class pluginConfig(val main: DiscordEconomyBridge) {
    val minBet: Double
        get() {
            return try {
                main.defaultConfig.getOrDefault("minBet", 100.0)
            } catch (e: ClassCastException) {
                DiscordEconomyBridge.logger.severe("Field \"minBet\" is invalid type, it must be a number.  The plugin will continue with default value.")
                100.0
            }
        }

    val maxBet: Double
        get() {
            return try {
                main.defaultConfig.getOrDefault("maxBet", 100.0)
            } catch (e: ClassCastException) {
                DiscordEconomyBridge.logger.severe("Field \"maxBet\" is invalid type, it must be a number.  The plugin will continue with default value.")
                100000000000000000.0
            }
        }

    val currency: String
        get() {
            return try {
                main.defaultConfig.getOrDefault("Currency", "$")
            } catch (e: ClassCastException) {
                DiscordEconomyBridge.logger.severe("Field \"Currency\" is of an invalid type, it must be an string (text). The plugin will continue with default value.")
                "$"
            }
        }

    val currencyLeftSide: Boolean
        get() {
            return try {
                main.defaultConfig.getOrDefault("CurrencyLeftSide", false)
            } catch (e: ClassCastException) {
                DiscordEconomyBridge.logger.severe("Field \"CurrencyLeftSide\" is of an invalid type, it must be an Boolean (true or false). The plugin will continue with default value.")
                false
            }
        }

    val commandTimeout: Long
        get() {
            return try {
                main.defaultConfig.getOrDefault("commandTimeout", 150L) * 1000L
            } catch (e: ClassCastException) {
                DiscordEconomyBridge.logger.severe("Field \"commandTimeout\" is of an invalid type, it must be an number. The plugin will continue with default value.")
                150000L
            }
        }

    val gameTimeout: Long
        get() {
            return try {
                main.defaultConfig.getOrDefault("gameTimeout", 300L) * 1000L
            } catch (e: ClassCastException) {
                DiscordEconomyBridge.logger.severe("Field \"gameTimeout\" is of an invalid type, it must be an number. The plugin will continue with default value.")
                300000L
            }
        }
}