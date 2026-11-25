package com.kuzatuvchi.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
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
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.ByteArrayInputStream

class MainActivity : Activity() {

    private val adServers = listOf(
        "ads.google.com", "doubleclick.net", "googlesyndication.com", "adserver",
        "taboola.com", "outbrain.com", "popads.net", "popcash.net", "propellerads.com",
        "onclickads.net", "adsterra.com", "exoclick.com", "realsrv.com", "ad-delivery.net",
        "hilltopads.net", "monetag.com", "tsyndicate.com", "juicyads.com", "mgid.com",
        "scorecardresearch.com", "zedo.com", "adform.net", "bebi.com", "1xbet"
    )

    private lateinit var webView: WebView
    private lateinit var menuContainer: LinearLayout
    private var currentAllowedHost: String = ""

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainContainer = LinearLayout(this)
        mainContainer.orientation = LinearLayout.VERTICAL
        mainContainer.setBackgroundColor(Color.parseColor("#060B12"))

        menuContainer = LinearLayout(this)
        menuContainer.orientation = LinearLayout.VERTICAL
        menuContainer.gravity = Gravity.CENTER_HORIZONTAL
        menuContainer.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        menuContainer.setPadding(60, 100, 60, 50)

        val headerLayout = LinearLayout(this)
        headerLayout.orientation = LinearLayout.VERTICAL
        headerLayout.gravity = Gravity.CENTER
        headerLayout.setPadding(0, 0, 0, 80)

        val title = TextView(this)
        title.text = "NEXUS // MANGA"
        title.textSize = 34f
        title.setTextColor(Color.parseColor("#00E5FF"))
        title.setTypeface(null, Typeface.BOLD)
        title.letterSpacing = 0.1f
        headerLayout.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "DOMAIN SHIELD & AD-BLOCK ACTIVE"
        subtitle.textSize = 10f
        subtitle.setTextColor(Color.parseColor("#8899A6"))
        subtitle.letterSpacing = 0.25f
        headerLayout.addView(subtitle)

        menuContainer.addView(headerLayout)

        val mangaSites = mapOf(
            "COMIX.TO" to "https://comix.to/",
            "COMICK.APP" to "https://comick.app",
            "MANGADEX" to "https://mangadex.org",
            "ASURA SCANS" to "https://asuracomic.net",
            "MANGA DISTRICT" to "https://mangadistrict.com",
            "MANGASEE" to "https://mangasee123.com",
            "FLAME COMICS" to "https://flamecomics.com",
            "DRAKE SCANS" to "https://drakescans.com",
            "VOID SCANS" to "https://void-scans.com"
        )

        val scrollView = ScrollView(this)
        scrollView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        scrollView.isScrollbarFadingEnabled = false

        val buttonList = LinearLayout(this)
        buttonList.orientation = LinearLayout.VERTICAL
        scrollView.addView(buttonList)

        var delayCounter = 0L
        for ((siteName, siteUrl) in mangaSites) {
            val btnContainer = LinearLayout(this)
            btnContainer.orientation = LinearLayout.HORIZONTAL
            btnContainer.gravity = Gravity.CENTER_VERTICAL

            val btnBg = GradientDrawable()
            btnBg.setColor(Color.parseColor("#0B131E"))
            btnBg.cornerRadius = 8f
            btnBg.setStroke(3, Color.parseColor("#1C3048"))
            btnContainer.background = btnBg

            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
            params.setMargins(0, 0, 0, 30)
            btnContainer.layoutParams = params
            btnContainer.setPadding(50, 0, 50, 0)

            val accentIcon = TextView(this)
            accentIcon.text = "⚡"
            accentIcon.textSize = 14f
            accentIcon.setPadding(0, 0, 30, 0)
            btnContainer.addView(accentIcon)

            val btnText = TextView(this)
            btnText.text = siteName
            btnText.textSize = 16f
            btnText.setTextColor(Color.WHITE)
            btnText.setTypeface(null, Typeface.BOLD)
            btnText.letterSpacing = 0.1f
            btnContainer.addView(btnText)

            btnContainer.alpha = 0f
            btnContainer.translationX = 100f
            btnContainer.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(400)
                .setStartDelay(delayCounter)
                .start()
            delayCounter += 50

            btnContainer.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                        btnBg.setStroke(4, Color.parseColor("#00E5FF"))
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                        btnBg.setStroke(3, Color.parseColor("#1C3048"))
                    }
                }
                false
            }

            btnContainer.setOnClickListener { openSite(siteUrl) }
            buttonList.addView(btnContainer)
        }

        menuContainer.addView(scrollView)

        webView = WebView(this)
        webView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webView.visibility = View.GONE

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            useWideViewPort = true
            loadWithOverviewMode = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
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
                if (!url.startsWith("http://") && !url.startsWith("https://")) return true

                val targetHost = request?.url?.host?.lowercase()?.removePrefix("www.") ?: ""
                val isSameSite = currentAllowedHost.isNotEmpty() &&
                    (targetHost == currentAllowedHost || targetHost.endsWith(".$currentAllowedHost"))
                val isCloudflare = targetHost.contains("cloudflare.com")

                if (!isSameSite && !isCloudflare) {
                    Toast.makeText(this@MainActivity, "🛡️ Ad Redirect Blocked", Toast.LENGTH_SHORT).show()
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val adKillerJs = """
                    (function() {
                        window.open = function() { return null; };
                        var style = document.createElement('style');
                        style.innerHTML = 'iframe:not([src*="cloudflare"]), ins, .adsbygoogle, [id^="ad_"], [id^="ads_"], [class*="ad-banner"], [class*="popup"], div[style*="z-index: 2147483647"], div[style*="z-index: 99999"] { display: none !important; opacity: 0 !important; pointer-events: none !important; width: 0 !important; height: 0 !important; }';
                        document.head.appendChild(style);
                    })();
                """.trimIndent()
                view?.evaluateJavascript(adKillerJs, null)
            }
        }

        mainContainer.addView(menuContainer)
        mainContainer.addView(webView)
        setContentView(mainContainer)
    }

    private fun openSite(url: String) {
        val host = Uri.parse(url).host?.lowercase() ?: ""
        currentAllowedHost = host.removePrefix("www.")

        menuContainer.animate()
            .alpha(0f)
            .translationX(-200f)
            .setDuration(350)
            .withEndAction { menuContainer.visibility = View.GONE }
            .start()

        webView.alpha = 0f
        webView.translationX = 200f
        webView.visibility = View.VISIBLE
        webView.loadUrl(url)

        webView.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(400)
            .setStartDelay(100)
            .start()
    }

    override fun onBackPressed() {
        if (webView.visibility == View.VISIBLE) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                currentAllowedHost = ""
                webView.animate()
                    .alpha(0f)
                    .translationX(200f)
                    .setDuration(350)
                    .withEndAction {
                        webView.visibility = View.GONE
                        webView.loadUrl("about:blank")

                        menuContainer.visibility = View.VISIBLE
                        menuContainer.translationX = -200f
                        menuContainer.animate()
                            .alpha(1f)
                            .translationX(0f)
                            .setDuration(400)
                            .start()
                    }.start()
            }
        } else {
            super.onBackPressed()
        }
    }
}
