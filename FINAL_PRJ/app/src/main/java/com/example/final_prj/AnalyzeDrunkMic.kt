package com.example.final_prj

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.final_prj.databinding.FragmentAnalyzeDrunkBinding
import com.example.final_prj.databinding.FragmentAnalyzeDrunkCamBinding
import com.example.final_prj.databinding.FragmentAnalyzeDrunkMicBinding

// 녹음 상태와 관련된 class
enum class State {
    BEFORE_RECORDING,
    ON_RECORDING
}

class AnalyzeDrunkMic : Fragment() {
    val TAG = "[[Tab_AnalyzeDrunk_MIC]]"
    var _binding: FragmentAnalyzeDrunkMicBinding? = null
    val binding get() = _binding!!

    var recordState = State.BEFORE_RECORDING
    var recorder: MediaRecorder ?= null
    var cnt = 0

    // 요청할 권한들을 담을 배열  =================
    private val requiredPermissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO
    )
    // 요청할 오디오 권한의 코드 정의  =============
    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }

    // lazy Init ==============================
    lateinit var mainActivity:MainActivity
    lateinit var btnRecord:ImageView
    lateinit var btnNext:Button
    lateinit var ckAry:Array<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAnalyzeDrunkMicBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity

        // Btns ==============================
        var btnBack = binding.btnBack
        btnRecord = binding.imgMic
        btnRecord.setImageResource(R.drawable.mic_black)

        btnNext = binding.btnMicNext

        // Arys ==============================
        ckAry = arrayOf(binding.imgCk1, binding.imgCk2, binding.imgCk3)

        // Btn Events ========================
        Log.d(TAG, "${TAG} is Create")
        btnBack.setOnClickListener {
            mainActivity.changeFragment(2)
        }

        if (btnRecord.isClickable == true) {
            btnRecord.setOnClickListener {
                requestAudioPermission()
            }
        }

        btnNext.setOnClickListener {
            mainActivity.changeFragment(4)
        }

        return binding.root
    }

    // 오디오 권한 접근 ==============
    private fun requestAudioPermission() {
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    // 권한에 따른 Result ===========
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mainActivity, "녹음중 REC . . .", Toast.LENGTH_SHORT).show()
            if (recordState == State.BEFORE_RECORDING) {
                startRecording()
                btnRecord.setImageResource(R.drawable.mic_red)
            }
            else {
                stopRecording()
                if (cnt < 3) btnRecord.setImageResource(R.drawable.mic_black)
                else btnRecord.setImageResource(R.drawable.btn_mic_block)
            }
        }
        else {
            Toast.makeText(mainActivity, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startRecording() {
        recorder = MediaRecorder()
            .apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) //format
                setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT) //인코더
                setOutputFile("${mainActivity.recordingFilePath}/recording_${cnt}.mp4")
                prepare()
            }

        recorder!!.start()
        recordState = State.ON_RECORDING
        Log.d(TAG, "${mainActivity.recordingFilePath}/recording_${cnt}.mp4 << Recording . . >>")
    }

    private fun stopRecording() {
        var ckImg = ckAry.get(cnt)
        recorder?.run {
            Log.d(TAG, "${mainActivity.recordingFilePath}/recording_${cnt}.mp4 << Recording STOP >>")
            stop()
            reset()
            release()
        }

        recorder = null
        recordState = State.BEFORE_RECORDING

        ckImg.setImageResource(R.drawable.ck_green)
        cnt += 1

        if (cnt == 3) {
            btnRecord.isClickable = false
            btnNext.visibility = View.VISIBLE
        }
    }
}