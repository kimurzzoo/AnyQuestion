package com.example.anyquestion.questioner

data class GroupSearchDTO(
    val roompassword : String
)

data class GroupSearchResultDTO(
    var ok : Boolean
)

data class MeDTO(
    var roompassword : String
)

data class MeResultDTO(
    var number : Int
)

data class MeOutResultDTO(
    var ok : Boolean
)