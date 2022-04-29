@file:JvmName("Main")

package de.kb1000.gudbot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent

@OptIn(PrivilegedIntent::class)
suspend fun main() {
    val bot = ExtensibleBot(config.token) {
        presence {
            config.presence?.let(this::playing)
        }
        applicationCommands {
            config.testGuild?.let { this.defaultGuild(it) }
        }

        extensions {
            add(::ConfigExtension)
            add(::TagExtension)
        }

        chatCommands {
            enabled = true
            defaultPrefix = "$"
        }

        intents {
            +Intent.GuildMembers
        }
    }
    bot.start()
}
