package com.example.society.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.society.Adapter.ProfilePostAdapter
import com.example.society.Model.PostingItemModel
import com.example.society.Model.PostingModel
import com.example.society.Model.ProfileDetails
import com.example.society.Mvvm.UserProfileViewModelFactory
import com.example.society.Mvvm.UserRepository
import com.example.society.Mvvm.UserViewModel
import com.example.society.R
import com.example.society.databinding.ActivityOtherProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OtherProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityOtherProfileBinding
    private lateinit var viewModel: UserViewModel
    private  var userId: String ? = null
    val firestore = FirebaseFirestore.getInstance()
    lateinit var profilePostAdapter: ProfilePostAdapter
    lateinit var showPostList: MutableList<PostingItemModel>
    var societyName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtherProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = UserRepository()
        val viewModelFactory = UserProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]

        showPostList = mutableListOf()
        profilePostAdapter = ProfilePostAdapter(this, showPostList)
        binding.profilePostAll.layoutManager = GridLayoutManager(this, 2)
        binding.profilePostAll.adapter = profilePostAdapter

        binding.profileProgressBar.visibility = View.VISIBLE
        binding.profileLayout.visibility = View.GONE
        binding.profileErrorImg.visibility = View.GONE

        viewModel.userProfile.observe(this) { profile ->
            if (profile != null) {
                profileDataSet(profile)
            } else {
                binding.profileProgressBar.visibility = View.GONE
                binding.profileLayout.visibility = View.GONE
                binding.profileErrorImg.visibility = View.VISIBLE
            }
        }

        binding.itemDetailsArrowBtn.setOnClickListener {
            finish()
        }

        userId = intent.getStringExtra("userId")
        viewModel.fetchUserProfile(userId!!)
        binding.itemDetailsArrowBtn

    }


    @SuppressLint("SetTextI18n")
    fun profileDataSet(profile: ProfileDetails) {
        binding.profileProgressBar.visibility = View.GONE
        binding.profileLayout.visibility = View.VISIBLE
        binding.viewUserNameText.text = profile.userName
        binding.viewSocietyText.text = profile.society
        binding.viewEmailText.text = profile.emailId
        societyName = profile.society
        binding.viewCategoryText.text = "+91${profile.number} | ${profile.houseNo}"
        Glide.with(this).load(profile.profileImg).placeholder(R.drawable.img)
            .into(binding.viewProfileImg)
        userId?.let { fetchInitialPosting(it) }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun fetchInitialPosting(uuid: String) {
        binding.showPostProgressBar.visibility = View.VISIBLE
        binding.showPostNoData.visibility = View.GONE
        binding.profilePostAll.visibility = View.GONE
        firestore.collection("Announcements").document(uuid).get()
            .addOnSuccessListener { document ->
                val inMove = document.toObject(PostingModel::class.java)
                if (inMove != null) {
                    for (item in inMove.userAnnouncements) {
                        if (userId == item.userId) {
                            showPostList.add(item)
                            profilePostAdapter.notifyDataSetChanged()
                            if (showPostList.isEmpty()) {
                                binding.showPostNoData.visibility = View.VISIBLE
                                binding.profilePostAll.visibility = View.GONE
                                binding.showPostProgressBar.visibility = View.GONE
                            } else {
                                binding.profilePostAll.visibility = View.VISIBLE
                                binding.showPostNoData.visibility = View.GONE
                                binding.showPostProgressBar.visibility = View.GONE
                            }
                        }else{
                            binding.showPostProgressBar.visibility = View.GONE
                        }
                    }
                }else{
                    binding.showPostNoData.visibility = View.VISIBLE
                    binding.profilePostAll.visibility = View.GONE
                    binding.showPostProgressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show()
                binding.showPostNoData.visibility = View.VISIBLE
                binding.profilePostAll.visibility = View.GONE
                binding.showPostProgressBar.visibility = View.GONE
            }
    }
}