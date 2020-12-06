package no.jonpus.enterprise2.mockexam

import no.jonpus.enterprise2.mockexam.rooms.dto.CollectionDto
import no.jonpus.enterprise2.mockexam.rooms.dto.RoomClass.*
import no.jonpus.enterprise2.mockexam.rooms.dto.RoomDto

object RoomCollection {

    fun get(): CollectionDto {

        val dto = CollectionDto()


        dto.prices.run {
            put(BRONZE, 100)
            put(SILVER, 500)
            put(GOLD, 1_000)
            put(PINK_DIAMOND, 100_000)
        }

        dto.prices.forEach { dto.millValue[it.key] = it.value / 4 }

        dto.roomClassProbabilities.run {
            put(SILVER, 0.10)
            put(GOLD, 0.01)
            put(PINK_DIAMOND, 0.001)
            put(BRONZE, 1 - get(SILVER)!! - get(GOLD)!! - get(PINK_DIAMOND)!!)
        }

        addRooms(dto)

        return dto
    }

    private fun addRooms(dto: CollectionDto) {

        dto.rooms.run {

            add(RoomDto("c000", "SpeedRun", 100, 200, BRONZE, "035-monster.svg"))
            add(RoomDto("c001", "Lion Queen", 100, 200, BRONZE, "056-monster.svg"))
            add(RoomDto("c002", "Bengt the Pooh", 100, 200, BRONZE, "070-monster.svg"))
            add(RoomDto("c003", "Slow Minutes", 100, 200, BRONZE, "100-monster.svg"))
            add(RoomDto("c004", "National Thrash", 100, 200, BRONZE, "075-monster.svg"))
            add(RoomDto("c005", "El Diego", 100, 200, BRONZE, "055-monster.svg"))
            add(RoomDto("c006", "Lo Fi", 100, 200, BRONZE, "063-monster.svg"))
            add(RoomDto("c007", "Not-Blood Diamond", 100, 200, BRONZE, "050-monster.svg"))
            add(RoomDto("c008", "Cowboys of the West", 100, 200, BRONZE, "019-monster.svg"))
            add(RoomDto("c009", "Lord of the Chain", 100, 200, BRONZE, "006-monster.svg"))
            add(RoomDto("c010", "TinMan", 100, 200, SILVER, "081-monster.svg"))
            add(RoomDto("c011", "Y-Men", 100, 200, SILVER, "057-monster.svg"))
            add(RoomDto("c012", "Mansion of the Sky", 100, 200, SILVER, "028-monster.svg"))
            add(RoomDto("c013", "My homeless man, Jon", 100, 200, SILVER, "032-monster.svg"))
            add(RoomDto("c014", "2Slow 2Calm", 100, 200, SILVER, "002-monster.svg"))
            add(RoomDto("c015", "Captain Mexico", 100, 200, GOLD, "036-monster.svg"))
            add(RoomDto("c016", "Loki", 100, 200, GOLD, "064-monster.svg"))
            add(RoomDto("c017", "A Christmas Tragedy", 100, 200, GOLD, "044-monster.svg"))
            add(RoomDto("c018", "Demon Away", 100, 200, GOLD, "041-monster.svg"))
            add(RoomDto("c019", "God of War", 100, 200, PINK_DIAMOND, "051-monster.svg"))
        }

        assert(dto.rooms.size == dto.rooms.map { it.roomId }.toSet().size)
        assert(dto.rooms.size == dto.rooms.map { it.movieName }.toSet().size)
        //assert(dto.rooms.size == dto.rooms.map { it.seats }.toSet().size)
        assert(dto.rooms.size == dto.rooms.map { it.imageId }.toSet().size)

    }
}