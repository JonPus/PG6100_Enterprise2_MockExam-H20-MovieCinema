package no.jonpus.enterprise2.mockexam

import io.restassured.RestAssured
import no.jonpus.enterprise2.mockexam.RestAPI.Companion.LATEST
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.annotation.PostConstruct

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [(Application::class)],
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class RestAPITest{

    @LocalServerPort
    protected var port = 0

    @PostConstruct
    fun init(){
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @Test
    fun testGetImg(){

        RestAssured.given().get("/api/rooms/imgs/001-monster.svg")
                .then()
                .statusCode(200)
                .contentType("image/svg+xml")
                .header("cache-control", `is`(notNullValue()))
    }

    @Test
    fun testGetCollection(){

        RestAssured.given().get("/api/rooms/collection_$LATEST")
                .then()
                .statusCode(200)
                .body("data.rooms.size", greaterThan(10))
    }

    @Test
    fun testGetCollectionOldVersion(){

        RestAssured.given().get("/api/rooms/collection_v0_002")
                .then()
                .statusCode(200)
                .body("data.rooms.size", greaterThan(10))
    }


}