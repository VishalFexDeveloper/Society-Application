package com.example.society.Activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.society.DialogBox.ShowProgress
import com.example.society.Model.ProfileDetails
import com.example.society.R
import com.example.society.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var phoneAuth: FirebaseAuth
    private var number: String? = null
    private val IMAGE_REQUEST = 9191
    private var imgUri: Uri? = null
    private lateinit var firebaseStorage: FirebaseStorage

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        phoneAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        number = intent.getStringExtra("number")
        saveProfile("profileActivity")
        binding.profileImg.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    IMAGE_REQUEST
                )
            }
        }

        binding.createAccountBtn.setOnClickListener {
            upLoadDetails()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == IMAGE_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Permission required to select an image", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST)
    }

    private fun upLoadDetails() {
        val userName = binding.userName.text.toString().trim()
        val email = binding.profileEmail.text.toString().trim()
        val society = binding.societyName.text.toString().trim().toLowerCase(Locale.ROOT)
        val pinCode = binding.pinCode.text.toString().trim()
        val houseNo = binding.houseNo.text.toString().trim()

        val selectedGenderId = binding.genderRadioGroup.checkedRadioButtonId
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        when {
            userName.isEmpty() -> {
                binding.userName.error = "Enter your full name"
                binding.userName.requestFocus()
            }

            email.isEmpty() -> {
                binding.profileEmail.error = "Enter your email address"
                binding.profileEmail.requestFocus()
            }

            society.isEmpty() -> {
                binding.societyName.error = "Enter your society name"
                binding.societyName.requestFocus()
            }

            pinCode.isEmpty() -> {
                binding.pinCode.error = "Enter your pin code"
                binding.pinCode.requestFocus()
            }


            imgUri == null || imgUri.toString().isEmpty() -> {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }

            selectedGenderId == -1 -> {
                Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
            }

            else -> {
                val selectedGender = when (selectedGenderId) {
                    R.id.male -> "Male"
                    R.id.female -> "Female"
                    R.id.other -> "Other"
                    else -> ""
                }

                if (email.matches(emailPattern.toRegex())) {
                    val progressDialog = ShowProgress.showProgressDialog(this, "Profile uploading ...")
                    phoneAuth.currentUser?.let { it ->
                        firebaseStorage.reference.child("userProfile").child(
                            it.uid
                        ).putFile(imgUri!!).addOnSuccessListener { task ->
                            task.storage.downloadUrl.addOnSuccessListener { imgageUri ->
                                val model = ProfileDetails(
                                    imgageUri.toString(),
                                    society,
                                    number,
                                    pinCode,
                                    userName,
                                    email,
                                    houseNo = houseNo,
                                    gender = selectedGender,
                                    userId = phoneAuth.currentUser?.uid
                                )

                                phoneAuth.currentUser?.let {
                                    db.collection("userDetails").document(it.uid)
                                        .set(model)
                                        .addOnSuccessListener {
                                            progressDialog.dismiss()
                                            val intent = Intent(this, HomeActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                            saveProfile("")
                                        }
                                        .addOnFailureListener { e ->
                                            progressDialog.dismiss()
                                            Toast.makeText(
                                                this,
                                                "Error uploading details: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }.addOnFailureListener {
                                progressDialog.dismiss()
                                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                            }

                        }.addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }
    }


    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                imgUri = selectedImageUri
                Glide.with(this).load(selectedImageUri).placeholder(R.drawable.img)
                    .into(binding.profileImg)
            }
        }
    }

    private fun saveProfile(profileActivity: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("profile", MODE_PRIVATE)
        val edits = sharedPreferences.edit()
        edits.putString("profileActivity", profileActivity)
        edits.apply()
    }


}