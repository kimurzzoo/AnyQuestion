package com.example.anyquestion.payment.paypal

data class PaypalSubmitDTO(
    val merNum : Int
)

data class PaypalSuccessDTO(
    val ok : Boolean
)

data class PaypalRefundDTO(
    val ok : Boolean
)