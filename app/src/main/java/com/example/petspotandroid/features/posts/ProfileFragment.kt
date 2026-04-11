package com.example.petspotandroid.features.posts

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivProfileImage = view.findViewById<ImageView>(R.id.ivProfileImage)
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvMemberSince = view.findViewById<TextView>(R.id.tvMemberSince)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val tvPhone = view.findViewById<TextView>(R.id.tvPhone)
        val tvReportsCount = view.findViewById<TextView>(R.id.tvReportsCount)
        val tvReunionsCount = view.findViewById<TextView>(R.id.tvReunionsCount)
        val tvListingsCount = view.findViewById<TextView>(R.id.tvListingsCount)
        val rvUserPosts = view.findViewById<RecyclerView>(R.id.rvUserPosts)
        val btnEditProfile = view.findViewById<MaterialButton>(R.id.btnEditProfile)

        adapter = PostsAdapter(emptyList()) { _ -> }
        rvUserPosts.layoutManager = LinearLayoutManager(requireContext())
        rvUserPosts.adapter = adapter

        authViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                tvUserName.text = "${it.firstName} ${it.lastName}"
                tvEmail.text = it.email
                tvPhone.text = it.phone

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it.createdAt
                val year = calendar.get(Calendar.YEAR)
                tvMemberSince.text = "Community Member Since $year"

                if (!it.avatarUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(it.avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .fit()
                        .centerCrop()
                        .into(ivProfileImage)
                } else {
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
            ToastHelper.showCustomToast(view, "Edit Profile coming soon!")
        }
    }
}