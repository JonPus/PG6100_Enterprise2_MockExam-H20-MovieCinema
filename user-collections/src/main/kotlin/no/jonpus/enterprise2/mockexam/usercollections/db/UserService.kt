package no.jonpus.enterprise2.mockexam.usercollections.db

import no.jonpus.enterprise2.mockexam.usercollections.RoomService
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.LockModeType

@Repository
interface UserRepository : CrudRepository<User, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.userId = :id")
    fun lockedFind(@Param("id") userId: String): User?

}

@Service
@Transactional
class UserService(
        private val userRepository: UserRepository,
        private val roomService: RoomService
) {

    companion object {
        const val ROOMS_PER_PACK = 5
    }

    fun findByIdEager(userId: String): User? {

        val user = userRepository.findById(userId).orElse(null)
        if (user != null) {
            user.ownedRooms.size
        }
        return user
    }

    fun registerNewUser(userId: String): Boolean {

        if (userRepository.existsById(userId)) {
            return false
        }

        val user = User()
        user.userId = userId
        user.roomPacks = 3
        user.coins = 1000
        userRepository.save(user)
        return true
    }

    private fun validateRoom(roomId: String) {
        if (!roomService.isInitialized()) {
            throw IllegalArgumentException("Room service is not initialized")
        }

        if (!roomService.roomCollection.any { it.roomId == roomId }) {
            throw IllegalArgumentException("Invalid roomId: $roomId")
        }
    }

    private fun validateUser(userId: String) {
        if (!userRepository.existsById(userId)) {
            throw IllegalArgumentException("User $userId does not exist")
        }
    }

    private fun validate(userId: String, roomId: String) {
        validateUser(userId)
        validateRoom(roomId)
    }

    fun buyRoom(userId: String, roomId: String) {
        validate(userId, roomId)

        val price = roomService.price(roomId)
        val user = userRepository.lockedFind(userId)!!

        if (user.coins < price) {
            throw IllegalArgumentException("Not enough coins")
        }

        user.coins -= price

        addRoom(user, roomId)
    }

    private fun addRoom(user: User, roomId: String) {
        user.ownedRooms.find { it.roomId == roomId }
                ?.apply { numberOfCopies++ }
                ?: RoomCopy().apply {
                    this.roomId = roomId
                    this.user = user
                    this.numberOfCopies = 1
                }.also { user.ownedRooms.add(it) }
    }

    fun millRoom(userId: String, roomId: String) {
        validate(userId, roomId)

        val user = userRepository.lockedFind(userId)!!

        val copy = user.ownedRooms.find { it.roomId == roomId }

        if (copy == null || copy.numberOfCopies == 0) {
            throw IllegalArgumentException("User $userId does not own a copy of $roomId")
        }

        copy.numberOfCopies--

        val millValue = roomService.millValue(roomId)
        user.coins += millValue
    }

    fun openPack(userId: String): List<String> {

        validateUser(userId)

        val user = userRepository.lockedFind(userId)!!

        if (user.roomPacks < 1) {
            throw IllegalArgumentException("No pack to open")
        }

        user.roomPacks--

        val selection = roomService.getRandomSelection(ROOMS_PER_PACK)

        val ids = mutableListOf<String>()

        selection.forEach {
            addRoom(user, it.roomId)
            ids.add(it.roomId)
        }

        return ids
    }
}