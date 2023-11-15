package com.example.checklist.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.checklist.Database
import com.example.checklist.R
import com.example.checklist.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val groupIconImg = root.findViewById<TextView>(R.id.groupIconImage)
        val groupname = root.findViewById<TextView>(R.id.groupName)
        val gridView = root.findViewById<GridView>(R.id.gridView)

        val groupnames = mutableListOf<String>("bakers", "roommates", "family")

        val adapter = GridviewAdapter(requireContext(), groupnames)
        gridView.adapter = adapter

        gridView.setOnItemClickListener{_, _, pos, id ->

        }




        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}