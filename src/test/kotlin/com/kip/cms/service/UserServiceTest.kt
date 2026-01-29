package com.kip.cms.service

import com.kip.cms.Service.UserService
import com.kip.cms.entity.User
import com.kip.cms.entity.UserRole
import com.kip.cms.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    private lateinit var userService: UserService

    private lateinit var testUser: User
    private lateinit var savedUser: User

    @BeforeEach
    fun setUp() {
        testUser = User(
            id = null,
            username = "testuser",
            email = "test@example.com",
            password = "plainPassword123",
            role = UserRole.AUTHOR,
            active = true
        )

        savedUser = testUser.copy(
            id = 1L,
            password = "hashedPassword123"
        )
    }

    // ==================== createUser Tests ====================

    @Test
    fun `createUser should hash password and save user with valid data`() {
        // Given
        val hashedPassword = "hashedPassword123"
        whenever(userRepository.findByUsername(testUser.username)).thenReturn(null)
        whenever(userRepository.findByEmail(testUser.email)).thenReturn(null)
        whenever(passwordEncoder.encode(testUser.password)).thenReturn(hashedPassword)
        whenever(userRepository.save(any())).thenReturn(savedUser)

        // When
        val result = userService.createUser(testUser)

        // Then
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(1L)
        assertThat(result.username).isEqualTo("testuser")
        assertThat(result.email).isEqualTo("test@example.com")
        assertThat(result.password).isEqualTo(hashedPassword)
        
        verify(userRepository).findByUsername(testUser.username)
        verify(userRepository).findByEmail(testUser.email)
        verify(passwordEncoder).encode(testUser.password)
        verify(userRepository).save(any())
    }

    @Test
    fun `createUser should throw IllegalArgumentException when username already exists`() {
        // Given
        val existingUser = testUser.copy(id = 2L)
        whenever(userRepository.findByUsername(testUser.username)).thenReturn(existingUser)

        // When & Then
        assertThatThrownBy { userService.createUser(testUser) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Username 'testuser' is already taken")

        verify(userRepository).findByUsername(testUser.username)
        verify(userRepository, never()).findByEmail(any())
        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `createUser should throw IllegalArgumentException when email already exists`() {
        // Given
        val existingUser = testUser.copy(id = 2L, username = "differentuser")
        whenever(userRepository.findByUsername(testUser.username)).thenReturn(null)
        whenever(userRepository.findByEmail(testUser.email)).thenReturn(existingUser)

        // When & Then
        assertThatThrownBy { userService.createUser(testUser) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Email 'test@example.com' is already registered")

        verify(userRepository).findByUsername(testUser.username)
        verify(userRepository).findByEmail(testUser.email)
        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `createUser should validate username before email`() {
        // Given
        val existingUserWithSameUsername = testUser.copy(id = 2L)
        whenever(userRepository.findByUsername(testUser.username)).thenReturn(existingUserWithSameUsername)

        // When & Then
        assertThatThrownBy { userService.createUser(testUser) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Username")

        verify(userRepository).findByUsername(testUser.username)
        verify(userRepository, never()).findByEmail(any())
    }

    @Test
    fun `createUser should create user with different roles`() {
        // Given
        val adminUser = testUser.copy(username = "admin", email = "admin@example.com", role = UserRole.ADMIN)
        val hashedPassword = "hashedPassword"
        val savedAdminUser = adminUser.copy(id = 1L, password = hashedPassword)
        
        whenever(userRepository.findByUsername(adminUser.username)).thenReturn(null)
        whenever(userRepository.findByEmail(adminUser.email)).thenReturn(null)
        whenever(passwordEncoder.encode(adminUser.password)).thenReturn(hashedPassword)
        whenever(userRepository.save(any())).thenReturn(savedAdminUser)

        // When
        val result = userService.createUser(adminUser)

        // Then
        assertThat(result.role).isEqualTo(UserRole.ADMIN)
        verify(userRepository).save(any())
    }

    @Test
    fun `createUser should create inactive user`() {
        // Given
        val inactiveUser = testUser.copy(active = false)
        val hashedPassword = "hashedPassword"
        val savedInactiveUser = inactiveUser.copy(id = 1L, password = hashedPassword)
        
        whenever(userRepository.findByUsername(inactiveUser.username)).thenReturn(null)
        whenever(userRepository.findByEmail(inactiveUser.email)).thenReturn(null)
        whenever(passwordEncoder.encode(inactiveUser.password)).thenReturn(hashedPassword)
        whenever(userRepository.save(any())).thenReturn(savedInactiveUser)

        // When
        val result = userService.createUser(inactiveUser)

        // Then
        assertThat(result.active).isFalse()
        verify(userRepository).save(any())
    }

    // ==================== findById Tests ====================

    @Test
    fun `findById should return user when user exists`() {
        // Given
        val userId = 1L
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(savedUser))

        // When
        val result = userService.findById(userId)

        // Then
        assertThat(result).isNotNull
        assertThat(result?.id).isEqualTo(userId)
        assertThat(result?.username).isEqualTo("testuser")
        assertThat(result?.email).isEqualTo("test@example.com")
        
        verify(userRepository).findById(userId)
    }

    @Test
    fun `findById should return null when user does not exist`() {
        // Given
        val userId = 999L
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        // When
        val result = userService.findById(userId)

        // Then
        assertThat(result).isNull()
        verify(userRepository).findById(userId)
    }

    @Test
    fun `findById should handle zero id`() {
        // Given
        val userId = 0L
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        // When
        val result = userService.findById(userId)

        // Then
        assertThat(result).isNull()
        verify(userRepository).findById(userId)
    }

    @Test
    fun `findById should handle negative id`() {
        // Given
        val userId = -1L
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        // When
        val result = userService.findById(userId)

        // Then
        assertThat(result).isNull()
        verify(userRepository).findById(userId)
    }

    // ==================== findByUsername Tests ====================

    @Test
    fun `findByUsername should return user when username exists`() {
        // Given
        val username = "testuser"
        whenever(userRepository.findByUsername(username)).thenReturn(savedUser)

        // When
        val result = userService.findByUsername(username)

        // Then
        assertThat(result).isNotNull
        assertThat(result?.username).isEqualTo(username)
        assertThat(result?.email).isEqualTo("test@example.com")
        
        verify(userRepository).findByUsername(username)
    }

    @Test
    fun `findByUsername should return null when username does not exist`() {
        // Given
        val username = "nonexistent"
        whenever(userRepository.findByUsername(username)).thenReturn(null)

        // When
        val result = userService.findByUsername(username)

        // Then
        assertThat(result).isNull()
        verify(userRepository).findByUsername(username)
    }

    @Test
    fun `findByUsername should handle empty username`() {
        // Given
        val username = ""
        whenever(userRepository.findByUsername(username)).thenReturn(null)

        // When
        val result = userService.findByUsername(username)

        // Then
        assertThat(result).isNull()
        verify(userRepository).findByUsername(username)
    }

    @Test
    fun `findByUsername should be case sensitive`() {
        // Given
        val username = "TestUser"
        val lowerCaseUsername = "testuser"
        whenever(userRepository.findByUsername(username)).thenReturn(null)
        whenever(userRepository.findByUsername(lowerCaseUsername)).thenReturn(savedUser)

        // When
        val resultUpperCase = userService.findByUsername(username)
        val resultLowerCase = userService.findByUsername(lowerCaseUsername)

        // Then
        assertThat(resultUpperCase).isNull()
        assertThat(resultLowerCase).isNotNull
        
        verify(userRepository).findByUsername(username)
        verify(userRepository).findByUsername(lowerCaseUsername)
    }

//     // ==================== Integration/Edge Case Tests ====================

//     @Test
//     fun `createUser should handle special characters in email`() {
//         // Given
//         val userWithSpecialEmail = testUser.copy(
//             username = "specialuser",
//             email = "special+test@example.co.uk"
//         )
//         val hashedPassword = "hashedPassword"
//         val savedSpecialUser = userWithSpecialEmail.copy(id = 1L, password = hashedPassword)
        
//         whenever(userRepository.findByUsername(userWithSpecialEmail.username)).thenReturn(null)
//         whenever(userRepository.findByEmail(userWithSpecialEmail.email)).thenReturn(null)
//         whenever(passwordEncoder.encode(userWithSpecialEmail.password)).thenReturn(hashedPassword)
//         whenever(userRepository.save(any())).thenReturn(savedSpecialUser)

//         // When
//         val result = userService.createUser(userWithSpecialEmail)

//         // Then
//         assertThat(result.email).isEqualTo("special+test@example.co.uk")
//         verify(userRepository).save(any())
//     }

//     @Test
//     fun `createUser should preserve all user properties except password`() {
//         // Given
//         val userWithAllProperties = User(
//             id = null,
//             username = "fulluser",
//             email = "full@example.com",
//             password = "originalPassword",
//             role = UserRole.EDITOR,
//             active = false
//         )
//         val hashedPassword = "hashedPassword"
//         val savedFullUser = userWithAllProperties.copy(id = 1L, password = hashedPassword)
        
//         whenever(userRepository.findByUsername(userWithAllProperties.username)).thenReturn(null)
//         whenever(userRepository.findByEmail(userWithAllProperties.email)).thenReturn(null)
//         whenever(passwordEncoder.encode(userWithAllProperties.password)).thenReturn(hashedPassword)
//         whenever(userRepository.save(any())).thenReturn(savedFullUser)

//         // When
//         val result = userService.createUser(userWithAllProperties)

//         // Then
//         assertThat(result.username).isEqualTo("fulluser")
//         assertThat(result.email).isEqualTo("full@example.com")
//         assertThat(result.password).isEqualTo(hashedPassword)
//         assertThat(result.role).isEqualTo(UserRole.EDITOR)
//         assertThat(result.active).isFalse()
        
//         verify(passwordEncoder).encode("originalPassword")
//     }

//     @Test
//     fun `createUser should call passwordEncoder exactly once`() {
//         // Given
//         whenever(userRepository.findByUsername(testUser.username)).thenReturn(null)
//         whenever(userRepository.findByEmail(testUser.email)).thenReturn(null)
//         whenever(passwordEncoder.encode(testUser.password)).thenReturn("hashedPassword")
//         whenever(userRepository.save(any())).thenReturn(savedUser)

//         // When
//         userService.createUser(testUser)

//         // Then
//         verify(passwordEncoder, times(1)).encode(testUser.password)
//     }

//     @Test
//     fun `findById should return users with different roles`() {
//         // Given
//         val adminUser = savedUser.copy(id = 1L, role = UserRole.ADMIN)
//         val editorUser = savedUser.copy(id = 2L, role = UserRole.EDITOR)
//         val viewerUser = savedUser.copy(id = 3L, role = UserRole.VIEWER)
        
//         whenever(userRepository.findById(1L)).thenReturn(Optional.of(adminUser))
//         whenever(userRepository.findById(2L)).thenReturn(Optional.of(editorUser))
//         whenever(userRepository.findById(3L)).thenReturn(Optional.of(viewerUser))

//         // When
//         val admin = userService.findById(1L)
//         val editor = userService.findById(2L)
//         val viewer = userService.findById(3L)

//         // Then
//         assertThat(admin?.role).isEqualTo(UserRole.ADMIN)
//         assertThat(editor?.role).isEqualTo(UserRole.EDITOR)
//         assertThat(viewer?.role).isEqualTo(UserRole.VIEWER)
//     }
// }
