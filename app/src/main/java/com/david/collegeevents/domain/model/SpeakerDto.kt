package com.david.collegeevents.domain.model

// Backend ke exact field names match karne chahiye (Gson/Moshi field name = JSON key)
data class SpeakerDto(
    val name: String,
    val photoUrl: String? = null,
    val designation: String? = null,
    val organization: String? = null,
    val bio: String? = null,
    val linkedinUrl: String? = null
)

data class JudgeDto(
    val name: String,
    val designation: String? = null,
    val bio: String? = null
)

data class DocumentDto(
    val docType: String,
    val fileUrl: String,
    val fileName: String? = null
)

data class CreateEventRequest(
    val title: String,
    val shortDescription: String,
    val detailedDescription: String,
    val bannerUrl: String,
    val category: String,
    val tags: List<String> = emptyList(),

    val startDateTime: String,
    val endDateTime: String,
    val eventMode: String,
    val venue: String? = null,
    val onlineMeetingLink: String? = null,

    val clubName: String,
    val coordinatorName: String,
    val coordinatorContact: String,

    val registrationRequired: Boolean = true,
    val registrationFee: String = "Free",
    val registrationDeadline: String? = null,
    val totalSeats: Int = 50,
    val registrationType: String = "INDIVIDUAL",
    val minTeamSize: Int? = null,
    val maxTeamSize: Int? = null,
    val registrationLink: String? = null,
    val eligibility: List<String> = listOf("ALL"),

    val eventType: String = "GENERAL",
    val rulesText: String? = null,
    val judgingCriteria: String? = null,
    val firstPrize: String? = null,
    val secondPrize: String? = null,
    val thirdPrize: String? = null,
    val hasParticipationCert: Boolean = false,
    val hasWinnerCert: Boolean = false,
    val problemStatement: String? = null,
    val submissionDeadline: String? = null,

    val speakers: List<SpeakerDto> = emptyList(),
    val judges: List<JudgeDto> = emptyList(),

    val documents: List<DocumentDto> = emptyList(),
    val whatsappLink: String? = null,
    val telegramLink: String? = null,
    val discordLink: String? = null,

    val statusBadge: String = "Registration Open"
)