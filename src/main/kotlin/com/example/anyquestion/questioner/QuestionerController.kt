package com.example.anyquestion.questioner

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/questioner")
class QuestionerController(private val questionerService : QuestionerService)
{
    @PostMapping("/search")
    fun groupSearch(@RequestBody groupSearchDTO : GroupSearchDTO) : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(questionerService.groupSearch(groupSearchDTO))
    }

    @PostMapping(value = ["/me"], produces = ["text/event-stream"], consumes=["application/json"])
    fun me(@RequestBody meDTO : MeDTO) : SseEmitter?
    {
        return questionerService.me(meDTO)
    }
}