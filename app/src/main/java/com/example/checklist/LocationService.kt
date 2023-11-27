package com.example.checklist

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.Service.START_NOT_STICKY
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.audiofx.BassBoost
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.ContactsContract.Data
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class LocationService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var userItemList: ArrayList<ArrayList<String>>? = null
    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var database:Database = Database()
        val username = getSharedPreferences("Checklist", MODE_PRIVATE).getString("username","empty").toString()
        // Start a coroutine in the service
        serviceScope.launch {
            // Coroutine code here
            withContext(Dispatchers.IO) {
                //background tasks
            }
            withContext(Dispatchers.Main) {
                //operations requiring the main thread

                //get list of groups that user participates in
                var groupList = database.getUserGroupList(username)

                //get list of items of each group that user is in
                //?????
                userItemList = database.getUserItemList(username,groupList)
                //Log.i("locationlog","${groupList.get(0)}")
            }
        }
        startForegroundService()
    }

    override fun onLocationChanged(location: Location) {
        // Handle location updates here
        var lat = location.latitude;
        var long = location.longitude
        //check grouplist of user and for each group check each tasks location if any
        Log.i("locationlog","lat: $lat, long: $long")

        //if task location nearby
        val taskLocationNearby:Boolean = true
        if (taskLocationNearby) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            val builder = NotificationCompat.Builder(this, "notif")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("group Y")
                .setContentText("Task item X is nearby")
                .build()

            val channel = NotificationChannel(
                "notif",
                "Task proximity Alert",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return START_NOT_STICKY
    }


    private fun startForegroundService() {
        val channelId = "LocationServiceChannel"
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Service")
            .setContentText("Tracking location")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        createNotificationChannel(channelId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            // Proceed with starting foreground service
            startForeground(1234, notification)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this.baseContext as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0f,this)

    }

    private fun createNotificationChannel(channelId: String) {
        val channel = NotificationChannel(
            channelId,
            "Location Service Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }



    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        // Stop location updates and clean up resources
        super.onDestroy()
    }


}
