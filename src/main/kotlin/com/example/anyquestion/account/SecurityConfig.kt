package com.example.anyquestion.account

import org.springframework.context.annotation.*
import org.springframework.security.config.annotation.web.configuration.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.authentication.*
import org.springframework.security.config.annotation.web.builders.*
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@EnableWebSecurity
class SecurityConfig(private val jwtTokenProvider: JwtTokenProvider,
                        private val blacklistRepository : BlacklistRepository): WebSecurityConfigurerAdapter() {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    override fun configure(http: HttpSecurity){
        http.
            httpBasic().disable() // rest api만 고려, 기본 설정 해제
            .csrf().disable() // csrf 보안 토큰 disable 처리
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 토큰 기반 인증이므로 세션 사용 안함
            .and()
            .authorizeRequests() // 요청에 대한 사용권한 체크
            .antMatchers("/auth/login", "/auth/register").permitAll() // 로그인, 회원가입은 누구나 접근 가능
            .antMatchers("/payment/paypal/success", "/payment/paypal/cancel").permitAll()
            .antMatchers("/payment/toss/success", "/payment/toss/fail").permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(JwtAuthenticationFilter(jwtTokenProvider, blacklistRepository), UsernamePasswordAuthenticationFilter::class.java)
    }
}