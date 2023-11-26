package com.example.checklist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ItemDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap:GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        val itemName = intent.getStringExtra("itemName")
        val itemUser = intent.getStringExtra("itemUser")
        val itemLocation = intent.getParcelableExtra<LatLng>("itemLocation")

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


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val itemLocation = intent.getParcelableExtra<LatLng>("itemLocation")
        Log.d("ItemDetailsActivity","$itemLocation")
        itemLocation?.let {
            mMap.addMarker(MarkerOptions().position(it).title("Item Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(it))
        }
    }
}