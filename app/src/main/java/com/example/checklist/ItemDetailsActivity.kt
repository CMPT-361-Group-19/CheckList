package com.example.checklist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.checklist.viewmodel.ChecklistViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ItemDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap:GoogleMap
    private var itemLocation: LatLng? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        val itemName = intent.getStringExtra("itemName")
        val itemUser = intent.getStringExtra("itemUser")
        itemLocation = intent.getParcelableExtra<LatLng>("itemLocation")

        val itemNameText = findViewById<TextView>(R.id.item_detail)
        val itemUserText = findViewById<TextView>(R.id.item_user)

        itemNameText.text = itemName
        itemUserText.text = "Added by: $itemUser"

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val closeButton = findViewById<Button>(R.id.close_button)
        closeButton.setOnClickListener {
            finish()
        }

        getItemDetails()


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Log.d("ItemDetailsActivity","$itemLocation")
        itemLocation?.let {
            mMap.addMarker(MarkerOptions().position(it).title("Item Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(it))
        }
    }


    private fun getItemDetails() {
        val viewModel = ViewModelProvider(this)[ChecklistViewModel::class.java]
        val groupId = "your_group_id"
        val itemName = intent.getStringExtra("itemName") ?: ""
        val username = "your_username"

        viewModel.getItemDetails(groupId, itemName, username)
        viewModel.itemDetails.observe(this) { itemDetails ->
            itemLocation = itemDetails.selectedPlace?.location
            itemLocation?.let{
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(it).title("Item Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(it))
            }
        }
    }
}