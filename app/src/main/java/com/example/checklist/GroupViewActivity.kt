package com.example.checklist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.GridView
import com.example.checklist.ui.home.GridviewAdapter

class GroupViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val groupIconImg = findViewById<TextView>(R.id.groupIconImage)
//        val groupname = findViewById<TextView>(R.id.groupName)

        setContentView(R.layout.activity_group_view)
        val gridView = findViewById<GridView>(R.id.gridView)

        val groupnames = mutableListOf<String>("bakers", "roommates", "family")

        val adapter = GridviewAdapter(this, groupnames)
        gridView.adapter = adapter

        gridView.setOnItemClickListener{_, _, pos, id ->
            val intent = Intent(this, ChecklistActivity::class.java)
            intent.putExtra("group identifier", "Bakers")
            startActivity(intent)
        }
    }
}