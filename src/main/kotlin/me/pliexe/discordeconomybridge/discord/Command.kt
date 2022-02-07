package me.pliexe.discordeconomybridge.discord

import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent
import me.pliexe.discordeconomybridge.DiscordEconomyBridge
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction
import net.dv8tion.jda.api.requests.restaction.interactions.UpdateInteractionAction
import org.bukkit.Server
import org.bukkit.configuration.file.FileConfiguration
import java.awt.Color
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

/*class DiscordUser (
    val username: String,
    val id: String,
    val avatarURL: String
        )

class DiscordMember (
    val nick: String?
        ) {}*/

class DiscordUser(
    val userNative: User?,
    val userSRV: github.scarsz.discordsrv.dependencies.jda.api.entities.User?
) {
    constructor(user: User): this(user, null)
    constructor(user: github.scarsz.discordsrv.dependencies.jda.api.entities.User): this(null, user)

    val id: String
        get() = userNative?.id ?: userSRV!!.id

    val name: String
        get() = userNative?.name ?: userSRV!!.name

    val discriminator: String
        get() = userNative?.discriminator ?: userSRV!!.discriminator

    val avatarUrl: String
        get() = if(userNative == null) userSRV!!.avatarUrl ?: userSRV.defaultAvatarUrl
        else userNative.avatarUrl ?: userNative.defaultAvatarUrl

    val asTag: String
        get() = userNative?.asTag ?: userSRV!!.asTag
}

class DiscordMember(
    val memberNative: Member?,
    val memberSRV: github.scarsz.discordsrv.dependencies.jda.api.entities.Member?
) {
    constructor(member: Member): this(member, null)
    constructor(member: github.scarsz.discordsrv.dependencies.jda.api.entities.Member): this(null, member)

    val id: String
        get() = memberNative?.id ?: memberSRV!!.id

    val nickname: String?
        get() = memberNative?.nickname ?: memberSRV?.nickname

    val user: DiscordUser
        get() = if(memberNative == null) DiscordUser(memberSRV!!.user) else DiscordUser(memberNative.user)

    val isOwner: Boolean
        get() = memberNative?.isOwner ?: memberSRV!!.isOwner

    fun isAdministrator(): Boolean {
        return memberNative?.hasPermission(Permission.ADMINISTRATOR)
            ?: memberSRV!!.hasPermission(github.scarsz.discordsrv.dependencies.jda.api.Permission.ADMINISTRATOR)
    }

    fun rolesContain(roleID: String): Boolean{
        return memberNative?.roles?.contains(roleID) ?: memberSRV!!.roles.contains(roleID)
    }
}

class EditOriginalMessageAction(
    val type: Type,
    val main: DiscordEconomyBridge,
    val editOriginalNative: WebhookMessageUpdateAction<net.dv8tion.jda.api.entities.Message>?,
    val editOriginalSRV: github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.WebhookMessageUpdateAction<github.scarsz.discordsrv.dependencies.jda.api.entities.Message>?
) {
    enum class Type {
        editOriginalNative,
        editOriginalSRV
    }

    constructor(editOriginalNative: WebhookMessageUpdateAction<net.dv8tion.jda.api.entities.Message>, main: DiscordEconomyBridge) : this(Type.editOriginalNative, main, editOriginalNative, null)
    constructor(editOriginalSRV: github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.WebhookMessageUpdateAction<github.scarsz.discordsrv.dependencies.jda.api.entities.Message>, main: DiscordEconomyBridge) : this(Type.editOriginalSRV, main, null, editOriginalSRV)

    fun queue(done: ((message: Message) -> Unit)) {
        when(type) {
            Type.editOriginalNative -> editOriginalNative!!.queue { done(Message(it, main)) }
            Type.editOriginalSRV -> editOriginalSRV!!.queue { done(Message(it, main)) }
        }
    }

    fun queue() {
        when(type) {
            Type.editOriginalNative -> editOriginalNative!!.queue()
            Type.editOriginalSRV -> editOriginalSRV!!.queue()
        }
    }

    fun setContent(content: String?): EditOriginalMessageAction {
        when(type) {
            Type.editOriginalNative -> editOriginalNative!!.setContent(content)
            Type.editOriginalSRV -> editOriginalSRV!!.setContent(content)
        }
        return this
    }

    fun setActionRow(buttons: List<Button>): EditOriginalMessageAction {
        when(type) {
            Type.editOriginalNative -> editOriginalNative!!.setActionRow(buttons.map { it.getNative() })
            Type.editOriginalSRV -> editOriginalSRV!!.setActionRow(buttons.map { it.getSRV() })
        }
        return this
    }

    fun removeActinRows(): EditOriginalMessageAction {
        when(type) {
            Type.editOriginalNative -> editOriginalNative!!.setActionRows()
            Type.editOriginalSRV -> editOriginalSRV!!.setActionRows()
        }
        return this
    }

    fun setYesOrNoButtons(yesLabel: String, noLabel: String): EditOriginalMessageAction {
        setActionRow(mutableListOf(
            Button.success("yes", yesLabel),
            Button.danger("no", noLabel)
        ))
        return this
    }
}

