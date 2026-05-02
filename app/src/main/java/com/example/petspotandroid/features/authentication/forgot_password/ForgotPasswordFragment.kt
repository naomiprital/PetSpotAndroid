package com.example.petspotandroid.features.authentication.forgot_password

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.petspotandroid.R
import com.example.petspotandroid.base.ToastHelper
import com.example.petspotandroid.features.authentication.auth.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    private var viewModel: AuthViewModel? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etResetEmail = view.findViewById<TextInputEditText>(R.id.etResetEmail)
        val btnSendResetLink = view.findViewById<MaterialButton>(R.id.btnSendResetLink)
        val tvBackToLogin = view.findViewById<TextView>(R.id.tvBackToLogin)
        val tvResend = view.findViewById<TextView>(R.id.tvResend)

        btnSendResetLink.setOnClickListener {
            val email = etResetEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel?.resetPassword(email)
            } else {
                ToastHelper.showCustomToast(view, "Please enter your email address.")
            }
        }

        tvResend.setOnClickListener {
            val email = etResetEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel?.resetPassword(email)
                ToastHelper.showCustomToast(view, "Attempting to resend...")
            } else {
                ToastHelper.showCustomToast(view, "Please enter your email address first.")
            }
        }

        tvBackToLogin.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel?.resetPasswordSuccess?.observe(viewLifecycleOwner) { success ->
            if (success) {
                ToastHelper.showCustomToast(requireView(), "Reset link sent! Check your inbox.")
                viewModel?.clearResetPasswordStatus()
            }
        }

        viewModel?.errorMessage?.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                ToastHelper.showCustomToast(requireView(), message)
            }
        }
    }
}
