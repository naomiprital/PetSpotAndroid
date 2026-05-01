package com.example.petspotandroid.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.petspotandroid.R
import com.example.petspotandroid.data.models.Post
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso

class UserPostsAdapter(
    private var posts: List<Post>,
    private val onItemClick: (Post) -> Unit,
    private val onEditClick: (Post) -> Unit,
    private val onDeleteClick: (Post) -> Unit,
    private val onResolveToggleClick: (Post) -> Unit
) : RecyclerView.Adapter<UserPostsAdapter.UserPostViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setPosts(newPosts: List<Post>) {
        this.posts = newPosts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_post, parent, false)
        return UserPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserPostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post, onItemClick, onEditClick, onDeleteClick, onResolveToggleClick)
    }

    override fun getItemCount(): Int = posts.size

    class UserPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.post_image)
        private val badgeResolved: View = itemView.findViewById(R.id.badge_resolved)
        private val statusView: TextView = itemView.findViewById(R.id.post_status)
        private val dateView: TextView = itemView.findViewById(R.id.post_date)
        private val descriptionView: TextView = itemView.findViewById(R.id.post_description)
        private val commentCountView: TextView = itemView.findViewById(R.id.post_comment_count)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
        private val btnResolve: MaterialButton = itemView.findViewById(R.id.btnResolve)
        private val llContentDimmed: View = itemView.findViewById(R.id.llContentDimmed)

        @SuppressLint("SetTextI18n")
        fun bind(
            post: Post,
            onItemClick: (Post) -> Unit,
            onEditClick: (Post) -> Unit,
            onDeleteClick: (Post) -> Unit,
            onResolveToggleClick: (Post) -> Unit
        ) {
            val context = itemView.context
            
            descriptionView.text = post.description
            dateView.text = post.eventDate
            commentCountView.text = post.comments.size.toString()

            val statusTextId = if (post.isLost) R.string.lost else R.string.found
            val statusColor = ContextCompat.getColor(context, if (post.isLost) R.color.status_lost_text else R.color.status_found_text)
            val statusBgColor = ContextCompat.getColor(context, if (post.isLost) R.color.status_lost_bg else R.color.status_found_bg)
            
            statusView.text = context.getString(statusTextId)
            statusView.setTextColor(statusColor)
            statusView.backgroundTintList = ColorStateList.valueOf(statusBgColor)

            if (post.isResolved) {
                badgeResolved.visibility = View.VISIBLE
                
                val resolvedBg = ContextCompat.getColor(context, R.color.resolved_bg)
                val resolvedText = ContextCompat.getColor(context, R.color.resolved_text)
                val resolvedStroke = ContextCompat.getColor(context, R.color.resolved_stroke)

                btnResolve.text = context.getString(R.string.mark_as_unresolved)
                btnResolve.setIconResource(R.drawable.ic_undo)
                btnResolve.backgroundTintList = ColorStateList.valueOf(resolvedBg)
                btnResolve.setTextColor(resolvedText)
                btnResolve.iconTint = ColorStateList.valueOf(resolvedText)
                btnResolve.setStrokeColor(ColorStateList.valueOf(resolvedStroke))
                
                btnEdit.isEnabled = false
                btnDelete.isEnabled = false
                llContentDimmed.alpha = 0.5f
                imageView.alpha = 0.5f
                
                btnEdit.setOnClickListener(null)
                btnDelete.setOnClickListener(null)
            } else {
                badgeResolved.visibility = View.GONE
                
                val foundBg = ContextCompat.getColor(context, R.color.status_found_bg)
                val unresolvedText = ContextCompat.getColor(context, R.color.unresolved_text)
                val unresolvedStroke = ContextCompat.getColor(context, R.color.unresolved_stroke)

                btnResolve.text = context.getString(R.string.mark_listing_as_resolved)
                btnResolve.setIconResource(R.drawable.ic_check_circle)
                btnResolve.backgroundTintList = ColorStateList.valueOf(foundBg)
                btnResolve.setTextColor(unresolvedText)
                btnResolve.iconTint = ColorStateList.valueOf(unresolvedText)
                btnResolve.setStrokeColor(ColorStateList.valueOf(unresolvedStroke))
                
                btnEdit.isEnabled = true
                btnDelete.isEnabled = true
                llContentDimmed.alpha = 1.0f
                imageView.alpha = 1.0f
                
                btnEdit.setOnClickListener { onEditClick(post) }
                btnDelete.setOnClickListener { onDeleteClick(post) }
            }

            if (post.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(post.imageUrl)
                    .fit()
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .error(android.R.drawable.ic_menu_camera)
                    .into(imageView)
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_camera)
            }

            btnResolve.setOnClickListener { onResolveToggleClick(post) }
            itemView.setOnClickListener { onItemClick(post) }
        }
    }
}
