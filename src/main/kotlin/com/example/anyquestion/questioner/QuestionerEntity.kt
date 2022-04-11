package com.example.anyquestion.questioner

import javax.persistence.*
import org.springframework.context.ApplicationEvent

@Entity
@Table(name = "question")
class Questioner(roomid : Int, userid : Long, number : Int)
{
    @Id
    @Column(name = "questionnumber", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var questionnumber : Int? = null

    @Column(name = "roomid", nullable = false)
    var roomid = roomid

    @Column(name = "userid", nullable = false)
    var userid = userid

    @Column(name = "number", nullable = false)
    var number = number
}

class QuestionEvent : ApplicationEvent
{
    var roomid : Int
    var userId : Long
    var role : Boolean

    constructor(obj : Any, roomid : Int, userId : Long, role : Boolean) : super(obj)
    {
        this.roomid = roomid
        this.userId = userId
        this.role = role
    }
    
}