package com.andrew.gymserver.auth

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {
    @PostMapping("/signup")
    fun signup(@RequestBody signUpRequest: SignUpRequest) {

    }
}