class InteractionHook(
    val interactionNative: InteractionHook?,
    val interactionSRV: github.scarsz.discordsrv.dependencies.jda.api.interactions.InteractionHook?,
    val main: DiscordEconomyBridge
) {
    constructor(event: InteractionHook, main: DiscordEconomyBridge) : this(event, null, main)
    constructor(event: github.scarsz.discordsrv.dependencies.jda.api.interactions.InteractionHook, main: DiscordEconomyBridge) : this(null, event, main)

    fun retrieveOriginal(done: (message: Message) -> Unit) {
        return if(interactionNative == null)
            interactionSRV!!.retrieveOriginal().queue { done(Message(it, main)) }
        else interactionNative.retrieveOriginal().queue  { done(Message(it, main)) }
    }

    fun editMessage(content: String): EditOriginalMessageAction {
        return if(interactionNative == null)
            EditOriginalMessageAction(interactionSRV!!.editOriginal(content), main)
        else EditOriginalMessageAction(interactionNative.editOriginal(content), main)
    }

    fun editMessage(embed: DiscordEmbed): EditOriginalMessageAction {
        if(embed.isEmpty)
            return editMessage(embed.content ?: "Missing embed values and content in discord_messages.yml!")

        return if(interactionNative == null)
            EditOriginalMessageAction(interactionSRV!!.editOriginalEmbeds(embed.getSRV().build()).setContent(embed.content), main)
                else EditOriginalMessageAction(interactionNative.editOriginalEmbeds(embed.getNative().build()).setContent(embed.content), main)
    }
}

class MessageAction(val type: Type,
                    val main: DiscordEconomyBridge,
                    val messageActionNative: MessageAction?,
                    val messageActionSRV: github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.MessageAction?,
                    val replyActionNative: ReplyAction?,
                    val replyActionSRV: github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.interactions.ReplyAction?,
                    val updateInteractionActionNative: UpdateInteractionAction?,
                    val updateInteractionActionSRV: github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.interactions.UpdateInteractionAction?,
                    ) {
    var buttons: List<Button>? = null

    enum class Type {
        NativeCommand,
        SRVCommand,
        NativeSlashCommand,
        SRVSlashCommand,
        UpdateInteractionActionNative,
        UpdateInteractionActionSRV
    }

    constructor(_messageAction: MessageAction, main: DiscordEconomyBridge) : this(Type.NativeCommand, main, _messageAction, null, null, null, null, null)
    constructor(_messageAction: github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.MessageAction, main: DiscordEconomyBridge) : this(Type.SRVCommand, main,null, _messageAction, null, null, null, null)

    constructor(_replyAction: ReplyAction, main: DiscordEconomyBridge) : this(Type.NativeSlashCommand, main,null, null, _replyAction, null, null, null)
    constructor(_replyAction: github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.interactions.ReplyAction, main: DiscordEconomyBridge) : this(Type.SRVSlashCommand, main,null, null, null, _replyAction, null, null)

    constructor(_updateInteractionAction: UpdateInteractionAction, main: DiscordEconomyBridge) : this(Type.UpdateInteractionActionNative, main, null, null, null, null, _updateInteractionAction, null)
    constructor(_updateInteractionSRV: github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.interactions.UpdateInteractionAction, main: DiscordEconomyBridge) : this(Type.UpdateInteractionActionSRV, main, null, null, null, null, null, _updateInteractionSRV)

    fun queue(done: ((message: Message) -> Unit)) {
        when (type) {
            Type.NativeCommand -> messageActionNative!!.queue { done(Message(it, main)) }
            Type.SRVCommand -> messageActionSRV!!.queue { done(Message(it, main)) }
            Type.NativeSlashCommand -> replyActionNative!!.queue { interactionHook -> interactionHook.retrieveOriginal().queue { done(Message(it, main)) } }
            Type.SRVSlashCommand -> replyActionSRV!!.queue { interactionHook -> interactionHook.retrieveOriginal().queue { done(Message(it, main)) } }
            Type.UpdateInteractionActionNative -> updateInteractionActionNative!!.queue { interactionHook -> interactionHook.retrieveOriginal().queue { done(Message(it, main))} }
            Type.UpdateInteractionActionSRV -> updateInteractionActionSRV!!.queue { interactionHook -> interactionHook.retrieveOriginal().queue { done(Message(it, main))} }
        }
    }

    fun queue(done: ((message: Message, interactionHook: me.pliexe.discordeconomybridge.discord.InteractionHook) -> Unit)) {
        when (type) {
            Type.NativeSlashCommand -> replyActionNative!!.queue { interactionHook -> interactionHook.retrieveOriginal().queue { done(Message(it, main), InteractionHook(interactionHook, main)) } }
            Type.SRVSlashCommand -> replyActionSRV!!.queue { interactionHook -> interactionHook.retrieveOriginal().queue { done(Message(it, main), InteractionHook(interactionHook, main)) } }
            Type.UpdateInteractionActionNative -> updateInteractionActionNative!!.queue { interactionHook -> interactionHook.retrieveOriginal().queue { done(Message(it, main), InteractionHook(interactionHook, main))} }
            Type.UpdateInteractionActionSRV -> updateInteractionActionSRV!!.queue { interactionHook -> interactionHook.retrieveOriginal().queue { done(Message(it, main), InteractionHook(interactionHook, main))} }
            else -> throw Error("This method may only be used for interactions! Dev only note. Please report this bug to the dev")
        }
    }

    fun queue() {
        when (type) {
            Type.NativeCommand -> messageActionNative!!.queue()
            Type.SRVCommand -> messageActionSRV!!.queue()
            Type.NativeSlashCommand -> replyActionNative!!.queue()
            Type.SRVSlashCommand -> replyActionSRV!!.queue()
            Type.UpdateInteractionActionNative -> updateInteractionActionNative!!.queue()
            Type.UpdateInteractionActionSRV -> updateInteractionActionSRV!!.queue()
        }
    }

    fun setContent(content: String?): me.pliexe.discordeconomybridge.discord.MessageAction {
        when (type) {
            Type.NativeCommand -> messageActionNative!!.content(content)
            Type.SRVCommand -> messageActionSRV!!.content(content)
            Type.NativeSlashCommand -> replyActionNative!!.setContent(content)
            Type.SRVSlashCommand -> replyActionSRV!!.setContent(content)
            Type.UpdateInteractionActionNative -> updateInteractionActionNative!!.setContent(content)
            Type.UpdateInteractionActionSRV -> updateInteractionActionSRV!!.setContent(content)
        }
        return this
    }

    fun setActionRow(buttons: List<Button>): me.pliexe.discordeconomybridge.discord.MessageAction {
        when (type) {
            Type.NativeCommand -> messageActionNative!!.setActionRow(buttons.map { it.getNative() })
            Type.SRVCommand -> messageActionSRV!!.setActionRow(buttons.map { it.getSRV() })
            Type.NativeSlashCommand -> replyActionNative!!.addActionRow(buttons.map { it.getNative() })
            Type.SRVSlashCommand -> replyActionSRV!!.addActionRow(buttons.map { it.getSRV() })
            Type.UpdateInteractionActionNative -> updateInteractionActionNative!!.setActionRow(buttons.map { it.getNative() })
            Type.UpdateInteractionActionSRV -> updateInteractionActionSRV!!.setActionRow(buttons.map { it.getSRV() })
        }
        return this
    }

    fun removeActinRows(): me.pliexe.discordeconomybridge.discord.MessageAction {
        when (type) {
            Type.NativeCommand -> messageActionNative!!.setActionRows()
            Type.SRVCommand -> messageActionSRV!!.setActionRows()
            Type.NativeSlashCommand -> replyActionNative!!.addActionRows()
            Type.SRVSlashCommand -> replyActionSRV!!.addActionRows()
            Type.UpdateInteractionActionNative -> updateInteractionActionNative!!.setActionRows()
            Type.UpdateInteractionActionSRV -> updateInteractionActionSRV!!.setActionRows()
        }
        return this
    }

    fun setYesOrNoButtons(yesLabel: String, noLabel: String): me.pliexe.discordeconomybridge.discord.MessageAction {
        setActionRow(mutableListOf(
            Button.success("yes", yesLabel),
            Button.danger("no", noLabel)
        ))
        return this
    }
}

