package com.example.anyquestion.account

import org.springframework.stereotype.Service
import org.springframework.security.authentication.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull

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
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(userDTO.email, userDTO.password, null)
            )
        }
        catch (e: BadCredentialsException) {
            throw BadCredentialsException("로그인 실패")
        }

        val accessToken = jwtTokenProvider.createToken(userDTO.email)
        val refreshToken = jwtTokenProvider.createRefreshToken()

        var userId = userRepository.findByEmail(userDTO.email).id

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

        var userEmail = SecurityUtil.getCurrentUserEmail()
        var userId = userRepository.findByEmail(userEmail).id

        val refreshToken = refreshTokenRepository.findById(userId!!)?: throw RuntimeException("logout user")

        if(!refreshToken.get().refreshToken.equals(tokenReissueDTO.refreshToken))
        {
            throw RuntimeException("token doesnt fit")
        }

        val newAccessToken = jwtTokenProvider.createToken(userEmail)
        val newRefreshToken = jwtTokenProvider.createRefreshToken()

        refreshTokenRepository.save(RefreshToken(userId, newRefreshToken))

        return TokenDTO(newAccessToken, newRefreshToken)
    }

    @Transactional
    fun logout() : LogoutDTO
    {
        var userEmail = SecurityUtil.getCurrentUserEmail()
        var userId = userRepository.findByEmail(userEmail).id

        blacklistRepository.save(ExpiredToken(SecurityUtil.nowAccessToken!!, userId!!))
        refreshTokenRepository.deleteById(userId!!)

        return LogoutDTO(true)
    }

    @Transactional
    fun withdrawal() : WithdrawalDTO
    {
        var userEmail = SecurityUtil.getCurrentUserEmail()
        var userId = userRepository.findByEmail(userEmail).id

        userRepository.deleteById(userId!!)

        return WithdrawalDTO(true)
    }
}