package com.example.final_prj

import android.app.Activity
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.final_prj.databinding.LoginMainBinding
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class LoginMain : AppCompatActivity() {
    val TAG = "[[Tab_Login]]"
    val binding by lazy { LoginMainBinding.inflate(layoutInflater) }
    // Mqtt ----------------------
//    var URL = "http://ex-alb-1767737241.us-east-2.elb.amazonaws.com"
    val URL = "http://172.30.1.43:8000"
    val brokerUrl = "tcp://172.30.1.43:1883" //Android IP
    private lateinit var mqttClient: MqttClient
    // Mqtt ----------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val webView = binding.loginMain
        val logoImg = binding.imgLogo
        val gifImg = binding.imgGif
        // GIF
        Glide.with(this).load(R.raw.soju).into(gifImg)

        // MQTT ------------------------------
        val clientID = "client_login"

        try {
            mqttClient = MqttClient(brokerUrl, clientID, MemoryPersistence())
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
                if (topic != null && mqttMessage != null && topic == "refri/login") {
                    val msg = mqttMessage.toString()
                    Log.d(TAG, msg)
                    
                    // Intent 생성
                    val intent = Intent(this@LoginMain, MainActivity::class.java)
                    intent.putExtra("ID",msg)
                    setResult(Activity.RESULT_OK, intent)
                    
                    // Activity 시작하기
                    finish()
                    startActivity(intent)
                }
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Message delivered")
            }
        })
        mqttClient.subscribe("refri/login")
        // MQTT ------------------------------

        // WebView ----------------------------
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.proceed()
            }
        }
        webView.loadUrl("${URL}/login/")
        // WebView ----------------------------
    }
}