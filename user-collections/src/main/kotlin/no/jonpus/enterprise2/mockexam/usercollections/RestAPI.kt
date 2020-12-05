package no.jonpus.enterprise2.mockexam.usercollections

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import no.jonpus.enterprise2.mockexam.rest.dto.RestResponseFactory
import no.jonpus.enterprise2.mockexam.rest.dto.WrappedResponse
import no.jonpus.enterprise2.mockexam.usercollections.db.UserService
import no.jonpus.enterprise2.mockexam.usercollections.dto.Command
import no.jonpus.enterprise2.mockexam.usercollections.dto.PatchResultDto
import no.jonpus.enterprise2.mockexam.usercollections.dto.PatchUserDto
import no.jonpus.enterprise2.mockexam.usercollections.dto.UserDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.IllegalArgumentException

@Api(value = "/api/user-collections", description = "Operations on room collections owned by users")
@RequestMapping(
        path = ["/api/user-collections"],
        produces = [(MediaType.APPLICATION_JSON_VALUE)]
)

@RestController
class RestAPI(
        private val userService: UserService
) {

    @ApiOperation("Retrieve card collection information for a specific user")
    @GetMapping(path = ["/{userId}"])
    fun getUserInfo(
            @PathVariable("userId") userId: String
    ): ResponseEntity<WrappedResponse<UserDto>> {

        val user = userService.findByIdEager(userId)
        if (user == null) {
            return RestResponseFactory.notFound("User $userId not found")
        }

        return RestResponseFactory.payload(200, DtoConverter.transform(user))
    }

    @ApiOperation("Create a new user, with the given id")
    @PutMapping(path = ["/{userId}"])
    fun createUser(
            @PathVariable("userId") userId: String
    ): ResponseEntity<WrappedResponse<Void>> {
        val ok = userService.registerNewUser(userId)
        return if (!ok) RestResponseFactory.userFailure("User $userId already exist")
        else RestResponseFactory.noPayload(201)
    }

    @ApiOperation("Execute a command on a user's collection, like for example buying/milling rooms")
    @PatchMapping(
            path = ["/{userId}"],
            consumes = [(MediaType.APPLICATION_JSON_VALUE)]
    )
    fun patchUser(
            @PathVariable("userId") userId: String,
            @RequestBody dto: PatchUserDto
    ): ResponseEntity<WrappedResponse<PatchResultDto>> {

        if (dto.command == null) {
            return RestResponseFactory.userFailure("Missing command")
        }

        if (dto.command == Command.OPEN_PACK) {
            val ids = try {
                userService.openPack(userId)
            } catch (e: IllegalArgumentException) {
                return RestResponseFactory.userFailure(e.message ?: "Failed to open pack")
            }
            return RestResponseFactory.payload(200, PatchResultDto().apply { roomIdsInOpenedPack.addAll(ids) })
        }

        val roomId = dto.roomId
                ?: return RestResponseFactory.userFailure("Missing room ID")

        if (dto.command == Command.BUY_ROOM) {
            try {
                userService.buyRoom(userId, roomId)
            } catch (e: IllegalArgumentException) {
                return RestResponseFactory.userFailure(e.message ?: "Failed to buy room $roomId")
            }
            return RestResponseFactory.payload(200, PatchResultDto())
        }

        if (dto.command == Command.MILL_ROOM) {
            try {
                userService.millRoom(userId, roomId)
            } catch (e: IllegalArgumentException) {
                return RestResponseFactory.userFailure(e.message ?: "Failed to mill room $roomId")
            }
            return RestResponseFactory.payload(200, PatchResultDto())
        }

        return RestResponseFactory.userFailure("Unrecognized command: ${dto.command}")
    }
}