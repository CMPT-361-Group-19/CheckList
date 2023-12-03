package com.example.checklist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class NewGroupActivity : AppCompatActivity() {

    private lateinit var username : String
    private lateinit var database: Database
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_new_group)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this@NewGroupActivity, GroupViewActivity::class.java))
                    true
                }
                R.id.group -> {
                    // Already in group, do nothing or refresh if needed
                    true
                }

                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.group

        database = Database()
        username = intent.getStringExtra("user")!!

        val context = this

        findViewById<Button>(R.id.buttonJoinGroup).setOnClickListener {
            val groupID = findViewById<EditText>(R.id.inputJoinGroup).text.toString()

            lifecycleScope.launch {
                var currentGroups = database.getGroupList()

                kotlinx.coroutines.delay(200)

                var ifExists = false

                for (group in currentGroups) {
                    if (group.groupId == groupID) {
                        ifExists = true
                    }
                }

                if (ifExists) {
                    database.writeGroupParticipant(groupID, username)
                    finish()
                } else {
                    Toast.makeText(context, "Error: Group doesn't Exist", Toast.LENGTH_LONG).show()
                }
            }
        }

        findViewById<Button>(R.id.buttonCreateGroup).setOnClickListener {
            val groupName = findViewById<EditText>(R.id.inputGroupName).text.toString()
            val groupDesc = findViewById<EditText>(R.id.inputGroupDesc).text.toString()

            lifecycleScope.launch {
                var currentGroups = database.getGroupList()

                kotlinx.coroutines.delay(200)

                var isUnique = true

                for (group in currentGroups) {
                    if (group.groupId == groupName) {
                        isUnique = false
                    }
                }

                if (isUnique) {
                    database.writeNewGroup(groupName, groupDesc)
                    database.writeGroupParticipant(groupName, username)
                    finish()
                } else {
                    Toast.makeText(context, "Error: Group already Exists", Toast.LENGTH_LONG).show()
                }
            }
        }

        findViewById<Button>(R.id.buttonCancelGroup).setOnClickListener {
            finish()
        }
    }

}