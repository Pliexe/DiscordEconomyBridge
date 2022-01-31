package me.pliexe.discordeconomybridge.discordsrv

import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent
import github.scarsz.discordsrv.api.events.DiscordReadyEvent
import github.scarsz.discordsrv.util.DiscordUtil
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.Listener
import me.pliexe.discordeconomybridge.discord.handlers.CommandHandler

class DiscordSRVListener(val main: DiscordEconomyBridge) {

    private val logger = main.logger


    @Subscribe
    fun discordReadyEvent(event: DiscordReadyEvent) {
        logger.info("Using DiscordSRV!")
        DiscordUtil.getJda().addEventListener(JDAListener(main))
    }
}
