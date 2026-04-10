package com.example.petspotandroid.features.authentication

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.petspotandroid.R
import com.example.petspotandroid.base.ToastHelper
import com.example.petspotandroid.dao.AppLocalDb
import com.example.petspotandroid.data.repository.AuthRepository
import com.example.petspotandroid.databinding.FragmentSignUpBinding
import com.example.petspotandroid.viewmodel.AuthViewModel

class SignUpFragment : Fragment() {
    private lateinit var viewModel: AuthViewModel
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    private var isImageSelected = false

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
                bitmap?.let {
                    binding.ivSelectedImage.setImageBitmap(it)
                    binding.tvUploadHint.text = "Photo selected"
                    binding.btnRemoveImage.visibility = View.VISIBLE
                    isImageSelected = true
                } ?: ToastHelper.showCustomToast(binding.root, "No image captured")
            }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                binding.ivSelectedImage.setImageURI(it)
                binding.tvUploadHint.text = "Photo selected"
                binding.btnRemoveImage.visibility = View.VISIBLE
                isImageSelected = true
            } ?: ToastHelper.showCustomToast(binding.root, "No image selected")
        }

        binding.btnUploadImage.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Choose your profile picture")

            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> cameraLauncher?.launch(null)
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
            isImageSelected = false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = AuthRepository(
            AppLocalDb.getDatabase(requireContext()).userDao(),
        )

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repository) as T
            }
        }

        viewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]

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

            val imageBitmap: Bitmap? = if (isImageSelected) {
                (binding.ivSelectedImage.drawable as? BitmapDrawable)?.bitmap
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

        viewModel.user.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                ToastHelper.showCustomToast(requireView(), "Registration Successful!")
                val navController = parentFragment?.findNavController()
                if (navController?.currentDestination?.id != R.id.postsListFragment) {
                    navController?.navigate(R.id.action_global_postsListFragment)
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
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