package com.example.checklist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.GridView
import android.widget.TextView
import com.example.checklist.ui.home.GridviewAdapter

class ChecklistActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val groupIconImg = findViewById<TextView>(R.id.groupIconImage)
//        val groupname = findViewById<TextView>(R.id.groupName)

        setContentView(R.layout.activity_checklist)
        val gridView = findViewById<GridView>(R.id.gridView)

        val groupnames = mutableListOf<String>("bakers", "roommates", "family")

        val adapter = GridviewAdapter(this, groupnames)
        gridView.adapter = adapter

        gridView.setOnItemClickListener{_, _, pos, id ->

        }
    }
}