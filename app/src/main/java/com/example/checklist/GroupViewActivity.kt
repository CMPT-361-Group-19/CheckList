package com.example.checklist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.checklist.ui.home.GridviewAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class GroupViewActivity : AppCompatActivity() {

    private lateinit var database: Database
    private lateinit var username: String
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_group_view)
//        val groupIconImg = findViewById<TextView>(R.id.groupIconImage)
//        val groupname = findViewById<TextView>(R.id.groupName)

        database = Database()
        username = getSharedPreferences("Checklist", MODE_PRIVATE).getString("username","empty").toString()
        val serviceIntent = Intent(this, LocationService::class.java)
        startForegroundService(serviceIntent)
        //Navigating between Activities
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.group -> {
                    val intent = Intent(this,NewGroupActivity::class.java)
                    intent.putExtra("user", username)
                    startActivity(intent)
                    true
                }
                R.id.home -> {
                    // Already in Home, do nothing or refresh if needed
                    true
                }
                else -> false
            }
        }
    }

    private fun processRestOfPage(groupList : ArrayList<String>) {
        val gridView = findViewById<GridView>(R.id.gridView)

        var numGroups = groupList.size

        val adapter = GridviewAdapter(this, groupList)
        gridView.adapter = adapter
        val textView = findViewById<TextView>(R.id.subtextMyGroups)
        textView.text = "You have ${numGroups.toString()} groups."

        Log.d("groups:", groupList.toString())

        findViewById<Button>(R.id.addGroupButton).setOnClickListener {newGroupClicked()}

        gridView.setOnItemClickListener{_, _, pos, id ->
            Log.d("debug:", pos.toString())
            Log.d("debug:", id.toString())
            val intent = Intent(this, ChecklistActivity::class.java)
            intent.putExtra("group identifier", groupList[pos])
            startActivity(intent)
        }
    }

    private fun newGroupClicked() {
        val intent = Intent(this,NewGroupActivity::class.java)
        intent.putExtra("user", username)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        Log.d("debug", "OnResume")

        lifecycleScope.launch {
            val groupList = database.getGroupsWithUser(username)

            kotlinx.coroutines.delay(200)

            processRestOfPage(groupList)
        }
    }
}