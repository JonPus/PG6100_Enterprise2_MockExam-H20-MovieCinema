package no.jonpus.enterprise2.mockexam

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.jonpus.enterprise2.mockexam.db.MoveInfoService
import no.jonpus.enterprise2.mockexam.db.MovieInfoRepository
import no.jonpus.enterprise2.mockexam.dto.MovieInfoDto
import no.jonpus.enterprise2.mockexam.rest.dto.PageDto
import no.jonpus.enterprise2.mockexam.rest.dto.RestResponseFactory
import no.jonpus.enterprise2.mockexam.rest.dto.WrappedResponse
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit

@Api(value = "/api/movies", description = "Movies and years of based on release of the movie in cinema")
@RequestMapping(path = ["/api/movies"], produces = [(MediaType.APPLICATION_JSON_VALUE)])
@RestController
class RestAPI(
    private val infoRepository: MovieInfoRepository, private val infoService: MoveInfoService
) {

    @ApiOperation("Retrieve the current movie release based on its year")
    @GetMapping(path = ["/{movieId}"])
    fun getMovieInfo(@PathVariable("movieId") movieId: String): ResponseEntity<WrappedResponse<MovieInfoDto>> {

        val movie = infoRepository.findById(movieId).orElse(null)
        if (movie == null) {
            return RestResponseFactory.notFound("Movie $movieId not found.")
        }

        return RestResponseFactory.payload(200, DtoConverter.transform(movie))
    }

    @ApiOperation("Create default info for a new movie")
    @PutMapping(path = ["/{movieId}"])
    fun createMovie(@PathVariable("movieId") movieId: String): ResponseEntity<WrappedResponse<Void>> {

        val ok = infoService.registerNewMovie(movieId)
        return if (!ok) RestResponseFactory.userFailure("Movie $movieId already exist")
        else RestResponseFactory.noPayload(201)
    }

    @ApiOperation("Return an iteraable page of movie list of movies, starting from earliest year")
    @GetMapping
    fun getAll(
        @ApiParam("Id of movie in the previous page")
        @RequestParam("keySetId", required = false)
        keySetId: String?,
        //
        @ApiParam("Year of the movie in the previous page")
        @RequestParam("keySetMovie", required = false)
        keySetMovie: Int?
    ): ResponseEntity<WrappedResponse<PageDto<MovieInfoDto>>> {

        val page = PageDto<MovieInfoDto>()

        val n = 10
        val movies = DtoConverter.transform(infoService.getNextPage(n, keySetId, keySetMovie))
        page.list = movies

        if (movies.size == n) {
            val last = movies.last()
            page.next = "/api/movies?keySetId=${last.movieId}&keySetMovie=${last.year}"
        }

        return ResponseEntity
            .status(200)
            .cacheControl(CacheControl.maxAge(1, TimeUnit.MINUTES).cachePublic())
            .body(WrappedResponse(200, page).validated())
    }
}