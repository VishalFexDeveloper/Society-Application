package com.example.society.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.society.Adapter.ShowPostAdapter
import com.example.society.Model.PostingItemModel
import com.example.society.Model.PostingModel
import com.example.society.Model.ProfileDetails
import com.example.society.Mvvm.UserProfileViewModelFactory
import com.example.society.Mvvm.UserRepository
import com.example.society.Mvvm.UserViewModel
import com.example.society.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var userId: String
    private lateinit var showPostAdapter: ShowPostAdapter
    private lateinit var viewModel: UserViewModel
    private lateinit var profileDetails: ProfileDetails
    companion object {
        lateinit var showPostList: MutableList<PostingItemModel>
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val repository = UserRepository()
        val viewModelFactory = UserProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]

        showPostList = mutableListOf()
        showPostAdapter = ShowPostAdapter(requireContext(), showPostList)
        binding.showAllPost.layoutManager = LinearLayoutManager(requireContext())
        binding.showAllPost.adapter = showPostAdapter

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "null"

        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                profileDetails = profile
                val societyName = profile.society
                if (!societyName.isNullOrEmpty()) {
                    viewModel.fetchFilteredPosts(societyName)
                }
            }
        }

        viewModel.showPostList.observe(viewLifecycleOwner, Observer { postList ->
            showPostList.clear()
            showPostList.addAll(postList)
            showPostAdapter.notifyDataSetChanged()
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.showPostProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.isEmpty.observe(viewLifecycleOwner, Observer { isEmpty ->
            if (isEmpty) {
                binding.showPostNoData.visibility = View.VISIBLE
                binding.showAllPost.visibility = View.GONE
            } else {
                binding.showPostNoData.visibility = View.GONE
                binding.showAllPost.visibility = View.VISIBLE
            }
        })

        viewModel.fetchUserProfile(userId)

        return binding.root

    }


}
