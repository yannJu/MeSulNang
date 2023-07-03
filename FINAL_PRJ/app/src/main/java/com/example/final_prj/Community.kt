package com.example.final_prj

import android.content.DialogInterface
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.final_prj.databinding.FragmentAnalyzeDrunkResultBinding
import com.example.final_prj.databinding.FragmentCommunityBinding
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

class Community : Fragment() {
    lateinit var mainActivity:MainActivity
    val TAG = "[[Tab_Community]]"
    var _binding: FragmentCommunityBinding? = null
    val binding get() = _binding!!
    var URL = ""
    var brokerUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "${TAG} is Create")

        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity
        URL = mainActivity.URL
        brokerUrl = mainActivity.brokerUrl

        var webView = binding.listCommunity

        // MQTT ------------------------------
        val mqttClient = mainActivity.mqttClient

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
                if (topic != null && mqttMessage != null) {
                    if (topic == "refri/logout") {
                        Log.d(TAG, "logout ${msg}")

                        // Intent 생성
                        val intent = Intent(mainActivity, LoginMain::class.java)

                        // Activity 시작하기
                        mainActivity.finish()
                        startActivity(intent)
                    }
                }
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Message delivered")
            }
        })
        mqttClient.subscribe("refri/logout")
        // MQTT ------------------------------

        // WebView ----------------------------
        webView.webChromeClient = object : WebChromeClient(){
            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                onJsConfirm(message!!, result!!)
                return true }
        }

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
        webView.loadUrl("${URL}/community/")
        // WebView ----------------------------

        return binding.root
    }

    fun onJsConfirm(message : String, result : JsResult) : Unit {
        val builder = AlertDialog.Builder(mainActivity)
        builder.setTitle("삭제 경고")
        builder.setMessage(message) // 버튼 클릭 이벤트

        val listener = DialogInterface.OnClickListener { _, clickEvent ->
            when (clickEvent) {
                DialogInterface.BUTTON_POSITIVE ->{
                    result.confirm()
                }
                DialogInterface.BUTTON_NEUTRAL -> {
                    result.cancel()
                }
            }
        }
        builder.setPositiveButton(android.R.string.ok, listener)
        builder.setNeutralButton(android.R.string.cancel, listener)
        builder.show()
    }
}