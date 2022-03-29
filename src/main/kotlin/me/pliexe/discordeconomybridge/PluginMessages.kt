package me.pliexe.discordeconomybridge

import org.bukkit.ChatColor

fun getValue(path: String, main: DiscordEconomyBridge, default: String): String {
    val value = main.pluginMessagesConfig.get(path)

    return when (value) {
        is ArrayList<*> -> {
            value.joinToString("\n")
        }
        is String -> {
            value
        }
        else -> {
            default
        }
    }
}

class PluginMessages(val main: DiscordEconomyBridge) {

    private val defaultHelpMessage: String = StringBuilder("\n" + ChatColor.BOLD + "▁▂▃▄▅ " + ChatColor.DARK_AQUA + ChatColor.BOLD + "DiscordEconomyBridge Commands" + ChatColor.RESET + "" + ChatColor.BOLD +  " ▅▄▃▂▁\n \n")
        .append(ChatColor.BOLD)
        .append(" > ")
        .append(ChatColor.YELLOW)
        .append(ChatColor.BOLD)
        .append("linkdiscord")
        .append(ChatColor.RESET)
        .append(ChatColor.BOLD)
        .append(" - Link your minecraft account to discord bot!\n")
        .append(ChatColor.BOLD)
        .append(" > ")
        .append(ChatColor.YELLOW)
        .append(ChatColor.BOLD)
        .append("unlinkdiscord")
        .append(ChatColor.RESET)
        .append(ChatColor.BOLD)
        .append(" - Unlink your minecraft account from discord bot!")
        .toString()
    private val defaultHelpMessageFail: String = StringBuilder("\n" + ChatColor.BOLD + "▁▂▃▄▅ " + ChatColor.DARK_AQUA + ChatColor.BOLD + "DiscordEconomyBridge Commands" + ChatColor.RESET + "" + ChatColor.BOLD +  " ▅▄▃▂▁\n \n")
        .append(ChatColor.BOLD)
        .append(" Commands for linking and unlinking are only available in dependent mode (Configured in config) or without DiscordSRV")
        .toString()


    val linkAccount: String
        get() = getValue("linkAccount", main, "\"{code}\". Direct Message (Private Message) {bot} the code to link your account to discord!")

    val alreadyLinked: String
        get() = getValue("alreadyLinked", main, "Your account is already linked to discord! Type /unlinkdiscord to unlink it.")

    val accountNotLInked: String
        get() = getValue("accountNotLInked", main, "Your account is not linked to discord!")

    val unlinkAccount: String
        get() = getValue("unlinkAccount", main, "You have successfully unlinked your account from discord!")

    val helpCommand: String
        get() = getValue("helpCommand", main, defaultHelpMessage)

    val helpCommandFail: String
        get() = getValue("helpCommandFail", main, defaultHelpMessageFail)

    val noConsole: String
        get() = getValue("noConsole", main, "This command can't be run in the console!")

    val linkingInProcess: String
        get() = getValue("linkingInProcess", main, "You have already started the link process! Your code was &l&9{code}&r.")

    val linkingDisabled: String
        get() = getValue("linkingDisabled", main, "Linking has been disabled through this plugin.")
}