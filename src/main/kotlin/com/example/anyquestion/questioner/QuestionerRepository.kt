package com.example.anyquestion.questioner

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

@Repository
interface QuestionerRepository : JpaRepository<Questioner, Int>
{
    @Query("select count(*) from Questioner q where q.roomid = :roomid")
    fun nowCount(@Param("roomid") roomid : Int) : Int

    fun deleteByUserid(userid : Long)

    @Query("select r.userid from Questioner r where r.roomid = :roomid and r.number = (select min(q.number) from Questioner q where q.roomid = :roomid)")
    fun nextQuestion(@Param("roomid") roomid : Int) : Long?
}