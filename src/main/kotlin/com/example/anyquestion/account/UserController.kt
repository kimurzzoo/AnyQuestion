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
    fun logout(@RequestHeader(value = "Authorization") token : String) : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(userService.logout(token))
    }

    @GetMapping("/withdrawal")
    fun withdrawal() : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(userService.withdrawal())
    }

    @PostMapping("/forgotpassword")
    fun forgotpassword(@RequestBody emailDTO : EmailDTO) : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(userService.tempPassword(emailDTO))
    }

    @PostMapping("/changepassword")
    fun changepassword(@RequestBody changePasswordDTO: ChangePasswordDTO) : ResponseEntity<*>
    {
        return ResponseEntity.ok().body(userService.changePassword(changePasswordDTO))
    }
}