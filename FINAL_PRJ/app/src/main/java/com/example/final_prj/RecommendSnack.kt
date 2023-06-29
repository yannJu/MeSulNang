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
import com.example.final_prj.databinding.FragmentRecommendSnackBinding
import com.example.final_prj.databinding.FragmentRefrigeratorStateBinding

class RecommendSnack : Fragment() {
    val TAG = "[[Tab_RecommendSnack]]"
    lateinit var mainActivity:MainActivity
    var _binding: FragmentRecommendSnackBinding? = null
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
        _binding = FragmentRecommendSnackBinding.inflate(inflater, container, false)
        var webView = binding.listRecommendSnack

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
        webView.loadUrl("http://www.naver.com")
        // WebView ----------------------------

        return binding.root
    }
}