class Button private constructor(
    val label: String,
    val link: String?,
    val customId: String?,
    val disabled: Boolean,
    val type: ButtonType
) {

    var emojiNative: Emoji? = null
    var emojiSrv: github.scarsz.discordsrv.dependencies.jda.api.entities.Emoji? = null

    enum class ButtonType {
        Primary,
        Secondary,
        Success,
        Danger,
        Link
    }

    companion object {
        fun primary(customId: String, label: String, disabled: Boolean = false): Button {
            return Button(label, null, customId, disabled, ButtonType.Primary)
        }

        fun secondary(customId: String, label: String, disabled: Boolean = false): Button {
            return Button(label, null, customId, disabled, ButtonType.Secondary)
        }

        fun success(customId: String, label: String, disabled: Boolean = false): Button {
            return Button(label, null, customId, disabled, ButtonType.Success)
        }

        fun danger(customId: String, label: String, disabled: Boolean = false): Button {
            return Button(label, null, customId, disabled, ButtonType.Danger)
        }

        fun link(label: String, link: String, disabled: Boolean = false): Button {
            return Button(label, link, null, disabled, ButtonType.Link)
        }
    }

    fun getNative(): net.dv8tion.jda.api.interactions.components.Button {
        return when(type) {
            ButtonType.Primary -> {
                if(label == null)
                    net.dv8tion.jda.api.interactions.components.Button.primary(customId!!, emojiNative!!).withDisabled(disabled)
                else net.dv8tion.jda.api.interactions.components.Button.primary(customId!!, label).withDisabled(disabled)
            }
            ButtonType.Danger -> {
                if(label == null)
                    net.dv8tion.jda.api.interactions.components.Button.danger(customId!!, emojiNative!!).withDisabled(disabled)
                else net.dv8tion.jda.api.interactions.components.Button.danger(customId!!, label).withDisabled(disabled)
            }
            ButtonType.Secondary -> {
                if(label == null)
                    net.dv8tion.jda.api.interactions.components.Button.secondary(customId!!, emojiNative!!).withDisabled(disabled)
                else net.dv8tion.jda.api.interactions.components.Button.secondary(customId!!, label).withDisabled(disabled)
            }
            ButtonType.Success -> {
                if(label == null)
                    net.dv8tion.jda.api.interactions.components.Button.success(customId!!, emojiNative!!).withDisabled(disabled)
                else net.dv8tion.jda.api.interactions.components.Button.success(customId!!, label).withDisabled(disabled)
            }
            ButtonType.Link -> {
                if(label == null)
                    net.dv8tion.jda.api.interactions.components.Button.link(link!!, emojiNative!!).withDisabled(disabled)
                else net.dv8tion.jda.api.interactions.components.Button.link(link!!, label).withDisabled(disabled)
            }
        }
    }

    fun getSRV(): github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button {
        return when(type) {
            ButtonType.Primary -> {
                if(label == null)
                    github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button.primary(customId!!, emojiSrv!!).withDisabled(disabled)
                else github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button.primary(customId!!, label).withDisabled(disabled)
            }
            ButtonType.Success -> {
                if(label == null)
                    github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button.success(customId!!, emojiSrv!!).withDisabled(disabled)
                else github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button.success(customId!!, label).withDisabled(disabled)
            }
            ButtonType.Danger -> {
                if(label == null)
                    github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button.danger(customId!!, emojiSrv!!).withDisabled(disabled)
                else github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button.danger(customId!!, label).withDisabled(disabled)
            }
            ButtonType.Secondary -> {
                if(label == null)
                    github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button.secondary(customId!!, emojiSrv!!).withDisabled(disabled)
                else github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button.secondary(customId!!, label).withDisabled(disabled)
            }
            ButtonType.Link -> {
                if(label == null)
                    github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button.link(link!!, emojiSrv!!).withDisabled(disabled)
                else github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button.link(link!!, label).withDisabled(disabled)
            }
        }
    }

}

