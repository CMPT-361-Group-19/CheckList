package com.example.checklist.groupchat

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.checklist.R

class MsgAdapter(private val dataList: ArrayList<ArrayList<String>>) : RecyclerView.Adapter<ChatMsgViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMsgViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.groupchat_message_layout, parent, false)
        return ChatMsgViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatMsgViewHolder, position: Int) {
        val currentItem = dataList.get(position)
        // Bind data to views in the ViewHolder
        // Example: holder.textView.text = currentItem
        holder.timeStamp.text = currentItem.get(0)
        holder.senderName.text = currentItem.get(1)
        holder.message.text = currentItem.get(2)

        Log.i("anothertest","${dataList.get(0).get(0)}")
    }

    override fun getItemCount() = dataList.size
}
