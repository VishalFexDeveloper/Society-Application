package com.example.society.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.society.Fragment.HomeFragment
import com.example.society.Model.PostingItemModel
import com.example.society.Model.PostingModel
import com.example.society.R
import com.example.society.databinding.ActivityMemberBinding
import com.example.society.databinding.ActivityPostItemDetailsBinding
import com.google.firebase.firestore.FirebaseFirestore

class PostItemDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivityPostItemDetailsBinding
    lateinit var postItems: PostingItemModel
    var userId: String? = null
    val REQUEST_CALL_PHONE = 1990

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostItemDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val postingId = intent.getStringExtra("postingId")
        userId = intent.getStringExtra("userId") // Use class-level userId

        binding.itemDetailsArrowBtn.setOnClickListener {
            finish()
        }

        binding.profileLayout.setOnClickListener {
            val intent = Intent(this,OtherProfileActivity::class.java).apply {
                putExtra("userId",userId)
            }
            startActivity(intent)

        }

        if (HomeFragment.showPostList.isEmpty()) {
            binding.showPostNoData.visibility = View.VISIBLE
            binding.itemDetailsProgressbar.visibility = View.GONE
        } else {
            binding.showPostNoData.visibility = View.GONE
            binding.itemDetailsProgressbar.visibility = View.VISIBLE
            binding.itemDetailsLayout.visibility = View.GONE

            if (userId != null) {
                FirebaseFirestore.getInstance().collection("Announcements").document(userId!!).get()
                    .addOnSuccessListener { documentSnapshot ->
                        val postingModel = documentSnapshot.toObject(PostingModel::class.java)
                        postingModel?.userAnnouncements?.let { announcements ->
                            val postItem = announcements.find { it.AnnId == postingId }
                            if (postItem != null) {
                                postItems = postItem
                                binding.itemDetailsName.text = postItems.userName
                                binding.itemDetailsTitle.text = postItems.title
                                binding.itemDetailscontacts.text = postItems.content
                                binding.itemDetailsSociety.text = postItems.society
                                binding.itemDetailsDate.text = "Date ${postItems.date}  Time ${postItems.time}"
                                Glide.with(this).load(postItems.announcementImg).placeholder(R.drawable.img).into(binding.itemDetailsBannerImg)
                                Glide.with(this).load(postItems.userProImg).placeholder(R.drawable.img).into(binding.itemDetailsProfileImg)

                                binding.itemDetailsProgressbar.visibility = View.GONE
                                binding.itemDetailsLayout.visibility = View.VISIBLE
                                if (postItem.userId == userId){
                                    binding.itemDetailsCallBtn.visibility = View.GONE
                                }
                            } else {
                                binding.showPostNoData.visibility = View.VISIBLE
                                binding.itemDetailsProgressbar.visibility = View.GONE
                                Toast.makeText(this, "Announcement not found.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener {
                        binding.showPostNoData.visibility = View.VISIBLE
                        binding.itemDetailsProgressbar.visibility = View.GONE
                        Toast.makeText(this, "Failed to load data.", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.itemDetailsCallBtn.setOnClickListener {
            val phoneNumber = postItems.phoneNumber
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(intent)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PHONE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                val phoneNumber = postItems.phoneNumber
                val intent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Permission denied to make calls.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
