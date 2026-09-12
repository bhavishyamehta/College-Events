package com.david.collegeevents.data.repository

import com.david.collegeevents.data.remote.ApiServices
import com.david.collegeevents.data.remote.dto.GenericErrorDto
import com.david.collegeevents.domain.model.EventSummary
import com.david.collegeevents.domain.repository.EventRepository
import com.david.collegeevents.utils.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val api: ApiServices
) : EventRepository {

    override fun getEvents(category: String?): Flow<Resource<List<EventSummary>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getEvents(category)
            if (response.isSuccessful && response.body() != null) {
                val domainEvents = response.body()!!.map { dto ->
                    EventSummary(
                        id = dto.id,
                        title = dto.title,
                        clubName = dto.clubName,
                        bannerUrl = dto.bannerUrl,
                        startDateTime = dto.startDateTime,
                        endDateTime = dto.endDateTime,
                        eventMode = dto.eventMode,
                        venue = dto.venue,
                        registrationBadge = dto.registrationBadge,
                        createdBy = dto.createdBy
                    )
                }
                emit(Resource.Success(domainEvents))
            } else {
                val errorMsg = try {
                    Gson().fromJson(response.errorBody()?.string(), GenericErrorDto::class.java).message
                } catch (e: Exception) { "Failed to load events" }
                emit(Resource.Error(errorMsg))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Check your internet connection"))
        }
    }
}