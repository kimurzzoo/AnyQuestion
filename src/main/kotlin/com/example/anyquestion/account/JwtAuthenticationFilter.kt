package com.example.anyquestion.account

import org.springframework.web.filter.GenericFilterBean
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import java.io.IOException


class JwtAuthenticationFilter(private val jwtTokenProvider: JwtTokenProvider,
                                private val blacklistRepository : BlacklistRepository): GenericFilterBean() {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        // 헤더에서 JWT 를 받아옵니다.
        val token: String? = jwtTokenProvider.resolveToken((request as HttpServletRequest))
        // 유효한 토큰인지 확인합니다.
        if (token != null && jwtTokenProvider.validateToken(token) && !blacklistRepository.existsById(token)) {
            // 토큰이 유효하면 토큰으로부터 유저 정보를 받아옵니다.
            SecurityUtil.nowAccessToken = token
            val authentication = jwtTokenProvider.getAuthentication(token)
            // SecurityContext 에 Authentication 객체를 저장합니다.
            SecurityContextHolder.getContext().authentication = authentication
        }
        chain.doFilter(request, response)
    }
}