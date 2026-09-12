package com.david.collegeevents.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SpeakerDto(
    @SerializedName("name") val name: String,
    @SerializedName("photoUrl") val photoUrl: String? = null,
    @SerializedName("designation") val designation: String? = null,
    @SerializedName("organization") val organization: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("linkedinUrl") val linkedinUrl: String? = null
)

data class JudgeDto(
    @SerializedName("name") val name: String,
    @SerializedName("designation") val designation: String? = null,
    @SerializedName("bio") val bio: String? = null
)

data class DocumentDto(
    @SerializedName("docType") val docType: String,
    @SerializedName("fileUrl") val fileUrl: String,
    @SerializedName("fileName") val fileName: String? = null
)

data class CreateEventRequest(
    // 1. Basic Info
    @SerializedName("title") val title: String,
    @SerializedName("shortDescription") val shortDescription: String,
    @SerializedName("detailedDescription") val detailedDescription: String,
    @SerializedName("bannerUrl") val bannerUrl: String,
    @SerializedName("category") val category: String,
    @SerializedName("tags") val tags: List<String> = emptyList(),

    // 2. Date & Venue
    @SerializedName("startDateTime") val startDateTime: String,
    @SerializedName("endDateTime") val endDateTime: String,
    @SerializedName("eventMode") val eventMode: String,
    @SerializedName("venue") val venue: String? = null,
    @SerializedName("onlineMeetingLink") val onlineMeetingLink: String? = null,

    // 3. Organizer
    @SerializedName("clubName") val clubName: String,
    @SerializedName("coordinatorName") val coordinatorName: String,
    @SerializedName("coordinatorContact") val coordinatorContact: String,

    // 4. Registration
    @SerializedName("registrationRequired") val registrationRequired: Boolean = true,
    @SerializedName("registrationFee") val registrationFee: String = "Free",
    @SerializedName("registrationDeadline") val registrationDeadline: String? = null,
    @SerializedName("totalSeats") val totalSeats: Int = 50,
    @SerializedName("registrationType") val registrationType: String = "INDIVIDUAL",
    @SerializedName("minTeamSize") val minTeamSize: Int? = null,
    @SerializedName("maxTeamSize") val maxTeamSize: Int? = null,
    @SerializedName("registrationLink") val registrationLink: String? = null,
    @SerializedName("eligibility") val eligibility: List<String> = listOf("ALL"),

    // 5. Competition / Hackathon
    @SerializedName("eventType") val eventType: String = "GENERAL",
    @SerializedName("rulesText") val rulesText: String? = null,
    @SerializedName("judgingCriteria") val judgingCriteria: String? = null,
    @SerializedName("firstPrize") val firstPrize: String? = null,
    @SerializedName("secondPrize") val secondPrize: String? = null,
    @SerializedName("thirdPrize") val thirdPrize: String? = null,
    @SerializedName("hasParticipationCert") val hasParticipationCert: Boolean = false,
    @SerializedName("hasWinnerCert") val hasWinnerCert: Boolean = false,
    @SerializedName("problemStatement") val problemStatement: String? = null,
    @SerializedName("submissionDeadline") val submissionDeadline: String? = null,

    // 6. Speakers / Judges
    @SerializedName("speakers") val speakers: List<SpeakerDto> = emptyList(),
    @SerializedName("judges") val judges: List<JudgeDto> = emptyList(),

    // 7. Documents & Socials
    @SerializedName("documents") val documents: List<DocumentDto> = emptyList(),
    @SerializedName("whatsappLink") val whatsappLink: String? = null,
    @SerializedName("telegramLink") val telegramLink: String? = null,
    @SerializedName("discordLink") val discordLink: String? = null,

    @SerializedName("statusBadge") val statusBadge: String = "Registration Open"
)

data class ImageUploadResponse(
    @SerializedName("url") val url: String
)