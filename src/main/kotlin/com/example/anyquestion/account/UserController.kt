package com.example.anyquestion.account


import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/auth")
class UserController(private val userService : UserService)
{
    @PostMapping("/login")
    fun login(@RequestBody userDTO : UserDTO) : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(userService.login(userDTO))
    }

    @PostMapping("/register")
    fun register(@RequestBody accountDTO : AccountDTO) : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(userService.register(accountDTO))
    }

    @PostMapping("/reissue")
    fun reissue(@RequestBody tokenReissueDTO : TokenReissueDTO) : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(userService.reissue(tokenReissueDTO))
    }

    @GetMapping("/logout")
    fun logout() : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(userService.logout())
    }

    @GetMapping("/withdrawal")
    fun withdrawal() : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(userService.withdrawal())
    }
}