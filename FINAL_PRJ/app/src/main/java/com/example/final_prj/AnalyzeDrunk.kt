package com.example.final_prj

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.final_prj.databinding.FragmentAnalyzeDrunkBinding

class AnalyzeDrunk : Fragment() {
    val TAG = "[[Tab_AnalyzeDrunk]]"
    var _binding: FragmentAnalyzeDrunkBinding? = null
    val binding get() = _binding!!
    lateinit var mainActivity:MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAnalyzeDrunkBinding.inflate(inflater, container, false)
        var btnStart = binding.btnAnalStart

        Log.d(TAG, "${TAG} is Create")
        btnStart.setOnClickListener {
            mainActivity!!.changeFragment(2)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}