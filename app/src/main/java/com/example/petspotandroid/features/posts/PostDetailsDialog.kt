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
import com.example.petspotandroid.viewmodel.AuthViewModel
import com.example.petspotandroid.viewmodel.PostsViewModel
import java.util.UUID

class PostDetailsDialog(private val post: Post) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_details, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val closeButton = view.findViewById<ImageButton>(R.id.closeButton)
        val posterName = view.findViewById<TextView>(R.id.posterName)
        val listerInfoButton = view.findViewById<TextView>(R.id.listerInfoTitle)
        val locationText = view.findViewById<TextView>(R.id.locationText)
        val seenOnText = view.findViewById<TextView>(R.id.seenOnText)
        val descriptionText = view.findViewById<TextView>(R.id.descriptionText)
        val postedDate = view.findViewById<TextView>(R.id.postedDate)
        val statusBadge = view.findViewById<TextView>(R.id.statusBadge)
        val postImage = view.findViewById<ImageView>(R.id.postImage)
        val profileImageView = view.findViewById<ImageView>(R.id.userProfileImage)
        val callButton = view.findViewById<MaterialButton>(R.id.callButton)

        closeButton.setOnClickListener { dismiss() }

        posterName.text = post.userName
        listerInfoButton.setOnClickListener {
            Toast.makeText(requireContext(), "Profile feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        locationText.text = post.lastSeenLocation
        seenOnText.text = post.eventDate
        descriptionText.text = post.description

        val postedFormat = SimpleDateFormat("'Posted' dd/MM/yyyy", Locale.getDefault())
        postedDate.text = postedFormat.format(Date(post.createdAt))

        val badgeTextId = if (post.isLost) R.string.lost else R.string.found
        val badgeColorId = if (post.isLost) R.color.status_lost else R.color.status_found
        val color = ContextCompat.getColor(requireContext(), badgeColorId)

        statusBadge.text = "${getString(badgeTextId)} ${post.petType}"
        statusBadge.setTextColor(color)

        statusBadge.background?.mutate()?.let {
            it.setTint(color)
            it.alpha = 40
        }


        if (post.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(post.imageUrl)
                .fit()
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_camera)
                .error(android.R.drawable.ic_menu_camera)
                .into(postImage)
        } else {
            postImage.setImageResource(android.R.drawable.ic_menu_camera)
        }

        if (post.authorProfileImageUrl.isNotEmpty()) {
            Picasso.get()
                .load(post.authorProfileImageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .fit()
                .centerCrop()
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.ic_person)
        }

        callButton.text = getString(R.string.call_lister, post.contactNumber)
        callButton.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:${post.contactNumber}".toUri()
            }
            startActivity(dialIntent)
        }

        val authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        val postsViewModel = ViewModelProvider(requireActivity())[PostsViewModel::class.java]

        val commentsRecycler = view.findViewById<RecyclerView>(R.id.commentsRecyclerView)
        val commentInput = view.findViewById<EditText>(R.id.commentEditText)
        val sendButton = view.findViewById<ImageButton>(R.id.sendCommentButton)
        val countBadge = view.findViewById<TextView>(R.id.commentsCountBadge)

        commentsRecycler.layoutManager = LinearLayoutManager(requireContext())
        val commentsAdapter = CommentsAdapter(post.comments)
        commentsRecycler.adapter = commentsAdapter
        countBadge.text = post.comments.size.toString()

        sendButton.setOnClickListener {
            val text = commentInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

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

                val updatedComments = post.comments.toMutableList()
                updatedComments.add(newComment)
                post.comments = updatedComments

                commentsAdapter.updateComments(post.comments)
                countBadge.text = post.comments.size.toString()
                commentInput.text.clear()

                postsViewModel.updatePost(post)
            } else {
                Toast.makeText(requireContext(), "Must be logged in to comment", Toast.LENGTH_SHORT).show()
            }
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