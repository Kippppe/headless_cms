# Test-Driven Development Guide for Headless CMS

## Overview

This guide provides a step-by-step approach to building a Headless CMS using Test-Driven Development (TDD). We'll follow the Red-Green-Refactor cycle and build incrementally from core entities to full REST APIs. A headless CMS separates content management from content presentation, providing content via APIs for consumption by any frontend.

## Development Order

1. **User Entity** (Foundation - Authentication & Authorization)
2. **ContentType Entity** (Schema Definition)
3. **Content Entity** (Core Business Logic)
4. **Media Entity** (Asset Management)
5. **Repository Layer** (Data Access)
6. **Service Layer** (Business Logic)
7. **REST API Layer** (Web Interface)

---

## Phase 1: User Entity Development

### 1.1 Create User Repository Test

#### File: `src/test/kotlin/com/headlesscms/repository/UserRepositoryTest.kt`

**Step 1: Write the first failing test**

```kotlin
@DataJpaTest
class UserRepositoryTest {

    @Test
    fun `should save and retrieve user with valid data`() {
        // This test will fail initially - write it first!
    }
}
```

**Test Cases to Implement (in order):**

1. `should save and retrieve user with valid data`
2. `should validate required username field`
3. `should validate required email field`
4. `should enforce unique username constraint`
5. `should enforce unique email constraint`
6. `should hash password before saving`
7. `should save user with different roles`
8. `should find user by username`
9. `should find user by email`

### 1.2 Create User Entity

#### File: `src/main/kotlin/com/headlesscms/entity/User.kt`

**Step 2: Create minimal entity to pass first test**

- Start with basic fields: id, username, email, password
- Add JPA annotations
- Include validation annotations
- Implement password hashing

**Business Rules:**

- Username: Required, unique, 3-50 characters, alphanumeric with underscores
- Email: Required, unique, valid format
- Password: Required, min 8 characters, hashed (BCrypt)
- Role: ADMIN, EDITOR, AUTHOR, VIEWER (default: AUTHOR)
- Active status: Boolean (default: true)
- Created/Updated timestamps

### 1.3 Create User Repository Interface

#### File: `src/main/kotlin/com/headlesscms/repository/UserRepository.kt`

**Step 3: Create repository interface**

```kotlin
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun findByActiveTrue(): List<User>
    fun findByRole(role: UserRole): List<User>
}
```

### 1.4 TDD Cycle for User

**Red-Green-Refactor Steps:**

1. **Red**: Write failing test
2. **Green**: Write minimal code to pass
3. **Refactor**: Improve code quality
4. Repeat for each test case

---

## Phase 2: ContentType Entity Development

### 2.1 Create ContentType Repository Test

#### File: `src/test/kotlin/com/headlesscms/repository/ContentTypeRepositoryTest.kt`

**Test Cases:**

1. `should save and retrieve content type with valid data`
2. `should validate required name field`
3. `should enforce unique content type name`
4. `should validate API identifier format`
5. `should enforce unique API identifier`
6. `should store field definitions as JSON`
7. `should handle optional description field`
8. `should find content types by active status`

### 2.2 Create ContentType Entity

#### File: `src/main/kotlin/com/headlesscms/entity/ContentType.kt`

**Business Rules:**

- Name: Required, unique, 2-100 characters (e.g., "Blog Post", "Product")
- API Identifier: Required, unique, lowercase with underscores (e.g., "blog_post")
- Description: Optional, max 500 characters
- Field Definitions: JSON structure defining content fields
- Active status: Boolean (default: true)
- Versioning: Version number for schema changes
- Created/Updated by User
- Timestamps

**Field Definition Structure (JSON):**

```json
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
```

### 2.3 Create ContentType Repository

#### File: `src/main/kotlin/com/headlesscms/repository/ContentTypeRepository.kt`

```kotlin
interface ContentTypeRepository : JpaRepository<ContentType, Long> {
    fun findByApiIdentifier(apiIdentifier: String): ContentType?
    fun findByActiveTrue(): List<ContentType>
    fun findByName(name: String): ContentType?
}
```

---

## Phase 3: Content Entity Development

### 3.1 Create Content Repository Test

#### File: `src/test/kotlin/com/headlesscms/repository/ContentRepositoryTest.kt`

**Test Cases:**

