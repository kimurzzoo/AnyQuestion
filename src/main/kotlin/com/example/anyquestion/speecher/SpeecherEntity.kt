package com.example.anyquestion.speecher

import javax.persistence.*

@Entity
@Table(name = "speecher")
class Speecher(id : Long)
{
    @Id
    @Column(name = "roomid", unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var roomid : Int? = null

    @Column(name = "userid", nullable = false)
    var userid = id
}

@Entity
@Table(name="room")
class Room(roomid : Int?, roompassword : String, roomnumber : Int)
{
    @Id
    @Column(name="roomid", unique = true)
    var roomid = roomid

    @Column(name = "roompassword", nullable = false)
    var roompassword = roompassword

    @Column(name = "roomnumber", nullable = false)
    var roomnumber = roomnumber
}