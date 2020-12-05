package no.jonpus.enterprise2.mockexam.rooms.dto

import io.swagger.annotations.ApiModelProperty

class RoomDto(

        @get:ApiModelProperty("The id of the ticket")
        var roomId: String? = null,

        @get:ApiModelProperty("The name of the ticket's movie")
        var movieName: String? = null,

        @get:ApiModelProperty("The ticket price")
        var price: Int? = null,

        @get:ApiModelProperty("Seats in the room")
        var seats: Int? = null,

        @get:ApiModelProperty("The rarity of the ticket")
        var roomClass: RoomClass? = null,

        @get:ApiModelProperty("The image associated with the ticket")
        var imageId: String? = null
)