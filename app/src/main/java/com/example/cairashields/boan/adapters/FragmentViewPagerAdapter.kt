package com.example.cairashields.boan.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.cairashields.boan.ui.CreditCardFragment
import com.example.cairashields.boan.ui.SignUpFragment

class FragmentViewPagerAdapter : FragmentPagerAdapter {
    private val NUM_ITEMS = 2

    constructor(fragmentManager: FragmentManager) : super(fragmentManager) {

    }

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        when(position){
//            0 -> fragment = SignUpFragment().newInstance()
//            1 -> fragment = CreditCardFragment().newInstance()
        }
        return fragment!!
    }

    override fun getCount(): Int {
        return NUM_ITEMS
    }
}