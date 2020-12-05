package no.jonpus.enterprise2.mockexam.usercollections.dto

import io.swagger.annotations.ApiModelProperty

class PatchResultDto {

    @get:ApiModelProperty("If a room pack was opened, specify which room were in it")
    var roomIdsInOpenedPack: MutableList<String> = mutableListOf()

}