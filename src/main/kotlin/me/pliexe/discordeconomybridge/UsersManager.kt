package me.pliexe.discordeconomybridge

import me.pliexe.discordeconomybridge.files.DataConfig
import java.util.*

class UsersManager(private val main: DiscordEconomyBridge) {
    private val users = hashMapOf<String, String>()

    fun LoadFromConfig() {
        if(DataConfig.get().contains("users")) {
            val users = DataConfig.get().getConfigurationSection("users").getKeys(false).forEach {
                if(DataConfig.get().contains("users.$it")) {
                    if(UUIDUtils.isValidUUID(it)) {
                        users[it] = DataConfig.get().getString("users.$it")
                    }
                }
            }
        }
    }

    fun GetPlayerUUID(username: String): UUID? {
        try {
            for(user in users) {
                if(user.value.equals(username, ignoreCase = true)) {
                    return UUID.fromString(user.key)
                }
            }
            return null
        } catch (e: Exception) {
            return null
        }
    }

    fun GetPlayerUsername(uuid: UUID): String? {
        return users[uuid.toString()]
    }

    fun GetPlayerUsername(uuid: String): String? {
        return users[uuid]
    }

    fun SavePlayer(username: String, uuid: UUID) {
        users[uuid.toString()] = username

        DataConfig.get().set("users.$uuid", username)
        DataConfig.save()
    }
}