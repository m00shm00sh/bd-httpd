import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.http.decodeURLQueryComponent

import chirp.ChirpService
import refresh.RefreshService
import tokens.JwtService
import user.UserService

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val config = AppConfig.config(environment.config.asPropertyMap())
    val db = Database(config.db)

    val userService = UserService(db)
    val refreshService = RefreshService(db)
    val jwtService = JwtService(config.jwt)
    val chirpService = ChirpService(db)
    doModule(config, userService, refreshService, jwtService, chirpService)

}

internal fun Application.doModule(
    config: AppConfig.App,
    userService: UserService,
    refreshService: RefreshService,
    jwtService: JwtService,
    chirpService: ChirpService,
) {
    configureSerialization()
    configureSecurity(userService, jwtService, config.polka)
    configureRouting(userService, refreshService, jwtService, chirpService, config.platform == "dev")

}