class InteractionCollector(
    val message: Message,
    val main: DiscordEconomyBridge,
    val time: Long,
    val count: Int?,
    val interval: Boolean = false
) {
    private var counter = 0
    private var timer: TimerTask

    private val collection = mutableListOf<ComponentInteractionEvent>()

    enum class DoneType {
        Expired,
        CountExceeded,
        Cancelled,
        MessageDeleted
    }

    var onDone: ((DoneType, List<ComponentInteractionEvent>) -> Unit)? = null
    var onClick: ((interactionEvent: ComponentInteractionEvent) -> Unit)? = null

    init {
        timer = Timer().schedule(time) { handleTimer() }
        registerEvent()
    }

    fun stop() {
        stopCollector(DoneType.Cancelled)
    }

    private fun stopCollector(type: DoneType) {
        unregisterEvent()
        timer.cancel()
        onDone?.let { it(type, collection) }
    }

    private fun handleTimer()
    {
        stopCollector(DoneType.Expired)
    }

    private fun registerEvent() {
        main.commandHandler.getMessageDeleteEvents()[message.id] = {
            stopCollector(DoneType.MessageDeleted)
        }
        main.commandHandler.getEvents()[message.id] = { interactionEvent ->
            if(count != null)
            {
                counter++
                if(counter > count)
                {
                    stopCollector(DoneType.CountExceeded)
                }
            }

            if(interval) {
                timer.cancel()
                timer = Timer().schedule(time) { handleTimer() }
            }

            if(onClick != null)
                onClick?.let { it(interactionEvent) }

            collection.add(interactionEvent)
        }
    }

    private fun unregisterEvent() {
        main.commandHandler.getEvents().remove(message.id)
        main.commandHandler.getMessageDeleteEvents().remove(message.id
        )
    }
}

class MessageDeleteEvent(
    val native: MessageDeleteEvent?,
    val srv: github.scarsz.discordsrv.dependencies.jda.api.events.message.MessageDeleteEvent?
) {
    constructor(event: MessageDeleteEvent): this(event, null)
    constructor(event: github.scarsz.discordsrv.dependencies.jda.api.events.message.MessageDeleteEvent): this(null, event)

    val messageId
        get() = native?.messageId ?: srv!!.messageId
}

class Message (
    val nativeMessage: net.dv8tion.jda.api.entities.Message?,
    val SRVMessage: github.scarsz.discordsrv.dependencies.jda.api.entities.Message?,
    val main: DiscordEconomyBridge
        ) {

    constructor(_message: net.dv8tion.jda.api.entities.Message, main: DiscordEconomyBridge) : this(_message, null, main)
    constructor(_message: github.scarsz.discordsrv.dependencies.jda.api.entities.Message, main: DiscordEconomyBridge) : this(null, _message, main)

    val id: String
        get() = nativeMessage?.id ?: SRVMessage!!.id

    val content: String
        get() = nativeMessage?.contentRaw ?: SRVMessage!!.contentRaw

    fun createInteractionCollector(time: Long, interval: Boolean, count: Int): InteractionCollector {
        return InteractionCollector(this, main, time, count, interval)
    }

    fun createInteractionCollector(time: Long, interval: Boolean): InteractionCollector {
        return InteractionCollector(this, main, time, null, interval)
    }

    fun createInteractionCollector(time: Long, count: Int): InteractionCollector {
        return InteractionCollector(this, main, time, count)
    }

    fun awaitYesOrNo(time: Long, filter: (ComponentInteractionEvent) -> Boolean, outcome: (accepted: Boolean, interaction: ComponentInteractionEvent?, deleted: Boolean) -> Unit) {
        val collector = createInteractionCollector(time, false)

        collector.onClick = { interaction ->
            if(filter(interaction)) {
                when(interaction.componentId) {
                    "yes" -> {
                        collector.stop()
                        outcome(true, interaction, false)
                    }
                    else -> {
                        collector.stop()
                        outcome(false, interaction, false)
                    }
                }
            } else {
                sendWrongUserInteractionMessage(interaction)
            }
        }

        collector.onDone = { type, _ ->
            when(type) {
                InteractionCollector.DoneType.Expired -> outcome(false, null, false)
                InteractionCollector.DoneType.MessageDeleted -> outcome(false, null, true)
            }
        }
    }

    fun editMessage(content: String): me.pliexe.discordeconomybridge.discord.MessageAction {
        return if(nativeMessage == null)
            MessageAction(SRVMessage!!.editMessage(content), main)
        else MessageAction(nativeMessage.editMessage(content), main)
    }

    fun editMessage(embed: DiscordEmbed): me.pliexe.discordeconomybridge.discord.MessageAction {
        if(embed.isEmpty)
            editMessage(embed.content ?: "Missing embed values and content in discord_messages.yml!")

        return if(nativeMessage == null)
            MessageAction(SRVMessage!!.editMessageEmbeds(embed.getSRV().build()).content(embed.content), main)
        else MessageAction(nativeMessage.editMessageEmbeds(embed.getNative().build()).content(embed.content), main)
    }

    fun awaitButtonInteractions(count: Int, timer: Long, onDone: ((type: InteractionCollector.DoneType, collected: List<ComponentInteractionEvent>) -> Unit), onClick: ((event: ComponentInteractionEvent) -> Boolean)? = null) {
        var i = 0
        val collected = mutableListOf<ComponentInteractionEvent>()
        val tmr = Timer().schedule(timer) {
            main.commandHandler.getEvents().remove(id)
            onDone(InteractionCollector.DoneType.Expired, collected)
        }
        main.commandHandler.getMessageDeleteEvents()[id] = {
            tmr.cancel()
            main.commandHandler.getEvents().remove(id)
            onDone(InteractionCollector.DoneType.MessageDeleted, collected)
        }
        main.commandHandler.getEvents()[id] = { event ->
            i++
            if(onClick != null)
                if(onClick(event)) {
                    tmr.cancel()
                    main.commandHandler.getEvents().remove(id)
                    main.commandHandler.getMessageDeleteEvents().remove(id)
                    onDone(InteractionCollector.DoneType.CountExceeded, collected)
                }
            collected.add(event)
            if(i >= count) {
                tmr.cancel()
                main.commandHandler.getEvents().remove(id)
                main.commandHandler.getMessageDeleteEvents().remove(id)
                onDone(InteractionCollector.DoneType.CountExceeded, collected)
            }
        }
    }

    fun awaitButtonInteractions(timer: Long, interval: Boolean = false, onDone: ((type: InteractionCollector.DoneType, collected: List<ComponentInteractionEvent>) -> Unit), onClick: ((event: ComponentInteractionEvent) -> Boolean)? = null) {
        val collected = mutableListOf<ComponentInteractionEvent>()
        var tmr = Timer().schedule(timer) {
            main.commandHandler.getEvents().remove(id)
            onDone(InteractionCollector.DoneType.Expired, collected)
        }
        main.commandHandler.getMessageDeleteEvents()[id] = {
            tmr.cancel()
            main.commandHandler.getEvents().remove(id)
            onDone(InteractionCollector.DoneType.MessageDeleted, collected)
        }
        main.commandHandler.getEvents()[id] = { event ->
            if(interval)
            {
                tmr.cancel()
                tmr = Timer().schedule(timer) {
                    main.commandHandler.getEvents().remove(id)
                    main.commandHandler.getMessageDeleteEvents().remove(id)
                    onDone(InteractionCollector.DoneType.Expired, collected)
                }
            }
            if(onClick != null)
                if(onClick(event)) {
                    tmr.cancel()
                    main.commandHandler.getEvents().remove(id)
                    main.commandHandler.getMessageDeleteEvents().remove(id)
                    onDone(InteractionCollector.DoneType.CountExceeded, collected)
                }
            collected.add(event)
        }
    }
}

