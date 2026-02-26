/*
 * LessonWebViewBridge.kt
 * 
 * PURPOSE: This class acts as a bridge between JavaScript code running in WebView and 
 * native Android (Kotlin) code. It allows JavaScript to communicate with Android and 
 * vice versa.
 * 
 * WHY: WebViews isolate JavaScript from Android for security. This bridge creates a 
 * controlled interface for safe communication.
 * 
 * USAGE: Add this to WebView with: webView.addJavascriptInterface(LessonWebViewBridge(), "Android")
 * Then in JavaScript, call: Android.methodName(params)
 */

package org.oppia.android.app.javascriptbridge

import android.webkit.JavascriptInterface
import android.util.Log

/**
 * Bridge class that exposes Android functionality to JavaScript code running in WebView.
 * All public methods here can be called from JavaScript using the "Android" object.
 */
class LessonWebViewBridge(
    private val onAnswerSubmitted: (answer: String, isCorrect: Boolean) -> Unit,
    private val onProgressUpdated: (progress: Int) -> Unit,
    private val onQuestionChanged: (questionId: String) -> Unit,
    private val onLessonCompleted: (score: Int) -> Unit
) {
    companion object {
        private const val TAG = "LessonWebViewBridge"
    }

    /**
     * Called when user submits an answer in the JavaScript quiz interface.
     * 
     * EXAMPLE from JavaScript:
     * Android.submitAnswer('user_answer_123', true);
     * 
     * @param answer The answer text/value provided by user
     * @param isCorrect Whether the answer is correct (evaluated by JavaScript)
     */
    @JavascriptInterface
    fun submitAnswer(answer: String, isCorrect: Boolean) {
        Log.d(TAG, "Answer submitted: $answer, Correct: $isCorrect")
        onAnswerSubmitted(answer, isCorrect)
    }

    /**
     * Called when quiz progress is updated in JavaScript.
     * 
     * EXAMPLE from JavaScript:
     * Android.updateProgress(50); // 50% complete
     * 
     * @param progress Percentage of lesson completed (0-100)
     */
    @JavascriptInterface
    fun updateProgress(progress: Int) {
        Log.d(TAG, "Progress updated: $progress%")
        onProgressUpdated(progress)
    }

    /**
     * Called when JavaScript advances to a new question.
     * 
     * EXAMPLE from JavaScript:
     * Android.onQuestionChanged('question_5');
     * 
     * @param questionId Unique identifier of the current question
     */
    @JavascriptInterface
    fun onQuestionChanged(questionId: String) {
        Log.d(TAG, "Question changed to: $questionId")
        onQuestionChanged(questionId)
    }

    /**
     * Called when user completes the entire lesson.
     * 
     * EXAMPLE from JavaScript:
     * Android.finishLesson(85); // Score: 85%
     * 
     * @param score Final score percentage (0-100)
     */
    @JavascriptInterface
    fun finishLesson(score: Int) {
        Log.d(TAG, "Lesson completed with score: $score%")
        onLessonCompleted(score)
    }

    /**
     * Utility method to log debug information from JavaScript.
     * Useful for debugging JavaScript code running in Android.
     * 
     * EXAMPLE from JavaScript:
     * Android.logDebug('Variable x is: ' + x);
     * 
     * @param message Debug message from JavaScript
     */
    @JavascriptInterface
    fun logDebug(message: String) {
        Log.d(TAG, "JS Log: $message")
    }

    /**
     * Utility method to log errors from JavaScript.
     * 
     * EXAMPLE from JavaScript:
     * Android.logError('Error occurred: ' + error);
     * 
     * @param errorMessage Error message from JavaScript
     */
    @JavascriptInterface
    fun logError(errorMessage: String) {
        Log.e(TAG, "JS Error: $errorMessage")
    }
}
