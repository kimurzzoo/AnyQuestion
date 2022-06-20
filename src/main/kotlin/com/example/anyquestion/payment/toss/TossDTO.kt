package com.example.anyquestion.payment.toss

data class TossSubmitDTO(
    val merNum : Int
)
data class TossRefundDTO(
    val paymentKey : String,
    val cancelReason : String
)

data class TossRefundResultDTO(
    val ok : Boolean
)