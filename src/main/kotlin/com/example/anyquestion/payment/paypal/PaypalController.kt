package com.example.anyquestion.payment.paypal

import com.example.anyquestion.account.SecurityUtil.Companion.getCurrentUserId
import com.example.anyquestion.account.UserRepository
import com.example.anyquestion.payment.*
import com.example.anyquestion.payment.Merchandise.Companion.descriptionList
import com.example.anyquestion.payment.Merchandise.Companion.durationList
import com.example.anyquestion.payment.Merchandise.Companion.merchandiseList
import com.paypal.api.payments.Amount
import com.paypal.api.payments.Links
import com.paypal.api.payments.RefundRequest
import com.paypal.api.payments.Sale
import com.paypal.base.rest.PayPalRESTException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.util.*

@RequestMapping("/payment/paypal")
@Controller
class PaypalController(private val paypalService: PaypalService,
                       private val paypalConfig: PaypalConfig,
                       private val paymentRepository: PaymentRepository,
                       private val durationRepository: DurationRepository,
                       private val refundRepository: RefundRepository,
                       private val userRepository: UserRepository)
{

    @Value("\${server.address}")
    val addr : String = ""

    @Value("\${server.port}")
    val port : String = ""

    @PostMapping("/submit")
    public fun payment(@RequestBody paypalSubmitDTO: PaypalSubmitDTO) : String
    {
        val successUrl = "http://$addr:$port/payment/paypal/cancel"
        val cancelUrl = "http://$addr:$port/payment/paypal/success"

        try
        {
            val payment = paypalService.createPayment(merchandiseList[paypalSubmitDTO.merNum],
            "USD",
            "paypal",
            "sale",
            descriptionList[paypalSubmitDTO.merNum],
            cancelUrl,
                successUrl)

            for (link : Links in payment.links)
            {
                if(link.rel.equals("approval_url"))
                {
                    return "redirect:" + link.href
                }
            }
        }
        catch (e : PayPalRESTException)
        {
            e.printStackTrace()
        }
        return "redirect:/"
    }

    @ResponseBody
    @GetMapping("/cancel")
    fun cancelPay() : String
    {
        return "canceled"
    }

    @Transactional
    @ResponseBody
    @GetMapping("/success")
    fun successPay(@RequestParam("paymentId") paymentId : String, @RequestParam("PayerID") payerId : String) : ResponseEntity<*>
    {
        try {
            var payment = paypalService.executePayment(paymentId, payerId)
            if(payment.state.equals("approved"))
            {
                val userid = getCurrentUserId(userRepository)
                val merId = payment.transactions.get(0).description.toInt()
                paymentRepository.save(PaymentEntity(userid,
                    "paypal",
                    payment.transactions.get(0).relatedResources.get(0).sale.id,
                    merId,
                    Timestamp(System.currentTimeMillis())
                ))

                var durationInc = durationRepository.findById(userid).get()
                var cal = Calendar.getInstance()
                cal.time= Date()
                if(durationInc.isBefore(cal))
                {
                    cal.add(durationList[merId][0], durationList[merId][1])
                    durationInc.expireddate.time = cal.time.time
                }
                else
                {
                    durationInc.add(merId)
                }

                durationRepository.save(durationInc)
                return ResponseEntity.ok().body(PaypalSuccessDTO(true))
            }
        }
        catch (e : PayPalRESTException)
        {
            e.printStackTrace()
        }
        return ResponseEntity.ok().body(PaypalSuccessDTO(false))
    }

    @Transactional
    @ResponseBody
    @GetMapping("/refund")
    fun refund(sale_id : String) : PaypalRefundDTO
    {
        val userid = getCurrentUserId(userRepository)
        if(!paymentRepository.existsByMethodAndPaymentid("paypal", sale_id))
            return PaypalRefundDTO(false)

        val paymentIns = paymentRepository.findByMethodAndPaymentid("paypal", sale_id)

        if(!userid.equals(paymentIns.userid))
            return PaypalRefundDTO(false)

        val durationIns = durationRepository.findById(paymentIns.userid).get()

        if(!durationIns.isRefundable(paymentIns.merid))
            return PaypalRefundDTO(false)
        else
            durationIns.subtract(paymentIns.merid)

        try
        {
            var amount = Amount()
            amount.total= merchandiseList[paymentIns.merid].toString()
            amount.currency="USD"

            var sale = Sale()
            sale.id=sale_id

            var refund = RefundRequest()
            refund.amount=amount

            var returnRefund = sale.refund(paypalConfig.apiContext(), refund)
            println(returnRefund)

            refundRepository.save(RefundEntity(userid, "paypal", sale_id, Timestamp(System.currentTimeMillis())))
            durationRepository.save(durationIns)

            return PaypalRefundDTO(true)
        }
        catch (e : PayPalRESTException)
        {
            println(e.message)
        }

        return PaypalRefundDTO(false)
    }
}