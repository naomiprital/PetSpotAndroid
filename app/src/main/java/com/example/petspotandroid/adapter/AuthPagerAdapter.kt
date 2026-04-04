package com.example.petspotandroid.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.petspotandroid.features.authentication.LoginFragment
import com.example.petspotandroid.features.authentication.SignUpFragment

class AuthPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

     enum class AuthTab {
        LOGIN,
        SIGN_UP
    }
    // There are 2 tabs: Login and Sign Up
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            AuthTab.LOGIN.ordinal -> LoginFragment()
            AuthTab.SIGN_UP.ordinal -> SignUpFragment()
            else -> LoginFragment()
        }
    }
}