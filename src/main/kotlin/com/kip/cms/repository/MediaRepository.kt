package com.kip.cms.repository

import com.kip.cms.entity.Media
import com.kip.cms.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MediaRepository : JpaRepository<Media, Long> {
    
    fun findByUploader(uploader: User): List<Media>
    
    fun findByMimeTypeStartingWith(mimeTypePrefix: String): List<Media>
    
    fun findByFilePath(filePath: String): Media?
    
    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<Media>
    
    @Query("SELECT m FROM Media m WHERE m.mimeType LIKE :mimeType%")
    fun findByMimeTypePattern(@Param("mimeType") mimeType: String): List<Media>
    
    @Query("SELECT m FROM Media m WHERE m.uploader = :uploader ORDER BY m.createdAt DESC")
    fun findByUploaderOrderByCreatedAtDesc(@Param("uploader") uploader: User): List<Media>
    
    @Query("SELECT m FROM Media m WHERE m.tags LIKE %:tag%")
    fun findByTagsContaining(@Param("tag") tag: String): List<Media>
    
    @Query("SELECT m FROM Media m WHERE m.fileSize > :minSize AND m.fileSize < :maxSize")
    fun findByFileSizeBetween(@Param("minSize") minSize: Long, @Param("maxSize") maxSize: Long): List<Media>
}