class DiscordEmbed(
    val nativeBuilder: EmbedBuilder?,
    val SRVBuilder: github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder?
) {
    var content: String? = null

    fun getNative(): EmbedBuilder {
        return nativeBuilder!!
    }

    fun getSRV(): github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder {
        return SRVBuilder!!
    }

    fun setTitle(title: String): DiscordEmbed {
        if(nativeBuilder == null)
            SRVBuilder!!.setTitle(title)
        else nativeBuilder.setTitle(title)

        return this
    }

    fun setDescription(description: String): DiscordEmbed {
        if(nativeBuilder == null)
            SRVBuilder!!.setDescription(description)
        else nativeBuilder.setDescription(description)

        return this
    }

    fun addField(name: String?, text: String?, inline: Boolean = false): DiscordEmbed {
        if(nativeBuilder == null)
            SRVBuilder!!.addField(name, text, inline)
        else nativeBuilder.addField(name, text, inline)

        return this
    }

    fun setColor(color: Color?): DiscordEmbed {
        if(nativeBuilder == null)
            SRVBuilder!!.setColor(color)
        else nativeBuilder.setColor(color)

        return this
    }

    fun setColor(color: Int): DiscordEmbed {
        if(nativeBuilder == null)
            SRVBuilder!!.setColor(color)
        else nativeBuilder.setColor(color)

        return this
    }

    fun setFooter(text: String?, iconUrl: String? = null): DiscordEmbed {
        if(nativeBuilder == null)
            SRVBuilder!!.setFooter(text, iconUrl)
        else nativeBuilder.setFooter(text, iconUrl)

        return this
    }

    fun setImage(url: String?): DiscordEmbed {
        if(nativeBuilder == null)
            SRVBuilder!!.setImage(url)
        else nativeBuilder.setImage(url)

        return this
    }

    fun setThumbnail(url: String?): DiscordEmbed {
        if(nativeBuilder == null)
            SRVBuilder!!.setThumbnail(url)
        else nativeBuilder.setThumbnail(url)

        return this
    }

    fun setAuthor(name: String?, url: String?, iconURL: String?): DiscordEmbed {
        if(nativeBuilder == null)
            SRVBuilder!!.setAuthor(name, url, iconURL)
        else nativeBuilder.setAuthor(name, url, iconURL)

        return this
    }

    val isEmpty: Boolean
        get() = nativeBuilder?.isEmpty ?: SRVBuilder!!.isEmpty
}

open class OptionBase (
    val description: String,
    val type: OptionType,
    )

class OptionData(
    description: String,
    type: OptionType,
    val isRequired: Boolean = true
    ) : OptionBase(description, type) {
    private var choices = HashMap<String, Any>()

    fun addChoice(name: String, value: String): OptionData {
        if(type != OptionType.STRING)
            throw IllegalArgumentException("Cannot add string choice for Option type $type")
        else {
            choices[name] = value
            return this
        }
    }

    fun addChoice(name: String, value: Int): OptionData {
        if(type != OptionType.INTEGER)
            throw IllegalArgumentException("Cannot add int choice for Option type $type")
        else {
            choices[name] = value
            return this
        }
    }

    fun toNative(name: String): net.dv8tion.jda.api.interactions.commands.build.OptionData {
        return net.dv8tion.jda.api.interactions.commands.build.OptionData(type, name, description, isRequired)
    }

    fun toSRV(name: String): github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.OptionData {
        return github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.OptionData(toSRVType(), name, description, isRequired)
    }

    private fun toSRVType(): github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType {
        return when(type) {
            OptionType.UNKNOWN -> github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType.UNKNOWN
            OptionType.SUB_COMMAND -> github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType.SUB_COMMAND
            OptionType.SUB_COMMAND_GROUP -> github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType.SUB_COMMAND_GROUP
            OptionType.STRING -> github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType.STRING
            OptionType.INTEGER -> github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType.INTEGER
            OptionType.BOOLEAN -> github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType.BOOLEAN
            OptionType.USER -> github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType.USER
            OptionType.CHANNEL -> github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType.CHANNEL
            OptionType.ROLE -> github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType.ROLE
            OptionType.MENTIONABLE -> github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType.MENTIONABLE
            OptionType.NUMBER -> github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType.NUMBER
        }
    }
}

