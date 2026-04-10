package com.example.petspotandroid.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petspotandroid.data.models.User
import com.example.petspotandroid.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _user = MutableLiveData<FirebaseUser?>()

    val user: LiveData<FirebaseUser?> = _user
    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    private val _resetPasswordSuccess = MutableLiveData<Boolean>()

    val resetPasswordSuccess: LiveData<Boolean> = _resetPasswordSuccess

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        val currentUser = repository.getCurrentUser()
        if (_user.value?.uid != currentUser?.uid) {
            _user.value = currentUser
        }

        if (currentUser != null) {
            loadUserData(currentUser.uid)
        } else {
            _userData.value = null
        }
    }

    fun refreshUserData() {
        val currentUser = repository.getCurrentUser()
        if (currentUser != null) {
            loadUserData(currentUser.uid)
        }
    }

    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            val result = repository.getUserData(userId)
            result.onSuccess { user ->
                if (_userData.value != user) {
                    _userData.value = user
                }
            }
        }
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
                loadUserData(firebaseUser.uid)
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
        phone: String,
        image: Bitmap? = null
    ) {
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank() || phone.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val userProfile = User(
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone
            )
            val result = repository.register(userProfile, password, image)
            _isLoading.value = false

            result.onSuccess { firebaseUser ->
                _user.value = firebaseUser
                loadUserData(firebaseUser.uid)
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Registration failed"
            }
        }
    }

    fun logout() {
        repository.logout()
        _user.value = null
        _userData.value = null
        _resetPasswordSuccess.value = false
    }

    fun clearResetPasswordStatus() {
        _resetPasswordSuccess.value = false
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Please enter your email address"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            val result = repository.resetPassword(email)
            _isLoading.value = false

            result.onSuccess {
                _resetPasswordSuccess.value = true
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Failed to send reset email"
                _resetPasswordSuccess.value = false
            }
        }
    }
}