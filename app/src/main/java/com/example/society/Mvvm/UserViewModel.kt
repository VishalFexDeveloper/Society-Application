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



    fun fetchUserProfile(userId: String) {
        viewModelScope.launch {
            val profile = repository.getUserProfile(userId)
            _userProfile.postValue(profile)
        }
    }




}