package com.david.collegeevents.domain.model

data class EventSummary(
    val id: String,
    val title: String,
    val clubName: String,
    val bannerUrl: String,
    val startDateTime: String,
    val endDateTime: String,
    val eventMode: String,
    val venue: String?,
    val registrationBadge: String,
    val createdBy: String
)