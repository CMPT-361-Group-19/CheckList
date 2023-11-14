package com.example.checklist

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.example.checklist.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        //example writing to database for Accounts:
        val database : Database = Database()


        database.writeNewAccount("bob", "hi")
        database.getAccount("bob","hi")

        //example creating new group
        database.writeNewGroup("Bakers","we bake desserts")
        database.getGroup("Bakers")
        
        //example creating a group participantlist within a group
        database.writeGroupParticipant("Bakers","bob")
        database.writeGroupParticipant("Bakers","rob")
        database.getGroupParticipants("Bakers")




        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
}