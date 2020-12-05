package no.jonpus.enterprise2.mockexam

import no.jonpus.enterprise2.mockexam.db.MoveInfoService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class MOMListener(
    private val infoService: MoveInfoService
) {
    companion object {
        private val log = LoggerFactory.getLogger(MOMListener::class.java)
    }

    @RabbitListener(queues = ["#{queue.name}"])
    fun receiveFromAMQP(movieId: String) {
        val ok = infoService.registerNewMovie(movieId)
        if (ok) {
            log.info("Registered new movie via MOM: $movieId")
        }
    }
}