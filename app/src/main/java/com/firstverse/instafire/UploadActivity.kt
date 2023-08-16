package com.firstverse.instafire

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firstverse.instafire.models.Post
import com.firstverse.instafire.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


private const val TAG = "UploadActivity"
class UploadActivity : AppCompatActivity() {
    private var photoUri: Uri? = null
    private var signedInUser: User? = null
    private lateinit var imageView: ImageView
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storeageReference: StorageReference
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        storeageReference = FirebaseStorage.getInstance().reference

        firestoreDb = FirebaseFirestore.getInstance()
        firestoreDb.collection("Users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG, "Signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Something wrong", exception)

            }
        val getResult =
                    registerForActivityResult(
                        ActivityResultContracts.StartActivityForResult()
                    ) {
                        if (it.resultCode == RESULT_OK) {
                            val data: Intent? = it.data
                            photoUri = data?.data
                            Log.i(TAG, "$photoUri")
                            imageView = findViewById<ImageView>(R.id.imageView)
                            imageView.setImageURI(photoUri)
                        }
                    }


                val btnPickImage = findViewById<Button>(R.id.btnPickImage)
                btnPickImage.setOnClickListener {
                    Log.i(TAG, "Open up image picker on device")
                    val intent: Intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "image/*"
                    getResult.launch(intent)
                }

                btnSubmit = findViewById<Button>(R.id.btnSubmit)
                btnSubmit.setOnClickListener {
                    handleSubmitButtonClick()
                }

    }

    private fun handleSubmitButtonClick() {
        if (photoUri == null){
            Toast.makeText(this, "No photo selected yet!", Toast.LENGTH_SHORT).show()
            return
        }
        val etDescription = findViewById<EditText>(R.id.etDescription)
        if (etDescription.text.isBlank()){
            Toast.makeText(this, "Description is blank!", Toast.LENGTH_SHORT).show()
            return
        }

        if (signedInUser == null){
            Toast.makeText(this, "No signed in user!", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false

        /* 3 tasks to complete image upload into storage, create post in database, clear description
        clear image view and direct back to profile activity
         */

        val photoUploadUri = photoUri as Uri
        val photoReference = storeageReference.child("images/${System.currentTimeMillis()}--photo.jpg")
        photoReference.putFile(photoUploadUri)
            .continueWithTask{
                photoUploadTask->
                Log.i(TAG, "Uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                photoReference.downloadUrl
            }.continueWithTask{downloadUrlTask ->
            val post = Post(
                etDescription.text.toString(),
                downloadUrlTask.result.toString(),
                System.currentTimeMillis(),
                signedInUser)

                firestoreDb.collection("Posts").add(post)
            }.addOnCompleteListener { postCreationTask ->
                btnSubmit.isEnabled = true
                if (!postCreationTask.isSuccessful){
                    Log.e(TAG, "Exception during Firebase operations", postCreationTask.exception)
                    Toast.makeText(this, "Failed to save post!", Toast.LENGTH_SHORT).show()
                }
                etDescription.text.clear()
                imageView.setImageResource(0)
                Toast.makeText(this, "Post creation succeeds!", Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME, signedInUser?.username)
                startActivity(profileIntent)
                finish()
            }

    }

}