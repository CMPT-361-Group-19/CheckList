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
        checkPermissions()
        checkLocationPermissions()
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

    private val REQUEST_CODE_LOCATION_PERMISSION = 123 // Unique request code

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION
        )

        val permissionsToRequest = mutableListOf<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE_LOCATION_PERMISSION
            )
        } else {
            // All permissions already granted, proceed with functionality

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // All permissions granted, proceed with using location-related features

                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.


                } else {
                    // Permission denied, handle accordingly (show a message, modify functionality, etc.)
                }
            }
            else -> {
                //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
    //private val REQUEST_CODE_LOCATION_PERMISSION = 125

    private fun checkLocationPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                REQUEST_CODE_LOCATION_PERMISSION
            )
        } else {
            // Permissions already granted, proceed with location-related functionality
            // Start your service here or perform location-related operations
        }
    }


}