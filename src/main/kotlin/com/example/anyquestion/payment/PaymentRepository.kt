package com.example.anyquestion.payment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<PaymentEntity, Long> {
    fun findByMethodAndPaymentid(method : String, paymentid : String) : PaymentEntity
    fun existsByMethodAndPaymentid(method : String, paymentid : String) : Boolean
}

@Repository
interface DurationRepository : JpaRepository<DurationEntity, Long>{

}

@Repository
interface RefundRepository : JpaRepository<RefundEntity, Long>{

}