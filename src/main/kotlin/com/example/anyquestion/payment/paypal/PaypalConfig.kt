package com.example.anyquestion.payment.paypal

import org.springframework.context.annotation.*
import org.springframework.beans.factory.annotation.Value

import com.paypal.base.rest.*
import kotlin.collections.HashMap

@Configuration
class PaypalConfig {

    @Value("\${paypal.client.app}")
    private lateinit var clientId : String

    @Value("\${paypal.client.secret}")
    private lateinit var clientSecret : String

    @Value("\${paypal.mode}")
    private lateinit var mode : String

    @Bean
    public fun paypalSdkConfig() : HashMap<String, String>
    {
        var configMap : HashMap<String, String> = HashMap<String, String>()
        configMap.put("mode", mode)
        return configMap
    }

    @Bean
    @kotlin.jvm.Throws(PayPalRESTException::class)
    public fun apiContext() : APIContext
    {
        var context : APIContext = APIContext(clientId, clientSecret, mode)
        context.setConfigurationMap(paypalSdkConfig())
        return context
    }
}