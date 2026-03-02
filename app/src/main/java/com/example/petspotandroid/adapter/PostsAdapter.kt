package com.example.petspotandroid.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petspotandroid.R
import com.example.petspotandroid.models.Post
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.ContextCompat

class PostsAdapter(
    private var posts: List<Post>,
    private val onPostClicked: (Post) -> Unit
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setPosts(newPosts: List<Post>) {
        this.posts = newPosts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)

        holder.itemView.setOnClickListener {
            onPostClicked(post)
        }
    }

    override fun getItemCount(): Int = posts.size

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.post_image)
        private val statusView: TextView = itemView.findViewById(R.id.post_status)
        private val typeView: TextView = itemView.findViewById(R.id.post_pet_type)
        private val locationView: TextView = itemView.findViewById(R.id.post_location)
        private val dateView: TextView = itemView.findViewById(R.id.post_date)
        private val descriptionView: TextView = itemView.findViewById(R.id.post_description)

        fun bind(post: Post) {
            locationView.text = post.lastSeenLocation
            descriptionView.text = post.text
            typeView.text = post.petType

            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            dateView.text = dateFormat.format(Date(post.timestamp))

            if (post.isLost) {
                statusView.text = ContextCompat.getString(itemView.context, R.string.lost)
                statusView.setBackgroundResource(R.drawable.bg_badge)
                val redColor = ContextCompat.getColor(itemView.context, R.color.status_lost)
                statusView.background.mutate().setTint(redColor)
            } else {
                statusView.text = ContextCompat.getString(itemView.context, R.string.found)
                statusView.setBackgroundResource(R.drawable.bg_badge)
                val greenColor = ContextCompat.getColor(itemView.context, R.color.status_found)
                statusView.background.mutate().setTint(greenColor)
            }

            if (!post.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(post.imageUrl)
                    .fit()
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .into(imageView)
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_camera)
            }
        }
    }
}