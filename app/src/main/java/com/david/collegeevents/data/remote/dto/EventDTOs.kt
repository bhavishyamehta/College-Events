package com.david.collegeevents.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EventDTOs(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("clubName") val clubName: String,
    @SerializedName("bannerUrl") val bannerUrl: String,
    @SerializedName("startDateTime") val startDateTime: String,
    @SerializedName("endDateTime") val endDateTime: String,
    @SerializedName("eventMode") val eventMode: String,
    @SerializedName("venue") val venue: String?,
    @SerializedName("registrationBadge") val registrationBadge: String,
    @SerializedName("createdBy") val createdBy: String,
)