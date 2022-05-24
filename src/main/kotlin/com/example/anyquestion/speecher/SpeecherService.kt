package com.example.anyquestion.speecher

import com.example.anyquestion.account.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*
import com.example.anyquestion.sse.*
import com.example.anyquestion.questioner.*

@Service
class SpeecherService(private val speecherRepository : SpeecherRepository,
                    private val questionerRepository : QuestionerRepository,
                    private val userRepository : UserRepository,
                    private val roomRepository : RoomRepository,
                    private val emitterService : EmitterService,
                    private val emitterRepository : EmitterRepository)
{
    private val randomString : Array<String> = arrayOf("charlie", "william", "michael", "jonathan", "elizabeth", "beatrice", "catherine", "jennifer")
    private val randomInt : Int = randomString.size
    private val random = Random()

    @Transactional
    fun groupCreate() : SseEmitter
    {
        var userId = SecurityUtil.getCurrentUserId()
        val speecher = Speecher(userId!!)
        speecherRepository.save(speecher)

        val num = random.nextInt(randomInt)
        val savedspeecher = speecherRepository.findByUserid(userId)
        val roompassword = randomString[num] + savedspeecher.roomid.toString()
        val room = Room(savedspeecher.roomid, roompassword, 1)
        roomRepository.save(room)
        var emitter = emitterService.subscribe(userId, true, "password:" + roompassword)
        return emitter
    }

    @Transactional
    fun groupDelete() : GroupDeleteResult
    {
        var userId = SecurityUtil.getCurrentUserId()
        var nowspeecher : Speecher? = null
        if(userId != null)
        {
            nowspeecher = speecherRepository.findByUserid(userId!!)
        }
        
        if(nowspeecher != null)
        {
            val restList = questionerRepository.allQuestioners(nowspeecher.roomid!!)
            if(restList.size > 0)
            {
                restList.forEach{
                    emitterService.sendToClient(emitterRepository.findByIdWithRole(it, false)!!, it, false, "group deleted")
                    emitterService.unsubscribe(it, false)
                }
            }

            if(speecherRepository.deleteByUserid(userId!!) > 0)
            {
                emitterService.unsubscribe(userId, true)
            }

            return GroupDeleteResult(true)
        }
        
        else
            return GroupDeleteResult(false)
    }

    fun groupFind() : SpeecherDTO
    {
        var userId = SecurityUtil.getCurrentUserId()
        val speecher = speecherRepository.findByUserid(userId!!)
        return SpeecherDTO(roomRepository.findById(speecher.roomid!!).get().roompassword)
    }

    @Transactional
    fun next() 
    {
        var userId = SecurityUtil.getCurrentUserId()
        val speecher = speecherRepository.findByUserid(userId!!)
        val currentUserList = questionerRepository.nextQuestion(speecher.roomid!!)
        if(currentUserList.size == 0)
        {
            emitterService.sendToClient(emitterRepository.findByIdWithRole(userId, true)!!, userId, true, "no")
        }
        else
        {
            val currentUser = currentUserList.get(0).userid

            emitterService.sendToClient(emitterRepository.findByIdWithRole(currentUser, false)!!, currentUser, false, "your question is ended")
            questionerRepository.deleteByUserid(currentUser)

            val nextUser = questionerRepository.nextQuestion(speecher.roomid!!)

            if(nextUser.size == 0)
            {
                emitterService.sendToClient(emitterRepository.findByIdWithRole(userId, true)!!, userId, true, "no")
            }
            else
            {
                emitterService.sendToClient(emitterRepository.findByIdWithRole(nextUser.get(0).userid, false)!!, nextUser.get(0).userid, false, "your turn")
                emitterService.sendToClient(emitterRepository.findByIdWithRole(userId, true)!!, userId, true, "next:" + userRepository.findById(nextUser.get(0).userid).get().name + ":" + nextUser.get(0).number.toString())
            }
        }
    }
}