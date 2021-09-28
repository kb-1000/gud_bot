package de.kb1000.gudbot

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.ktorm.database.Database
import org.ktorm.database.JdbcTransactionManager
import java.sql.SQLIntegrityConstraintViolationException


private fun connect(): Database {
    val hikariConfig = HikariConfig(config.database.toProperties())
    val dataSource = HikariDataSource(hikariConfig)

    return Database(
        transactionManager = JdbcTransactionManager { dataSource.connection },
        exceptionTranslator = { ex ->
            when {
                ex.sqlState.startsWith("23") -> SQLIntegrityConstraintViolationException(ex.message, ex.sqlState, ex.errorCode, ex)
                else -> ex
            }
        }
    )
    return Database.connect(dataSource)
}

val database by lazy(::connect)
