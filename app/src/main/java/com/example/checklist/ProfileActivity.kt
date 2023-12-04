package com.example.checklist

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch


class ProfileActivity: AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var database: Database
    private lateinit var imageButton: ImageView
    private lateinit var username: String

    private lateinit var imageButtonPass: ImageView

    private lateinit var userGroups: MutableLiveData<ArrayList<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)
        val gridView = findViewById<GridView>(R.id.gridView)
        username = getSharedPreferences("Checklist", MODE_PRIVATE).getString("username","empty").toString()

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.group -> {
                    startActivity(Intent(this,NewGroupActivity::class.java))
                    true
                }
                R.id.home -> {
                    startActivity(Intent(this, GroupViewActivity::class.java))
                    true
                }
                R.id.profile -> {
                    // Already in Home, do nothing or refresh if needed
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.profile
        findViewById<Button>(R.id.logoutButton).setOnClickListener { logout() }
        database = Database()
        imageButton = findViewById(R.id.imgButton)
        imageButtonPass = findViewById(R.id.imgButtonPass)
        userGroups = MutableLiveData()
//        get username from shared Preference
        val username = getSharedPreferences("Checklist", MODE_PRIVATE).getString("username","empty").toString()

//        set text of editext to username
        val usernameEditText = findViewById<EditText>(R.id.nameText)
        val passwordEditText = findViewById<EditText>(R.id.Password)
        usernameEditText.setText(username)

        lifecycleScope.launch {
            val retrievedGroups = database.getUserAssociatedGroups (username)

            retrievedGroups.observe(this@ProfileActivity) { groups ->
                groups?.let {
                    userGroups.value = it
                    val items = userGroups.value ?: emptyList() // Get the list directly
                    val adapter =
                        ArrayAdapter(this@ProfileActivity, android.R.layout.simple_list_item_1, items)
                    gridView.adapter = adapter
                }
            }
        }

        imageButton.setOnClickListener {
            if(username.equals(usernameEditText.text.toString())){
                Toast.makeText(this, "Error: Username is the same", Toast.LENGTH_LONG).show()
            }
            else
            {
                database.checkUniqueUsername(usernameEditText.text.toString()) { isUnique ->
                    if (isUnique) {
                        //            call database update username from a coroutine
                        lifecycleScope.launch {
//                update username in Database
                            database.updateUsername(username,usernameEditText.text.toString())
//               update shared preference
                            getSharedPreferences("Checklist", MODE_PRIVATE).edit().putString("username",usernameEditText.text.toString()).apply()
                        }
                        Toast.makeText(this, "Username Updated!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Error: Username Already Exists", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        imageButtonPass.setOnClickListener {
//            get username from sharedpref
            val username = getSharedPreferences("Checklist", MODE_PRIVATE).getString("username","empty").toString()
            if(passwordEditText.text.toString().isEmpty()){
                Toast.makeText(this, "Error: Password is Blank", Toast.LENGTH_LONG).show()
            }
            else
            {
                lifecycleScope.launch {
                    //                update password in Database
                    database.updatePassword(username,passwordEditText.text.toString())
                }
//                empty the edittext
                passwordEditText.setText("")
                Toast.makeText(this, "Password Updated!", Toast.LENGTH_SHORT).show()
            }

        }

//        //        set adapter to gridview
//        val items = arrayOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6")
//
//        // Create ArrayAdapter to populate the GridView
//
//        // Create ArrayAdapter to populate the GridView
//        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userGroups)
//        gridView.adapter = adapter

            }
    private fun logout(){
        getSharedPreferences("Checklist", MODE_PRIVATE).edit().putBoolean("loggedIn",false).apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

    }
        }






