package com.example.checklist

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.checklist.activities.GroupViewActivity
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
    private var itemFlag: String? = null
    private lateinit var notificationManager: NotificationManager
    private var isServiceRunning = false


    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var database = Database()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "notif",
            "Task proximity Alert",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {  }
        notificationManager.createNotificationChannel(channel)
        val username = getSharedPreferences("Checklist", MODE_PRIVATE).getString("username","empty").toString()
        // Start a coroutine in the service
        serviceScope.launch {
            withContext(Dispatchers.IO) {
                //operations requiring the main thread

                //get list of groups that user participates in
                var groupList = database.getUserGroupList(username)

                //get list of items of each group that user is in
                userItemList = database.getUserItemList(username,groupList)
//                Log.i("itemslist","${userItemList!!.get(0).get(1)}")
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

        if (userItemList != null) {

            Log.d("flag","nullled $userItemList")
            for (item in userItemList!!) {
            //if task item location near current location
                Log.d("flag","nullled2")
            if (kotlin.math.abs(item[2].toDouble() - long) <= 0.02 ) {
                if (kotlin.math.abs(item[3].toDouble() - lat) <= 0.02 ) {
                    //create notification
                    // Create an explicit intent for an Activity in your app.
                    val intent = Intent(this, GroupViewActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                    val builder = NotificationCompat.Builder(baseContext, "notif")
                        .setSmallIcon(R.drawable.group_svgrepo_com)
                        .setContentTitle("Group: ${item[0]}")
                        .setContentText("Task item ${item[1]} is nearby")
                        .setContentIntent(pendingIntent)
                        //.setAutoCancel(true)

                    with(NotificationManagerCompat.from(this)) {
                        // notificationId is a unique int for each notification that you must define.
                        if (ActivityCompat.checkSelfPermission(
                                 baseContext,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        if (itemFlag != item[1]) {
                            notify(1234, builder.build())
                            // Check if the device supports vibration
                            val vibrator: Vibrator? = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

                            // Check if the vibrator service is available
                            if (vibrator?.hasVibrator() == true) {
                                // Vibrate for 500 milliseconds
                                vibrator.vibrate(500)
                        }

                    }
                        itemFlag = item[1]


                    }


                }


            }
        }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_ACTION") {
            // Stop or cancel the service when the stop action is clicked
            //stopForeground(true)
            isServiceRunning = false
            stopSelf()
        }
        if(isServiceRunning){
            return START_NOT_STICKY
        }
        startForegroundService()
        isServiceRunning = true
        return START_NOT_STICKY
    }


    private fun startForegroundService() {
        val stopIntent = Intent(this, LocationService::class.java)
        stopIntent.action = "STOP_ACTION"
        val pendingStopIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val channelId = "LocationServiceChannel"
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Service")
            .setContentText("Tracking location")
            .setSmallIcon(R.drawable.group_svgrepo_com)
            .addAction(R.drawable.lock, "Stop Service", pendingStopIntent)
            .build()

        createNotificationChannel(channelId)
        if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.FOREGROUND_SERVICE
        ) != PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            // Proceed with starting foreground service
            startForeground(1234, notification)
        }


            // Permissions are granted, request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2f, this)

        //checkPermissions()
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
