package com.example.anyquestion.questioner

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/questioner")
class QuestionerController(private val questionerService : QuestionerService)
{
    @PostMapping("/search")
    fun groupSearch(@RequestBody groupSearchDTO : GroupSearchDTO) : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(questionerService.groupSearch(groupSearchDTO))
    }

    @PostMapping("/me")
    fun me(@RequestBody meDTO : MeDTO) : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(questionerService.me(meDTO))
    }

    /*@GetMapping(value = "/subscribe", produces = "text/event-stream")
    fun subscribe(@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") lastEventId : String) : SseEmitter
    {
        return questionerService.subscribe()
    }*/
}