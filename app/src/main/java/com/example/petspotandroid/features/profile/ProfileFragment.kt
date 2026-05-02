package com.example.petspotandroid.features.profile

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petspotandroid.R
import com.example.petspotandroid.dao.AppLocalDB
import com.example.petspotandroid.data.repository.auth.AuthRepository
import com.example.petspotandroid.databinding.FragmentProfileBinding
import com.example.petspotandroid.features.authentication.auth.AuthViewModel
import com.example.petspotandroid.features.authentication.auth.AuthViewModelFactory
import com.example.petspotandroid.features.new_report.NewReportDialog
import com.example.petspotandroid.features.post_details.PostDetailsDialog
import com.example.petspotandroid.features.posts_list.PostsViewModel
import com.squareup.picasso.Picasso
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels {
        val repository = AuthRepository(AppLocalDB.db.userDao)
        AuthViewModelFactory(repository)
    }

    private val postsViewModel: PostsViewModel by viewModels()

    private lateinit var adapter: UserPostsAdapter
    private var isImageUpdated = false

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            binding.ivProfileImage.setImageBitmap(it)
            isImageUpdated = true
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.ivProfileImage.setImageURI(it)
            isImageUpdated = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = UserPostsAdapter(
            posts = emptyList(),
            onItemClick = { post ->
                if (!post.isResolved) {
                    PostDetailsDialog.newInstance(post.id).show(parentFragmentManager, "PostDetailsDialog")
                }
            },
            onEditClick = { post ->
                NewReportDialog.newInstance(post.id).show(parentFragmentManager, "EditReportDialog")
            },
            onDeleteClick = { post ->
                showDeleteConfirmation(post.id)
            },
            onResolveToggleClick = { post ->
                val updatedPost = post.copy(isResolved = !post.isResolved)
                postsViewModel.updatePost(updatedPost) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Status updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        binding.rvUserPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUserPosts.adapter = adapter
    }

    private fun setupObservers() {
        authViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUserName.text = "${it.firstName} ${it.lastName}"
                binding.tvEmail.text = user.email
                binding.tvPhone.text = user.phone

                val joinYear = Calendar.getInstance().apply {
                    timeInMillis = user.createdAt
                }.get(Calendar.YEAR)
                binding.tvMemberSince.text = getString(R.string.community_member_since, joinYear)

                if (!it.avatarUrl.isNullOrEmpty() && !isImageUpdated) {
                    Picasso.get()
                        .load(it.avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .fit()
                        .centerCrop()
                        .into(binding.ivProfileImage)
                } else if (!isImageUpdated) {
                    binding.ivProfileImage.setImageResource(R.drawable.ic_person)
                }

                postsViewModel.getMyPosts(it.id).observe(viewLifecycleOwner) { userPosts ->
                    adapter.setPosts(userPosts)
                    binding.tvReportsCount.text = userPosts.size.toString()
                    binding.tvListingsCount.text = userPosts.size.toString()
                    binding.tvReunionsCount.text = userPosts.count { p -> p.isResolved }.toString()
                }
            }
        }

        authViewModel.updateProfileSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                toggleEditMode(false)
                isImageUpdated = false
                authViewModel.clearUpdateProfileStatus()
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            val user = authViewModel.userData.value
            binding.etFirstName.setText(user?.firstName)
            binding.etLastName.setText(user?.lastName)
            binding.etPhone.setText(user?.phone)
            toggleEditMode(true)
        }

        binding.btnCancelEdit.setOnClickListener {
            toggleEditMode(false)
            isImageUpdated = false
            authViewModel.refreshUserData()
        }

        binding.btnSaveProfile.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            val imageBitmap: Bitmap? = if (isImageUpdated) {
                (binding.ivProfileImage.drawable as? BitmapDrawable)?.bitmap
            } else {
                null
            }
            authViewModel.updateProfile(firstName, lastName, phone, imageBitmap)
        }

        binding.ivProfileImage.setOnClickListener {
            if (binding.btnSaveProfile.isVisible) showImageSourceDialog()
        }
    }

    private fun toggleEditMode(isEdit: Boolean) {
        binding.btnEditProfile.isVisible = !isEdit
        binding.btnCancelEdit.isVisible = isEdit
        binding.btnSaveProfile.isVisible = isEdit
        binding.tvUserName.isVisible = !isEdit
        binding.llEditName.isVisible = isEdit
        binding.tvPhone.isVisible = !isEdit
        binding.tilPhone.isVisible = isEdit
        binding.ivCameraOverlay.isVisible = isEdit
        binding.vImageDimOverlay.isVisible = isEdit
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Gallery", "Cancel")
        AlertDialog.Builder(requireContext())
            .setTitle("Update Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher.launch(null)
                    1 -> galleryLauncher.launch("image/*")
                }
            }.show()
    }

    private fun showDeleteConfirmation(postId: String) {
        val post = adapter.getPosts().find { it.id == postId } ?: return

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this report?")
            .setPositiveButton("Delete") { _, _ ->
                postsViewModel.deletePost(post) { success, _ ->
                    val msg = if (success) "Deleted" else "Delete failed"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}