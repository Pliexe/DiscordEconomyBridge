package me.pliexe.discordeconomybridge.discord


import com.github.sidhant92.boolparser.application.BooleanExpressionEvaluator
import de.leonhard.storage.sections.FlatFileSection
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import me.pliexe.discordeconomybridge.formatMoney
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.JexlException
import org.apache.commons.jexl3.MapContext
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import kotlin.random.Random


val varDetect = Regex("(?<!\\\\)\\{var_[A-z]+}")
val varDetectWithPrefix = Regex("(?<!\\\\)\\{var_[A-z]+:[A-z]+}")
val varDetectStandard = Regex("(?<!\\\\)\\{([^}]*)}")
val varDetectStandardEscaped = Regex("\\\\\\{([^}]*)}")
val lettersDetect = Regex("^[a-zA-Z0-9]+\$")

enum class CommandType {
    DiscordUser,
    DiscordMember,
    MinecraftPlayer,
    Double,
    Int,
    String
}

abstract class Argument(
    val varName: String,
    val name: String,
    val description: String,
    val type: CommandType,
    val required: Boolean,
    val dontIgnore: Boolean
)

class MinecraftPlayerArg  (
    varName: String,
    name: String,
    description: String,
    type: CommandType,
    required: Boolean,
    dontIgnore: Boolean,
    private val failIf: Any?,
    private val logicFailRes: String?
) : Argument(varName, name, description, type, required, dontIgnore) {

    fun Conditions(player: UniversalPlayer, values: HashMap<String, Any>): String? {

        when(failIf) {
            is Boolean -> if(!failIf) return logicFailRes ?: "Failed due to $failIf results in true"
            is Int -> if(failIf.toDouble() > 0) return "Failed due to $failIf results in true"
            is Double -> if(failIf > 0) return "Failed due to $failIf results in true"
            is String -> {
                when(val res = resolveValue(failIf, values)) {
                    is Boolean -> if(!res) return logicFailRes ?: "Failed due to $failIf results in true"
                    is Int -> if(res.toDouble() > 0) return "Failed due to $failIf results in true"
                    is Double -> if(res > 0) return "Failed due to $failIf results in true"
                    is String, null -> {
                        if((res ?: failIf) == player.name) return "${player.name}  may not interact with this command"
                        else if((res ?: failIf) == player.uniqueId) return "${player.name}  may not interact with this command"
                    }
                }
            }
            else -> return null
        }

        return null
    }
}

class DiscordUserArg  (
    varName: String,
    name: String,
    description: String,
    type: CommandType,
    required: Boolean,
    dontIgnore: Boolean,
    private val failIf: Any?,
    private val logicFailRes: String?
) : Argument(varName, name, description, type, required, dontIgnore) {

    fun Conditions(userId: String, values: HashMap<String, Any>): String? {

        when(failIf) {
            is Boolean -> if(!failIf) return logicFailRes ?: "Failed due to $failIf results in true"
            is Int -> if(failIf.toDouble() > 0) return "Failed due to $failIf results in true"
            is Double -> if(failIf > 0) return "Failed due to $failIf results in true"
            is String -> {
                when(val res = resolveValue(failIf, values)) {
                    is Boolean -> if(!res) return logicFailRes ?: "Failed due to $failIf results in true"
                    is Int -> if(res.toDouble() > 0) return "Failed due to $failIf results in true"
                    is Double -> if(res > 0) return "Failed due to $failIf results in true"
                    is String, null -> {
                        if((res ?: failIf) == userId) return "<@$userId>  may not interact with this command"
                    }
                }
            }
            else -> return null
        }

        return null
    }
}

class DiscordMemberArg  (
    varName: String,
    name: String,
    description: String,
    type: CommandType,
    required: Boolean,
    dontIgnore: Boolean,
    private val failIf: Any?,
    private val disallowedRoles: List<String>?,
    private val requiredRoles: List<String>?,
    private val logicFailRes: String?
) : Argument(varName, name, description, type, required, dontIgnore) {

    fun Conditions(userId: String, roles: List<String>, values: HashMap<String, Any>): String? {

        when(failIf) {
            is Boolean -> if(!failIf) return logicFailRes ?: "Failed due to $failIf results in true"
            is Int -> if(failIf.toDouble() > 0) return "Failed due to $failIf results in true"
            is Double -> if(failIf > 0) return "Failed due to $failIf results in true"
            is String -> {
                when(val res = resolveValue(failIf, values)) {
                    is Boolean -> if(!res) return logicFailRes ?: "Failed due to $failIf results in true"
                    is Int -> if(res.toDouble() > 0) return "Failed due to $failIf results in true"
                    is Double -> if(res > 0) return "Failed due to $failIf results in true"
                    is String, null -> {

                        if((res ?: failIf) == userId) return "<@$userId>  may not interact with this command"


                        if(disallowedRoles != null)
                            if(roles.any { disallowedRoles.contains(it) }) return "<@$userId> may not interact with this command"

                        if(requiredRoles != null)
                            if(!requiredRoles.all { roles.contains(it) }) return "<@$userId> may not interact with this command"
                    }
                }
            }
            else -> return null
        }

        return null
    }
}

class StringArg  (
    varName: String,
    name: String,
    description: String,
    type: CommandType,
    required: Boolean,
    dontIgnore: Boolean,
    private val failIf: Any?,
    private val logicFailRes: String?
) : Argument(varName, name, description, type, required, dontIgnore) {

    fun Conditions(input: String, values: HashMap<String, Any>): String? {

        when(failIf) {
            is Boolean -> if(!failIf) return logicFailRes ?: "Failed due to $failIf results in true"
            is Int -> if(failIf.toDouble() > 0) return "Failed due to $failIf results in true"
            is Double -> if(failIf > 0) return "Failed due to $failIf results in true"
            is String -> {
                when(val res = resolveValue(failIf, values)) {
                    is Boolean -> if(!res) return logicFailRes ?: "Failed due to $failIf results in true"
                    is Int -> if(res.toDouble() > 0) return "Failed due to $failIf results in true"
                    is Double -> if(res > 0) return "Failed due to $failIf results in true"
                    null, is String -> {
                        if((res ?: failIf) == input) return "$name may not match the following text: ${(res ?: failIf)}"
                    }
                }
            }
            else -> return null
        }

        return null
    }
}

