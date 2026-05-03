package com.example.petspotandroid.features.authentication.auth

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.ViewModel
import com.example.petspotandroid.data.repository.auth.AuthRepository
import com.example.petspotandroid.model.User
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository.instance

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    val userData: LiveData<User?> = _user.switchMap { firebaseUser ->
        if (firebaseUser != null) {
            repository.refreshUserData(firebaseUser.uid)
            repository.getUserLiveData(firebaseUser.uid)
        } else {
            MutableLiveData(null)
        }
    }

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _resetPasswordSuccess = MutableLiveData<Boolean>()
    val resetPasswordSuccess: LiveData<Boolean> = _resetPasswordSuccess

    private val _updateProfileSuccess = MutableLiveData<Boolean>()
    val updateProfileSuccess: LiveData<Boolean> = _updateProfileSuccess

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        val currentUser = repository.getCurrentUser()
        if (currentUser != null && _user.value?.uid != currentUser.uid) {
            _user.value = currentUser
        }
    }

    fun refreshUserData() {
        val currentUser = repository.getCurrentUser()
        if (currentUser != null) {
            _user.value = currentUser
            repository.refreshUserData(currentUser.uid)
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true

        repository.login(email, password) { result ->
            _isLoading.postValue(false)

            result.onSuccess { firebaseUser ->
                _user.postValue(firebaseUser)
            }.onFailure { exception ->
                _errorMessage.postValue(exception.message ?: "Login failed")
            }
        }
    }

    fun register(
        user: User,
        password: String,
        image: Bitmap? = null
    ) {
        if (user.email.isBlank() || password.isBlank() || user.firstName.isBlank() ||
            user.lastName.isBlank() || user.phone.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true

        repository.register(user, password, image) { result ->
            _isLoading.postValue(false)

            result.onSuccess { firebaseUser ->
                _user.postValue(firebaseUser)
            }.onFailure { exception ->
                _errorMessage.postValue(exception.message ?: "Registration failed")
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, phone: String, image: Bitmap?) {
        if (firstName.isBlank() || lastName.isBlank() || phone.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true

        repository.updateUserProfile(firstName, lastName, phone, image) { result ->
            _isLoading.postValue(false)

            result.onSuccess {
                _updateProfileSuccess.postValue(true)
            }.onFailure { exception ->
                _errorMessage.postValue(exception.message ?: "Update failed")
            }
        }
    }

    fun clearUpdateProfileStatus() {
        _updateProfileSuccess.value = false
        _errorMessage.value = null
    }

    fun logout() {
        repository.logout()
        _user.value = null
        _resetPasswordSuccess.value = false
    }

    fun clearResetPasswordStatus() {
        _resetPasswordSuccess.value = false
    }

    fun resetPassword(email: String) {
        _isLoading.value = true
        _errorMessage.value = null

        repository.checkEmailExists(email) { checkResult ->
            if (checkResult.isSuccess) {
                val emailExists = checkResult.getOrNull() == true

                if (emailExists) {
                    repository.resetPassword(email) { resetResult ->
                        _isLoading.postValue(false)
                        if (resetResult.isSuccess) {
                            _resetPasswordSuccess.postValue(true)
                        } else {
                            _errorMessage.postValue("Failed to send reset link: ${resetResult.exceptionOrNull()?.message}")
                        }
                    }
                } else {
                    _isLoading.postValue(false)
                    _errorMessage.postValue("No account found with this email address.")
                }
            } else {
                _isLoading.postValue(false)
                _errorMessage.postValue("Error checking account: ${checkResult.exceptionOrNull()?.message}")
            }
        }
    }
}