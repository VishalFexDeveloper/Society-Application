package com.example.society.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.society.Model.PostingItemModel
import com.example.society.R
import com.example.society.databinding.ProfilePostLayoutBinding

class ProfilePostAdapter (  private val context: Context,
private val showPostList: MutableList<PostingItemModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ViewHolder(val binding: ProfilePostLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(announcement: PostingItemModel) {
            binding.profilePostTitle.text = announcement.title
            Glide.with(context).load(announcement.announcementImg).placeholder(R.drawable.img).into(binding.profilePostBanner)

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ProfilePostLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
