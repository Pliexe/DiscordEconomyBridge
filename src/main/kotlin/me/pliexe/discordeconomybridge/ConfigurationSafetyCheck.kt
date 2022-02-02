package me.pliexe.discordeconomybridge

import me.pliexe.discordeconomybridge.filemanager.Config
import java.util.logging.Logger

fun TokenCheck(main: DiscordEconomyBridge): Boolean {
    val defaultConfig = main.defaultConfig
    val logger = main.logger

    if(!defaultConfig.isSet("TOKEN")) {
        logger.severe("TOKEN field missing from config.yml, disabling plugin...")
        return false
    } else if(!defaultConfig.isString("TOKEN")) {
        logger.severe("TOKEN field is invalid type. It must be a text(string) in config.yml, disabling plugin...")
        return false
    }

    return true
}

fun CheckForConfigurations(main: DiscordEconomyBridge): Boolean {
    val defaultConfig = main.defaultConfig
    val dMessageConfig = main.discordMessagesConfig
    val logger = main.logger

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

    return CheckForMessageConfig(dMessageConfig, logger)
}

fun CheckForMessageConfig(dMessageConfig: Config, logger: Logger): Boolean {

    if(!dMessageConfig.isSet("noPermissionMessage")) {
        logger.severe("noPermissionMessage is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "noPermissionMessage"))
    {
        logger.severe("You don't have field, description or title set at noPermissionMessage.")
        return false
    }

    if(!dMessageConfig.isSet("failMessage")) {
        logger.severe("failMessage is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "failMessage"))
    {
        logger.severe("You don't have field, description or title set at failMessage.")
        return false
    }

    if(!dMessageConfig.isSet("addmoneyCommandEmbed")) {
        logger.severe("addmoneyCommandEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "addmoneyCommandEmbed"))
    {
        logger.severe("You don't have field, description or title set at addmoneyCommandEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("removemoneyCommandEmbed")) {
        logger.severe("removemoneyCommandEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "removemoneyCommandEmbed"))
    {
        logger.severe("You don't have field, description or title set at removemoneyCommandEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("balanceCommandEmbed")) {
        logger.severe("balanceCommandEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "balanceCommandEmbed"))
    {
        logger.severe("You don't have field, description or title set at balanceCommandEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("helpCommandEmbed")) {
        logger.severe("helpCommandEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "helpCommandEmbed"))
    {
        logger.severe("You don't have field, description or title set at helpCommandEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("leaderboardCommandEmbed")) {
        logger.severe("leaderboardCommandEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "leaderboardCommandEmbed") && !(dMessageConfig.isString("leaderboardCommandEmbed.descriptionRepeat") || (dMessageConfig.isString("leaderboardCommandEmbed.fieldRepeatName") && dMessageConfig.isString("leaderboardCommandEmbed.fieldRepeatValue"))))
    {
        logger.severe("You don't have field, description or title set at leaderboardCommandEmbed.")
        return false
    }
    
    // Coinflip

    if(!dMessageConfig.isSet("coinflipCommandEmbed")) {
        logger.severe("coinflipCommandEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "coinflipCommandEmbed") )
    {
        logger.severe("You don't have field, description or title set at coinflipCommandEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("coinflipCommandConfirmEmbed")) {
        logger.severe("coinflipCommandConfirmEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "coinflipCommandConfirmEmbed") )
    {
        logger.severe("You don't have field, description or title set at coinflipCommandConfirmEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("coinflipCommandDeclineEmbed")) {
        logger.severe("coinflipCommandDeclineEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "coinflipCommandDeclineEmbed") )
    {
        logger.severe("You don't have field, description or title set at coinflipCommandDeclineEmbed.")
        return false
    }

    // Blackjack

    if(!dMessageConfig.isSet("blackjackCommandShowEmbed")) {
        logger.severe("blackjackCommandShowEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "blackjackCommandShowEmbed") )
    {
        logger.severe("You don't have field, description or title set at blackjackCommandShowEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("blackjackCommandBlackjackOutcomePlayerEmbed")) {
        logger.severe("blackjackCommandBlackjackOutcomePlayerEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "blackjackCommandBlackjackOutcomePlayerEmbed") )
    {
        logger.severe("You don't have field, description or title set at blackjackCommandBlackjackOutcomePlayerEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("blackjackCommandBlackjackOutcomeDealerEmbed")) {
        logger.severe("blackjackCommandBlackjackOutcomeDealerEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "blackjackCommandBlackjackOutcomeDealerEmbed") )
    {
        logger.severe("You don't have field, description or title set at blackjackCommandBlackjackOutcomeDealerEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("blackjackCommandDrawOutcomeEmbed")) {
        logger.severe("blackjackCommandDrawOutcomeEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "blackjackCommandDrawOutcomeEmbed") )
    {
        logger.severe("You don't have field, description or title set at blackjackCommandDrawOutcomeEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("blackjackCommandDrawBlackjackOutcomeEmbed")) {
        logger.severe("blackjackCommandDrawBlackjackOutcomeEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "blackjackCommandDrawBlackjackOutcomeEmbed") )
    {
        logger.severe("You don't have field, description or title set at blackjackCommandDrawBlackjackOutcomeEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("blackjackCommandBustPlayerEmbed")) {
        logger.severe("blackjackCommandBustPlayerEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "blackjackCommandBustPlayerEmbed") )
    {
        logger.severe("You don't have field, description or title set at blackjackCommandBustPlayerEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("blackjackCommandBustDealerEmbed")) {
        logger.severe("blackjackCommandBustDealerEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "blackjackCommandBustDealerEmbed") )
    {
        logger.severe("You don't have field, description or title set at blackjackCommandBustDealerEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("blackjackCommandPlayerWinEmbed")) {
        logger.severe("blackjackCommandPlayerWinEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "blackjackCommandPlayerWinEmbed") )
    {
        logger.severe("You don't have field, description or title set at blackjackCommandPlayerWinEmbed.")
        return false
    }

    if(!dMessageConfig.isSet("blackjackCommandDealerWinEmbed")) {
        logger.severe("blackjackCommandDealerWinEmbed is not set in discord_messages.yml")
        return false
    } else if(!validateEmbed(dMessageConfig, "blackjackCommandDealerWinEmbed") )
    {
        logger.severe("You don't have field, description or title set at blackjackCommandDealerWinEmbed.")
        return false
    }

//    if(dMessageConfig.isSet("noPermissionMessage")) {
//        logger.severe("noPermissionMessage is not set in discord_messages.yml")
//        return false
//    } else if(!validateEmbed(dMessageConfig, "noPermissionMessage"))
//    {
//        logger.severe("You don't have field, description or title set at noPermissionMessage. Restoring default description!")
//        dMessageConfig.set("noPermissionMessage.description", "You don't have permission to run that command!")
//    }
//
//    if(dMessageConfig.isSet("failMessage")) {
//        logger.severe("failMessage is not set in discord_messages.yml")
//        return false
//    } else if(!validateEmbed(dMessageConfig, "failMessage"))
//    {
//        logger.severe("You don't have field, description or title set at failMessage. Restoring default fields and description!")
//        dMessageConfig.set("failMessage.description", "❌ {message}")
//        dMessageConfig.set("failMessage.fields.Usage.text", "%discord_command_prefix%%discord_command_name% %discord_command_usage%")
//    }
//
//    if(dMessageConfig.isSet("addmoneyCommandEmbed")) {
//        logger.severe("addmoneyCommandEmbed is not set in discord_messages.yml")
//        return false
//    } else if(!validateEmbed(dMessageConfig, "addmoneyCommandEmbed"))
//    {
//        logger.severe("You don't have field, description or title set at addmoneyCommandEmbed. Restoring default fields and description!")
//        dMessageConfig.set("addmoneyCommandEmbed.description", "Added {amount_increase} to %player_name%'s balance")
//    }
//
//    if(dMessageConfig.isSet("removemoneyCommandEmbed")) {
//        logger.severe("removemoneyCommandEmbed is not set in discord_messages.yml")
//        return false
//    } else if(!validateEmbed(dMessageConfig, "removemoneyCommandEmbed"))
//    {
//        logger.severe("You don't have field, description or title set at removemoneyCommandEmbed. Restoring default fields and description!")
//        dMessageConfig.set("removemoneyCommandEmbed.description", "Removed {amount_decrease} from %player_name%'s balance")
//    }
//
//    if(dMessageConfig.isSet("balanceCommandEmbed")) {
//        logger.severe("balanceCommandEmbed is not set in discord_messages.yml")
//        return false
//    } else if(!validateEmbed(dMessageConfig, "balanceCommandEmbed"))
//    {
//        logger.severe("You don't have field, description or title set at balanceCommandEmbed. Restoring default fields and description!")
//        dMessageConfig.set("balanceCommandEmbed.fields.Username.text", "%player_name%")
//        dMessageConfig.set("balanceCommandEmbed.fields.Username.inline", true)
//
//        dMessageConfig.set("balanceCommandEmbed.fields.Status.text", "%player_online%")
//        dMessageConfig.set("balanceCommandEmbed.fields.Status.inline", true)
//
//        dMessageConfig.set("balanceCommandEmbed.fields.Balance.text", "%custom_vault_eco_balance%")
//        dMessageConfig.set("balanceCommandEmbed.fields.Balance.inline", true)
//    }
//
//    if(dMessageConfig.isSet("helpCommandEmbed")) {
//        logger.severe("helpCommandEmbed is not set in discord_messages.yml")
//        return false
//    } else if(!validateEmbed(dMessageConfig, "helpCommandEmbed"))
//    {
//        logger.severe("You don't have field, description or title set at helpCommandEmbed. Disabling plugin!")
//        return false
//    }
//
//    if(dMessageConfig.isSet("leaderboardCommandEmbed")) {
//        logger.severe("leaderboardCommandEmbed is not set in discord_messages.yml")
//        return false
//    } else if(!validateEmbed(dMessageConfig, "leaderboardCommandEmbed") )
//    {
//        logger.severe("You don't have field, description or title set at leaderboardCommandEmbed. Restoring default description!")
//        dMessageConfig.set("leaderboardCommandEmbed.descriptionRepeat", "{index}# %player_name% - %custom_vault_eco_balance%")
//    }

    return true
}

fun validateEmbed(dMessageConfig: Config, path: String): Boolean {
    return dMessageConfig.isString("$path.description") || dMessageConfig.isString("$path.title") || (dMessageConfig.isConfigurationSection("$path.fields") && dMessageConfig.getConfigurationSection("$path.fields").getKeys(false)
        .isNotEmpty())
}