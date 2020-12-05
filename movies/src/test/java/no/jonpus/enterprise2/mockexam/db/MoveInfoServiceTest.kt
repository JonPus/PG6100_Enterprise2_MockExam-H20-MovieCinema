package no.jonpus.enterprise2.mockexam.db

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles("FakeData,test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
internal class MoveInfoServiceTest {

    @Autowired
    private lateinit var service: MoveInfoService

    @Autowired
    private lateinit var repository: MovieInfoRepository

    @Test
    fun testInit() {
        assertTrue(repository.count() > 0)
    }

    @Test
    fun testCreateMovie() {
        val n = repository.count()

        service.registerNewMovie("Andrea and his friends")
        assertEquals(n + 1, repository.count())
    }

    @Test
    fun testPage(){

        val n = 5
        val page = service.getNextPage(n)

        for (i in 0 until n - 1){
            assertTrue(page[i].year >= page[i + 1].year)
        }
    }
}