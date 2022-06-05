package com.example.anyquestion.payment.paypal

import org.springframework.stereotype.Service

import com.paypal.api.payments.*
import com.paypal.base.rest.APIContext
import com.paypal.base.rest.PayPalRESTException
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class PaypalService(private val apiContext : APIContext) {

    @kotlin.jvm.Throws(PayPalRESTException::class)
    public fun createPayment(total : Double,
                             currency : String,
                             method : String,
                             intent : String,
                             description : String,
                             cancelUrl : String,
                             successUrl : String) : Payment
    {
        var amount = Amount()
        amount.currency=currency
        var newtotal = BigDecimal(total).setScale(2, RoundingMode.HALF_UP).toDouble()
        amount.total=String.format("%.2f", newtotal)

        var transaction = Transaction()
        transaction.description=description
        transaction.amount=amount

        var transactions = mutableListOf<Transaction>()
        transactions.add(transaction)

        var payer = Payer()
        payer.paymentMethod=method

        var payment = Payment()
        payment.intent=intent
        payment.payer=payer
        payment.transactions=transactions

        var redirectUrls = RedirectUrls()
        redirectUrls.cancelUrl = cancelUrl
        redirectUrls.returnUrl = successUrl
        payment.redirectUrls = redirectUrls

        return payment.create(apiContext)
    }

    @kotlin.jvm.Throws(PayPalRESTException::class)
    public fun executePayment(paymentId : String, payerId : String) : Payment
    {
        var payment = Payment()
        payment.id=paymentId
        var paymentExecute = PaymentExecution()
        paymentExecute.payerId=payerId
        return payment.execute(apiContext, paymentExecute)
    }
}