class NumberArg  (
    varName: String,
    name: String,
    description: String,
    type: CommandType,
    required: Boolean,
    dontIgnore: Boolean,
    private val failIf: Any?,
    private val logicFailRes: String?,
    private val failIfG: Number?,
    private val failIfL: Number?,
    private val failIfGE: Number?,
    private val failIfLE: Number?,
    ) : Argument(varName, name, description, type, required, dontIgnore) {

    fun Conditions(input: Double, values: HashMap<String, Any>): String? {
        when(failIf) {
            is Boolean -> if(!failIf) return logicFailRes ?: "Failed due to $failIf results in true"
            is Int -> if(failIf.toDouble() == input) return "$name may not be equal to $failIf"
            is Double -> if(failIf == input) return "$name may not be equal to $failIf"
            is String -> {
                when(val res = resolveValue(failIf, values)) {
                    is Boolean -> if(!res) return logicFailRes ?: "Failed due to $failIf results in true"
                    is Int -> if(res.toDouble() == input) return "$name may not be equal to $res"
                    is Double -> if(res == input) return "$name may not be equal to $res"
                }
            }
        }

        if(failIfG != null)
            if(failIfG.toDouble()> input)
                return "$name may not be bigger than $failIfG"

        if(failIfL != null)
            if(failIfL.toDouble() > input)
                return "$name may not be lower than $failIfL"

        if(failIfGE != null)
            if(failIfGE.toDouble() > input)
                return "$name may not be bigger or equal to $failIfGE"

        if(failIfLE != null)
            if(failIfLE.toDouble() > input)
                return "$name may not be lower or equal to $failIfLE"

        return null
    }

    fun Conditions(input: Int, values: HashMap<String, Any>): String? {
        when(failIf) {
            is Boolean -> if(!failIf) return logicFailRes ?: "Failed due to $failIf results in true"
            is Int -> if(failIf == input) return "$name may not be equal to $failIf"
            is Double -> if(failIf.toInt() == input) return "$name may not be equal to $failIf"
            is String -> {
                when(val res = resolveValue(failIf, values)) {
                    is Boolean -> if(!res) return logicFailRes ?: "Failed due to $failIf results in true"
                    is Int -> if(res == input) return "$name may not be equal to $res"
                    is Double -> if(res.toInt() == input) return "$name may not be equal to $res"
                }
            }
        }

        if(failIfG != null)
            if(failIfG.toInt() > input)
                return "$name may not be bigger than $failIfG"

        if(failIfL != null)
            if(failIfL.toInt() > input)
                return "$name may not be lower than $failIfL"

        if(failIfGE != null)
            if(failIfGE.toInt() > input)
                return "$name may not be bigger or equal to $failIfGE"

        if(failIfLE != null)
            if(failIfLE.toInt() > input)
                return "$name may not be lower or equal to $failIfLE"

        return null
    }
}

fun resolveValue(text: String, values: HashMap<String, Any>): Any? {
    return values[text]
}

fun failIf(section: FlatFileSection, values: HashMap<String, Any>, cond: Double): Boolean {
    section.getString("failIfBigger")?.also {
        when(val value = it.toDoubleOrNull()) {
            null -> {
                values[it]?.also { v ->
                    if(v is Int) {
                        if (v > cond.toInt()) return true
                    }
                    else if(v is Double) {
                        if(v > cond) return true
                    }
                }
            }
            else -> {
                if(value > cond) return true
            }
        }
    }

    section.getString("failIfSmaller")?.also {
        when(val value = it.toDoubleOrNull()) {
            null -> {
                values[it]?.also { v ->
                    if(v is Int) {
                        if (v < cond.toInt()) return true
                    }
                    else if(v is Double) {
                        if(v < cond) return true
                    }
                }
            }
            else -> {
                if(value < cond) return true
            }
        }
    }

    section.getString("failIfBiggerOrEqual")?.also {
        when(val value = it.toDoubleOrNull()) {
            null -> {
                values[it]?.also { v ->
                    if(v is Int) {
                        if (v >= cond.toInt()) return true
                    }
                    else if(v is Double) {
                        if(v >= cond) return true
                    }
                }
            }
            else -> {
                if(value >= cond) return true
            }
        }
    }

    section.getString("failIfSmallerOrEqual")?.also {
        when(val value = it.toDoubleOrNull()) {
            null -> {
                values[it]?.also { v ->
                    if(v is Int) {
                        if (v <= cond.toInt()) return true
                    }
                    else if(v is Double) {
                        if(v <= cond) return true
                    }
                }
            }
            else -> {
                if(value <= cond) return true
            }
        }
    }

    section.getString("failIf")?.also {
        return when(val value = it.toDoubleOrNull()) {
            null -> {
                values[it]?.let { v ->
                    return when (v) {
                        is Int -> v == cond.toInt()
                        is Double -> v == cond
                        else -> false
                    }
                }
                false
            }
            else -> value == cond
        }
    }
    return false
}

fun failIf(section: FlatFileSection, values: HashMap<String, Any>, cond: Int): Boolean {
    section.getString("failIfBigger")?.also {
        when(val value = it.toIntOrNull()) {
            null -> {
                values[it]?.also { v ->
                    if(v is Int) {
                        if (v > cond) return true
                    }
                    else if(v is Double) {
                        if(v > cond.toDouble()) return true
                    }
                }
            }
            else -> {
                if(value > cond) return true
            }
        }
    }

    section.getString("failIfSmaller")?.also {
        when(val value = it.toIntOrNull()) {
            null -> {
                values[it]?.also { v ->
                    if(v is Int) {
                        if (v < cond) return true
                    }
                    else if(v is Double) {
                        if(v < cond.toDouble()) return true
                    }
                }
            }
            else -> {
                if(value < cond) return true
            }
        }
    }

    section.getString("failIfBiggerOrEqual")?.also {
        when(val value = it.toIntOrNull()) {
            null -> {
                values[it]?.also { v ->
                    if(v is Int) {
                        if (v >= cond) return true
                    }
                    else if(v is Double) {
                        if(v >= cond.toDouble()) return true
                    }
                }
            }
            else -> {
                if(value >= cond) return true
            }
        }
    }

    section.getString("failIfSmallerOrEqual")?.also {
        when(val value = it.toIntOrNull()) {
            null -> {
                values[it]?.also { v ->
                    if(v is Int) {
                        if (v <= cond) return true
                    }
                    else if(v is Double) {
                        if(v <= cond.toDouble()) return true
                    }
                }
            }
            else -> {
                if(value <= cond) return true
            }
        }
    }

    section.getString("failIf")?.also {
        return when(val value = it.toIntOrNull()) {
            null -> {
                values[it]?.let { v ->
                    return when (v) {
                        is Int -> v == cond
                        is Double -> v == cond.toDouble()
                        else -> false
                    }
                }
                false
            }
            else -> value == cond
        }
    }
    return false
}

