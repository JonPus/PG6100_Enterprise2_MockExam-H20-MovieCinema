package no.jonpus.enterprise2.mockexam

import no.jonpus.enterprise2.mockexam.db.MovieInfo
import no.jonpus.enterprise2.mockexam.dto.MovieInfoDto

object DtoConverter {

    fun transform(info: MovieInfo): MovieInfoDto =
        info.run { MovieInfoDto(movieId, movieTitle, directors, year) }

    fun transform(movies: Iterable<MovieInfo>): List<MovieInfoDto> = movies.map { transform(it) }
}