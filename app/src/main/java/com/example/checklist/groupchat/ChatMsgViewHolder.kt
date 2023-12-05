package com.example.checklist.groupchat

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.checklist.R


class ChatMsgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // Define views within your item layout
    val timeStamp: TextView = itemView.findViewById(R.id.textTimestamp)
    val message: TextView = itemView.findViewById(R.id.textMessage)
    val senderName: TextView = itemView.findViewById(R.id.textSenderName)
}

