package com.example.anyquestion.speecher

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SpeecherRepository : JpaRepository<Speecher, Int>
{
    fun findByUserid(userId : Long) : Speecher
    fun deleteByUserid(userId : Long) : Int
}