class CommandOptions() {
    val options = HashMap<String, OptionBase>()

    private var defaultEnabled = true

    fun getCommandDataNative(name: String, description: String): net.dv8tion.jda.api.interactions.commands.build.CommandData {
        val cmdData = net.dv8tion.jda.api.interactions.commands.build.CommandData(name, description)

        options.forEach { (key, value) ->
            when(value.type)
            {
                OptionType.STRING, OptionType.INTEGER -> {
                    cmdData.addOption(value.type, key, value.description, (value as OptionData).isRequired)
                    }
            }
        }

        return cmdData
    }

    fun toNative(name: String, description: String): CommandData {
        val cmdData = CommandData(name, description)
            .setDefaultEnabled(defaultEnabled)

        options.forEach { (key, value) ->
            if(value is OptionData)
                cmdData.addOptions(value.toNative(key))
        }

        return cmdData
    }

    fun toSRV(name: String, description: String): github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData {
        val cmdData = github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData(name, description)
            .setDefaultEnabled(defaultEnabled)

        options.forEach { (key, value) ->
            if(value is OptionData)
                cmdData.addOptions(value.toSRV(key))
        }

        return cmdData
    }

    fun setDefaultEnabled(enabled: Boolean): CommandOptions {
        defaultEnabled = enabled
        return this
    }

    fun addOption(type: OptionType, name: String, description: String, required: Boolean = true): CommandOptions {
        options[name] = OptionData(description, type, required)
        return this
    }
}

class ComponentInteractionEvent(
    val main: DiscordEconomyBridge,
    val eventNative: GenericComponentInteractionCreateEvent?,
    val eventSRV: github.scarsz.discordsrv.dependencies.jda.api.events.interaction.GenericComponentInteractionCreateEvent?
) {
    constructor(main: DiscordEconomyBridge, event: GenericComponentInteractionCreateEvent) : this(main, event, null)
    constructor(main: DiscordEconomyBridge, event: github.scarsz.discordsrv.dependencies.jda.api.events.interaction.GenericComponentInteractionCreateEvent) : this(main, null, event)

    val componentId: String
        get() = eventNative?.componentId ?: eventSRV!!.componentId

    val user: DiscordUser
        get() = if(eventNative == null) DiscordUser(eventSRV!!.user) else DiscordUser(eventNative.user)

    val member: DiscordMember?
        get() {
            return if(eventNative == null)
                eventSRV?.member?.let { DiscordMember(it) }
            else eventNative.member?.let { DiscordMember(it) }
        }

    fun reply(content: String): me.pliexe.discordeconomybridge.discord.MessageAction {
        return if(eventNative == null)
            MessageAction(eventSRV!!.reply(content), main)
        else MessageAction(eventNative.reply(content), main)
    }

    fun reply(embed: DiscordEmbed): me.pliexe.discordeconomybridge.discord.MessageAction {
        if(embed.isEmpty)
            reply(embed.content ?: "Missing embed values and content in discord_messages.yml!")

        return if(eventNative == null)
            MessageAction(eventSRV!!.replyEmbeds(embed.getSRV().build()).setContent(embed.content), main)
        else MessageAction(eventNative.replyEmbeds(embed.getNative().build()).setContent(embed.content), main)
    }

    fun replyEphemeral(content: String): me.pliexe.discordeconomybridge.discord.MessageAction {
        return if(eventNative == null)
            MessageAction(eventSRV!!.reply(content).setEphemeral(true), main)
        else MessageAction(eventNative.reply(content).setEphemeral(true), main)
    }

    fun replyEphemeral(embed: DiscordEmbed): me.pliexe.discordeconomybridge.discord.MessageAction {
        if(embed.isEmpty)
            reply(embed.content ?: "Missing embed values and content in discord_messages.yml!")

        return if(eventNative == null)
            MessageAction(eventSRV!!.replyEmbeds(embed.getSRV().build()).setContent(embed.content).setEphemeral(true), main)
        else MessageAction(eventNative.replyEmbeds(embed.getNative().build()).setContent(embed.content).setEphemeral(true), main)
    }

    fun editMessage(content: String): me.pliexe.discordeconomybridge.discord.MessageAction {
        return if(eventNative == null)
            MessageAction(eventSRV!!.editMessage(content), main)
        else MessageAction(eventNative.editMessage(content), main)
    }

    fun editMessage(embed: DiscordEmbed): me.pliexe.discordeconomybridge.discord.MessageAction {
        if(embed.isEmpty)
            editMessage(embed.content ?: "Missing embed values and content in discord_messages.yml!")

        return if(eventNative == null)
            MessageAction(eventSRV!!.editMessageEmbeds(embed.getSRV().build()).setContent(embed.content), main)
        else MessageAction(eventNative.editMessageEmbeds(embed.getNative().build()).setContent(embed.content), main)
    }

    fun CreateEmbed(): DiscordEmbed {
        return if(eventSRV == null)
            DiscordEmbed(EmbedBuilder(), null)
        else DiscordEmbed(null, github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder())
    }

    fun getYMLEmbed(path: String, filter: ((text: String) -> String), resolveScript: ((command: String) -> Boolean)? = null, ignoreDescription: Boolean = false): DiscordEmbed {
        return me.pliexe.discordeconomybridge.discord.getYMLEmbed(main, CreateEmbed(), path, filter, resolveScript, ignoreDescription)
    }
}

