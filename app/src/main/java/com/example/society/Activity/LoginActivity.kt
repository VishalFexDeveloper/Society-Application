package com.example.society.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.society.DialogBox.ShowProgress
import com.example.society.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private lateinit var binding: ActivityLoginBinding
    private var number :String?=null
    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.continueNumBtn.setOnClickListener {
            var phoneNumber = binding.editNumber.text.toString().trim()
            phoneNumber = phoneNumber.replace(" ", "")
            if (phoneNumber.startsWith("+91")) {
                phoneNumber = phoneNumber.removePrefix("+91").trim()
            }
            if (phoneNumber.length > 10) {
                phoneNumber = phoneNumber.takeLast(10)
            }
            if (phoneNumber.length == 10 && phoneNumber.all { it.isDigit() }) {
                sendVerificationCode(phoneNumber)
                number = phoneNumber
            } else {
                Toast.makeText(
                    this,
                    "Please enter a valid 10-digit phone number",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val showProgress = ShowProgress.showProgressDialog(this,"Please wait, verifying your phone number")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential)
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Verification failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showProgress.dismiss()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    showProgress.dismiss()
                    this@LoginActivity.verificationId = verificationId
                    val intent = Intent(this@LoginActivity, OtpVerifyActivity::class.java).apply {
                        putExtra("verificationId", verificationId)
                        putExtra("number",number)
                    }
                    startActivity(intent)
                    finish()
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }





}