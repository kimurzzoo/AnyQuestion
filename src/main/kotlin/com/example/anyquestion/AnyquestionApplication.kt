package com.example.anyquestion

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class AnyquestionApplication

fun main(args: Array<String>) {
	runApplication<AnyquestionApplication>(*args)
}
