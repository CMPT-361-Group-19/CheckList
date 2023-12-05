package com.example.checklist.activities
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.checklist.adapters.ChecklistAdapter
import com.example.checklist.R
import com.example.checklist.viewmodel.ChecklistItem
import com.example.checklist.viewmodel.ChecklistViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.util.Locale

class ChecklistActivity : AppCompatActivity() {
    private val tag = "ChecklistActivity"
    private lateinit var bottomNavigationView: BottomNavigationView
    private var groupIdentifier: String = "Bakers"

    private lateinit var checkListAdapter: ChecklistAdapter
    private lateinit var viewModel: ChecklistViewModel
    private lateinit var username: String
    lateinit var micIV: ImageView
    private lateinit var speechRecognitionLauncher: ActivityResultLauncher<Intent>
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
                    startActivity(Intent(this@ChecklistActivity, ProfileActivity::class.java))
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
            checklistItemClicked(item)
        }

        recyclerView.layoutManager = LinearLayoutManager(this);
        Log.d(tag," inside checklist2 ${viewModel.groupItems.value}")


        viewModel.groupItems.observe(this){
            Log.d(tag," inside checklist3")
            checkListAdapter.updateDataset(it)
            checkListAdapter.notifyDataSetChanged()
        }
        findViewById<ImageView>(R.id.chatButton).setOnClickListener{
            val intent = Intent(this,GroupChatActivity::class.java)
            intent.putExtra("groupId",groupIdentifier)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.addButton).setOnClickListener{
            val intent = Intent(this, AddItemActivity::class.java)
            intent.putExtra("groupId",groupIdentifier)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.exitGroup).setOnClickListener {
            viewModel.exitGroup(username,groupIdentifier)
            finish()
        }

        micIV = findViewById(R.id.mic)
        speechRecognitionLauncher = initializeSpeechActivityLauncher()

        // on below line we are adding on click
        // listener for mic image view.
        micIV.setOnClickListener {
            onMicClicked()
        }

        val easyAddEditText = findViewById<EditText>(R.id.easyAddEditText)

        easyAddEditText.setOnEditorActionListener(
            TextView.OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Get the entered text
                    val newItem: String = easyAddEditText.text.toString()

                    // Add the new item to your checklist (implement your own logic)
                   easySaveTaskToDB(newItem)

                    // Clear the EditText after adding the item
                    easyAddEditText.text.clear()
                    return@OnEditorActionListener true
                }
                false
            })

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
                        Toast.makeText(this@ChecklistActivity,"Entry deleted",Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(this@ChecklistActivity,"You cannot delete entries made by other members!",Toast.LENGTH_SHORT).show()
                        viewHolder.adapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                            ?.let { checkListAdapter.notifyItemChanged(it) }
                    }
                }
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun onMicClicked(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                ),
                3
            )
        }
        // on below line we are calling speech recognizer intent.
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        // on below line we are passing language model
        // and model free form in our intent
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        // on below line we are passing our
        // language as a default language.
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

        // on below line we are specifying a try catch block.
        // in this block we are calling a start activity
        // for result method and passing our result code.
        try {
            speechRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            // on below line we are displaying error message in toast
            Toast
                .makeText(
                    this, " " + e.message,
                    Toast.LENGTH_SHORT
                )
                .show()
        }}

    private fun checklistItemClicked(item: ChecklistItem){
        Log.d("ChecklistActivity","Item clicked: ${item.item}")
        val intent = Intent(this@ChecklistActivity, ItemDetailsActivity::class.java)
        intent.putExtra("itemName",item.item)
        intent.putExtra("itemUser",username)
        intent.putExtra("itemLocation",item.selectedPlace?.location)
        intent.putExtra("groupId",groupIdentifier)
        startActivity(intent)
    }

    private fun initializeSpeechActivityLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Check if the result is OK and has data
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            // Extract the data from the Intent
            val res: ArrayList<String>? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            if(res?.get(0) != null){
                val item =  res?.get(0).toString()
                easySaveTaskToDB(item)
            }
        }
    }}

    private fun easySaveTaskToDB(itemName: String) {
        Log.d(tag, "look in save task $username")
        val checkListItem = ChecklistItem(itemName, false.toString(), username, SelectedPlace())
        if (itemName.isNullOrBlank()) {
            Toast.makeText(this, "Fields cannot be null", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addGroupItems(groupIdentifier,checkListItem)
        }
    }
}