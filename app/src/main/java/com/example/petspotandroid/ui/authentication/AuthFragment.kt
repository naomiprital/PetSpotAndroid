package com.example.petspotandroid.ui.authentication

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.petspotandroid.R
import com.google.android.material.card.MaterialCardView
import androidx.core.view.isVisible

class AuthFragment : Fragment(R.layout.fragment_auth) {
    private lateinit var layoutLogin: View
    private lateinit var layoutSignUp: View
    private lateinit var tabLoginBg: MaterialCardView
    private lateinit var tabSignUpBg: MaterialCardView
    private lateinit var tvLoginText: TextView
    private lateinit var tvSignUpText: TextView
    private lateinit var tvTerms: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutLogin = view.findViewById(R.id.layoutLogin)
        layoutSignUp = view.findViewById(R.id.layoutSignUp)
        tabLoginBg = view.findViewById(R.id.tabLoginBg)
        tabSignUpBg = view.findViewById(R.id.tabSignUpBg)
        tvLoginText = view.findViewById(R.id.tvLoginText)
        tvSignUpText = view.findViewById(R.id.tvSignUpText)
        tvTerms = view.findViewById(R.id.tvTerms)

        // Handle Toggles
        tabLoginBg.setOnClickListener {
            showLogin()
        }

        tabSignUpBg.setOnClickListener {
            showSignUp()
        }

        // Ensure correct state on rotation or returning from backstack
        if (layoutSignUp.isVisible) {
            showSignUp()
        } else {
            showLogin()
        }
    }

    private fun showLogin() {
        layoutLogin.visibility = View.VISIBLE
        layoutSignUp.visibility = View.GONE

        tabLoginBg.setCardBackgroundColor(Color.WHITE)
        tvLoginText.setTextColor(ContextCompat.getColor(requireContext(), R.color.petspot_orange))

        tabSignUpBg.setCardBackgroundColor(Color.TRANSPARENT)
        tvSignUpText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
        tvTerms.visibility = View.VISIBLE
    }

    private fun showSignUp() {
        layoutLogin.visibility = View.GONE
        layoutSignUp.visibility = View.VISIBLE

        tabSignUpBg.setCardBackgroundColor(Color.WHITE)
        tvSignUpText.setTextColor(ContextCompat.getColor(requireContext(), R.color.petspot_orange))

        tabLoginBg.setCardBackgroundColor(Color.TRANSPARENT)
        tvLoginText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
        tvTerms.visibility = View.GONE
    }
}