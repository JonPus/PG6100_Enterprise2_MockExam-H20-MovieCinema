package no.jonpus.enterprise2.mockexam.cinema.e2etests

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.awaitility.Awaitility
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

@Disabled
@Testcontainers
class RestIT {

    companion object {

        init {
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
            RestAssured.port = 80
        }

        class KDockerComposeContainer(id: String, path: File) :
            DockerComposeContainer<KDockerComposeContainer>(id, path)

        @Container
        @JvmField
        val env = KDockerComposeContainer("movie-cinema", File("../docker-compose.yml"))
            .withExposedService(
                "discovery", 8500,
                Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(240))
            )
            .withLogConsumer("rooms_0") { print("[ROOM_0] " + it.utf8String) }
            .withLogConsumer("rooms_1") { print("[ROOM_1] " + it.utf8String) }
            .withLogConsumer("user-collections") { print("[USER_COLLECTION] " + it.utf8String) }
            .withLogConsumer("movies") { print("[MOVIES] " + it.utf8String) }
            .withLocalCompose(true)


        @BeforeAll
        @JvmStatic
        fun waitForServers() {
            Awaitility.await().atMost(240, TimeUnit.SECONDS)
                .pollDelay(Duration.ofSeconds(20))
                .pollInterval(Duration.ofSeconds(10))
                .ignoreExceptions()
                .until {

                    RestAssured.given().baseUri("http://${env.getServiceHost("discovery", 8500)}")
                        .port(env.getServicePort("discovery", 8500))
                        .get("/v1/agent/services")
                        .then()
                        .body("size()", CoreMatchers.equalTo(5))

                    true
                }
        }
    }

    @Test
    fun testGetCollection() {

        Awaitility.await().atMost(120, TimeUnit.SECONDS)
            .pollInterval(Duration.ofSeconds(10))
            .ignoreExceptions()
            .until {
                given().get("/api/rooms/collection_v1_000")
                    .then()
                    .statusCode(200)
                    .body("data.rooms.size", greaterThan(10))
                true
            }
    }

    @Test
    fun testGetMovies() {
        Awaitility.await().atMost(120, TimeUnit.SECONDS)
            .pollInterval(Duration.ofSeconds(10))
            .ignoreExceptions()
            .until {
                given().accept(ContentType.JSON)
                    .get("/api/movies")
                    .then()
                    .statusCode(200)
                    .body("data.list.size()", greaterThan(0))
                true
            }
    }

    @Test
    fun testCreateUser() {
        Awaitility.await().atMost(120, TimeUnit.SECONDS)
            .pollInterval(Duration.ofSeconds(10))
            .ignoreExceptions()
            .until {

                val id = "foo_testCreateUser_" + System.currentTimeMillis()

                given().get("/api/user-collections/$id")
                    .then()
                    .statusCode(401)

                val password = "123456"

                val cookie = given().contentType(ContentType.JSON)
                    .body("""
                                {
                                    "userId": "$id",
                                    "password": "$password"
                                }
                            """.trimIndent())
                    .post("/api/auth/signUp")
                    .then()
                    .statusCode(201)
                    .header("Set-Cookie", CoreMatchers.not(CoreMatchers.equalTo(null)))
                    .extract().cookie("SESSION")

                given().cookie("SESSION", cookie)
                    .put("/api/user-collections/$id")

                given().cookie("SESSION", cookie)
                    .get("/api/user-collections/$id")
                    .then()
                    .statusCode(200)

                true
            }
    }

    @Test
    fun testUserCollectionAccessControl() {

        val alice = "alice_testUserCollectionAccessControl_" + System.currentTimeMillis()
        val eve =   "eve_testUserCollectionAccessControl_" + System.currentTimeMillis()

        given().get("/api/user-collections/$alice").then().statusCode(401)
        given().put("/api/user-collections/$alice").then().statusCode(401)
        given().patch("/api/user-collections/$alice").then().statusCode(401)

        val cookie = given().contentType(ContentType.JSON)
            .body("""
                                {
                                    "userId": "$eve",
                                    "password": "123456"
                                }
                            """.trimIndent())
            .post("/api/auth/signUp")
            .then()
            .statusCode(201)
            .header("Set-Cookie", CoreMatchers.not(CoreMatchers.equalTo(null)))
            .extract().cookie("SESSION")



        given().cookie("SESSION", cookie)
            .get("/api/user-collections/$alice")
            .then()
            .statusCode(403)
    }

    @Test
    fun testAMQPSignUp() {
        Awaitility.await().atMost(120, TimeUnit.SECONDS)
            .pollInterval(Duration.ofSeconds(10))
            .ignoreExceptions()
            .until {

                val id = "foo_testCreateUser_" + System.currentTimeMillis()

                given().get("/api/auth/user")
                    .then()
                    .statusCode(401)

                given().put("/api/user-collections/$id")
                    .then()
                    .statusCode(401)

                /*given().get("/api/movies/$id")
                    .then()
                    .statusCode(404)*/

                val password = "123456"

                val cookie = given().contentType(ContentType.JSON)
                    .body("""
                                {
                                    "userId": "$id",
                                    "password": "$password"
                                }
                            """.trimIndent())
                    .post("/api/auth/signUp")
                    .then()
                    .statusCode(201)
                    .header("Set-Cookie", CoreMatchers.not(CoreMatchers.equalTo(null)))
                    .extract().cookie("SESSION")

                given().cookie("SESSION", cookie)
                    .get("/api/auth/user")
                    .then()
                    .statusCode(200)

                Awaitility.await().atMost(10, TimeUnit.SECONDS)
                    .pollInterval(Duration.ofSeconds(2))
                    .ignoreExceptions()
                    .until {
                        given().cookie("SESSION", cookie)
                            .get("/api/user-collections/$id")
                            .then()
                            .statusCode(200)

                        /*given().get("/api/movies/$id")
                            .then()
                            .statusCode(200)
                            .body("data.movie", CoreMatchers.equalTo(0))*/

                        true
                    }

                true
            }
    }


}