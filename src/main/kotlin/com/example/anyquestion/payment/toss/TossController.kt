package com.example.anyquestion.payment.toss

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

@Controller
@RequestMapping("/payment/toss")
class TossController {
    private val objectMapper = ObjectMapper()

    @Value("\${toss.privatekey}")
    val SECRET_KEY : String = ""

    @GetMapping("/submit")
    fun submit() : String
    {
        return "tossstart"
    }

    @RequestMapping("/success")
    fun success(@RequestParam("paymentKey") paymentKey : String,
                @RequestParam("orderId") orderId : String,
                @RequestParam("amount") amount : String) : String
    {
        val encodedAuthHeader = Base64.getEncoder().encodeToString(("$SECRET_KEY:").toByteArray())

        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.tosspayments.com/v1/payments/$paymentKey"))
            .header("Authorization", "Basic $encodedAuthHeader")
            .header("Content-Type", "application/json")
            .method("POST", HttpRequest.BodyPublishers.ofString("{\"amount\":$amount,\"orderId\":\"$orderId\"}"))
            .build()

        val response: HttpResponse<String> =
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

        return if (response.statusCode() == HttpStatus.OK.value()) {
            /**
             * 4. 빌링키, 카드 정보 포함된 Json 으로 성공 View 리턴
             */
            val jsonNode = objectMapper.readTree(response.body())
            "success"
        } else {
            "fail"
        }
    }
}