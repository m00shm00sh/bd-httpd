
import chirp.ChirpRequest
import chirp.ChirpResponse
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import refresh.RefreshResponse
import user.UserRequest
import user.UserResponse
import user.UserResponseWithToken
import util.withServer
import webhook.PolkaRequest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testStaticAndMisc() = withServer {
        post("/admin/reset").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        /* static */
        get("/app/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(bodyAsText().contains("Welcome to Chirpy"))

        }
        get("/app/assets/logo.png").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        /* misc */
        get("/admin/metrics").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertTrue(bodyAsText().contains("has been visited 2"))
        }
        get("/api/healthz").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("OK", bodyAsText())
        }
    }

    @Test
    fun testWebhooks() = withServer { c ->
        post("/admin/reset").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        lateinit var saulID: UUID
        /* user crud - create */
        post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(
                UserRequest("saul@bettercall.com", "123456")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<UserResponse>()
            assertEquals("saul@bettercall.com", response.email)
            assertFalse(response.isRed)
            saulID = response.id!!
        }

        /* webhooks */
        post("/api/polka/webhooks") {
            contentType(ContentType.Application.Json)
            setBody(
                PolkaRequest("user.upgraded", PolkaRequest.PolkaData(saulID))
            )
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
        post("/api/polka/webhooks") {
            contentType(ContentType.Application.Json)
            header("Authorization", "ApiKey ${c.polka.key}")
            setBody(
                PolkaRequest("user.upgraded", PolkaRequest.PolkaData(saulID))
            )
        }.apply {
            assertEquals(HttpStatusCode.NoContent, status)
        }
        post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(
                UserRequest("saul@bettercall.com", "123456")
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<UserResponseWithToken>()
            assertTrue(response.isRed)
        }
    }

    @Test
    fun testRefreshToken() = withServer { c ->
        lateinit var saulAccessToken: String
        lateinit var saulRefreshToken: String
        post("/admin/reset").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(
                UserRequest("saul@bettercall.com", "123456")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<UserResponse>()
            assertEquals("saul@bettercall.com", response.email)
            assertFalse(response.isRed)
        }
        post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(
                UserRequest("saul@bettercall.com", "123456")
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<UserResponseWithToken>()
            assertFalse(response.isRed)
            saulAccessToken = response.accessToken
            saulRefreshToken = response.refreshToken
        }


        /* refresh tokens */
        post("/api/chirps") {
            contentType(ContentType.Application.Json)
            bearerAuth(saulRefreshToken)
            setBody(
                ChirpRequest("Let’s just say I know a guy... who knows a guy... who knows another guy.")
            )
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
        post("/api/chirps") {
            contentType(ContentType.Application.Json)
            bearerAuth(saulAccessToken)
            setBody(
                ChirpRequest("Let’s just say I know a guy... who knows a guy... who knows another guy.")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
        lateinit var saulAccess2: String
        post("/api/refresh") {
            bearerAuth(saulRefreshToken)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<RefreshResponse>()
            saulAccess2 = response.token
        }
        post("/api/chirps") {
            contentType(ContentType.Application.Json)
            bearerAuth(saulAccess2)
            setBody(
                ChirpRequest("I'm the guy who's gonna win you this case.")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
        post("/api/revoke") {
            bearerAuth(saulRefreshToken)
        }.apply {
            assertEquals(HttpStatusCode.NoContent, status)
        }
        post("/api/refresh") {
            bearerAuth(saulRefreshToken)
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testModifyUser() = withServer {
        post("/admin/reset").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(
                UserRequest("saul@bettercall.com", "123456")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<UserResponse>()
            assertEquals("saul@bettercall.com", response.email)
            assertFalse(response.isRed)
        }
        post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(
                UserRequest("saul@bettercall.com", "123456")
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        /* modify user */
        post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("walt@breakingbad.com", "123456"))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<UserResponse>()
        }
        lateinit var waltToken: String
        post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("walt@breakingbad.com", "123456"))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<UserResponseWithToken>()
            waltToken = response.accessToken
        }
        put("/api/users") {
            contentType(ContentType.Application.Json)
            bearerAuth(waltToken)
            setBody(UserRequest("walter@breakingbad.com", "losPollosHermanos"))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<UserResponse>()
            assertEquals("walter@breakingbad.com", response.email)
        }
        put("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("walter@breakingbad.com", "j3ssePinkM@nCantCook"))
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
        put("/api/users") {
            contentType(ContentType.Application.Json)
            bearerAuth("badToken")
            setBody(UserRequest("walter@breakingbad.com", "j3ssePinkM@nCantCook"))
        }.apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testDeleteChirp() = withServer {
        post("/admin/reset").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(
                UserRequest("saul@bettercall.com", "123456")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<UserResponse>()
            assertEquals("saul@bettercall.com", response.email)
            assertFalse(response.isRed)
        }
        lateinit var saulAccessToken: String
        post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(
                UserRequest("saul@bettercall.com", "123456")
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<UserResponseWithToken>()
            saulAccessToken = response.accessToken
        }
        post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("walt@breakingbad.com", "123456"))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
        lateinit var waltToken: String
        post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("walt@breakingbad.com", "123456"))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<UserResponseWithToken>()
            waltToken = response.accessToken
        }

        /* delete chirp */
        lateinit var chirpId: UUID
        post("/api/chirps") {
            contentType(ContentType.Application.Json)
            bearerAuth(waltToken)
            setBody(
                ChirpRequest("I did it for me. I liked it. I was good at it. And I was really... I was alive.")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<ChirpResponse>()
            chirpId = response.id
        }
        get("/api/chirps/${chirpId}").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        delete("/api/chirps/${chirpId}").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }

        delete("/api/chirps/${chirpId}") {
            bearerAuth(saulAccessToken)
        }.apply {
            assertEquals(HttpStatusCode.Forbidden, status)
        }
        delete("/api/chirps/${chirpId}") {
            bearerAuth(waltToken)
        }.apply {
            assertEquals(HttpStatusCode.NoContent, status)
        }
        get("/api/chirps/${chirpId}").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun testGetChrips1() = withServer { c ->
        post("/admin/reset").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(
                UserRequest("saul@bettercall.com", "123456")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
        post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(
                UserRequest("saul@bettercall.com", "123456")
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<UserResponseWithToken>()
        }
        post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("walt@breakingbad.com", "123456"))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
        lateinit var waltToken: String
        lateinit var waltId: UUID
        post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("walt@breakingbad.com", "123456"))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<UserResponseWithToken>()
            waltToken = response.accessToken
            waltId = response.id
        }

        post("/api/chirps") {
            contentType(ContentType.Application.Json)
            bearerAuth(waltToken)
            setBody(
                ChirpRequest("I'm the one who knocks!")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<ChirpResponse>()
            assertEquals("I'm the one who knocks!", response.body)
        }
        post("/api/chirps") {
            contentType(ContentType.Application.Json)
            bearerAuth(waltToken)
            setBody(
                ChirpRequest("Gale!")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<ChirpResponse>()
            assertEquals("Gale!", response.body)
        }
        lateinit var skylerId: UUID
        post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("skyler@breakingbad.com", "000111"))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<UserResponse>()
            skylerId = response.id!!
        }
        lateinit var skylerAccess: String
        post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("skyler@breakingbad.com", "000111"))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<UserResponseWithToken>()
            skylerAccess = response.accessToken
        }
        post("/api/chirps") {
            contentType(ContentType.Application.Json)
            bearerAuth(skylerAccess)
            setBody(
                ChirpRequest("Mr President....")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<ChirpResponse>()
            assertEquals("Mr President....", response.body)
        }
        get("/api/chirps?author_id=${waltId}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val bodies = body<List<ChirpResponse>>().map { it.body }
            assertTrue("I'm the one who knocks!" in bodies)
            assertTrue("Gale!" in bodies)
            assertTrue("Mr President...." !in bodies)
        }
        get("/api/chirps?author_id=${skylerId}").apply {
            assertEquals(HttpStatusCode.OK, status)
            val bodies = body<List<ChirpResponse>>().map { it.body }
            assertTrue("Mr President...." in bodies)
            assertTrue("I'm the one who knocks!" !in bodies)
            assertTrue("Gale!" !in bodies)
        }
    }

    @Test
    fun testGetChirps2() = withServer {
        post("/admin/reset").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(
                UserRequest("saul@bettercall.com", "123456")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
        lateinit var saulAccessToken: String
        post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(
                UserRequest("saul@bettercall.com", "123456")
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<UserResponseWithToken>()
            saulAccessToken = response.accessToken
        }
        post("/api/users") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("walt@breakingbad.com", "123456"))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
        lateinit var waltToken: String
        lateinit var waltId: UUID
        post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(UserRequest("walt@breakingbad.com", "123456"))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = body<UserResponseWithToken>()
            waltToken = response.accessToken
            waltId = response.id
        }

        post("/api/chirps") {
            contentType(ContentType.Application.Json)
            bearerAuth(waltToken)
            setBody(
                ChirpRequest("I'm the one who knocks!")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<ChirpResponse>()
            assertEquals("I'm the one who knocks!", response.body)
        }
        post("/api/chirps") {
            contentType(ContentType.Application.Json)
            bearerAuth(waltToken)
            setBody(
                ChirpRequest("Gale!")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<ChirpResponse>()
            assertEquals("Gale!", response.body)
        }
        post("/api/chirps") {
            contentType(ContentType.Application.Json)
            bearerAuth(waltToken)
            setBody(
                ChirpRequest("Cmon Pinkman")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<ChirpResponse>()
            assertEquals("Cmon Pinkman", response.body)
        }
        post("/api/chirps") {
            contentType(ContentType.Application.Json)
            bearerAuth(waltToken)
            setBody(
                ChirpRequest("Darn that fly, I just wanna cook")
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val response = body<ChirpResponse>()
            assertEquals("Darn that fly, I just wanna cook", response.body)
        }
        get("/api/chirps?sort=desc").apply {
            assertEquals(HttpStatusCode.OK, status)
            val bodies = body<List<ChirpResponse>>().map { it.body }
            assertEquals(
                listOf(
                    "Darn that fly, I just wanna cook",
                    "Cmon Pinkman",
                    "Gale!",
                    "I'm the one who knocks!"
                ), bodies
            )
        }
        get("/api/chirps?sort=asc").apply {
            assertEquals(HttpStatusCode.OK, status)
            val bodies = body<List<ChirpResponse>>().map { it.body }
            assertEquals(
                listOf(
                    "I'm the one who knocks!",
                    "Gale!",
                    "Cmon Pinkman",
                    "Darn that fly, I just wanna cook"
                ), bodies
            )
        }


    }

}
