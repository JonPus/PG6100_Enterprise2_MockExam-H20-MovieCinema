package no.jonpus.enterprise2.mockexam.rest.exception

class UserInputValidationException(
        message: String,
        val httpCode : Int = 400
) : RuntimeException(message)