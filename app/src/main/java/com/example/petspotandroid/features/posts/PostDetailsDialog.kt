package com.example.petspotandroid.features.posts

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.petspotandroid.R
import com.example.petspotandroid.data.models.Post
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petspotandroid.adapter.CommentsAdapter
import com.example.petspotandroid.data.models.Comment
import com.example.petspotandroid.features.profile.UserProfileDialog
import com.example.petspotandroid.viewmodel.AuthViewModel
import com.example.petspotandroid.viewmodel.PostsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class PostDetailsDialog(private val post: Post) : DialogFragment() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var postsViewModel: PostsViewModel
    private lateinit var commentsAdapter: CommentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        postsViewModel = ViewModelProvider(requireActivity())[PostsViewModel::class.java]

        setupStaticUi(view)
        setupPostImages(view)
        setupCommentsSection(view)
        setupClickListeners(view)
    }

    private fun setupStaticUi(view: View) {
        view.findViewById<TextView>(R.id.posterName).text = post.userName
        view.findViewById<TextView>(R.id.locationText).text = post.lastSeenLocation
        view.findViewById<TextView>(R.id.seenOnText).text = post.eventDate
        view.findViewById<TextView>(R.id.descriptionText).text = post.description

        val postedFormat = SimpleDateFormat("'Posted' dd/MM/yyyy", Locale.getDefault())
        view.findViewById<TextView>(R.id.postedDate).text = postedFormat.format(Date(post.createdAt))

        setupStatusBadge(view)
    }

    @SuppressLint("SetTextI18n")
    private fun setupStatusBadge(view: View) {
        val statusBadge = view.findViewById<TextView>(R.id.statusBadge)
        val badgeTextId = if (post.isLost) R.string.lost else R.string.found
        val badgeColorId = if (post.isLost) R.color.status_lost else R.color.status_found
        val color = ContextCompat.getColor(requireContext(), badgeColorId)

        statusBadge.text = "${getString(badgeTextId)} ${post.petType}"
        statusBadge.setTextColor(color)
        statusBadge.background?.mutate()?.let {
            it.setTint(color)
            it.alpha = 40
        }
    }

    private fun setupPostImages(view: View) {
        val postImage = view.findViewById<ImageView>(R.id.postImage)
        val profileImageView = view.findViewById<ImageView>(R.id.userProfileImage)

        val imageUrl = post.imageUrl.ifEmpty { null }
        Picasso.get()
            .load(imageUrl)
            .fit()
            .centerCrop()
            .placeholder(android.R.drawable.ic_menu_camera)
            .error(android.R.drawable.ic_menu_camera)
            .into(postImage)

        val profileUrl = post.authorProfileImageUrl.ifEmpty { null }
        Picasso.get()
            .load(profileUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .fit()
            .centerCrop()
            .into(profileImageView)
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<ImageButton>(R.id.closeButton).setOnClickListener { dismiss() }

        val onProfileClick = View.OnClickListener { openUserProfile(post.authorId) }
        view.findViewById<TextView>(R.id.posterName).setOnClickListener(onProfileClick)
        view.findViewById<TextView>(R.id.listerInfoTitle).setOnClickListener(onProfileClick)
        view.findViewById<ImageView>(R.id.userProfileImage).setOnClickListener(onProfileClick)

        view.findViewById<MaterialButton>(R.id.callButton).apply {
            text = getString(R.string.call_lister, post.contactNumber)
            setOnClickListener {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = "tel:${post.contactNumber}".toUri()
                }
                startActivity(dialIntent)
            }
        }
    }

    private fun setupCommentsSection(view: View) {
        val commentsRecycler = view.findViewById<RecyclerView>(R.id.commentsRecyclerView)
        val countBadge = view.findViewById<TextView>(R.id.commentsCountBadge)
        val commentInput = view.findViewById<EditText>(R.id.commentEditText)
        val sendButton = view.findViewById<ImageButton>(R.id.sendCommentButton)

        commentsRecycler.layoutManager = LinearLayoutManager(requireContext())
        commentsAdapter = CommentsAdapter(post.comments) { commenterId ->
            openUserProfile(commenterId)
        }
        commentsRecycler.adapter = commentsAdapter
        countBadge.text = post.comments.size.toString()

        sendButton.setOnClickListener {
            handleNewComment(commentInput, countBadge)
        }
    }

    private fun handleNewComment(input: EditText, countBadge: TextView) {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return

        val currentUser = authViewModel.userData.value
        val currentUserId = authViewModel.user.value?.uid

        if (currentUser != null && currentUserId != null) {
            val newComment = Comment(
                id = UUID.randomUUID().toString(),
                authorId = currentUserId,
                authorName = "${currentUser.firstName} ${currentUser.lastName}",
                authorProfileImageUrl = currentUser.avatarUrl ?: "",
                text = text,
                timestamp = System.currentTimeMillis()
            )

            post.comments = post.comments.toMutableList().apply { add(newComment) }
            commentsAdapter.updateComments(post.comments)
            countBadge.text = post.comments.size.toString()
            input.text.clear()
            postsViewModel.updatePost(post)
        } else {
            Toast.makeText(requireContext(), "Must be logged in to comment", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUserProfile(userId: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(com.example.petspotandroid.data.models.User::class.java)
                if (user != null) {
                    UserProfileDialog().apply {
                        setUser(user)
                        show(this@PostDetailsDialog.parentFragmentManager, "UserProfileDialog")
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error loading profile", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.90).toInt()
        dialog?.window?.setLayout(width, height)
    }
}