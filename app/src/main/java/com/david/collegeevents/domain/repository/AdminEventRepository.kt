package com.david.collegeevents.domain.repository

import com.david.collegeevents.data.remote.dto.CreateEventRequest
import com.david.collegeevents.utils.Resource
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface AdminEventRepository {
    fun uploadBanner(imagePart: MultipartBody.Part): Flow<Resource<String>>
    fun deleteImage(imageUrl: String): Flow<Resource<Boolean>>

    fun uploadDocument(filePart: MultipartBody.Part): Flow<Resource<String>>
    fun deleteDocument(fileUrl: String): Flow<Resource<Boolean>>

    fun submitEvent(request: CreateEventRequest): Flow<Resource<String>>
    fun modifyEvent(eventId: String, request: CreateEventRequest): Flow<Resource<String>>
    fun dropEvent(eventId: String): Flow<Resource<String>>
}