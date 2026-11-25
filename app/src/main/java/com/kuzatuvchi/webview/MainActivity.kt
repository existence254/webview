package com.example.mangabrowser

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayInputStream

class MainActivity : AppCompatActivity() {

    // Add known ad and tracker domains here
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

        // Make it act like a standard mobile browser to bypass bot checks
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // Using a standard Chrome user agent helps pass Cloudflare
            userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?, 
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url.toString()
                
                // Block known ad domains by returning a blank, empty response
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
