package com.example.checklist.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checklist.Database
import com.example.checklist.SelectedPlace
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class ChecklistViewModel(): ViewModel() {
    private val tag = "ChecklistViewModel"
    private val database = Database()
    private val _itemDetails = MutableLiveData<ChecklistItem>()
    val itemDetails: LiveData<ChecklistItem> get() = _itemDetails
    val groupItems get() = database.groupItems

    fun getGroupItems(groupId: String) {
        Log.d(tag, "look inside get groups viewmodel")
        viewModelScope.launch {
            Log.d(tag, "look inside get groups viewmodel2")
            database.attachGroupItemListener(groupId)
        }
    }


    fun addGroupItems(groupId: String, checklistItem: ChecklistItem) {
        Log.d(tag, "look inside add group item viewmodel")
        viewModelScope.launch {
            database.addGroupItems(groupId, checklistItem)
            Log.d(tag, "look inside get groups viewmodel2 ${groupItems.value}")
        }
    }

    fun changeItemStatus(groupId: String, item: String, username: String, isChecked: Boolean) {
        viewModelScope.launch {
            database.changeItemStatus(groupId, item, username, isChecked)
        }
    }

    fun deleteItemIfValid(groupId: String, item: String, username: String){
        viewModelScope.launch {
            if(database.deleteItemIfValidUser(groupId,item,username)){
//                Toast.makeText(this@ChecklistViewModel,"Item Deleted",Toast.LENGTH_SHORT).show()
                Log.d(tag, "deleted")
                }
            else {
                Log.d(tag, "not deleted")

            }
        }
    }

    fun getItemDetails(groupId: String, itemName: String, username: String){
        viewModelScope.launch{
            try{
                Log.d("inside here","inside here")
                val itemDetails = database.getGroupItemDetails(groupId, itemName, username)
                Log.d("inside here", "in vm selectedPlace: ${itemDetails?.selectedPlace}")
//                _itemDetails.postValue(ChecklistItem(itemName, itemDetails?.isChecked ?: "",username, itemDetails?.selectedPlace))
                _itemDetails.value = itemDetails
                Log.d("inside here", "in vm ${_itemDetails.value}}")

            } catch(e: Exception){
                Log.d(tag, "error getting item details")}
        }
    }
}

data class ChecklistItem(val item: String, var isChecked: String, val username: String, val selectedPlace: SelectedPlace? = null)