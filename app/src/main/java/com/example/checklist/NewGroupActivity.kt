package com.example.checklist

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class NewGroupActivity : AppCompatActivity() {

    private lateinit var username : String
    private lateinit var database: Database
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_new_group)

        database = Database()
        username = intent.getStringExtra("user")!!

        findViewById<Button>(R.id.buttonJoinGroup).setOnClickListener {
            val groupID = findViewById<EditText>(R.id.inputJoinGroup).text.toString()
            database.writeGroupParticipant(groupID, username)
            finish()
        }

        findViewById<Button>(R.id.buttonCreateGroup).setOnClickListener {
            val groupName = findViewById<EditText>(R.id.inputGroupName).text.toString()
            val groupDesc = findViewById<EditText>(R.id.inputGroupDesc).text.toString()
            database.writeNewGroup(groupName, groupDesc)
            database.writeGroupParticipant(groupName, username)
            finish()
        }

        findViewById<Button>(R.id.buttonCancelGroup).setOnClickListener {
            finish()
        }
    }

}