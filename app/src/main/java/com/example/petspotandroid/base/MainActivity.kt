package com.example.petspotandroid.base

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.petspotandroid.R
import com.example.petspotandroid.dao.AppLocalDb
import com.example.petspotandroid.data.repository.AuthRepository
import com.example.petspotandroid.viewmodel.AuthViewModel
import com.example.petspotandroid.viewmodel.AuthViewModelFactory
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private val authViewModel: AuthViewModel by viewModels {
        val userDao = AppLocalDb.getDatabase(this).userDao()
        val repository = AuthRepository(userDao, applicationContext)
        AuthViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val ivAvatar = findViewById<ImageView>(R.id.ivAvatar)

        authViewModel.user.observe(this) { firebaseUser ->
            if (firebaseUser == null) {
                if (navController.currentDestination?.id != R.id.authFragment) {
                    navController.navigate(R.id.action_global_authFragment)
                }
            }
        }

        authViewModel.userData.observe(this) { user ->
            if (user?.avatarUrl != null && user.avatarUrl.isNotEmpty()) {
                Picasso.get()
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivAvatar)
            } else {
                ivAvatar.setImageResource(R.drawable.ic_launcher_background)
            }
        }

        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.authFragment,
                R.id.forgotPasswordFragment -> {
                    toolbar.visibility = View.GONE
                }
                else -> {
                    toolbar.visibility = View.VISIBLE
                }
            }
        }

        setupProfileMenu()
    }

    private fun setupProfileMenu() {
        val btnProfileMenu = findViewById<MaterialCardView>(R.id.btnProfileMenu)

        btnProfileMenu.setOnClickListener { anchorView ->
            val popupView = layoutInflater.inflate(R.layout.layout_profile_dropdown, null)
            val widthInPixels = (200 * resources.displayMetrics.density).toInt()

            val popupWindow = PopupWindow(
                popupView,
                widthInPixels,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )

            popupWindow.elevation = 10f

            val btnLogout = popupView.findViewById<TextView>(R.id.btnMenuLogout)
            val btnHome = popupView.findViewById<TextView>(R.id.btnMenuHome)
            val btnProfile = popupView.findViewById<TextView>(R.id.btnMenuProfile)
            val tvMenuName = popupView.findViewById<TextView>(R.id.tvMenuName)
            val tvMenuEmail = popupView.findViewById<TextView>(R.id.tvMenuEmail)
            val ivMenuAvatar = popupView.findViewById<ImageView>(R.id.ivMenuAvatar)

            val currentUser = authViewModel.user.value
            val userData = authViewModel.userData.value

            if (currentUser != null) {
                tvMenuEmail.text = currentUser.email
                tvMenuName.text = if (userData != null) "${userData.firstName} ${userData.lastName}" else "PetSpot User"
                
                if (userData?.avatarUrl != null && userData.avatarUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(userData.avatarUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(ivMenuAvatar)
                }
            }

            btnLogout.setOnClickListener {
                popupWindow.dismiss()
                authViewModel.logout()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                if (navController.currentDestination?.id != R.id.authFragment) {
                    navController.navigate(R.id.action_global_authFragment)
                }
            }

            btnHome.setOnClickListener {
                popupWindow.dismiss()
                Toast.makeText(this, "Going Home...", Toast.LENGTH_SHORT).show()
            }

            btnProfile.setOnClickListener {
                popupWindow.dismiss()
                Toast.makeText(this, "Opening Profile...", Toast.LENGTH_SHORT).show()
            }

            popupWindow.showAsDropDown(anchorView, 0, 16, Gravity.END)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}