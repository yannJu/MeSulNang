package com.example.final_prj

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.final_prj.databinding.FragmentAnalyzeDrunkBinding
import com.example.final_prj.databinding.FragmentAnalyzeDrunkMicBinding
import com.example.final_prj.databinding.FragmentAnalyzeDrunkResultBinding

class AnalyzeDrunkResult : Fragment() {
    val TAG = "[[Tab_AnalyzeDrunk_Result]]"
    var _binding: FragmentAnalyzeDrunkResultBinding? = null
    val binding get() = _binding!!
    lateinit var mainActivity:MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(TAG, "${TAG} is Create")

        _binding = FragmentAnalyzeDrunkResultBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity

        // layout Views =============
        var txtLoad = binding.txtLoading
        var gifLoad = binding.imgLoading

        // GIF
        Glide.with(this).load(R.raw.loading).into(gifLoad)

        return binding.root
    }
}