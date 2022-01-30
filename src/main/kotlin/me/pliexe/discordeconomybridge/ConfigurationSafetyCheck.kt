package me.pliexe.discordeconomybridge

import me.pliexe.discordeconomybridge.filemanager.Config
import java.util.logging.Logger

fun TokenCheck(): Boolean {
    val defaultConfig = DiscordEconomyBridge.Instance!!.defaultConfig
    val logger = DiscordEconomyBridge.Instance!!.logger

    if(!defaultConfig.isSet("TOKEN")) {
        logger.severe("TOKEN field missing from config.yml, disabling plugin...")
        return false
    } else if(!defaultConfig.isString("TOKEN")) {
        logger.severe("TOKEN field is invalid type. It must be a text(string) in config.yml, disabling plugin...")
        return false
    }

    return true
}

fun CheckForConfigurations(): Boolean {
    val defaultConfig = DiscordEconomyBridge.Instance!!.defaultConfig
    val dMessageConfig = DiscordEconomyBridge.Instance!!.discordMessagesConfig
    val logger = DiscordEconomyBridge.Instance!!.logger

    if(!defaultConfig.isString("PREFIX")) {
        logger.severe("PREFIX field missing from config.yml, disabling plugin...")
        return false
    } else if(!defaultConfig.isString("PREFIX")) {
        logger.severe("PREFIX field is invalid type. It must be a text(string) in config.yml, disabling plugin...")
        return false
    }

    if(!defaultConfig.isString("Currency")) {
        logger.severe("Currency field missing from config.yml, disabling plugin...")
        return false
    } else if(!defaultConfig.isString("Currency")) {
        logger.severe("Currency field is invalid type. It must be a text(string) in config.yml, disabling plugin...")
        return false
    }

    if(!defaultConfig.isSet("CurrencyLeftSide")) {
        logger.severe("CurrencyLeftSide field missing from config.yml, disabling plugin...")
        return false
    } else if(!defaultConfig.isBoolean("CurrencyLeftSide")) {
        logger.severe("CurrencyLeftSide field is invalid type. It must be a true or false (boolean) in config.yml, disabling plugin...")
        return false
    }

    return CheckForMessageConfig(defaultConfig, dMessageConfig, logger)
}

fun CheckForMessageConfig(defaultConfig: Config, dMessageConfig: Config, logger: Logger): Boolean {

    if(dMessageConfig.isSet("noPermissionMessage")) {
        logger.severe("noPermissionMessage is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "noPermissionMessage"))
    {
        logger.severe("You don't have field, description or title set at noPermissionMessage. Restoring default description!")
        dMessageConfig.set("noPermissionMessage.description", "You don't have permission to run that command!")
    }

    if(dMessageConfig.isSet("failMessage")) {
        logger.severe("failMessage is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "failMessage"))
    {
        logger.severe("You don't have field, description or title set at failMessage. Restoring default fields and description!")
        dMessageConfig.set("failMessage.description", "❌ {message}")
        dMessageConfig.set("failMessage.fields.Usage.text", "%discord_command_prefix%%discord_command_name% %discord_command_usage%")
    }

    if(dMessageConfig.isSet("addmoneyCommandEmbed")) {
        logger.severe("addmoneyCommandEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "addmoneyCommandEmbed"))
    {
        logger.severe("You don't have field, description or title set at addmoneyCommandEmbed. Restoring default fields and description!")
        dMessageConfig.set("addmoneyCommandEmbed.description", "Added {amount_increase} to %player_name%'s balance")
    }

    if(dMessageConfig.isSet("removemoneyCommandEmbed")) {
        logger.severe("removemoneyCommandEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "removemoneyCommandEmbed"))
    {
        logger.severe("You don't have field, description or title set at removemoneyCommandEmbed. Restoring default fields and description!")
        dMessageConfig.set("removemoneyCommandEmbed.description", "Removed {amount_decrease} from %player_name%'s balance")
    }

    if(dMessageConfig.isSet("balanceCommandEmbed")) {
        logger.severe("balanceCommandEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "balanceCommandEmbed"))
    {
        logger.severe("You don't have field, description or title set at balanceCommandEmbed. Restoring default fields and description!")
        dMessageConfig.set("balanceCommandEmbed.fields.Username.text", "%player_name%")
        dMessageConfig.set("balanceCommandEmbed.fields.Username.inline", true)

        dMessageConfig.set("balanceCommandEmbed.fields.Status.text", "%player_online%")
        dMessageConfig.set("balanceCommandEmbed.fields.Status.inline", true)

        dMessageConfig.set("balanceCommandEmbed.fields.Balance.text", "%custom_vault_eco_balance%")
        dMessageConfig.set("balanceCommandEmbed.fields.Balance.inline", true)
    }

    if(dMessageConfig.isSet("helpCommandEmbed")) {
        logger.severe("helpCommandEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "helpCommandEmbed"))
    {
        logger.severe("You don't have field, description or title set at helpCommandEmbed. Disabling plugin!")
        return false
    }

    if(dMessageConfig.isSet("leaderboardCommandEmbed")) {
        logger.severe("leaderboardCommandEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "leaderboardCommandEmbed") && ((dMessageConfig.isSet("descriptionRepeat") && dMessageConfig.isString("descriptionRepeat")) || (dMessageConfig.isSet("fieldRepeatName") && dMessageConfig.isString("fieldRepeatName") && dMessageConfig.isSet("fieldRepeatValue") && dMessageConfig.isString("fieldRepeatValue"))))
    {
        logger.severe("You don't have field, description or title set at leaderboardCommandEmbed. Restoring default description!")
        dMessageConfig.set("leaderboardCommandEmbed.descriptionRepeat", "{index}# %player_name% - %custom_vault_eco_balance%")
    }

    return true
}

fun validateEmbed(dMessageConfig: Config, path: String): Boolean {
    return dMessageConfig.isString("$path.description") || dMessageConfig.isString("$path.title") || (dMessageConfig.isConfigurationSection("$path.fields") && dMessageConfig.getConfigurationSection("$path.fields").getKeys(false)
        .isNotEmpty())
}