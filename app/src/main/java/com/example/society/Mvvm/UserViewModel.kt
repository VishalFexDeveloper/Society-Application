package com.example.society.Mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.society.Model.PostingItemModel
import com.example.society.Model.ProfileDetails
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _userProfile = MutableLiveData<ProfileDetails?>()
    val userProfile: LiveData<ProfileDetails?> get() = _userProfile

    private val _showPostList = MutableLiveData<List<PostingItemModel>>()
    val showPostList: LiveData<List<PostingItemModel>> get() = _showPostList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> get() = _isEmpty

    fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            val profile = repository.getUserProfile(userId)
            _userProfile.postValue(profile)
        }
    }

    fun fetchFilteredPosts(societyName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val filteredPosts = repository.getFilteredPosts(societyName)
            _isLoading.value = false
            _showPostList.value = filteredPosts
            _isEmpty.value = filteredPosts.isEmpty()
        }
    }

}