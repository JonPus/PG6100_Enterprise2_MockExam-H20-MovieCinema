package no.jonpus.enterprise2.mockexam.cinema.auth

import javax.validation.constraints.NotBlank

class AuthDto(

    @get:NotBlank
    var userId: String? = null,

    @get:NotBlank
    var password: String? = null
)