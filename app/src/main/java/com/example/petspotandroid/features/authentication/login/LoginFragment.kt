package com.example.petspotandroid.features.authentication.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.petspotandroid.R
import com.example.petspotandroid.databinding.FragmentLoginBinding
import com.example.petspotandroid.features.authentication.auth.AuthFragmentDirections
import com.example.petspotandroid.features.authentication.auth.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            val action = AuthFragmentDirections.actionAuthFragmentToForgotPasswordFragment()
            findNavController().navigate(action)
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                disableLoginButton()
            } else {
                enableLoginButton()
            }
        }

        viewModel.user.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                findNavController().navigate(R.id.action_global_postsListFragment)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.resetPasswordSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Password reset email sent!", Toast.LENGTH_SHORT).show()
                viewModel.clearResetPasswordStatus()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun disableLoginButton() {
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Logging in..."
    }

    @SuppressLint("SetTextI18n")
    private fun enableLoginButton() {
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = "Welcome Back!"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}