1. `should save and retrieve content with valid data`
2. `should validate required content type association`
3. `should validate required author association`
4. `should store content data as JSON`
5. `should support content status transitions`
6. `should find content by status`
7. `should find content by content type`
8. `should find content by author`
9. `should support content versioning`
10. `should find published content only`
11. `should filter content by publish date range`
12. `should support content slug uniqueness within content type`

### 3.2 Create Content Entity

#### File: `src/main/kotlin/com/headlesscms/entity/Content.kt`

**Business Rules:**

- Content Type: Required (ManyToOne relationship)
- Author: Required (ManyToOne relationship to User)
- Status: DRAFT, PUBLISHED, ARCHIVED, SCHEDULED (default: DRAFT)
- Content Data: JSON object matching ContentType field definitions
- Slug: Required, URL-friendly, unique within content type
- Publish Date: Optional, for scheduling
- Version: Integer for content versioning
- Metadata: JSON for SEO and custom fields
- Created/Updated timestamps
- Published/Archived timestamps

**Content Data Structure (JSON):**

```json
{
  "title": "Getting Started with Headless CMS",
  "body": "<p>Rich text content here...</p>",
  "featured_image": "/media/abc123.jpg",
  "tags": ["cms", "tutorial"]
}
```

### 3.3 Create Content Repository

#### File: `src/main/kotlin/com/headlesscms/repository/ContentRepository.kt`

**Custom Query Methods:**

```kotlin
interface ContentRepository : JpaRepository<Content, Long> {
    fun findByContentType(contentType: ContentType): List<Content>
    fun findByAuthor(author: User): List<Content>
    fun findByStatus(status: ContentStatus): List<Content>
    fun findByContentTypeAndStatus(contentType: ContentType, status: ContentStatus): List<Content>
    fun findByContentTypeAndSlug(contentType: ContentType, slug: String): Content?
    fun findByStatusAndPublishDateBefore(status: ContentStatus, date: LocalDateTime): List<Content>
    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<Content>
}
```

---

## Phase 4: Media Entity Development

### 4.1 Create Media Repository Test

#### File: `src/test/kotlin/com/headlesscms/repository/MediaRepositoryTest.kt`

**Test Cases:**

1. `should save and retrieve media with valid data`
2. `should validate required filename`
3. `should validate required file path`
4. `should validate required mime type`
5. `should store file size`
6. `should associate media with uploader`
7. `should find media by mime type`
8. `should find media by uploader`
9. `should support media tags`
10. `should validate file path uniqueness`

### 4.2 Create Media Entity

#### File: `src/main/kotlin/com/headlesscms/entity/Media.kt`

**Business Rules:**

- Filename: Required, original filename
- File Path: Required, unique, storage path/URL
- Mime Type: Required (e.g., "image/jpeg", "application/pdf")
- File Size: Required (in bytes)
- Uploader: Required (ManyToOne relationship to User)
- Alt Text: Optional, for accessibility
- Title: Optional
- Description: Optional
- Tags: Optional, for organization
- Metadata: JSON for EXIF, dimensions, etc.
- Created/Updated timestamps

### 4.3 Create Media Repository

#### File: `src/main/kotlin/com/headlesscms/repository/MediaRepository.kt`

```kotlin
interface MediaRepository : JpaRepository<Media, Long> {
    fun findByUploader(uploader: User): List<Media>
    fun findByMimeTypeStartingWith(mimeTypePrefix: String): List<Media>
    fun findByFilePath(filePath: String): Media?
    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<Media>
}
```

---

## Phase 5: Repository Layer Testing

### 5.1 Integration Tests

#### File: `src/test/kotlin/com/headlesscms/repository/IntegrationRepositoryTest.kt`

**Test Cases:**

1. `should handle user-content relationships correctly`
2. `should handle content type-content relationships correctly`
3. `should cascade delete content when content type is deleted`
4. `should support complex queries across entities`
5. `should maintain referential integrity`
6. `should handle content versioning correctly`
7. `should support full-text search on content`

### 5.2 Configuration for Tests

