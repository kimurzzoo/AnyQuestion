package com.example.anyquestion.speecher

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomRepository : JpaRepository<Room, Int>
{
    fun findByRoompassword(room_password : String) : Room
    fun existsByRoompassword(room_password : String) : Boolean
}