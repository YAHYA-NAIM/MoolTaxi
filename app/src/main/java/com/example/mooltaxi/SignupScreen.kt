package com.example.mooltaxi

import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupScreen : Fragment() {

    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var licenseType: EditText
    private lateinit var age: EditText
    private lateinit var signUpButton: Button
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var signInText: TextView
    private lateinit var error: TextView
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        name = view.findViewById(R.id.name)
        email = view.findViewById(R.id.editTextTextEmailAddress)
        password = view.findViewById(R.id.editTextTextPassword)
        licenseType = view.findViewById(R.id.licencetype)
        age = view.findViewById(R.id.age)
        signUpButton = view.findViewById(R.id.button)
        passwordVisibilityToggle = view.findViewById(R.id.hide)
        signInText = view.findViewById(R.id.signintext)
        error = view.findViewById(R.id.error2)

        // Toggle password visibility
        passwordVisibilityToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        // Handle sign-up button click
        signUpButton.setOnClickListener {
            signUpUser()
        }

        // Handle sign-in text click
        signInText.setOnClickListener {
            findNavController().navigate(R.id.action_signupScreen_to_loginScreen)
        }
    }

    // Function to toggle password visibility
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordVisibilityToggle.setImageResource(R.drawable.hidepass)
        } else {
            password.inputType = InputType.TYPE_CLASS_TEXT
            passwordVisibilityToggle.setImageResource(R.drawable.showpass)
        }
        isPasswordVisible = !isPasswordVisible
        password.setSelection(password.text.length)
    }

    // Function to handle user sign-up
    private fun signUpUser() {
        val nameInput = name.text.toString().trim()
        val emailInput = email.text.toString().trim()
        val passwordInput = password.text.toString().trim()
        val licenseTypeInput = licenseType.text.toString().trim()
        val ageInput = age.text.toString().trim()

        // Validation checks
        if (TextUtils.isEmpty(nameInput)) {
            name.error = "Name is required"
            return
        }

        if (TextUtils.isEmpty(emailInput)) {
            email.error = "Email is required"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.error = "Please enter a valid email address"
            return
        }

        if (TextUtils.isEmpty(passwordInput)) {
            password.error = "Password is required"
            return
        }

        if (passwordInput.length < 8) {
            password.error = "Password must be at least 8 characters"
            return
        }

        if (TextUtils.isEmpty(licenseTypeInput)) {
            licenseType.error = "License type is required"
            return
        }

        if (TextUtils.isEmpty(ageInput)) {
            age.error = "Age is required"
            return
        }

        // Firebase sign-up logic
        auth.createUserWithEmailAndPassword(emailInput, passwordInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Save user info to SQLite instead of Firestore
                        saveUserInfoToSQLite(nameInput, emailInput, licenseTypeInput, ageInput)
                    }
                } else {
                    error.text = "Sign-up failed: ${task.exception?.message}"
                    Toast.makeText(requireContext(), "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to save user information to SQLite
    private fun saveUserInfoToSQLite(name: String, email: String, licenseType: String, age: String) {
        val dbHelper = DatabaseHelper(requireContext())

        // Get the current timestamp
        val createdAt = System.currentTimeMillis().toString()

        // Insert user info into SQLite database
        dbHelper.insertUser(name, email, licenseType, age, createdAt)

        // Show success message
        Toast.makeText(requireContext(), "User information saved successfully!", Toast.LENGTH_SHORT).show()

        // Navigate to Home screen
        findNavController().navigate(R.id.action_signupScreen_to_homeScreen)
    }
}
