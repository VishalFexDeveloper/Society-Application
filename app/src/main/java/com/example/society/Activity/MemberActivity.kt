package com.example.society.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.society.Adapter.MembersAdapter
import com.example.society.Model.ProfileDetails
import com.example.society.Mvvm.UserViewModel
import com.example.society.Mvvm.UserProfileViewModelFactory
import com.example.society.Mvvm.UserRepository
import com.example.society.databinding.ActivityMemberBinding
import com.google.firebase.firestore.FirebaseFirestore

class MemberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMemberBinding
    private lateinit var showAllMembersList: MutableList<ProfileDetails>
    private lateinit var membersAdapter: MembersAdapter
    private lateinit var viewModel: UserViewModel
    private var userId: String? =null
    var societyName:String?=null
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("userId")
        societyName = intent.getStringExtra("society")

        showAllMembersList = mutableListOf()
        membersAdapter = MembersAdapter(this, showAllMembersList)
        binding.allMembersList.layoutManager = LinearLayoutManager(this)
        binding.allMembersList.adapter = membersAdapter

        val repository = UserRepository()
        val viewModelFactory = UserProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]

        getAllMembers()

        binding.membersArrowBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("openFragment", "ProfileFragment")
            }
            startActivity(intent)
            finish()
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    fun getAllMembers() {
        FirebaseFirestore.getInstance().collection("userDetails").get()
            .addOnSuccessListener { snapshot ->
                val allMembersList = snapshot.toObjects(ProfileDetails::class.java)
                showAllMembersList.clear()

                for (member in allMembersList) {
                    if (member.userId != userId && member.society == societyName) {
                        showAllMembersList.add(member)
                    }
                }

                membersAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("openFragment", "ProfileFragment")
        }
        startActivity(intent)
        finish()
    }
}