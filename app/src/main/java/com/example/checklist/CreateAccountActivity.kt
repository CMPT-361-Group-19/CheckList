package com.example.checklist

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CreateAccountActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        // If Submit button is clicked
        findViewById<Button>(R.id.createAccountSubmitButton).setOnClickListener {
            val name : String = findViewById<EditText>(R.id.create_username).text.toString()
            val pswrd : String = findViewById<EditText>(R.id.create_password).text.toString()
            createAccount(name, pswrd)
        }

        // Goes back to Sign In Page
        findViewById<TextView>(R.id.signInText).setOnClickListener{signInTextClicked()}
    }

    private fun createAccount(username : String, password : String) {

        // Check if Username or Password is Blank
        if (username.isEmpty()) {
            Toast.makeText(this, "Error: Username is Blank", Toast.LENGTH_LONG).show()
            return
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Error: Password is Blank", Toast.LENGTH_LONG).show()
            return
        }

        // Check if Username already exists in database
        val database = Database()
        database.checkUniqueUsername(username) { isUnique ->
            if (isUnique) {
                database.writeNewAccount(username, password)
                Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error: Username Already Exists", Toast.LENGTH_LONG).show()
            }
        }
        return
    }

    private fun signInTextClicked() {
        finish()
    }

}