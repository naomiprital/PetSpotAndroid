package com.example.petspotandroid.features.profile

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petspotandroid.R
import com.example.petspotandroid.base.ToastHelper
import com.example.petspotandroid.features.post_details.PostDetailsDialog
import com.example.petspotandroid.features.new_report.NewReportDialog
import com.example.petspotandroid.features.authentication.auth.AuthViewModel
import com.example.petspotandroid.features.posts_list.PostsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import java.io.File
import java.util.Calendar

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val authViewModel: AuthViewModel by activityViewModels()
    private val postsViewModel: PostsViewModel by viewModels()
    private lateinit var adapter: UserPostsAdapter
    private var cameraLauncher: ActivityResultLauncher<Uri>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    private var isImageUpdated = false
    private var selectedImageUri: Uri? = null
    private var tempCameraUri: Uri? = null

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

        adapter = UserPostsAdapter(
            posts = emptyList(),
            onItemClick = { post ->
                if (!post.isResolved) {
                    val dialog = PostDetailsDialog(post)
                    dialog.show(parentFragmentManager, "PostDetailsDialog")
                }
            },
            onEditClick = { post ->
                val dialog = NewReportDialog.newInstance(post)
                dialog.show(parentFragmentManager, "EditReportDialog")
            },
            onDeleteClick = { post ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.delete_post_title)
                    .setMessage(R.string.delete_post_message)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        postsViewModel.deletePost(post) { _, messageRes ->
                            ToastHelper.showCustomToast(requireView(), getString(messageRes))
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            },
            onResolveToggleClick = { post ->
                val updatedPost = post.copy(isResolved = !post.isResolved)
                postsViewModel.updatePost(updatedPost) { success, messageRes ->
                    if (success) {
                        val statusRes =
                            if (updatedPost.isResolved) R.string.listing_marked_as_resolved_success else R.string.listing_marked_as_unresolved_success
                        ToastHelper.showCustomToast(requireView(), getString(statusRes))
                    } else {
                        ToastHelper.showCustomToast(requireView(), getString(messageRes))
                    }
                }
            }
        )
        rvUserPosts.layoutManager = LinearLayoutManager(requireContext())
        rvUserPosts.adapter = adapter

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempCameraUri != null) {
                ivProfileImage.setImageURI(tempCameraUri)
                selectedImageUri = tempCameraUri
                isImageUpdated = true
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                ivProfileImage.setImageURI(it)
                selectedImageUri = it
                isImageUpdated = true
            }
        }

        authViewModel.user.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                authViewModel.getUserData(firebaseUser.uid).observe(viewLifecycleOwner) { user ->
                    user?.let {
                        tvUserName.text = "${it.firstName} ${it.lastName}"
                        tvEmail.text = it.email
                        tvPhone.text = it.phone

                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = it.createdAt
                        val year = calendar.get(Calendar.YEAR)
                        tvMemberSince.text = getString(R.string.community_member_since, year)

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

                            val reunions = posts.count { post -> post.isResolved }
                            tvReunionsCount.text = reunions.toString()
                        }
                    }
                }
            }
        }

        btnEditProfile.setOnClickListener {
            toggleEditMode(true,
                btnEditProfile, btnCancelEdit, btnSaveProfile,
                tvUserName, llEditName, tvPhone, tilPhone,
                ivCameraOverlay, vImageDimOverlay)

            val firebaseUser = authViewModel.user.value
            if (firebaseUser != null) {
                authViewModel.getUserData(firebaseUser.uid).observe(viewLifecycleOwner) { user ->
                    user?.let {
                        etFirstName.setText(it.firstName)
                        etLastName.setText(it.lastName)
                        etPhone.setText(it.phone)
                    }
                }
            }
        }

        btnCancelEdit.setOnClickListener {
            toggleEditMode(false,
                btnEditProfile, btnCancelEdit, btnSaveProfile,
                tvUserName, llEditName, tvPhone, tilPhone,
                ivCameraOverlay, vImageDimOverlay)
            isImageUpdated = false
            selectedImageUri = null
            authViewModel.refreshUserData()
        }

        btnSaveProfile.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                ToastHelper.showCustomToast(requireView(), "Please fill all fields")
                return@setOnClickListener
            }

            btnSaveProfile.text = getString(R.string.saving)
            btnSaveProfile.isEnabled = false

            authViewModel.updateProfile(firstName, lastName, phone, selectedImageUri)
        }

        authViewModel.updateProfileSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                ToastHelper.showCustomToast(requireView(), getString(R.string.profile_updated_successfully))

                btnSaveProfile.text = getString(R.string.save_changes)
                btnSaveProfile.isEnabled = true

                toggleEditMode(false,
                    btnEditProfile, btnCancelEdit, btnSaveProfile,
                    tvUserName, llEditName, tvPhone, tilPhone,
                    ivCameraOverlay, vImageDimOverlay)

                isImageUpdated = false
                selectedImageUri = null
                authViewModel.clearUpdateProfileStatus()
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                ToastHelper.showCustomToast(requireView(), message)

                btnSaveProfile.text = getString(R.string.save_changes)
                btnSaveProfile.isEnabled = true
            }
        }

        val showImageOptions = {
            val options = arrayOf(getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(
                R.string.cancel))
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.update_profile_picture_title)
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> galleryLauncher?.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            builder.show()
        }

        ivProfileImage.setOnClickListener {
            if (btnSaveProfile.isVisible) {
                showImageOptions()
            }
        }
    }

    private fun launchCamera() {
        val photoFile = File(requireContext().cacheDir, "profile_camera_${System.currentTimeMillis()}.jpg")
        tempCameraUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher?.launch(tempCameraUri!!)
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
