package com.example.checklist.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.checklist.R

class GridviewAdapter(private val context: Context, private val groupNames:MutableList<String>) :BaseAdapter() {
    override fun getCount(): Int {
        return groupNames.size
    }

    override fun getItem(pos: Int): Any {
        return groupNames[pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.group_icon, null)
        } else {
            convertView
        }

//        val groupIconImg:ImageView = view.findViewById(R.id.groupIconImage)
        val groupname: TextView= view.findViewById(R.id.groupName)

        groupname.text= groupNames[position]

        return view

    }

}