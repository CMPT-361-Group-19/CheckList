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

    fun getGroupsWithUser(user: String) : ArrayList<String> {
        database = Firebase.database.reference

        var groupList = arrayListOf<String>()
        var groupName = ""

        database.child("groups").get().addOnSuccessListener() {
            for (thing in it.children) {
                if (thing.child("participants").hasChild(user)) {
                    Log.d("groupID:", "${thing.getValue(Group::class.java)!!.groupId}")
                    groupName = thing.getValue(Group::class.java)!!.groupId!!
                    groupList.add(groupName)
                }
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting group data", it)
        }

        return groupList
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

}