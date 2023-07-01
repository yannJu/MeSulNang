package com.example.final_prj

import android.net.http.SslError
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.final_prj.databinding.FragmentRecommendCocktailBinding
import com.example.final_prj.databinding.FragmentRefrigeratorStateBinding

class RecommendCocktail : Fragment() {
    val TAG = "[[Tab_RecommendCocktail]]"
    lateinit var mainActivity:MainActivity
    var _binding: FragmentRecommendCocktailBinding? = null
    val binding get() = _binding!!
    var URL = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Context를 Activity로 형변환하여 할당
        mainActivity = context as MainActivity
        URL = mainActivity.URL
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRecommendCocktailBinding.inflate(inflater, container, false)
        var webView = binding.listRecommendCocktail
        var txtInfo = binding.txtRecInfo
        var btnDefault = binding.btnDefault
        var btnWeather = binding.btnWeather

        URL = "${mainActivity.URL}/recommend"
        txtInfo.text = "좋아하는 주류를 검색하면 유사 주류를 추천받을 수 있습니다 ^ . ^"

        btnDefault.setOnClickListener {
            URL = "${mainActivity.URL}/recommend"
            txtInfo.text = "좋아하는 주류를 검색하면 유사 주류를 추천받을 수 있습니다 ^ . ^"
            webView.loadUrl("${URL}")
        }

        btnWeather.setOnClickListener {
            URL = "${mainActivity.URL}/recommend/weather"
            txtInfo.text = "지역(시/군/구)을 입력하시면 해당 지역의 날씨에 따라 주류를 추천받을 수 있습니다 ^ . ^"
            webView.loadUrl("${URL}")
        }

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
        webView.loadUrl("${URL}")
        // WebView ----------------------------

        return binding.root
    }
}