package com.example.anyquestion.account

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: JpaRepository<User, Long> {
    fun findByEmail(username : String) : User

    fun existsByEmail(email : String) : Boolean
}

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long>
{
    fun existsByRefreshToken(token : String) : Boolean
    fun deleteByRefreshToken(token : String)
}

@Repository
interface BlacklistRepository : JpaRepository<ExpiredToken, String>
{
}