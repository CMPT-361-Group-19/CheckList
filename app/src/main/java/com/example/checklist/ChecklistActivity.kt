package com.example.checklist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.checklist.viewmodel.ChecklistItem
import com.example.checklist.viewmodel.ChecklistViewModel

class ChecklistActivity : AppCompatActivity() {
    private val tag = "ChecklistActivity"
//    private lateinit var groupIdentifier: String
    private val groupIdentifier: String = "Bakers"

    private lateinit var viewModel: ChecklistViewModel
    private lateinit var username: String
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tag," inside checklist")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist)

        viewModel = ViewModelProvider(this)[ChecklistViewModel::class.java]

        viewModel.getGroupItems(groupIdentifier)

        username = getSharedPreferences("Checklist", MODE_PRIVATE).getString("username","empty").toString()
        val dataset: ArrayList<ChecklistItem>? = viewModel.groupItems.value

        val checkListAdapter = ChecklistAdapter(dataset,viewModel,groupIdentifier,username)

        val recyclerView: RecyclerView = findViewById(R.id.checklist_recycler_view)
        recyclerView.adapter = checkListAdapter
        recyclerView.layoutManager = LinearLayoutManager(this);
        Log.d(tag," inside checklist2 ${viewModel.groupItems.value}")


        viewModel.groupItems.observe(this){
            Log.d(tag," inside checklist3")
            checkListAdapter.updateDataset(it)
            checkListAdapter.notifyDataSetChanged()
        }
        Log.d(tag," inside checklist4")

        Log.d(tag," inside checklist5")
    }
}