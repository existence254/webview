package com.kuzatuvchi.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.io.ByteArrayInputStream

class MainActivity : Activity() {

    private val adServers = listOf("ads.google.com", "doubleclick.net", "adserver", "taboola", "outbrain")
    private lateinit var webView: WebView
    private lateinit var menuContainer: LinearLayout

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Main App Background (Deep cinematic gradient)
        val mainContainer = LinearLayout(this)
        mainContainer.orientation = LinearLayout.VERTICAL
        val mainGradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(Color.parseColor("#0f2027"), Color.parseColor("#203a43"), Color.parseColor("#2c5364"))
        )
        mainContainer.background = mainGradient

        // Container to center the glass card perfectly on screen
        menuContainer = LinearLayout(this)
        menuContainer.gravity = Gravity.CENTER
        menuContainer.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // 2. The Frosted Glass Card
        val glassCard = LinearLayout(this)
        glassCard.orientation = LinearLayout.VERTICAL
        glassCard.gravity = Gravity.CENTER
        
        val glassBackground = GradientDrawable()
        glassBackground.setColor(Color.parseColor("#22FFFFFF")) // 13% opaque white for frosted look
        glassBackground.cornerRadius = 60f
        glassBackground.setStroke(3, Color.parseColor("#44FFFFFF")) // Semi-transparent border
        glassCard.background = glassBackground
        glassCard.setPadding(60, 80, 60, 80)
        
        val cardParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(80, 0, 80, 0)
        glassCard.layoutParams = cardParams

        // App Title
        val title = TextView(this)
        title.text = "Manga Hub"
        title.textSize = 32f
        title.setTextColor(Color.WHITE)
        title.setPadding(0, 0, 0, 80)
        title.gravity = Gravity.CENTER
        glassCard.addView(title)

        val mangaSites = mapOf(
            "Comick" to "https://comick.app",
            "MangaDex" to "https://mangadex.org",
            "MangaSee" to "https://mangasee123.com"
        )

        // Glass Buttons
        for ((siteName, siteUrl) in mangaSites) {
            val btn = Button(this)
            btn.text = siteName
            btn.textSize = 16f
            btn.setTextColor(Color.WHITE)
            
            // Button Glass Effect
            val btnBg = GradientDrawable()
            btnBg.setColor(Color.parseColor("#44000000")) // Dark translucent
            btnBg.cornerRadius = 30f
            btnBg.setStroke(2, Color.parseColor("#33FFFFFF"))
            btn.background = btnBg
            
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 140)
            params.setMargins(0, 15, 0, 15)
            btn.layoutParams = params
            
            btn.setOnClickListener { openSite(siteUrl) }
            glassCard.addView(btn)
        }

        menuContainer.addView(glassCard)

        // 3. Ad-Blocking WebView
        webView = WebView(this)
        webView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webView.visibility = View.GONE
        
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val url = request?.url.toString()
                if (adServers.any { url.contains(it, ignoreCase = true) }) {
                    return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
                }
                return super.shouldInterceptRequest(view, request)
            }
        }

        mainContainer.addView(menuContainer)
        mainContainer.addView(webView)
        setContentView(mainContainer)
    }

    // 4. Smooth Fade Animations
    private fun openSite(url: String) {
        // Fade out the glass menu
        menuContainer.animate().alpha(0f).setDuration(400).withEndAction {
            menuContainer.visibility = View.GONE
            
            // Fade in the web reader
            webView.alpha = 0f
            webView.visibility = View.VISIBLE
            webView.loadUrl(url)
            webView.animate().alpha(1f).setDuration(500).start()
        }.start()
    }

    override fun onBackPressed() {
        if (webView.visibility == View.VISIBLE) {
            if (webView.canGoBack()) {
                webView.goBack() 
            } else {
                // Fade out the web reader and bring the glass menu back
                webView.animate().alpha(0f).setDuration(400).withEndAction {
                    webView.visibility = View.GONE
                    webView.loadUrl("about:blank")
                    
                    menuContainer.visibility = View.VISIBLE
                    menuContainer.animate().alpha(1f).setDuration(500).start()
                }.start()
            }
        } else {
            super.onBackPressed() 
        }
    }
}
