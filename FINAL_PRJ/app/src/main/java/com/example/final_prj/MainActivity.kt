package com.example.final_prj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.final_prj.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MainActivity : AppCompatActivity() {
    lateinit var tab_refrigerator_state:RefrigeratorState
    lateinit var tab_recommend_cocktail:RecommendCocktail
    lateinit var tab_recommend_snacks:RecommendSnack
    lateinit var tab_community:Community
    lateinit var tab_analyze_drunk:AnalyzeDrunk
    lateinit var tab_analyze_drunk_cam:AnalyzeDrunkCam
    lateinit var tab_analyze_drunk_mic:AnalyzeDrunkMic
    lateinit var tab_analyze_drunk_result:AnalyzeDrunkResult
    lateinit var imgByteArray:ByteArray

    var ID = ""
    val TAG = "[[MainActivity]]"

    // Mqtt ----------------------
//    val URL = "http://ex-alb-1767737241.us-east-2.elb.amazonaws.com"
    val URL = "http://172.30.1.43:8000"
    val brokerUrl = "tcp://172.30.1.43:1883" //Android IP
    lateinit var mqttClient: MqttClient
    val clientID = "client_main"
    // Mqtt ----------------------
    // External Mem Dir

    val recordingFilePath = "/data/data/com.example.final_prj/cache"

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        var binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ID 출력
//        ID = intent.getStringExtra("ID")!!
//        Log.d(TAG, "ID : ${ID}")

        // MQTT ------------------------------
        try {
            mqttClient = MqttClient(brokerUrl, clientID, MemoryPersistence())
//            val options = MqttConnectOptions()
//            options.connectionTimeout = 5
//            mqttClient.connect(options)
            mqttClient.connect()
        } catch(ex: MqttException) {
            ex.printStackTrace()
        }

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
                if (topic != null && mqttMessage != null && topic == "refri/logout") {
                    val msg = mqttMessage.toString()
                    Log.d(TAG, msg)

                    if (msg == "success") {
                        // Intent 생성
                        val intent = Intent(this@MainActivity, LoginMain::class.java)

                        // Activity 시작하기
                        finish()
                        startActivity(intent)
                    }
                }
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Message delivered")
            }
        })
//        mqttClient.subscribe("refri/logout")
        // MQTT ------------------------------

        // tab 과 프래그먼트 연결
        tab_refrigerator_state = RefrigeratorState()
        tab_recommend_cocktail = RecommendCocktail()
        tab_recommend_snacks = RecommendSnack()
        tab_community = Community()
        tab_analyze_drunk = AnalyzeDrunk()
        tab_analyze_drunk_cam = AnalyzeDrunkCam()
        tab_analyze_drunk_mic = AnalyzeDrunkMic()
        tab_analyze_drunk_result = AnalyzeDrunkResult()
        // 초기화면 설정
        supportFragmentManager.beginTransaction().add(R.id.Constraint, tab_refrigerator_state).commit()

        // 탭 클릭시 동작
        binding.tabMenu.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> {
                        // refrigerator State Tab
                        replaceView(tab_refrigerator_state)
                    }
                    1 -> {
                        // recommend snack Tab
                        replaceView(tab_recommend_snacks)
                    }
                    2 -> {
                        // recommend cocktail Tab
                        replaceView(tab_recommend_cocktail)
                    }
                    3 -> {
                        // community tab
                        replaceView(tab_community)
                    }
                    4 -> {
                        // community tab
                        replaceView(tab_analyze_drunk)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    // Fragment to Fragment
    fun changeFragment(index: Int){
        when(index){
            1 -> {
                replaceView(tab_analyze_drunk)
            }

            2 -> {
                replaceView(tab_analyze_drunk_cam)
            }
            3 -> {
                replaceView(tab_analyze_drunk_mic)
            }
            4 -> {
                replaceView(tab_analyze_drunk_result)
            }
        }
    }

    private fun replaceView(tab: Fragment) {
        // 화면을 변경하는 func
        var selectedFragment:Fragment? = null

        selectedFragment = tab
        selectedFragment?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.Constraint, it).commit()
        }
    }
}