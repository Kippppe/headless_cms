package com.kip.cms.repository

import com.kip.cms.entity.ContentType
import com.kip.cms.entity.User
import com.kip.cms.entity.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.context.ActiveProfiles
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.assertThrows

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ContentTypeRepositoryTest {

    @Autowired
    private lateinit var contentTypeRepository: ContentTypeRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: User
    private lateinit var sampleFieldDefinitions: String

    @BeforeEach
    fun setUp() {
        // Clean up for isolated testing (similar to @DataJpaTest behavior)
        contentTypeRepository.deleteAll()
        userRepository.deleteAll()
        
        // Create test user
        testUser = User(
            username = "testuser",
            email = "test@example.com",
            password = "hashedpassword123",
            role = UserRole.ADMIN
        )
        testUser = userRepository.save(testUser)

        // Sample field definitions JSON
        sampleFieldDefinitions = """
            {
              "fields": [
                {
                  "name": "title",
                  "type": "text",
                  "required": true,
                  "validations": {
                    "maxLength": 200
                  }
                },
                {
                  "name": "body",
                  "type": "richtext",
                  "required": true
                },
                {
                  "name": "featured_image",
                  "type": "media",
                  "required": false
                }
              ]
            }
        """.trimIndent()
    }

    @Test
    fun `should save and retrieve content type with valid data`() {
        // Given
        val contentType = ContentType(
            name = "Blog Post",
            apiIdentifier = "blog_post",
            description = "A standard blog post content type",
            fieldDefinitions = sampleFieldDefinitions,
            active = true,
            version = 1,
            createdBy = testUser,
            updatedBy = testUser
        )

        // When
        val savedContentType = contentTypeRepository.save(contentType)

        // Then
        val retrievedContentType = contentTypeRepository.findById(savedContentType.id!!).orElse(null)
        assertThat(retrievedContentType).isNotNull
        assertThat(retrievedContentType!!.name).isEqualTo("Blog Post")
        assertThat(retrievedContentType.apiIdentifier).isEqualTo("blog_post")
        assertThat(retrievedContentType.description).isEqualTo("A standard blog post content type")
        assertThat(retrievedContentType.fieldDefinitions).isEqualTo(sampleFieldDefinitions)
        assertThat(retrievedContentType.active).isTrue()
        assertThat(retrievedContentType.version).isEqualTo(1)
        assertThat(retrievedContentType.createdBy.id).isEqualTo(testUser.id)
        assertThat(retrievedContentType.updatedBy.id).isEqualTo(testUser.id)
        assertThat(retrievedContentType.createdAt).isNotNull()
        assertThat(retrievedContentType.updatedAt).isNotNull()
    }

    @Test
    fun `should validate required name field`() {
        // Given
        val contentType = ContentType(
            name = "", // Invalid: blank name
            apiIdentifier = "blog_post",
            fieldDefinitions = sampleFieldDefinitions,
            createdBy = testUser,
            updatedBy = testUser
        )

        // When & Then
        assertThatThrownBy {
            contentTypeRepository.save(contentType)
            contentTypeRepository.flush()
        }.isInstanceOf(ConstraintViolationException::class.java)
    }

    @Test
    fun `should enforce unique content type name`() {
        // Given
        val contentType1 = ContentType(
            name = "Blog Post",
            apiIdentifier = "blog_post",
            fieldDefinitions = sampleFieldDefinitions,
            createdBy = testUser,
            updatedBy = testUser
        )
        contentTypeRepository.save(contentType1)
        contentTypeRepository.flush()

        val contentType2 = ContentType(
            name = "Blog Post", // Duplicate name
            apiIdentifier = "blog_post_2",
            fieldDefinitions = sampleFieldDefinitions,
            createdBy = testUser,
            updatedBy = testUser
        )

        // When & Then
        assertThatThrownBy {
            contentTypeRepository.save(contentType2)
            contentTypeRepository.flush()
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `should validate API identifier format`() {
        // Given
        val contentType = ContentType(
            name = "Blog Post",
            apiIdentifier = "", // Invalid: blank API identifier
            fieldDefinitions = sampleFieldDefinitions,
            createdBy = testUser,
            updatedBy = testUser
        )

        // When & Then
        assertThatThrownBy {
            contentTypeRepository.save(contentType)
            contentTypeRepository.flush()
        }.isInstanceOf(ConstraintViolationException::class.java)
    }

    @Test
    fun `should enforce unique API identifier`() {
        // Given
        val contentType1 = ContentType(
            name = "Blog Post",
            apiIdentifier = "blog_post",
            fieldDefinitions = sampleFieldDefinitions,
            createdBy = testUser,
            updatedBy = testUser
        )
        contentTypeRepository.save(contentType1)
        contentTypeRepository.flush()

        val contentType2 = ContentType(
            name = "Article",
            apiIdentifier = "blog_post", // Duplicate API identifier
            fieldDefinitions = sampleFieldDefinitions,
            createdBy = testUser,
            updatedBy = testUser
        )

        // When & Then
        assertThatThrownBy {
            contentTypeRepository.save(contentType2)
            contentTypeRepository.flush()
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `should store field definitions as JSON`() {
        // Given
        val contentType = ContentType(
            name = "Product",
            apiIdentifier = "product",
            fieldDefinitions = sampleFieldDefinitions,
            createdBy = testUser,
            updatedBy = testUser
        )

        // When
        val savedContentType = contentTypeRepository.save(contentType)

        // Then
        val retrievedContentType = contentTypeRepository.findById(savedContentType.id!!).orElse(null)
        assertThat(retrievedContentType!!.fieldDefinitions).isEqualTo(sampleFieldDefinitions)
        assertThat(retrievedContentType.fieldDefinitions).contains("title")
        assertThat(retrievedContentType.fieldDefinitions).contains("body")
        assertThat(retrievedContentType.fieldDefinitions).contains("featured_image")
    }

    @Test
    fun `should handle optional description field`() {
        // Given
        val contentTypeWithDescription = ContentType(
            name = "Blog Post",
            apiIdentifier = "blog_post_with_desc",
            description = "A blog post with description",
            fieldDefinitions = sampleFieldDefinitions,
            createdBy = testUser,
            updatedBy = testUser
        )

        val contentTypeWithoutDescription = ContentType(
            name = "Article",
            apiIdentifier = "article_no_desc",
            description = null, // Optional field
            fieldDefinitions = sampleFieldDefinitions,
            createdBy = testUser,
            updatedBy = testUser
        )

        // When
        val savedWithDescription = contentTypeRepository.save(contentTypeWithDescription)
        val savedWithoutDescription = contentTypeRepository.save(contentTypeWithoutDescription)

        // Then
        assertThat(savedWithDescription.description).isEqualTo("A blog post with description")
        assertThat(savedWithoutDescription.description).isNull()
    }

    @Test
    fun `should find content types by active status`() {
        // Given
        val activeContentType = ContentType(
            name = "Active Content Type",
            apiIdentifier = "active_type",
            fieldDefinitions = sampleFieldDefinitions,
            active = true,
            createdBy = testUser,
            updatedBy = testUser
        )

        val inactiveContentType = ContentType(
            name = "Inactive Content Type",
            apiIdentifier = "inactive_type",
            fieldDefinitions = sampleFieldDefinitions,
            active = false,
            createdBy = testUser,
            updatedBy = testUser
        )

        contentTypeRepository.save(activeContentType)
        contentTypeRepository.save(inactiveContentType)

        // When
        val activeContentTypes = contentTypeRepository.findByActiveTrue()

        // Then
        assertThat(activeContentTypes).hasSize(1)
        assertThat(activeContentTypes[0].name).isEqualTo("Active Content Type")
        assertThat(activeContentTypes[0].active).isTrue()
    }

    @Test
    fun `should find content type by API identifier`() {
        // Given
        val contentType = ContentType(
            name = "Blog Post",
            apiIdentifier = "blog_post",
            fieldDefinitions = sampleFieldDefinitions,
            createdBy = testUser,
            updatedBy = testUser
        )
        contentTypeRepository.save(contentType)

        // When
        val foundContentType = contentTypeRepository.findByApiIdentifier("blog_post")

        // Then
        assertThat(foundContentType).isNotNull
        assertThat(foundContentType!!.name).isEqualTo("Blog Post")
        assertThat(foundContentType.apiIdentifier).isEqualTo("blog_post")
    }

    @Test
    fun `should return null when content type not found by API identifier`() {
        // When
        val foundContentType = contentTypeRepository.findByApiIdentifier("non_existent")

        // Then
        assertThat(foundContentType).isNull()
    }

    @Test
    fun `should find content type by name`() {
        // Given
        val contentType = ContentType(
            name = "Blog Post",
            apiIdentifier = "blog_post",
            fieldDefinitions = sampleFieldDefinitions,
            createdBy = testUser,
            updatedBy = testUser
        )
        contentTypeRepository.save(contentType)

        // When
        val foundContentType = contentTypeRepository.findByName("Blog Post")

        // Then
        assertThat(foundContentType).isNotNull
        assertThat(foundContentType!!.name).isEqualTo("Blog Post")
        assertThat(foundContentType.apiIdentifier).isEqualTo("blog_post")
    }
}
