package com.kuzatuvchi.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

class MainActivity : Activity() {

    private val adServers = listOf(
        "ads.google.com", 
        "doubleclick.net", 
        "adserver",
        "taboola",
        "outbrain"
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?, 
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url.toString()
                if (adServers.any { url.contains(it, ignoreCase = true) }) {
                    return WebResourceResponse(
                        "text/plain", 
                        "utf-8", 
                        ByteArrayInputStream("".toByteArray())
                    )
                }
                return super.shouldInterceptRequest(view, request)
            }
        }

        // Set this to your preferred manga site
        webView.loadUrl("https://google.com") 
    }
}
