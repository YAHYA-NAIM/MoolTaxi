package com.example.mooltaxi

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mooltaxi.R
import com.google.firebase.auth.FirebaseAuth

class LoginScreen : Fragment() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpText: TextView
    private lateinit var passwordVisibilityToggle: ImageView
    private lateinit var errorTextView: TextView
    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loginpage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        email = view.findViewById(R.id.editTextTextEmailAddress)
        password = view.findViewById(R.id.editTextTextPassword)
        loginButton = view.findViewById(R.id.button)
        passwordVisibilityToggle = view.findViewById(R.id.imageView7)
        signUpText = view.findViewById(R.id.textView9)
        errorTextView = view.findViewById(R.id.error)

        // Toggle password visibility
        passwordVisibilityToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        // Handle login button click
        loginButton.setOnClickListener {
            loginUser()
        }

        // Navigate to sign-up page
        signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_loginScreen_to_signupScreen)
        }

        // Navigate to forgot password page
        view.findViewById<TextView>(R.id.forgetpass).setOnClickListener {
            findNavController().navigate(R.id.action_loginScreen_to_forgetpassword)
        }
    }

    // Function to toggle password visibility
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            password.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordVisibilityToggle.setImageResource(R.drawable.hidepass) // Change to hidepass icon
        } else {
            password.inputType = android.text.InputType.TYPE_CLASS_TEXT
            passwordVisibilityToggle.setImageResource(R.drawable.showpass) // Change to showpass icon
        }
        isPasswordVisible = !isPasswordVisible
        password.setSelection(password.text.length) // Move cursor to the end
    }

    // Function to handle user login
    private fun loginUser() {
        val emailInput = email.text.toString().trim()
        val passwordInput = password.text.toString().trim()

        // Clear previous error messages
        errorTextView.text = ""

        // Validate email and password fields
        if (TextUtils.isEmpty(emailInput)) {
            errorTextView.text = "Please enter your email."
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            errorTextView.text = "Please enter a valid email address."
            return
        }

        if (TextUtils.isEmpty(passwordInput)) {
            errorTextView.text = "Please enter your password."
            return
        }

        // Firebase login logic
        auth.signInWithEmailAndPassword(emailInput, passwordInput)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Clear the error text if login is successful
                    errorTextView.text = ""
                    // Navigate to Home screen
                    findNavController().navigate(R.id.action_loginScreen_to_homeScreen)
                } else {
                    // Display a general error message for incorrect email or password
                    errorTextView.text = "Email or password are incorrect."
                }
            }
    }
}
