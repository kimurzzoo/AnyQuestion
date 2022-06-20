package com.example.anyquestion.payment.toss

import com.example.anyquestion.account.SecurityUtil.Companion.getCurrentUserEmail
import com.example.anyquestion.account.SecurityUtil.Companion.getCurrentUserId
import com.example.anyquestion.account.UserRepository
import com.example.anyquestion.payment.*
import com.example.anyquestion.payment.Merchandise.Companion.merchandiseWonList
import com.example.anyquestion.payment.Merchandise.Companion.merchandiseWonarr
import com.example.anyquestion.util.OtherUtils.Companion.genOrderId
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.sql.Timestamp
import java.util.*

@Controller
@RequestMapping("/payment/toss")
class TossController(private val userRepository: UserRepository,
                     private val paymentRepository: PaymentRepository,
                     private val durationRepository: DurationRepository,
                     private val refundRepository: RefundRepository) {
    private val objectMapper = ObjectMapper()

    @Value("\${toss.privatekey}")
    val secretkey : String = ""

    @Value("\${toss.clientkey}")
    val clientkey : String = ""

    val encodedAuthHeader : String = Base64.getEncoder().encodeToString(("$secretkey:").toByteArray())

    @PostMapping("/submit")
    fun submit(@RequestBody tossSubmitDTO: TossSubmitDTO, model : Model) : String
    {
        model["merNum"] = tossSubmitDTO.merNum
        model["clientkey"] = clientkey
        model["amount"] = merchandiseWonarr[tossSubmitDTO.merNum]
        model["userOrderId"] = genOrderId(getCurrentUserEmail())
        return "tossstart"
    }

    @GetMapping("/success")
    fun success(@RequestParam("paymentKey") paymentKey : String,
                @RequestParam("orderId") orderId : String,
                @RequestParam("amount") amount : String) : String
    {


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
            val userid = getCurrentUserId(userRepository)
            var durationInc = durationRepository.findById(userid).get()
            val merId = amount.toInt()
            paymentRepository.save(PaymentEntity(userid, "toss", paymentKey, merchandiseWonList[merId]!!, Timestamp(System.currentTimeMillis())))

            var cal = Calendar.getInstance()
            cal.time= Date()
            if(durationInc.isBefore(cal))
            {
                cal.add(Merchandise.durationList[merId][0], Merchandise.durationList[merId][1])
                durationInc.expireddate.time = cal.time.time
            }
            else
            {
                durationInc.add(merId)
            }
            durationRepository.save(durationInc)
            "success"
        } else {
            "fail"
        }
    }

    @PostMapping("/refund")
    fun refund(tossRefundDTO: TossRefundDTO) : TossRefundResultDTO
    {
        val userid = getCurrentUserId(userRepository)
        if(!paymentRepository.existsByMethodAndPaymentid("toss", tossRefundDTO.paymentKey))
            return TossRefundResultDTO(false)

        val paymentIns = paymentRepository.findByMethodAndPaymentid("toss", tossRefundDTO.paymentKey)

        if(!userid.equals(paymentIns.userid))
            return TossRefundResultDTO(false)

        val durationIns = durationRepository.findById(paymentIns.userid).get()

        if(!durationIns.isRefundable(paymentIns.merid))
            return TossRefundResultDTO(false)
        else
            durationIns.subtract(paymentIns.merid)

        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://api.tosspayments.com/v1/payments/${tossRefundDTO.paymentKey}"))
            .header("Authorization", "Basic $encodedAuthHeader")
            .header("Content-Type", "application/json")
            .method("POST", HttpRequest.BodyPublishers.ofString("{\"cancelReason\":${tossRefundDTO.cancelReason}}"))
            .build()

        val response: HttpResponse<String> =
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

        if(response.statusCode() == HttpStatus.OK.value())
        {
            refundRepository.save(RefundEntity(userid, "toss", tossRefundDTO.paymentKey, Timestamp(System.currentTimeMillis())))
            durationRepository.save(durationIns)
            return TossRefundResultDTO(true)
        }
        else
        {
            return TossRefundResultDTO(false)
        }
    }
}