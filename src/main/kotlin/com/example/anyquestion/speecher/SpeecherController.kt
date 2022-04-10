package com.example.anyquestion.speecher

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/speecher")
class SpeecherController(private val speecherService : SpeecherService)
{
    @GetMapping("/create")
    fun groupCreate() : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(speecherService.groupCreate())
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