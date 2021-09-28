import ch.qos.logback.core.joran.spi.ConsoleTarget
import de.kb1000.gudbot.ConfigKt
import de.kb1000.gudbot.StartupTimeBasedTriggeringPolicy

def environment = ConfigKt.config.environment
def defaultLevel = INFO

if (environment == "debug") {
    defaultLevel = DEBUG

    // Silence warning about missing native PRNG on Windows
    logger("io.ktor.util.random", ERROR)
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss:SSS Z} | %5level | %40.40logger{40} | %msg%n"
    }

    target = ConsoleTarget.SystemErr
}

appender("FILE", RollingFileAppender) {
    file = "logs/log.log"
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "log.%d{yyyyMMdd}_%d{HHmmss,aux}.log.gz"
        timeBasedFileNamingAndTriggeringPolicy(StartupTimeBasedTriggeringPolicy)
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss:SSS Z} | %5level | %40.40logger{40} | %msg%n"
    }
}

root(defaultLevel, ["CONSOLE", "FILE"])