class CommandEventData (
    val main: DiscordEconomyBridge,
    val type: Type,
    val commandName: String,
    val prefix: String = "/",
    val args: List<String>? = null,

    private val guildEventNative: GuildMessageReceivedEvent?,
    private val guildEventSRV: github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent?,

    private val globalEventNative: MessageReceivedEvent?,
    private val globalEventSRV: github.scarsz.discordsrv.dependencies.jda.api.events.message.MessageReceivedEvent?,

    private val slashCommandEventNative: net.dv8tion.jda.api.events.interaction.SlashCommandEvent?,
    private val slashCommandEventSRV: SlashCommandEvent?
        ) {

    val member: DiscordMember?
        get() {
            return when(type) {
                Type.GuildMessageNative -> guildEventNative!!.member?.let { DiscordMember(it) }
                Type.MessageNative -> globalEventNative!!.member?.let { DiscordMember(it) }
                Type.SlashCommandNative -> slashCommandEventNative!!.member?.let { DiscordMember(it) }
                Type.GuildMessageSRV -> guildEventSRV!!.member?.let { DiscordMember(it) }
                Type.MessageSRV -> globalEventSRV!!.member?.let { DiscordMember(it) }
                Type.SlashCommandSRV -> slashCommandEventSRV!!.member?.let { DiscordMember(it) }
            }
        }

    val author: DiscordUser
        get() {
            return when(type) {
                Type.GuildMessageNative -> DiscordUser(guildEventNative!!.author)
                Type.MessageNative -> DiscordUser(globalEventNative!!.author)
                Type.SlashCommandNative -> DiscordUser(slashCommandEventNative!!.user)
                Type.GuildMessageSRV -> DiscordUser(guildEventSRV!!.author)
                Type.MessageSRV -> DiscordUser(globalEventSRV!!.author)
                Type.SlashCommandSRV -> DiscordUser(slashCommandEventSRV!!.user)
            }
        }

    val user: DiscordUser
        get() = author

    val message: Message?
        get() {
            return when(type) {
                Type.GuildMessageNative -> Message(guildEventNative!!.message, main)
                Type.MessageNative -> Message(globalEventNative!!.message, main)
                Type.SlashCommandNative -> null
                Type.GuildMessageSRV -> Message(guildEventSRV!!.message, main)
                Type.MessageSRV -> Message(globalEventSRV!!.message, main)
                Type.SlashCommandSRV -> null
            }
        }

    fun isNative(): Boolean {
        return type.ordinal < 3
    }

    fun inGuild(): Boolean {
        return type != Type.MessageSRV && type != Type.MessageNative
    }

    fun isSlashCommand(): Boolean {
        return type == Type.SlashCommandSRV || type == Type.SlashCommandNative
    }

    fun getOptionDouble(name: String): Double? {
        return if(isNative())
            slashCommandEventNative!!.options.find { it.name == name }?.asDouble
        else slashCommandEventSRV!!.options.find { it.name == name }?.asDouble
    }

    fun getOptionInt(name: String): Int? {
        return if(isNative())
            slashCommandEventNative!!.options.find { it.name == name }?.asLong?.toInt()
        else slashCommandEventSRV!!.options.find { it.name == name }?.asLong?.toInt()
    }

    fun getOptionString(name: String): String? {
        return if(isNative())
            slashCommandEventNative!!.options.find { it.name == name }?.asString
        else slashCommandEventSRV!!.options.find { it.name == name }?.asString
    }

    fun getOptionMember(name: String): DiscordMember? {
        return if(isNative())
        {
            val member = slashCommandEventNative!!.options.find { it.name == name }?.asMember
            if(member == null) null
            else DiscordMember(member)
        }
        else {
            val member = slashCommandEventSRV!!.options.find { it.name == name }?.asMember
            if(member == null) null
            else DiscordMember(member)
        }
    }

    fun getOptionUser(name: String): DiscordUser? {
        return if(isNative())
        {
            val user = slashCommandEventNative!!.options.find { it.name == name }?.asUser
            if(user == null) null
            else DiscordUser(user)
        }
        else
        {
            val user = slashCommandEventSRV!!.options.find { it.name == name }?.asUser
            if(user == null) null
            else DiscordUser(user)
        }
    }

    fun getUserMention(index: Int): DiscordUser {
        return when(type) {
            Type.GuildMessageNative -> DiscordUser(guildEventNative!!.message.mentionedUsers[index])
            Type.GuildMessageSRV -> DiscordUser(guildEventSRV!!.message.mentionedUsers[index])
            Type.MessageNative -> DiscordUser(globalEventNative!!.message.mentionedUsers[index])
            Type.MessageSRV -> DiscordUser(globalEventSRV!!.message.mentionedUsers[index])
            else -> null!!
        }
    }

    fun userMentionsSize(): Int {
        return when(type) {
            Type.GuildMessageNative -> guildEventNative!!.message.mentionedUsers.size
            Type.GuildMessageSRV -> guildEventSRV!!.message.mentionedUsers.size
            Type.MessageNative -> globalEventNative!!.message.mentionedUsers.size
            Type.MessageSRV -> globalEventSRV!!.message.mentionedUsers.size
            else -> 0
        }
    }

    fun getMemberMention(index: Int): DiscordMember {
        return when(type) {
            Type.GuildMessageNative -> DiscordMember(guildEventNative!!.message.mentionedMembers[index])
            Type.GuildMessageSRV -> DiscordMember(guildEventSRV!!.message.mentionedMembers[index])
            Type.MessageNative -> DiscordMember(globalEventNative!!.message.mentionedMembers[index])
            Type.MessageSRV -> DiscordMember(globalEventSRV!!.message.mentionedMembers[index])
            else -> null!!
        }
    }

    fun memberMentionsSize(): Int {
        return when(type) {
            Type.GuildMessageNative -> guildEventNative!!.message.mentionedMembers.size
            Type.GuildMessageSRV -> guildEventSRV!!.message.mentionedMembers.size
            Type.MessageNative -> globalEventNative!!.message.mentionedMembers.size
            Type.MessageSRV -> globalEventSRV!!.message.mentionedMembers.size
            else -> 0
        }
    }

    enum class Type {
        GuildMessageNative,
        MessageNative,
        SlashCommandNative,
        GuildMessageSRV,
        MessageSRV,
        SlashCommandSRV
    }

    constructor(main: DiscordEconomyBridge, event: GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) : this(main, Type.GuildMessageNative, commandName, prefix, args, event, null, null, null, null, null)
    constructor(main: DiscordEconomyBridge, event: github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageReceivedEvent, commandName: String, prefix: String, args: List<String>) : this(main, Type.GuildMessageSRV, commandName, prefix, args, null, event, null, null, null, null)
    constructor(main: DiscordEconomyBridge, event: MessageReceivedEvent, commandName: String, prefix: String, args: List<String>) : this(main, Type.MessageNative, commandName, prefix, args, null, null, event, null, null, null)
    constructor(main: DiscordEconomyBridge, event: github.scarsz.discordsrv.dependencies.jda.api.events.message.MessageReceivedEvent, commandName: String, prefix: String, args: List<String>) : this(main, Type.MessageSRV, commandName, prefix, args, null, null, null, event, null, null)
    constructor(main: DiscordEconomyBridge, event: net.dv8tion.jda.api.events.interaction.SlashCommandEvent, commandName: String, prefix: String) : this(main, Type.SlashCommandNative, commandName, prefix, null, null, null, null, null, event, null)
    constructor(main: DiscordEconomyBridge, event: SlashCommandEvent, commandName: String, prefix: String) : this(main, Type.SlashCommandNative, commandName, prefix, null, null, null, null, null, null, event)

    fun sendMessage(embed: DiscordEmbed): me.pliexe.discordeconomybridge.discord.MessageAction {
        if(embed.isEmpty)
            sendMessage(embed.content ?: "Missing embed values and content in discord_messages.yml!")

        return when(type) {
            Type.GuildMessageNative -> MessageAction(guildEventNative!!.channel.sendMessageEmbeds(embed.getNative().build()).content(embed.content), main)
            Type.GuildMessageSRV -> MessageAction(guildEventSRV!!.channel.sendMessageEmbeds(embed.getSRV().build()).content(embed.content), main)
            Type.MessageNative -> MessageAction(globalEventNative!!.channel.sendMessageEmbeds(embed.getNative().build()).content(embed.content), main)
            Type.MessageSRV -> MessageAction(globalEventSRV!!.channel.sendMessageEmbeds(embed.getSRV().build()).content(embed.content), main)
            Type.SlashCommandNative -> MessageAction(slashCommandEventNative!!.replyEmbeds(embed.getNative().build()).setContent(embed.content), main)
            Type.SlashCommandSRV -> MessageAction(slashCommandEventSRV!!.replyEmbeds(embed.getSRV().build()).setContent(embed.content), main)
        }
    }

    fun sendMessage(content: String): me.pliexe.discordeconomybridge.discord.MessageAction {
        return when(type) {
            Type.GuildMessageNative -> MessageAction(guildEventNative!!.channel.sendMessage(content), main)
            Type.GuildMessageSRV -> MessageAction(guildEventSRV!!.channel.sendMessage(content), main)
            Type.MessageNative -> MessageAction(globalEventNative!!.channel.sendMessage(content), main)
            Type.MessageSRV -> MessageAction(globalEventSRV!!.channel.sendMessage(content), main)
            Type.SlashCommandNative -> MessageAction(slashCommandEventNative!!.reply(content), main)
            Type.SlashCommandSRV -> MessageAction(slashCommandEventSRV!!.reply(content), main)
        }
    }

    fun CreateEmbed(): DiscordEmbed {
        return if(isNative())
                DiscordEmbed(EmbedBuilder(), null)
            else DiscordEmbed(null, github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder())
    }

    fun getYMLEmbed(path: String, filter: ((text: String) -> String), resolveScript: ((command: String) -> Boolean)? = null, ignoreDescription: Boolean = false): DiscordEmbed {
        return me.pliexe.discordeconomybridge.discord.getYMLEmbed(main, CreateEmbed(), path, filter, resolveScript, ignoreDescription)
    }

    fun sendYMLEmbed(path: String, filter: ((text: String) -> String), resolveScript: ((command: String) -> Boolean)? = null, ignoreDescription: Boolean = false): me.pliexe.discordeconomybridge.discord.MessageAction {
        return sendMessage(getYMLEmbed(path, filter, resolveScript, ignoreDescription))
    }
}

