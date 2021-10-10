package de.kb1000.gudbot

import com.charleskorn.kaml.Yaml
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.gateway.ReadyEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.nio.file.Files
import kotlin.io.path.Path

@Serializable
data class Config(
    val database: Map<String, String>,
    val presence: String? = null,
    val token: String,
    val environment: String,
    @SerialName("testGuild")
    @JvmField
    private val _testGuild: Long? = null,
) {
    val testGuild
        get() = _testGuild?.toULong()
}

private fun loadConfig() = Files.newInputStream(Path("config.yaml")).use {
    Yaml.default.decodeFromStream(Config.serializer(), it)
}

val config by lazy { loadConfig() }


class ConfigExtension : Extension() {
    override val name = "config"

    var ownerIds = setOf<Snowflake>()

    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                val appInfo = kord.getApplicationInfo()
                val appOwner = appInfo.ownerId
                val teamOwner = appInfo.team?.ownerUserId
                val teamUsers = appInfo.team?.members?.map { it.userId } ?: listOf()
                val mutableOwnerIds = mutableListOf(appOwner, *teamUsers.toTypedArray())
                if (teamOwner != null) {
                    mutableOwnerIds.add(teamOwner)
                }
                ownerIds = mutableOwnerIds.toSet()
            }
        }
    }
}
