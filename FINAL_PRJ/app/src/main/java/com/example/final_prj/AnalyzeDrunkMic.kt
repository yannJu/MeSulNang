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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.final_prj.databinding.FragmentAnalyzeDrunkBinding
import com.example.final_prj.databinding.FragmentAnalyzeDrunkCamBinding
import com.example.final_prj.databinding.FragmentAnalyzeDrunkMicBinding
import java.io.File

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
    var isModelExist = false
    var savePath = ""

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
    lateinit var ckImg:ImageView
    lateinit var txtFirstMsg:TextView
    lateinit var txtFirstwarn:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isModelExist = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAnalyzeDrunkMicBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity
        val modelFilePath = File("${mainActivity.recordingFilePath}/${mainActivity.saveMicModelFile}")

        // Btns ==============================
        var btnBack = binding.btnBack
        txtFirstMsg = binding.txtMicModelInfo
        txtFirstwarn = binding.txtWarn
        btnRecord = binding.imgMic
        btnRecord.setImageResource(R.drawable.mic_black)
        ckImg = binding.imgCk
        btnNext = binding.btnMicNext
        savePath = "${mainActivity.recordingFilePath}/${mainActivity.saveMicModelFile}"

        // model 음성 데이터가 존재할 경우 isModelExist 변수 동기화
        // model 음성 데이터가 존재하면 info 텍스트 지우기 + savePath 변경
        if (modelFilePath.exists()) isModelExist = true
        if (isModelExist) {
            txtFirstMsg.text = "분석 데이터 수집을 위해 녹음을 진행합니다."
            txtFirstwarn.visibility = View.GONE
            savePath = "${mainActivity.recordingFilePath}/${mainActivity.saveMicFile}"
        }

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
            if (isModelExist) mainActivity.changeFragment(4)
            else {
                btnNext.text = "결과보기"
                btnNext.visibility = View.GONE
                // 다시 녹음할 수 있도록 ui 수정
                ckImg.setImageResource(R.drawable.ck_blank)
                btnRecord.setImageResource(R.drawable.mic_black)
                txtFirstMsg.text = "분석 데이터 수집을 위해 녹음을 진행합니다."
                txtFirstwarn.visibility = View.GONE
                isModelExist = true
            }
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
            if (recordState == State.BEFORE_RECORDING) {
                if (!isModelExist) Toast.makeText(mainActivity, "최초 데이터 녹음중 REC . . .", Toast.LENGTH_SHORT).show()
                else Toast.makeText(mainActivity, "취함 분석 데이터 녹음중 REC . . .", Toast.LENGTH_SHORT).show()
                startRecording()
                btnRecord.setImageResource(R.drawable.mic_red)
            }
            else {
                stopRecording()
                if (isModelExist) btnRecord.setImageResource(R.drawable.btn_mic_block)
                else savePath = "${mainActivity.recordingFilePath}/${mainActivity.saveMicFile}"
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
                setOutputFile(savePath)
                prepare()
            }

        recorder!!.start()
        recordState = State.ON_RECORDING
        Log.d(TAG, "${savePath} << Recording . . >>")
    }

    private fun stopRecording() {
        recorder?.run {
            Log.d(TAG, "${savePath} << Recording STOP >>")
            stop()
            reset()
            release()
        }

        recorder = null
        recordState = State.BEFORE_RECORDING

        ckImg.setImageResource(R.drawable.ck_green)
        btnNext.visibility = View.VISIBLE
        if (isModelExist) {
            btnRecord.isClickable = false
            btnNext.text = "결과보기"
        }
    }
}