package com.example.mooltaxi

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.example.mooltaxi.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize the BottomNavigationView
        bottomNavigationView = binding.bottomNavigationView

        // Set up navigation item selection listener
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.map -> {
                    // Show the Map fragment
                    showFragment(Map())
                    true
                }
                R.id.compteur ->{
                    showFragment(Compteur())
                    true
                }
                R.id.profile -> {
                    // Show the Profile fragment
                    showFragment(Profile())
                    true
                }
                else -> false
            }
        }

        // Set default fragment to Map if no savedInstanceState exists
        if (savedInstanceState == null) {
            showFragment(Map())  // You can replace Map() with MapFragment() if you renamed it
        }

        return binding.root
    }

    // Function to replace the current fragment inside the FrameLayout
    private fun showFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}



