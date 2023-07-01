package com.example.final_prj

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.final_prj.databinding.FragmentAnalyzeDrunkCamBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class AnalyzeDrunkCam : Fragment() {
    val TAG = "[[Tab_AnalyzeDrunk_CAM]]"
    var _binding: FragmentAnalyzeDrunkCamBinding? = null
    val binding get() = _binding!!

    // 요청할 카메라 권한의 코드 정의  =============
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 2
    }

    // lazy init
    private val packageManager:PackageManager by lazy {mainActivity.packageManager}
    lateinit var mainActivity:MainActivity
    lateinit var btnNext:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAnalyzeDrunkCamBinding.inflate(inflater, container, false)
        var btnBack = binding.btnBack
        var btnCam = binding.btnCam
        btnNext = binding.btnCamNext

        mainActivity = activity as MainActivity
        Log.d(TAG, "${TAG} is Create")

        // Btn Events =================
        btnBack.setOnClickListener {
            mainActivity.changeFragment(1)
        }

        btnNext.setOnClickListener {
            mainActivity!!.changeFragment(3)
        }

        btnCam.setOnClickListener {
            if (checkPermission()) dispatchTakePictureIntent()
            else requestPermission()
        }

        return binding.root
    }

    // 카메라 권한 접근 ==============
    private fun requestPermission() {
        ActivityCompat.requestPermissions(mainActivity, arrayOf(READ_EXTERNAL_STORAGE, CAMERA), 1)
    }

    // 권한이 있는지 확인 ============
    private fun checkPermission():Boolean {
        return (ContextCompat.checkSelfPermission(mainActivity, CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mainActivity, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    // 권한에 따른 Result ===========
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mainActivity, "권한이 설정되었습니다.", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(mainActivity, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 카메라 실행 ===================
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    // 카메라 촬영 후 이미지 받아오기 ====
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val file = File(mainActivity.filePath, "${mainActivity.saveCamFile}")
        var outputStream: OutputStream? = null

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            var imgView = binding.imgCap

            // 촬영한 이미지를 setting 하기
            imgView.setImageBitmap(imageBitmap)

            // bitmap to bytearray
            file.createNewFile()
            outputStream = FileOutputStream(file)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // jpg 로 변환
            outputStream.close() // Stream 닫기

            // 다음 버튼 생성
            btnNext.visibility = View.VISIBLE
        }
    }
}