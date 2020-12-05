package no.jonpus.enterprise2.mockexam.usercollections

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import no.jonpus.enterprise2.mockexam.rest.dto.WrappedResponse
import no.jonpus.enterprise2.mockexam.usercollections.db.UserRepository
import no.jonpus.enterprise2.mockexam.usercollections.db.UserService
import no.jonpus.enterprise2.mockexam.usercollections.dto.Command
import no.jonpus.enterprise2.mockexam.usercollections.dto.PatchUserDto
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.annotation.PostConstruct


@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [(RestAPITest.Companion.Initializer::class)])
internal class RestAPITest {

    @LocalServerPort
    protected var port = 0

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    companion object {

        private lateinit var wiremockServer: WireMockServer

        @BeforeAll
        @JvmStatic
        fun initClass() {
            wiremockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort().notifier(ConsoleNotifier(true)))
            wiremockServer.start()

            val dto = WrappedResponse(code = 200, data = FakeData.getCollectionDto()).validated()
            val json = ObjectMapper().writeValueAsString(dto)

            wiremockServer.stubFor(
                    WireMock.get(WireMock.urlMatching("/api/rooms/collection_.*"))
                            .willReturn(WireMock.aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json; charset=utf-8")
                                    .withBody(json)))
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            wiremockServer.stop()
        }

        class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
            override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
                TestPropertyValues.of("roomServiceAddress: localhost:${wiremockServer.port()}")
                        .applyTo(configurableApplicationContext.environment)
            }
        }
    }

    @PostConstruct
    fun init() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.basePath = "/api/user-collections"
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @BeforeEach
    fun initTest() {
        userRepository.deleteAll()
    }

    @Test
    fun testAccessControl() {

        val id = "foo"

        given().get("/$id").then().statusCode(401)
        given().put("/$id").then().statusCode(401)
        given().patch("/$id").then().statusCode(401)

        given().auth().basic("bar", "123")
                .get("/$id")
                .then()
                .statusCode(403)
    }

    @Test
    fun testGetUser() {

        val id = "foo"
        userService.registerNewUser(id)

        given().auth().basic(id, "123")
                .get("/$id")
                .then()
                .statusCode(200)
    }

    @Test
    fun testCreateUser() {
        val id = "foo"

        given().auth().basic(id, "123")
                .put("/$id")
                .then()
                .statusCode(201)

        assertTrue(userRepository.existsById(id))
    }

    @Test
    fun testBuyCard() {

        val userId = "foo"
        val roomId = "c00"

        given().auth().basic(userId, "123").put("/$userId").then().statusCode(201)

        given().auth().basic(userId, "123")
                .contentType(ContentType.JSON)
                .body(PatchUserDto(Command.BUY_ROOM, roomId))
                .patch("/$userId")
                .then()
                .statusCode(200)

        val user = userService.findByIdEager(userId)!!
        assertTrue(user.ownedRooms.any { it.roomId == roomId })
    }

    @Test
    fun testOpenPack() {

        val userId = "foo"
        given().auth().basic(userId, "123").put("/$userId").then().statusCode(201)

        val before = userService.findByIdEager(userId)!!
        val totRooms = before.ownedRooms.sumBy { it.numberOfCopies }
        val totPacks = before.roomPacks
        assertTrue(totPacks > 0)

        given().auth().basic(userId, "123")
                .contentType(ContentType.JSON)
                .body(PatchUserDto(Command.OPEN_PACK))
                .patch("/$userId")
                .then()
                .statusCode(200)

        val after = userService.findByIdEager(userId)!!
        assertEquals(totPacks - 1, after.roomPacks)
        assertEquals(totRooms + UserService.ROOMS_PER_PACK,
                after.ownedRooms.sumBy { it.numberOfCopies })
    }

    @Test
    fun testMillRoom() {

        val userId = "foo"
        given().auth().basic(userId, "123").put("/$userId").then().statusCode(201)

        val before = userRepository.findById(userId).get()
        val coins = before.coins

        given().auth().basic(userId, "123")
                .contentType(ContentType.JSON)
                .body(PatchUserDto(Command.OPEN_PACK))
                .patch("/$userId")
                .then()
                .statusCode(200)

        val between = userService.findByIdEager(userId)!!
        val n = between.ownedRooms.sumBy { it.numberOfCopies }

        val roomId = between.ownedRooms[0].roomId!!
        given().auth().basic(userId, "123")
                .contentType(ContentType.JSON)
                .body(PatchUserDto(Command.MILL_ROOM, roomId))
                .patch("/$userId")
                .then()
                .statusCode(200)

        val after = userService.findByIdEager(userId)!!
        assertTrue(after.coins > coins)
        assertEquals(n - 1, after.ownedRooms.sumBy { it.numberOfCopies })
    }


}