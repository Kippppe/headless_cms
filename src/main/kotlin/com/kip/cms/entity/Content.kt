package com.kip.cms.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
@Table(
    name = "content",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["content_type_id", "slug"], name = "uk_content_type_slug")
    ]
)
data class Content(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_type_id", nullable = false)
    val contentType: ContentType,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ContentStatus = ContentStatus.DRAFT,

    @Column(nullable = false, columnDefinition = "TEXT", name = "content_data")
    @field:NotBlank(message = "Content data is required")
    val contentData: String,

    @Column(nullable = false, length = 200)
    @field:NotBlank(message = "Slug is required")
    @field:Size(min = 1, max = 200, message = "Slug must be between 1 and 200 characters")
    @field:Pattern(
        regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
        message = "Slug must contain only lowercase letters, numbers, and hyphens"
    )
    val slug: String,

    @Column(nullable = false)
    val version: Int = 1,

    @Column(name = "publish_date")
    val publishDate: LocalDateTime? = null,

    @Column(columnDefinition = "TEXT")
    val metadata: String? = null,

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "created_at")
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(nullable = false, name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @Column(name = "published_at")
    val publishedAt: LocalDateTime? = null,

    @Column(name = "archived_at")
    val archivedAt: LocalDateTime? = null
) {
    override fun toString(): String {
        return "Content(id=$id, slug='$slug', status=$status, version=$version, publishDate=$publishDate)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Content

        if (id != other.id) return false
        if (slug != other.slug) return false
        if (contentType.id != other.contentType.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + slug.hashCode()
        result = 31 * result + (contentType.id?.hashCode() ?: 0)
        return result
    }
}
