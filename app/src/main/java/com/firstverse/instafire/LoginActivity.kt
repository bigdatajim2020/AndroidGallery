package com.firstverse.instafire

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

private const val TAG="LoginActivity"
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btn=findViewById<Button>(R.id.btnLogin)
        Log.i("Test", btn.toString())
        val auth=FirebaseAuth.getInstance()
        if (auth.currentUser!=null){
            goPostsActivity()
        }
        btn.setOnClickListener{
            btn.isEnabled = false

            val email=findViewById<EditText>(R.id.etEmail).text.toString()

            val password=findViewById<EditText>(R.id.etPassword).text.toString()

            if (email.isBlank() || password.isBlank()){
                Toast.makeText(this, "Email or password can't be null", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //connect to Firebase auth check
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                btn.isEnabled=true
                if (task.isSuccessful){
                    Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
                    goPostsActivity()
                } else{
                    Log.i(TAG, "signInWithEmail failed", task.exception)
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

    private fun goPostsActivity() {
        Log.i(TAG, "goPostsActivity")
        val intent = Intent(this, PostActivity::class.java)
        startActivity(intent)
        finish()

    }
}