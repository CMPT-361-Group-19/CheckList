package com.example.checklist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.checklist.viewmodel.ChecklistItem
import com.example.checklist.viewmodel.ChecklistViewModel

class ChecklistAdapter(
    private var dataSet: ArrayList<ChecklistItem>?,
    private val viewModel: ChecklistViewModel,
    private val groupIdentifier: String,
    private val username: String
):  RecyclerView.Adapter<ChecklistAdapter.ViewHolder>() {
    private val tag = "CheckListAdapter"
    class ViewHolder(view: View,private val viewModel: ChecklistViewModel,private val username: String,private val groupIdentifier: String) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox
        private val tag = "CheckListHolder"


        init {
            // Define click listener for the ViewHolder's View
            checkBox = view.findViewById(R.id.checkBox)
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d(tag,"look unchecked/checked ${buttonView.text} $isChecked")
                viewModel.changeItemStatus(groupIdentifier,buttonView.text.toString(),username,isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.text_row_item, parent, false)

        return ViewHolder(view,viewModel,username, groupIdentifier)
    }

    override fun getItemCount(): Int {
        return dataSet?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(tag,"look inside bind")
        holder.checkBox.text = dataSet?.get(position)?.item ?: "Empty"
        holder.checkBox.isChecked = dataSet?.get(position)?.isChecked.toBoolean()
    }

    fun updateDataset(list: ArrayList<ChecklistItem>){
        dataSet = list
    }
}