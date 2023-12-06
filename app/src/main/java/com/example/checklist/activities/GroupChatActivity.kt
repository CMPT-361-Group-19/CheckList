package com.example.checklist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.checklist.activities.GroupViewActivity
import com.example.checklist.activities.NewGroupActivity
import com.example.checklist.groupchat.MsgAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GroupChatActivity:AppCompatActivity() {
    private lateinit var username: String
    private lateinit var bottomNavigationView: BottomNavigationView
    private var groupIdentifier: String = "Bakers"
    private val database = Database()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groupchat)
        username = getSharedPreferences("Checklist", MODE_PRIVATE).getString("username", "empty")
            .toString()
        //Log.i("user", "$username")

        groupIdentifier = intent.getStringExtra("groupId").toString()
        val msgContent:EditText = findViewById(R.id.edit_text_message)
        val sendButton:Button = findViewById(R.id.button_send)
        val toolBar:androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_group_chat)
        toolBar.title = groupIdentifier
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view_messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        var data: ArrayList<ArrayList<String>>

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                //background tasks
                data = database.getGroupMessages(groupIdentifier)

            }
            withContext(Dispatchers.Main) {
                //ui


                val adapter = MsgAdapter(data)
                Log.i("smth", "${adapter.itemCount}")
                adapter.notifyDataSetChanged()
                recyclerView.adapter = adapter
                //adapter.notifyDataSetChanged()

                var databaseRef = Firebase.database.reference

                val valueEventListener = object : ValueEventListener {


                    override fun onDataChange(snapshot: DataSnapshot) {
                        lifecycleScope.launch {


                            withContext(Dispatchers.IO) {
                                //background tasks
                                data = database.getGroupMessages(groupIdentifier)

                            }
                            withContext(Dispatchers.Main) {
                                val adapter = MsgAdapter(data)
                                //Log.i("smth", "${adapter.itemCount}")
                                adapter.notifyDataSetChanged()
                                recyclerView.adapter = adapter
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle potential errors here
                    }
                }
                databaseRef.child("groups").child(groupIdentifier).child("groupchat").addValueEventListener(valueEventListener)

            }
        }
        sendButton.setOnClickListener() {
            if (msgContent.text.toString() != null) {
                val currentTimeMillis = System.currentTimeMillis()

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formattedDate = sdf.format(Date(currentTimeMillis))


                database.writeGroupchatMsg(groupIdentifier,username, msgContent.text.toString(), formattedDate)
                msgContent.setText("")
            }
        }
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.group -> {
                    val intent = Intent(this, NewGroupActivity::class.java)
                    intent.putExtra("user", username)
                    startActivity(intent)
                    true
                }
                R.id.home -> {
                    startActivity(Intent(this, GroupViewActivity::class.java))
                    true
                }
                else -> false
            }
        }



    }
}