import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

import chirp.ChirpService
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import refresh.RefreshService
import tokens.JwtService
import user.UserService

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val config = AppConfig.config(environment.config.asPropertyMap())
    val db = Database(config.db)

    doModule(db, config)

}

internal fun Application.doModule(db: Database, config: AppConfig.App) {
    val module = module {
        single { UserService(db) }
        single { RefreshService(db) }
        single { JwtService(config.jwt) }
        single { ChirpService(db) }
    }
    install(Koin) {
        slf4jLogger()
        modules(module)
    }
    configureSerialization()
    configureSecurity(config.polka)
    configureRouting(config.platform == "dev")

}