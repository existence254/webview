package com.kuzatuvchi.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.ByteArrayInputStream

class MainActivity : Activity() {

    private val adServers = listOf(
        "ads.google.com", "doubleclick.net", "adserver", "taboola", "outbrain",
        "popads.net", "popcash.net", "propellerads.com", "onclickads.net",
        "adsterra.com", "exoclick.com", "realsrv.com", "ad-delivery.net",
        "syndication", "adnetwork", "track", "analytics", "scorecardresearch", 
        "zedo", "adform", "bebi.com", "wix.com", "redirect", "click", "popunder"
    )
    
    private lateinit var webView: WebView
    private lateinit var menuContainer: LinearLayout

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Sleeker Dark Cinematic Background
        val mainContainer = LinearLayout(this)
        mainContainer.orientation = LinearLayout.VERTICAL
        val mainGradient = GradientDrawable(
            GradientDrawable.Orientation.BR_TL,
            intArrayOf(Color.parseColor("#091014"), Color.parseColor("#1b2838"), Color.parseColor("#2a475e"))
        )
        mainContainer.background = mainGradient

        menuContainer = LinearLayout(this)
        menuContainer.gravity = Gravity.CENTER
        menuContainer.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val glassCard = LinearLayout(this)
        glassCard.orientation = LinearLayout.VERTICAL
        glassCard.gravity = Gravity.CENTER
        
        val glassBackground = GradientDrawable()
        glassBackground.setColor(Color.parseColor("#1AFFFFFF")) // Thinner, more premium frosted glass
        glassBackground.cornerRadius = 80f
        glassBackground.setStroke(2, Color.parseColor("#33FFFFFF"))
        glassCard.background = glassBackground
        glassCard.setPadding(60, 90, 60, 90)
        
        val cardParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(70, 0, 70, 0)
        glassCard.layoutParams = cardParams

        val title = TextView(this)
        title.text = "Manga Hub"
        title.textSize = 36f
        title.setTextColor(Color.WHITE)
        title.setPadding(0, 0, 0, 60)
        title.gravity = Gravity.CENTER
        glassCard.addView(title)

        val mangaSites = mapOf(
            "Comix.to" to "https://comix.to/",
            "Comick" to "https://comick.app",
            "MangaDex" to "https://mangadex.org",
            "Asura Scans" to "https://asuracomic.net",
            "Manga District" to "https://mangadistrict.com",
            "MangaSee" to "https://mangasee123.com",
            "Flame Comics" to "https://flamecomics.com",
            "Drake Scans" to "https://drakescans.com",
            "Void Scans" to "https://void-scans.com"
        )

        val scrollView = ScrollView(this)
        scrollView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 900)
        scrollView.isScrollbarFadingEnabled = false
        
        val buttonList = LinearLayout(this)
        buttonList.orientation = LinearLayout.VERTICAL
        scrollView.addView(buttonList)

        for ((siteName, siteUrl) in mangaSites) {
            val btn = Button(this)
            btn.text = siteName
            btn.textSize = 16f
            btn.setTextColor(Color.WHITE)
            btn.isAllCaps = false
            
            val btnBg = GradientDrawable()
            btnBg.setColor(Color.parseColor("#44000000"))
            btnBg.cornerRadius = 40f
            btnBg.setStroke(2, Color.parseColor("#44FFFFFF"))
            btn.background = btnBg
            
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
            params.setMargins(0, 15, 0, 15)
            btn.layoutParams = params
            
            // Interactive Press Animation (Shrinks when touched)
            btn.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.92f).scaleY(0.92f).setDuration(100).start()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
                false
            }
            
            btn.setOnClickListener { openSite(siteUrl) }
            buttonList.addView(btn)
        }

        glassCard.addView(scrollView)
        menuContainer.addView(glassCard)

        webView = WebView(this)
        webView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webView.visibility = View.GONE
        
        // --- PERFORMANCE AND ANTI-LAG OPTIMIZATIONS ---
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null) // Forces GPU hardware acceleration
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true // Allows sites to cache image data locally
            cacheMode = WebSettings.LOAD_DEFAULT // Uses cache when available for speed
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE // Fixes broken images
            
            setSupportMultipleWindows(false) 
            javaScriptCanOpenWindowsAutomatically = false 
            userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }
        
        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val url = request?.url.toString()
                if (adServers.any { url.contains(it, ignoreCase = true) }) {
                    return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.startsWith("http://") || url.startsWith("https://")) return false 
                return true 
            }
        }

        mainContainer.addView(menuContainer)
        mainContainer.addView(webView)
        setContentView(mainContainer)
    }

    // --- CREATIVE TRANSITIONS ---
    private fun openSite(url: String) {
        // Zoom out the menu
        menuContainer.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(450)
            .withEndAction { menuContainer.visibility = View.GONE }.start()

        // Slide the web view up from the bottom
        webView.alpha = 0f
        webView.translationY = 300f 
        webView.visibility = View.VISIBLE
        webView.loadUrl(url)
        
        webView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(100)
            .start()
    }

    override fun onBackPressed() {
        if (webView.visibility == View.VISIBLE) {
            if (webView.canGoBack()) {
                webView.goBack() 
            } else {
                // Slide web view down and fade out
                webView.animate()
                    .alpha(0f)
                    .translationY(300f)
                    .setDuration(400)
                    .withEndAction {
                        webView.visibility = View.GONE
                        webView.loadUrl("about:blank")
                        
                        // Zoom the menu back in
                        menuContainer.visibility = View.VISIBLE
                        menuContainer.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(450)
                            .start()
                    }.start()
            }
        } else {
            super.onBackPressed() 
        }
    }
}
