package de.kb1000.gudbot

import ch.qos.logback.core.PropertyDefinerBase
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.gateway.ReadyEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.io.path.Path
import kotlin.io.path.inputStream

@Serializable
data class Config(
    val database: Map<String, String>,
    val presence: String? = null,
    val token: String,
    val environment: String,
    @SerialName("testGuild")
    private val _testGuild: Long? = null,
) {
    val testGuild
        get() = _testGuild?.toULong()
}

private fun loadConfig() = Path("config.yaml").inputStream().use {
    Yaml.default.decodeFromStream(Config.serializer(), it)
}

val config by lazy { loadConfig() }

class DefaultLevelPropertyDefiner : PropertyDefinerBase() {
    override fun getPropertyValue() = when (config.environment) {
        "debug" -> "DEBUG"
        else -> "INFO"
    }
}


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
                val mutableOwnerIds = teamUsers.toMutableList()
                if (teamOwner != null) mutableOwnerIds.add(teamOwner)
                if (appOwner != null) mutableOwnerIds.add(appOwner)
                ownerIds = mutableOwnerIds.toSet()
            }
        }
    }
}
