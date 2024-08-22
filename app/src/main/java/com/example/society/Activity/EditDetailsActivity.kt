package com.example.society.Activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.society.DialogBox.ShowProgress
import com.example.society.Mvvm.UserViewModel
import com.example.society.Mvvm.UserProfileViewModelFactory
import com.example.society.Mvvm.UserRepository
import com.example.society.R
import com.example.society.databinding.ActivityEditDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditDetailsBinding
    private lateinit var viewModel: UserViewModel
    private lateinit var userId: String
    private lateinit var db: FirebaseFirestore
    lateinit var firebaseStorage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 9191
    private var imgUri: String? = null
    private var selcetImg:String?=null
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        val repository = UserRepository()
        val viewModelFactory = UserProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]

        binding.EditProgressBar.visibility = View.VISIBLE
        binding.EditProfileLayout.visibility = View.GONE

        viewModel.userProfile.observe(this) { profile ->
            if (profile != null) {
                binding.EditProgressBar.visibility = View.GONE
                binding.EditProfileLayout.visibility = View.VISIBLE

                val gander = profile.gender
                binding.updateUserNameEdit.setText(profile.userName)
                binding.updateSocietyEdit.setText(profile.society)
                when (gander) {
                    "Female" -> {
                        binding.updateGanderGroup.check(R.id.updateFemale)
                    }

                    "Male" -> {
                        binding.updateGanderGroup.check(R.id.updateMale)
                    }

                    "Other" -> {
                        binding.updateGanderGroup.check(R.id.updateOther)
                    }
                }
                binding.updateHouseEdit.setText(profile.houseNo)
                binding.updatePinCodeEdit.setText(profile.pinCode)
                binding.updateNumberEdit.setText(profile.number)
                binding.updateEmailIdEdit.setText(profile.emailId)
                imgUri = profile.profileImg
                Glide.with(this).load(profile.profileImg).placeholder(R.drawable.img)
                    .into(binding.updateProfileImg)

            } else {
                binding.EditProgressBar.visibility = View.GONE
                binding.EditProfileLayout.visibility = View.GONE
                binding.editProfileErrorImg.visibility = View.VISIBLE
            }
        }
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "null"
        viewModel.fetchUserProfile(userId)

        binding.editSaveBtn.setOnClickListener {
            updateUserProfile()
        }

        binding.updateProfileImg.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    PICK_IMAGE_REQUEST
                )
            }
        }
        binding.upDateArrowBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("openFragment", "ProfileFragment")
            }
            startActivity(intent)
            finish()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PICK_IMAGE_REQUEST) {
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
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun updateUserProfile() {
        val userName = binding.updateUserNameEdit.text.toString().trim()
        val email = binding.updateEmailIdEdit.text.toString().trim()
        val society = binding.updateSocietyEdit.text.toString().trim()
        val pinCode = binding.updatePinCodeEdit.text.toString().trim()
        val houseNo = binding.updateHouseEdit.text.toString().trim()
        val number = binding.updateNumberEdit.text.toString().trim()
        val selectedGenderId = binding.updateGanderGroup.checkedRadioButtonId
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        if (number.isEmpty() || userName.isEmpty() || email.isEmpty() || society.isEmpty() || pinCode.isEmpty() || selectedGenderId == -1) {
            if (number.isEmpty()) binding.updateNumberEdit.error = "Enter your phone number"
            if (userName.isEmpty()) binding.updateUserNameEdit.error = "Enter your full name"
            if (email.isEmpty()) binding.updateEmailIdEdit.error = "Enter your email address"
            if (society.isEmpty()) binding.updateSocietyEdit.error = "Enter your society name"
            if (pinCode.isEmpty()) binding.updatePinCodeEdit.error = "Enter your pin code"
            if (selectedGenderId == -1) Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.matches(emailPattern.toRegex())) {
            val selectedGender = when (selectedGenderId) {
                R.id.updateMale -> "Male"
                R.id.updateFemale -> "Female"
                R.id.updateOther -> "Other"
                else -> ""
            }
            val progressDialog = ShowProgress.showProgressDialog(this, "Updating profile...")
            if (selcetImg != null){
                val selectedUri = Uri.parse(selcetImg)
                firebaseStorage.reference.child("userProfile").child(userId).putFile(selectedUri).addOnSuccessListener { task ->
                    task.storage.downloadUrl.addOnSuccessListener { imagesUri ->
                        val model = mapOf(
                            "profileImg" to imagesUri.toString(),
                            "society" to society,
                            "number" to number,
                            "pinCode" to pinCode,
                            "userName" to userName,
                            "emailId" to email,
                            "houseNo" to houseNo,
                            "gender" to selectedGender,
                            "userId" to userId
                        )
                        db.collection("userDetails").document(userId)
                            .set(model)
                            .addOnSuccessListener {
                                progressDialog.dismiss()
                                val intent = Intent(this, HomeActivity::class.java).apply {
                                    putExtra("openFragment", "ProfileFragment")
                                }
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                progressDialog.dismiss()
                                Toast.makeText(this, "Error uploading details: ${e.message}", Toast.LENGTH_SHORT).show()
                            }

                    }.addOnFailureListener {
                        Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }else{
                val model = mapOf(
                    "profileImg" to imgUri,
                    "society" to society,
                    "number" to number,
                    "pinCode" to pinCode,
                    "userName" to userName,
                    "emailId" to email,
                    "houseNo" to houseNo,
                    "gender" to selectedGender,
                    "userId" to userId
                )
                db.collection("userDetails").document(userId)
                    .set(model)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        val intent = Intent(this, HomeActivity::class.java).apply {
                            putExtra("openFragment", "ProfileFragment")
                        }
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(this, "Error uploading details: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }


        } else {
            binding.updateEmailIdEdit.error = "Invalid email format"
        }
    }


    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                selcetImg = selectedImageUri.toString()
                Glide.with(this).load(selectedImageUri).placeholder(R.drawable.img)
                    .into(binding.updateProfileImg)
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }



}