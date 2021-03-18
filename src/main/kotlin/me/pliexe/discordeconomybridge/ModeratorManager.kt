package me.pliexe.discordeconomybridge

import net.dv8tion.jda.api.entities.Role

class ModeratorManager(private val main: DiscordEconomyBridge) {
    private val roles = mutableListOf<String>()

    fun LoadFromConfig() {
        if(!main.config.isList("discordModerators")) return
        main.config.getStringList("discordModerators").forEach {
            if(it !is String) return
            if(it.length == 18) {
                roles.add(it)
            }
        }
    }

    fun isModerator(_roles: List<Role>): Boolean
    {
        for(role in _roles) {
            if(roles.contains(role.id)) {
                return true
            }
        }

        return false
    }
}