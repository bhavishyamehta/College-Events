package com.david.collegeevents.data.repository

import com.david.collegeevents.data.remote.ApiServices
import com.david.collegeevents.data.remote.dto.GenericErrorDto
import com.david.collegeevents.domain.model.EventSummary
import com.david.collegeevents.domain.model.UserProfile
import com.david.collegeevents.domain.repository.UserRepository
import com.david.collegeevents.utils.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: ApiServices
) : UserRepository {

    override fun getUserProfile(): Flow<Resource<UserProfile>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getProfile()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val userProfile = UserProfile(
                    fullName = body.fullName,
                    enrollmentNumber = body.enrollmentNumber,
                    branchDepartment = body.branchDepartment,
                    totalEvents = body.totalEvents,
                    certificatesCount = body.certificatesCount,
                    registeredEvents = body.registeredEvents.map { dto ->
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
                )
                emit(Resource.Success(userProfile))
            } else {
                val errorMsg = try {
                    Gson().fromJson(response.errorBody()?.string(), GenericErrorDto::class.java).message
                } catch (e: Exception) { "Failed to load profile data" }
                emit(Resource.Error(errorMsg))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "Server error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Network failure. Check connection."))
        }
    }
}