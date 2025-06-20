
import chirp.ChirpRequest
import chirp.ChirpResponse
import util.Method
import util.testEndpoint
import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode
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
        testEndpoint<Unit, Unit>(Method.PostJson, "/admin/reset")

        /* static */
        testEndpoint<Unit, String>(Method.Get, "/app/") {
            assertTrue(contains("Welcome to Chirpy"))
        }
        testEndpoint<Unit, Unit>(Method.Get, "/app/assets/logo.png")

        /* misc */
        testEndpoint<Unit, String>(Method.Get, "/admin/metrics") {
            assertTrue(contains("has been visited 2"))
        }
        testEndpoint<Unit, String>(Method.Get, "/api/healthz") {
            assertEquals("OK", this)
        }
    }

    @Test
    fun testWebhooks() = withServer { c ->
        testEndpoint<Unit, Unit>(Method.Post, "/admin/reset")

        lateinit var saulID: UUID
        /* user crud - create */
        testEndpoint<UserRequest, UserResponse>(Method.PostJson, "/api/users",
            reqBody = UserRequest("saul@bettercall.com", "123456"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("saul@bettercall.com", email)
            assertFalse(isRed)
            saulID = id!!
        }

        /* webhooks */
        testEndpoint<PolkaRequest, Unit>(Method.PostJson, "/api/polka/webhooks",
            reqBody = PolkaRequest("user.upgraded", PolkaRequest.PolkaData(saulID)),
            responseCode = HttpStatusCode.Unauthorized
        )
        testEndpoint<PolkaRequest, Unit>(Method.PostJson, "/api/polka/webhooks",
            { header("Authorization", "ApiKey ${c.polka.key}") },
            PolkaRequest("user.upgraded", PolkaRequest.PolkaData(saulID)),
            responseCode = HttpStatusCode.NoContent
        )
        testEndpoint<UserRequest, UserResponseWithToken>(Method.PostJson, "/api/login",
            reqBody = UserRequest("saul@bettercall.com", "123456")
        ) {
            assertTrue(isRed)
        }
    }

    @Test
    fun testRefreshToken() = withServer { c ->
        testEndpoint<Unit, Unit>(Method.Post, "/admin/reset")

        lateinit var saulAccessToken: String
        lateinit var saulRefreshToken: String
        testEndpoint<UserRequest, UserResponse>(Method.PostJson, "/api/users",
            reqBody = UserRequest("saul@bettercall.com", "123456"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("saul@bettercall.com", email)
            assertFalse(isRed)
        }
        testEndpoint<UserRequest, UserResponseWithToken>(Method.PostJson, "/api/login",
            reqBody = UserRequest("saul@bettercall.com", "123456")
        ) {
            assertFalse(isRed)
            saulAccessToken = accessToken
            saulRefreshToken = refreshToken
        }

        /* refresh tokens */
        testEndpoint<ChirpRequest, Unit>(Method.PostJson, "/api/chirps",
            { bearerAuth(saulRefreshToken) },
            ChirpRequest("Let’s just say I know a guy... who knows a guy... who knows another guy."),
            responseCode = HttpStatusCode.Unauthorized
        )
        testEndpoint<ChirpRequest, Unit>(Method.PostJson, "/api/chirps",
            { bearerAuth(saulAccessToken) },
            ChirpRequest("Let’s just say I know a guy... who knows a guy... who knows another guy."),
            responseCode = HttpStatusCode.Created
        )
        lateinit var saulAccess2: String
        testEndpoint<Unit, RefreshResponse>(Method.Post, "/api/refresh",
            { bearerAuth(saulRefreshToken) }
        ) {
            saulAccess2 = token
        }
        testEndpoint<ChirpRequest, Unit>(Method.PostJson, "/api/chirps",
            { bearerAuth(saulAccess2) },
            ChirpRequest("I'm the guy who's gonna win you this case."),
            responseCode = HttpStatusCode.Created
        )
        testEndpoint<Unit, Unit>(Method.Post, "/api/revoke",
            { bearerAuth(saulRefreshToken) },
            responseCode = HttpStatusCode.NoContent
        )
        testEndpoint<Unit, Unit>(Method.Post, "/api/refresh",
            { bearerAuth(saulRefreshToken) },
            responseCode = HttpStatusCode.Unauthorized
        )
    }

    @Test
    fun testModifyUser() = withServer {
        testEndpoint<Unit, Unit>(Method.Post, "/admin/reset")

        /* modify user */
        testEndpoint<UserRequest, UserResponse>(Method.PostJson, "/api/users",
            reqBody = UserRequest("walt@breakingbad.com", "123456"),
            responseCode = HttpStatusCode.Created
        )
        lateinit var waltToken: String
        testEndpoint<UserRequest, UserResponseWithToken>(Method.PostJson, "/api/login",
            reqBody = UserRequest("walt@breakingbad.com", "123456")
        ) {
            waltToken = accessToken
        }
        testEndpoint<UserRequest, UserResponse>(Method.PutJson, "/api/users",
            { bearerAuth(waltToken) },
            UserRequest("walter@breakingbad.com", "losPollosHermanos")
        ) {
            assertEquals("walter@breakingbad.com", email)
        }
        testEndpoint<UserRequest, Unit>(Method.PutJson, "/api/users",
            reqBody = UserRequest("walter@breakingbad.com", "j3ssePinkM@nCantCook"),
            responseCode = HttpStatusCode.Unauthorized
        )
        testEndpoint<UserRequest, Unit>(Method.PutJson, "/api/users",
            { bearerAuth("badToken") },
            UserRequest("walter@breakingbad.com", "j3ssePinkM@nCantCook"),
            responseCode = HttpStatusCode.Unauthorized
        )
    }

    @Test
    fun testValidateChirp() = withServer {
        testEndpoint<Unit, Unit>(Method.Post, "/admin/reset")

        testEndpoint<UserRequest, UserResponse>(Method.PostJson, "/api/users",
            reqBody = UserRequest("saul@bettercall.com", "123456"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("saul@bettercall.com", email)
        }
        lateinit var saulAccessToken: String
        testEndpoint<UserRequest, UserResponseWithToken>(Method.PostJson, "/api/login",
            reqBody = UserRequest("saul@bettercall.com", "123456")
        ) {
            saulAccessToken = accessToken
        }

        testEndpoint<ChirpRequest, Unit>(Method.PostJson, "/api/chirps",
            { bearerAuth(saulAccessToken) },
            ChirpRequest(
                "lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            ),
            responseCode = HttpStatusCode.BadRequest
        )
        testEndpoint<ChirpRequest, ChirpResponse>(Method.PostJson, "/api/chirps",
            { bearerAuth(saulAccessToken) },
            ChirpRequest(
                "I really need a kerfuffle to go to bed sooner, Fornax !"
            ),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("I really need a **** to go to bed sooner, **** !", body)
        }
    }

    @Test
    fun testDeleteChirp() = withServer {
        testEndpoint<Unit, Unit>(Method.Post, "/admin/reset")

        testEndpoint<UserRequest, UserResponse>(Method.PostJson, "/api/users",
            reqBody = UserRequest("saul@bettercall.com", "123456"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("saul@bettercall.com", email)
        }
        lateinit var saulAccessToken: String
        testEndpoint<UserRequest, UserResponseWithToken>(Method.PostJson, "/api/login",
            reqBody = UserRequest("saul@bettercall.com", "123456")
        ) {
            saulAccessToken = accessToken
        }

        testEndpoint<UserRequest, UserResponse>(Method.PostJson, "/api/users",
            reqBody = UserRequest("walt@breakingbad.com", "123456"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("walt@breakingbad.com", email)
        }
        lateinit var waltToken: String
        testEndpoint<UserRequest, UserResponseWithToken>(Method.PostJson, "/api/login",
            reqBody = UserRequest("walt@breakingbad.com", "123456")
        ) {
            waltToken = accessToken
        }

        /* delete chirp */
        lateinit var chirpId: UUID
        testEndpoint<ChirpRequest, ChirpResponse>(Method.PostJson, "/api/chirps",
            { bearerAuth(waltToken) },
            ChirpRequest(
                "I did it for me. I liked it. I was good at it. And I was really... I was alive."
            ),
            responseCode = HttpStatusCode.Created
        ) {
            chirpId = id
        }
        testEndpoint<Unit, Unit>(Method.GetJson, "/api/chirps/${chirpId}")
        testEndpoint<Unit, Unit>(Method.Delete, "/api/chirps/${chirpId}",
            responseCode = HttpStatusCode.Unauthorized
        )
        testEndpoint<Unit, Unit>(Method.Delete, "/api/chirps/${chirpId}",
            { bearerAuth(saulAccessToken) },
            responseCode = HttpStatusCode.Forbidden
        )
        testEndpoint<Unit, Unit>(Method.Delete, "/api/chirps/${chirpId}",
            { bearerAuth(waltToken) },
            responseCode = HttpStatusCode.NoContent
        )
        testEndpoint<Unit, Unit>(Method.GetJson, "/api/chirps/${chirpId}",
            responseCode = HttpStatusCode.NotFound
        )
    }

    @Test
    fun testGetChirps1() = withServer { c ->
        testEndpoint<Unit, Unit>(Method.Post, "/admin/reset")

        testEndpoint<UserRequest, UserResponse>(Method.PostJson, "/api/users",
            reqBody = UserRequest("walt@breakingbad.com", "123456"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("walt@breakingbad.com", email)
        }

        lateinit var waltToken: String
        lateinit var waltId: UUID
        testEndpoint<UserRequest, UserResponseWithToken>(Method.PostJson, "/api/login",
            reqBody = UserRequest("walt@breakingbad.com", "123456")
        ) {
            waltToken = accessToken
            waltId = id
        }

        testEndpoint<ChirpRequest, ChirpResponse>(Method.PostJson, "/api/chirps",
            { bearerAuth(waltToken) }, ChirpRequest("I'm the one who knocks!"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("I'm the one who knocks!", body)
        }
        testEndpoint<ChirpRequest, ChirpResponse>(Method.PostJson, "/api/chirps",
            { bearerAuth(waltToken) }, ChirpRequest("Gale!"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("Gale!", body)
        }
        lateinit var skylerId: UUID
        testEndpoint<UserRequest, UserResponse>(Method.PostJson, "/api/users",
            reqBody = UserRequest("skyler@breakingbad.com", "000111"),
            responseCode = HttpStatusCode.Created
        ) {
            skylerId = id!!
        }
        lateinit var skylerAccess: String

        testEndpoint<UserRequest, UserResponseWithToken>(Method.PostJson, "/api/login",
            reqBody = UserRequest("skyler@breakingbad.com", "000111"),
        ) {
            skylerAccess = accessToken
        }

        testEndpoint<ChirpRequest, ChirpResponse>(Method.PostJson, "/api/chirps",
            { bearerAuth(skylerAccess) }, ChirpRequest("Mr President...."),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("Mr President....", body)
        }
        testEndpoint<Unit, List<ChirpResponse>>(Method.GetJson, "/api/chirps?author_id=${waltId}") {
            val bodies = map { it.body }
            assertTrue("I'm the one who knocks!" in bodies)
            assertTrue("Gale!" in bodies)
            assertTrue("Mr President...." !in bodies)
        }
        testEndpoint<Unit, List<ChirpResponse>>(Method.GetJson, "/api/chirps?author_id=${skylerId}") {
            val bodies = map { it.body }
            assertTrue("Mr President...." in bodies)
            assertTrue("I'm the one who knocks!" !in bodies)
            assertTrue("Gale!" !in bodies)
        }
    }

    @Test
    fun testGetChirps2() = withServer {
        testEndpoint<Unit, Unit>(Method.Post, "/admin/reset")

        testEndpoint<UserRequest, UserResponse>(Method.PostJson, "/api/users",
            reqBody = UserRequest("walt@breakingbad.com", "123456"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("walt@breakingbad.com", email)
        }

        lateinit var waltToken: String
        testEndpoint<UserRequest, UserResponseWithToken>(Method.PostJson, "/api/login",
            reqBody = UserRequest("walt@breakingbad.com", "123456")
        ) {
            waltToken = accessToken
        }

        testEndpoint<ChirpRequest, ChirpResponse>(Method.PostJson, "/api/chirps",
            { bearerAuth(waltToken) }, ChirpRequest("I'm the one who knocks!"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("I'm the one who knocks!", body)
        }
        testEndpoint<ChirpRequest, ChirpResponse>(Method.PostJson, "/api/chirps",
            { bearerAuth(waltToken) }, ChirpRequest("Gale!"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("Gale!", body)
        }
        testEndpoint<ChirpRequest, ChirpResponse>(Method.PostJson, "/api/chirps",
            { bearerAuth(waltToken) }, ChirpRequest("Cmon Pinkman"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("Cmon Pinkman", body)
        }
        testEndpoint<ChirpRequest, ChirpResponse>(Method.PostJson, "/api/chirps",
            { bearerAuth(waltToken) }, ChirpRequest("Darn that fly, I just wanna cook"),
            responseCode = HttpStatusCode.Created
        ) {
            assertEquals("Darn that fly, I just wanna cook", body)
        }

        testEndpoint<Unit, List<ChirpResponse>>(Method.GetJson, "/api/chirps?sort=desc") {
            val bodies = map { it.body }
            assertEquals(
                listOf(
                    "Darn that fly, I just wanna cook",
                    "Cmon Pinkman",
                    "Gale!",
                    "I'm the one who knocks!"
                ), bodies
            )
        }
        testEndpoint<Unit, List<ChirpResponse>>(Method.GetJson, "/api/chirps?sort=asc") {
            val bodies = map { it.body }
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
