package com.firstverse.instafire

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firstverse.instafire.models.Post
import java.math.BigInteger
import java.security.MessageDigest

class PostsAdapter(val context: Context, val posts: List<Post>): RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(post: Post){
            val username = itemView.findViewById<TextView>(R.id.tvUsername)
            val desc=itemView.findViewById<TextView>(R.id.tvDescription)
            val image=itemView.findViewById<ImageView>(R.id.ivPost)
            val uploadTime=itemView.findViewById<TextView>(R.id.tvRelativeTime)
            val extrImage=itemView.findViewById<ImageView>(R.id.ivProfileImage)
            username.text=post.user?.username
            desc.text=post.description
            Glide.with(context).load(post.imageUrl).into(image)
            Glide.with(context).load(getProfileImageUrl(username.toString())).into(extrImage)
            uploadTime.text=DateUtils.getRelativeTimeSpanString(post.creationTimeMs)
        }

        private fun getProfileImageUrl(username: String): String{
            val digest=MessageDigest.getInstance("MD5")
            val hash=digest.digest(username.toByteArray())
            val bigInt = BigInteger(hash)
            val hex=bigInt.abs().toString(16)
            return "https://www.gravatar.com/avatar/$hex?d=identicon"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount()=posts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }


}