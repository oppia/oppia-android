/*
 * SimpleLessonActivity.kt
 * 
 * PURPOSE: Simple Activity that loads and displays an HTML lesson in WebView.
 * This is the minimum code needed to make JavaScript-Android communication work.
 * 
 * STEPS:
 * 1. Create WebView
 * 2. Enable JavaScript
 * 3. Add JavaScript bridge
 * 4. Load HTML from assets
 * 5. Listen for messages from JavaScript
 */

package org.oppia.android.app.javascriptbridge

import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SimpleLessonActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create WebView
        val webView = WebView(this)
        setContentView(webView)
        
        // === STEP 1: Enable JavaScript ===
        webView.settings.javaScriptEnabled = true
        
        // === STEP 2: Create Bridge and Add to WebView ===
        val bridge = SimpleJavaScriptBridge { message ->
            // This is called when JavaScript calls Android.showMessage()
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        webView.addJavascriptInterface(bridge, "Android")
        
        // === STEP 3: Load HTML Lesson ===
        val htmlContent = """
            <html>
            <body style="font-family: Arial; text-align: center; padding: 20px;">
                <h1>Welcome to Quiz</h1>
                <p>What is 2 + 2?</p>
                <input type="text" id="answer" placeholder="Your Answer">
                <button onclick="submit()">Submit</button>
                
                <script>
                    function submit() {
                        var answer = document.getElementById('answer').value;
                        if (answer == '4') {
                            Android.showMessage('Correct! Score: 100%');
                            Android.submitScore(100);
                        } else {
                            Android.showMessage('Wrong! Try again');
                        }
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
        
        webView.loadData(htmlContent, "text/html", "UTF-8")
    }
}
