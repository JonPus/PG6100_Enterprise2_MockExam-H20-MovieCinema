package no.jonpus.enterprise2.mockexam.dto

import io.swagger.annotations.ApiModelProperty

data class MovieInfoDto(

    @get:ApiModelProperty("The of the movie")
    var movieId: String? = null,

    @get:ApiModelProperty("The title of the movie")
    var movieTitle: String? = null,

    @get:ApiModelProperty("The director of the movie")
    var directors: String? = null,

    @get:ApiModelProperty("The year the movie was made")
    var year: Int? = null
)



