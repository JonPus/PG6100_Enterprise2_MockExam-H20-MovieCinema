package no.jonpus.enterprise2.mockexam

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import no.jonpus.enterprise2.mockexam.rest.dto.WrappedResponse
import no.jonpus.enterprise2.mockexam.rooms.dto.CollectionDto
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.concurrent.TimeUnit


@Api(value = "/api/rooms", description = "Operation on the rooms existing in the cinema")
@RequestMapping(path = ["/api/rooms"])
@RestController
class RestAPI {

    companion object {
        const val LATEST = "v1_000"
    }


    @ApiOperation("Return info on all the rooms in the cinema")
    @GetMapping(
            path = ["/collection_$LATEST"],
            produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getLatest(): ResponseEntity<WrappedResponse<CollectionDto>> {

        val collection = RoomCollection.get()

        return ResponseEntity
                .status(200)
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                .body(WrappedResponse(200, collection).validated())

    }

    @ApiOperation("Old-version endpoints. Will automatically redirect to most recent version")
    @GetMapping(path = [
        "/collection_v0_001",
        "/collection_v0_002",
        "/collection_v0_003"
    ])
    fun getOld() : ResponseEntity<Void>{

        return ResponseEntity.status(301)
                .location(URI.create("/api/rooms/collection_$LATEST"))
                .build()
    }

    @ApiOperation("Return the image for the specified room")
    @GetMapping(
            path= ["/imgs/{imgId}"],
            produces= ["image/svg+xml"]
    )
    fun getImage(@PathVariable("imgId") imgId: String) : ResponseEntity<String>{

        val folder = when{
            imgId.run{ endsWith("-monster.svg") || endsWith("-cyclops.svg")
                    || endsWith("-dragon.svg") || endsWith("-snake.svg")}
            -> "/1236106-monsters"
            else -> return ResponseEntity.status(400).build()
        }

        val svg = javaClass.getResource("$folder/svg/$imgId")?.readText()
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity
                .status(200)
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                .body(svg)
    }
}