package com.example.checklist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LoginActivity : AppCompatActivity() {

    lateinit var usernameField: String
    lateinit var passwordField: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val database = Database()
        database.writeNewAccount("bob", "hi")


        findViewById<Button>(R.id.logInButton).setOnClickListener {logInButtonClicked()}
        findViewById<TextView>(R.id.signInText).setOnClickListener{signInTextClicked()}
    }

    private fun logInButtonClicked(){
        val database = Database()
        usernameField = findViewById<EditText>(R.id.emailText).text.toString()
        passwordField = findViewById<EditText>(R.id.passwordText).text.toString()

        database.validSignInCredentials(usernameField, passwordField){ isValid ->
            if(isValid) {
                Log.d("inside", "you are clicking $isValid")
                val sharedPreferences = getSharedPreferences("Checklist", MODE_PRIVATE)
                sharedPreferences.edit().putString("username",usernameField).apply()
                getSharedPreferences("Checklist", MODE_PRIVATE).edit().putBoolean("loggedIn",true)
                val intent = Intent(this, GroupViewActivity::class.java)
                startActivity(intent)
            }
            else {
                Toast.makeText(this,"Invalid credentials",Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun signInTextClicked(){
        val intent = Intent(this,CreateAccountActivity::class.java)
        startActivity(intent)
    }

}