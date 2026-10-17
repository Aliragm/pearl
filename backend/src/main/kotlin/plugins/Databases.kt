package gg.pearl.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val explicitEnvUrl = System.getenv("DB_URL")?.takeIf { it.isNotBlank() }
    val dbUrl = explicitEnvUrl
        ?: environment.config.propertyOrNull("database.url")?.getString()?.takeIf { it.isNotBlank() }
        ?: "jdbc:postgresql://localhost:5432/pearl_db"
    val dbUser = System.getenv("DB_USER")?.takeIf { it.isNotBlank() }
        ?: environment.config.propertyOrNull("database.user")?.getString()?.takeIf { it.isNotBlank() }
        ?: "postgres"
    val dbPassword = System.getenv("DB_PASSWORD")?.takeIf { it.isNotBlank() }
        ?: environment.config.propertyOrNull("database.password")?.getString()?.takeIf { it.isNotBlank() }
        ?: "postgres"

    log.info("Connecting to database at $dbUrl...")

    try {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = dbUrl
            username = dbUser
            password = dbPassword
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        val dataSource = HikariDataSource(hikariConfig)

        // Flyway schema migration
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()

        val migrationResult = flyway.migrate()
        log.info("Flyway applied ${migrationResult.migrationsExecuted} migrations successfully.")

        Database.connect(dataSource)
    } catch (e: Exception) {
        if (explicitEnvUrl != null) {
            throw e
        }
        log.warn("Could not connect to default local PostgreSQL at $dbUrl (${e.message}). Running without active DB connection.")
    }
}
