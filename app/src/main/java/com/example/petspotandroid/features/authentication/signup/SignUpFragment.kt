package com.example.petspotandroid.features.authentication.signup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.petspotandroid.R
import com.example.petspotandroid.dao.AppLocalDB
import com.example.petspotandroid.data.repository.auth.AuthRepository
import com.example.petspotandroid.databinding.FragmentSignUpBinding
import com.example.petspotandroid.features.authentication.auth.AuthViewModel
import com.example.petspotandroid.features.authentication.auth.AuthViewModelFactory

class SignUpFragment : Fragment() {

    private var binding: FragmentSignUpBinding? = null
    private var isImageSelected = false

    private val viewModel: AuthViewModel by viewModels {
        val userDao = AppLocalDB.db.userDao
        val repository = AuthRepository(userDao)
        AuthViewModelFactory(repository)
    }

    @SuppressLint("SetTextI18n")
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            binding?.ivSelectedImage?.setImageBitmap(it)
            binding?.tvUploadHint?.text = "Photo selected"
            binding?.btnRemoveImage?.visibility = View.VISIBLE
            isImageSelected = true
        }
    }

    @SuppressLint("SetTextI18n")
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding?.ivSelectedImage?.setImageURI(it)
            binding?.tvUploadHint?.text = "Photo selected"
            binding?.btnRemoveImage?.visibility = View.VISIBLE
            isImageSelected = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding?.btnUploadImage?.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
            AlertDialog.Builder(requireContext())
                .setTitle("Choose your profile picture")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> cameraLauncher.launch(null)
                        1 -> galleryLauncher.launch("image/*")
                        2 -> dialog.dismiss()
                    }
                }
                .show()
        }

        binding?.btnRemoveImage?.setOnClickListener {
            binding?.ivSelectedImage?.setImageResource(R.drawable.ic_cloud_upload)
            binding?.tvUploadHint?.text = getString(R.string.tap_to_upload_your_photo)
            binding?.btnRemoveImage?.visibility = View.GONE
            isImageSelected = false
        }

        binding?.btnSignUp?.setOnClickListener {
            handleSignUp()
        }
    }

    private fun handleSignUp() {
        val firstName = binding?.etFirstName?.text.toString().trim()
        val lastName = binding?.etLastName?.text.toString().trim()
        val phone = binding?.etPhone?.text.toString().trim()
        val email = binding?.etEmail?.text.toString().trim()
        val password = binding?.etPassword?.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val imageBitmap: Bitmap? = if (isImageSelected) {
            (binding?.ivSelectedImage?.drawable as? BitmapDrawable)?.bitmap
        } else {
            null
        }

        viewModel.register(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            image = imageBitmap
        )
    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                Toast.makeText(requireContext(), "Registration Successful!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_global_postsListFragment)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding?.btnSignUp?.isEnabled = !isLoading
            binding?.btnSignUp?.text = if (isLoading) "Creating Account..." else "Sign Up"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}