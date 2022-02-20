package no.jonpus.enterprise2.mockexam.usercollections.model

import no.jonpus.enterprise2.mockexam.rooms.dto.RoomClass
import no.jonpus.enterprise2.mockexam.rooms.dto.RoomDto

data class Room(
    var roomId: String,
    var roomClass: RoomClass
) {
    constructor(dto: RoomDto) : this(

        dto.roomId ?: throw IllegalArgumentException("Null roomId"),
        dto.roomClass ?: throw IllegalArgumentException("null room class")
    )
}