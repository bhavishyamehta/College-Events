package com.david.collegeevents.domain.model

import com.david.collegeevents.data.remote.dto.DocumentDto
import com.david.collegeevents.data.remote.dto.JudgeDto
import com.david.collegeevents.data.remote.dto.SpeakerDto

data class EventDetail(
    val id: String,
    val title: String,
    val shortDescription: String,
    val detailedDescription: String,
    val clubName: String,
    val bannerUrl: String,
    val category: String,
    val tags: List<String>,

    val startDateTime: String,
    val endDateTime: String,
    val eventMode: String,
    val venue: String?,
    val onlineMeetingLink: String?,

    val coordinatorName: String,
    val coordinatorContact: String,

    val registrationRequired: Boolean,
    val registrationFee: String,
    val registrationDeadline: String?,
    val registrationType: String,
    val minTeamSize: Int?,
    val maxTeamSize: Int?,
    val registrationLink: String?,
    val eligibility: List<String>,

    val eventType: String,
    val rulesText: String?,
    val judgingCriteria: String?,
    val firstPrize: String?,
    val secondPrize: String?,
    val thirdPrize: String?,
    val hasParticipationCert: Boolean,
    val hasWinnerCert: Boolean,
    val problemStatement: String?,
    val submissionDeadline: String?,

    val speakers: List<SpeakerDto>,
    val judges: List<JudgeDto>,
    val documents: List<DocumentDto>,

    val whatsappLink: String?,
    val telegramLink: String?,
    val discordLink: String?,

    val seatAvailability: String,
    val isUserRegistered: Boolean,
    val creatorId: String,
    val createdBy: String
)