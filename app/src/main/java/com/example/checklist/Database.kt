package com.example.checklist

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import kotlinx.coroutines.flow.callbackFlow

class Database {
    private lateinit var database : DatabaseReference

    @IgnoreExtraProperties
    data class Account(val username : String? = null, val password : String?)

    //write account to database
    fun writeNewAccount(username: String, password: String) {
        val account = Account(username,password)
        database = Firebase.database.reference
        database.child("accounts").child(username).child("username").setValue(username)
        database.child("accounts").child(username).child("password").setValue(password)
    }

    //read from database
    //prints account info in logcat for reference
    fun getAccount(username: String, password: String) : Account? {
        var data: Account? = null
        database = Firebase.database.reference
        database.child("accounts").child(username).get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            //data = it.getValue(Account::class.java)

        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
        return data
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

    @IgnoreExtraProperties
    data class Group(val groupId: String? = null, val desc: String? = null)

    //write group to database
    fun writeNewGroup(groupId: String, desc: String) {
        val group = Group(groupId,desc)
        database = Firebase.database.reference
        database.child("groups").child(groupId).setValue(group)
    }

    //get the group associated with the group id
    fun getGroup(groupId: String): Group? {
        var data: Group? = null
        database = Firebase.database.reference
        val groupResult = database.child("groups").child(groupId).get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")


        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
        return data
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

    //returns an arraylist of Strings that contains usernames of the participants of a particular group
    fun getGroupParticipants(groupId: String) : ArrayList<String> {

        var participantList: ArrayList<String> = ArrayList<String>()
        database = Firebase.database.reference
        val groupResult = database.child("groups").child(groupId).child("participants").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            for (childSnapshot in it.children) {
                // Get the value of the username
                val value = childSnapshot.value
                participantList.add(value.toString())
                // Print or do something with the key and value
                Log.d("FirebaseData", "Key: $groupId, Value: $value")
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        return participantList
    }


}