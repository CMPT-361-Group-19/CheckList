package com.example.checklist
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.checklist.viewmodel.ChecklistItem
import com.example.checklist.viewmodel.ChecklistViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration

class ChecklistActivity : AppCompatActivity() {
    private val tag = "ChecklistActivity"
    private lateinit var bottomNavigationView: BottomNavigationView
//    private lateinit var groupIdentifier: String
    private var groupIdentifier: String = "Bakers"

    private lateinit var checkListAdapter: ChecklistAdapter
    private lateinit var viewModel: ChecklistViewModel
    private lateinit var username: String
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tag," inside checklist")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.group -> {
                    startActivity(Intent(this@ChecklistActivity, NewGroupActivity::class.java))
                    true
                }
                R.id.home -> {
                    startActivity(Intent(this@ChecklistActivity, GroupViewActivity::class.java))
                    true
                }
                R.id.profile -> {
                    startActivity(Intent(this@ChecklistActivity, profile::class.java))
                    true
                }
                else -> false
            }
        }


        groupIdentifier = intent.getStringExtra("group identifier").toString()

        findViewById<TextView>(R.id.groupName).text = groupIdentifier

        viewModel = ViewModelProvider(this)[ChecklistViewModel::class.java]

        viewModel.getGroupItems(groupIdentifier)

        username = getSharedPreferences("Checklist", MODE_PRIVATE).getString("username","empty").toString()
        val dataset: ArrayList<ChecklistItem>? = viewModel.groupItems.value

        checkListAdapter = ChecklistAdapter(dataset,viewModel,groupIdentifier,username)
        val recyclerView: RecyclerView = findViewById(R.id.checklist_recycler_view)
        recyclerView.adapter = checkListAdapter

        checkListAdapter.onItemClickListener = {item ->
            Log.d("ChecklistActivity","Item clicked: ${item.item}")
            val intent = Intent(this@ChecklistActivity,ItemDetailsActivity::class.java)
            intent.putExtra("itemName",item.item)
            intent.putExtra("itemUser",username)
            intent.putExtra("itemLocation",item.selectedPlace?.location)
            intent.putExtra("groupId",groupIdentifier)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this);
        Log.d(tag," inside checklist2 ${viewModel.groupItems.value}")


        viewModel.groupItems.observe(this){
            Log.d(tag," inside checklist3")
            checkListAdapter.updateDataset(it)
            checkListAdapter.notifyDataSetChanged()
        }

        findViewById<Button>(R.id.addButton).setOnClickListener{
            val intent = Intent(this,AddItemActivity::class.java)
            intent.putExtra("groupId",groupIdentifier)
            startActivity(intent)
        }

        findViewById<Button>(R.id.exitGroup).setOnClickListener {
            viewModel.exitGroup(username,groupIdentifier)
            finish()
        }

        ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val itemToDelete = viewModel.groupItems.value?.get(viewHolder.adapterPosition)?.item
                lifecycleScope.launch {
                    val isDeleted = itemToDelete?.let {viewModel.deleteItemIfValid(groupIdentifier,it,username)}
                    if(isDeleted?.await() == true){
                        Toast.makeText(this@ChecklistActivity,"Deleted entry",Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(this@ChecklistActivity,"Ypu cannot delete entries made by other members",Toast.LENGTH_SHORT).show()
                        viewHolder.adapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                            ?.let { checkListAdapter.notifyItemChanged(it) }
                    }
                }
            }
        }).attachToRecyclerView(recyclerView)
    }
}