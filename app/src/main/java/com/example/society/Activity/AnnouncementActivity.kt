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
import com.example.society.Model.PostingItemModel
import com.example.society.Model.PostingModel
import com.example.society.Model.ProfileDetails
import com.example.society.Mvvm.UserViewModel
import com.example.society.Mvvm.UserViewModelFactory
import com.example.society.Mvvm.UserRepository
import com.example.society.R
import com.example.society.databinding.ActivityAnnouncementBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID

class AnnouncementActivity : AppCompatActivity() {
    lateinit var binding: ActivityAnnouncementBinding
    private lateinit var viewModel: UserViewModel
    private lateinit var userId: String
    private var announcementBanner: Uri? = null
    private val PICK_IMAGE_REQUEST = 9191
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        val repository = UserRepository()
        val viewModelFactory = UserViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]

        binding.announcementImgLayout.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
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

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "null"
        viewModel.fetchUserProfile(userId)


        binding.announcementShareButton.setOnClickListener {
            viewModel.userProfile.observe(this) { profile ->
                if (profile != null) {
                    postAnnouncement(profile)
                }
            }
        }

        binding.postLoadBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    @Deprecated("Deprecated in Java")
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
                Toast.makeText(
                    this,
                    "Permission required to select an image",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Deprecated(
        "This method has been deprecated in favor of using the Activity Result API\n  " +
                "    which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n " +
                "     contracts for common intents available in\n   " +
                "   {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n   " +
                "   testing, and allow receiving results in separate, testable classes independent from your\n " +
                "     activity. Use\n   " +
                "   {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n     " +
                " with the appropriate {@link ActivityResultContract} and handling the result in the\n   " +
                "   {@link ActivityResultCallback#onActivityResult(Object) callback}."
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                announcementBanner = selectedImageUri
                Glide.with(this).load(selectedImageUri).placeholder(R.drawable.img)
                    .into(binding.announcementFullImg)
                binding.announcementFullImg.visibility = View.VISIBLE
                binding.announcementImage.visibility = View.GONE
                binding.announcementBannerText.visibility = View.GONE
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postAnnouncement(profile: ProfileDetails) {
        val title = binding.announcementTitle.text.toString().trim()
        val content = binding.announcementContent.text.toString().trim()
        val date = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
        val currentTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val timeString = currentTime.format(formatter)
        when {
            title.isEmpty() -> {
                binding.announcementTitle.error = "Enter announcement title"
                binding.announcementTitle.requestFocus()
            }

            content.isEmpty() -> {
                binding.announcementContent.error = "Enter announcement content"
                binding.announcementContent.requestFocus()
            }


            announcementBanner == null || announcementBanner.toString().isEmpty() -> {
                Toast.makeText(this, "Please select an announcement banner", Toast.LENGTH_SHORT)
                    .show()
            }

            else -> {
                val announcementId = UUID.randomUUID().toString()
                val progressDialog = ShowProgress.showProgressDialog(this, "Uploading announcement...")
                firebaseStorage.reference.child("Announcements").child(userId).child(announcementId)
                    .putFile(
                        announcementBanner!!
                    ).addOnSuccessListener { task ->
                    task.storage.downloadUrl.addOnSuccessListener { imgUri ->
                        val postingItemModel = PostingItemModel(
                            AnnId = announcementId,
                            title = title,
                            content = content,
                            date = date,
                            isPinned = false,
                            announcementImg = imgUri.toString(),
                            userProImg = profile.profileImg,
                            userName = profile.userName,
                            time = timeString,
                            society = profile.society,
                            userId = userId,
                            phoneNumber = profile.number
                        )
                        val userDocRef = firestore.collection("Announcements").document(userId)
                        userDocRef.get().addOnSuccessListener { documentSnapshot ->
                            val postingModel: PostingModel = if (documentSnapshot.exists()) {
                                documentSnapshot.toObject(PostingModel::class.java) ?: PostingModel()
                            } else {
                                PostingModel()
                            }
                            postingModel.userAnnouncements.forEach { item ->
                                if (item.AnnId == null) {
                                    item.AnnId = UUID.randomUUID().toString()
                                }
                            }
                            postingModel.userAnnouncements.add(postingItemModel)
                            userDocRef.set(postingModel)
                                .addOnSuccessListener {
                                    progressDialog.dismiss()
                                    val intent = Intent(this, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    progressDialog.dismiss()
                                    Toast.makeText(this, "Failed to post announcement.", Toast.LENGTH_SHORT).show()
                                }
                        }.addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Failed to retrieve data.", Toast.LENGTH_SHORT).show()
                        }
                    }

                }

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