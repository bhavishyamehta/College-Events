package com.david.collegeevents.presentation.createEvent

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.david.collegeevents.data.remote.dto.CreateEventRequest
import com.david.collegeevents.data.remote.dto.DocumentDto
import com.david.collegeevents.domain.repository.AdminEventRepository
import com.david.collegeevents.domain.repository.EventDetailsRepository
import com.david.collegeevents.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

// Documents jo abhi local device pe pade hain, upload hone baaki hai
data class PendingDocument(
    val localUri: Uri,
    val docType: String,
    val fileName: String
)

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val repository: AdminEventRepository,
    private val eventDetailsRepository: EventDetailsRepository,
    private val application: Application
) : ViewModel() {

    var state by mutableStateOf(CreateEventState())
        private set

    private val _event = MutableSharedFlow<CreateEventUiEvent>()
    val event = _event.asSharedFlow()

    fun populateFieldsForEdit(existingEventId: String) {
        viewModelScope.launch {
            state = state.copy(isLoadingEvent = true)
            when (val result = eventDetailsRepository.getEventDetail(existingEventId).first { it !is Resource.Loading }) {
                is Resource.Success -> {
                    state = state.copy(isLoadingEvent = false, prefillEvent = result.data)
                }
                is Resource.Error -> {
                    state = state.copy(isLoadingEvent = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    /**
     * Main submit function. Sequential flow:
     * 1. Agar naya banner select hua hai (localUri != null) to sabse pehle upload karo
     * 2. Agar naye documents pending hain to unko bhi upload karo
     * 3. Fir poora CreateEventRequest object banao aur backend ko bhejo
     */
    fun submitEvent(
        eventId: String?,
        bannerLocalUri: Uri?,
        existingBannerUrl: String?,
        pendingDocuments: List<PendingDocument>,
        existingDocuments: List<DocumentDto>,
        buildRequest: (finalBannerUrl: String, finalDocuments: List<DocumentDto>) -> CreateEventRequest
    ) {
        viewModelScope.launch {
            state = state.copy(isPublishingEvent = true, errorMessage = null, publishingStage = "Uploading banner...")

            // ── Step 1: Banner upload (sirf agar naya select hua hai) ─────────
            val finalBannerUrl: String
            if (bannerLocalUri != null) {
                val uploadResult = uploadToServer(bannerLocalUri, "banner_upload", "image/*", isDocument = false)
                if (uploadResult == null) {
                    state = state.copy(isPublishingEvent = false, publishingStage = null, errorMessage = "Banner upload failed. Try again.")
                    return@launch
                }
                finalBannerUrl = uploadResult
            } else if (!existingBannerUrl.isNullOrBlank()) {
                finalBannerUrl = existingBannerUrl
            } else {
                state = state.copy(isPublishingEvent = false, publishingStage = null, errorMessage = "Please select an event banner.")
                return@launch
            }

            // ── Step 2: Pending documents upload (agar koi hai) ────────────────
            val uploadedDocs = mutableListOf<DocumentDto>()
            uploadedDocs.addAll(existingDocuments)

            if (pendingDocuments.isNotEmpty()) {
                state = state.copy(publishingStage = "Uploading documents (0/${pendingDocuments.size})...")
                pendingDocuments.forEachIndexed { index, doc ->
                    state = state.copy(publishingStage = "Uploading documents (${index + 1}/${pendingDocuments.size})...")
                    val url = uploadToServer(doc.localUri, "doc_upload", "*/*", isDocument = true)
                    if (url == null) {
                        state = state.copy(isPublishingEvent = false, publishingStage = null, errorMessage = "Failed to upload '${doc.fileName}'. Try again.")
                        return@launch
                    }
                    uploadedDocs.add(DocumentDto(docType = doc.docType, fileUrl = url, fileName = doc.fileName))
                }
            }

            // ── Step 3: Create/Update event ─────────────────────────────────────
            state = state.copy(publishingStage = if (eventId == null) "Creating event..." else "Saving changes...")
            val request = buildRequest(finalBannerUrl, uploadedDocs)

            val flowResult = if (eventId == null) {
                repository.submitEvent(request)
            } else {
                repository.modifyEvent(eventId, request)
            }

            when (val result = flowResult.first { it !is Resource.Loading }) {
                is Resource.Success -> {
                    state = state.copy(isPublishingEvent = false, publishingStage = null, executionSuccess = true)
                }
                is Resource.Error -> {
                    state = state.copy(isPublishingEvent = false, publishingStage = null, errorMessage = result.message ?: "Failed to save event")
                }
                else -> Unit
            }
        }
    }

    // Banner ke liye repository.uploadBanner(), documents ke liye repository.uploadDocument()
    private suspend fun uploadToServer(uri: Uri, tempPrefix: String, mimeType: String, isDocument: Boolean): String? {
        return try {
            val file = fileFromUri(uri, tempPrefix)
            val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val flow = if (isDocument) repository.uploadDocument(part) else repository.uploadBanner(part)
            when (val result = flow.first { it !is Resource.Loading }) {
                is Resource.Success -> result.data
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun fileFromUri(uri: Uri, prefix: String): File {
        val inputStream = application.contentResolver.openInputStream(uri)
        val file = File.createTempFile(prefix, ".tmp", application.cacheDir)
        inputStream?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }

    fun resetErrors() {
        state = state.copy(errorMessage = null)
    }

    sealed class CreateEventUiEvent {
        data class ShowToast(val msg: String) : CreateEventUiEvent()
    }
}