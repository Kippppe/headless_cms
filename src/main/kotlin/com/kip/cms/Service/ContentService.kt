package com.kip.cms.Service

import com.kip.cms.entity.Content
import com.kip.cms.entity.ContentStatus
import com.kip.cms.entity.ContentType
import com.kip.cms.entity.User
import com.kip.cms.repository.ContentRepository
import com.kip.cms.repository.ContentTypeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime  

@Service
@Transactional
class ContentService(
    private val contentRepository: ContentRepository,
    private val contentTypeRepository: ContentTypeRepository
) {
    fun createContent(content: Content): Content{
        validateUniqueSlug(content.contentType, content.slug)

        return contentRepository.save(content)
    }

    fun publishContent(id: Long): Content{
        val content = contentRepository.findById(id)
        .orElseThrow {IllegalArgumentException("Content not found")}

       if (content.contentData.isBlank()){
        throw IllegalArgumentException("Need a content to publish")
       }

    val publishedContent = content.copy(
        status = ContentStatus.PUBLISHED,
        publishDate = LocalDateTime.now()
    )
    return contentRepository.save(publishedContent)

    }
    
    private fun validateUniqueSlug(contentType: ContentType, slug: String) {
        if (contentRepository.findByContentTypeAndSlug(contentType, slug) != null) {
            throw IllegalArgumentException("Slug '${slug}' is already used")
        }
    }
}