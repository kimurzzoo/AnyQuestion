package com.example.anyquestion.sse

import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.lang.RuntimeException

@Service
class EmitterService(private val emitterRepository : EmitterRepository)
{
    private val DEFAULT_TIMEOUT = 60L * 1000 * 60

    fun subscribe(userid : Long, role : Boolean, datum : Any) : SseEmitter
    {
        val emitter = emitterRepository.save(userid, SseEmitter(DEFAULT_TIMEOUT), role)

        emitter.onCompletion{emitterRepository.deleteById(userid, role)}
        emitter.onTimeout{emitterRepository.deleteById(userid, role)}

        sendToClient(emitter, userid, role, datum)
        return emitter
    }

    fun sendToClient(emitter : SseEmitter, userid : Long, role : Boolean, datum : Any)
    {
        try
        {
            emitter.send(SseEmitter.event().id("sse").name("sse").data(datum))
        }
        catch(exception : IOException)
        {
            emitterRepository.deleteById(userid, role)
            throw RuntimeException("연결 오류")
        }
    }

    fun unsubscribe(userid : Long, role : Boolean)
    {
        emitterRepository.deleteById(userid, role)
    }
}