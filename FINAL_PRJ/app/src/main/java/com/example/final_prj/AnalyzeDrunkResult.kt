package com.example.final_prj

import android.content.Intent
import android.graphics.Color
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
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.File

class AnalyzeDrunkResult : Fragment() {
    val TAG = "[[Tab_AnalyzeDrunk_Result]]"
    var _binding: FragmentAnalyzeDrunkResultBinding? = null
    val binding get() = _binding!!
    lateinit var mainActivity:MainActivity

    private lateinit var mqttClient: MqttClient
    var URL = ""

    // S3 -------------
    val AWS_STORAGE_BUCKET_NAME = awsID().AWS_STORAGE_BUCKET_NAME
    val AWS_POOL_ID = awsID().AWS_POOL_ID
    // S3 -------------
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

        var analyze_img = File("${mainActivity.recordingFilePath}/${mainActivity.saveCamFile}")
        var analyze_voice_model = File("${mainActivity.recordingFilePath}/${mainActivity.saveMicModelFile}")
        var analyze_records = File("${mainActivity.recordingFilePath}/${mainActivity.saveMicFile}")

        uploadWithTransferUtility("images/${mainActivity.saveCamFile}", analyze_img)
        uploadWithTransferUtility("model-voice/${mainActivity.saveMicModelFile}", analyze_voice_model)
        uploadWithTransferUtility("voice/${mainActivity.saveMicFile}", analyze_records)

        // layout Views =============
        var txtLoad = binding.txtLoading
        var gifLoad = binding.imgLoading
        var txtwarn = binding.txtLoadingWarn
        var resultImg = binding.imgResult
        var txtResult = binding.txtResult
        var txtResultMsg = binding.txtResultMsg
        var btnReturn = binding.btnReturn

        // 취함 결과 Result Array (0: 안취함, 1: 보통, 2:  취함)
        var imgAry = arrayOf(R.drawable.drunk_img0, R.drawable.drunk_img1, R.drawable.drunk_img2)
        var resultAry = arrayOf(
            "지금부터 시작이야아",
            "술기운이 올라와요~",
            "취하셨군요!"
        )
        var resultColorAry = arrayOf("#75d327", "#eb9e3a","#f57f7f")
        var msgAry = arrayOf(
            "술을 마신다고 문제가 \n해결되는 것은 아니지만\n우유를 마신다고 \n해결되는 것도 없다\n\n-스코틀랜드 격언",
            "술을 물처럼 마시는 자는\n술을 마실 가치가 없다\n\n-프리드리히 V. 보덴슈테트",
            "술잔과 입술 사이에는 \n많은 실수가 있다\n\n-팔라다스"
        )

        // GIF
        Glide.with(this).load(R.raw.soju).into(gifLoad)

        // Btn Event -------------------------
        btnReturn.setOnClickListener {
            mainActivity!!.changeFragment(1)
        }

        // MQTT ------------------------------
        mqttClient = mainActivity.mqttClient

        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(throwable: Throwable?) {
                throwable?.printStackTrace()
                try {
                    mqttClient.reconnect()
                } catch(ex: MqttException){
                    ex.printStackTrace()
                }
            }
            override fun messageArrived(topic: String?, mqttMessage: MqttMessage?) {
                val msg = mqttMessage.toString()
                Log.d(TAG, "${topic}->[[${msg}]]")
                if (topic != null && mqttMessage != null) {
                    if (topic == "analyze/result") {
                        mainActivity.runOnUiThread {
                            // 기존 View visible -> GONE
                            txtwarn.visibility = View.GONE
                            txtLoad.visibility = View.GONE
                            gifLoad.visibility = View.GONE
                            // View 띄우기
                            txtResult.visibility = View.VISIBLE
                            txtResultMsg.visibility = View.VISIBLE
                            resultImg.visibility = View.VISIBLE
                            // 결과에 따라 값 다르게 주기
                            if (msg == "0") {
                                txtResult.text = resultAry[0]
                                txtResult.setTextColor(Color.parseColor(resultColorAry[0]))
                                txtResultMsg.text = msgAry[0]
                                resultImg.setImageResource(imgAry[0])
                            }
                            else if (msg == "1") {
                                txtResult.text = resultAry[1]
                                txtResult.setTextColor(Color.parseColor(resultColorAry[1]))
                                txtResultMsg.text = msgAry[1]
                                resultImg.setImageResource(imgAry[1])
                            }
                            else if (msg == "2") {
                                txtResult.text = resultAry[2]
                                txtResult.setTextColor(Color.parseColor(resultColorAry[2]))
                                txtResultMsg.text = msgAry[2]
                                resultImg.setImageResource(imgAry[2])
                            }

                            btnReturn.visibility = View.VISIBLE
                        }
                    }
                }
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Message delivered")
            }
        })
        mqttClient.subscribe("analyze/result")
        // MQTT ------------------------------

        return binding.root
    }

    // s3 upload Func ---------------------------
    fun uploadWithTransferUtility(fileName: String, file: File) {

        val credentialsProvider = CognitoCachingCredentialsProvider(
            context,
            AWS_POOL_ID, // 자격 증명 풀 ID
            Regions.US_EAST_2 // 리전 (ck 해야함)
        )

        TransferNetworkLossHandler.getInstance(context)

        val transferUtility = TransferUtility.builder()
            .context(context)
            .defaultBucket(AWS_STORAGE_BUCKET_NAME)
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

    // sol2
//    setTransferListener(new TransferListener() {
//        @Override
//        public void onStateChanged(int id, TransferState state) {
//            Log.d(TAG, "onStateChanged: " + id + ", " + state.toString());
//
//        }
//
//        @Override
//        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//            float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
//            int percentDone = (int)percentDonef;
//            Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
//        }
//
//        @Override
//        public void onError(int id, Exception ex) {
//            Log.e(TAG, ex.getMessage());
//        }
//    });
}