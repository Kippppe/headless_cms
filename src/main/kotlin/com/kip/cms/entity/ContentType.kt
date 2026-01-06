package com.kip.cms.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
@Table(name = "content_types")
data class ContentType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 100)
    @field:NotBlank(message = "Content type name is required")
    @field:Size(min = 2, max = 100, message = "Content type name must be between 2 and 100 characters")
    val name: String,

    @Column(nullable = false, unique = true, length = 100, name = "api_identifier")
    @field:NotBlank(message = "API identifier is required")
    @field:Size(min = 2, max = 100, message = "API identifier must be between 2 and 100 characters")
    @field:Pattern(
        regexp = "^[a-z][a-z0-9_]*[a-z0-9]$|^[a-z]$", 
        message = "API identifier must be lowercase with underscores, start with letter"
    )
    val apiIdentifier: String,

    @Column(length = 500)
    @field:Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT", name = "field_definitions")
    @field:NotBlank(message = "Field definitions are required")
    val fieldDefinitions: String,

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(nullable = false)
    val version: Int = 1,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    val createdBy: User,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by_id", nullable = false)
    val updatedBy: User,

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "created_at")
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(nullable = false, name = "updated_at")
    val updatedAt: LocalDateTime? = null
) {
    override fun toString(): String {
        return "ContentType(id=$id, name='$name', apiIdentifier='$apiIdentifier', description='$description', active=$active, version=$version)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContentType

        if (id != other.id) return false
        if (name != other.name) return false
        if (apiIdentifier != other.apiIdentifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + apiIdentifier.hashCode()
        return result
    }
}
