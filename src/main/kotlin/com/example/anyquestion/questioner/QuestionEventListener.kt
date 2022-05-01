package com.example.anyquestion.questioner

import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.stereotype.Component
import org.springframework.scheduling.annotation.Async
import com.example.anyquestion.sse.*
import com.example.anyquestion.speecher.*
import com.example.anyquestion.account.*

@Component
class QuestionEventListener(private val emitterService : EmitterService,
                            private val emitterRepository : EmitterRepository,
                            private val speecherRepository : SpeecherRepository,
                            private val questionerRepository : QuestionerRepository,
                            private val userRepository : UserRepository)
{
    @TransactionalEventListener
    @Async
    fun handler(questionEvent : QuestionEvent)
    {
        val speecher = speecherRepository.findById(questionEvent.roomid).get()
                                                    emitterService.sendToClient(emitterRepository.findByIdWithRole(speecher.userid, true)!!,
                                                    speecher.userid,
                                                    true,
                                                    "next:" + userRepository.findById(questionEvent.userId).get().name + ":" + questionerRepository.findByRoomidAndUserid(questionEvent.roomid, questionEvent.userId).number.toString())
    }
}