fun failIfEquals(section: FlatFileSection, values: HashMap<String, Any>, cond: String): Boolean {
    section.getString("failIf")?.also {
        when(val temp = it.toDoubleOrNull()) {
            null -> {
                if(values.containsKey(it))
                {
                    when(values[it]) {
                        is Double -> return values[it] != 0
                        is Int -> return values[it] != 0
                        is String -> return values[it]!! == cond
                    }
                } else {
                    return it == cond
                }
            }
            else -> return temp != 0.0
        }
    }
    return false
}


var booleanExpressionEvaluator = BooleanExpressionEvaluator()
fun actionConditionChecker(section: FlatFileSection, values: HashMap<String, Any>, format: List<String>?, formatter: DecimalFormat, main: DiscordEconomyBridge): Boolean {

    section.get("onlyIf")?.also { cond ->
        when(cond) {
            is Boolean -> if(!cond) return false
            is Double -> if(cond != 0.0) return false
            is Int -> if(cond != 0) return false
            is String -> {
                values[cond]?.let {
                    when(it) {
                        is Boolean -> if(!it) return false
                        is Double -> if(it != 0.0) return false
                        is Int -> if(it != 0) return false
                    }
                } ?: run {
                    val r = resolveStringWithValues(cond, values, format, formatter, main)
                    ExpressionEvaluator.evaluateAsBoolean(r)?.also {
                        return it
                    }

                    throw Exception("Invalid expression: $r in $section")
                }
            }
        }
    }

    section.get("onlyIfNot")?.also { cond ->
        when(cond) {
            is Boolean -> if(cond) return false
            is Double -> if(cond == 0.0) return false
            is Int -> if(cond == 0) return false
            is String -> {
                values[cond]?.let {
                    when(it) {
                        is Boolean -> if(it) return false
                        is Double -> if(it == 0.0) return false
                        is Int -> if(it == 0) return false
                    }
                } ?: run {
                    val r = resolveStringWithValues(cond, values, format, formatter, main)
                    ExpressionEvaluator.evaluateAsBoolean(r)?.also {
                        return it
                    }

                    throw Exception("Invalid expression: $r in $section")
                }
            }
        }
    }

    section.get("onlyIfNull")?.also { cond ->
        when(cond) {
            is String -> {
                if(values[cond] != null) return false
            }
        }
    }

    section.get("onlyIfNotNull")?.also { cond ->
        when(cond) {
            is String -> {
                if(values[cond] == null) return false
            }
        }
    }

    return true
}

class ExpressionEvaluator {
    companion object {
        private val jexlEngine = JexlBuilder().create()
        private val jexlContext = MapContext()

        fun evaluate(expression: String): Any? = try {
            val jexlExpression = jexlEngine.createExpression(expression)
            jexlExpression.evaluate(jexlContext)
        } catch (e: JexlException) {
            DiscordEconomyBridge.logger.warning("Could not evaluate expression '$expression'")
            null
        }

        fun evaluateAsBoolean(expression: String, warn: Boolean = true): Boolean? {
            val booleanValue = evaluate(expression) as? Boolean
            if (booleanValue == null && warn) {
                DiscordEconomyBridge.logger.warning("Could not evaluate expression '$expression' as Boolean")
            }
            return booleanValue
        }

        fun evaluateAsDouble(expression: String, warn: Boolean = true): Double? {
            val doubleValue = evaluate(expression) as? Double
            if (doubleValue == null && warn) {
                DiscordEconomyBridge.logger.warning("Could not evaluate expression '$expression' as Boolean")
            }
            return doubleValue
        }
    }
}


fun resolveStringWithValues(str: String, values: HashMap<String, Any>, format: List<String>?, formatter: DecimalFormat, main: DiscordEconomyBridge): String {
    return str.replace(varDetectWithPrefix) {
        val vls = it.value.split(":")
        val propName = vls[0].substring(5)
        values[propName]?.let { value ->
            when(value) {
                is DiscordMember -> {
                    when(vls[1].substring(0, vls[1].length - 1)) {
                        "name" -> value.user.name
                        "discriminator" -> value.user.discriminator
                        "id" -> value.user.id
                        "avatar" -> value.user.avatarUrl
                        "mention" -> value.user.asMention
                        "nick" -> value.nickname
                        "roles" -> value.roles.joinToString(", ") { it.name }
                        "joined", "joined_date" -> value.timeJoined.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                        "joined_time" -> value.timeJoined.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))
                        "joined_time_short" -> value.timeJoined.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"))
                        "joined_date_short" -> value.timeJoined.format(DateTimeFormatter.ofPattern("MM/dd/yy"))
                        "timeCreated", "timeCreated_date" -> value.user.timeCreated.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                        "timeCreated_time" -> value.user.timeCreated.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))
                        "timeCreated_time_short" -> value.user.timeCreated.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"))
                        "timeCreated_date_short" -> value.user.timeCreated.format(DateTimeFormatter.ofPattern("MM/dd/yy"))
                        else -> value.toString()
                    }
                }
                is DiscordUser -> {
                    when(vls[1].substring(0, vls[1].length - 1)) {
                        "name" -> value.name
                        "discriminator" -> value.discriminator
                        "id" -> value.id
                        "avatar" -> value.avatarUrl
                        "mention" -> value.asMention
                        "timeCreated", "timeCreated_date" -> value.timeCreated.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                        "timeCreated_time" -> value.timeCreated.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))
                        "timeCreated_time_short" -> value.timeCreated.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"))
                        "timeCreated_date_short" -> value.timeCreated.format(DateTimeFormatter.ofPattern("MM/dd/yy"))
                        else -> value.toString()
                    }
                }
                is UniversalPlayer -> {
                    when(vls[1].substring(0, vls[1].length - 1)) {
                        "name" -> value.name
                        "uniqueId", "uuid" -> value.uniqueId.toString()
                        "online" -> value.isOnline.toString()
                        "online_words" -> if(value.isOnline) "Online" else "Offline"
                        "online_emoji" -> if(value.isOnline) "\uD83D\uDFE2" else "\uD83D\uDD34"
                        "online_lower" -> if(value.isOnline) "online" else "offline"
                        "online_upper" -> if(value.isOnline) "ONLINE" else "OFFLINE"
                        "balance" -> formatMoney(value.getBalance(main), main.pluginConfig.currency, main.pluginConfig.currencyLeftSide, formatter)
                        "balance_unformatted" -> value.getBalance(main).toString()
                        else -> value.toString()
                    }
                }
                is String -> value
                is Number -> if(format != null && format.contains(propName)) formatMoney(value, main, formatter) else value.toString()
                else -> value.toString()
            }
        } ?: it.value
    }.replace(varDetect) {
        val propName = it.value.substring(5, it.value.length - 1)
        values[propName]?.let { value ->
            if (value is String) value else (if(value is Number && format != null && format.contains(propName)) formatMoney(value, main, formatter) else value.toString())
        } ?: it.value
    }.replace(varDetectStandard) {
        (ExpressionEvaluator.evaluate(it.value)?.toString()?.let {
            if (it.startsWith("[") && it.endsWith("]")) it.substring(1, it.length - 1) else it
        } ?: it.value)
    }.replace(varDetectStandardEscaped) {
        it.value.substring(1)
    }
}

