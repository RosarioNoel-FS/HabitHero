package com.example.habithero.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habithero.data.FirestoreRepository
import com.example.habithero.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class SettingsUiState(
    val loading: Boolean = true,
    val user: User = User(),
    val nameEditing: String = "",
    val message: String? = null
)


class SettingsViewModel(
    private val repo: FirestoreRepository = FirestoreRepository()
) : ViewModel() {


    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state


    init { refresh() }


    fun refresh() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, message = null)
        runCatching { repo.getCurrentUser() }
            .onSuccess { u -> _state.value = SettingsUiState(loading = false, user = u, nameEditing = u.name) }
            .onFailure { e -> _state.value = _state.value.copy(loading = false, message = e.message) }
    }


    fun onNameChange(v: String) { _state.value = _state.value.copy(nameEditing = v) }


    fun saveName() = viewModelScope.launch {
        val name = _state.value.nameEditing.trim()
        if (name.isEmpty()) return@launch
        runCatching { repo.updateName(name) }
            .onSuccess { refresh() }
            .onFailure { e -> _state.value = _state.value.copy(message = e.message) }
    }


    fun uploadPhoto(uri: Uri) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, message = null)
        runCatching { repo.uploadProfileImage(uri) }
            .onSuccess { photoUrl ->
                // Re-fetch the user to get the updated photoUrl and bust the cache with a timestamp
                val updatedUser = repo.getCurrentUser().copy(photoUrl = "$photoUrl?t=${System.currentTimeMillis()}")
                _state.value = _state.value.copy(loading = false, user = updatedUser)
            }
            .onFailure { e -> _state.value = _state.value.copy(loading = false, message = e.message) }
    }


    fun signOut() = repo.signOut()
}