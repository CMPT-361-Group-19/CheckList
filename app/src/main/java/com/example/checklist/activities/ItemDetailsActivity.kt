package com.example.checklist.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.checklist.R
import com.example.checklist.viewmodel.ChecklistViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class ItemDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap:GoogleMap
    private var itemLocation: LatLng? = null
    private var itemUser = ""
    private var itemName = ""
    private var groupId = ""
    private var itemAdder: MutableLiveData<String> = MutableLiveData("")
    private var itemComments: MutableLiveData<String> = MutableLiveData("No comments")
    private var itemDate: MutableLiveData<String> = MutableLiveData("No date set")
    private lateinit var googleMapsButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        itemName = intent.getStringExtra("itemName").toString()
        groupId = intent.getStringExtra("groupId").toString()
        itemUser = intent.getStringExtra("itemUser").toString()
        itemLocation = intent.getParcelableExtra<LatLng>("itemLocation")

        val itemNameText = findViewById<TextView>(R.id.item_detail)
        val itemUserText = findViewById<TextView>(R.id.item_user)
        val itemCommentText = findViewById<TextView>(R.id.item_comments)
        val itemDateText = findViewById<TextView>(R.id.item_date)

        itemNameText.text = itemName
        itemUserText.text = "Added by: $itemAdder"

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val closeButton = findViewById<Button>(R.id.close_button)
        closeButton.setOnClickListener {
            finish()
        }

        googleMapsButton = findViewById(R.id.googleMaps)
        googleMapsButton.setOnClickListener {
            val gmmIntentUri = Uri.parse("google.navigation:q=${itemLocation?.latitude},${itemLocation?.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            startActivity(mapIntent)
        }

        itemAdder.observe(this){
            Log.d("i am","inside the observe")
            itemUserText.text = "Added by: ${itemAdder.value}"
        }
        itemComments.observe(this){
            Log.d("i am","inside the observe")
            itemCommentText.text = "Comments: ${it}"
        }
        itemDate.observe(this){
            Log.d("i am","inside the observe")
            itemDateText.text = "Completion date: ${it}"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Log.d("ItemDetailsActivity","$itemLocation")
        itemLocation?.let {
            mMap.addMarker(MarkerOptions().position(it).title("Item Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15.0f))
            googleMapsButton.isEnabled = true
        }
        getItemDetails()

    }


    private fun getItemDetails() {
        val viewModel = ViewModelProvider(this)[ChecklistViewModel::class.java]
        Log.d("inside here", "calling with this $groupId $itemName $itemUser")
        viewModel.getItemDetails(groupId, itemName)
        viewModel.itemDetails.observe(this) { itemDetails ->
            Log.d("inside here", "observe triggered")
            itemLocation = itemDetails.selectedPlace?.location
            itemAdder.value = itemDetails.username
            itemDetails.comments?.let { itemComments.value = it }
            itemDetails.completionDate?.let { itemDate.value = it }
            itemLocation?.let{
                Log.d("inside here", "location is $itemLocation ${itemAdder.value}")
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(it).title("Item Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15.0f))
            }
        }
    }
}