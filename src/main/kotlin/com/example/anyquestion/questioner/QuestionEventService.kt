package com.example.anyquestion.questioner

import org.springframework.stereotype.Component
import org.springframework.context.ApplicationEventPublisher

@Component
class QuestionEventService(private val applicationEventPublisher : ApplicationEventPublisher)
{
    fun publishCustomEvent(roomid : Int, userId : Long, role : Boolean)
    {
        var questionEvent = QuestionEvent(this as Any, roomid, userId, role)
        applicationEventPublisher.publishEvent(questionEvent)
    }
}
