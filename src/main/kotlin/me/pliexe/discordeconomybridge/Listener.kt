package me.pliexe.discordeconomybridge

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class Listener : Listener {
//
//    @EventHandler
//    fun on(event: PlayerQuitEvent) {
//        if(event.player.uniqueId != null) {
//            main.usersManager.SavePlayer(event.player.name, event.player.uniqueId)
//        }
//    }
//
//    @EventHandler
//    fun on(event: PlayerJoinEvent) {
//        if(event.player.uniqueId != null) {
//            main.usersManager.SavePlayer(event.player.name, event.player.uniqueId)
//        }
//    }

    @EventHandler
    fun on(event: PlayerQuitEvent) {
        if(event.player.uniqueId != null) {
            DiscordEconomyBridge.userCache.set(event.player.name, event.player.uniqueId.toString())
        }
    }

}
