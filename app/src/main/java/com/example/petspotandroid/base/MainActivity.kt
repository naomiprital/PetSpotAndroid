package com.example.petspotandroid.base

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.petspotandroid.R
import com.example.petspotandroid.databinding.ActivityMainBinding
import com.example.petspotandroid.features.authentication.auth.AuthViewModel
import com.example.petspotandroid.features.profile_menu.ProfileMenuHelper
import com.example.petspotandroid.model.User
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel.checkCurrentUser()
        setupNavigation()
        observeUserData()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuthScreen = destination.id == R.id.authFragment || destination.id == R.id.forgotPasswordFragment
            binding.mainToolbar.visibility = if (isAuthScreen) View.GONE else View.VISIBLE
            if (!isAuthScreen) authViewModel.refreshUserData()
        }

        binding.btnProfileMenu.setOnClickListener { view ->
            ProfileMenuHelper(view, currentUser, navController) {
                authViewModel.logout()
            }.show()
        }
    }

    private fun observeUserData() {
        authViewModel.userData.observe(this) { user ->
            this.currentUser = user

            val avatarUrl = user?.avatarUrl

            if (!avatarUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_person)
            }
        }
    }
}