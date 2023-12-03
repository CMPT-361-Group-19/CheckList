package com.example.checklist

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.checklist.ui.home.GridviewAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch


class GroupViewActivity : AppCompatActivity() {

    private lateinit var database: Database
    private lateinit var username: String
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isLoggedIn = getSharedPreferences("Checklist", MODE_PRIVATE).getBoolean("loggedIn",false)
        if(isLoggedIn){
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }


        setContentView(R.layout.activity_group_view)

        checkServicePermissions()
        database = Database()


        username = getSharedPreferences("Checklist", MODE_PRIVATE).getString("username","empty").toString()
        //Navigating between Activities
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.group -> {
                    val intent = Intent(this,NewGroupActivity::class.java)
                    intent.putExtra("user", username)
                    startActivity(intent)
                    true
                }
                R.id.home -> {
                    // Already in Home, do nothing or refresh if needed
                    true
                }
                else -> false
            }
        }
        findViewById<Button>(R.id.logoutButton).setOnClickListener { logout() }
    }

    private fun updateList(groupList : ArrayList<String>) {
        val gridView = findViewById<GridView>(R.id.gridView)

        var numGroups = groupList.size

        val adapter = GridviewAdapter(this, groupList)
        gridView.adapter = adapter
        var textView = findViewById<TextView>(R.id.textMyGroups)
        textView.text = "Welcome ${username}!"
        textView = findViewById<TextView>(R.id.subtextMyGroups)
        textView.text = "You have ${numGroups.toString()} groups."

        Log.d("groups:", groupList.toString())

        gridView.setOnItemClickListener{_, _, pos, id ->
            Log.d("debug:", pos.toString())
            Log.d("debug:", id.toString())
            val intent = Intent(this, ChecklistActivity::class.java)
            intent.putExtra("group identifier", groupList[pos])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d("debug", "OnResume")

        lifecycleScope.launch {

            val groupList = database.getGroupsWithUser(username)
            kotlinx.coroutines.delay(400)
            processRestOfPage(groupList)

            database.getGroupsWithUser(username)
        }

        database.groupList.observe(this) {
            updateList(it)

        }

    }


    private fun checkLocationPermissions(): Boolean {
        Log.d("going","going")
        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            Log.d("location", "requesting permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                0
            )
            return false
        }
        return true
    }


    private fun checkServicePermissions(){
        checkLocationPermissions()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED){
            Log.d("going", "requesting permission2")
            askPermissionForBackgroundUsage()
        }
    }

    private fun askPermissionForBackgroundUsage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Needed!")
                .setMessage("Background Location Permission Needed!, tap \"Allow all time in the next screen\" then press Back")
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this, arrayOf<String>(
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ), 2
                        )
                    })
                .setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, which ->
                    // User declined for Background Location Permission.
                })
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                2
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted for Background Location Permission.
                checkAllPermissions()
            } else {
                // User declined for Background Location Permission.
            }
        }
    }

    private fun checkAllPermissions() {
        Log.d("going","going2")

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION
        )
        for (permission in permissions){
            if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                return
            }
        }
        Log.d("going","going3")
        val serviceIntent = Intent(this, LocationService::class.java)
        startForegroundService(serviceIntent)
    }


    private fun logout(){
        getSharedPreferences("Checklist", MODE_PRIVATE).edit().putBoolean("loggedIn",false)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

    }


}