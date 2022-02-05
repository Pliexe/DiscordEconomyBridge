package me.pliexe.discordeconomybridge.discord

import github.scarsz.discordsrv.DiscordSRV
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import java.util.*

class LinkHandler(val main: DiscordEconomyBridge) {
    fun getUuid(discordId: String): UUID? {
        return DiscordSRV.getPlugin().accountLinkManager.getUuid(discordId)
    }
}