#### File: `src/test/resources/application-test.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
```

---

## Phase 6: Service Layer Development

Following TDD principles, we write tests first, then implement the services to make them pass. The service layer contains business logic and coordinates between the repository layer and controllers.

### 6.1 User Service Tests

#### File: `src/test/kotlin/com/headlesscms/service/UserServiceTest.kt`

**Key Test Cases Implemented:**

1. **User Creation & Authentication**

   - ✅ `should create user successfully with hashed password`
   - ✅ `should throw exception when creating user with duplicate username`
   - ✅ `should throw exception when creating user with duplicate email`
   - ✅ `should authenticate user with correct credentials`
   - ✅ `should fail authentication with incorrect password`

2. **User Retrieval**

   - ✅ `should find user by id successfully`
   - ✅ `should return null when user not found by id`
   - ✅ `should find all active users`
   - ✅ `should find users by role`

3. **User Management**
   - ✅ `should update user profile successfully`
   - ✅ `should update user password with validation`
   - ✅ `should deactivate user successfully` (soft delete)
   - ✅ `should validate role changes based on permissions`

**Testing Approach:**

```kotlin
@Mock
private lateinit var userRepository: UserRepository
@Mock
private lateinit var passwordEncoder: PasswordEncoder
private lateinit var userService: UserService

