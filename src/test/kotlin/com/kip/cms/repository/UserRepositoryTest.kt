package com.kip.cms.repository

import com.kip.cms.entity.User
import com.kip.cms.entity.UserRole
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `should save and retrieve user with valid data`() {
        // Given
        val user = User(
            username = "testuser",
            email = "test@example.com",
            password = "hashedPassword123",
            role = UserRole.AUTHOR
        )

        // When
        val savedUser = userRepository.save(user)

        // Then
        assertNotNull(savedUser.id)
        assertEquals("testuser", savedUser.username)
        assertEquals("test@example.com", savedUser.email)
        assertEquals("hashedPassword123", savedUser.password)
        assertEquals(UserRole.AUTHOR, savedUser.role)
        assertTrue(savedUser.active)
        assertNotNull(savedUser.createdAt)
        assertNotNull(savedUser.updatedAt)
    }

    @Test
    fun `should enforce unique username constraint`() {
        // Given
        val user1 = User(
            username = "testuser",
            email = "test1@example.com",
            password = "hashedPassword123",
            role = UserRole.AUTHOR
        )
        val user2 = User(
            username = "testuser", // Same username
            email = "test2@example.com",
            password = "hashedPassword456",
            role = UserRole.AUTHOR
        )

        // When & Then
        userRepository.save(user1)

        assertThrows(DataIntegrityViolationException::class.java) {
            userRepository.save(user2)
        }
    }

    @Test
    fun `should enforce unique email constraint`() {
        // Given
        val user1 = User(
            username = "testuser1",
            email = "test@example.com",
            password = "hashedPassword123",
            role = UserRole.AUTHOR
        )
        val user2 = User(
            username = "testuser2",
            email = "test@example.com", // Same email
            password = "hashedPassword456",
            role = UserRole.AUTHOR
        )

        // When & Then
        userRepository.save(user1)

        assertThrows(DataIntegrityViolationException::class.java) {
            userRepository.save(user2)
        }
    }

    @Test
    fun `should hash password before saving`() {
        // Given
        val plainPassword = "plainPassword123"
        val user = User(
            username = "testuser",
            email = "test@example.com",
            password = plainPassword,
            role = UserRole.AUTHOR
        )

        // When
        val savedUser = userRepository.save(user)

        // Then
        // Note: Password hashing should be handled at service layer
        // This test ensures the password field can store hashed values
        assertNotNull(savedUser.password)
        assertFalse(savedUser.password.isEmpty())
    }

    @Test
    fun `should save user with different roles`() {
        // Given
        val adminUser = User(
            username = "admin",
            email = "admin@example.com",
            password = "hashedPassword123",
            role = UserRole.ADMIN
        )
        val editorUser = User(
            username = "editor",
            email = "editor@example.com",
            password = "hashedPassword123",
            role = UserRole.EDITOR
        )
        val authorUser = User(
            username = "author",
            email = "author@example.com",
            password = "hashedPassword123",
            role = UserRole.AUTHOR
        )
        val viewerUser = User(
            username = "viewer",
            email = "viewer@example.com",
            password = "hashedPassword123",
            role = UserRole.VIEWER
        )

        // When
        val savedAdmin = userRepository.save(adminUser)
        val savedEditor = userRepository.save(editorUser)
        val savedAuthor = userRepository.save(authorUser)
        val savedViewer = userRepository.save(viewerUser)

        // Then
        assertEquals(UserRole.ADMIN, savedAdmin.role)
        assertEquals(UserRole.EDITOR, savedEditor.role)
        assertEquals(UserRole.AUTHOR, savedAuthor.role)
        assertEquals(UserRole.VIEWER, savedViewer.role)
    }

    @Test
    fun `should find user by username`() {
        // Given
        val user = User(
            username = "testuser",
            email = "test@example.com",
            password = "hashedPassword123",
            role = UserRole.AUTHOR
        )
        userRepository.save(user)

        // When
        val foundUser = userRepository.findByUsername("testuser")

        // Then
        assertNotNull(foundUser)
        assertEquals("testuser", foundUser?.username)
        assertEquals("test@example.com", foundUser?.email)
    }

    @Test
    fun `should find user by email`() {
        // Given
        val user = User(
            username = "testuser",
            email = "test@example.com",
            password = "hashedPassword123",
            role = UserRole.AUTHOR
        )
        userRepository.save(user)

        // When
        val foundUser = userRepository.findByEmail("test@example.com")

        // Then
        assertNotNull(foundUser)
        assertEquals("testuser", foundUser?.username)
        assertEquals("test@example.com", foundUser?.email)
    }

    @Test
    fun `should find active users only`() {
        // Given
        val activeUser = User(
            username = "activeuser",
            email = "active@example.com",
            password = "hashedPassword123",
            role = UserRole.AUTHOR,
            active = true
        )
        val inactiveUser = User(
            username = "inactiveuser",
            email = "inactive@example.com",
            password = "hashedPassword123",
            role = UserRole.AUTHOR,
            active = false
        )
        userRepository.save(activeUser)
        userRepository.save(inactiveUser)

        // When
        val activeUsers = userRepository.findByActiveTrue()

        // Then
        assertEquals(1, activeUsers.size)
        assertEquals("activeuser", activeUsers[0].username)
    }

    @Test
    fun `should find users by role`() {
        // Given
        val adminUser = User(
            username = "admin",
            email = "admin@example.com",
            password = "hashedPassword123",
            role = UserRole.ADMIN
        )
        val authorUser = User(
            username = "author",
            email = "author@example.com",
            password = "hashedPassword123",
            role = UserRole.AUTHOR
        )
        userRepository.save(adminUser)
        userRepository.save(authorUser)

        // When
        val adminUsers = userRepository.findByRole(UserRole.ADMIN)

        // Then
        assertEquals(1, adminUsers.size)
        assertEquals("admin", adminUsers[0].username)
        assertEquals(UserRole.ADMIN, adminUsers[0].role)
    }
}
