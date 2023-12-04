package com.example.checklist

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.checklist.viewmodel.AddItemViewModel
import com.example.checklist.viewmodel.ChecklistItem
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDate
import java.util.Calendar


class AddItemActivity : AppCompatActivity() {
    private val tag = "AddItemActivity"
    private lateinit var viewModel: AddItemViewModel
    private var groupIdentifier: String = "Bakers"
    private val selectedPlace: SelectedPlace = SelectedPlace()
    private lateinit var username: String
    private lateinit var bottomNavigationView: BottomNavigationView
    private var dateString = ""
    private lateinit var date: LocalDate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        username = getSharedPreferences("Checklist", MODE_PRIVATE).getString("username", "empty")
            .toString()
        viewModel = ViewModelProvider(this)[AddItemViewModel::class.java]

        date = LocalDate.now()

//        supportActionBar?.setDisplayShowTitleEnabled(false)
//        supportActionBar?.setDisplayShowHomeEnabled(false)
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
        findViewById<RadioButton>(R.id.today).setOnClickListener {
            date = LocalDate.now()
            dateString = date.toString()
        }
        findViewById<RadioButton>(R.id.tomorrow).setOnClickListener {
            date = LocalDate.now().plusDays(1)
            dateString = date.toString()
        }
        findViewById<RadioButton>(R.id.calendarImageView).setOnClickListener {
            onCalendarClick()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.action_cancel).isVisible = true
        menu.findItem(R.id.action_save).isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cancel ->             // Handle the back action
                true

            R.id.action_save ->             // Handle the save action
                true

            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
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
        Log.d(tag, "look in save task $date")
        val itemName = findViewById<TextView>(R.id.itemText).text.toString()
        val comments = findViewById<EditText>(R.id.notesText).text.toString()
        val checkListItem = ChecklistItem(itemName, false.toString(), username, selectedPlace, dateString, comments)
        if (itemName.isNullOrBlank() || selectedPlace.location == null) {
            Toast.makeText(this, "Fields cannot be null", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.saveTask(groupIdentifier, checkListItem)
            finish()

        }
    }

    private fun onCalendarClick(){
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH)
        val dayOfMonth: Int = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog and show it
        val datePickerDialog = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                date = LocalDate.of(year, monthOfYear,dayOfMonth)
                dateString = date.toString()
            },
            year,
            month,
            dayOfMonth
        )

        // Show the DatePickerDialog
        datePickerDialog.show()
    }
}

data class SelectedPlace(var name: String? = "", var location: LatLng? = LatLng(0.0,0.0), var address: String? = "", var rating: Double? = null, var userRatings: Int? = null)
