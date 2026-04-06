package com.example.petspotandroid.features.authentication

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.petspotandroid.R
import com.google.android.material.card.MaterialCardView

class AuthFragment : Fragment(R.layout.fragment_auth) {
    private lateinit var tabLoginBg: MaterialCardView
    private lateinit var tabSignUpBg: MaterialCardView
    private lateinit var tvLoginText: TextView
    private lateinit var tvSignUpText: TextView
    private lateinit var tvTerms: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLoginBg = view.findViewById(R.id.tabLoginBg)
        tabSignUpBg = view.findViewById(R.id.tabSignUpBg)
        tvLoginText = view.findViewById(R.id.tvLoginText)
        tvSignUpText = view.findViewById(R.id.tvSignUpText)
        tvTerms = view.findViewById(R.id.tvTerms)

        tabLoginBg.setOnClickListener {
            showLogin()
        }

        tabSignUpBg.setOnClickListener {
            showSignUp()
        }

        if (savedInstanceState == null) {
            showLogin()
        }
    }

    private fun showLogin() {
        childFragmentManager.beginTransaction()
            .replace(R.id.formContainer, LoginFragment())
            .commit()

        tabLoginBg.setCardBackgroundColor(Color.WHITE)
        tvLoginText.setTextColor(ContextCompat.getColor(requireContext(), R.color.petspot_orange))

        tabSignUpBg.setCardBackgroundColor(Color.TRANSPARENT)
        tvSignUpText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
        tvTerms.visibility = View.VISIBLE
    }

    private fun showSignUp() {
        childFragmentManager.beginTransaction()
            .replace(R.id.formContainer, SignUpFragment())
            .commit()

        tabSignUpBg.setCardBackgroundColor(Color.WHITE)
        tvSignUpText.setTextColor(ContextCompat.getColor(requireContext(), R.color.petspot_orange))

        tabLoginBg.setCardBackgroundColor(Color.TRANSPARENT)
        tvLoginText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_text))
        tvTerms.visibility = View.GONE
    }
}