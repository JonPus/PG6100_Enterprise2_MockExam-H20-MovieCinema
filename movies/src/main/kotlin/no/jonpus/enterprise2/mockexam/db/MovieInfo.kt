package no.jonpus.enterprise2.mockexam.db


import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class MovieInfo(

        @get:Id
        @get:NotBlank
        var movieId: String? = null,

        @get:NotBlank
        var movieTitle: String? = null,

        @get:NotBlank
        var directors: String? = null,

        @get:NotNull
        @get:Min(0)
        var year: Int = 0
)