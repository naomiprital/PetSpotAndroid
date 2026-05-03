package com.example.petspotandroid.features.profile_menu

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.navigation.NavController
import com.example.petspotandroid.R
import com.example.petspotandroid.model.User
import com.squareup.picasso.Picasso

class ProfileMenuHelper(
    private val anchorView: View,
    private val currentUser: User?,
    private val navController: NavController,
    private val onLogout: () -> Unit
) {
    @SuppressLint("InflateParams", "SetTextI18n")
    fun show() {
        val context = anchorView.context
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.layout_profile_dropdown, null)

        val widthInDp = 220
        val pxWidth = (widthInDp * context.resources.displayMetrics.density).toInt()
        val popupWindow = PopupWindow(popupView, pxWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        currentUser?.let { user ->
            popupView.findViewById<TextView>(R.id.tvMenuName).text = "${user.firstName} ${user.lastName}"
            popupView.findViewById<TextView>(R.id.tvMenuEmail).text = user.email
            val ivAvatar = popupView.findViewById<ImageView>(R.id.ivMenuAvatar)
            if (!user.avatarUrl.isNullOrEmpty()) {
                Picasso.get().load(user.avatarUrl).placeholder(R.drawable.ic_person).into(ivAvatar)
            }
        }

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
            onLogout()
            navController.navigate(R.id.authFragment)
            popupWindow.dismiss()
        }

        popupWindow.elevation = 10f
        popupWindow.showAsDropDown(anchorView, -pxWidth + anchorView.width, 10)
    }
}