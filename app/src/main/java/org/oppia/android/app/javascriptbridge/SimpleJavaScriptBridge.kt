/*
 * SimpleJavaScriptBridge.kt
 * 
 * PURPOSE: Simple bridge to communicate between JavaScript and Android.
 * 
 * HOW IT WORKS:
 * 1. JavaScript calls Android methods through the "Android" object
 * 2. Android methods use callbacks to respond
 * 
 * EXAMPLE USAGE IN JAVASCRIPT:
 * Android.showMessage("User completed the quiz!");
 */

package org.oppia.android.app.javascriptbridge

import android.webkit.JavascriptInterface
import android.util.Log

class SimpleJavaScriptBridge(
    private val onMessageReceived: (message: String) -> Unit
) {
    
    /**
     * Called from JavaScript: Android.showMessage("Hello")
     */
    @JavascriptInterface
    fun showMessage(message: String) {
        Log.d("JS_Bridge", message)
        onMessageReceived(message)
    }
    
    /**
     * Called from JavaScript: Android.submitScore(85)
     */
    @JavascriptInterface
    fun submitScore(score: Int) {
        Log.d("JS_Bridge", "Score received: $score")
    }
}
