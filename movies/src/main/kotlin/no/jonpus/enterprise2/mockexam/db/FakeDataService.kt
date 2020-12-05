package no.jonpus.enterprise2.mockexam.db

import com.github.javafaker.Faker
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct
import kotlin.random.Random


@Profile("FakeData")
@Service
@Transactional
class FakeDataService(
    val repository: MovieInfoRepository
) {

    private val faker = Faker()

    @PostConstruct
    fun init() {
        for (i in 0..49) {
            createRandomMovie("Foo" + i.toString().padStart(2, '0'))
        }
    }

    fun createRandomMovie(movieId: String) {
        val info = MovieInfo(
            movieId,
            faker.funnyName().toString(),
            faker.funnyName().toString(),
            Random.nextInt(50)
        )
        repository.save(info)
    }

}