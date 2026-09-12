package com.david.collegeevents.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateEventResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("id") val id: String
)