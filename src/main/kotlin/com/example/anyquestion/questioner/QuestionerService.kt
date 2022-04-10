package com.example.anyquestion.questioner

import com.example.anyquestion.speecher.*
import com.example.anyquestion.account.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*

@Service
class QuestionerService(private val userRepository : UserRepository, private val roomRepository : RoomRepository, private val questionerRepository : QuestionerRepository, private val questionEventService : QuestionEventService)
{
    @Transactional
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
    fun me(meDTO : MeDTO) : MeResultDTO
    {
        var meResultDTO = MeResultDTO(-1)
        if(roomRepository.existsByRoompassword(meDTO.roompassword))
        {
            var email = SecurityUtil.getCurrentUserEmail()
            var userid = userRepository.findByEmail(email).id
            var room = roomRepository.findByRoompassword(meDTO.roompassword)
            questionEventService.publishCustomEvent(room.roomid!!, userid!!)
            var questioner = Questioner(room.roomid!!, userid!!, room.roomnumber)
            roomRepository.save(Room(room.roomid, room.roompassword, room.roomnumber + 1))
            meResultDTO.number = room.roomnumber
            questionerRepository.save(questioner)
        }

        return meResultDTO
    }
}