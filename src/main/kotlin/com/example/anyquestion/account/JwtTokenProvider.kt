package com.example.anyquestion.account

import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import java.util.*
import io.jsonwebtoken.security.SignatureException
import io.jsonwebtoken.*
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import javax.servlet.http.HttpServletRequest
import com.example.anyquestion.secret.Secret

@Component
class JwtTokenProvider(private val userDetailsService: UserDetailsService)
{
    private var secretKey = Secret.secretKey

    private val tokenValidTime = 30 * 60 * 1000L
    private val refreshTokenValidTime = 60 * 60 * 24 * 7 * 1000L

    @PostConstruct
    protected fun init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.toByteArray())
    }

    // JWT 토큰 생성
    fun createToken(userPk: String): String {
        val claims: Claims = Jwts.claims().setSubject(userPk) // JWT payload 에 저장되는 정보단위
        claims["userPk"] = userPk // 정보는 key / value 쌍으로 저장된다.
        val now = Date()
        return Jwts.builder()
            .setHeaderParam("typ", "JWT")
            .setClaims(claims) // 정보 저장
            .setIssuedAt(now) // 토큰 발행 시간 정보
            .setExpiration(Date(now.time + tokenValidTime)) // set Expire Time
            .signWith(SignatureAlgorithm.HS256, secretKey) // 사용할 암호화 알고리즘과
            // signature 에 들어갈 secret값 세팅
            .compact()
    }

    fun createRefreshToken() : String
    {
        val now = Date()

        return Jwts.builder()
                .setExpiration(Date(now.time + refreshTokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // JWT 토큰에서 인증 정보 조회
    fun getAuthentication(token: String): Authentication {
        val userDetails = userDetailsService.loadUserByUsername(getUserPk(token))
        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }


    // 토큰에서 회원 정보 추출
    fun getUserPk(token: String): String {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body.subject
    }

    // Request의 Header에서 token 값을 가져옵니다. "X-AUTH-TOKEN" : "TOKEN값'
    fun resolveToken(request: HttpServletRequest): String? {
        return request.getHeader("Authorization")
    }

    // 토큰의 유효성 + 만료일자 확인
    fun validateToken(jwtToken: String): Boolean {
        return try {
            val claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken)
            !claims.body.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }
}