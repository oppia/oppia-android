/*
 * ============================================
 * SIMPLE EXAMPLE: HOW IT WORKS
 * ============================================
 * 
 * This is a complete minimal example showing JavaScript-Android communication.
 * Copy-paste ready!
 */

// FILE 1: MySimpleBridge.kt
// ========================
package org.oppia.android.app.demo

import android.webkit.JavascriptInterface

class MySimpleBridge(val callback: (String) -> Unit) {
    
    // Called from JavaScript: Android.onResult("answer_123");
    @JavascriptInterface
    fun onResult(result: String) {
        callback(result)  // Send to Activity
    }
}


// FILE 2: MySimpleActivity.kt
// ===========================
package org.oppia.android.app.demo

import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MySimpleActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val webView = WebView(this)
        setContentView(webView)
        
        // Enable JavaScript
        webView.settings.javaScriptEnabled = true
        
        // Create bridge and add to WebView
        val bridge = MySimpleBridge { result ->
            // When JavaScript calls Android.onResult(), this runs
            Toast.makeText(this, "Got: $result", Toast.LENGTH_SHORT).show()
        }
        webView.addJavascriptInterface(bridge, "Android")
        
        // Load HTML
        val html = """
            <html>
            <body style="padding: 20px; text-align: center;">
                <h1>Simple Lesson</h1>
                <button onclick="sendToAndroid()">Click Me</button>
                
                <script>
                    function sendToAndroid() {
                        Android.onResult("User clicked button!");
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
        
        webView.loadData(html, "text/html", "UTF-8")
    }
}


/*
 * ============================================
 * HOW THIS WORKS VISUALLY
 * ============================================
 * 
 * When user clicks the button in HTML:
 * 
 *     HTML Button
 *          ↓
 *     onclick="sendToAndroid()"
 *          ↓
 *     JavaScript function runs
 *          ↓
 *     Android.onResult("User clicked button!")
 *          ↓
 *     Bridge receives the call
 *          ↓
 *     callback(result) is called
 *          ↓
 *     Toast shows: "Got: User clicked button!"
 * 
 * That's it! Very simple.
 */


// ============================================
// WHAT TO CHANGE TO MAKE YOUR OWN
// ============================================

// 1. Change the Bridge Method Name:
//    From: Android.onResult()
//    To:   Android.submitScore()

// 2. Change the HTML Content:
//    From: "Click Me" button
//    To:   Your quiz question

// 3. Change the Toast Message:
//    From: Toast showing the result
//    To:   Save to database, update UI, etc.

// 4. Add More Methods to Bridge:
//    @JavascriptInterface
//    fun anotherMethod(data: String) {
//        callback(data)
//    }

// ============================================
// COPY-PASTE CHECKLIST
// ============================================

// [ ] 1. Create MySimpleBridge.kt with code above
// [ ] 2. Create MySimpleActivity.kt with code above  
// [ ] 3. Add to AndroidManifest.xml:
//       <activity android:name=".demo.MySimpleActivity" />
// [ ] 4. Start activity from anywhere:
//       startActivity(Intent(this, MySimpleActivity::class.java))
// [ ] 5. Run on device and click button to test

// ============================================
// COMMON ERRORS & FIXES
// ============================================

// Error: "Android is undefined"
// Fix: Add @JavascriptInterface annotation to method
//      Make sure webView.settings.javaScriptEnabled = true

// Error: "Method not found"
// Fix: Make sure webView.addJavascriptInterface(bridge, "Android") is called
//      Check spelling of method name

// Error: "Nothing happens when I click"
// Fix: Check logcat for errors: adb logcat
//      Make sure JavaScript function has correct name

// ============================================
// NEXT STEPS
// ============================================

// 1. Look at SimpleJavaScriptBridge.kt for more examples
// 2. Look at simple_quiz.html for a working quiz
// 3. Modify both files to create your own interactive lesson
// 4. Test on real device for best results
