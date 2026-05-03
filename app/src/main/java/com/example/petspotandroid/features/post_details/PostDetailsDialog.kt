package com.example.petspotandroid.features.post_details

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petspotandroid.R
import com.example.petspotandroid.databinding.FragmentPostDetailsBinding
import com.example.petspotandroid.features.authentication.auth.AuthViewModel
import com.example.petspotandroid.features.comments.CommentsAdapter
import com.example.petspotandroid.features.user_info.UserProfileDialog
import com.example.petspotandroid.model.Comment
import com.example.petspotandroid.model.Post
import com.example.petspotandroid.model.User
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class PostDetailsDialog : DialogFragment() {

    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostDetailsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private var commentsAdapter: CommentsAdapter? = null
    private var currentPost: Post? = null
    private var currentUserProfile: User? = null

    companion object {
        private const val ARG_POST_ID = "arg_post_id"

        fun newInstance(postId: String): PostDetailsDialog {
            return PostDetailsDialog().apply {
                arguments = Bundle().apply { putString(ARG_POST_ID, postId) }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.userData.observe(viewLifecycleOwner) { user ->
            this.currentUserProfile = user
        }

        val currentUid = authViewModel.user.value?.uid
        currentUid?.let { authViewModel.refreshUserData() }

        val postId = arguments?.getString(ARG_POST_ID) ?: return dismiss()

        setupRecyclerView()

        viewModel.getPost(postId).observe(viewLifecycleOwner) { post ->
            post?.let {
                currentPost = it
                bindPostData(it)
            }
        }

        authViewModel.user.observe(viewLifecycleOwner) { firebaseUser ->
            binding.sendCommentButton.isEnabled = firebaseUser != null
        }

        val onPosterClicked = View.OnClickListener {
            currentPost?.let { post ->
                val profileDialog = UserProfileDialog()
                profileDialog.show(parentFragmentManager, "UserProfileDialog")
                profileDialog.setUserId(post.authorId)
            }
        }

        binding.posterName.setOnClickListener(onPosterClicked)
        binding.userProfileImage.setOnClickListener(onPosterClicked)

        binding.closeButton.setOnClickListener { dismiss() }
        binding.sendCommentButton.setOnClickListener { handleNewComment() }
    }

    private fun bindPostData(post: Post) {
        binding.posterName.text = post.userName
        binding.locationText.text = post.lastSeenLocation
        binding.seenOnText.text = post.eventDate
        binding.descriptionText.text = post.description

        val postedFormat = SimpleDateFormat("'Posted' dd/MM/yyyy", Locale.getDefault())
        binding.postedDate.text = postedFormat.format(Date(post.createdAt))

        val colorId = if (post.isLost) R.color.status_lost else R.color.status_found
        val color = ContextCompat.getColor(requireContext(), colorId)
        binding.statusBadge.text = "${getString(if (post.isLost) R.string.lost else R.string.found)} ${post.petType}"
        binding.statusBadge.setTextColor(color)
        binding.statusBadge.background?.mutate()?.apply {
            setTint(color)
            alpha = 40
        }

        Picasso.get().load(post.imageUrl).placeholder(android.R.drawable.ic_menu_camera).into(binding.postImage)
        Picasso.get().load(post.authorProfileImageUrl).placeholder(R.drawable.ic_person).into(binding.userProfileImage)

        binding.callButton.apply {
            text = getString(R.string.call_lister, post.contactNumber)
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_DIAL).apply { data = "tel:${post.contactNumber}".toUri() })
            }
        }

        commentsAdapter?.updateComments(post.comments)
        binding.commentsCountBadge.text = post.comments.size.toString()
    }

    private fun setupRecyclerView() {
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        commentsAdapter = CommentsAdapter(emptyList()) { commenterId ->
            val profileDialog = UserProfileDialog()
            profileDialog.setUserId(commenterId)
            profileDialog.show(parentFragmentManager, "UserProfileDialog")
        }
        binding.commentsRecyclerView.adapter = commentsAdapter
    }

    private fun handleNewComment() {
        val text = binding.commentEditText.text.toString().trim()
        val user = currentUserProfile // Use the observed value
        val userId = authViewModel.user.value?.uid
        val post = currentPost

        // If this hits return, the button does nothing.
        // Added a toast so you know why it's failing!
        if (text.isEmpty() || user == null || userId == null || post == null) {
            if (user == null) Toast.makeText(requireContext(), "Loading user profile...", Toast.LENGTH_SHORT).show()
            return
        }

        val comment = Comment(
            id = UUID.randomUUID().toString(),
            authorId = userId,
            authorName = "${user.firstName} ${user.lastName}",
            authorProfileImageUrl = user.avatarUrl ?: "",
            text = text,
            timestamp = System.currentTimeMillis()
        )

        viewModel.addComment(post, comment) { success ->
            if (success) {
                binding.commentEditText.text.clear()
            } else {
                Toast.makeText(requireContext(), "Failed to add comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.90).toInt()
            setLayout(width, height)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}