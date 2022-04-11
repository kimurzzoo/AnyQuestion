package com.example.anyquestion.speecher

import com.example.anyquestion.account.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*
import com.example.anyquestion.sse.EmitterService

@Service
class SpeecherService(private val speecherRepository : SpeecherRepository, private val userRepository : UserRepository, private val roomRepository : RoomRepository, private val emitterService : EmitterService)
{
    private val randomString : Array<String> = arrayOf("charlie", "william", "michael", "jonathan", "elizabeth", "beatrice", "catherine", "jennifer")
    private val randomInt : Int = randomString.size
    private val random = Random()

    @Transactional
    fun groupCreate() : SseEmitter
    {
        var userEmail = SecurityUtil.getCurrentUserEmail()
        var userId = userRepository.findByEmail(userEmail).id
        val speecher = Speecher(userId!!)
        speecherRepository.save(speecher)

        val num = random.nextInt(randomInt)
        val savedspeecher = speecherRepository.findByUserid(userId)
        val roompassword = randomString[num] + savedspeecher.roomid.toString()
        val room = Room(savedspeecher.roomid, roompassword, 1)
        roomRepository.save(room)
        var emitter = emitterService.subscribe(userId, true)
        emitterService.sendToClient(emitter, userId, true, roompassword)
        return emitter
    }

    @Transactional
    fun groupDelete() : GroupDeleteResult
    {
        var userEmail = SecurityUtil.getCurrentUserEmail()
        var userId = userRepository.findByEmail(userEmail).id
        if(speecherRepository.deleteByUserid(userId!!) > 0)
        {
            emitterService.unsubscribe(userId, true)
            return GroupDeleteResult(true)
        }
        else
            return GroupDeleteResult(false)
    }

    @Transactional
    fun groupFind() : SpeecherDTO
    {
        var userEmail = SecurityUtil.getCurrentUserEmail()
        var userId = userRepository.findByEmail(userEmail).id
        val speecher = speecherRepository.findByUserid(userId!!)
        return SpeecherDTO(roomRepository.findById(speecher.roomid!!).get().roompassword)
    }

    //@Transactional
    //fun next() : 
}