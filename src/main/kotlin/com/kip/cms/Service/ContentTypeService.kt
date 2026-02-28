package com.kip.cms.Service

import com.kip.cms.entity.ContentType
import com.kip.cms.repository.ContentTypeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ContentTypeService(
    private val contentTypeRepository: ContentTypeRepository
) {
    fun createContentType(contentType: ContentType): ContentType {
        validateUniqueName(contentType.name)
        validateUniqueApiIdentifier(contentType.apiIdentifier)

        return contentTypeRepository.save(contentType)
    }

    private fun validateUniqueName(name: String) {
    if (contentTypeRepository.findByName(name)!= null) {
        throw IllegalArgumentException("Name '${name}' is already used")
    }
}

    private fun validateUniqueApiIdentifier(apiIdentifier: String) {
    if (contentTypeRepository.findByApiIdentifier(apiIdentifier) != null) {
        throw IllegalArgumentException("ApiIdentifier '${apiIdentifier}' is already used")
    }
}

    fun findByApiIdentifier(apiIdentifier: String): ContentType? {
        return contentTypeRepository.findByApiIdentifier(apiIdentifier)
    }

    fun deactivateContentType(id: Long): ContentType {
        val contentType = contentTypeRepository.findById(id)
            .orElseThrow {IllegalArgumentException("ContentType not found")}

        val deactiveContentType = contentType.copy(
            active = false
        )

        return contentTypeRepository.save(deactiveContentType)
    }
}
 