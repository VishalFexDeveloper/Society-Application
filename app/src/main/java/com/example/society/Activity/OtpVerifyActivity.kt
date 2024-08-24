package com.example.society.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.society.databinding.ActivityOtpVerifyBinding
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class OtpVerifyActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityOtpVerifyBinding
    private var verificationId: String? = null
    private var number: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        verificationId = intent.getStringExtra("verificationId").toString()
        number = intent.getStringExtra("number")

        binding.continueOtpBtn.setOnClickListener {
            val sendOtp = binding.verticalOtp.text.toString().trim()
            if (sendOtp.length == 6 && sendOtp.isNotEmpty()) {
                verifyCode(sendOtp)
            }
        }
    }

    private fun verifyCode(code: String) {
        val credential = verificationId?.let { PhoneAuthProvider.getCredential(it, code) }
        if (credential != null) {
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    checkIfUserAlreadyRegistered()
                } else {
                    Toast.makeText(this, "Verification failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkIfUserAlreadyRegistered() {
        val userDocumentRef =
            auth.currentUser?.let {
                FirebaseFirestore.getInstance().collection("userDetails").document(
                    it.uid
                )
            }
        userDocumentRef?.get()?.addOnSuccessListener { document ->
            if (document.exists()) {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("number", number)
                }
                startActivity(intent)
                finish()
            }
        }?.addOnFailureListener { e ->
            Toast.makeText(
                this,
                "Error checking registration status: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


}