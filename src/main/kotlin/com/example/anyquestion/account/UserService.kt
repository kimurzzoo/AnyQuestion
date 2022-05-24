package com.example.anyquestion.account

import org.springframework.stereotype.Service
import org.springframework.security.authentication.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull
import com.example.anyquestion.secret.Secret

@Service
class UserService(private val userRepository : UserRepository,
                    private val refreshTokenRepository : RefreshTokenRepository,
                    private val blacklistRepository : BlacklistRepository,
                    private val authenticationManager : AuthenticationManager,
                    private val jwtTokenProvider : JwtTokenProvider)
{
    @Autowired
    lateinit var passwordEncoder : PasswordEncoder

    @Transactional
    fun login(userDTO : UserDTO) : TokenDTO
    {
        try
        {
            println("로그인 중")
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(userDTO.email, userDTO.password, null)
            )
        }
        catch (e: BadCredentialsException) {
            throw BadCredentialsException("로그인 실패")
        }

        var userId = userRepository.findByEmail(userDTO.email).id

        val accessToken = jwtTokenProvider.createToken(userId.toString())
        val refreshToken = jwtTokenProvider.createRefreshToken()

        refreshTokenRepository.save(RefreshToken(userId!!, refreshToken))

        return TokenDTO(accessToken, refreshToken)
    }

    @Transactional
    fun register(accountDTO : AccountDTO) : RegisterResultDTO
    {
        val registerresult = RegisterResultDTO(false)

        if(accountDTO.password.length < 10)
            return registerresult

        if(!accountDTO.password.equals(accountDTO.passwordConfirm))
            return registerresult

        if(accountDTO.name.length < 2)
            return registerresult

        val email_regex = Secret.email_regex

        if(!accountDTO.email.matches(email_regex))
        {
            return registerresult
        }

        if(userRepository.existsByEmail(accountDTO.email))
            return registerresult

        val user = User(accountDTO.name, accountDTO.email, passwordEncoder.encode(accountDTO.password))
        userRepository.save(user)

        registerresult.ok = true

        return registerresult
    }

    @Transactional
    fun reissue(tokenReissueDTO : TokenReissueDTO) : TokenDTO
    {
        if(tokenReissueDTO.refreshToken == null)
        {
            throw RuntimeException("refreshtoken isnt valid")
        }
        else
        {
            if(!jwtTokenProvider.validateToken(tokenReissueDTO.refreshToken))
            {
                if(refreshTokenRepository.existsByRefreshToken(tokenReissueDTO.refreshToken))
                {
                    refreshTokenRepository.deleteByRefreshToken(tokenReissueDTO.refreshToken)
                }
            }
        }

        var userId = SecurityUtil.getCurrentUserId().toLong()

        val refreshToken = refreshTokenRepository.findById(userId!!)?: throw RuntimeException("logout user")

        if(!refreshToken.get().refreshToken.equals(tokenReissueDTO.refreshToken))
        {
            throw RuntimeException("token doesnt fit")
        }

        val newAccessToken = jwtTokenProvider.createToken(userId.toString())
        val newRefreshToken = jwtTokenProvider.createRefreshToken()

        refreshTokenRepository.save(RefreshToken(userId, newRefreshToken))

        return TokenDTO(newAccessToken, newRefreshToken)
    }

    @Transactional
    fun logout(token : String) : LogoutDTO
    {
        var userId = SecurityUtil.getCurrentUserId().toLong()

        blacklistRepository.save(ExpiredToken(token, userId!!))
        refreshTokenRepository.deleteById(userId!!)

        return LogoutDTO(true)
    }

    @Transactional
    fun withdrawal() : WithdrawalDTO
    {
        var userid = SecurityUtil.getCurrentUserId().toLong()

        userRepository.deleteById(userid)

        return WithdrawalDTO(true)
    }
}