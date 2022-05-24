package com.example.anyquestion.account

import org.springframework.security.core.*
import org.springframework.security.core.context.SecurityContextHolder

class SecurityUtil
{
    companion object
    {
        fun getCurrentUserId() : String
        {
            val authentication : Authentication = SecurityContextHolder.getContext().getAuthentication()

            if(authentication == null || authentication.getName() == null)
            {
                throw RuntimeException("SecurityContext에 인증정보가 없습니다")
            }

            return authentication.getName()
        }
    }
}