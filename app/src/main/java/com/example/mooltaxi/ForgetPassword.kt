package com.example.mooltaxi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth


class ForgetPassword : Fragment() {

    private lateinit var emailEditText: EditText
    private lateinit var resetPasswordButton: Button
    private lateinit var backbtn : ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_forgetpassword, container, false)

        emailEditText = view.findViewById(R.id.email) // Updated ID
        resetPasswordButton = view.findViewById(R.id.restpass) // Updated ID
        backbtn=view.findViewById(R.id.back)

        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Send password reset email
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Handle errors
                        Toast.makeText(context, "Failed to send password reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        backbtn.setOnClickListener {
            findNavController().navigate(R.id.action_forgetpassword_to_loginScreen)
        }
        return view
    }
}
