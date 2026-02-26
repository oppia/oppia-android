/*
 * HOW TO USE THIS FEATURE - STEP BY STEP GUIDE
 * ============================================
 * 
 * This implementation adds JavaScript-based interactive lessons to Oppia-Android.
 * Here's how to use it in your project.
 * 
 */

// ============================================
// STEP 1: Add Activity to AndroidManifest.xml
// ============================================
// In app/src/main/AndroidManifest.xml, add this inside <application> tag:

/*
    <activity
        android:name=".javascriptbridge.SimpleLessonActivity"
        android:label="Interactive Lesson" />
*/

// ============================================
// STEP 2: Start the Activity from Anywhere
// ============================================
// In any Activity or Fragment, use this code:

package org.oppia.android.app.home  // Example: in home activity

import android.content.Intent
import org.oppia.android.app.javascriptbridge.SimpleLessonActivity

class HomeActivity {
    fun startLesson() {
        val intent = Intent(this, SimpleLessonActivity::class.java)
        startActivity(intent)
    }
}

// ============================================
// STEP 3: Customize HTML Content
// ============================================
// Edit simple_quiz.html to change:
// - Question text: Change "What is 5 × 8?" to your question
// - Correct answer: Change correctAnswer = '40' to your answer
// - Styling: Modify CSS to match your app's theme

// ============================================
// STEP 4: What Each File Does
// ============================================

// SimpleJavaScriptBridge.kt
// - Creates a bridge between JavaScript and Android
// - Has 2 methods: showMessage() and submitScore()
// - When JavaScript calls Android.showMessage(), it gets handled here

// SimpleLessonActivity.kt
// - Creates a WebView
// - Enables JavaScript
// - Adds the bridge
// - Loads HTML content

// simple_quiz.html
// - The actual quiz displayed to user
// - JavaScript calls Android methods when user submits answer

// ============================================
// STEP 5: Add More JavaScript Methods
// ============================================
// If you want to add more communication, do this:

// A) In SimpleJavaScriptBridge.kt, add new method:
/*
    @JavascriptInterface
    fun sendUserName(name: String) {
        Log.d("JS_Bridge", "User name: $name")
    }
*/

// B) In HTML/JavaScript, call it:
/*
    Android.sendUserName("John");
*/

// ============================================
// STEP 6: How the Communication Works
// ============================================

// Data Flow:
// HTML Form ——> JavaScript code ——> Android.methodName() ——> SimpleJavaScriptBridge ——> Activity

// Example:
// 1. User types answer in HTML form
// 2. User clicks "Submit" button
// 3. JavaScript checks if answer is correct
// 4. JavaScript calls: Android.submitScore(100);
// 5. SimpleJavaScriptBridge receives the score
// 6. Activity is notified and can update database/UI

// ============================================
// STEP 7: Common Customizations
// ============================================

// To load HTML from assets instead of hardcoding:
/*
    webView.loadUrl("file:///android_asset/simple_quiz.html")
*/

// To pass data to the lesson:
/*
    val intent = Intent(this, SimpleLessonActivity::class.java)
    intent.putExtra("lesson_type", "math")
    startActivity(intent)
    
    // Get in activity:
    val lessonType = intent.getStringExtra("lesson_type")
*/

// ============================================
// STEP 8: Testing
// ============================================

// Run this on Android device/emulator:
/*
    adb logcat | grep JS_Bridge
*/
// This will show all messages from JavaScript bridge in logcat

// ============================================
// KEY POINTS TO REMEMBER
// ============================================

// 1. @JavascriptInterface annotation makes method accessible from JavaScript
// 2. webView.settings.javaScriptEnabled = true MUST be set
// 3. webView.addJavascriptInterface(bridge, "Android") registers the bridge
// 4. In JavaScript, use Android.methodName() to call Android methods
// 5. Data is passed as String or Int - no complex objects
