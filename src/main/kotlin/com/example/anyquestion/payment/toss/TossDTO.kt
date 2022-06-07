package com.example.anyquestion.payment.toss

data class TossRefundDTO(
    val paymentKey : String,
    val cancelReason : String
)

data class TossRefundResultDTO(
    val ok : Boolean
)