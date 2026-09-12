package com.david.collegeevents.presentation.profile

import com.david.collegeevents.domain.model.EventSummary
import com.david.collegeevents.domain.model.UserProfile

data class ProfileState(
    val isLoading: Boolean = false,
    val profileData: UserProfile? = null,
    val error: String? = null,
    val isLoggedOut: Boolean = false,
    val userRole: String = "STUDENT",
    val createdEvents: List<EventSummary> = emptyList(),
    val isLoadingCreatedEvents: Boolean = false
)