package de.kb1000.gudbot.database

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.time.Instant

// No unsigned type support?
object ULongSqlType : SqlType<ULong>(Types.BIGINT, "bigint") {
    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: ULong) {
        ps.setLong(index, parameter.toLong())
    }

    override fun doGetResult(rs: ResultSet, index: Int): ULong {
        return rs.getLong(index).toULong()
    }
}

fun BaseTable<*>.ulong(name: String) = this.registerColumn(name, ULongSqlType)

interface Tag : Entity<Tag> {
    companion object : Entity.Factory<Tag>()

    val id: Int
    var guildId: ULong
    var name: String
    var creator: ULong
    var creationTime: Instant
    var content: String
}

object Tags : Table<Tag>("tags_tag") {
    val id = int("id").primaryKey().bindTo { it.id }
    val guildId = ulong("guild_id").bindTo { it.guildId }
    val name = varchar("name").bindTo { it.name }
    val creator = ulong("creator").bindTo { it.creator }
    val creationTime = timestamp("creation_time").bindTo { it.creationTime }
    val content = text("content").bindTo { it.content }
}

val Database.tags get() = this.sequenceOf(Tags)

interface TrustedRole : Entity<TrustedRole> {
    companion object : Entity.Factory<TrustedRole>()

    val id: Int
    var guildId: ULong
    var roleId: ULong
}

object TrustedRoles : Table<TrustedRole>("tags_trustedrole") {
    val id = int("id").primaryKey().bindTo { it.id }
    val guildId = ulong("guild_id").bindTo { it.guildId }
    val roleId = ulong("role_id").bindTo { it.roleId }
}

val Database.trustedRoles get() = this.sequenceOf(TrustedRoles)


interface TrustedUser : Entity<TrustedUser> {
    companion object : Entity.Factory<TrustedUser>()

    val id: Int
    var guildId: ULong
    var userId: ULong
}

object TrustedUsers : Table<TrustedUser>("tags_trusteduser") {
    val id = int("id").primaryKey().bindTo { it.id }
    val guildId = ulong("guild_id").bindTo { it.guildId }
    val userId = ulong("user_id").bindTo { it.userId }
}

val Database.trustedUsers get() = this.sequenceOf(TrustedUsers)
