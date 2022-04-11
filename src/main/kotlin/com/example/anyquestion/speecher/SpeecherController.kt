package com.example.anyquestion.speecher

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/speecher")
class SpeecherController(private val speecherService : SpeecherService)
{
    @GetMapping(value = ["/create"], produces = ["text/event-stream"])
    fun groupCreate() : SseEmitter
    {
        return speecherService.groupCreate()
    }

    @GetMapping("/delete")
    fun groupDelete() : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(speecherService.groupDelete())
    }

    @GetMapping("/find")
    fun groupFind() : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(speecherService.groupFind())
    }
}