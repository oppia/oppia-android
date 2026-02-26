/*
 * ============================================
 * QUICK REFERENCE CARD
 * ============================================
 * 
 * Save this and refer to it when adding new features!
 */

// ============================================
// 1. CREATING THE BRIDGE
// ============================================

// Simple Pattern:
@JavascriptInterface
fun methodName(parameter: String) {
    // Do something with the data
}

// Examples:
@JavascriptInterface
fun showMessage(msg: String) { }     // Takes String

@JavascriptInterface
fun submitScore(score: Int) { }      // Takes Int

@JavascriptInterface
fun finishLesson() { }               // Takes nothing


// ============================================
// 2. ADDING TO WEBVIEW
// ============================================

val webView = WebView(this)

// Enable JavaScript (REQUIRED!)
webView.settings.javaScriptEnabled = true

// Add Bridge
val bridge = MyBridge()
webView.addJavascriptInterface(bridge, "Android")
// ☝ Remember: "Android" is the name used in JavaScript


// ============================================
// 3. LOADING CONTENT
// ============================================

// Option A: Load from file
webView.loadUrl("file:///android_asset/lesson.html")

// Option B: Load from string
val html = "<html><body>Hello</body></html>"
webView.loadData(html, "text/html", "UTF-8")


// ============================================
// 4. CALLING FROM JAVASCRIPT
// ============================================

// In HTML/JavaScript file:
Android.methodName(parameter);  // Important: semicolon and parentheses

// Examples:
Android.showMessage("Hello Android");
Android.submitScore(100);
Android.finishLesson();


// ============================================
// 5. GETTING DATA BACK TO ACTIVITY
// ============================================

// Option A: Using callback
val bridge = MyBridge { data ->
    // This runs when JavaScript sends data
    Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
}

// Option B: Using global variable
var lastScore = 0
@JavascriptInterface
fun submitScore(score: Int) {
    lastScore = score  // Store globally
}


// ============================================
// 6. COMMON MISTAKES & FIXES
// ============================================

// MISTAKE 1: Forgot @JavascriptInterface
@JavascriptInterface  // ← DON'T FORGET!
fun myMethod(data: String) { }

// MISTAKE 2: JavascriptEnabled = false
webView.settings.javaScriptEnabled = true  // ← MUST BE TRUE!

// MISTAKE 3: Wrong registration name
webView.addJavascriptInterface(bridge, "Android")
// Now use: Android.method() NOT Android123.method()

// MISTAKE 4: Wrong method name in JavaScript
Android.showMessage()  // If method is showMessage
// NOT Android.ShowMessage() - case sensitive!

// MISTAKE 5: Calling Android method on UI thread
runOnUiThread {
    // Do UI operations here
}


// ============================================
// 7. FILE STRUCTURE
// ============================================

project/
├── app/src/main/java/
│   └── org/oppia/android/app/javascriptbridge/
│       ├── SimpleJavaScriptBridge.kt      ← Bridge class
│       └── SimpleLessonActivity.kt        ← Activity class
├── app/src/main/assets/
│   └── simple_quiz.html                   ← HTML lesson
└── app/src/main/res/
    └── AndroidManifest.xml                ← Register Activity


// ============================================
// 8. MANIFEST ENTRY
// ============================================

<activity
    android:name=".javascriptbridge.SimpleLessonActivity"
    android:label="Interactive Lesson" />


// ============================================
// 9. STARTING ACTIVITY
// ============================================

val intent = Intent(this, SimpleLessonActivity::class.java)
startActivity(intent)


// ============================================
// 10. EXAMPLE: ADDING NEW FEATURE
// ============================================

// Step 1: Add method to Bridge
@JavascriptInterface
fun saveUserName(name: String) {
    Log.d("JS", "User: $name")
}

// Step 2: Call from JavaScript
Android.saveUserName("John");

// Step 3: Done! That's all!


// ============================================
// 11. LOGGING FOR DEBUGGING
// ============================================

// In Kotlin:
Log.d("TAG", "Message")

// In JavaScript (goes to Kotlin logs):
console.log("Debug message");

// View logs:
// adb logcat | grep TAG


// ============================================
// 12. SECURITY BEST PRACTICES
// ============================================

// ✓ DO: Validate input data
if (data.isEmpty()) return
if (score < 0 || score > 100) return

// ✓ DO: Use try-catch for errors
try {
    val result = processData(data)
} catch (e: Exception) {
    Log.e("TAG", "Error: ${e.message}")
}

// ✓ DO: Check if Android object exists in JavaScript
if (typeof Android !== 'undefined') {
    Android.method();
}

// ✗ DON'T: Load untrusted JavaScript
// ✗ DON'T: Expose sensitive methods to JavaScript


// ============================================
// 13. TESTING CHECKLIST
// ============================================

// [ ] Can JavaScript call Android methods?
// [ ] Are callback values received correctly?
// [ ] Does UI update after JavaScript call?
// [ ] No crashes in logcat?
// [ ] Works on emulator and real device?
// [ ] Data is saved correctly?


// ============================================
// 14. NEXT FEATURES TO ADD
// ============================================

// You can extend this with:
// ✓ Timer/stopwatch
// ✓ Sound effects
// ✓ Analytics tracking
// ✓ Database storage
// ✓ Achievements/badges
// ✓ Offline support
// ✓ Multiple languages

// All follow the same pattern:
// 1. Add method to bridge
// 2. Call from JavaScript
// 3. Handle in Activity callback
// Done!
