package com.example.mooltaxi

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mooltaxi.R
import com.google.firebase.auth.FirebaseAuth

class SplachScreen : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splach_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Log the current value of "Finished" flag
        Log.d("SplashScreen", "Onboarding finished status: ${onboardingDone()}")
        Handler().postDelayed({
            // Check if user is logged in
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // User is logged in
                if (onboardingDone()) {
                    findNavController().navigate(R.id.action_splachScreen_to_homeScreen) // Navigate to HomeScreen
                } else {
                    findNavController().navigate(R.id.action_splachScreen_to_languageSelect) // Navigate to ViewPagerFragment for onboarding
                }
            } else {
                // User is not logged in, navigate to Login Screen
                findNavController().navigate(R.id.action_splachScreen_to_languageSelect)
            }
        }, 3000) // Delay of 3000 ms (3 seconds)
    }
    private fun onboardingDone(): Boolean {
        val sharedPref = requireActivity().getSharedPreferences("onboarding", Context.MODE_PRIVATE)
        val isFinished = sharedPref.getBoolean("Finished", false)
        Log.d("Onboarding", "Onboarding Finished: $isFinished") // Log for debugging
        return isFinished
    }
}
