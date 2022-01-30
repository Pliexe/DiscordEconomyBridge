package me.pliexe.discordeconomybridge.discord.commands

import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.discord.Command
import me.pliexe.discordeconomybridge.discord.GetYmlEmbed
import me.pliexe.discordeconomybridge.discord.setDiscordPlaceholders
import me.pliexe.discordeconomybridge.discord.setPlaceholdersForDiscordMessage
import me.pliexe.discordeconomybridge.getEmbedFromYml
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class Help(main: DiscordEconomyBridge): Command(main) {

    override val usage: String
        get() = ""

    override fun run(event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) {
        event.channel.sendMessageEmbeds(GetYmlEmbed(event.channel, {
            if(event.member == null)
                setDiscordPlaceholders(event.author, it)
            else setDiscordPlaceholders(event.member!!, it)
        }, "helpCommandEmbed", main.discordMessagesConfig).build()).queue()
    }
}