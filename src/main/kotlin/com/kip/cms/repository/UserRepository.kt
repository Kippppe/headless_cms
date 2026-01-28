package com.kip.cms.repository

import com.kip.cms.entity.User
import com.kip.cms.entity.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    /**
     * Find user by username (unique identifier)
     * @param username the username to search for
     * @return User if found, null otherwise
     */
    fun findByUsername(username: String): User?
    
    /**
     * Find user by email address (unique identifier)
     * @param email the email to search for
     * @return User if found, null otherwise
     */
    fun findByEmail(email: String): User?
    
    /**
     * Find all active users
     * @return List of all users where active = true
     */
    fun findByActiveTrue(): List<User>
    
    /**
     * Find all users with a specific role
     * @param role the role to filter by
     * @return List of users with the specified role
     */
    fun findByRole(role: UserRole): List<User>
}
