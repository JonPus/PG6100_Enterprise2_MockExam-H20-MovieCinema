package no.jonpus.enterprise2.mockexam.usercollections

import no.jonpus.enterprise2.mockexam.rest.dto.WrappedResponse
import no.jonpus.enterprise2.mockexam.rooms.dto.CollectionDto
import no.jonpus.enterprise2.mockexam.rooms.dto.RoomClass
import no.jonpus.enterprise2.mockexam.usercollections.model.Collection
import no.jonpus.enterprise2.mockexam.usercollections.model.Room
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import javax.annotation.PostConstruct
import kotlin.random.Random

@Service
class RoomService(
        private val client: RestTemplate,
        private val circuitBreakerFactory: Resilience4JCircuitBreakerFactory
) {

    companion object {
        private val log = LoggerFactory.getLogger(RoomService::class.java)
    }

    protected var collection: Collection? = null

    @Value("\${roomServiceAddress}")
    private lateinit var roomServiceAddress: String

    val roomCollection: List<Room>
        get() = collection?.rooms ?: listOf()

    private val lock = Any()

    private lateinit var cb: CircuitBreaker

    fun isInitialized() = roomCollection.isNotEmpty()

    @PostConstruct
    fun init() {

        cb = circuitBreakerFactory.create("circuitBreakerToCards")

        synchronized(lock) {
            if (roomCollection.isNotEmpty()) {
                return
            }
            fetchData()
        }
    }

    protected fun fetchData() {

        val version = "v1_000"
        val uri = UriComponentsBuilder
                .fromUriString("http://${roomServiceAddress.trim()}/api/rooms/collection_$version")
                .build().toUri()

        val response = cb.run(
                {
                    client.exchange(
                            uri,
                            HttpMethod.GET,
                            null,
                            object : ParameterizedTypeReference<WrappedResponse<CollectionDto>>() {})
                },
                { e ->
                    log.error("Failed to fetch data from Room Service: ${e.message}")
                    null
                }
        ) ?: return

        if (response.statusCodeValue != 200) {
            log.error("Error in fetching data from Room Service. Status ${response.statusCodeValue}." +
                    "Message; " + response.body.message)
        }

        try {
            collection = Collection(response.body.data!!)
        } catch (e: Exception) {
            log.error("Failed to parse room collection info: ${e.message}")
        }
    }

    private fun verifyCollection() {

        if (collection == null) {
            fetchData()

            if (collection == null) {
                throw IllegalStateException("No collection info")
            }
        }
    }

    fun millValue(roomId: String): Int {
        verifyCollection()
        val room: Room = roomCollection.find { it.roomId == roomId }
                ?: throw IllegalArgumentException("Invalid roomId $roomId")

        return collection!!.millValues[room.roomClass]!!
    }

    fun price(roomId: String): Int {
        verifyCollection()
        val room: Room = roomCollection.find { it.roomId == roomId }
                ?: throw IllegalArgumentException("Invalid roomId $roomId")

        return collection!!.prices[room.roomClass]!!
    }

    fun getRandomSelection(n: Int): List<Room> {

        if (n <= 0) {
            throw IllegalArgumentException("Non-positive n: $n")
        }

        verifyCollection()

        val selection = mutableListOf<Room>()

        val probabilities = collection!!.roomClassProbabilities
        val bronze = probabilities[RoomClass.BRONZE]!!
        val silver = probabilities[RoomClass.SILVER]!!
        val gold = probabilities[RoomClass.GOLD]!!
        //val pink = probabilities[RoomClass.PINK_DIAMOND]!!

        repeat(n) {
            val p = Math.random()
            val r = when {
                p <= bronze -> RoomClass.BRONZE
                p > bronze && p <= bronze + silver -> RoomClass.SILVER
                p > bronze + silver && p <= bronze + silver + gold -> RoomClass.GOLD
                p > bronze + silver + gold -> RoomClass.PINK_DIAMOND
                else -> throw IllegalStateException("BUG for p=$p")
            }
            val room = collection!!.roomsByRoomClass[r].let { it!![Random.nextInt(it.size)] }
            selection.add(room)
        }
        return selection
    }
}