@BeforeEach
fun setUp() {
    MockitoAnnotations.openMocks(this)
    userService = UserService(userRepository, passwordEncoder)
}
```

### 6.2 ContentType Service Tests

#### File: `src/test/kotlin/com/headlesscms/service/ContentTypeServiceTest.kt`

**Key Test Cases Implemented:**

1. **Content Type Creation**

   - ✅ `should create content type successfully`
   - ✅ `should throw exception when creating content type with duplicate name`
   - ✅ `should throw exception when creating content type with duplicate API identifier`
   - ✅ `should validate field definitions structure`
   - ✅ `should sanitize API identifier to lowercase with underscores`

2. **Content Type Management**

   - ✅ `should update content type schema with version increment`
   - ✅ `should validate field type changes for existing content`
   - ✅ `should find content type by API identifier`
   - ✅ `should list all active content types`

3. **Schema Validation**
   - ✅ `should validate required field definitions`
   - ✅ `should validate field type enum values`
   - ✅ `should validate field name uniqueness within schema`

### 6.3 Content Service Tests

#### File: `src/test/kotlin/com/headlesscms/service/ContentServiceTest.kt`

**Key Test Cases Implemented:**

1. **Content Creation & Validation**

   - ✅ `should create content successfully`
   - ✅ `should throw exception when creating content with non-existent content type`
   - ✅ `should throw exception when creating content with non-existent author`
   - ✅ `should validate content data against content type schema`
   - ✅ `should generate unique slug if not provided`
   - ✅ `should throw exception for duplicate slug within same content type`

2. **Content Status Management**

   - ✅ `should publish content successfully`
   - ✅ `should schedule content for future publication`
   - ✅ `should archive content successfully`
   - ✅ `should throw exception when publishing content with validation errors`
   - ✅ `should validate status transitions`

3. **Content Retrieval & Filtering**

   - ✅ `should find content by id successfully`
   - ✅ `should find content by content type and status`
   - ✅ `should find content by slug within content type`
   - ✅ `should retrieve published content only for public API`
   - ✅ `should support pagination for content listing`

4. **Content Versioning**
   - ✅ `should create new version when updating published content`
   - ✅ `should maintain version history`
   - ✅ `should retrieve specific content version`
   - ✅ `should rollback to previous version`

**Business Logic Rules Enforced:**

- Content data must match ContentType field definitions
- Required fields must be present
- Field types must match schema
- Slug must be unique within content type
- Only valid status transitions allowed (DRAFT → PUBLISHED, PUBLISHED → ARCHIVED, etc.)
- Content cannot be published without required fields
- Scheduled content automatically published at publish date
- Version increments on each update to published content

### 6.4 Media Service Tests

#### File: `src/test/kotlin/com/headlesscms/service/MediaServiceTest.kt`

**Key Test Cases Implemented:**

1. **Media Upload & Storage**

   - ✅ `should upload media successfully`
   - ✅ `should validate file type against allowed mime types`
   - ✅ `should validate file size limits`
   - ✅ `should generate unique file path`
   - ✅ `should extract metadata from uploaded file`

2. **Media Management**

   - ✅ `should update media metadata successfully`
   - ✅ `should delete media and associated file`
   - ✅ `should find media by uploader`
   - ✅ `should filter media by type (images, documents, videos)`

3. **Image Processing**
   - ✅ `should generate thumbnails for images`
   - ✅ `should extract image dimensions`
   - ✅ `should optimize image file size`

### 6.5 Service Layer Implementation

#### File: `src/main/kotlin/com/headlesscms/service/UserService.kt`

**Key Features:**

```kotlin
@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun createUser(user: User): User {
        // Validate unique username and email
        validateUniqueUsername(user.username)
        validateUniqueEmail(user.email)

        // Hash password
        val hashedPassword = passwordEncoder.encode(user.password)
        val userWithHashedPassword = user.copy(password = hashedPassword)

        return userRepository.save(userWithHashedPassword)
    }

    fun authenticateUser(username: String, password: String): User? {
        val user = userRepository.findByUsername(username) ?: return null
        return if (passwordEncoder.matches(password, user.password)) user else null
    }

    // Soft delete implementation
    fun deactivateUser(id: Long): Boolean {
        val user = userRepository.findById(id).orElse(null) ?: return false
        val deactivatedUser = user.copy(active = false)
        userRepository.save(deactivatedUser)
        return true
    }
}
```

#### File: `src/main/kotlin/com/headlesscms/service/ContentTypeService.kt`

**Key Features:**

```kotlin
@Service
class ContentTypeService(
    private val contentTypeRepository: ContentTypeRepository,
    private val contentRepository: ContentRepository
) {

    fun createContentType(contentType: ContentType): ContentType {
        // Validate unique name and API identifier
        validateUniqueName(contentType.name)
        validateUniqueApiIdentifier(contentType.apiIdentifier)

        // Sanitize API identifier
        val sanitizedIdentifier = sanitizeApiIdentifier(contentType.apiIdentifier)

        // Validate field definitions
        validateFieldDefinitions(contentType.fieldDefinitions)

        val sanitizedContentType = contentType.copy(
            apiIdentifier = sanitizedIdentifier,
            version = 1
        )

        return contentTypeRepository.save(sanitizedContentType)
    }

    fun updateContentTypeSchema(id: Long, newFieldDefinitions: String): ContentType {
        val contentType = contentTypeRepository.findById(id).orElseThrow()

        // Validate new schema
        validateFieldDefinitions(newFieldDefinitions)

        // Check impact on existing content
        validateSchemaChangeImpact(contentType, newFieldDefinitions)

        // Increment version
        val updatedContentType = contentType.copy(
            fieldDefinitions = newFieldDefinitions,
            version = contentType.version + 1
        )

        return contentTypeRepository.save(updatedContentType)
    }

    private fun sanitizeApiIdentifier(identifier: String): String {
        return identifier.lowercase().replace(Regex("[^a-z0-9]"), "_")
    }
}
```

#### File: `src/main/kotlin/com/headlesscms/service/ContentService.kt`

**Key Features:**

```kotlin
@Service
class ContentService(
    private val contentRepository: ContentRepository,
    private val contentTypeRepository: ContentTypeRepository,
    private val userRepository: UserRepository
) {

    fun createContent(content: Content): Content {
        // Validate content type and author exist
        validateContentTypeExists(content.contentType.id)
        validateAuthorExists(content.author.id)

        // Validate content data against schema
        validateContentData(content.contentType, content.contentData)

        // Generate slug if not provided
        val slug = content.slug ?: generateSlug(content.contentData)

        // Validate unique slug within content type
        validateUniqueSlug(content.contentType, slug)

        val contentWithSlug = content.copy(
            slug = slug,
            version = 1,
            status = ContentStatus.DRAFT
        )

        return contentRepository.save(contentWithSlug)
    }

    fun publishContent(id: Long, publishDate: LocalDateTime? = null): Content {
        val content = contentRepository.findById(id).orElseThrow()

        // Validate content is ready for publishing
        validateContentForPublishing(content)

        val status = if (publishDate != null && publishDate.isAfter(LocalDateTime.now())) {
            ContentStatus.SCHEDULED
        } else {
            ContentStatus.PUBLISHED
        }

        val publishedContent = content.copy(
            status = status,
            publishDate = publishDate ?: LocalDateTime.now()
        )

        return contentRepository.save(publishedContent)
    }

    fun updateContent(id: Long, newContentData: String): Content {
        val content = contentRepository.findById(id).orElseThrow()

        // Validate new content data
        validateContentData(content.contentType, newContentData)

        // If content is published, create new version
        val newVersion = if (content.status == ContentStatus.PUBLISHED) {
            content.version + 1
        } else {
            content.version
        }

        val updatedContent = content.copy(
            contentData = newContentData,
            version = newVersion,
            updatedAt = LocalDateTime.now()
        )

        return contentRepository.save(updatedContent)
    }

    private fun validateContentData(contentType: ContentType, contentData: String) {
        val schema = parseFieldDefinitions(contentType.fieldDefinitions)
        val data = parseContentData(contentData)

        // Validate required fields
        schema.fields.filter { it.required }.forEach { field ->
            if (!data.containsKey(field.name)) {
                throw IllegalArgumentException("Required field '${field.name}' is missing")
            }
        }

        // Validate field types
        data.forEach { (fieldName, value) ->
            val fieldDef = schema.fields.find { it.name == fieldName }
                ?: throw IllegalArgumentException("Unknown field: $fieldName")

            validateFieldType(fieldDef, value)
        }
    }

    private fun generateSlug(contentData: String): String {
        val data = parseContentData(contentData)
        val title = data["title"]?.toString() ?: "untitled"
        return title.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }
}
```

#### File: `src/main/kotlin/com/headlesscms/service/MediaService.kt`

**Key Features:**

```kotlin
@Service
class MediaService(
    private val mediaRepository: MediaRepository,
    private val storageService: StorageService
) {

    fun uploadMedia(file: MultipartFile, uploader: User, metadata: Map<String, Any>): Media {
        // Validate file type
        validateMimeType(file.contentType)

        // Validate file size
        validateFileSize(file.size)

        // Generate unique file path
        val filePath = generateUniqueFilePath(file.originalFilename!!)

        // Store file
        storageService.store(file, filePath)

        // Extract metadata
        val extractedMetadata = extractMetadata(file)

        // Create media entity
        val media = Media(
            filename = file.originalFilename!!,
            filePath = filePath,
            mimeType = file.contentType!!,
            fileSize = file.size,
            uploader = uploader,
            metadata = (metadata + extractedMetadata).toString()
        )

        return mediaRepository.save(media)
    }

    fun deleteMedia(id: Long): Boolean {
        val media = mediaRepository.findById(id).orElse(null) ?: return false

        // Delete physical file
        storageService.delete(media.filePath)

        // Delete database record
        mediaRepository.delete(media)

        return true
    }

    private fun validateMimeType(mimeType: String?) {
        val allowedTypes = listOf(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "video/mp4", "audio/mpeg"
        )

        if (mimeType == null || !allowedTypes.contains(mimeType)) {
            throw IllegalArgumentException("File type not allowed: $mimeType")
        }
    }

    private fun validateFileSize(size: Long) {
        val maxSize = 10 * 1024 * 1024 // 10MB
        if (size > maxSize) {
            throw IllegalArgumentException("File size exceeds maximum allowed size")
        }
    }
}
```

### 6.6 TDD Cycle Demonstration

**Red-Green-Refactor Example:**

1. **🔴 RED**: Write failing test

   ```kotlin
   @Test
   fun `should validate content data against content type schema`() {
       // Test that expects IllegalArgumentException for invalid data
   }
   ```

2. **🟢 GREEN**: Write minimal code to pass

   ```kotlin
   fun createContent(content: Content): Content {
       validateContentData(content.contentType, content.contentData)
       return contentRepository.save(content)
   }
   ```

3. **🔵 REFACTOR**: Improve code quality
   ```kotlin
   // Extract validation logic, add detailed error messages, optimize performance
   ```

### 6.7 Business Logic Validation

**Content Management Rules:**

- **Schema Validation**: Content data must match ContentType field definitions
- **Required Fields**: All required fields must be present
- **Type Checking**: Field values must match declared types
- **Slug Uniqueness**: Slugs must be unique within a content type
- **Status Transitions**: Only valid state changes allowed
- **Publishing Rules**: Content must pass validation before publishing
- **Versioning**: Published content creates new versions on update

**User Management Rules:**

- **Unique Credentials**: Username and email must be unique
- **Password Security**: Passwords hashed with BCrypt
- **Role-Based Access**: Actions validated against user role
- **Soft Delete**: Users deactivated, not deleted

**Media Management Rules:**

- **File Type Validation**: Only allowed MIME types accepted
- **Size Limits**: Files must not exceed maximum size
- **Unique Paths**: File paths must be unique
- **Metadata Extraction**: Automatic extraction of file metadata

### 6.8 Testing Strategy

**Unit Testing with Mockito:**

```kotlin
// Mock external dependencies
@Mock private lateinit var contentRepository: ContentRepository
@Mock private lateinit var contentTypeRepository: ContentTypeRepository

