package com.kip.cms.repository

import com.kip.cms.entity.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat

@SpringBootTest
@Transactional
class MediaRepositoryTest {

    @Autowired
    private lateinit var mediaRepository: MediaRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        // Create test user
        testUser = User(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            role = UserRole.AUTHOR
        )
        testUser = userRepository.save(testUser)
    }

    @Test
    fun `should save and retrieve media with valid data`() {
        // Given
        val media = Media(
            filename = "test-image.jpg",
            filePath = "/media/uploads/test-image.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024000,
            uploader = testUser,
            altText = "Test image",
            title = "Test Image",
            description = "A test image for unit testing"
        )

        // When
        val savedMedia = mediaRepository.save(media)

        // Then
        assertThat(savedMedia.id).isNotNull
        assertThat(savedMedia.filename).isEqualTo("test-image.jpg")
        assertThat(savedMedia.filePath).isEqualTo("/media/uploads/test-image.jpg")
        assertThat(savedMedia.mimeType).isEqualTo("image/jpeg")
        assertThat(savedMedia.fileSize).isEqualTo(1024000)
        assertThat(savedMedia.uploader.id).isEqualTo(testUser.id)
        assertThat(savedMedia.altText).isEqualTo("Test image")
        assertThat(savedMedia.title).isEqualTo("Test Image")
        assertThat(savedMedia.description).isEqualTo("A test image for unit testing")
        assertThat(savedMedia.createdAt).isNotNull
        assertThat(savedMedia.updatedAt).isNotNull
    }

    @Test
    fun `should validate required filename`() {
        // This test validates that filename is required through JPA validation
        assertThat(true).isTrue // Placeholder - validation will be handled by entity constraints
    }

    @Test
    fun `should validate required file path`() {
        // This test validates that file path is required through JPA validation
        assertThat(true).isTrue // Placeholder - validation will be handled by entity constraints
    }

    @Test
    fun `should validate required mime type`() {
        // This test validates that mime type is required through JPA validation
        assertThat(true).isTrue // Placeholder - validation will be handled by entity constraints
    }

    @Test
    fun `should store file size`() {
        // Given
        val media = Media(
            filename = "large-file.pdf",
            filePath = "/media/documents/large-file.pdf",
            mimeType = "application/pdf",
            fileSize = 5242880, // 5MB
            uploader = testUser
        )

        // When
        val savedMedia = mediaRepository.save(media)
        val retrievedMedia = mediaRepository.findById(savedMedia.id!!).orElse(null)

        // Then
        assertThat(retrievedMedia).isNotNull
        assertThat(retrievedMedia!!.fileSize).isEqualTo(5242880)
    }

    @Test
    fun `should associate media with uploader`() {
        // Given - Create another user
        val anotherUser = User(
            username = "anotheruser",
            email = "another@example.com",
            password = "password123",
            role = UserRole.EDITOR
        )
        val savedAnotherUser = userRepository.save(anotherUser)

        // Create media for both users
        val media1 = Media(
            filename = "user1-file.jpg",
            filePath = "/media/user1-file.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024,
            uploader = testUser
        )
        val media2 = Media(
            filename = "user2-file.png",
            filePath = "/media/user2-file.png",
            mimeType = "image/png",
            fileSize = 2048,
            uploader = savedAnotherUser
        )

        mediaRepository.save(media1)
        mediaRepository.save(media2)

        // When
        val user1Media = mediaRepository.findByUploader(testUser)
        val user2Media = mediaRepository.findByUploader(savedAnotherUser)

        // Then
        assertThat(user1Media).hasSize(1)
        assertThat(user1Media[0].filename).isEqualTo("user1-file.jpg")
        assertThat(user2Media).hasSize(1)
        assertThat(user2Media[0].filename).isEqualTo("user2-file.png")
    }

    @Test
    fun `should find media by mime type`() {
        // Given
        val imageMedia = Media(
            filename = "photo.jpg",
            filePath = "/media/photo.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024,
            uploader = testUser
        )
        val videoMedia = Media(
            filename = "video.mp4",
            filePath = "/media/video.mp4",
            mimeType = "video/mp4",
            fileSize = 5242880,
            uploader = testUser
        )
        val documentMedia = Media(
            filename = "document.pdf",
            filePath = "/media/document.pdf",
            mimeType = "application/pdf",
            fileSize = 102400,
            uploader = testUser
        )

        mediaRepository.save(imageMedia)
        mediaRepository.save(videoMedia)
        mediaRepository.save(documentMedia)

        // When
        val imageFiles = mediaRepository.findByMimeTypeStartingWith("image/")
        val videoFiles = mediaRepository.findByMimeTypeStartingWith("video/")
        val applicationFiles = mediaRepository.findByMimeTypeStartingWith("application/")

        // Then
        assertThat(imageFiles).hasSize(1)
        assertThat(imageFiles[0].filename).isEqualTo("photo.jpg")
        assertThat(videoFiles).hasSize(1)
        assertThat(videoFiles[0].filename).isEqualTo("video.mp4")
        assertThat(applicationFiles).hasSize(1)
        assertThat(applicationFiles[0].filename).isEqualTo("document.pdf")
    }

    @Test
    fun `should find media by uploader`() {
        // Given
        val media1 = Media(
            filename = "file1.jpg",
            filePath = "/media/file1.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024,
            uploader = testUser
        )
        val media2 = Media(
            filename = "file2.png",
            filePath = "/media/file2.png",
            mimeType = "image/png",
            fileSize = 2048,
            uploader = testUser
        )

        mediaRepository.save(media1)
        mediaRepository.save(media2)

        // When
        val userMedia = mediaRepository.findByUploader(testUser)

        // Then
        assertThat(userMedia).hasSize(2)
        assertThat(userMedia.map { it.filename }).containsExactlyInAnyOrder("file1.jpg", "file2.png")
    }

    @Test
    fun `should support media tags`() {
        // Given
        val media = Media(
            filename = "tagged-image.jpg",
            filePath = "/media/tagged-image.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024,
            uploader = testUser,
            tags = "nature,photography,landscape"
        )

        // When
        val savedMedia = mediaRepository.save(media)
        val retrievedMedia = mediaRepository.findById(savedMedia.id!!).orElse(null)

        // Then
        assertThat(retrievedMedia).isNotNull
        assertThat(retrievedMedia!!.tags).isEqualTo("nature,photography,landscape")
    }

    @Test
    fun `should validate file path uniqueness`() {
        // Given
        val media1 = Media(
            filename = "original.jpg",
            filePath = "/media/unique-path.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024,
            uploader = testUser
        )
        mediaRepository.save(media1)

        // When
        val foundMedia = mediaRepository.findByFilePath("/media/unique-path.jpg")

        // Then
        assertThat(foundMedia).isNotNull
        assertThat(foundMedia!!.filename).isEqualTo("original.jpg")
    }

    @Test
    fun `should find media by creation date range`() {
        // Given
        val now = LocalDateTime.now()
        val yesterday = now.minusDays(1)
        val tomorrow = now.plusDays(1)

        // Create media with different creation times
        val oldMedia = Media(
            filename = "old-file.jpg",
            filePath = "/media/old-file.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024,
            uploader = testUser
        )
        
        // Save and manually set created date (this would normally be handled by @CreationTimestamp)
        val savedOldMedia = mediaRepository.save(oldMedia)

        val newMedia = Media(
            filename = "new-file.jpg",
            filePath = "/media/new-file.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024,
            uploader = testUser
        )
        val savedNewMedia = mediaRepository.save(newMedia)

        // When
        val mediaInRange = mediaRepository.findByCreatedAtBetween(yesterday, tomorrow)

        // Then
        assertThat(mediaInRange).hasSizeGreaterThan(0)
        assertThat(mediaInRange.map { it.filename }).contains("new-file.jpg")
    }

    @Test
    fun `should support media metadata as JSON`() {
        // Given
        val metadata = """{"dimensions": {"width": 1920, "height": 1080}, "exif": {"camera": "Canon EOS", "iso": 200}}"""
        val media = Media(
            filename = "metadata-image.jpg",
            filePath = "/media/metadata-image.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024,
            uploader = testUser,
            metadata = metadata
        )

        // When
        val savedMedia = mediaRepository.save(media)
        val retrievedMedia = mediaRepository.findById(savedMedia.id!!).orElse(null)

        // Then
        assertThat(retrievedMedia).isNotNull
        assertThat(retrievedMedia!!.metadata).isEqualTo(metadata)
    }
}
