package com.example.anyquestion.payment

import com.example.anyquestion.payment.Merchandise.Companion.durationList
import java.sql.Timestamp
import java.util.*
import javax.persistence.*

@Table(name="payment")
@Entity
class PaymentEntity(userid : Long, method : String, paymentid : String, merid : Int, paymentdate : Timestamp) {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long? = null

    @Column(name="userid", nullable = false)
    var userid : Long = userid

    @Column(name = "method", nullable = false)
    var method : String = method

    @Column(name="paymentid", nullable = false)
    var paymentid : String = paymentid

    @Column(name = "merid", nullable = false)
    var merid : Int = merid

    @Column(name = "paymentdate", nullable = false)
    var paymentdate : Timestamp = paymentdate
}

@Table(name="duration")
@Entity
class DurationEntity(userid : Long, expireddate : Timestamp){
    @Id
    @Column(name = "userid", nullable = false)
    var userid : Long = userid

    @Column(name = "expireddate", nullable = false)
    var expireddate : Timestamp = expireddate

    fun isRefundable(merid : Int) : Boolean
    {
        var cal = Calendar.getInstance()
        cal.time = expireddate
        cal.add(durationList[merid][0], (-1) * durationList[merid][1])

        var nowcal = Calendar.getInstance()
        nowcal.time = Date()
        return cal.after(nowcal)
    }

    fun isBefore(nowdate : Calendar) : Boolean
    {
        var cal = Calendar.getInstance()
        cal.time=expireddate

        return cal.before(nowdate)
    }

    fun add(merId : Int)
    {
        var cal = Calendar.getInstance()
        cal.time = expireddate
        cal.add(durationList[merId][0], durationList[merId][0])
        expireddate.time = cal.time.time
    }

    fun subtract(merId : Int)
    {
        var cal = Calendar.getInstance()
        cal.time = expireddate
        cal.add(durationList[merId][0], (-1) * durationList[merId][0])
        expireddate.time = cal.time.time
    }
}

@Table(name="refund")
@Entity
class RefundEntity(userid : Long, method : String, paymentid : String, refunddate : Timestamp){
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long? = null

    @Column(name="userid", nullable = false)
    var userid : Long = userid

    @Column(name = "method", nullable = false)
    var method : String = method

    @Column(name="paymentid", nullable = false)
    var paymentid : String = paymentid

    @Column(name = "refunddate", nullable = false)
    var refunddate : Timestamp = refunddate
}

class Merchandise{
    companion object{
        val methodList = listOf("paypal", "toss")
        val merchandiseList = listOf(1.00, 10.00)
        val durationList = listOf(listOf(Calendar.MONTH, 1), listOf(Calendar.YEAR, 1))
        val descriptionList = listOf("0", "1")
    }
}