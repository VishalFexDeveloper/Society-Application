package com.example.society.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.society.Model.ProfileDetails
import com.example.society.R
import com.example.society.databinding.MembersLayoutBinding

class MembersAdapter(private val context: Context,
                     private val showPostList: MutableList<ProfileDetails>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ViewHolder(val binding: MembersLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(announcement: ProfileDetails) {
            binding.showUserNameText.text = announcement.userName
            binding.showCategoryText.text = announcement.society
            Glide.with(context).load(announcement.profileImg).placeholder(R.drawable.img).into(binding.showProfileImg)

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = MembersLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val announcement = showPostList[position]
            holder.bind(announcement)
        }
    }

    override fun getItemCount(): Int {
        return showPostList.size
    }


}
