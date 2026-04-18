package com.example.petspotandroid.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.petspotandroid.R
import com.example.petspotandroid.dao.AppLocalDb
import com.example.petspotandroid.data.models.User
import com.example.petspotandroid.data.repository.AuthRepository
import com.example.petspotandroid.viewmodel.AuthViewModel
import com.example.petspotandroid.viewmodel.AuthViewModelFactory
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private val authViewModel: AuthViewModel by viewModels {
        val userDao = AppLocalDb.getDatabase(this).userDao()
        val repository = AuthRepository(userDao)
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
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivAvatar)
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person)
            }
        }

        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.authFragment -> {
                    toolbar.visibility = View.GONE
                }
                else -> {
                    toolbar.visibility = View.VISIBLE
                }
            }
        }

        setupProfileMenu()
    }

    @SuppressLint("InflateParams")
    private fun setupProfileMenu() {
        val btnProfileMenu = findViewById<MaterialCardView>(R.id.btnProfileMenu)

        btnProfileMenu.setOnClickListener { anchorView ->
            val popupView = layoutInflater.inflate(R.layout.layout_profile_dropdown, null)
            val widthInPixels = (220 * resources.displayMetrics.density).toInt()

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

            val updateUI = { user: com.google.firebase.auth.FirebaseUser?, data: User? ->
                if (user != null) {
                    tvMenuEmail.text = user.email
                }
                
                if (data != null) {
                    val fullName = "${data.firstName} ${data.lastName}".trim()
                    if (fullName.isNotEmpty()) {
                        tvMenuName.text = fullName
                    } else {
                        tvMenuName.text = if (!user?.displayName.isNullOrBlank()) user.displayName else "PetSpot User"
                    }
                    
                    if (!data.avatarUrl.isNullOrEmpty()) {
                        Picasso.get()
                            .load(data.avatarUrl)
                            .fit()
                            .centerCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(ivMenuAvatar)
                    }
                } else {
                    tvMenuName.text = if (!user?.displayName.isNullOrBlank()) user.displayName else "PetSpot User"
                }
            }

            updateUI(authViewModel.user.value, authViewModel.userData.value)
            
            authViewModel.refreshUserData()

            val userObserver = Observer<com.google.firebase.auth.FirebaseUser?> { user ->
                updateUI(user, authViewModel.userData.value)
            }
            val userDataObserver = Observer<User?> { data ->
                updateUI(authViewModel.user.value, data)
            }

            authViewModel.user.observeForever(userObserver)
            authViewModel.userData.observeForever(userDataObserver)

            popupWindow.setOnDismissListener {
                authViewModel.user.removeObserver(userObserver)
                authViewModel.userData.removeObserver(userDataObserver)
            }

            btnLogout.setOnClickListener {
                popupWindow.dismiss()
                authViewModel.logout()
                if (navController.currentDestination?.id != R.id.authFragment) {
                    navController.navigate(R.id.action_global_authFragment)
                }
            }

            btnHome.setOnClickListener {
                popupWindow.dismiss()
                if (navController.currentDestination?.id != R.id.postsListFragment) {
                    navController.navigate(R.id.action_global_postsListFragment)
                }
            }

            btnProfile.setOnClickListener {
                popupWindow.dismiss()
                if (navController.currentDestination?.id != R.id.profileFragment) {
                    navController.navigate(R.id.action_postsListFragment_to_profileFragment)
                }
            }

            popupWindow.showAsDropDown(anchorView, 0, 16, Gravity.END)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}