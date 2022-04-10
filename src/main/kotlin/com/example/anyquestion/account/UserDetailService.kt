package com.example.anyquestion.account

import org.springframework.stereotype.Service
import org.springframework.security.core.userdetails.*

@Service
class UserDetailService(private val userRepository : UserRepository) : UserDetailsService
{
    override fun loadUserByUsername(username : String) : UserDetails
    {
        return userRepository.findByEmail(username) ?: throw UsernameNotFoundException("존재하지 않는 username 입니다.")
    }
}