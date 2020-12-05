package no.jonpus.enterprise2.mockexam.db

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.TypedQuery

@Repository
interface MovieInfoRepository : CrudRepository<MovieInfo, String>

@Service
@Transactional
class MoveInfoService(
    val repository: MovieInfoRepository,
    val em: EntityManager
) {
    fun registerNewMovie(movieId: String): Boolean {

        if (repository.existsById(movieId)) {
            return false
        }
        val info = MovieInfo(movieId, "foo", "bar", 2020)
        repository.save(info)
        return true
    }

    fun getNextPage(size: Int, keySetId: String? = null, keySetMovie: Int? = null): List<MovieInfo> {

        if (size < 1 || size > 1000) {
            throw IllegalArgumentException("Invalid size value: $size")
        }

        if ((keySetId == null && keySetMovie != null) || (keySetId != null && keySetMovie == null)) {
            throw IllegalArgumentException("keySetId and keySetMovie should be both missing, or both present")
        }

        val query: TypedQuery<MovieInfo>
        if (keySetId == null) {

            query = em.createQuery(
                "SELECT m FROM MovieInfo m ORDER BY m.year DESC, m.movieId DESC", MovieInfo::class.java
            )
        } else {
            query = em.createQuery(
                "SELECT m FROM MovieInfo m WHERE m.year <? 2 OR (m.year =? 2 AND m.movieId<?1) ORDER BY m.year DESC, m.movieId DESC",
                MovieInfo::class.java
            )
            query.setParameter(1, keySetId)
            query.setParameter(2, keySetMovie)
        }
        query.maxResults = size

        return query.resultList
    }
}