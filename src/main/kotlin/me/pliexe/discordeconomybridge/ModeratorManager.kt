package me.pliexe.discordeconomybridge

import me.pliexe.discordeconomybridge.discord.DiscordMember

val roleCheck = Regex("^\\d+$")

class ModeratorManager(private val main: DiscordEconomyBridge) {
    private val roles = mutableListOf<String>()

    fun getRoles (): MutableList<String> {
        return roles
    }

    fun resetRoles() { roles.clear() }

    fun LoadFromConfig() {
        if(main.config.isList("discordModerators")) {
            main.config.getStringList("discordModerators").forEach {
                if(it !is String) return
                if(roleCheck.matches(it)) {
                    roles.add(it)
                }
            }
        } else if(main.config.isString("discordModerators")) {
            val role = main.config.getString("discordModerators")
            if(roleCheck.matches(role))
                roles.add(role)
        }


    }

    fun isModerator(member: DiscordMember): Boolean
    {
        if (main.config.isBoolean("ignorePermissionsForOwner"))
            if (main.config.getBoolean("ignorePermissionsForOwner"))
                if(member.isOwner) return true

        if(main.config.isBoolean("ignorePermissionsForAdministrators"))
            if(main.config.getBoolean("ignorePermissionsForAdministrators"))
                if(member.isAdministrator()) return true

        for(role in roles) {
            if(member.rolesContain(role)) return true
        }

        return false
    }
}