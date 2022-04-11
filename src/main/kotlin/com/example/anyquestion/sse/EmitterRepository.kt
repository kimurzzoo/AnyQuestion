package com.example.anyquestion.sse

import java.util.*
import org.springframework.stereotype.Repository
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Repository
class EmitterRepository
{
    var questionerMap = ConcurrentHashMap<Long, SseEmitter>()
    var speecherMap = ConcurrentHashMap<Long, SseEmitter>()

    fun save(userid : Long, sseEmitter : SseEmitter, role : Boolean) : SseEmitter
    {
        if(role)
        {
            speecherMap.put(userid, sseEmitter)
        }
        else
        {
            questionerMap.put(userid, sseEmitter)
        }

        return sseEmitter
    }

    fun deleteById(userid : Long, role : Boolean)
    {
        if(role)
        {
            speecherMap.remove(userid)!!.complete()
        }
        else
        {
            questionerMap.remove(userid)!!.complete()
        }
    }

    fun findByIdWithRole(userid : Long, role : Boolean) : SseEmitter?
    {
        if(role)
        {
            return speecherMap.get(userid)
        }
        else
        {
            return questionerMap.get(userid)
        }
    }
}