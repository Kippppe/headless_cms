package com.kip.cms.Service

import com.kip.cms.entity.Media
import com.kip.cms.entity.User
import com.kip.cms.repository.MediaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MediaService(
	private val mediaRepository: MediaRepository
){
	fun uploadMedia(media: Media): Media{
		validateUniqueFilePath(media.filePath)

		return mediaRepository.save(media)
	}

	private fun validateUniqueFilePath(filePath: String){
		if (mediaRepository.findByFilePath(filePath)!=null){
			throw IllegalArgumentException("FilePath'${filePath}' is already used")
		}
	}

	fun findById(id:Long): Media?{
		return mediaRepository.findById(id)
		.orElse(null)
	}

	fun findByUploader(uploader: User): List<Media>{
		return mediaRepository.findByUploader(uploader)
	}

	fun findByMimeTypeStartingWith(mimeTypePrefix: String): List<Media>{
		return mediaRepository.findByMimeTypeStartingWith(mimeTypePrefix)
	}
}


