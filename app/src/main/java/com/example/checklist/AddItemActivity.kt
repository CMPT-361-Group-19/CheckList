package com.example.checklist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.checklist.viewmodel.AddItemViewModel
import com.example.checklist.viewmodel.ChecklistItem
import com.example.checklist.viewmodel.ChecklistViewModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class AddItemActivity : AppCompatActivity() {
    private val tag = "AddItemActivity"
    private lateinit var viewModel: AddItemViewModel
    private var groupIdentifier: String = "Bakers"
    private val selectedPlace: SelectedPlace = SelectedPlace()
    private lateinit var username: String
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        username = getSharedPreferences("Checklist", MODE_PRIVATE).getString("username", "empty")
            .toString()
        viewModel = ViewModelProvider(this)[AddItemViewModel::class.java]

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        //Navigation to other activities
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

        groupIdentifier = intent.getStringExtra("groupId").toString()


        val apiKey = getString(R.string.apiKey)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }


        val placesAPIFragment =
            supportFragmentManager.findFragmentById(R.id.placesAPI) as AutocompleteSupportFragment

        onLocationSelected(placesAPIFragment)

        findViewById<Button>(R.id.saveTaskButton).setOnClickListener {
            saveTaskToDB()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.action_back).isVisible = true
        return true
    }

    private fun onLocationSelected(placesAPIFragment: AutocompleteSupportFragment) {
        placesAPIFragment.setPlaceFields(
            listOf(
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.PHONE_NUMBER,
                Place.Field.LAT_LNG,
                Place.Field.OPENING_HOURS,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL
            )
        )

        placesAPIFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                val name = place.name
                val address = place.address
                val phone = place.phoneNumber.toString()
                val latlng = place.latLng
                val latitude = latlng?.latitude
                val longitude = latlng?.longitude
                val rating = place.rating
                val userRatings = place.userRatingsTotal

                selectedPlace.location = latlng
                selectedPlace.address = address
                selectedPlace.name = name
                selectedPlace.rating = rating
                selectedPlace.userRatings = userRatings
            }

            override fun onError(p0: Status) {
                Log.d(tag, "look $p0")
                Toast.makeText(applicationContext, "Some error occurred", Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun saveTaskToDB() {
        Log.d(tag, "look in save task")
        val itemName = findViewById<TextView>(R.id.itemText).text.toString()
        val checkListItem = ChecklistItem(itemName, false.toString(), username, selectedPlace)
        if (itemName.isNullOrBlank() || selectedPlace.location == null) {
            Toast.makeText(this, "Fields cannot be null", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.saveTask(groupIdentifier, checkListItem)
            finish()

        }
    }
}

data class SelectedPlace(var name: String? = null, var location: LatLng? = null, var address: String? = null, var rating: Double? = null, var userRatings: Int? = null)
