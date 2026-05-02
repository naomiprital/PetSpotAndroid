package com.example.petspotandroid.base

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.petspotandroid.R
import com.example.petspotandroid.dao.AppLocalDB
import com.example.petspotandroid.data.repository.auth.AuthRepository
import com.example.petspotandroid.databinding.ActivityMainBinding
import com.example.petspotandroid.features.authentication.auth.AuthViewModel
import com.example.petspotandroid.features.authentication.auth.AuthViewModelFactory
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(AppLocalDB.db.userDao))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeUserData()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Hide/Show toolbar based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.authFragment, R.id.forgotPasswordFragment -> {
                    binding.mainToolbar.visibility = View.GONE
                }
                else -> {
                    binding.mainToolbar.visibility = View.VISIBLE
                }
            }
        }

        // Click on the toolbar profile card opens the custom dropdown
        binding.btnProfileMenu.setOnClickListener { view ->
            showProfileDropdown(view)
        }
    }

    private fun showProfileDropdown(anchorView: View) {
        // 1. Inflate the custom dropdown layout
        val inflater = layoutInflater
        val popupView = inflater.inflate(R.layout.layout_profile_dropdown, null)

        // 1. Convert 220dp to actual Pixels based on the device screen density
        val widthInDp = 220
        val pxWidth = (widthInDp * resources.displayMetrics.density).toInt()

        // 2. Pass the calculated pixel width to the constructor
        val popupWindow = PopupWindow(
            popupView,
            pxWidth, // Fixed width in pixels (220dp)
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // 3. Find views INSIDE the popupView (Fixes the Unresolved Reference)
        val ivMenuAvatar = popupView.findViewById<ImageView>(R.id.ivMenuAvatar)
        val tvMenuName = popupView.findViewById<TextView>(R.id.tvMenuName)
        val tvMenuEmail = popupView.findViewById<TextView>(R.id.tvMenuEmail)
        val btnHome = popupView.findViewById<TextView>(R.id.btnMenuHome)
        val btnProfile = popupView.findViewById<TextView>(R.id.btnMenuProfile)
        val btnLogout = popupView.findViewById<TextView>(R.id.btnMenuLogout)

        // 4. Fill with current user data
        authViewModel.userData.value?.let { user ->
            tvMenuName.text = "${user.firstName} ${user.lastName}"
            tvMenuEmail.text = user.email
            if (!user.avatarUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .into(ivMenuAvatar)
            }
        }

        // 5. Setup Navigation inside the menu
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        btnHome.setOnClickListener {
            navController.navigate(R.id.postsListFragment)
            popupWindow.dismiss()
        }

        btnProfile.setOnClickListener {
            if (navController.currentDestination?.id != R.id.profileFragment) {
                navController.navigate(R.id.profileFragment)
            }
            popupWindow.dismiss()
        }

        btnLogout.setOnClickListener {
            // Optional: Call authViewModel.logout() here if you have it
            navController.navigate(R.id.authFragment)
            popupWindow.dismiss()
        }

        // 6. Show the popup
        popupWindow.elevation = 10f
        // -180 shifts it to the left so it aligns with the edge of the screen
        popupWindow.showAsDropDown(anchorView, -pxWidth + anchorView.width, 10)
    }

    private fun observeUserData() {
        authViewModel.userData.observe(this) { user ->
            user?.avatarUrl?.let { url ->
                if (url.isNotEmpty()) {
                    Picasso.get()
                        .load(url)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.ivAvatar)
                }
            }
        }
    }
}