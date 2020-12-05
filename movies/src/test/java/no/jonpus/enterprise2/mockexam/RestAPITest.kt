package no.jonpus.enterprise2.mockexam

import io.restassured.RestAssured
import io.restassured.http.ContentType
import no.jonpus.enterprise2.mockexam.db.MovieInfoRepository
import no.jonpus.enterprise2.mockexam.rest.dto.PageDto
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.annotation.PostConstruct

@ActiveProfiles("FakeData,test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [(Application::class)],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
internal class RestAPITest {

    @LocalServerPort
    protected var port = 0

    @Autowired
    private lateinit var repository: MovieInfoRepository

    @PostConstruct
    fun init() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    val page: Int = 10

    @Test
    fun testGetPage() {

        RestAssured.given().accept(ContentType.JSON)
            .get("/api/movies")
            .then()
            .statusCode(200)
            .body("data.list.size()", CoreMatchers.equalTo(page))
    }

    @Test
    fun testAllPages() {

        val read = mutableSetOf<String>()

        var page = RestAssured.given().accept(ContentType.JSON)
            .get("/api/movies")
            .then()
            .statusCode(200)
            .body("data.list.size()", CoreMatchers.equalTo(page))
            .extract().body().jsonPath()
            .getObject("data", object : io.restassured.common.mapper.TypeRef<PageDto<Map<String, Object>>>() {})
        read.addAll(page.list.map { it["movieId"].toString() })

        checkOrder(page)

        while (page.next != null) {

            page = RestAssured.given().accept(ContentType.JSON)
                .get(page.next)
                .then()
                .statusCode(200)
                .extract().body().jsonPath()
                .getObject("data", object : io.restassured.common.mapper.TypeRef<PageDto<Map<String, Object>>>() {})
            read.addAll(page.list.map { it["movieId"].toString() })
            checkOrder(page)
        }

        val total = repository.count().toInt()

        assertEquals(total, read.size)
    }

    private fun checkOrder(page: PageDto<Map<String, Object>>) {
        for (i in 0 until page.list.size - 1) {
            val ascore = page.list[i]["year"].toString().toInt()
            val bscore = page.list[i + 1]["year"].toString().toInt()
            val aid = page.list[i]["movieId"].toString()
            val bid = page.list[i + 1]["movieId"].toString()
            assertTrue(ascore >= bscore)
            if (ascore == bscore) {
                assertTrue(aid > bid)
            }
        }
    }

}