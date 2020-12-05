package no.jonpus.enterprise2.mockexam.usercollections.model

import no.jonpus.enterprise2.mockexam.rooms.dto.CollectionDto
import no.jonpus.enterprise2.mockexam.rooms.dto.RoomClass
import java.lang.IllegalArgumentException
import kotlin.math.abs


data class Collection(

        val rooms: List<Room>,

        val prices: Map<RoomClass, Int>,

        val millValues: Map<RoomClass, Int>,

        val roomClassProbabilities: Map<RoomClass, Double>
) {
    constructor(dto: CollectionDto) : this(
            dto.rooms.map { Room(it) },
            dto.prices.toMap(),
            dto.millValue.toMap(),
            dto.roomClassProbabilities.toMap()
    )

    val roomsByRoomClass: Map<RoomClass, List<Room>> = rooms.groupBy { it.roomClass }

    init {
        if (rooms.isEmpty()) {
            throw IllegalArgumentException("No rooms")
        }
        RoomClass.values().forEach {
            requireNotNull(prices[it])
            requireNotNull(millValues[it])
            requireNotNull(roomClassProbabilities[it])
        }

        val p = roomClassProbabilities.values.sum()
        if (abs(1 - p) > 0.00001) {
            throw IllegalArgumentException("Invalid probability sum: $p")
        }
    }
}