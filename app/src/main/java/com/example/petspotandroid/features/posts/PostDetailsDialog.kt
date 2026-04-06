package com.example.petspotandroid.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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

        view.findViewById<ImageButton>(R.id.closeButton).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.posterName).text = post.userName
        view.findViewById<TextView>(R.id.locationText).text = post.lastSeenLocation
        view.findViewById<TextView>(R.id.seenOnText).text = post.eventDate
        view.findViewById<TextView>(R.id.descriptionText).text = post.description

        val postedFormat = SimpleDateFormat("'Posted' dd/MM/yyyy", Locale.getDefault())
        view.findViewById<TextView>(R.id.postedDate).text = postedFormat.format(Date(post.createdAt))

        val statusBadge = view.findViewById<TextView>(R.id.statusBadge)
        val badgeTextId = if (post.isLost) R.string.lost else R.string.found
        val badgeColorId = if (post.isLost) R.color.status_lost else R.color.status_found

        statusBadge.text = getString(badgeTextId) + " " + post.petType
        statusBadge.setTextColor(ContextCompat.getColor(requireContext(), badgeColorId))
        statusBadge.background.mutate().setTint(ContextCompat.getColor(requireContext(), badgeColorId))
        statusBadge.background.alpha = 50

        val postImage = view.findViewById<ImageView>(R.id.postImage)
        if (post.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(post.imageUrl)
                .fit()
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_camera)
                .into(postImage)
        }

        val callButton = view.findViewById<MaterialButton>(R.id.callButton)
            callButton.text = getString(R.string.call_lister, post.contactNumber)
            callButton.setOnClickListener {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = "tel:${post.contactNumber}".toUri()
                }
                startActivity(dialIntent)
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