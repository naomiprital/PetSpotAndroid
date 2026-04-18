package com.example.petspotandroid.features.posts

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petspotandroid.R
import com.example.petspotandroid.adapter.PostsAdapter
import com.example.petspotandroid.base.ToastHelper
import com.example.petspotandroid.dao.AppLocalDb
import com.example.petspotandroid.data.repository.AuthRepository
import com.example.petspotandroid.viewmodel.AuthViewModel
import com.example.petspotandroid.viewmodel.AuthViewModelFactory
import com.example.petspotandroid.viewmodel.PostsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import java.util.Calendar

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val authViewModel: AuthViewModel by viewModels {
        val userDao = AppLocalDb.getDatabase(requireContext()).userDao()
        val repository = AuthRepository(userDao)
        AuthViewModelFactory(repository)
    }

    private val postsViewModel: PostsViewModel by viewModels()

    private lateinit var adapter: PostsAdapter
    
    private var cameraLauncher: ActivityResultLauncher<Void?>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    private var isImageUpdated = false

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivProfileImage = view.findViewById<ImageView>(R.id.ivProfileImage)
        val ivCameraOverlay = view.findViewById<ImageView>(R.id.ivCameraOverlay)
        val vImageDimOverlay = view.findViewById<View>(R.id.vImageDimOverlay)
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvMemberSince = view.findViewById<TextView>(R.id.tvMemberSince)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val tvReportsCount = view.findViewById<TextView>(R.id.tvReportsCount)
        val tvReunionsCount = view.findViewById<TextView>(R.id.tvReunionsCount)
        val tvListingsCount = view.findViewById<TextView>(R.id.tvListingsCount)
        val rvUserPosts = view.findViewById<RecyclerView>(R.id.rvUserPosts)
        
        val btnEditProfile = view.findViewById<MaterialButton>(R.id.btnEditProfile)
        val btnCancelEdit = view.findViewById<MaterialButton>(R.id.btnCancelEdit)
        val btnSaveProfile = view.findViewById<MaterialButton>(R.id.btnSaveProfile)

        val tvPhone = view.findViewById<TextView>(R.id.tvPhone)
        
        val llEditName = view.findViewById<View>(R.id.llEditName)
        val tilPhone = view.findViewById<TextInputLayout>(R.id.tilPhone)
        
        val etFirstName = view.findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = view.findViewById<TextInputEditText>(R.id.etLastName)
        val etPhone = view.findViewById<TextInputEditText>(R.id.etPhone)

        adapter = PostsAdapter(emptyList()) { _ -> }
        rvUserPosts.layoutManager = LinearLayoutManager(requireContext())
        rvUserPosts.adapter = adapter

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                ivProfileImage.setImageBitmap(it)
                isImageUpdated = true
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                ivProfileImage.setImageURI(it)
                isImageUpdated = true
            }
        }

        authViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                tvUserName.text = "${it.firstName} ${it.lastName}"
                tvEmail.text = it.email
                tvPhone.text = it.phone

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it.createdAt
                val year = calendar.get(Calendar.YEAR)
                tvMemberSince.text = "Community Member Since $year"

                if (!it.avatarUrl.isNullOrEmpty() && !isImageUpdated) {
                    Picasso.get()
                        .load(it.avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .fit()
                        .centerCrop()
                        .into(ivProfileImage)
                } else if (it.avatarUrl.isNullOrEmpty() && !isImageUpdated) {
                    ivProfileImage.setImageResource(R.drawable.ic_person)
                }

                postsViewModel.getMyPosts(it.id).observe(viewLifecycleOwner) { posts ->
                    adapter.setPosts(posts)
                    tvReportsCount.text = posts.size.toString()
                    tvListingsCount.text = posts.size.toString()
                    
                    val reunions = posts.count { post -> !post.isLost }
                    tvReunionsCount.text = reunions.toString()
                }
            }
        }

        btnEditProfile.setOnClickListener {
            toggleEditMode(true, 
                btnEditProfile, btnCancelEdit, btnSaveProfile, 
                tvUserName, llEditName, tvPhone, tilPhone,
                ivCameraOverlay, vImageDimOverlay)
            
            val user = authViewModel.userData.value
            etFirstName.setText(user?.firstName)
            etLastName.setText(user?.lastName)
            etPhone.setText(user?.phone)
        }

        btnCancelEdit.setOnClickListener {
            toggleEditMode(false, 
                btnEditProfile, btnCancelEdit, btnSaveProfile, 
                tvUserName, llEditName, tvPhone, tilPhone,
                ivCameraOverlay, vImageDimOverlay)
            isImageUpdated = false
            authViewModel.refreshUserData()
        }

        btnSaveProfile.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            val imageBitmap: Bitmap? = if (isImageUpdated) {
                (ivProfileImage.drawable as? BitmapDrawable)?.bitmap
            } else {
                null
            }

            authViewModel.updateProfile(firstName, lastName, phone, imageBitmap)
        }

        authViewModel.updateProfileSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                ToastHelper.showCustomToast(requireView(), "Profile updated successfully!")
                toggleEditMode(false, 
                    btnEditProfile, btnCancelEdit, btnSaveProfile, 
                    tvUserName, llEditName, tvPhone, tilPhone,
                    ivCameraOverlay, vImageDimOverlay)
                isImageUpdated = false
                authViewModel.clearUpdateProfileStatus()
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                ToastHelper.showCustomToast(requireView(), message)
            }
        }

        val showImageOptions = {
            val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Update profile picture")
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> cameraLauncher?.launch(null)
                    1 -> galleryLauncher?.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            builder.show()
        }

        ivProfileImage.setOnClickListener {
            if (btnSaveProfile.visibility == View.VISIBLE) {
                showImageOptions()
            }
        }
    }

    private fun toggleEditMode(
        isEdit: Boolean,
        btnEdit: View, btnCancel: View, btnSave: View,
        tvName: View, editName: View, tvPh: View, tilPh: View,
        cameraOverlay: View, dimOverlay: View
    ) {
        btnEdit.visibility = if (isEdit) View.GONE else View.VISIBLE
        btnCancel.visibility = if (isEdit) View.VISIBLE else View.GONE
        btnSave.visibility = if (isEdit) View.VISIBLE else View.GONE
        
        tvName.visibility = if (isEdit) View.GONE else View.VISIBLE
        editName.visibility = if (isEdit) View.VISIBLE else View.GONE
        
        tvPh.visibility = if (isEdit) View.GONE else View.VISIBLE
        tilPh.visibility = if (isEdit) View.VISIBLE else View.GONE
        
        cameraOverlay.visibility = if (isEdit) View.VISIBLE else View.GONE
        dimOverlay.visibility = if (isEdit) View.VISIBLE else View.GONE
    }
}