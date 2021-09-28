package de.kb1000.gudbot

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescedString
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatGroupCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.hasPermission
import de.kb1000.gudbot.database.*
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Member
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.rest.builder.message.create.allowedMentions
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.notInList
import org.ktorm.dsl.or
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.removeIf
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant

class TagExtension : Extension() {
    override val name = "tags"
    private val tagCommandMap = mutableMapOf<Long, MutableMap<String, PublicSlashCommand<Arguments>>>()
    private val configExtension: ConfigExtension by lazy { bot.findExtension()!! }

    class TagContentArgs : Arguments() {
        val name by string("name", "Name of the tag")
        val content by coalescedString("content", "Content of the tag")
    }

    class TagNameArgs : Arguments() {
        val name by string("name", "Name of the tag")
    }

    class TrustMemberArgs : Arguments() {
        val member by member("member", "Member to trust")
    }

    class TrustRoleArgs : Arguments() {
        val role by role("role", "Role to trust")
    }

    class UntrustMemberArgs : Arguments() {
        val member by member("member", "Member to untrust")
    }

    class UntrustRoleArgs : Arguments() {
        val role by role("role", "Role to untrust")
    }

    private suspend fun createTagCommand(tag: Tag): PublicSlashCommand<Arguments> {
        val command = PublicSlashCommand<Arguments>(this, null, null, null)
        command.run {
            name = tag.name
            description = "\"${tag.name}\" tag"

            guild(tag.guildId)

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

    private suspend fun isAdmin(member: Member): Boolean {
        return member.hasPermission(Permission.Administrator) or configExtension.ownerIds.contains(member.id)
    }

    private suspend fun isTrustedUser(member: Member): Boolean {
        return isAdmin(member) or run {
            true
        } // XXX
    }

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
                }

                action {
                    try {
                        val tag = Tag {
                            guildId = guild!!.id.value
                            name = arguments.name
                            creator = user.id.value
                            creationTime = Instant.now()
                            content = arguments.content
                        }
                        database.tags.add(tag)
                        applicationCommandRegistry.register(createTagCommand(tag))
                        respond {
                            content = "\u2705 Successfully created tag \"${arguments.name}\""
                            allowedMentions {
                            }
                        }
                    } catch (_: SQLIntegrityConstraintViolationException) {
                        respond {
                            content = "\u274c Error: tag \"${arguments.name}\" already exists"
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
                    val slashCommand = tagCommandMap[guild!!.id.value]!![arguments.name]!!
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

                check { isAdmin(memberFor(event)!!.asMember()) }

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

                check { isAdmin(memberFor(event)!!.asMember()) }

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

                check { isAdmin(memberFor(event)!!.asMember()) }

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

                check { isAdmin(memberFor(event)!!.asMember()) }

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
                }

                action {
                    try {
                        val tag = Tag {
                            guildId = guild!!.id.value
                            name = arguments.name
                            creator = user!!.id.value
                            creationTime = Instant.now()
                            content = arguments.content
                        }
                        database.tags.add(tag)
                        applicationCommandRegistry.register(createTagCommand(tag))
                        channel.createMessage {
                            content = "\u2705 Successfully created tag \"${arguments.name}\""
                            allowedMentions {
                            }
                        }

                    } catch (_: SQLIntegrityConstraintViolationException) {
                        channel.createMessage {
                            content = "\u274c Error: tag \"${arguments.name}\" already exists"
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
        }

        for (tag in database.tags) {
            publicSlashCommand(createTagCommand(tag))
        }
        event<ReadyEvent> {
            action {
                val ids = kord.guilds.map { it.id.value }.toList()
                database.tags.removeIf { it.guildId notInList ids }
            }
        }
    }
}