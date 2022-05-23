package com.example.anyquestion.questioner

import com.example.anyquestion.speecher.*
import com.example.anyquestion.account.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*
import com.example.anyquestion.sse.EmitterService

@Service
class QuestionerService(private val userRepository : UserRepository,
                        private val roomRepository : RoomRepository,
                        private val questionerRepository : QuestionerRepository,
                        private val questionEventService : QuestionEventService,
                        private val emitterService : EmitterService)
{
    fun groupSearch(groupSearchDTO : GroupSearchDTO) : GroupSearchResultDTO
    {
        var groupSearchResultDTO = GroupSearchResultDTO(false)

        if(roomRepository.existsByRoompassword(groupSearchDTO.roompassword))
        {
            groupSearchResultDTO.ok = true
        }

        return groupSearchResultDTO
    }

    @Transactional
    fun me(meDTO : MeDTO) : SseEmitter?
    {
        var emitter : SseEmitter? = null
        if(roomRepository.existsByRoompassword(meDTO.roompassword))
        {
            var email = SecurityUtil.getCurrentUserEmail()
            var userid = userRepository.findByEmail(email).id
            val room = roomRepository.findByRoompassword(meDTO.roompassword)
            val roomid : Int = room.roomid!!
            val roomnumber : Int = room.roomnumber
            var questioner = Questioner(roomid, userid!!, roomnumber)
            roomRepository.save(Room(roomid, room.roompassword, roomnumber + 1))
            emitter = emitterService.subscribe(userid, false, roomnumber.toString())
            if(questionerRepository.nowCount(roomid) == 0)
            {
                print("no questioner before here")
                questionerRepository.save(questioner)
                questionEventService.publishCustomEvent(roomid, userid)
                emitterService.sendToClient(emitter, userid, false, "your turn")
            }
            else
            {
                print("wait for next")
                questionerRepository.save(questioner)
            }
        }

        return emitter
    }

    @Transactional
    fun meOut() : MeOutResultDTO
    {
        var meOutResultDTO = MeOutResultDTO(false)

        var email = SecurityUtil.getCurrentUserEmail()
        var userid = userRepository.findByEmail(email).id

        questionerRepository.deleteByUserid(userid!!)
        emitterService.unsubscribe(userid!!, false)
        meOutResultDTO.ok = true
        return meOutResultDTO
    }
}