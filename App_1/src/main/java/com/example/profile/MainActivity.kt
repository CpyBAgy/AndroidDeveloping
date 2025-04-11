package com.example.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.profile.models.Post
import com.example.profile.models.User

class MainActivity : AppCompatActivity() {

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var followFilledButton: Button
    private lateinit var followOutlinedButton: Button
    private lateinit var messageButton: Button
    private lateinit var avatarImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var nameTextView: TextView
    private lateinit var additionalInfoTextView: TextView
    private lateinit var postsCountTextView: TextView
    private lateinit var followersCountTextView: TextView
    private lateinit var followingCountTextView: TextView

    private var user = User(
        id = 1,
        name = "Ivan Bashkatov",
        username = "CpyBAgy",
        avatarUrl = "https://sun9-20.userapi.com/s/v1/ig2/fmnsWTrH3sEPA9GJslSnAxq1RjzD5yjczC28bNcl8Xc5ZvgmCJv46FT_3z8d-dBGH1t9h7o2j76_-_FQQYw3wTX1.jpg?quality=95&as=32x32,48x48,72x72,108x108,160x160,240x240,360x360,480x480,540x540,640x640,720x720,960x960&from=bu&u=HfsMTLJECGGXSWWW76zMFzxOyYtJrz-UOSB_HFjgjCA&cs=807x807",
        followersCount = 256,
        followingCount = 128,
        postsCount = 4,
        isFollowing = false,
        additionalInfo = "Личный блог айтишника"
    )

    private val postsList = mutableListOf(
        Post(
            id = 1,
            userId = 1,
            text = "Легенда",
            imageUrl = "https://sun9-20.userapi.com/s/v1/ig2/fmnsWTrH3sEPA9GJslSnAxq1RjzD5yjczC28bNcl8Xc5ZvgmCJv46FT_3z8d-dBGH1t9h7o2j76_-_FQQYw3wTX1.jpg?quality=95&as=32x32,48x48,72x72,108x108,160x160,240x240,360x360,480x480,540x540,640x640,720x720,960x960&from=bu&u=HfsMTLJECGGXSWWW76zMFzxOyYtJrz-UOSB_HFjgjCA&cs=807x807",
            likesCount = 201,
            commentsCount = 34
        ),
        Post(
            id = 2,
            userId = 1,
            text = "Новичок в команде",
            imageUrl = "https://sun9-80.userapi.com/impg/_7AuWxRWOhn7nQGjcSTlNAFyJdI2QS48lmIOfA/_VfnqM9z5Nc.jpg?size=1620x2160&quality=95&sign=885d2a3f4f50e2702d52a54feeb97280&type=album",
            likesCount = 157,
            commentsCount = 23,
            isLiked = true
        ),
        Post(
            id = 3,
            userId = 1,
            text = "Руки вверх",
            imageUrl = "https://sun6-21.userapi.com/impg/tZog7t2hdkw7ucA87jLByr7z_bF_5ldIqnEWkw/pozsQo9into.jpg?size=1620x2160&quality=95&sign=fade2a32ab22602ac7060a714fec10ef&type=album",
            likesCount = 89,
            commentsCount = 12
        ),
        Post(
            id = 4,
            userId = 1,
            text = "На серьезном",
            imageUrl = "https://sun9-65.userapi.com/impg/nSzqKg9Z86zJnZUgzN3B6RzfRSpiaNBRZFULOw/6JRP2hE2v4o.jpg?size=960x1280&quality=95&sign=dc6dafe495e06479745027bbb8142b09&type=album",
            likesCount = 201,
            commentsCount = 34
        ),

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()

        setupRecyclerView()

        populateUserData()

        setupClickListeners()
    }

    private fun initViews() {
        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        followFilledButton = findViewById(R.id.followFilledButton)
        followOutlinedButton = findViewById(R.id.followOutlinedButton)
        messageButton = findViewById(R.id.messageButton)
        avatarImageView = findViewById(R.id.avatarImageView)
        usernameTextView = findViewById(R.id.usernameTextView)
        nameTextView = findViewById(R.id.nameTextView)
        additionalInfoTextView = findViewById(R.id.additionalInfoTextView)
        postsCountTextView = findViewById(R.id.postsCountTextView)
        followersCountTextView = findViewById(R.id.followersCountTextView)
        followingCountTextView = findViewById(R.id.followingCountTextView)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            onLikeClickListener = { post, position -> handleLikeClick(post, position) },
            onCommentClickListener = { post -> handleCommentClick(post) }
        )

        postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = postAdapter
            setHasFixedSize(true)
        }

        postAdapter.submitList(postsList)
    }

    private fun populateUserData() {
        Glide.with(this)
            .load(user.avatarUrl)
            .circleCrop()
            .into(avatarImageView)

        usernameTextView.text = user.username
        nameTextView.text = user.name
        additionalInfoTextView.text = user.additionalInfo

        postsCountTextView.text = user.postsCount.toString()
        followersCountTextView.text = user.followersCount.toString()
        followingCountTextView.text = user.followingCount.toString()

        updateFollowButtonState()
    }

    private fun setupClickListeners() {
        followFilledButton.setOnClickListener {
            handleFollowClick()
        }

        followOutlinedButton.setOnClickListener {
            handleFollowClick()
        }

        messageButton.setOnClickListener {
            Toast.makeText(this, "Открыть диалог с ${user.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFollowClick() {
        user = user.copy(
            isFollowing = !user.isFollowing,
            followersCount = if (user.isFollowing) user.followersCount - 1 else user.followersCount + 1
        )

        updateFollowButtonState()
        followersCountTextView.text = user.followersCount.toString()

        Toast.makeText(
            this,
            if (user.isFollowing) "Вы подписались на ${user.username}" else "Вы отписались от ${user.username}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateFollowButtonState() {
        if (user.isFollowing) {
            followFilledButton.visibility = View.GONE
            followOutlinedButton.visibility = View.VISIBLE
        } else {
            followFilledButton.visibility = View.VISIBLE
            followOutlinedButton.visibility = View.GONE
        }
    }

    private fun handleLikeClick(post: Post, position: Int) {
        val newIsLiked = !post.isLiked
        val newLikesCount = if (newIsLiked) post.likesCount + 1 else post.likesCount - 1

        postAdapter.updatePostLikeStatus(position, newIsLiked, newLikesCount)
    }

    private fun handleCommentClick(post: Post) {
        Toast.makeText(
            this,
            "Открыть комментарии к посту: ${post.text.take(20)}...",
            Toast.LENGTH_SHORT
        ).show()
    }
}