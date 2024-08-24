package com.example.society.Mvvm

import android.util.Log
import com.example.society.Model.PostingItemModel
import com.example.society.Model.PostingModel
import com.example.society.Model.ProfileDetails
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getUserProfile(userId: String): ProfileDetails? {
        return try {
            val documentSnapshot = db.collection("userDetails").document(userId).get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(ProfileDetails::class.java)
            } else {
                Log.e("Firestore", "Document does not exist for userId: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error retrieving profile details for userId: $userId", e)
            null
        }
    }
    suspend fun getFilteredPosts(societyName: String): List<PostingItemModel>{
        return try {
            val querySnapshot = db.collection("Announcements").get().await()
            val postingList = querySnapshot.toObjects(PostingModel::class.java)
            val filteredPosts = mutableListOf<PostingItemModel>()
            for (posting in postingList) {
                for (item in posting.userAnnouncements) {
                    if (item.society == societyName) {
                        filteredPosts.add(item)
                    }
                }
            }
            filteredPosts.sortByDescending { it.date }
            filteredPosts
        }catch (e: Exception) {
            Log.e("AnnouncementsRepo", "Error fetching posts: ${e.message}")
            emptyList()
        }
    }

}
