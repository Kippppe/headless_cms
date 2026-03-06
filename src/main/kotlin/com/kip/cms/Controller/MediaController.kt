package com.kip.cms.controller

import com.kip.cms.entity.Media
import com.kip.cms.Service.MediaService
import com.kip.cms.Service.UserService
import com.kip.cms.entity.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/media")
class MediaController(
    private val mediaService: MediaService,
    private val userService: UserService
){
    @PostMapping
    fun uploadMedia(@RequestBody media: Media): Media{
        return mediaService.uploadMedia(media)
    }
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): Media?{
        return mediaService.findById(id)
    }
    @GetMapping("/uploader/{uploaderId}")
    fun findByUploader(@PathVariable uploaderId: Long): List<Media>{
    val user = userService.findById(uploaderId)
          ?:throw IllegalArgumentException("User not found")    
    return mediaService.findByUploader(user)
    }
    @GetMapping("/mime/{mimeTypePrefix}")
    fun findByMimeTypeStartingWith(@PathVariable mimeTypePrefix: String): List<Media>{
    return mediaService.findByMimeTypeStartingWith(mimeTypePrefix)
    }

}