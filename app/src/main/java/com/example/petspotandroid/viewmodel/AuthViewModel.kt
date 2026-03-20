package com.example.petspotandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petspotandroid.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // 1. LiveData for the Loading Spinner
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 2. LiveData for Authentication Success (holds the logged-in user)
    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    // 3. LiveData for Error Messages (to show Toasts/Snackbars)
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // --- Functions called by the UI ---

    // Requirement: Check if user is already logged in when app starts
    fun checkCurrentUser() {
        _user.value = repository.currentUser
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        // Turn on the loading spinner
        _isLoading.value = true

        // Launch a background coroutine
        viewModelScope.launch {
            val result = repository.login(email, password)

            // Turn off the loading spinner
            _isLoading.value = false

            // Update LiveData based on success or failure
            result.onSuccess { firebaseUser ->
                _user.value = firebaseUser
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Login failed"
            }
        }
    }

    fun register(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.register(email, password)
            _isLoading.value = false

            result.onSuccess { firebaseUser ->
                _user.value = firebaseUser
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Registration failed"
            }
        }
    }
}