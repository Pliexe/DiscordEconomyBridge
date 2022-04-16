package me.pliexe.discordeconomybridge.discord

class CooldownManager {
    private val cooldowns = mutableMapOf<String, Long>()

    fun isOnCooldown(key: String): Boolean {
        return if(cooldowns.containsKey(key)) {
            val cooldown = cooldowns[key]!!
            if(System.currentTimeMillis() > cooldown) {
                cooldowns.remove(key)
                false
            } else true
        } else false
    }

    fun Add(command: String, cooldown: Long) {
        cooldowns[command] = System.currentTimeMillis() + cooldown
    }

    fun Remove(command: String) {
        cooldowns.remove(command)
    }

    fun getCooldown(commandName: String): String {
        val cooldown = cooldowns[commandName]!!
        val ms = cooldown - System.currentTimeMillis()
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = (ms / (1000 * 60 * 60)) % 24
        val days = (ms / (1000 * 60 * 60 * 24))

        val str = StringBuilder()
        if(days > 0) str.append("$days days, ")
        if(hours > 0 || days > 0) str.append("$hours hours, ")
        if(minutes > 0 || hours > 0 || days > 0) str.append("$minutes minutes, ")
        str.append("$seconds seconds")
        return str.toString()
    }
}