package no.jonpus.enterprise2.mockexam.usercollections.db


import javax.persistence.*
import javax.validation.constraints.*

@Entity
class RoomCopy(

        @get:Id
        @get:GeneratedValue
        var id: Long? = null,

        @get:ManyToOne
        @get:NotNull
        var user: User? = null,

        @get:NotBlank
        var roomId: String? = null,

        @get:Min(0)
        var numberOfCopies: Int = 0

)