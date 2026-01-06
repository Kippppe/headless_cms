package com.kip.cms.repository

import com.kip.cms.entity.ContentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ContentTypeRepository : JpaRepository<ContentType, Long> {
    
    /**
     * Find content type by API identifier (unique identifier for API access)
     * @param apiIdentifier the API identifier to search for
     * @return ContentType if found, null otherwise
     */
    fun findByApiIdentifier(apiIdentifier: String): ContentType?
    
    /**
     * Find all active content types
     * @return List of all content types where active = true
     */
    fun findByActiveTrue(): List<ContentType>
    
    /**
     * Find content type by name (unique identifier for display)
     * @param name the content type name to search for
     * @return ContentType if found, null otherwise
     */
    fun findByName(name: String): ContentType?
}
