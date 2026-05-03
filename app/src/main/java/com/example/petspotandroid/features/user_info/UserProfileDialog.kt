package com.example.petspotandroid.features.user_info

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.petspotandroid.R
import com.squareup.picasso.Picasso

class UserProfileDialog : DialogFragment() {

    private val viewModel: UserProfileViewModel by viewModels()
    private var targetUserId: String? = null

    fun setUserId(userId: String) {
        this.targetUserId = userId
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

        targetUserId?.let { userId ->
            viewModel.getUserData(userId).observe(viewLifecycleOwner) { user ->
                user?.let {
                    tvUserName.text = "${it.firstName} ${it.lastName}"
                    tvUserHandle.text = it.email
                    tvContactEmail.text = it.email
                    tvContactPhone.text = it.phone

                    Picasso.get()
                        .load(it.avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .fit()
                        .centerCrop()
                        .into(ivProfileImage)
                }
            }

            viewModel.getUserStats(userId).observe(viewLifecycleOwner) { stats ->
                tvReportsCount.text = stats.first.toString()
                tvReunionsCount.text = stats.second.toString()
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