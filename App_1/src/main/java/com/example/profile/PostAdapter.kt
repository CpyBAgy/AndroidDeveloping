package com.example.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.profile.models.Post

class PostAdapter(
    private val onLikeClickListener: (Post, Int) -> Unit,
    private val onCommentClickListener: (Post) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view, onLikeClickListener, onCommentClickListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class PostViewHolder(
        itemView: View,
        private val onLikeClick: (Post, Int) -> Unit,
        private val onCommentClick: (Post) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val postImageView: ImageView = itemView.findViewById(R.id.postImageView)
        private val postTextView: TextView = itemView.findViewById(R.id.postTextView)
        private val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        private val likesTextView: TextView = itemView.findViewById(R.id.likesTextView)
        private val commentButton: ImageButton = itemView.findViewById(R.id.commentButton)
        private val commentsTextView: TextView = itemView.findViewById(R.id.commentsTextView)

        fun bind(post: Post, position: Int) {
            postTextView.text = post.text

            likesTextView.text = post.likesCount.toString()
            commentsTextView.text = post.commentsCount.toString()

            post.imageUrl?.let {
                postImageView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(it)
                    .centerCrop()
                    .into(postImageView)
            } ?: run {
                postImageView.visibility = View.GONE
            }

            updateLikeButtonState(post.isLiked)

            likeButton.setOnClickListener {
                onLikeClick(post, position)
            }

            commentButton.setOnClickListener {
                onCommentClick(post)
            }
        }

        private fun updateLikeButtonState(isLiked: Boolean) {
            likeButton.setImageResource(
                if (isLiked) R.drawable.ic_liked
                else R.drawable.ic_like
            )
        }
    }

    fun updatePostLikeStatus(position: Int, isLiked: Boolean, likesCount: Int) {
        val post = getItem(position)
        val updatedPost = post.copy(isLiked = isLiked, likesCount = likesCount)

        val currentList = currentList.toMutableList()
        currentList[position] = updatedPost
        submitList(currentList)

        notifyItemChanged(position)
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}