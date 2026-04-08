package com.example.petspotandroid.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.petspotandroid.R
import com.example.petspotandroid.data.models.Comment
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentsAdapter(private var comments: List<Comment>) :
    RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateComments(newComments: List<Comment>) {
        this.comments = newComments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarView: ImageView = itemView.findViewById(R.id.commentAvatar)
        private val nameView: TextView = itemView.findViewById(R.id.commentAuthorName)
        private val textView: TextView = itemView.findViewById(R.id.commentText)
        private val timeView: TextView = itemView.findViewById(R.id.commentTime)

        fun bind(comment: Comment) {
            nameView.text = comment.authorName
            textView.text = comment.text

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeView.text = timeFormat.format(Date(comment.timestamp))

            if (comment.authorProfileImageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(comment.authorProfileImageUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .fit()
                    .centerCrop()
                    .into(avatarView)
            } else {
                avatarView.setImageResource(R.drawable.ic_person)
            }
        }
    }
}