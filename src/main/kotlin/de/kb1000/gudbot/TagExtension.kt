package de.kb1000.gudbot

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatGroupCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasPermission
import de.kb1000.gudbot.database.*
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Member
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.rest.Image
import dev.kord.rest.builder.message.create.allowedMentions
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KLogging
import org.jetbrains.annotations.Contract
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant
import java.util.regex.Pattern

class TagExtension : Extension() {
    companion object : KLogging() {
        @JvmStatic
        val namePattern: Pattern = Pattern.compile("^[\\w-]{1,32}\$", Pattern.UNICODE_CHARACTER_CLASS)
    }

    override val name = "tags"
    private val tagCommandMap = mutableMapOf<ULong, MutableMap<String, PublicSlashCommand<Arguments>>>()
    private val configExtension: ConfigExtension by lazy { bot.findExtension()!! }

    class TagContentArgs : Arguments() {
        val name by string {
            name = "name"
            description = "Name of the tag"
        }
        val content by coalescingString {
            name = "content"
            description = "Content of the tag"
        }
    }

    class TagNameArgs : Arguments() {
        val name by string {
            name ="name"
            description ="Name of the tag"
        }
    }

    class TrustMemberArgs : Arguments() {
        val member by member {
            name = "member"
            description = "Member to trust"
        }
    }

    class TrustRoleArgs : Arguments() {
        val role by role {
            name = "role"
            description = "Role to trust"
        }
    }

    class UntrustMemberArgs : Arguments() {
        val member by member {
            name = "member"
            description = "Member to untrust"
        }
    }

    class UntrustRoleArgs : Arguments() {
        val role by role {
            name = "role"
            description = "Role to untrust"
        }
    }

    private suspend fun createTagCommand(tag: Tag): PublicSlashCommand<Arguments> {
        val command = PublicSlashCommand<Arguments>(this, null, null, null)
        command.run {
            name = tag.name
            description = "\"${tag.name}\" tag"

            guild(tag.guildId.toLong())

            action {
                respond {
                    content =
                        database.tags.find { (it.guildId eq guild!!.id.value) and (it.name eq this@run.name) }?.content
                            ?: "\u274c Tag not found."
                    allowedMentions {
                    }
                }
            }
        }

        tagCommandMap.computeIfAbsent(tag.guildId) { mutableMapOf() }[tag.name] = command
        return command
    }

    @Contract(pure = true)
    private suspend fun isAdmin(member: Member): Boolean {
        return member.hasPermission(Permission.Administrator) or configExtension.ownerIds.contains(member.id)
    }

    @Contract(pure = true)
    private suspend fun isTrustedUser(member: Member) = isAdmin(member) || member.roleIds.let { roleIds ->
        roleIds.isNotEmpty() && database.trustedRoles.any {
            (it.guildId eq member.guildId.value) and (it.roleId inList roleIds.map(Snowflake::value))
        }
    } || database.trustedUsers.any { (it.guildId eq member.guildId.value) and (it.userId eq member.id.value) }

