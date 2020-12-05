package no.jonpus.enterprise2.mockexam.usercollections

import no.jonpus.enterprise2.mockexam.usercollections.db.RoomCopy
import no.jonpus.enterprise2.mockexam.usercollections.db.User
import no.jonpus.enterprise2.mockexam.usercollections.dto.RoomCopyDto
import no.jonpus.enterprise2.mockexam.usercollections.dto.UserDto

object DtoConverter {

    fun transform(user: User): UserDto {

        return UserDto().apply {
            userId = user.userId
            coins = user.coins
            roomPacks = user.roomPacks
            ownedRooms = user.ownedRooms.map { transform(it) }.toMutableList()
        }
    }

    fun transform(roomCopy: RoomCopy): RoomCopyDto {
        return RoomCopyDto().apply {
            roomId = roomCopy.roomId
            numberOfCopies = roomCopy.numberOfCopies
        }
    }
}