package me.pliexe.discordeconomybridge.discord

import de.leonhard.storage.Json
import de.leonhard.storage.SimplixBuilder
import github.scarsz.discordsrv.DiscordSRV
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule
import kotlin.random.Random

private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

class LinkHandler(val main: DiscordEconomyBridge) {
    private var jsonStorage: Json? = null

    private val toLink = HashMap<String, UUID>()
    private val timers = HashMap<String, TimerTask>()
    private var useDSRV = true

    fun initNative() {
        jsonStorage = SimplixBuilder
            .fromPath("linked_accounts", main.dataFolder.path)
            .createJson()

        useDSRV = main.discordSRVActive && (if(main.config.isBoolean("useDiscordSRVLinking")) main.config.getBoolean("useDiscordSRVLinking") else false)
    }

    fun underWaitList(uuid: UUID): Boolean {
        return toLink.containsValue(uuid)
    }

    fun getCode(uuid: UUID): String? {

        for((key, value) in toLink) {
            if(value == uuid) {
                return key
            }
        }

        return null
    }

    fun getUuid(discordId: String): UUID? {
        return if(useDSRV)
            DiscordSRV.getPlugin().accountLinkManager.getUuid(discordId)
        else {
            if(isLinked(discordId))
                UUID.fromString(jsonStorage!!.getString(discordId))
            else return null
        }
    }

    fun getId(uuid: UUID): String? {
        if(useDSRV) {
            return DiscordSRV.getPlugin().accountLinkManager.getDiscordId(uuid)
        } else {
            val uuidStr = uuid.toString()

            jsonStorage!!.keySet().forEach {
                if(jsonStorage!!.getString(it) == uuidStr)
                    return it
            }

            return null
        }
    }

    fun isLinked(discordId: String): Boolean {
        return if(useDSRV)
                DiscordSRV.getPlugin().accountLinkManager.getUuid(discordId) != null
            else
                jsonStorage!!.contains(discordId)
    }

    fun isLinked(uuid: UUID): Boolean {
        if(useDSRV) {
            return DiscordSRV.getPlugin().accountLinkManager.getDiscordId(uuid) != null
        } else {
            val uuidStr = uuid.toString()

            jsonStorage!!.keySet().forEach {
                if(jsonStorage!!.getString(it) == uuidStr)
                    return true
            }

            return false
        }
    }

    fun prepareLink(uuid: UUID): String {
        val code = generateCode()

        toLink[code] = uuid
        timers[code] = Timer().schedule(300000) {
            toLink.remove(code)
            timers.remove(code)
        }

        return code
    }

    fun tryLink(code: String, discordId: String): Boolean {
        return if(toLink.contains(code)) {
            timers[code]!!.cancel()
            timers.remove(code)

            if(useDSRV)
                DiscordSRV.getPlugin().accountLinkManager.link(discordId, toLink[code])
            else
                saveLink(discordId, toLink[code]!!)

            toLink.remove(code)

            true
        } else false
    }

    private fun generateCode(): String {
        val randomCode = (1..4)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")

        return if(toLink.contains(randomCode)) generateCode()
        else randomCode
    }

    private fun saveLink(discordId: String, uuid: UUID) {
        jsonStorage!!.set(discordId, uuid.toString())
    }

    fun unLink(uuid: UUID) {
        if(useDSRV)
            DiscordSRV.getPlugin().accountLinkManager.unlink(uuid)
        else {
            getId(uuid)?.let { unLink(it) }
        }
    }

    fun unLink(discordId: String) {
        if(useDSRV)
            DiscordSRV.getPlugin().accountLinkManager.unlink(discordId)
        else jsonStorage!!.remove(discordId)
    }
}