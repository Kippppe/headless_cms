package com.kip.cms.controller

import com.kip.cms.entity.ContentType
import com.kip.cms.Service.ContentTypeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contentType")
class ContentTypeController(
    private val contentTypeService: ContentTypeService
){
    @PostMapping
    fun createContentType(@RequestBody contentType: ContentType):ContentType{
        return contentTypeService.createContentType(contentType)
    }

    @GetMapping("/{apiIdentifier}")
    fun findByApiIdentifier(@PathVariable apiIdentifier: String): ContentType?{
        return contentTypeService.findByApiIdentifier(apiIdentifier)
    }

    @PutMapping("/{id}/deactivate")
    fun deactivateContentType(@PathVariable id: Long): ContentType{
        return contentTypeService.deactivateContentType(id)
    }
}

