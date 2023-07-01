package com.example.final_prj

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.final_prj.databinding.FragmentRefrigeratorStateBinding
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
class RefrigeratorState : Fragment() {
    val TAG = "[[Tab_RefrigeratprState]]"
    lateinit var mainActivity:MainActivity
    private lateinit var mqttClient: MqttClient

    var _binding:FragmentRefrigeratorStateBinding? = null
    val binding get() = _binding!!
    var URL = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Context를 Activity로 형변환하여 할당
        mainActivity = context as MainActivity
        URL = mainActivity.URL
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRefrigeratorStateBinding.inflate(inflater, container, false)
        var webView = binding.listRefrigerator
        var tempTxt = binding.txtEditTemp
        var spinner = binding.spinnerFunc
        var itemList = resources.getStringArray(R.array.itemList)
        var adapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1, itemList)
        var refriName = ""

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
                    if (topic == "refri/refriname") {
                        refriName = msg
                    }

                    if (topic == "${refriName}/refri/sensors/temp") {
                        mainActivity.runOnUiThread {
                            tempTxt.text = msg
                        }
                    }

                    if (topic == "refri/logout" && msg == "success") {
                        // Intent 생성
                        val intent = Intent(mainActivity, LoginMain::class.java)

                        // Activity 시작하기
                        mainActivity.finish()
                        startActivity(intent)
                        mqttClient.disconnect()
                    }
                }
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Message delivered")
            }
        })
        mqttClient.subscribe(arrayOf("${mainActivity.ID}/refri/sensors/temp", "refri/logout"))
        // MQTT ------------------------------

        // Spinner ----------------------------
        spinner.adapter = adapter
        spinner.setSelection(0)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0) Toast.makeText(context, itemList[position], Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        // Spinner ----------------------------

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
        webView.loadUrl("${URL}/frige/")
        // WebView ----------------------------

        return binding.root
    }
}