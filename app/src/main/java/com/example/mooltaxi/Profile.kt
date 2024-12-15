package com.example.mooltaxi

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

class Profile : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var ageTextView: TextView
    private lateinit var licenseTypeTextView: TextView
    private lateinit var qrcodeImageView: ImageView
    private lateinit var logoutButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize views
        nameTextView = view.findViewById(R.id.fullname)
        emailTextView = view.findViewById(R.id.emailplace)
        ageTextView = view.findViewById(R.id.age)
        licenseTypeTextView = view.findViewById(R.id.ltype)
        qrcodeImageView = view.findViewById(R.id.qrcode)
        logoutButton = view.findViewById(R.id.Logout)

        // Initialize SQLite Database Helper
        dbHelper = DatabaseHelper(requireContext())

        // Fetch current user email from Firebase
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail != null) {
            // Query SQLite to get user details using the email
            val user = dbHelper.getUserByEmail(currentUserEmail)

            if (user != null) {
                // Populate the profile views with the user data
                nameTextView.text = user.name
                emailTextView.text = user.email
                ageTextView.text = user.age
                licenseTypeTextView.text = user.licenseType

                // Create and set QR code with user profile information
                val profileInfo = "Name: ${user.name}\nEmail: ${user.email}\nAge: ${user.age}\nLicense Type: ${user.licenseType}"
                val qrCodeBitmap = generateQRCode(profileInfo)
                qrcodeImageView.setImageBitmap(qrCodeBitmap)
            }
        }

        // Handle logout button click
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Navigate to the Login Screen using Navigation Component
            findNavController().navigate(R.id.action_homeScreen_to_loginScreen)
        }

        return view
    }
    private fun generateQRCode(profileInfo: String): Bitmap? {
        val qrCodeWriter = QRCodeWriter()
        val hints = hashMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.MARGIN] = 1
        try {
            val bitMatrix = qrCodeWriter.encode(profileInfo, BarcodeFormat.QR_CODE, 500, 500, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            return bmp
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
