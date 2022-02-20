package no.jonpus.enterprise2.mockexam.usercollections.dto

import io.swagger.annotations.ApiModelProperty

enum class Command {

    OPEN_PACK,

    MILL_ROOM,

    BUY_ROOM
}

data class PatchUserDto(

        @get:ApiModelProperty("Command to execute on a user's collection")
        var command: Command? = null,

        @get:ApiModelProperty("Optional ticket Id, if the command requires one")
        var roomId: String? = null
)