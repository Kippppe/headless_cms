package com.kip.cms.Service

import com.kip.cms.entity.User
import com.kip.cms.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun createUser(user: User): User {
        validateUniqueUsername(user.username)
        validateUniqueUserEmail(user.email)

        val hashedPassword = passwordEncoder.encode(user.password)
        val userWithHashedPassword = user.copy(password = hashedPassword)

        return userRepository.save(userWithHashedPassword)
    }

    fun findById(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }

    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    private fun validateUniqueUsername(username: String) {
        if (userRepository.findByUsername(username) != null) {
            throw IllegalArgumentException("Username '$username' is already taken")
        }
    }

    private fun validateUniqueUserEmail(email: String) {
        if (userRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("Email '$email' is already registered")
        }
    }
}