package com.example.anyquestion.questioner

import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.stereotype.Component
import org.springframework.scheduling.annotation.Async

@Component
class QuestionEventListener
{
    @TransactionalEventListener
    @Async
    fun handler(questionEvent : QuestionEvent)
    {
        println(questionEvent.roomid.toString() + " " + questionEvent.userId.toString())
    }
}