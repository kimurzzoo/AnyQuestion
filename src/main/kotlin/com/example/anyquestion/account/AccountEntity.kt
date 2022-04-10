package com.example.anyquestion.account

import javax.persistence.*
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.GrantedAuthority

@Entity
@Table(name = "account")
class User(name : String, email : String, m_password : String) : UserDetails
{
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long? = null

    @Column(name = "name", nullable = false)
    var name: String = name

    @Column(name = "email", unique = true, nullable = false)
    var email : String = email

    @Column(name = "password", nullable = false)
    var m_password : String = m_password

    override fun getAuthorities(): MutableCollection<out GrantedAuthority>? {
        return null
    }

    override fun getPassword(): String {
        return m_password
    }

    override fun getUsername(): String {
        return email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}

@Entity
@Table(name = "refresh")
class RefreshToken(id : Long, refreshToken : String)
{
    @Id
    @Column(name = "id", unique = true, nullable = false)
    var id : Long = id

    @Column(name = "refreshtoken", nullable = false)
    var refreshToken : String = refreshToken
}

@Entity
@Table(name = "blacklist")
class ExpiredToken(expiredToken : String, userId : Long)
{
    @Id
    @Column(name = "expiredtoken", unique = true, nullable = false)
    var expiredToken = expiredToken

    @Column(name="id", nullable = false)
    var userId = userId
}