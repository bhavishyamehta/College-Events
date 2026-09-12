package com.david.collegeevents.presentation.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.david.collegeevents.domain.usecase.GetUserProfileUseCase
import com.david.collegeevents.domain.usecase.GetEventsUseCase
import com.david.collegeevents.utils.Resource
import com.david.collegeevents.utils.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getAllEventsUseCase: GetEventsUseCase,   // ⚠️ existing event-list use case reuse kar rahe hain
    private val tokenManager: TokenManager
) : ViewModel() {

    var state by mutableStateOf(ProfileState())
        private set

    init {
        observeUserRole()
        getProfile()
    }

    private fun observeUserRole() {
        viewModelScope.launch {
            tokenManager.userRoleFlow.collect { role ->
                state = state.copy(userRole = role)
                // Role pata chalte hi, agar Teacher/Admin hai toh unke created events fetch karo
                if (role == "TEACHER" || role == "ADMIN") {
                    fetchCreatedEvents()
                }
            }
        }
    }

    fun getProfile() {
        getUserProfileUseCase().onEach { result ->
            state = when (result) {
                is Resource.Loading -> state.copy(isLoading = true, error = null)
                is Resource.Success -> state.copy(isLoading = false, profileData = result.data)
                is Resource.Error -> state.copy(isLoading = false, error = result.message)
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchCreatedEvents() {
        viewModelScope.launch {
            getAllEventsUseCase(category = "All").onEach { result ->   // ✅ "All" pass karo, null nahi
                state = when (result) {
                    is Resource.Loading -> state.copy(isLoadingCreatedEvents = true)
                    is Resource.Success -> {
                        val currentUserName = state.profileData?.fullName
                        val myEvents = result.data?.filter { it.createdBy == currentUserName } ?: emptyList()
                        state.copy(isLoadingCreatedEvents = false, createdEvents = myEvents)
                    }
                    is Resource.Error -> state.copy(isLoadingCreatedEvents = false)
                }
            }.launchIn(this)
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearSession()
            state = state.copy(isLoggedOut = true)
        }
    }
}