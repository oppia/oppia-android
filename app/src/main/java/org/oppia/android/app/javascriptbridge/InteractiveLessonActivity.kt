/*
 * InteractiveLessonActivity.kt
 * 
 * PURPOSE: Activity that hosts WebView-based interactive lessons.
 * Demonstrates how to use LessonWebViewBridge and LessonWebViewManager.
 * 
 * WHAT THIS DOES:
 * 1. Sets up a WebView with proper security settings
 * 2. Loads HTML/JavaScript lessons from assets
 * 3. Listens for events from JavaScript via the bridge
 * 4. Updates Android UI based on lesson progress
 * 5. Stores completion data for later retrieval
 * 
 * FLOW:
 * User starts activity → WebView loads HTML lesson → JavaScript runs in WebView →
 * User interacts → JavaScript calls Android methods → Activity updates UI + stores data
 */

package org.oppia.android.app.javascriptbridge

import android.os.Bundle
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

/**
 * Activity for displaying and managing interactive JavaScript-based lessons.
 * 
 * USAGE:
 * Starting this activity:
 * val intent = Intent(context, InteractiveLessonActivity::class.java).apply {
 *     putExtra("lesson_name", "algebra_quiz.html")
 *     putExtra("lesson_title", "Algebra Quiz")
 * }
 * startActivity(intent)
 */
class InteractiveLessonActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "InteractiveLessonActivity"
        
        // Intent keys for passing data to this activity
        const val EXTRA_LESSON_NAME = "lesson_name"
        const val EXTRA_LESSON_TITLE = "lesson_title"
    }

    // UI Components
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var statusText: TextView

    // Lesson data
    private var lessonName: String = ""
    private var lessonTitle: String = ""
    private var lessonScore: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interactive_lesson)

        Log.d(TAG, "InteractiveLessonActivity created")

        // Get lesson info from intent
        lessonName = intent.getStringExtra(EXTRA_LESSON_NAME) ?: "algebra_quiz.html"
        lessonTitle = intent.getStringExtra(EXTRA_LESSON_TITLE) ?: "Interactive Lesson"

        // Set activity title
        supportActionBar?.title = lessonTitle

        // Initialize UI components
        initializeUI()

        // Set up WebView
        setupWebView()

        // Load the lesson
        loadLesson()
    }

    /**
     * Initializes UI components from the layout XML.
     * Maps Kotlin variables to XML layout elements.
     */
    private fun initializeUI() {
        Log.d(TAG, "Initializing UI components")

        webView = findViewById(R.id.lesson_webview)
        progressBar = findViewById(R.id.lesson_progress_bar)
        progressText = findViewById(R.id.lesson_progress_text)
        statusText = findViewById(R.id.lesson_status_text)

        // Set initial values
        progressBar.progress = 0
        progressText.text = "0%"
        statusText.text = "Loading lesson..."
    }

    /**
     * Configures the WebView with proper security and JavaScript settings.
     * Creates the bridge that enables JavaScript-Android communication.
     */
    private fun setupWebView() {
        Log.d(TAG, "Setting up WebView")

        // Create the WebView manager
        val webViewManager = LessonWebViewManager(this)

        // Create the bridge with callbacks for JavaScript events
        val bridge = LessonWebViewBridge(
            onAnswerSubmitted = { answer, isCorrect ->
                handleAnswerSubmitted(answer, isCorrect)
            },
            onProgressUpdated = { progress ->
                handleProgressUpdated(progress)
            },
            onQuestionChanged = { questionId ->
                handleQuestionChanged(questionId)
            },
            onLessonCompleted = { score ->
                handleLessonCompleted(score)
            }
        )

        // Configure WebView with security settings
        webViewManager.setupWebView(webView, bridge)

        Log.d(TAG, "WebView configured successfully")
    }

    /**
     * Loads the HTML lesson into the WebView.
     * Constructs the proper asset URL and loads it.
     */
    private fun loadLesson() {
        Log.d(TAG, "Loading lesson: $lessonName")

        val webViewManager = LessonWebViewManager(this)
        val lessonUrl = webViewManager.getAssetUrl("lessons/$lessonName")

        try {
            webView.loadUrl(lessonUrl)
            statusText.text = "Lesson loaded"
        } catch (e: Exception) {
            Log.e(TAG, "Error loading lesson: ${e.message}")
            statusText.text = "Error loading lesson"
            Toast.makeText(this, "Error loading lesson", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Called when user submits an answer from the JavaScript quiz.
     * This is triggered by: Android.submitAnswer(answer, isCorrect)
     * 
     * @param answer The answer text/value
     * @param isCorrect Whether it was correct
     */
    private fun handleAnswerSubmitted(answer: String, isCorrect: Boolean) {
        Log.d(TAG, "Answer submitted - Answer: $answer, Correct: $isCorrect")

        // Run on UI thread
        runOnUiThread {
            if (isCorrect) {
                statusText.text = "✓ Correct!"
                Toast.makeText(this, "Great job!", Toast.LENGTH_SHORT).show()
            } else {
                statusText.text = "✗ Try again"
            }
        }

        // TODO: Store answer in database for later analysis
        // This could be implemented using Room or other persistence layer
    }

    /**
     * Called when JavaScript updates the progress bar.
     * This is triggered by: Android.updateProgress(progress)
     * 
     * @param progress Percentage of lesson completed (0-100)
     */
    private fun handleProgressUpdated(progress: Int) {
        Log.d(TAG, "Progress updated: $progress%")

        // Run on UI thread to update UI
        runOnUiThread {
            progressBar.progress = progress
            progressText.text = "$progress%"
        }

        // TODO: You could trigger additional logic here, like:
        // - Unlock achievements for certain progress levels
        // - Show motivational messages at milestones
        // - Send analytics events
    }

    /**
     * Called when JavaScript advances to a new question.
     * This is triggered by: Android.onQuestionChanged(questionId)
     * 
     * @param questionId The ID of the current question
     */
    private fun handleQuestionChanged(questionId: String) {
        Log.d(TAG, "Question changed to: $questionId")

        // Run on UI thread
        runOnUiThread {
            statusText.text = "Question: $questionId"
        }

        // TODO: Additional logic when question changes:
        // - Log question view analytics
        // - Prepare hint system
        // - Reset timer if question has time limit
    }

    /**
     * Called when user completes the entire lesson.
     * This is triggered by: Android.finishLesson(score)
     * 
     * @param score Final score percentage (0-100)
     */
    private fun handleLessonCompleted(score: Int) {
        Log.d(TAG, "Lesson completed with score: $score%")

        lessonScore = score

        // Run on UI thread
        runOnUiThread {
            progressBar.progress = 100
            progressText.text = "100%"
            statusText.text = "Lesson completed! Score: $score%"

            Toast.makeText(
                this,
                "Congratulations! You scored $score%",
                Toast.LENGTH_LONG
            ).show()

            // TODO: After completion, you might want to:
            // 1. Save the score to the database
            // 2. Update user profile/progress
            // 3. Show certificate or achievement
            // 4. Navigate to next lesson
        }

        // Save completion data
        saveLessonCompletion(score)
    }

    /**
     * Saves lesson completion data to storage.
     * This is where you'd integrate with your database/persistence layer.
     * 
     * @param score The final score
     */
    private fun saveLessonCompletion(score: Int) {
        Log.d(TAG, "Saving lesson completion - Lesson: $lessonName, Score: $score")

        // TODO: Implement database storage
        // Example using Room:
        /*
        val completion = LessonCompletion(
            lessonId = lessonName,
            lessonTitle = lessonTitle,
            score = score,
            completedAt = System.currentTimeMillis()
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            lessonDao.insertCompletion(completion)
        }
        */

        // For now, just log it
        Log.i(TAG, "Lesson completion saved successfully")
    }

    /**
     * Cleanup when activity is destroyed.
     */
    override fun onDestroy() {
        Log.d(TAG, "Destroying InteractiveLessonActivity")

        // Clean up WebView
        webView.destroy()

        super.onDestroy()
    }

    /**
     * Handle back button - confirm if lesson in progress
     */
    override fun onBackPressed() {
        Log.d(TAG, "Back button pressed")

        if (progressBar.progress < 100) {
            // Show confirmation dialog if lesson not complete
            android.app.AlertDialog.Builder(this)
                .setTitle("Exit Lesson?")
                .setMessage("You haven't completed this lesson. Exit anyway?")
                .setPositiveButton("Exit") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("Continue") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }
}
