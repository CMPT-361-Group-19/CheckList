package com.example.checklist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.checklist.viewmodel.ChecklistItem
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.database
import com.google.firebase.database.values
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Database {
    private lateinit var database : DatabaseReference
    private val _groupItems = MutableLiveData<ArrayList<ChecklistItem>>().apply {
        value = ArrayList()
    }
    val groupItems get() = _groupItems

    private val _groupList = MutableLiveData<ArrayList<String>>().apply {
        value = ArrayList()
    }
    val groupList get() = _groupList

    private val groupItemListener = object: ChildEventListener{
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val list = _groupItems.value
            val task = snapshot.key.toString()
            val isChecked = snapshot.child("checked").value
            val username = snapshot.child("username").value
            val checklistItem = ChecklistItem(task,isChecked.toString(),username.toString())
            Log.d("look", "onChildAdded before: $list")
            list?.add(checklistItem)
            Log.d("look", "onChildAdded after: $list")
            _groupItems.value = list
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val list = _groupItems.value
            val task = snapshot.key.toString()
            val isChecked = snapshot.child("checked").value
            val username = snapshot.child("username").value
            val checklistItem = ChecklistItem(task,isChecked.toString(),username.toString())
            Log.d("look", "onChildChangedBefore: $list")
            val foundItem = list?.find { it.item == checklistItem.item }
            foundItem?.let{it.isChecked = checklistItem.isChecked}
            Log.d("look", "onChildChanged after: $list")
            _groupItems.value = list
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            val list = _groupItems.value
            Log.d("look", "onChildRemoved before: $list")
            val task = snapshot.key.toString()
            val isChecked = snapshot.child("checked").value
            val username = snapshot.child("username").value
            val checklistItem = ChecklistItem(task,isChecked.toString(),username.toString())
            list?.remove(checklistItem)
            Log.d("look", "onChildRemoved after: $list")
            _groupItems.value = list
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d("look", "loadPost:onCancelled", error.toException())

        }

    }


    @IgnoreExtraProperties
    data class Account(val username : String? = null, val password : String?) {
        // Secondary constructor for Firebase deserialization
        constructor() : this("", "")
    }

    //write account to database
    fun writeNewAccount(username: String, password: String) {
        val account = Account(username,password)
        database = Firebase.database.reference
        database.child("accounts").child(username).child("username").setValue(username)
        database.child("accounts").child(username).child("password").setValue(password)
    }

    //read from database
    suspend fun getAccount(username: String, password: String) : Account? {
        database = Firebase.database.reference
        val dataSnapshot = database.child("accounts").get().await()

        for (snapshot in dataSnapshot.children) {
            val account = snapshot.getValue(Account::class.java)
            if (account != null) {
                if (account.username == username) {
                    return account
                }
            }
        }
        return null // Account not found
    }

    fun validSignInCredentials(username: String, password: String,callback: (Boolean) -> Unit) {
        var data: Account = Account(username,password)
        database = Firebase.database.reference
        database.child("accounts").child(username).child("password").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value} $password")
            callback(it.value == password)

        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
            callback(false)

        }
        Log.d("inside", "returning false")
    }

    fun checkUniqueUsername(username : String, callback: (Boolean) -> Unit) {
        database = Firebase.database.reference

        database.child("accounts").child(username).get().addOnSuccessListener() {
            Log.i("insdie:", "Got value ${it.value.toString()}")
            callback(it.value == null)
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
    }

    data class Group(var groupId: String? = null, var desc: String? = null)

    //write group to database
    fun writeNewGroup(groupId: String, desc: String) {
        val group = Group(groupId,desc)
        database = Firebase.database.reference
        val groupRef = database.child("groups").child(groupId).setValue(group)

    }


    suspend fun getGroup(groupName: String): Group? {
        database = Firebase.database.reference
        val dataSnapshot = database.child("groups").get().await()

        for (snapshot in dataSnapshot.children) {
            val group = snapshot.getValue(Group::class.java)
            if (group?.groupId == groupName) {

                return group
            }
        }
        return null // Group not found
    }



    //return the list of groups in the database
    suspend fun getGroupList() : ArrayList<Group> {
        var groupList:ArrayList<Group> = ArrayList<Group>()
        database = Firebase.database.reference
        val dataSnapshot = database.child("groups").get().await()
        for (snapshot in dataSnapshot.children) {
            val group = snapshot.getValue(Group::class.java)
            if (group != null) {
                groupList.add(group)
            }
        }
        return groupList

    }


    //writes a user into a group's participant list
    fun writeGroupParticipant(groupId: String? = null, username: String? = null) {
        database = Firebase.database.reference

        val group = groupId?.let {
            if (username != null) {
                database.child("groups").child(it).child("participants").child(username).setValue(username)
            }
        }

    }

    fun getGroupsWithUser(user: String) {

        database = Firebase.database.reference

        var myGroupList = ArrayList<String>()

        database.child("groups").get().addOnSuccessListener() {
            for (thing in it.children) {
                if (thing.child("participants").hasChild(user)) {
                    Log.d("groupID:", "${thing.getValue(Group::class.java)!!.groupId}")
                    myGroupList.add(thing.getValue(Group::class.java)!!.groupId!!)
                }
            }
            groupList.value = myGroupList
        }.addOnFailureListener{
            Log.e("firebase", "Error getting group data", it)
        }
    }

    //returns an arraylist of Strings that contains usernames of the participants of a particular group
    suspend fun getGroupParticipants(groupId: String) : ArrayList<String> {
        var participantList: ArrayList<String> = ArrayList<String>()
        database = Firebase.database.reference
        val dataSnapshot = database.child("groups").child(groupId).child("participants").get().await()
        for (snapshot in dataSnapshot.children) {
            val username = snapshot.getValue(String::class.java)
            if (username != null) {
                participantList.add(username)
            }
        }
        return participantList
    }

    suspend fun addGroupItems(groupId: String, checklistItem: ChecklistItem) {
        withContext(Dispatchers.IO) {
            database = Firebase.database.reference
                database.child("groups").child(groupId).child("items").child(checklistItem.item)
                    .setValue(checklistItem)
        }
    }


    fun attachGroupItemListener(groupId: String){
        database = Firebase.database.reference
        database.child("groups").child(groupId).child("items").addChildEventListener(groupItemListener)

    }

    fun changeItemStatus(groupId: String, item: String, username: String, isChecked:Boolean) {
        database = Firebase.database.reference
        database.child("groups").child(groupId).child("items").child(item).child("checked").setValue(isChecked)

    }

    suspend fun deleteItemIfValidUser(groupId: String, item: String, username: String): Boolean{
        database = Firebase.database.reference
        val snapShot = database.child("groups").child(groupId).child("items").child(item).child("username").get().await()
        if(snapShot.value.toString() == username){
            Log.d("look", "removing val $item $username")
            snapShot.ref.parent?.removeValue()
            return true
        }
        Log.d("look", "removed val $item $username")
        return false
    }

    // extracting details about a certain item added to checklist.
    suspend fun getGroupItemDetails(groupId: String, itemName: String, username: String): ChecklistItem {
        database = Firebase.database.reference
        val dataSnapshot = database.child("groups").child(groupId).child("items").child(itemName).get().await()
        Log.d("inside here","inside getGroupItems ${dataSnapshot.value}")
        val adderName = dataSnapshot.child("username").value.toString()
        val lat = dataSnapshot.child("selectedPlace").child("location").child("latitude").value.toString()
        val long = dataSnapshot.child("selectedPlace").child("location").child("longitude").value.toString()

//        val item = dataSnapshot.getValue(ChecklistItem::class.java)
        val item = ChecklistItem(itemName, isChecked = "false",username=adderName, selectedPlace =SelectedPlace(location = LatLng(lat.toDouble(),long.toDouble())))
        Log.d("inside here", "in the db $item")
        return item
    }

    suspend fun getUserGroupList(username: String): ArrayList<String>{
        database = Firebase.database.reference
        var groupList: ArrayList<String> = ArrayList()
        val dataSnapshot = database.child("accounts").child(username).child("groupList").get().await()
        for (snapshot in dataSnapshot.children) {
            val item = snapshot.getValue(String::class.java)
            if (item != null) {
                groupList.add(item)
            }
        }

        return groupList
    }

    suspend fun getUserAssociatedGroups(username: String): MutableLiveData<ArrayList<String>>{
        database = Firebase.database.reference
        val groupList: MutableLiveData<ArrayList<String>> = MutableLiveData()
        val tempList = arrayListOf<String>()

        // get all the groups from groups, then go to path participants, then check if username is in there
        val dataSnapshot = database.child("groups").get().await()
        for (snapshot in dataSnapshot.children) {
            val item = snapshot.getValue(Group::class.java)
            if (item != null) {
                if (item.groupId != null) {
                    if (snapshot.child("participants").hasChild(username)) {
                        tempList.add(item.groupId!!)
                    }
                }
            }
        }
        println()
        groupList.postValue(tempList)
        return groupList
    }

    //data class ItemList(var groupId: String? = null,var itemName: String? = null, var lat: Double? = null, var long: Double? = null)
    suspend fun getUserItemList(username: String, groupList:ArrayList<String>): ArrayList<ArrayList<String>>{
        database = Firebase.database.reference
        //val groupList = getUserGroupList(username)
        var itemList: ArrayList<ArrayList<String>> = ArrayList()
        for (group in groupList) {
            val dataSnapshot = database.child("groups").child(group).child("items").get().await()
            for (snapshot in dataSnapshot.children) {
                //snapshot.getValue<ChecklistItem>()
                //snapshot.childrenCount
                Log.i("loca", snapshot.key.toString())
                //snapshot.key.toString()
                var groups = ArrayList<String>()
                //add item name
                groups.add(group)
                groups.add(snapshot.key.toString())
                val long = database.child("groups").child(group).child("items")
                    .child(snapshot.key.toString()).child("selectedPlace")
                    .child("location").child("longitude").get().await()

                val lat = database.child("groups").child(group).child("items")
                    .child(snapshot.key.toString()).child("selectedPlace")
                    .child("location").child("latitude").get().await()
                groups.add(2, long.getValue(Double::class.java).toString())
                groups.add(3,lat.getValue(Double::class.java).toString())

                itemList.add(groups)
            }
        }


        return itemList
    }

    suspend fun removeUserFromGroup(username: String, groupId: String){
        withContext(Dispatchers.IO) {
            database = Firebase.database.reference
            database.child("groups").child(groupId).child("participants").child(username)
                .removeValue()
        }
    }

//    update username
    suspend fun updateUsername(oldUsername: String, newUsername: String) {
    withContext(Dispatchers.IO) {
        database = Firebase.database.reference

//        Update on Accounts level
        // Step 1: Copy data from old key to new key
        database.child("accounts").child(oldUsername).get()
            .addOnSuccessListener { oldDataSnapshot ->
                if (oldDataSnapshot.exists()) {
                    // Get the data from the old key
                    val userData = oldDataSnapshot.getValue()
//                    update username in userData to newUsername
                    // Create a new node with the new key and set the data
                    database.child("accounts").child(newUsername).setValue(userData)
                        .addOnSuccessListener {
                            database.child("accounts").child(newUsername).child("username")
                                .setValue(newUsername)
                            // Step 2: After successfully copying, remove the old key
                            database.child("accounts").child(oldUsername).removeValue()
                                .addOnSuccessListener {
                                    // Old key removed, update complete
                                    // Handle success or any additional operations
                                }
                                .addOnFailureListener { /* Handle failure */ }
                        }
                        .addOnFailureListener { /* Handle failure */ }
                } else {
                    // Handle if the old key doesn't exist
                }
            }.addOnFailureListener { /* Handle failure */ }

//        Update on Groups Participant level
//            remove the name from group participants and update it with the new name
        database.child("groups").get().addOnSuccessListener { snapshot ->
            for (thing in snapshot.children) {
                val groupId = thing.key // Get the group ID directly

                // Check if the group has the old username as a participant
                if (thing.child("participants").hasChild(oldUsername)) {
                    database.child("groups").child(groupId!!).child("participants")
                        .child(oldUsername).removeValue()
                    database.child("groups").child(groupId).child("participants").child(newUsername)
                        .setValue(newUsername)
                }
            }
        }.addOnFailureListener { error ->
            Log.e("firebase", "Error getting group data", error)
        }

//        update within group item with username
        database.child("groups").get().addOnSuccessListener { snapshot ->
            for (thing in snapshot.children) {
                val groupId = thing.key // Get the group ID directly

                // Iterate through each item within the group
                for (itemSnapshot in thing.child("items").children) {
                    val username = itemSnapshot.child("username").getValue(String::class.java)
                    if (username == oldUsername) {
                        itemSnapshot.child("username").ref.setValue(newUsername)
                    }
                }
            }
        }.addOnFailureListener { error ->
            Log.e("firebase", "Error getting group data", error)
        }


    }
}

    suspend fun updatePassword(username: String, newPassword: String){
        withContext(Dispatchers.IO) {
            database = Firebase.database.reference
            database.child("accounts").child(username).child("password").setValue(newPassword)
        }
    }



}