// Test business logic in isolation
`when`(contentTypeRepository.findById(1L))
    .thenReturn(Optional.of(mockContentType))
```

**Test Coverage:**

- ✅ Happy path scenarios
- ✅ Error conditions and edge cases
- ✅ Business rule validation
- ✅ Schema validation logic
- ✅ State transition validation
- ✅ Data integrity checks
- ✅ Complex business logic (versioning, publishing workflow)

---

## Phase 7: REST API Development

### 7.1 User Controller Tests

#### File: `src/test/kotlin/com/headlesscms/controller/UserControllerTest.kt`

**Test Cases:**

1. `should register user via POST request`
2. `should authenticate user and return JWT token`
3. `should retrieve current user profile via GET request`
4. `should update user profile via PUT request`
5. `should list users with pagination (admin only)`
6. `should handle validation errors properly`
7. `should return 401 for unauthorized access`
8. `should return 403 for forbidden access based on role`

### 7.2 ContentType Controller Tests

#### File: `src/test/kotlin/com/headlesscms/controller/ContentTypeControllerTest.kt`

**Test Cases:**

1. `should create content type via POST request (admin only)`
2. `should retrieve content type by API identifier`
3. `should list all content types via GET request`
4. `should update content type schema via PUT request`
5. `should validate field definitions on create and update`
6. `should return appropriate HTTP status codes`

### 7.3 Content Controller Tests

#### File: `src/test/kotlin/com/headlesscms/controller/ContentControllerTest.kt`

**Test Cases:**

1. `should create content via POST request`
2. `should retrieve content by ID`
3. `should list content with filtering and pagination`
4. `should update content via PUT request`
5. `should publish content via POST to publish endpoint`
6. `should archive content via POST to archive endpoint`
7. `should retrieve content by slug`
8. `should validate content data against schema`
9. `should return published content only for public API`
10. `should support content search and filtering`

### 7.4 Media Controller Tests

#### File: `src/test/kotlin/com/headlesscms/controller/MediaControllerTest.kt`

**Test Cases:**

1. `should upload media via POST request with multipart data`
2. `should retrieve media by ID`
3. `should list media with filtering and pagination`
4. `should update media metadata via PUT request`
5. `should delete media via DELETE request`
6. `should serve media files with proper content type`
7. `should validate file type and size on upload`

### 7.5 Controller Implementation

#### Files:

- `src/main/kotlin/com/headlesscms/controller/UserController.kt`
- `src/main/kotlin/com/headlesscms/controller/ContentTypeController.kt`
- `src/main/kotlin/com/headlesscms/controller/ContentController.kt`
- `src/main/kotlin/com/headlesscms/controller/MediaController.kt`

**Example: Content Controller**

```kotlin
@RestController
@RequestMapping("/api/v1/content")
class ContentController(
    private val contentService: ContentService
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR', 'AUTHOR')")
    fun createContent(@Valid @RequestBody request: CreateContentRequest): ResponseEntity<ContentResponse> {
        val content = contentService.createContent(request.toContent())
        return ResponseEntity.status(HttpStatus.CREATED).body(ContentResponse.from(content))
    }

    @GetMapping("/{id}")
    fun getContent(@PathVariable id: Long): ResponseEntity<ContentResponse> {
        val content = contentService.findById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ContentResponse.from(content))
    }

    @GetMapping
    fun listContent(
        @RequestParam(required = false) contentType: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ContentResponse>> {
        val contents = contentService.findContent(contentType, status, page, size)
        return ResponseEntity.ok(contents.map { ContentResponse.from(it) })
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    fun publishContent(
        @PathVariable id: Long,
        @RequestBody(required = false) request: PublishContentRequest?
    ): ResponseEntity<ContentResponse> {
        val content = contentService.publishContent(id, request?.publishDate)
        return ResponseEntity.ok(ContentResponse.from(content))
    }
}
```

### 7.6 DTO Classes

#### File: `src/main/kotlin/com/headlesscms/dto/`

**User DTOs:**

- `UserRegistrationRequest.kt`
- `UserLoginRequest.kt`
- `UserResponse.kt`
- `UpdateUserRequest.kt`

**ContentType DTOs:**

- `CreateContentTypeRequest.kt`
- `UpdateContentTypeRequest.kt`
- `ContentTypeResponse.kt`
- `FieldDefinitionDTO.kt`

**Content DTOs:**

- `CreateContentRequest.kt`
- `UpdateContentRequest.kt`
- `ContentResponse.kt`
- `PublishContentRequest.`
