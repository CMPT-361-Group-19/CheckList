package com.example.checklist.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checklist.Database
import com.example.checklist.SelectedPlace
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class ChecklistViewModel(): ViewModel() {
    private val tag = "ChecklistViewModel"
    private val database = Database()
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
}

data class ChecklistItem(val item: String, var isChecked: String, val username: String, val selectedPlace: SelectedPlace? = null)