package com.example.checklist

import android.util.Log
import com.example.checklist.Database.Group
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class Database {
    private lateinit var database : DatabaseReference

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

}