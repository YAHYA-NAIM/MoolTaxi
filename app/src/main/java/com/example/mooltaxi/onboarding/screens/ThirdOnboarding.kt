package com.example.mooltaxi.onboarding.screens

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.mooltaxi.R


class ThirdOnboarding : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_third_onboarding, container, false)



        view.findViewById<Button>(R.id.getstarted).setOnClickListener {
            findNavController().navigate(R.id.action_viewPagerFragment_to_signupScreen)
            onboardingdone()
        }
        return view
    }

    private fun onboardingdone() {
        val sharedPref = requireActivity().getSharedPreferences("onboarding", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished", true)
        editor.apply()  // Use apply for asynchronous writing
        Log.d("Onboarding", "Onboarding Finished: true") // Log for debugging
    }
}