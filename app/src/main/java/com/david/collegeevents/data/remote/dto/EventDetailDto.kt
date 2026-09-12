package com.david.collegeevents.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EventDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("shortDescription") val shortDescription: String,
    @SerializedName("detailedDescription") val detailedDescription: String,
    @SerializedName("clubName") val clubName: String,
    @SerializedName("bannerUrl") val bannerUrl: String,
    @SerializedName("category") val category: String,
    @SerializedName("tags") val tags: List<String>,

    @SerializedName("startDateTime") val startDateTime: String,
    @SerializedName("endDateTime") val endDateTime: String,
    @SerializedName("eventMode") val eventMode: String,
    @SerializedName("venue") val venue: String?,
    @SerializedName("onlineMeetingLink") val onlineMeetingLink: String?,

    @SerializedName("coordinatorName") val coordinatorName: String,
    @SerializedName("coordinatorContact") val coordinatorContact: String,

    @SerializedName("registrationRequired") val registrationRequired: Boolean,
    @SerializedName("registrationFee") val registrationFee: String,
    @SerializedName("registrationDeadline") val registrationDeadline: String?,
    @SerializedName("registrationType") val registrationType: String,
    @SerializedName("minTeamSize") val minTeamSize: Int?,
    @SerializedName("maxTeamSize") val maxTeamSize: Int?,
    @SerializedName("registrationLink") val registrationLink: String?,
    @SerializedName("eligibility") val eligibility: List<String>,

    @SerializedName("eventType") val eventType: String,
    @SerializedName("rulesText") val rulesText: String?,
    @SerializedName("judgingCriteria") val judgingCriteria: String?,
    @SerializedName("firstPrize") val firstPrize: String?,
    @SerializedName("secondPrize") val secondPrize: String?,
    @SerializedName("thirdPrize") val thirdPrize: String?,
    @SerializedName("hasParticipationCert") val hasParticipationCert: Boolean,
    @SerializedName("hasWinnerCert") val hasWinnerCert: Boolean,
    @SerializedName("problemStatement") val problemStatement: String?,
    @SerializedName("submissionDeadline") val submissionDeadline: String?,

    @SerializedName("speakers") val speakers: List<SpeakerDto>,
    @SerializedName("judges") val judges: List<JudgeDto>,
    @SerializedName("documents") val documents: List<DocumentDto>,

    @SerializedName("whatsappLink") val whatsappLink: String?,
    @SerializedName("telegramLink") val telegramLink: String?,
    @SerializedName("discordLink") val discordLink: String?,

    @SerializedName("seatAvailability") val seatAvailability: String,
    @SerializedName("isUserRegistered") val isUserRegistered: Boolean,
    @SerializedName("creatorId") val creatorId: String,
    @SerializedName("createdBy") val createdBy: String
)