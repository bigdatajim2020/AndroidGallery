package com.firstverse.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstverse.instafire.models.Post
import com.firstverse.instafire.models.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val TAG = "PostsActivity"
public const val EXTRA_USERNAME="EXTRA_USERNAME"
open class PostActivity : AppCompatActivity() {

    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostsAdapter

    private var signedInUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        //create layout file for one post
        //create data source
        posts = mutableListOf()
        //create adapter
        adapter= PostsAdapter(this, posts)
        //bind adapter and layout manager to rv

        val rvPost=findViewById<RecyclerView>(R.id.rvPosts)

        rvPost.adapter=adapter
        rvPost.layoutManager=LinearLayoutManager(this)

        Log.i(TAG, "Posts Test Info")

        firestoreDb = FirebaseFirestore.getInstance()

        firestoreDb.collection("Users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser=userSnapshot.toObject(User::class.java)
                Log.i(TAG, "Signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Something wrong", exception)
                            }
        var postsReference = firestoreDb.collection("Posts")
            .limit(20)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)
        val username=intent.getStringExtra(EXTRA_USERNAME)
        if (username!=null){
            supportActionBar?.title=username
           postsReference= postsReference.whereEqualTo("user.username", username)
        }

        postsReference.addSnapshotListener { snapshot, exception ->
            if (exception!=null || snapshot == null){
                Log.e(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Post::class.java)

            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()

            Log.i("Post List", postList.size.toString())
            for (post in postList){
                Log.i("Post List Object ", "Post $post")
            }
        }
        val fab=findViewById<FloatingActionButton>(R.id.fabCreate)
        fab.setOnClickListener{
            val intent =Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.menu_profile){
            val intent= Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)
    }
}