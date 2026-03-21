package com.example.petspotandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petspotandroid.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _user = MutableLiveData<FirebaseUser?>()

    val user: LiveData<FirebaseUser?> = _user
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // TODO: Check if user is already logged in when app starts
    fun checkCurrentUser() {
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            val result = repository.login(email, password)

            _isLoading.value = false

            result.onSuccess { firebaseUser ->
                _user.value = firebaseUser
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Login failed"
            }
        }
    }

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ) {
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank() || phone.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.register(email, password, firstName, lastName, phone)
            _isLoading.value = false

            result.onSuccess { firebaseUser ->
//                TODO: Set the logged in user
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Registration failed"
            }
        }
    }
}