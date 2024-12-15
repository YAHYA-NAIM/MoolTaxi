package com.example.mooltaxi

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.util.*

class LanguageSelect : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_language_select, container, false)

        // Spinner for language selection
        val spinnerLanguage = view.findViewById<Spinner>(R.id.spinnerLanguage)

        // Set up the spinner with language options
        val languages = listOf("English", "Arabic", "French")
        val languageCodes = listOf("en", "ar", "fr") // Corrected codes
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        // Get current locale to set spinner position
        val currentLanguage = Locale.getDefault().language
        val defaultPosition = languageCodes.indexOf(currentLanguage)
        if (defaultPosition != -1) spinnerLanguage.setSelection(defaultPosition)

        // Set spinner listener
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLanguageCode = languageCodes[position]
                if (Locale.getDefault().language != selectedLanguageCode) {
                    setLocale(requireContext(), selectedLanguageCode)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle system back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish() // Close the app when back is pressed from this fragment
        }

        // Next button to navigate to the onboarding page
        view.findViewById<Button>(R.id.Nextbutton).setOnClickListener {
            findNavController().navigate(R.id.action_languageSelectFragment_to_viewPagerFragment)
        }
    }

    private fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
