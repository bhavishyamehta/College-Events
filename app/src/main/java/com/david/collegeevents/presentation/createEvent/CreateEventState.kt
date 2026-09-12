package com.david.collegeevents.presentation.createEvent

import com.david.collegeevents.domain.model.EventDetail

data class CreateEventState(
    val isPublishingEvent: Boolean = false,
    val publishingStage: String? = null,
    val executionSuccess: Boolean = false,
    val errorMessage: String? = null,

    val isLoadingEvent: Boolean = false,
    val prefillEvent: EventDetail? = null   // edit-mode: poora object ek hi jagah
)