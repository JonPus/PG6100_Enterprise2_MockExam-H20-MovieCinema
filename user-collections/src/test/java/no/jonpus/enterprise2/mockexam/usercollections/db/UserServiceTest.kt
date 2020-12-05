package no.jonpus.enterprise2.mockexam.usercollections.db

import no.jonpus.enterprise2.mockexam.usercollections.FakeData
import no.jonpus.enterprise2.mockexam.usercollections.RoomService
import no.jonpus.enterprise2.mockexam.usercollections.model.Collection
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.RestTemplate

@Profile("UserServiceTest")
@Primary
@Service
class FakeCardService : RoomService(RestTemplate(), Resilience4JCircuitBreakerFactory()) {

    override fun fetchData() {
        val dto = FakeData.getCollectionDto()
        super.collection = Collection(dto)
    }
}

@ActiveProfiles("UserServiceTest,test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
internal class UserServiceTest {


    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun initTest() {
        userRepository.deleteAll()
    }

    @Test
    fun testCreateUser() {
        val id = "foo"
        assertTrue(userService.registerNewUser(id))
        assertTrue(userRepository.existsById(id))
    }

    @Test
    fun testFailCreateUserTwice() {
        val id = "foo"
        assertTrue(userService.registerNewUser(id))
        assertFalse(userService.registerNewUser(id))
    }

    @Test
    fun testBuyRoom() {

        val userId = "foo"
        val ticketId = "c00"

        userService.registerNewUser(userId)
        userService.buyRoom(userId, ticketId)

        val user = userService.findByIdEager(userId)!!
        assertTrue(user.ownedRooms.any { it.roomId == ticketId })
    }

    @Test
    fun testBuyRoomFailNotEnoughMoney() {

        val userId = "foo"
        val ticketId = "c09"

        userService.registerNewUser(userId)

        val e = assertThrows(IllegalArgumentException::class.java) {
            userService.buyRoom(userId, ticketId)
        }
        assertTrue(e.message!!.contains("coins"), "Wrong error message: ${e.message}")
    }

    @Test
    fun testOpenPack() {

        val userId = "foo"
        userService.registerNewUser(userId)

        val before = userService.findByIdEager(userId)!!
        val totRooms = before.ownedRooms.sumBy { it.numberOfCopies }
        val totPacks = before.roomPacks
        assertTrue(totPacks > 0)

        val n = userService.openPack(userId).size
        assertEquals(UserService.ROOMS_PER_PACK, n)

        val after = userService.findByIdEager(userId)!!
        assertEquals(totPacks - 1, after.roomPacks)
        assertEquals(totRooms + UserService.ROOMS_PER_PACK,
                after.ownedRooms.sumBy { it.numberOfCopies })
    }

    @Test
    fun testOpenPackFail() {

        val userId = "foo"
        userService.registerNewUser(userId)

        val before = userService.findByIdEager(userId)!!
        val totPacks = before.roomPacks

        repeat(totPacks) {
            userService.openPack(userId)
        }

        val after = userService.findByIdEager(userId)!!
        assertEquals(0, after.roomPacks)

        assertThrows(IllegalArgumentException::class.java) {
            userService.openPack(userId)
        }

    }

    @Test
    fun testMillRoom() {

        val userId = "foo"
        userService.registerNewUser(userId)

        val before = userRepository.findById(userId).get()
        val coins = before.coins

        userService.openPack(userId)

        val between = userService.findByIdEager(userId)!!
        val n = between.ownedRooms.sumBy { it.numberOfCopies }
        userService.millRoom(userId, between.ownedRooms[0].roomId!!)

        val after = userService.findByIdEager(userId)!!
        assertTrue(after.coins > coins)
        assertEquals(n - 1, after.ownedRooms.sumBy { it.numberOfCopies })
    }
}