package com.example.society.Fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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
import com.example.society.R
import com.example.society.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var userId: String
    private lateinit var showPostAdapter: ShowPostAdapter
    val firestore = FirebaseFirestore.getInstance()

    companion object {
        lateinit var showPostList: MutableList<PostingItemModel>
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        showPostList = mutableListOf()
        showPostAdapter = ShowPostAdapter(requireContext(), showPostList)
        binding.showAllPost.layoutManager = LinearLayoutManager(requireContext())
        binding.showAllPost.adapter = showPostAdapter

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "null"

        getUserPosting()

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getUserPosting() {
        binding.showPostProgressBar.visibility = View.VISIBLE
        binding.showPostNoData.visibility = View.GONE
        binding.showAllPost.visibility = View.GONE

        firestore.collection("userDetails").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val profileDetails = documentSnapshot.toObject(ProfileDetails::class.java)
                val societyName = profileDetails?.society

                firestore.collection("Announcements").get()
                    .addOnSuccessListener { querySnapshot ->
                        val postingList = querySnapshot.toObjects(PostingModel::class.java)
                        val filteredPosts = ArrayList<PostingItemModel>()

                        for (posting in postingList) {
                            for (item in posting.userAnnouncements) {
                                if (item.society == societyName) {
                                    filteredPosts.add(item)
                                }
                            }
                        }
                        filteredPosts.sortByDescending { it.date }

                        showPostList.clear()
                        showPostList.addAll(filteredPosts)
                        showPostAdapter.notifyDataSetChanged()

                        if (filteredPosts.isEmpty()) {
                            binding.showPostNoData.visibility = View.VISIBLE
                            binding.showAllPost.visibility = View.GONE
                        } else {
                            binding.showPostNoData.visibility = View.GONE
                            binding.showAllPost.visibility = View.VISIBLE
                        }

                        binding.showPostProgressBar.visibility = View.GONE
                    }
                    .addOnFailureListener {
                        binding.showPostNoData.visibility = View.VISIBLE
                        binding.showPostProgressBar.visibility = View.GONE
                        binding.showAllPost.visibility = View.GONE
                    }
            }
            .addOnFailureListener {
                binding.showPostNoData.visibility = View.VISIBLE
                binding.showPostProgressBar.visibility = View.GONE
                binding.showAllPost.visibility = View.GONE
            }
    }

}
