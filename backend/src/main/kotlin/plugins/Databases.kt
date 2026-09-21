package gg.pearl.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val dbUrl = System.getenv("DB_URL")
        ?: environment.config.propertyOrNull("database.url")?.getString()?.takeIf { it.isNotBlank() }
    val dbUser = System.getenv("DB_USER")
        ?: environment.config.propertyOrNull("database.user")?.getString()?.takeIf { it.isNotBlank() }
    val dbPassword = System.getenv("DB_PASSWORD")
        ?: environment.config.propertyOrNull("database.password")?.getString()?.takeIf { it.isNotBlank() }

    if (dbUrl.isNullOrBlank() || dbUser.isNullOrBlank() || dbPassword.isNullOrBlank()) {
        log.warn("Database environment variables (DB_URL, DB_USER, DB_PASSWORD) are not configured. Skipping database connection.")
        return
    }

    log.info("Connecting to database at $dbUrl...")

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
}
