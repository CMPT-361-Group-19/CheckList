package com.example.checklist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checklist.Database
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddItemViewModel(): ViewModel() {
    private val tag = "AddItemViewModel"
    private val database = Database()


    fun saveTask(groupId: String, checklistItem: ChecklistItem)
    {
        viewModelScope.launch {
            Log.d(tag,"look in view model save")
            database.addGroupItems(groupId,checklistItem)
        }
    }
}