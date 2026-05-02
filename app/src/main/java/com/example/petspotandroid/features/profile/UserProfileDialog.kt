package com.example.petspotandroid.features.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.petspotandroid.R
import com.example.petspotandroid.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class UserProfileDialog : DialogFragment() {
    private var targetUser: User? = null
    fun setUser(user: User) {
        this.targetUser = user
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_user_profile, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose = view.findViewById<ImageButton>(R.id.closeButton)
        val ivProfileImage = view.findViewById<ImageView>(R.id.ivUserProfileImage)
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvUserHandle = view.findViewById<TextView>(R.id.tvUserHandle)
        val tvContactEmail = view.findViewById<TextView>(R.id.tvContactEmail)
        val tvContactPhone = view.findViewById<TextView>(R.id.tvContactPhone)
        val tvReportsCount = view.findViewById<TextView>(R.id.tvReportsCount)
        val tvReunionsCount = view.findViewById<TextView>(R.id.tvReunionsCount)

        btnClose.setOnClickListener { dismiss() }

        targetUser?.let { user ->
            tvUserName.text = "${user.firstName} ${user.lastName}"
            tvUserHandle.text = user.email
            tvContactEmail.text = user.email
            tvContactPhone.text = user.phone

            if (!user.avatarUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .fit()
                    .centerCrop()
                    .into(ivProfileImage)
            } else {
                ivProfileImage.setImageResource(R.drawable.ic_person)
            }

            val db = FirebaseFirestore.getInstance()
            val userId = user.id

            if (userId.isNotEmpty()) {
                db.collection("posts")
                    .whereEqualTo("authorId", userId)
                    .get()
                    .addOnSuccessListener { documents ->
                        val totalReports = documents.size()

                        val reunions = documents.count { doc ->
                            doc.getBoolean("isResolved") == true
                        }

                        tvReportsCount.text = totalReports.toString()
                        tvReunionsCount.text = reunions.toString()
                    }
                    .addOnFailureListener {
                        tvReportsCount.text = "-"
                        tvReunionsCount.text = "-"
                    }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}