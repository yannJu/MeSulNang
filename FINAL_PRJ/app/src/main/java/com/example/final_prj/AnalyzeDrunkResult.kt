package com.example.final_prj

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.bumptech.glide.Glide
import com.example.final_prj.databinding.FragmentAnalyzeDrunkResultBinding
import java.io.File

class AnalyzeDrunkResult : Fragment() {
    val TAG = "[[Tab_AnalyzeDrunk_Result]]"
    var _binding: FragmentAnalyzeDrunkResultBinding? = null
    val binding get() = _binding!!
    lateinit var mainActivity:MainActivity
    lateinit var awsID:awsID

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
        awsID = activity as awsID

        var analyze_img = File("${mainActivity.recordingFilePath}/${mainActivity.saveCamFile}")
        var analyze_voice_model = File("${mainActivity.recordingFilePath}/${mainActivity.saveMicModelFile}")
        var analyze_records = File("${mainActivity.recordingFilePath}/${mainActivity.saveMicFile}")

        uploadWithTransferUtility("images/${mainActivity.saveCamFile}", analyze_img)
        uploadWithTransferUtility("model-voice/${mainActivity.saveMicModelFile}", analyze_voice_model)
        uploadWithTransferUtility("voice/${mainActivity.saveMicFile}", analyze_records)

        // layout Views =============
        var txtLoad = binding.txtLoading
        var gifLoad = binding.imgLoading

        // GIF
        Glide.with(this).load(R.raw.soju).into(gifLoad)

        return binding.root
    }

    // s3 upload Func ---------------------------
    fun uploadWithTransferUtility(fileName: String, file: File) {

        val credentialsProvider = CognitoCachingCredentialsProvider(
            mainActivity,
            awsID.AWS_POOL_ID, // 자격 증명 풀 ID
            Regions.US_EAST_2 // 리전 (ck 해야함)
        )

        TransferNetworkLossHandler.getInstance(mainActivity)

        val transferUtility = TransferUtility.builder()
            .context(mainActivity)
            .defaultBucket(awsID.AWS_STORAGE_BUCKET_NAME)
            .s3Client(AmazonS3Client(credentialsProvider, Region.getRegion(Regions.US_EAST_2)))
            .build()

        /* Store the new created Image file path */

        val uploadObserver = transferUtility.upload(fileName, file, CannedAccessControlList.PublicRead)

        //CannedAccessControlList.PublicRead 읽기 권한 추가

        // Attach a listener to the observer
        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    // Handle a completed upload
                    Log.d(TAG, "Upload-FIN")
                }
            }

            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                val done = (((current.toDouble() / total) * 100.0).toInt())
                Log.d(TAG, "UPLOAD - - ID: $id, percent done = $done")
            }

            override fun onError(id: Int, ex: Exception) {
                Log.d(TAG, "UPLOAD ERROR - - ID: $id - - EX: ${ex.message.toString()}")
            }
        })

        // If you prefer to long-poll for updates
        if (uploadObserver.state == TransferState.COMPLETED) {
            /* Handle completion */

        }
    }
}