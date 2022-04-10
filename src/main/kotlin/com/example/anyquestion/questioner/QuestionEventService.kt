package com.example.anyquestion.questioner

import org.springframework.stereotype.Component
import org.springframework.context.ApplicationEventPublisher

@Component
class QuestionEventService(private val applicationEventPublisher : ApplicationEventPublisher)
{
    fun publishCustomEvent(roomid : Int, userId : Long)
    {
        var questionEvent = QuestionEvent(this as Any, roomid, userId)
        applicationEventPublisher.publishEvent(questionEvent)
    }
}
