package com.example.petspotandroid.features.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.petspotandroid.R
import com.example.petspotandroid.base.ToastHelper
import com.example.petspotandroid.dao.AppLocalDb
import com.example.petspotandroid.data.repository.AuthRepository
import com.example.petspotandroid.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var viewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = AuthRepository(
            AppLocalDb.getDatabase(requireContext()).userDao()
        )

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repository) as T
            }
        }

        viewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tvForgotPassword)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                ToastHelper.showCustomToast(view, "Please fill out all fields")
            }
        }

        tvForgotPassword.visibility = View.VISIBLE
        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.resetPassword(email)
            } else {
                ToastHelper.showCustomToast(view, "Please enter your email to reset password")
            }
        }

        viewModel.resetPasswordSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                ToastHelper.showCustomToast(requireView(), "Password reset email sent!")
                viewModel.clearResetPasswordStatus()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            btnLogin.isEnabled = !isLoading
            btnLogin.text = if (isLoading) "Logging in..." else "Welcome Back!"
        }

        viewModel.user.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                val navController = parentFragment?.findNavController()
                if (navController?.currentDestination?.id != R.id.postsListFragment) {
                    navController?.navigate(R.id.action_global_postsListFragment)
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                ToastHelper.showCustomToast(requireView(), message)
            }
        }
    }
}