package com.example.petspotandroid.features.authentication.login

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.petspotandroid.R
import com.example.petspotandroid.base.ToastHelper
import com.example.petspotandroid.features.authentication.auth.AuthFragmentDirections
import com.example.petspotandroid.features.authentication.auth.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment(R.layout.fragment_login) {
    private var viewModel: AuthViewModel? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tvForgotPassword)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            viewModel?.login(email, password)
        }

        tvForgotPassword.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                AuthFragmentDirections.actionAuthFragmentToForgotPasswordFragment()
            )
        )

        viewModel?.user?.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                // Navigate to feed on success
                findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToFeedFragment())
            }
        }

        viewModel?.errorMessage?.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                ToastHelper.showCustomToast(requireView(), message)
            }
        }
    }
}
