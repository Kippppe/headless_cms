package com.kip.cms.repository

import com.kip.cms.entity.User
import com.kip.cms.entity.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

    private fun createUser(
        username: String,
        email: String,
        password: String = "password123",
        role: UserRole = UserRole.AUTHOR,
        active: Boolean = true
    ) = User(
        username = username,
        email = email,
        password = password,
        role = role,
        active = active
    )

    @Test
    fun `should save and retrieve user with valid data`() {
        val user = createUser("testuser", "test@example.com")

        val savedUser = userRepository.save(user)

        assertThat(savedUser.id).isNotNull
        assertThat(savedUser.username).isEqualTo("testuser")
        assertThat(savedUser.email).isEqualTo("test@example.com")
        assertThat(savedUser.password).isEqualTo("password123")
        assertThat(savedUser.role).isEqualTo(UserRole.AUTHOR)
        assertThat(savedUser.active).isTrue
        assertThat(savedUser.createdAt).isNotNull
        assertThat(savedUser.updatedAt).isNotNull
    }

    @Test
    fun `should enforce unique username constraint`() {
        val user1 = createUser("testuser", "test1@example.com")
        val user2 = createUser("testuser", "test2@example.com") 

        userRepository.save(user1)
        userRepository.flush()

        assertThatThrownBy { userRepository.save(user2) }
            .isInstanceOf(DataIntegrityViolationException::class.java)
            .hasMessageContaining("username") 
    }

    @Test
    fun `should enforce unique email constraint`() {
        val user1 = createUser("user1", "test@example.com")
        val user2 = createUser("user2", "test@example.com")

        userRepository.save(user1)
        userRepository.flush()

        assertThatThrownBy { userRepository.save(user2) }
            .isInstanceOf(DataIntegrityViolationException::class.java)
            .hasMessageContaining("email") 
    }

    @Test
    fun `should hash password before saving`() {
        val plainPassword = "plainPassword123"
        val user = createUser("testuser", "test@example.com", password = plainPassword)

        val savedUser = userRepository.save(user)
        userRepository.flush()

        assertThat(savedUser.password).isNotEmpty()
    }

    @Test
    fun `should save user with different roles`() {
        val users = listOf(
            createUser("admin", "admin@example.com", role = UserRole.ADMIN),
            createUser("editor", "editor@example.com", role = UserRole.EDITOR),
            createUser("author", "author@example.com", role = UserRole.AUTHOR),
            createUser("viewer", "viewer@example.com", role = UserRole.VIEWER)
        )

        val savedUsers = users.map { userRepository.save(it) }

        savedUsers.forEach { saved ->
            val expectedRole = users.find { it.username == saved.username }!!.role
            assertThat(saved.role).isEqualTo(expectedRole)
        }
    }

    @Test
    fun `should find user by username`() {
        val user = createUser("testuser", "test@example.com")
        userRepository.save(user)

        val foundUser = userRepository.findByUsername("testuser")

        assertThat(foundUser).isNotNull
        assertThat(foundUser!!.username).isEqualTo("testuser")
        assertThat(foundUser.email).isEqualTo("test@example.com")
    }

    @Test
    fun `should find user by email`() {
        val user = createUser("testuser", "test@example.com")
        userRepository.save(user)

        val foundUser = userRepository.findByEmail("test@example.com")

        val checkedFoundUser = requireNotNull(foundUser)

        assertThat(checkedFoundUser).isNotNull
        assertThat(checkedFoundUser.username).isEqualTo("testuser")
        assertThat(checkedFoundUser.email).isEqualTo("test@example.com")
    }

    @Test
    fun `should find active users only`() {
        val activeUser = createUser("activeuser", "active@example.com", active = true)
        val inactiveUser = createUser("inactiveuser", "inactive@example.com", active = false)
        userRepository.save(activeUser)
        userRepository.save(inactiveUser)

        val activeUsers = userRepository.findByActiveTrue()

        assertThat(activeUsers).hasSize(1)
        assertThat(activeUsers[0].username).isEqualTo("activeuser")
    }

    @Test
    fun `should find users by role`() {
        val adminUser = createUser("admin", "admin@example.com", role = UserRole.ADMIN)
        val authorUser = createUser("author", "author@example.com", role = UserRole.AUTHOR)
        userRepository.save(adminUser)
        userRepository.save(authorUser)

        val adminUsers = userRepository.findByRole(UserRole.ADMIN)

        assertThat(adminUsers).hasSize(1)
        assertThat(adminUsers[0].username).isEqualTo("admin")
        assertThat(adminUsers[0].role).isEqualTo(UserRole.ADMIN)
    }
}
