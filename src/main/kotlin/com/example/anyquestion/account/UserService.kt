package com.example.anyquestion.account

import org.springframework.stereotype.Service
import org.springframework.security.authentication.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.mail.javamail.*
import org.springframework.mail.SimpleMailMessage
import com.example.anyquestion.secret.Secret
import java.util.Properties
import java.security.SecureRandom
import javax.annotation.PostConstruct

@Service
class UserService(private val userRepository : UserRepository,
                    private val refreshTokenRepository : RefreshTokenRepository,
                    private val blacklistRepository : BlacklistRepository,
                    private val authenticationManager : AuthenticationManager,
                    private val jwtTokenProvider : JwtTokenProvider,
                    private val passwordEncoder : PasswordEncoder)
{
    private var mailSender = JavaMailSenderImpl()
    val myEmail = "kimurzzzoo@gmail.com"

    @PostConstruct
    fun mailSenderInit()
    {
        mailSender.setHost("smtp.gmail.com")
        mailSender.setPort(587)
        mailSender.setDefaultEncoding("utf-8")
        mailSender.setUsername(myEmail)
        mailSender.setPassword("vrcfazgfvfxlnshs")

        var javaMailProperties = Properties()
        javaMailProperties.setProperty("mail.transport.protocol", "smtp")
        javaMailProperties.setProperty("mail.smtp.auth", "true")
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true")
        javaMailProperties.setProperty("mail.debug", "debug")
        javaMailProperties.setProperty("mail.smtp.ssl.trust", "smtp.gmail.com")
        mailSender.setJavaMailProperties(javaMailProperties)
    }

    @Transactional
    fun login(userDTO : UserDTO) : TokenDTO
    {
        var userId = userRepository.findByEmail(userDTO.email).id

        try
        {
            println("로그인 중")
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(userId.toString(), userDTO.password, null)
            )
        }
        catch (e: BadCredentialsException) {
            throw BadCredentialsException("로그인 실패")
        }

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

        var userId = SecurityUtil.getCurrentUserId()

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
        var userId = SecurityUtil.getCurrentUserId()

        blacklistRepository.save(ExpiredToken(token, userId!!))
        refreshTokenRepository.deleteById(userId!!)

        return LogoutDTO(true)
    }

    @Transactional
    fun withdrawal() : WithdrawalDTO
    {
        var userid = SecurityUtil.getCurrentUserId()

        userRepository.deleteById(userid)

        return WithdrawalDTO(true)
    }

    @Transactional
    fun tempPassword(emailDTO : EmailDTO) : TempPasswordDTO
    {
        if(userRepository.existsByEmail(emailDTO.email))
        {
            val rnd = SecureRandom()
            var tempPassword = ""
            var lettertype : Int

            for(i : Int in 1..10)
            {
                lettertype = rnd.nextInt(2)
                when (lettertype)
                {
                    0 -> tempPassword += (rnd.nextInt(26) + 65).toChar()
                    1 -> tempPassword += (rnd.nextInt(26) + 97).toChar()
                    2 -> tempPassword += (rnd.nextInt(10)).toString()
                }
            }

            val user = userRepository.findByEmail(emailDTO.email)
            user.m_password = passwordEncoder.encode(tempPassword)
            userRepository.save(user)

            var smm = SimpleMailMessage()
            smm.setFrom(myEmail)
            smm.setTo(emailDTO.email)
            smm.setSubject("Temporal password - AnyQuestion")
            smm.setText("This is your temporal password.\nPlease change your password after login\n" + tempPassword)

            mailSender.send(smm)

            return TempPasswordDTO(true)
        }
        else
        {
            return TempPasswordDTO(false)
        }
    }

    @Transactional
    fun changePassword(changePasswordDTO: ChangePasswordDTO) : ChangePasswordResultDTO
    {
        val userId = SecurityUtil.getCurrentUserId()
        var nowuser = userRepository.findById(userId).get()

        if(passwordEncoder.matches(changePasswordDTO.nowPassword, nowuser.m_password)
            && changePasswordDTO.newPassword.equals(changePasswordDTO.newPassword_confirm)
            && changePasswordDTO.newPassword.length >= 10) {

            nowuser.m_password = passwordEncoder.encode(changePasswordDTO.newPassword)
            userRepository.save(nowuser)
            return ChangePasswordResultDTO(true)
        }
        else
        {
            return ChangePasswordResultDTO(false)
        }
    }
}