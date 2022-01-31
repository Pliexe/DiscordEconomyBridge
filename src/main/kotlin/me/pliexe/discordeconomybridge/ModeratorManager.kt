package me.pliexe.discordeconomybridge

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role

class ModeratorManager(private val main: DiscordEconomyBridge) {
    private val roles = mutableListOf<String>()

    fun getRoles (): MutableList<String> {
        return roles
    }

    fun LoadFromConfig() {
        if(main.config.isList("discordModerators")) {
            main.config.getStringList("discordModerators").forEach {
                if(it !is String) return
                if(it.length == 18) {
                    roles.add(it)
                }
            }
        } else if(main.config.isString("discordModerators")) {
            val role = main.config.getString("discordModerators")
            if(role.length == 18)
                roles.add(role)
        }


    }

    fun isModerator(member: Member): Boolean
    {
        if(member.isOwner) return true

        if(main.config.isBoolean("ignorePermissionsForAdministrators"))
            if(main.config.getBoolean("ignorePermissionsForAdministrators"))
                if(member.hasPermission(Permission.ADMINISTRATOR)) return true

        for(role in member.roles) {
            if(roles.contains(role.id)) {
                return true
            }
        }

        return false
    }

    fun isModerator(member: github.scarsz.discordsrv.dependencies.jda.api.entities.Member): Boolean
    {
        if(member.isOwner) return true

        if(main.config.isBoolean("ignorePermissionsForAdministrators"))
            if(main.config.getBoolean("ignorePermissionsForAdministrators"))
                if(member.hasPermission(github.scarsz.discordsrv.dependencies.jda.api.Permission.ADMINISTRATOR)) return true

        for(role in member.roles) {
            if(roles.contains(role.id)) {
                return true
            }
        }

        return false
    }
}