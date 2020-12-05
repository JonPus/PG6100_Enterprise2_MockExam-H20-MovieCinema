package no.jonpus.enterprise2.mockexam.usercollections.dto

import io.swagger.annotations.ApiModelProperty

data class UserDto(

        @get:ApiModelProperty("The id of the user")
        var userId: String? = null,

        @get:ApiModelProperty("The amount of coins owned by the user")
        var coins: Int? = null,

        @get:ApiModelProperty("The number of un-opened room packs the user owns")
        var roomPacks: Int? = null,

        @get:ApiModelProperty("List of rooms owned by the user")
        var ownedRooms: MutableList<RoomCopyDto> = mutableListOf()
)