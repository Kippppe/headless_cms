package com.kip.cms.controller

import com.kip.cms.entity.User
import com.kip.cms.Service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): User? {
        return userService.findById(id)
    }

    @GetMapping("/username/{username}")
    fun getUserByUsername(@PathVariable username: String): User?{
        return userService.findByUsername(username)
    }

    @PostMapping
    fun createUser(@RequestBody user: User): User {
        return userService.createUser(user)
    }
}