package com.example.petspotandroid.base

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.snackbar.Snackbar

object ToastHelper {
    fun showCustomToast(view: View, message: String, duration: Int = Snackbar.LENGTH_LONG) {
        val snackbar = Snackbar.make(view, message, duration)
        
        val snackbarView = snackbar.view
        
        snackbarView.background = AppCompatResources.getDrawable(view.context, android.R.drawable.toast_frame)
        
        val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.gravity = Gravity.CENTER
        textView.maxLines = 20
        
        val params = snackbarView.layoutParams
        
        when (params) {
            is FrameLayout.LayoutParams -> {
                params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                params.bottomMargin = 64 // Stick more to the bottom (was 120)
                params.width = FrameLayout.LayoutParams.WRAP_CONTENT
                snackbarView.layoutParams = params
            }
            is androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams -> {
                params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                params.bottomMargin = 64
                params.width = FrameLayout.LayoutParams.WRAP_CONTENT
                snackbarView.layoutParams = params
            }
            else -> {
                snackbarView.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
            }
        }

        snackbar.show()
    }
}