package com.example.petspotandroid.ui.authentication

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.petspotandroid.R
import com.example.petspotandroid.adapter.AuthPagerAdapter
import com.google.android.material.card.MaterialCardView

class AuthFragment : Fragment(R.layout.fragment_auth) {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLoginBg: MaterialCardView
    private lateinit var tabSignUpBg: MaterialCardView
    private lateinit var tvLoginText: TextView
    private lateinit var tvSignUpText: TextView
    private lateinit var tvTerms: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        tabLoginBg = view.findViewById(R.id.tabLoginBg)
        tabSignUpBg = view.findViewById(R.id.tabSignUpBg)
        tvLoginText = view.findViewById(R.id.tvLoginText)
        tvSignUpText = view.findViewById(R.id.tvSignUpText)
        tvTerms = view.findViewById(R.id.tvTerms)

        val adapter = AuthPagerAdapter(this)
        viewPager.adapter = adapter

        // Tells ViewPager to preload the hidden tab in the background
        viewPager.offscreenPageLimit = 1

        tabLoginBg.setOnClickListener {
            viewPager.currentItem = AuthPagerAdapter.AuthTab.LOGIN.ordinal
        }

        tabSignUpBg.setOnClickListener {
            viewPager.currentItem = AuthPagerAdapter.AuthTab.SIGN_UP.ordinal
        }

        // Handle Swipes
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabUI(position)
                viewPager.postDelayed({
                    val recyclerView =
                        viewPager.getChildAt(0) as androidx.recyclerview.widget.RecyclerView
                    val currentView = recyclerView.layoutManager?.findViewByPosition(position)

                    currentView?.let { view ->
                        view.measure(
                            View.MeasureSpec.makeMeasureSpec(
                                viewPager.width,
                                View.MeasureSpec.EXACTLY
                            ),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        )

                        val newHeight = view.measuredHeight
                        if (viewPager.layoutParams.height != newHeight) {
                            viewPager.layoutParams.height = newHeight
                            viewPager.requestLayout()
                        }
                    }
                }, 50)
            }
        })
    }

    private fun updateTabUI(position: Int) {
        val activeBgColor = Color.WHITE
        val inactiveBgColor = Color.TRANSPARENT
        val activeTextColor = ContextCompat.getColor(requireContext(), R.color.petspot_orange)
        val inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.gray_text)

        if (position == AuthPagerAdapter.AuthTab.LOGIN.ordinal) {
            tabLoginBg.setCardBackgroundColor(activeBgColor)
            tvLoginText.setTextColor(activeTextColor)

            tabSignUpBg.setCardBackgroundColor(inactiveBgColor)
            tvSignUpText.setTextColor(inactiveTextColor)
            tvTerms.visibility = View.VISIBLE
        } else {
            tabSignUpBg.setCardBackgroundColor(activeBgColor)
            tvSignUpText.setTextColor(activeTextColor)

            tabLoginBg.setCardBackgroundColor(inactiveBgColor)
            tvLoginText.setTextColor(inactiveTextColor)
            tvTerms.visibility = View.GONE
        }
    }
}