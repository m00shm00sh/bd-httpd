package util

import Database
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import java.nio.file.Files

internal fun getDatabase(): Database {
    val tmpDir = Files.createTempDirectory("chirpkt").toString()
    val url = "${tmpDir}/dctest.db"
    val flyway = Flyway.configure().run {
        dataSource(
            "jdbc:sqlite:$url", null, null
        )
        load()
    }
    flyway.migrate()
    val db = Database(HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:$url"
        addDataSourceProperty("journal_mode", "wal")
        addDataSourceProperty("foreign_keys", "on")
    }))
    return db
}