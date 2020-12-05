package no.jonpus.enterprise2.mockexam.usercollections

import no.jonpus.enterprise2.mockexam.rooms.dto.CollectionDto
import no.jonpus.enterprise2.mockexam.rooms.dto.RoomClass.*
import no.jonpus.enterprise2.mockexam.rooms.dto.RoomDto

object FakeData {

    fun getCollectionDto(): CollectionDto {

        val dto = CollectionDto()

        dto.prices[BRONZE] = 100
        dto.prices[SILVER] = 500
        dto.prices[GOLD] = 1_000
        dto.prices[PINK_DIAMOND] = 100_000

        dto.prices.forEach { dto.millValue[it.key] = it.value / 4 }
        dto.prices.keys.forEach { dto.roomClassProbabilities[it] = 0.25 }

        dto.rooms.run {
            add(RoomDto(roomId = "c00", roomClass = BRONZE))
            add(RoomDto(roomId = "c01", roomClass = BRONZE))
            add(RoomDto(roomId = "c02", roomClass = BRONZE))
            add(RoomDto(roomId = "c03", roomClass = BRONZE))
            add(RoomDto(roomId = "c04", roomClass = SILVER))
            add(RoomDto(roomId = "c05", roomClass = SILVER))
            add(RoomDto(roomId = "c06", roomClass = SILVER))
            add(RoomDto(roomId = "c07", roomClass = GOLD))
            add(RoomDto(roomId = "c08", roomClass = GOLD))
            add(RoomDto(roomId = "c09", roomClass = PINK_DIAMOND))
        }
        return dto
    }
}