    override suspend fun setup() {
        publicSlashCommand {
            name = "tags"
            description = "A command to manage tags"

            check { anyGuild() }

            publicSubCommand(::TagContentArgs) {
                name = "create"
                description = "Create a tag"

                check { anyGuild() }

                check {
                    val member = memberFor(event)?.asMember()
                    passed = member?.let { isTrustedUser(it) } ?: false
                    if (!passed) {
                        fail("You don't have permission to use this command.")
                    }
                }

                action {
                    val name = arguments.name.lowercase()
                    if (!namePattern.matcher(name).matches()) {
                        respond {
                            content = "\u274c Error: invalid tag name \"${name}\"!"
                            allowedMentions {
                            }
                        }
                        return@action
                    }
                    try {
                        val tag = Tag {
                            guildId = guild!!.id.value
                            this.name = name
                            creator = user.id.value
                            creationTime = Instant.now()
                            content = arguments.content
                        }
                        database.tags.add(tag)
                        applicationCommandRegistry.register(createTagCommand(tag))
                        respond {
                            content = "\u2705 Successfully created tag \"${name}\""
                            allowedMentions {
                            }
                        }
                    } catch (_: SQLIntegrityConstraintViolationException) {
                        respond {
                            content = "\u274c Error: tag \"${name}\" already exists"
                            allowedMentions {
                            }
                        }
                    }
                }
            }

            publicSubCommand(::TagContentArgs) {
                name = "edit"
                description = "Edit a tag"

                check { anyGuild() }

                check {
                    val member = memberFor(event)?.asMember()
                    passed = member?.let { isTrustedUser(it) } ?: false
                    if (!passed) {
                        fail("You don't have permission to use this command.")
                    }
                }

                action {
                    val isAdmin = isAdmin(member!!.asMember())
                    val tag =
                        database.tags.find { (it.guildId eq guild!!.id.value) and (it.name eq arguments.name) and ((it.creator eq user.id.value) or isAdmin) }
                    if (tag == null) {
                        respond {
                            content = "\u274c Tag does not exist, or you're not the owner of the tag."
                            allowedMentions {
                            }
                        }
                    } else {
                        tag.content = arguments.content
                        tag.flushChanges()
                        respond {
                            content = "\u2705 Tag \"${arguments.name}\" edited successfully."
                            allowedMentions {
                            }
                        }
                    }
                }
            }

            publicSubCommand(::TagNameArgs) {
                name = "remove"
                description = "Remove a tag"

                check { anyGuild() }

                action {
                    val isAdmin = isAdmin(member!!.asMember())
                    val removed =
                        database.tags.removeIf { (it.guildId eq guild!!.id.value) and (it.name eq arguments.name) and ((it.creator eq user.id.value) or isAdmin) }
                    val slashCommand = tagCommandMap[guild!!.id.value]?.get(arguments.name)
                    if (slashCommand != null)
                        applicationCommandRegistry.unregister(slashCommand)
                    respond {
                        content = if (removed == 0)
                            "\u274c Tag does not exist, or you're not the owner of the tag."
                        else
                            "\u2705 Tag \"${arguments.name}\" removed successfully."
                        allowedMentions {
                        }
                    }
                }
            }

            publicSubCommand(::TrustMemberArgs) {
                name = "trust_member"
                description = "Add a member to the trust list"

                check { anyGuild() }

                check {
                    passed = isAdmin(memberFor(event)!!.asMember())
                    if (!passed) {
                        fail("You don't have permission to use this command.")
                    }
                }

                action {
                    try {
                        database.trustedUsers.add(TrustedUser {
                            guildId = arguments.member.guildId.value
                            userId = arguments.member.id.value
                        })
                        respond {
                            content =
                                "\u2705 Successfully added ${arguments.member.nicknameMention} (${arguments.member.username}) to the trust list."
                            allowedMentions {
                            }
                        }
                    } catch (_: SQLIntegrityConstraintViolationException) {
                        respond {
                            content =
                                "\u274c ${arguments.member.nicknameMention} (${arguments.member.username}) is already on the trust list."
                            allowedMentions {
                            }
                        }
                    }
                }
            }

            publicSubCommand(::TrustRoleArgs) {
                name = "trust_role"
                description = "Add a role to the trust list"

                check { anyGuild() }

                check {
                    passed = isAdmin(memberFor(event)!!.asMember())
                    if (!passed) {
                        fail("You don't have permission to use this command.")
                    }
                }

                action {
                    try {
                        database.trustedRoles.add(TrustedRole {
                            guildId = arguments.role.guildId.value
                            roleId = arguments.role.id.value
                        })
                        respond {
                            content =
                                "\u2705 Successfully added ${arguments.role.mention} (${arguments.role.name}) to the trust list."
                            allowedMentions {
                            }
                        }
                    } catch (_: SQLIntegrityConstraintViolationException) {
                        respond {
                            content =
                                "\u274c ${arguments.role.mention} (${arguments.role.name}) is already on the trust list."
                            allowedMentions {
                            }
                        }
                    }

                }
            }

            publicSubCommand(::UntrustMemberArgs) {
                name = "untrust_member"
                description = "Remove a member from the trust list"

                check { anyGuild() }

                check {
                    passed = isAdmin(memberFor(event)!!.asMember())
                    if (!passed) {
                        fail("You don't have permission to use this command.")
                    }
                }

                action {
                    if (database.trustedUsers.removeIf { (it.guildId eq arguments.member.guildId.value) and (it.userId eq arguments.member.id.value) } != 0) {
                        respond {
                            content =
                                "\u2705 Successfully removed ${arguments.member.nicknameMention} (${arguments.member.username}) from the trust list."
                            allowedMentions {
                            }
                        }
                    } else {
                        respond {
                            content =
                                "\u274c ${arguments.member.nicknameMention} (${arguments.member.username}) is not on the trust list."
                            allowedMentions {
                            }
                        }
                    }
                }
            }

            publicSubCommand(::UntrustRoleArgs) {
                name = "untrust_role"
                description = "Remove a role from the trust list"

                check { anyGuild() }

                check {
                    passed = isAdmin(memberFor(event)!!.asMember())
                    if (!passed) {
                        fail("You don't have permission to use this command.")
                    }
                }

                action {
                    if (database.trustedRoles.removeIf { (it.guildId eq arguments.role.guildId.value) and (it.roleId eq arguments.role.id.value) } != 0) {
                        respond {
                            content =
                                "\u2705 Successfully removed ${arguments.role.mention} (${arguments.role.name}) from the trust list."
                            allowedMentions {
                            }
                        }
                    } else {
                        respond {
                            content =
                                "\u274c ${arguments.role.mention} (${arguments.role.name}) is not on the trust list."
                            allowedMentions {
                            }
                        }
                    }
                }
            }

            publicSubCommand {
                name = "list"
                description = "List tags"

                check { anyGuild() }

                action {
                    val guild = guild!!.asGuild()
                    respond {
                        embed {
                            description = database.tags.filter { it.guildId eq guild.id.value }.mapColumns { it.name }
                                .joinToString("\n")
                            author {
                                name = guild.name
                                icon = guild.getIconUrl(Image.Format.WEBP)
                            }
                        }

                        allowedMentions {
                        }
                    }
                }
            }
        }

        chatGroupCommand {
            name = "tags"
            description = "A command to manage tags"

            check { anyGuild() }

            chatCommand(::TagContentArgs) {
                name = "create"
                description = "Create a tag"

                check { anyGuild() }

                check {
                    val member = memberFor(event)?.asMember()
                    passed = member?.let { isTrustedUser(it) } ?: false
                    if (!passed) {
                        logger.debug("Failing check")
                        fail("You don't have permission to use this command.")
                    }
                }

                action {
                    val name = arguments.name.lowercase()
                    if (!namePattern.matcher(name).matches()) {
                        channel.createMessage {
                            content = "\u274c Error: invalid tag name \"${name}\"!"
                            allowedMentions {
                            }
                        }
                        return@action
                    }
                    try {
                        val tag = Tag {
                            guildId = guild!!.id.value
                            this.name = name
                            creator = user!!.id.value
                            creationTime = Instant.now()
                            content = arguments.content
                        }
                        database.tags.add(tag)
                        applicationCommandRegistry.register(createTagCommand(tag))
                        channel.createMessage {
                            content = "\u2705 Successfully created tag \"${name}\""
                            allowedMentions {
                            }
                        }

                    } catch (_: SQLIntegrityConstraintViolationException) {
                        channel.createMessage {
                            content = "\u274c Error: tag \"${name}\" already exists"
                            allowedMentions {
                            }
                        }
                    }
                }
            }


            chatCommand(::TagContentArgs) {
                name = "edit"
                description = "Edit a tag"

                check { anyGuild() }

                check {
                    val member = memberFor(event)?.asMember()
                    passed = member?.let { isTrustedUser(it) } ?: false
                    if (!passed) {
                        fail("You don't have permission to use this command.")
                    }
                }

                action {
                    val isAdmin = isAdmin(member!!.asMember())
                    val tag =
                        database.tags.find { (it.guildId eq guild!!.id.value) and (it.name eq arguments.name) and ((it.creator eq user!!.id.value) or isAdmin) }
                    if (tag == null) {
                        channel.createMessage {
                            content = "\u274c Tag does not exist, or you're not the owner of the tag."
                            allowedMentions {
                            }
                        }
                    } else {
                        tag.content = arguments.content
                        tag.flushChanges()
                        channel.createMessage {
                            content = "\u2705 Tag \"${arguments.name}\" edited successfully."
                            allowedMentions {
                            }
                        }
                    }
                }
            }

            chatCommand(::TagNameArgs) {
                name = "tag"
                description = "Display a tag'/cheeses contents"

                check { anyGuild() }

                action {
                    channel.createMessage {
                        content =
                            database.tags.find { (it.name eq arguments.name) and (it.guildId eq guild!!.id.value) }?.content
                                ?: "\u274c Tag not found."
                        allowedMentions {
                        }
                    }
                }
            }
        }

        for (tag in database.tags) {
            publicSlashCommand(createTagCommand(tag))
        }
        event<ReadyEvent> {
            action {
                val ids = kord.guilds.map { it.id.value }.toList()
                if (ids.isNotEmpty())
                    database.tags.removeIf { it.guildId notInList ids }
            }
        }
    }
}
