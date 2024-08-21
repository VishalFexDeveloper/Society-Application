package com.example.society.Mvvm

import android.util.Log
import com.example.society.Model.PostingItemModel
import com.example.society.Model.PostingModel
import com.example.society.Model.ProfileDetails
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUserProfile(userId: String): ProfileDetails? {
        return try {
            val documentSnapshot = firestore.collection("userDetails").document(userId).get().await()
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




}
