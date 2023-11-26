package com.example.checklist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
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
    private var itemUser = ""
    private var itemName = ""
    private var groupId = ""
    private var itemAdder: MutableLiveData<String> = MutableLiveData("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        itemName = intent.getStringExtra("itemName").toString()
        groupId = intent.getStringExtra("groupId").toString()
        itemUser = intent.getStringExtra("itemUser").toString()
        itemLocation = intent.getParcelableExtra<LatLng>("itemLocation")

        val itemNameText = findViewById<TextView>(R.id.item_detail)
        val itemUserText = findViewById<TextView>(R.id.item_user)

        itemNameText.text = itemName
        itemUserText.text = "Added by: $itemAdder"

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val closeButton = findViewById<Button>(R.id.close_button)
        closeButton.setOnClickListener {
            finish()
        }
        itemAdder.observe(this){
            itemUserText.text = "Added by: ${itemAdder.value}"
        }



    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        Log.d("ItemDetailsActivity","$itemLocation")
        itemLocation?.let {
            mMap.addMarker(MarkerOptions().position(it).title("Item Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(it))
        }
        getItemDetails()
    }


    private fun getItemDetails() {
        val viewModel = ViewModelProvider(this)[ChecklistViewModel::class.java]
        Log.d("inside here", "calling with this $groupId $itemName $itemUser")
        viewModel.getItemDetails(groupId, itemName, itemUser)
        viewModel.itemDetails.observe(this) { itemDetails ->
            Log.d("inside here", "observe triggered")
            itemLocation = itemDetails.selectedPlace?.location
            itemAdder.value = itemDetails.username
            itemLocation?.let{
                Log.d("inside here", "location is $itemLocation")
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(it).title("Item Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(it))
            }
        }
    }
}