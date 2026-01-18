package com.kip.cms.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
@Table(
    name = "media",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["file_path"], name = "uk_media_file_path")
    ]
)
data class Media(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 255)
    @field:NotBlank(message = "Filename is required")
    @field:Size(max = 255, message = "Filename cannot exceed 255 characters")
    val filename: String,

    @Column(nullable = false, unique = true, length = 500, name = "file_path")
    @field:NotBlank(message = "File path is required")
    @field:Size(max = 500, message = "File path cannot exceed 500 characters")
    val filePath: String,

    @Column(nullable = false, length = 100, name = "mime_type")
    @field:NotBlank(message = "MIME type is required")
    @field:Size(max = 100, message = "MIME type cannot exceed 100 characters")
    val mimeType: String,

    @Column(nullable = false, name = "file_size")
    @field:Min(value = 0, message = "File size must be non-negative")
    val fileSize: Long,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id", nullable = false)
    val uploader: User,

    @Column(length = 255, name = "alt_text")
    @field:Size(max = 255, message = "Alt text cannot exceed 255 characters")
    val altText: String? = null,

    @Column(length = 200)
    @field:Size(max = 200, message = "Title cannot exceed 200 characters")
    val title: String? = null,

    @Column(length = 1000)
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,

    @Column(length = 500)
    @field:Size(max = 500, message = "Tags cannot exceed 500 characters")
    val tags: String? = null,

    @Column(columnDefinition = "TEXT")
    val metadata: String? = null,

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "created_at")
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(nullable = false, name = "updated_at")
    val updatedAt: LocalDateTime? = null
) {
    override fun toString(): String {
        return "Media(id=$id, filename='$filename', filePath='$filePath', mimeType='$mimeType', fileSize=$fileSize)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Media

        if (id != other.id) return false
        if (filePath != other.filePath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + filePath.hashCode()
        return result
    }
}
