package com.kip.cms.repository

import com.kip.cms.entity.Content
import com.kip.cms.entity.ContentType
import com.kip.cms.entity.ContentStatus
import com.kip.cms.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ContentRepository : JpaRepository<Content, Long> {
    
    fun findByContentType(contentType: ContentType): List<Content>
    
    fun findByAuthor(author: User): List<Content>
    
    fun findByStatus(status: ContentStatus): List<Content>
    
    fun findByContentTypeAndStatus(contentType: ContentType, status: ContentStatus): List<Content>
    
    fun findByContentTypeAndSlug(contentType: ContentType, slug: String): Content?
    
    fun findByStatusAndPublishDateBefore(status: ContentStatus, date: LocalDateTime): List<Content>
    
    fun findByStatusAndPublishDateAfter(status: ContentStatus, date: LocalDateTime): List<Content>
    
    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<Content>
    
    @Query("SELECT c FROM Content c WHERE c.status = :status AND c.publishDate <= :now")
    fun findPublishedContentByDate(@Param("status") status: ContentStatus, @Param("now") now: LocalDateTime): List<Content>
    
    @Query("SELECT c FROM Content c WHERE c.contentType = :contentType AND c.status = 'PUBLISHED' ORDER BY c.publishDate DESC")
    fun findPublishedContentByType(@Param("contentType") contentType: ContentType): List<Content>
    
    @Query("SELECT c FROM Content c WHERE c.status = 'PUBLISHED' AND c.publishDate <= CURRENT_TIMESTAMP ORDER BY c.publishDate DESC")
    fun findAllPublishedContent(): List<Content>
    
    // Additional methods for integration testing
    fun findByAuthorAndStatus(author: User, status: ContentStatus): List<Content>
    
    fun findByContentTypeAndAuthor(contentType: ContentType, author: User): List<Content>
    
    fun findByContentDataContainingIgnoreCase(searchTerm: String): List<Content>
    
    fun findByContentDataContaining(searchTerm: String): List<Content>
}
