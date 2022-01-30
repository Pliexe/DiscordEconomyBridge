package me.pliexe.discordeconomybridge.discordsrv

import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.DiscordReadyEvent
import github.scarsz.discordsrv.util.DiscordUtil
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.Listener

class DiscordSRVListener(val main: DiscordEconomyBridge) {

    @Subscribe
    fun discordReadyevent(event: DiscordReadyEvent) {
        DiscordUtil.getJda().addEventListener(Listener(main, main.server, main.defaultConfig))

        main.logger.info("Using DiscordSRV!")
    }
}