class CustomCommand(main: DiscordEconomyBridge, override val name: String, override val description: String, override val usage: String) : Command(main) {

    private var args: MutableList<Argument> = mutableListOf()


    fun loadArguments(inputSection: FlatFileSection) {
        val inputs = inputSection.singleLayerKeySet()

        if(inputs.isNotEmpty()) {

            inputSection.singleLayerKeySet().forEach { varName ->
                when(val type = inputSection.get("$varName.type")) {
                    is String -> {

                        when(type) {
                            "MinecraftPlayer" -> {
                                args.add(MinecraftPlayerArg(
                                    varName,
                                    inputSection.getOrDefault("$varName.name", "Player"),
                                    inputSection.getOrDefault("$varName.description", "No description set"),
                                    CommandType.MinecraftPlayer,
                                    inputSection.getOrDefault("$varName.required", true),
                                    inputSection.getOrDefault("$varName.dontIgnore", false),
                                    inputSection.get("$varName.failIf"),
                                    inputSection.getString("$varName.logicFailReason")
                                ))
                            }
                            "DiscordUser" -> {
                                args.add(DiscordUserArg(
                                    varName,
                                    inputSection.getOrDefault("$varName.name", "User"),
                                    inputSection.getOrDefault("$varName.description", "No description set"),
                                    CommandType.DiscordUser,
                                    inputSection.getOrDefault("$varName.required", true),
                                    inputSection.getOrDefault("$varName.dontIgnore", false),
                                    inputSection.get("$varName.failIf"),
                                    inputSection.getString("$varName.logicFailReason")
                                ))
                            }
                            "DiscordMember" -> {
                                args.add(DiscordMemberArg(
                                    varName,
                                    inputSection.getOrDefault("$varName.name", "Member"),
                                    inputSection.getOrDefault("$varName.description", "No description set"),
                                    CommandType.DiscordMember,
                                    inputSection.getOrDefault("$varName.required", true),
                                    inputSection.getOrDefault("$varName.dontIgnore", false),
                                    inputSection.get("$varName.failIf"),
                                    (when(val temp = inputSection.get("$varName.failIfRole")) {
                                        is List<*> -> temp as List<String>
                                        is String -> listOf(temp)
                                        else -> null
                                    }),
                                    (when(val temp = inputSection.get("$varName.failIfMissingRole")) {
                                        is List<*> -> temp as List<String>
                                        is String -> listOf(temp)
                                        else -> null
                                    }),
                                    inputSection.getString("$varName.logicFailReason")
                                ))
                            }
                            "Number", "Double" -> {
                                args.add(NumberArg(
                                    varName,
                                    inputSection.getOrDefault("$varName.name", "Number"),
                                    inputSection.getOrDefault("$varName.description", "No description set"),
                                    CommandType.Double,
                                    inputSection.getOrDefault("$varName.required", true),
                                    inputSection.getOrDefault("$varName.dontIgnore", false),
                                    inputSection.get("$varName.failIf"),
                                    inputSection.getString("$varName.logicFailReason"),
                                    inputSection.getDouble("$varName.failIfBigger"),
                                    inputSection.getDouble("$varName.failIfSmaller"),
                                    inputSection.getDouble("$varName.failIfBiggerOrEqual"),
                                    inputSection.getDouble("$varName.failIfSmallerOrEqual")
                                    ))
                            }
                            "String", "Text" -> {
                                args.add(StringArg(
                                    varName,
                                    inputSection.getOrDefault("$varName.name", "String"),
                                    inputSection.getOrDefault("$varName.description", "No description set"),
                                    CommandType.String,
                                    inputSection.getOrDefault("$varName.required", true),
                                    inputSection.getOrDefault("$varName.dontIgnore", false),
                                    inputSection.get("$varName.failIf"),
                                    inputSection.getString("$varName.logicFailReason")
                                ))
                            }
                            "WholeNumber", "Int" -> {
                                args.add(NumberArg(
                                    varName,
                                    inputSection.getOrDefault("$varName.name", "WholeNumber"),
                                    inputSection.getOrDefault("$varName.description", "No description set"),
                                    CommandType.Int,
                                    inputSection.getOrDefault("$varName.required", true),
                                    inputSection.getOrDefault("$varName.dontIgnore", false),
                                    inputSection.get("$varName.failIf"),
                                    inputSection.getString("$varName.logicFailReason"),
                                    inputSection.getDouble("$varName.failIfBigger"),
                                    inputSection.getDouble("$varName.failIfSmaller"),
                                    inputSection.getDouble("$varName.failIfBiggerOrEqual"),
                                    inputSection.getDouble("$varName.failIfSmallerOrEqual")
                                ))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getCommandOptions(): CommandOptions {
        val options = CommandOptions()

        args.forEach {
            options.addOption(
                (when(it.type) {
                    CommandType.Int -> OptionType.INTEGER
                    CommandType.Double -> OptionType.NUMBER
                    CommandType.String, CommandType.MinecraftPlayer -> OptionType.STRING
                    CommandType.DiscordMember, CommandType.DiscordUser -> OptionType.USER
                }),
                it.name,
                it.description,
                it.required
            )
        }

        return options
    }

    override fun run(event: CommandEventData) {

        val values = HashMap<String, Any>()

        if(event.isSlashCommand()) {
            args.forEach { arg ->
                when(arg.type) {
                    CommandType.Int -> {
                        event.getOptionInt(arg.name)?.also {
                            (arg as NumberArg).Conditions(it, values)?.let { res -> return fail(event, res) }
                                ?: run {
                                    values[arg.varName] = it
                                }
                        }
                    }
                    CommandType.Double -> {
                        event.getOptionDouble(arg.name)?.also {
                            (arg as NumberArg).Conditions(it, values)?.let { res -> return fail(event, res) }
                                ?: run {
                                    values[arg.varName] = it
                                }
                        }
                    }
                    CommandType.String -> {
                        event.getOptionString(arg.name)?.also {
                            (arg as StringArg).Conditions(it, values)?.let { res -> return fail(event, res) }
                                ?: run {
                                    values[arg.varName] = it
                                }
                        }
                    }
                    CommandType.DiscordUser -> {
                        event.getOptionUser(arg.name)?.also {
                            (arg as DiscordUserArg).Conditions(it.id, values)?.let { res -> return fail(event, res) }
                                ?: run {
                                    values[arg.varName] = it
                                }
                        }
                    }
                    CommandType.DiscordMember -> {
                        event.getOptionMember(arg.name)?.also {
                            (arg as DiscordMemberArg).Conditions(it.id, it.roleIDs, values)?.let { res -> return fail(event, res) }
                                ?: run {
                                    values[arg.varName] = it
                                }
                        }
                    }
                    CommandType.MinecraftPlayer -> {
                        event.getOptionString(arg.name)?.also { str ->
                            UniversalPlayer.getByString(str).also {
                                if(it == null)
                                    return fail(event, "Minecraft player not found!")
                                else {
                                    (arg as MinecraftPlayerArg).Conditions(it, values)?.let { res -> return fail(event, res) }
                                        ?: run {
                                            values[arg.varName] = it
                                        }
                                }
                            }
                        }
                    }
                }
            }
        } else {

            val reqArgs = args.filter { it.required }
            val reqSize = reqArgs.size

            var i = 0

            args.forEach { arg ->

                if(i >= event.args!!.size) {
                    if(i < reqSize)
                        return fail(event, "Argument **${reqArgs[i].name}** is missing!")
                } else {
                    when(arg.type) {
                        CommandType.Int -> {
                            val parsed = event.args[i].toIntOrNull()

                            if(parsed == null)
                            {
                                if(arg.required)
                                    return fail(event, "Argument **${arg.name}** may only be a whole number!")
                            } else {
                                val res = (arg as NumberArg).Conditions(parsed, values)
                                if(res == null)
                                {
                                    values[arg.varName] = parsed
                                    i++
                                }
                                else return fail(event, res)
                            }
                        }
                        CommandType.Double -> {
                            val parsed = event.args[i].toDoubleOrNull()

                            if(parsed == null)
                            {
                                if(arg.required)
                                    return fail(event, "Argument **${arg.name}** may only be a number!")
                            } else {
                                val res = (arg as NumberArg).Conditions(parsed, values)
                                if(res == null)
                                {
                                    values[arg.varName] = parsed
                                    i++
                                }
                                else return fail(event, res)
                            }
                        }
                        CommandType.MinecraftPlayer -> {
                            val player = UniversalPlayer.getByString(event.args[i])
                            if(player == null) {
                                if(arg.required)
                                    return fail(event, "The player was not found with this username or the UUID provided is invalid!")
                            } else {
                                val res = (arg as MinecraftPlayerArg).Conditions(player, values)
                                if(res == null) {
                                    values[arg.varName] = player
                                    i++
                                } else return fail(event, res)
                            }
                        }
                        CommandType.DiscordUser -> {
                            try {
                                val user = event.getUserByInput(event.args[i])

                                (arg as DiscordUserArg).Conditions(user.id, values)?.let {
                                    return fail(event, it)
                                } ?: run {
                                    values[arg.varName] = user
                                    i++
                                }
                            } catch (e: CommandEventData.UserGetterException) {
                                return fail(event, e.message!!)
                            }
                        }
                        CommandType.DiscordMember -> {
                            try {
                                val member = event.getMemberByInput(event.args[i])

                                (arg as DiscordMemberArg).Conditions(member.id, member.roleIDs, values)?.let {
                                    return fail(event, it)
                                } ?: run {
                                    values[arg.varName] = member
                                    i++
                                }
                            } catch (e: CommandEventData.MemberGetterException) {
                                return fail(event, e.message!!)
                            }
                        }
                        CommandType.String -> {
                            (arg as StringArg).Conditions(event.args[i], values)?.let { return fail(event, it) }
                                ?: run {
                                    values[arg.varName] = event.args[i]
                                    i++
                                }
                        }
                    }
                }
            }
        }

        val doFormat = main.customCommandsConfig.getStringList("commands.$name.format")
        val formatter = DecimalFormat("#,###.##")

        main.customCommandsConfig.singleLayerKeySet("commands.$name.actions").forEach { varName ->
            val section = main.customCommandsConfig.getSection("commands.$name.actions.$varName")
            section.getString("type")?.let { type ->
                when(type) {
                    "DefineNumber", "DefineDouble" -> {
                        section.getString("value")?.also {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                it.toDoubleOrNull()?.let { value -> values[varName] = value }
                            }
                        }
                    }
                    "DefineWholeNumber", "DefineInt" -> {
                        section.getString("value")?.also {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                it.toIntOrNull()?.let { value -> values[varName] = value }
                            }
                        }
                    }
                    "DefineText", "DefineString" -> {
                        section.getString("value")?.also {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                values[varName] = resolveStringWithValues(it, values, doFormat, formatter, main)
                            } else {
                                section.getString("valueElse")?.also { otherIt ->
                                    values[varName] = resolveStringWithValues(otherIt, values, doFormat, formatter, main)
                                }
                            }
                        }
                    }
                    "DefineLogicStatement", "DefineBoolean", "DefineBool" -> {
                        section.getString("value")?.also {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                values[varName] = it.toBoolean()
                            }
                        }
                    }
                    "RandomNumber", "RandomDouble" -> {
                        val min = section.get("min").let { if(it is String) it.toDouble() else null }
                        val max = section.get("max").let { if(it is String) it.toDouble() else null }

                        if(min != null && max != null) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                val value = Random.nextDouble(min, max + 1)

                                if(!failIf(section, values, value))
                                    values[varName] = value
                            }
                        }
                    }

                    "RandomWholeNumber", "RandomInt", "RandomInteger" -> {
                        val min = section.get("min").let { if(it is String) it.toInt() else null }
                        val max = section.get("max").let { if(it is String) it.toInt() else null }

                        if(min != null && max != null) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                val value = Random.nextInt(min, max + 1)

                                if(!failIf(section, values, value))
                                    values[varName] = value
                            }
                        }
                    }

                    "RandomText", "RandomString" -> {
                        if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                            val texts = section.getStringList("options")

                            val value = texts[(0 until texts.size).random()]

                            if(!failIfEquals(section, values, value))
                                values[varName] = resolveStringWithValues(value, values, doFormat, formatter, main)
                        }
                    }

                    "TextEquals" -> {
                        if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                            var val1: String? = section.getString("text1")
                            var val2: String? = section.getString("text2")

                            if(val1 != null && val2 != null) {
                                if(values.containsKey(val1))
                                    if(values[val1] is String) val1 = (values[val1]!! as String)

                                if(values.containsKey(val2))
                                    if(values[val2] is String) val2 = (values[val2]!! as String)

                                values[varName] = val1 == val2
                            }
                        }
                    }

                    "NumberEquals" -> {
                        if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                            val num1Str = section.getString("num1")
                            val num2Str = section.getString("num2")

                            if(num1Str != num2Str && num2Str != num1Str) {
                                var num1 = num1Str.toDoubleOrNull()
                                var num2 = num2Str.toDoubleOrNull()

                                if(num1Str != null)
                                    if(values[num1Str] is Number) num1 = (values[num1Str] as Number?)?.toDouble()

                                if(num2Str != null)
                                    if(values[num2Str] is Number) num2 = (values[num2Str] as Number?)?.toDouble()

                                if(num1 != null && num2 != null) {
                                    values[varName] = num1 == num2
                                }
                            }
                        }
                    }

