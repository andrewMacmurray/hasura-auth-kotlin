package com.andrew.gymserver.auth

import FindUserQuery
import com.andrew.gymserver.utils.pipe
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {
    @GetMapping("/do-something")
    suspend fun doSomething(): FindUserQuery.User? {
        return getAUser()
    }

    @GetMapping("/create-user")
    suspend fun createUser(): UserDetails? {
        return createAUser(
            CreateUserDetails(
                username = "askdj",
                email = "b@c.com",
                firstName = "Random",
                secondName = "Person",
                passwordHash = "aksjdhaskdjhasjkd"
            )
        )?.pipe { UserDetails(it.id, it.username, it.email) }
    }
}
