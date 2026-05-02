package com.example.petspotandroid.features.authentication.auth

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.petspotandroid.data.models.FirebaseAuthModel
import com.example.petspotandroid.model.User
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {
    private val firebaseAuthModel = FirebaseAuthModel.instance
    private val _isLoading = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    private val _resetPasswordSuccess = MutableLiveData<Boolean>()
    val resetPasswordSuccess: LiveData<Boolean> = _resetPasswordSuccess
    private val _updateProfileSuccess = MutableLiveData<Boolean>()
    val updateProfileSuccess: LiveData<Boolean> = _updateProfileSuccess

    val user: LiveData<FirebaseUser?> = firebaseAuthModel.user

    val userData: LiveData<User?> = user.switchMap { firebaseUser ->
        if (firebaseUser != null) {
            firebaseAuthModel.getUserById(firebaseUser.uid)
        } else {
            MutableLiveData(null)
        }
    }

    fun getUserData(userId: String): LiveData<User?> {
        return firebaseAuthModel.getUserById(userId)
    }

    fun refreshUserData(userId: String? = firebaseAuthModel.getCurrentUser()?.uid) {
        if (userId == null) return
        firebaseAuthModel.refreshUserData(userId)
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        firebaseAuthModel.login(email, password) { result ->
            _isLoading.value = false
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Login failed"
            }
        }
    }

    fun register(email: String, password: String, firstName: String, lastName: String, phone: String, imageUri: Uri?) {
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank() || phone.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        val userProfile = User(firstName = firstName, lastName = lastName, email = email, phone = phone)

        firebaseAuthModel.registerWithUri(userProfile, password, imageUri) { result ->
            _isLoading.value = false
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Registration failed"
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, phone: String, imageUri: Uri?) {
        if (firstName.isBlank() || lastName.isBlank() || phone.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        firebaseAuthModel.updateUserProfileWithUri(firstName, lastName, phone, imageUri) { result ->
            _isLoading.value = false
            if (result.isSuccess) {
                _updateProfileSuccess.value = true
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Update failed"
            }
        }
    }

    fun logout() {
        firebaseAuthModel.logout()
    }

    fun clearResetPasswordStatus() {
        _resetPasswordSuccess.value = false
    }

    fun clearUpdateProfileStatus() {
        _updateProfileSuccess.value = false
        _errorMessage.value = null
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Please enter your email address"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        firebaseAuthModel.checkEmailExists(email) { checkResult ->
            if (checkResult.isSuccess && checkResult.getOrDefault(false)) {
                firebaseAuthModel.resetPassword(email) { resetTask ->
                    _isLoading.value = false
                    if (resetTask.isSuccess) {
                        _resetPasswordSuccess.value = true
                    } else {
                        _errorMessage.value = resetTask.exceptionOrNull()?.message ?: "Failed to send reset link"
                    }
                }
            } else {
                _isLoading.value = false
                _errorMessage.value = if (checkResult.isFailure) {
                    checkResult.exceptionOrNull()?.message ?: "Error checking account"
                } else {
                    "No account found with this email address."
                }
            }
        }
    }
}