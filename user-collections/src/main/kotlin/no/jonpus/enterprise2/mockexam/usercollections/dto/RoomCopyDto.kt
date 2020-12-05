package no.jonpus.enterprise2.mockexam.usercollections.dto

import io.swagger.annotations.ApiModelProperty

data class RoomCopyDto(

        @get:ApiModelProperty("Id of the room")
        var roomId: String? = null,

        @get:ApiModelProperty("Number of copies of the room that the user owns")
        var numberOfCopies: Int? = null
)