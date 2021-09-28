package de.kb1000.gudbot.database

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.Instant

interface Tag : Entity<Tag> {
    companion object : Entity.Factory<Tag>()
    val id: Int
    var guildId: Long
    var name: String
    var creator: Long
    var creationTime: Instant
    var content: String
}

object Tags : Table<Tag>("tags_tag") {
    val id = int("id").primaryKey().bindTo { it.id }
    val guildId = long("guild_id").bindTo { it.guildId }
    val name = varchar("name").bindTo { it.name }
    val creator = long("creator").bindTo { it.creator }
    val creationTime = timestamp("creation_time").bindTo { it.creationTime }
    val content = text("content").bindTo { it.content }
}

val Database.tags get() = this.sequenceOf(Tags)

interface TrustedRole : Entity<TrustedRole> {
    companion object : Entity.Factory<TrustedRole>()
    val id: Int
    var guildId: Long
    var roleId: Long
}

object TrustedRoles : Table<TrustedRole>("tags_trustedrole") {
    val id = int("id").primaryKey().bindTo { it.id }
    val guildId = long("guild_id").bindTo { it.guildId }
    val roleId = long("role_id").bindTo { it.roleId }
}

val Database.trustedRoles get() = this.sequenceOf(TrustedRoles)


interface TrustedUser : Entity<TrustedUser> {
    companion object : Entity.Factory<TrustedUser>()
    val id: Int
    var guildId: Long
    var userId: Long
}

object TrustedUsers : Table<TrustedUser>("tags_trusteduser") {
    val id = int("id").primaryKey().bindTo { it.id }
    val guildId = long("guild_id").bindTo { it.guildId }
    val userId = long("user_id").bindTo { it.userId }
}

val Database.trustedUsers get() = this.sequenceOf(TrustedUsers)