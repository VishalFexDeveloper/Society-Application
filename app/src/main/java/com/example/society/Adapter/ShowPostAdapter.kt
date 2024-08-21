package com.example.society.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.society.Activity.OtherProfileActivity
import com.example.society.Activity.PostItemDetailsActivity
import com.example.society.Model.PostingItemModel
import com.example.society.R
import com.example.society.databinding.PostShowLauoutBinding

class ShowPostAdapter(
    private val context: Context,
    private val showPostList: MutableList<PostingItemModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ViewHolder(val binding: PostShowLauoutBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(announcement: PostingItemModel) {
            binding.showUserNameText.text = announcement.userName
            binding.showCategoryText.text = announcement.society
            binding.showTitleText.text = announcement.title
            binding.timeText.text = announcement.date
            Glide.with(context).load(announcement.announcementImg).placeholder(R.drawable.img).into(binding.showBannerImg)
            Glide.with(context).load(announcement.userProImg).placeholder(R.drawable.img).into(binding.showProfileImg)

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
         val binding = PostShowLauoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val announcement = showPostList[position]
            holder.bind(announcement)
            holder.binding.root.setOnClickListener {
                val intent = Intent(context,PostItemDetailsActivity::class.java).apply {
                    putExtra("postingId",showPostList[position].AnnId)
                    putExtra("userId",showPostList[position].userId)
                }
                context.startActivity(intent)
            }
            holder.binding.profileLayout.setOnClickListener {
                val intent = Intent(context, OtherProfileActivity::class.java).apply {
                    putExtra("userId",showPostList[position].userId)
                }
                context.startActivity(intent)
            }

        }
    }

    override fun getItemCount(): Int {
        return showPostList.size
    }


}
