package com.david.collegeevents.data.repository

import com.david.collegeevents.data.remote.ApiServices
import com.david.collegeevents.data.remote.dto.GenericErrorDto
import com.david.collegeevents.domain.model.EventDetail
import com.david.collegeevents.domain.repository.EventDetailsRepository
import com.david.collegeevents.utils.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class EventDetailsRepositoryImpl @Inject constructor(
    private val api: ApiServices
) : EventDetailsRepository {

    override fun getEventDetail(eventId: String): Flow<Resource<EventDetail>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getEventDetail(eventId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                emit(
                    Resource.Success(
                        EventDetail(
                            id = body.id,
                            title = body.title,
                            shortDescription = body.shortDescription,
                            detailedDescription = body.detailedDescription,
                            clubName = body.clubName,
                            bannerUrl = body.bannerUrl,
                            category = body.category,
                            tags = body.tags,

                            startDateTime = body.startDateTime,
                            endDateTime = body.endDateTime,
                            eventMode = body.eventMode,
                            venue = body.venue,
                            onlineMeetingLink = body.onlineMeetingLink,

                            coordinatorName = body.coordinatorName,
                            coordinatorContact = body.coordinatorContact,

                            registrationRequired = body.registrationRequired,
                            registrationFee = body.registrationFee,
                            registrationDeadline = body.registrationDeadline,
                            registrationType = body.registrationType,
                            minTeamSize = body.minTeamSize,
                            maxTeamSize = body.maxTeamSize,
                            registrationLink = body.registrationLink,
                            eligibility = body.eligibility,

                            eventType = body.eventType,
                            rulesText = body.rulesText,
                            judgingCriteria = body.judgingCriteria,
                            firstPrize = body.firstPrize,
                            secondPrize = body.secondPrize,
                            thirdPrize = body.thirdPrize,
                            hasParticipationCert = body.hasParticipationCert,
                            hasWinnerCert = body.hasWinnerCert,
                            problemStatement = body.problemStatement,
                            submissionDeadline = body.submissionDeadline,

                            speakers = body.speakers,
                            judges = body.judges,
                            documents = body.documents,

                            whatsappLink = body.whatsappLink,
                            telegramLink = body.telegramLink,
                            discordLink = body.discordLink,

                            seatAvailability = body.seatAvailability,
                            isUserRegistered = body.isUserRegistered,
                            creatorId = body.creatorId,
                            createdBy = body.createdBy
                        )
                    )
                )
            } else {
                emit(Resource.Error("Failed to fetch event breakdown data"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Check backend link setup connection"))
        }
    }

    override fun toggleRegistration(eventId: String, register: Boolean): Flow<Resource<String>> =
        flow {
            emit(Resource.Loading())
            try {
                val response =
                    if (register) api.registerForEvent(eventId) else api.deregisterFromEvent(eventId)
                if (response.isSuccessful && response.body() != null) {
                    emit(Resource.Success(response.body()!!.message))
                } else {
                    val err = Gson().fromJson(
                        response.errorBody()?.string(),
                        GenericErrorDto::class.java
                    ).message
                    emit(Resource.Error(err))
                }
            } catch (e: Exception) {
                emit(Resource.Error("Operation transaction failed processing downstream"))
            }
        }
}