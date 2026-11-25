package com.kuzatuvchi.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
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
    private lateinit var webContainer: FrameLayout
    private var currentAllowedHost: String = ""

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Immersive Mode
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )

        val mainContainer = FrameLayout(this)
        mainContainer.setBackgroundColor(Color.parseColor("#060B12"))

        // --- 1. THE NEXUS HUB (MENU SCREEN) ---
        menuContainer = LinearLayout(this)
        menuContainer.orientation = LinearLayout.VERTICAL
        menuContainer.gravity = Gravity.CENTER_HORIZONTAL
        menuContainer.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        menuContainer.setPadding(60, 100, 60, 50)

        val headerLayout = LinearLayout(this)
        headerLayout.orientation = LinearLayout.VERTICAL
        headerLayout.gravity = Gravity.CENTER
        headerLayout.setPadding(0, 0, 0, 60)

        val title = TextView(this)
        title.text = "NEXUS // MANGA"
        title.textSize = 34f
        title.setTextColor(Color.parseColor("#00E5FF"))
        title.setTypeface(null, Typeface.BOLD)
        title.letterSpacing = 0.1f
        headerLayout.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "UNIVERSAL SEARCH ACTIVE"
        subtitle.textSize = 10f
        subtitle.setTextColor(Color.parseColor("#8899A6"))
        subtitle.letterSpacing = 0.25f
        headerLayout.addView(subtitle)

        menuContainer.addView(headerLayout)

        // --- NEW: CYBERPUNK SEARCH BAR ---
        val searchContainer = LinearLayout(this)
        searchContainer.orientation = LinearLayout.HORIZONTAL
        searchContainer.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, 50) }

        val searchInput = EditText(this)
        searchInput.layoutParams = LinearLayout.LayoutParams(0, 140, 1f)
        searchInput.hint = "Enter URL or Search Manga..."
        searchInput.setHintTextColor(Color.parseColor("#556677"))
        searchInput.setTextColor(Color.WHITE)
        searchInput.setPadding(40, 0, 40, 0)
        searchInput.setSingleLine(true)
        searchInput.imeOptions = EditorInfo.IME_ACTION_GO
        
        val searchBg = GradientDrawable()
        searchBg.setColor(Color.parseColor("#0B131E"))
        searchBg.cornerRadius = 8f
        searchBg.setStroke(2, Color.parseColor("#1C3048"))
        searchInput.background = searchBg

        val searchBtn = TextView(this)
        searchBtn.text = "GO"
        searchBtn.textSize = 16f
        searchBtn.setTextColor(Color.parseColor("#00E5FF"))
        searchBtn.setTypeface(null, Typeface.BOLD)
        searchBtn.gravity = Gravity.CENTER
        
        val searchBtnBg = GradientDrawable()
        searchBtnBg.setColor(Color.parseColor("#060B12"))
        searchBtnBg.cornerRadius = 8f
        searchBtnBg.setStroke(2, Color.parseColor("#00E5FF"))
        searchBtn.background = searchBtnBg
        
        val searchBtnParams = LinearLayout.LayoutParams(140, 140)
        searchBtnParams.setMargins(20, 0, 0, 0)
        searchBtn.layoutParams = searchBtnParams

        searchContainer.addView(searchInput)
        searchContainer.addView(searchBtn)
        menuContainer.addView(searchContainer)

        // Logic to execute search and hide keyboard
        val executeSearch = {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                // Hide Android Keyboard
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
                
                // Determine if URL or Google Search
                val finalUrl = if (query.startsWith("http://") || query.startsWith("https://")) {
                    query
                } else if (query.contains(".") && !query.contains(" ")) {
                    "https://$query"
                } else {
                    "https://www.google.com/search?q=${Uri.encode(query)}"
                }
                openSite(finalUrl)
                searchInput.text.clear()
            }
        }

        searchBtn.setOnClickListener { executeSearch() }
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                executeSearch()
                true
            } else false
        }

        // --- QUICK ACCESS BUTTONS ---
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

        // --- 2. WEB READER SCREEN (WITH FLOATING MENU BUTTON) ---
        webContainer = FrameLayout(this)
        webContainer.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webContainer.visibility = View.GONE

        webView = WebView(this)
        webView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        val floatingMenuBtn = TextView(this)
        floatingMenuBtn.text = "✦ MENU"
        floatingMenuBtn.textSize = 12f
        floatingMenuBtn.setTextColor(Color.parseColor("#00E5FF"))
        floatingMenuBtn.setTypeface(null, Typeface.BOLD)
        floatingMenuBtn.letterSpacing = 0.1f
        
        val fabBg = GradientDrawable()
        fabBg.setColor(Color.parseColor("#CC060B12")) 
        fabBg.cornerRadius = 50f
        fabBg.setStroke(2, Color.parseColor("#00E5FF"))
        floatingMenuBtn.background = fabBg
        floatingMenuBtn.setPadding(50, 25, 50, 25)
        
        val fabParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        fabParams.gravity = Gravity.BOTTOM or Gravity.END 
        fabParams.setMargins(0, 0, 60, 60)
        floatingMenuBtn.layoutParams = fabParams

        floatingMenuBtn.setOnClickListener { returnToMenu() }
        
        floatingMenuBtn.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }

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
                val isGoogleSearch = currentAllowedHost == "google.com"

                if (!isSameSite && !isCloudflare) {
                    // NEW: If you click a link inside Google, allow the jump and lock onto the new site!
                    if (isGoogleSearch) {
                        currentAllowedHost = targetHost
                        return false
                    }
                    Toast.makeText(this@MainActivity, "🛡️ Ad Redirect Blocked", Toast.LENGTH_SHORT).show()
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val immersiveJs = """
                    (function() {
                        window.open = function() { return null; };
                        var style = document.createElement('style');
                        style.innerHTML = `
                            iframe:not([src*="cloudflare"]), ins, .adsbygoogle, [id^="ad_"], [id^="ads_"], [class*="ad-banner"], [class*="popup"], div[style*="z-index: 2147483647"] { display: none !important; opacity: 0 !important; pointer-events: none !important; width: 0 !important; height: 0 !important; }
                            header, footer, nav, .navbar, .header, .footer, #comments, .comments-area, #disqus_thread, .sidebar { display: none !important; }
                            body, html { background-color: #000000 !important; color: #555 !important; }
                            .reading-content, .page-break, .viewer-dir, .container { max-width: 100% !important; width: 100% !important; margin: 0 !important; padding: 0 !important; }
                        `;
                        document.head.appendChild(style);
                    })();
                """.trimIndent()
                view?.evaluateJavascript(immersiveJs, null)
            }
        }

        webContainer.addView(webView)
        webContainer.addView(floatingMenuBtn) 

        mainContainer.addView(menuContainer)
        mainContainer.addView(webContainer)
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

        webContainer.alpha = 0f
        webContainer.translationX = 200f
        webContainer.visibility = View.VISIBLE
        webView.loadUrl(url)

        webContainer.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(400)
            .setStartDelay(100)
            .start()
    }

    private fun returnToMenu() {
        currentAllowedHost = ""
        webContainer.animate()
            .alpha(0f)
            .translationX(200f)
            .setDuration(350)
            .withEndAction {
                webContainer.visibility = View.GONE
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

    override fun onBackPressed() {
        if (webContainer.visibility == View.VISIBLE) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                returnToMenu()
            }
        } else {
            super.onBackPressed()
        }
    }
}