                    "DiscordUserEquals", "DiscordMemberEquals" -> {
                        if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                            val user1 = section.getString("user1")
                            val user2 = section.getString("user2")

                            if(user1 != null && user2 != null) {
                                var id1: String? = null
                                var id2: String? = null

                                if(event.isPureIdRegex.matches(user1))
                                    id1 = user1
                                else if(values[user1] is String) {
                                    if((values[user1] as String).matches(event.isPureIdRegex)) id1 = (values[user1] as String)
                                } else if(values[user1] is DiscordUser)
                                    id1 = (values[user1] as DiscordUser).id
                                else if(values[user1] is DiscordMember)
                                    id1 = (values[user1] as DiscordMember).id
                                else id1 = null

                                if(event.isPureIdRegex.matches(user2))
                                    id2 = user2
                                else if(values[user2] is String) {
                                    if((values[user2] as String).matches(event.isPureIdRegex)) id2 = (values[user2] as String)
                                } else if(values[user2] is DiscordUser)
                                    id2 = (values[user2] as DiscordUser).id
                                else if(values[user2] is DiscordMember)
                                    id2 = (values[user2] as DiscordMember).id
                                else id2 = null

                                if(id1 != null && id2 != null)
                                    values[varName] = id1 == id2
                                else values[varName] = section.getBoolean("default")
                            }
                        }
                    }

                    "Smaller" -> {
                        val num1str = section.getString("num1")
                        val num2str = section.getString("num2")

                        if(num1str != null && num2str != num1str) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                var num1: Double? = num1str.toDoubleOrNull()
                                var num2: Double? = num2str.toDoubleOrNull()

                                if(num1 == null)
                                    values[num1str]?.let { if(it is Double) num1 = it else if(it is Int) num1 = it.toDouble() }

                                if(num2 == null)
                                    values[num2str]?.let { if(it is Double) num2 = it else if(it is Int) num2 = it.toDouble() }

                                if(num1 != null && num2 != null)
                                    values[varName] = num1!! < num2!!
                                else section.getBoolean("default")
                            }
                        }
                    }

                    "Bigger" -> {
                        val num1str = section.getString("num1")
                        val num2str = section.getString("num2")

                        if(num1str != null && num2str != num1str) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                var num1: Double? = num1str.toDoubleOrNull()
                                var num2: Double? = num2str.toDoubleOrNull()

                                if(num1 == null)
                                    values[num1str]?.let { if(it is Double) num1 = it else if(it is Int) num1 = it.toDouble() }

                                if(num2 == null)
                                    values[num2str]?.let { if(it is Double) num2 = it else if(it is Int) num2 = it.toDouble() }

                                if(num1 != null && num2 != null)
                                    values[varName] = num1!! > num2!!
                                else section.getBoolean("default")
                            }
                        }
                    }

                    "SmallerOrEqual" -> {
                        val num1str = section.getString("num1")
                        val num2str = section.getString("num2")

                        if(num1str != null && num2str != num1str) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                var num1: Double? = num1str.toDoubleOrNull()
                                var num2: Double? = num2str.toDoubleOrNull()

                                if(num1 == null)
                                    values[num1str]?.let { if(it is Double) num1 = it else if(it is Int) num1 = it.toDouble() }

                                if(num2 == null)
                                    values[num2str]?.let { if(it is Double) num2 = it else if(it is Int) num2 = it.toDouble() }

                                if(num1 != null && num2 != null)
                                    values[varName] = num1!! <= num2!!
                                else section.getBoolean("default")
                            }
                        }
                    }

                    "BiggerOrEqual" -> {
                        val num1str = section.getString("num1")
                        val num2str = section.getString("num2")

                        if(num1str != null && num2str != num1str) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                var num1: Double? = num1str.toDoubleOrNull()
                                var num2: Double? = num2str.toDoubleOrNull()

                                if(num1 == null)
                                    values[num1str]?.let { if(it is Double) num1 = it else if(it is Int) num1 = it.toDouble() }

                                if(num2 == null)
                                    values[num2str]?.let { if(it is Double) num2 = it else if(it is Int) num2 = it.toDouble() }

                                if(num1 != null && num2 != null)
                                    values[varName] = num1!! >= num2!!
                                else section.getBoolean("default")
                            }
                        }
                    }

                    "IncreasePlayerMoney" -> {
                        val amountStr = section.getString("amount")
                        val playerStr = section.getString("player")

                        if(amountStr != null && playerStr != null) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                if(values[playerStr] is UniversalPlayer) {
                                    var amount: Double? = amountStr.toDoubleOrNull()

                                    if(amount == null)
                                    {
                                        if (lettersDetect.find(amountStr) == null) {
                                            ExpressionEvaluator.evaluateAsDouble(resolveStringWithValues(amountStr, values, doFormat, formatter, main), false)?.let { amount = it }
                                        } else
                                        {
                                            values[amountStr]?.let { if(it is Double) amount = it else if(it is Int) amount = it.toDouble() }
                                        }
                                    }

                                    if(amount != null)
                                        (values[playerStr] as UniversalPlayer).depositPlayer(main, amount!!)
                                }
                            }
                        }
                    }

                    "DecreasePlayerMoney" -> {
                        val amountStr = section.getString("amount")
                        val playerStr = section.getString("player")

                        if(amountStr != null && playerStr != null) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                if(values[playerStr] is UniversalPlayer) {
                                    var amount: Double? = amountStr.toDoubleOrNull()

                                    if(amount == null)
                                    {
                                        if (lettersDetect.find(amountStr) == null) {
                                            ExpressionEvaluator.evaluateAsDouble(resolveStringWithValues(amountStr, values, doFormat, formatter, main), false)?.let { amount = it }
                                        } else
                                        {
                                            values[amountStr]?.let { if(it is Double) amount = it else if(it is Int) amount = it.toDouble() }
                                        }
                                    }

                                    if(amount != null)
                                        (values[playerStr] as UniversalPlayer).withdrawPlayer(main, amount!!)
                                }
                            }
                        }
                    }

                    "IsNull", "IsUnset" -> {
                        val isNullStr = section.getString("isNull") ?: section.getString("isUnset")
                        val isNotNullStr = section.getString("isNotNull") ?: section.getString("isNotUnset") ?: section.getString("isSet")

                        if(isNullStr != null || isNotNullStr != null) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                if(isNullStr != null)
                                {
                                    values[isNullStr].also {
                                        values[isNullStr] = it == null
                                    }
                                } else if(isNotNullStr != null) {
                                    values[isNotNullStr].also {
                                        values[isNotNullStr] = it == null
                                    }
                                }
                            }
                        }
                    }

                    "InvertLogic", "Invert", "Negate", "Negació", "Negation" -> {
                        val value = section.getString("value")

                        if(value != null) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                values[value]?.also {
                                    values[varName] = if(it is Boolean) !it else false
                                } ?: run { values[varName] = true }
                            }
                        }
                    }

                    "GetCommandExecutorPlayer" -> {
                        if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                            val playerUuid = main.linkHandler.getUuid(event.author.id)

                            if(playerUuid != null) {
                                values[varName] = UniversalPlayer.getByUUID(playerUuid)
                            }
                        }
                    }

                    "GetMinecraftPlayer", "GetPlayer" -> {
                        val value = section.getString("from")

                        if(value != null) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                val uuid = values[value]?.let {
                                    when(it) {
                                        is DiscordUser -> main.linkHandler.getUuid(it.id)
                                        is DiscordMember -> main.linkHandler.getUuid(it.id)
                                        else -> null
                                    }
                                }

                                uuid?.also { values[varName] = UniversalPlayer.getByUUID(it) } ?: run {
                                    val value2 = section.getString("onlyIfNot")
                                    if(value2 != null) {
                                        val uuid2 = values[value2]?.let {
                                            when(it) {
                                                is DiscordUser -> main.linkHandler.getUuid(it.id)
                                                is DiscordMember -> main.linkHandler.getUuid(it.id)
                                                else -> null
                                            }
                                        }

                                        uuid2?.also { values[varName] = UniversalPlayer.getByUUID(it) }
                                    }
                                }
                            }
                        }
                    }

                    "CheckBalance" -> {
                        val playerStr = section.getString("player")
                        val amountStr = section.getString("amount")

                        if(playerStr != null && amountStr != null) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                val player = values[playerStr] as? UniversalPlayer
                                if(player != null) {

                                    var amount: Double? = amountStr.toDoubleOrNull()

                                    if(amount == null)
                                    {
                                        if (lettersDetect.find(amountStr) == null) {
                                            ExpressionEvaluator.evaluateAsDouble(resolveStringWithValues(amountStr, values, doFormat, formatter, main), false)?.let { amount = it }
                                        } else
                                        {
                                            values[amountStr]?.let { if(it is Double) amount = it else if(it is Int) amount = it.toDouble() }
                                        }
                                    }

                                    if(amount != null)
                                        values[varName] = main.getEconomy().getBalance(player.offlinePlayer) >= amount!!

                                } else values[varName] = false
                            }
                        }
                    }

                    "AddNumber" -> {
                        val num1str = section.getString("num1")
                        val num2str = section.getString("num2")

                        if(num1str != null && num2str != num1str) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                var num1: Double? = num1str.toDoubleOrNull()
                                var num2: Double? = num2str.toDoubleOrNull()

                                if(num1 == null)
                                    values[num1str]?.let { if(it is Double) num1 = it else if(it is Int) num1 = it.toDouble() }

                                if(num2 == null)
                                    values[num2str]?.let { if(it is Double) num2 = it else if(it is Int) num2 = it.toDouble() }

                                if(num1 != null && num2 != null)
                                    values[varName] = num1!! + num2!!
                                else section.getString("default")?.let {
                                    var d = it.toDoubleOrNull()

                                    if(d == null)
                                        values[it]?.let { x -> if(x is Double) d = x else if(x is Int) num1 = x.toDouble() }

                                    if(d != null) values[varName] = d!!
                                }
                            }
                        }
                    }

                    "SubNumber", "SubtrackNumber" -> {
                        val num1str = section.getString("num1")
                        val num2str = section.getString("num2")

                        if(num1str != null && num2str != num1str) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                var num1: Double? = num1str.toDoubleOrNull()
                                var num2: Double? = num2str.toDoubleOrNull()

                                if(num1 == null)
                                    values[num1str]?.let { if(it is Double) num1 = it else if(it is Int) num1 = it.toDouble() }

                                if(num2 == null)
                                    values[num2str]?.let { if(it is Double) num2 = it else if(it is Int) num2 = it.toDouble() }

                                if(num1 != null && num2 != null)
                                    values[varName] = num1!! - num2!!
                                else section.getString("default")?.let {
                                    var d = it.toDoubleOrNull()

                                    if(d == null)
                                        values[it]?.let { x -> if(x is Double) d = x else if(x is Int) num1 = x.toDouble() }

                                    if(d != null) values[varName] = d!!
                                }
                            }
                        }
                    }

                    "MultNumber", "MultiplyNumber" -> {
                        val num1str = section.getString("num1")
                        val num2str = section.getString("num2")

                        if(num1str != null && num2str != num1str) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                var num1: Double? = num1str.toDoubleOrNull()
                                var num2: Double? = num2str.toDoubleOrNull()

                                if(num1 == null)
                                    values[num1str]?.let { if(it is Double) num1 = it else if(it is Int) num1 = it.toDouble() }

                                if(num2 == null)
                                    values[num2str]?.let { if(it is Double) num2 = it else if(it is Int) num2 = it.toDouble() }

                                if(num1 != null && num2 != null)
                                    values[varName] = num1!! * num2!!
                                else section.getString("default")?.let {
                                    var d = it.toDoubleOrNull()

                                    if(d == null)
                                        values[it]?.let { x -> if(x is Double) d = x else if(x is Int) num1 = x.toDouble() }

                                    if(d != null) values[varName] = d!!
                                }
                            }
                        }
                    }

                    "DivNumber", "DivideNumber" -> {
                        val num1str = section.getString("num1")
                        val num2str = section.getString("num2")

                        if(num1str != null && num2str != num1str) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                var num1: Double? = num1str.toDoubleOrNull()
                                var num2: Double? = num2str.toDoubleOrNull()

                                if(num1 == null)
                                    values[num1str]?.let { if(it is Double) num1 = it else if(it is Int) num1 = it.toDouble() }

                                if(num2 == null)
                                    values[num2str]?.let { if(it is Double) num2 = it else if(it is Int) num2 = it.toDouble() }

                                if(num1 != null && num2 != null)
                                    values[varName] = num1!! / num2!!
                                else section.getString("default")?.let {
                                    var d = it.toDoubleOrNull()

                                    if(d == null)
                                        values[it]?.let { x -> if(x is Double) d = x else if(x is Int) num1 = x.toDouble() }

                                    if(d != null) values[varName] = d!!
                                }
                            }
                        }
                    }
                    "RemainsNumber", "ModuleOperator" -> {
                        val num1str = section.getString("num1")
                        val num2str = section.getString("num2")

                        if(num1str != null && num2str != num1str) {
                            if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                                var num1: Double? = num1str.toDoubleOrNull()
                                var num2: Double? = num2str.toDoubleOrNull()

                                if(num1 == null)
                                    values[num1str]?.let { if(it is Double) num1 = it else if(it is Int) num1 = it.toDouble() }

                                if(num2 == null)
                                    values[num2str]?.let { if(it is Double) num2 = it else if(it is Int) num2 = it.toDouble() }

                                if(num1 != null && num2 != null)
                                    values[varName] = num1!! % num2!!
                                else section.getString("default")?.let {
                                    var d = it.toDoubleOrNull()

                                    if(d == null)
                                        values[it]?.let { x -> if(x is Double) d = x else if(x is Int) num1 = x.toDouble() }

                                    if(d != null) values[varName] = d!!
                                }
                            }
                        }
                    }

                    "EarlyMessage" -> {
                        if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                            event.sendYMLEmbed("${section.pathPrefix}.message", main.customCommandsConfig, { str ->
                                setCommandPlaceholders(resolveStringWithValues(str, values, doFormat, formatter, main), event.prefix, event.commandName, description, usage)
                            }).queue()
                            return
                        }
                    }

                    "FailMessage" -> {
                        if(actionConditionChecker(section, values, doFormat, formatter, main)) {
                            val message = section.getString("message")
                            if(message != null)
                            {
                                fail(event, resolveStringWithValues(message, values, doFormat, formatter, main))
                                return
                            }
                        }
                    }

                    "GetDiscordMemberFromMinecraftPlayer" -> {
                        val from = section.getString("from")
                        if(from != null) {
                            values[from]?.let {
                                if(it is UniversalPlayer) {
                                    main.linkHandler.getId(it.uniqueId)?.let { id ->
                                        val member = event.getMemberById(id)
                                        if(member != null)
                                            values[varName] = member
                                    }
                                }
                            }
                        }
                    }

                    "IsLinked" -> {
                        val from = section.getString("from")
                        if(from != null) {
                            values[varName]?.let {
                                when (it) {
                                    is UniversalPlayer -> values[from] = main.linkHandler.isLinked(it.uniqueId)
                                    is DiscordMember -> values[from] = main.linkHandler.isLinked(it.id)
                                    is DiscordUser -> values[from] = main.linkHandler.isLinked(it.id)
                                }
                            }
                        }
                    }
                }
            }
        }

        val memberPlaceholder: DiscordMember = main.customCommandsConfig.getString("commands.$name.enablePlaceholdersForMember")?.let { value ->
            values[value]?.let {
                when(it) {
                    is DiscordMember -> it
                    is DiscordUser -> event.getMemberById(it.id)
                    is UniversalPlayer -> main.linkHandler.getId(it.uniqueId)?.let { id -> event.getMemberById(id) }
                    else -> null
                }
            }?: event.member!!
        }?: event.member!!

        val playerPlaceholder: UniversalPlayer? = main.customCommandsConfig.getString("commands.$name.enablePlaceholdersFor")?.let { value ->
            values[value]?.let { if(it is UniversalPlayer) it else null }
        }

        event.sendYMLEmbed("commands.$name.message", main.customCommandsConfig, { str ->
            val str1 = setCommandPlaceholders(resolveStringWithValues(str, values, doFormat, formatter, main), event.prefix, event.commandName, description, usage)

            if(playerPlaceholder != null)
                setPlaceholdersForDiscordMessage(memberPlaceholder, playerPlaceholder, str1)
            else setDiscordPlaceholders(memberPlaceholder, str1)
        }).queue()

        main.commandHandler.commandComplete(this, event)
    }
}