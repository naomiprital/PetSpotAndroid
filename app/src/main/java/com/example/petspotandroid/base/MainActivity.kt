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
import com.example.petspotandroid.databinding.ActivityMainBinding
import com.example.petspotandroid.features.authentication.auth.AuthViewModel
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
        authViewModel.refreshUserData()

        setupNavigation()
        observeUserData()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.authFragment, R.id.forgotPasswordFragment -> {
                    binding.mainToolbar.visibility = View.GONE
                }
                else -> {
                    binding.mainToolbar.visibility = View.VISIBLE
                    authViewModel.refreshUserData()
                }
            }
        }

        binding.btnProfileMenu.setOnClickListener { view ->
            showProfileDropdown(view)
        }
    }

    private fun showProfileDropdown(anchorView: View) {
        val popupView = layoutInflater.inflate(R.layout.layout_profile_dropdown, null)
        val widthInDp = 220
        val pxWidth = (widthInDp * resources.displayMetrics.density).toInt()

        val popupWindow = PopupWindow(popupView, pxWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        val ivMenuAvatar = popupView.findViewById<ImageView>(R.id.ivMenuAvatar)
        val tvMenuName = popupView.findViewById<TextView>(R.id.tvMenuName)
        val tvMenuEmail = popupView.findViewById<TextView>(R.id.tvMenuEmail)

        currentUser?.let { user ->
            tvMenuName.text = "${user.firstName} ${user.lastName}"
            tvMenuEmail.text = user.email
            if (!user.avatarUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .into(ivMenuAvatar)
            }
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        popupView.findViewById<TextView>(R.id.btnMenuHome).setOnClickListener {
            navController.navigate(R.id.postsListFragment)
            popupWindow.dismiss()
        }

        popupView.findViewById<TextView>(R.id.btnMenuProfile).setOnClickListener {
            if (navController.currentDestination?.id != R.id.profileFragment) {
                navController.navigate(R.id.profileFragment)
            }
            popupWindow.dismiss()
        }

        popupView.findViewById<TextView>(R.id.btnMenuLogout).setOnClickListener {
            authViewModel.logout()
            navController.navigate(R.id.authFragment)
            popupWindow.dismiss()
        }

        popupWindow.elevation = 10f
        popupWindow.showAsDropDown(anchorView, -pxWidth + anchorView.width, 10)
    }

    private fun observeUserData() {
        authViewModel.userData.observe(this) { user ->
            this.currentUser = user

            user?.let {
                if (!it.avatarUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(it.avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .into(binding.ivAvatar)
                }
            }
        }
    }
}