package com.kip.cms.controller

import com.kip.cms.entity.Content
import com.kip.cms.Service.ContentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contents")
class ContentController(
    private val contentService: ContentService
){
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): Content?{
        return contentService.findById(id)
    }

    @PostMapping
    fun createContent(@RequestBody content: Content): Content{
        return contentService.createContent(content)
    }

    @PutMapping("/{id}/publish")
    fun publishContent(@PathVariable id: Long): Content{
        return contentService.publishContent(id)
    }
}