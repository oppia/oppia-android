/*
 * LessonWebViewManager.kt
 * 
 * PURPOSE: Manages WebView configuration and setup for displaying interactive lessons.
 * Handles all the complex WebView setup in one centralized place.
 * 
 * WHY: WebView requires many security and performance settings. This manager
 * encapsulates best practices and keeps code DRY (Don't Repeat Yourself).
 * 
 * USAGE: Create an instance and call setupWebView() to configure a WebView for lessons
 */

package org.oppia.android.app.javascriptbridge

import android.webkit.WebView
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.util.Log
import android.content.Context

/**
 * Manages WebView setup and configuration for interactive lessons.
 * Applies security settings, JavaScript bridge, and optimizations.
 */
class LessonWebViewManager(private val context: Context) {
    companion object {
        private const val TAG = "LessonWebViewManager"
    }

    /**
     * Configures a WebView for displaying interactive lessons with JavaScript support.
     * 
     * WHAT THIS DOES:
     * 1. Enables JavaScript (required for interactivity)
     * 2. Sets up file access for offline content
     * 3. Configures caching to work offline
     * 4. Disables dangerous features
     * 5. Adds the JavaScript-Android bridge
     * 
     * @param webView The WebView to configure
     * @param bridge The JavaScript bridge for Android communication
     */
    fun setupWebView(webView: WebView, bridge: LessonWebViewBridge) {
        Log.d(TAG, "Setting up WebView for interactive lessons")
        
        // Access and configure WebSettings
        val webSettings: WebSettings = webView.settings.apply {
            
            // === JAVASCRIPT SETUP ===
            javaScriptEnabled = true  // Enable JavaScript for interactive content
            javaScriptCanOpenWindowsAutomatically = false  // Security: prevent pop-ups
            
            // === CACHING & OFFLINE SUPPORT ===
            // Allow WebView to cache files for offline access
            cacheMode = WebSettings.LOAD_DEFAULT
            domStorageEnabled = true  // Enable DOM Storage API
            databaseEnabled = true     // Enable database API
            
            // === FILE ACCESS ===
            // Allow access to assets folder (where we store HTML/JS lessons)
            allowFileAccess = true
            allowContentAccess = true
            
            // === PERFORMANCE ===
            // Improve rendering performance
            loadWithOverviewMode = true
            useWideViewPort = true
            
            // === SECURITY ===
            // Disable features that could be exploited
            allowFileAccessFromFileURLs = false  // Security: block local file access
            allowUniversalAccessFromFileURLs = false  // Security: prevent XSS
            builtInZoomControls = true  // User can zoom
            displayZoomControls = false  // Hide zoom buttons (Android handles this)
            
            // === USER AGENT ===
            // Identify as mobile for proper rendering
            userAgentString = userAgentString + " OppiaAndroid/1.0"
        }
        
        // Add the JavaScript bridge
        // This makes the "Android" object available in JavaScript
        webView.addJavascriptInterface(bridge, "Android")
        
        // Set up WebViewClient to handle navigation and errors
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                Log.d(TAG, "Page started loading: $url")
                super.onPageStarted(view, url, favicon)
            }
            
            override fun onPageFinished(view: WebView, url: String) {
                Log.d(TAG, "Page finished loading: $url")
                super.onPageFinished(view, url)
            }
            
            override fun onReceivedError(
                view: WebView,
                request: android.webkit.WebResourceRequest,
                error: android.webkit.WebResourceError
            ) {
                Log.e(TAG, "Error loading ${request.url}: ${error.description}")
                super.onReceivedError(view, request, error)
            }
        }
        
        // Set up WebChromeClient for handling console messages
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: String, lineNumber: Int, sourceID: String): Boolean {
                Log.d(TAG, "JS Console [$sourceID:$lineNumber]: $message")
                return true
            }
        }
    }
    
    /**
     * Helper method to load a lesson from the assets folder.
     * 
     * WHAT THIS DOES:
     * Constructs the proper file:// URL to load HTML from assets
     * 
     * EXAMPLE:
     * webView.loadUrl(manager.getAssetUrl("lessons/algebra_quiz.html"))
     * 
     * @param assetPath Relative path in assets folder (e.g., "lessons/quiz.html")
     * @return Full file URL to load in WebView
     */
    fun getAssetUrl(assetPath: String): String {
        return "file:///android_asset/$assetPath"
    }
    
    /**
     * Injects JavaScript code into the WebView after the page loads.
     * Useful for modifying content or adding functionality.
     * 
     * EXAMPLE:
     * manager.injectJavaScript(webView, "console.log('Page loaded!');")
     * 
     * @param webView The WebView to inject into
     * @param javaScriptCode The JavaScript code to execute
     */
    fun injectJavaScript(webView: WebView, javaScriptCode: String) {
        Log.d(TAG, "Injecting JavaScript code")
        webView.evaluateJavascript(javaScriptCode, null)
    }
}
