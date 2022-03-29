package me.pliexe.discordeconomybridge

import org.bukkit.configuration.file.FileConfiguration
import java.text.DecimalFormat
import java.util.*

fun getMultilineableString(config: FileConfiguration,path: String): String? {
    if(config.isString(path)) {
        return config.getString(path)
    } else if(config.isList(path)) {
        return config.getStringList(path).joinToString("\n")
    } else return null
}

fun formatMoney(amount: Number, main: DiscordEconomyBridge, formatter: DecimalFormat = DecimalFormat("#,###.##")): String {
    return formatMoney(amount, main.pluginConfig.currency, main.pluginConfig.currencyLeftSide, formatter)
}

fun formatMoney(amount: Number, currency: String, leftSide: Boolean, formatter: DecimalFormat): String {
    if(leftSide)
        return currency + formatter.format(amount)
    else return formatter.format(amount) + currency
}

class UUIDUtils {
    companion object {
        private val uuidregexdash = Regex("(.{8})(.{4})(.{4})(.{4})(.+)")

        fun getUUIDFromString(raw: String?): UUID? {
            if(raw == null) return null

            return if(raw.length == 32) {
                try {
                    UUID.fromString(raw.replace(uuidregexdash, "$1-$2-$3-$4-$5"))
                } catch (e: Exception) {
                    null
                }
            } else if(raw.length == 36) {
                try {
                    UUID.fromString(raw)
                } catch (e: Exception) {
                    null
                }
            } else null
        }

        fun isValidUUID(raw: String, checkWithoutDashes: Boolean = false): Boolean {
            if(checkWithoutDashes) {
                return when(raw.length) {
                    32 -> {
                        return try {
                            UUID.fromString(raw.replace(uuidregexdash, "$1-$2-$3-$4-$5"))
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }
                    36 -> {
                        return try {
                            UUID.fromString(raw)
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }
                    else -> false
                }
            } else {
                return if(raw.length == 36) {
                    try {
                        UUID.fromString(raw)
                        true
                    } catch (e: Exception) {
                        false
                    }
                } else false
            }
        }
    }
}