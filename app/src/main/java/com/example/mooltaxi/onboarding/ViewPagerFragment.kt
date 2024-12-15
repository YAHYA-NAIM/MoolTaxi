package com.example.mooltaxi.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.mooltaxi.R
import com.example.mooltaxi.onboarding.screens.FirstOnboarding
import com.example.mooltaxi.onboarding.screens.SecondOnboarding
import com.example.mooltaxi.onboarding.screens.ThirdOnboarding

class ViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)

        val fragmentlist= arrayListOf<Fragment>(
            FirstOnboarding(),
            SecondOnboarding(),
            ThirdOnboarding()
        )

        val adapter = ViewPagerAdapter(
            fragmentlist
            ,requireActivity().supportFragmentManager
            ,lifecycle)

        view.findViewById<ViewPager2>(R.id.viewPager).adapter = adapter

        return view
    }

}