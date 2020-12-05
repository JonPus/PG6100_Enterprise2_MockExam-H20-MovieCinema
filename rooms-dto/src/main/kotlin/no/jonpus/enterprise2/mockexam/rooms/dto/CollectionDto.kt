package no.jonpus.enterprise2.mockexam.rooms.dto

import io.swagger.annotations.ApiModelProperty

class CollectionDto(

        @get:ApiModelProperty("Info on all the tickets in the cinema")
        var rooms: MutableList<RoomDto> = mutableListOf(),

        @get:ApiModelProperty("Cost of each ticket, based on its Ticket class")
        var prices: MutableMap<RoomClass, Int> = mutableMapOf(),

        @get:ApiModelProperty("Milling/sell value of each ticket, based on its ticket class")
        var millValue: MutableMap<RoomClass, Int> = mutableMapOf(),

        //To be changed
        @get:ApiModelProperty("Probabilities of getting a ticket of a specific ticket class when opening ticket pack")
        var roomClassProbabilities: MutableMap<RoomClass, Double> = mutableMapOf()


)