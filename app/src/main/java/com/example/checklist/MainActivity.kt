package com.example.checklist

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.checklist.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.database.core.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //adding new accounts to database (example)
        val database : Database = Database()
        database.writeNewAccount("user1", "pass1")
        database.writeNewAccount("user2", "pass2")

        //getting account from database (example)
        lifecycleScope.launch {

            //update ui within this coroutine
            val account = database.getAccount("user1","pass1")!! // Some suspend function fetching data

            account.username?.let { Log.i("Username: ", it) }
            account.password?.let { Log.i("Password: ", it) }
        }


        //creating new groups (example)
        database.writeNewGroup("Group1","desc1")
        database.writeNewGroup("Group2"," desc2")
        database.writeNewGroup("Group3", "desc3")
        database.writeNewGroup("Group4", "desc4")


        //Getting the group of a specified group id
        lifecycleScope.launch {
            val group = database.getGroup("Group1")!! // Some suspend function fetching data
            //updateUI(result)
            group.groupId?.let { Log.i("Group", it) }
        }


        //retrieve list of groups as arrayList
        lifecycleScope.launch {
            val groupList = database.getGroupList()!!

            //updateUI(result)
            for (group in groupList) {
                group.groupId?.let { Log.i("GroupList: ", it) }
            }
        }



        
        //creating a group participantlist within a group (example)
        database.writeGroupParticipant("Group1","user1")
        database.writeGroupParticipant("Group1","user2")
        database.writeGroupParticipant("Group2","user2")

        //getting a certain group's participant list (example)
        lifecycleScope.launch {
            val participantList = database.getGroupParticipants("Group1")!!

            //can updateUI within this coroutine
            for (participant in participantList) {
                participant.let { Log.i("Participants", it) }
            }
        }



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
}