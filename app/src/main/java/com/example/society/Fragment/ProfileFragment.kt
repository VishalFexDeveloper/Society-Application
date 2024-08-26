package com.example.society.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.society.Activity.EditDetailsActivity
import com.example.society.Activity.LoginActivity
import com.example.society.Activity.MemberActivity
import com.example.society.Adapter.ProfilePostAdapter
import com.example.society.Model.PostingItemModel
import com.example.society.Model.PostingModel
import com.example.society.Model.ProfileDetails
import com.example.society.Mvvm.UserViewModel
import com.example.society.Mvvm.UserViewModelFactory
import com.example.society.Mvvm.UserRepository
import com.example.society.R
import com.example.society.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: UserViewModel
    private lateinit var userId: String
    val firestore = FirebaseFirestore.getInstance()
    lateinit var profilePostAdapter: ProfilePostAdapter
    lateinit var showPostList: MutableList<PostingItemModel>
    var societyName: String? = null
    var profileUri:String?=null
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val repository = UserRepository()
        val viewModelFactory = UserViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]

        showPostList = mutableListOf()
        profilePostAdapter = ProfilePostAdapter(requireContext(), showPostList)
        binding.profilePostAll.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.profilePostAll.adapter = profilePostAdapter

        binding.profileProgressBar.visibility = View.VISIBLE
        binding.profileLayout.visibility = View.GONE
        binding.profileErrorImg.visibility = View.GONE

        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                profileDataSet(profile)
            } else {
                binding.profileProgressBar.visibility = View.GONE
                binding.profileLayout.visibility = View.GONE
                binding.profileErrorImg.visibility = View.VISIBLE
            }
        }


        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "null"
        viewModel.fetchUserProfile(userId)
        binding.logOut.setOnClickListener {
            showDialogBox()
        }
        binding.profileEdit.setOnClickListener {
            val intent = Intent(requireContext(), EditDetailsActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.societyMembers.setOnClickListener {
            val intent = Intent(requireContext(), MemberActivity::class.java).apply {
                putExtra("userId", userId)
                putExtra("society", societyName)
            }
            startActivity(intent)
            requireActivity().finish()
        }
        return binding.root
    }

    private fun redirectToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    @SuppressLint("SetTextI18n")
    fun profileDataSet(profile: ProfileDetails) {
        binding.profileProgressBar.visibility = View.GONE
        binding.profileLayout.visibility = View.VISIBLE
        binding.viewUserNameText.text = profile.userName
        binding.viewSocietyText.text = profile.society
        binding.viewEmailText.text = profile.emailId
        societyName = profile.society
        profileUri = profile.profileImg
        binding.viewCategoryText.text = "+91${profile.number} | ${profile.houseNo}"
        Glide.with(this).load(profile.profileImg).placeholder(R.drawable.img)
            .into(binding.viewProfileImg)
        fetchInitialPosting(userId)
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
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
                binding.showPostNoData.visibility = View.VISIBLE
                binding.profilePostAll.visibility = View.GONE
                binding.showPostProgressBar.visibility = View.GONE
            }
    }

    private fun showDialogBox() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                redirectToLogin()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


}