abstract class Command(protected val main: DiscordEconomyBridge) {

    protected val config: FileConfiguration = main.config
    protected val server: Server = main.server

    abstract val name: String
    abstract val usage: String
    abstract val description: String

//    abstract val guildOnly: Boolean

    abstract fun run(event: CommandEventData)

    abstract fun getCommandOptions(): CommandOptions

    open val adminCommand = false

    fun getSlashCommandDataNative(): CommandData {
        return getCommandOptions().toNative(name, description)
    }

    fun getSlashCommandDataSRV(): github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData {
        return getCommandOptions().toSRV(name, description)
    }

    fun fail(event: CommandEventData, message: String) {
        event.sendYMLEmbed("failMessage", {
            val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), name, description, usage)
                .replace("{message}", message)

            if(event.member == null)
                setDiscordPlaceholders(event.author, form)
            else
                setDiscordPlaceholders(event.member!!, form)
        }).queue()
    }

    fun noPermission(event: CommandEventData)
    {
        event.sendYMLEmbed("noPermissionMessage", {
            val form = setCommandPlaceholders(it, main.defaultConfig.getString("PREFIX"), name, description, usage)

            if(event.member == null)
                setDiscordPlaceholders(event.author, form)
            else setDiscordPlaceholders(event.member!!, form)
        }).queue()
    }
}