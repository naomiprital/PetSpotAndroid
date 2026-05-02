package com.example.petspotandroid.features.authentication.signup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.petspotandroid.R
import com.example.petspotandroid.base.ToastHelper
import com.example.petspotandroid.databinding.FragmentSignUpBinding
import com.example.petspotandroid.features.authentication.auth.AuthViewModel
import java.io.File

class SignUpFragment : Fragment() {
    private var viewModel: AuthViewModel? = null
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    
    private var cameraLauncher: ActivityResultLauncher<Uri>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    
    private var selectedImageUri: Uri? = null
    private var tempCameraUri: Uri? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempCameraUri != null) {
                binding.ivSelectedImage.setImageURI(tempCameraUri)
                selectedImageUri = tempCameraUri
                binding.tvUploadHint.text = "Photo selected"
                binding.btnRemoveImage.visibility = View.VISIBLE
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                binding.ivSelectedImage.setImageURI(it)
                selectedImageUri = it
                binding.tvUploadHint.text = "Photo selected"
                binding.btnRemoveImage.visibility = View.VISIBLE
            }
        }

        binding.btnUploadImage.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Choose your profile picture")

            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> galleryLauncher?.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }

            builder.show()
        }

        binding.btnRemoveImage.setOnClickListener {
            binding.ivSelectedImage.setImageResource(R.drawable.ic_cloud_upload)
            binding.tvUploadHint.text = getString(R.string.tap_to_upload_your_photo)
            binding.btnRemoveImage.visibility = View.GONE
            selectedImageUri = null
        }

        return binding.root
    }

    private fun launchCamera() {
        val photoFile = File(requireContext().cacheDir, "temp_signup_image_${System.currentTimeMillis()}.jpg")
        tempCameraUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher?.launch(tempCameraUri!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignUp.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                ToastHelper.showCustomToast(view, "Please fill all fields")
                return@setOnClickListener
            }

            viewModel?.register(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                imageUri = selectedImageUri
            )
        }

        viewModel?.user?.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                ToastHelper.showCustomToast(requireView(), "Registration Successful!")
                val navController = findNavController()
                if (navController.currentDestination?.id != R.id.postsListFragment) {
                    navController.navigate(R.id.action_global_postsListFragment)
                }
            }
        }

        viewModel?.errorMessage?.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                ToastHelper.showCustomToast(requireView(), message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
