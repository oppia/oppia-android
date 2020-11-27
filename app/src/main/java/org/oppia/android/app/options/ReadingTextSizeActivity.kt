package org.oppia.android.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity to change the text size of the reading content in the app. */
class ReadingTextSizeActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var readingTextSizeActivityPresenter: ReadingTextSizeActivityPresenter
  private lateinit var prefKey: String
  private lateinit var prefSummaryValue: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    prefKey = intent.getStringExtra(KEY_READING_TEXT_SIZE_PREFERENCE_TITLE)
    prefSummaryValue = (
      if (savedInstanceState != null) {
        savedInstanceState.get(KEY_READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE)
      } else {
        intent.getStringExtra(KEY_READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE)
      }
      ) as String
    readingTextSizeActivityPresenter.handleOnCreate(prefSummaryValue)
  }

  companion object {
    internal const val KEY_READING_TEXT_SIZE_PREFERENCE_TITLE = "READING_TEXT_SIZE_PREFERENCE"
    internal const val KEY_READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE =
      "READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE"
    /** Returns a new [Intent] to route to [ReadingTextSizeActivity]. */
    fun createReadingTextSizeActivityIntent(
      context: Context,
      prefKey: String,
      summaryValue: String?
    ): Intent {
      val intent = Intent(context, ReadingTextSizeActivity::class.java)
      intent.putExtra(KEY_READING_TEXT_SIZE_PREFERENCE_TITLE, prefKey)
      intent.putExtra(KEY_READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE, summaryValue)
      return intent
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(
      KEY_READING_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE,
      readingTextSizeActivityPresenter.getSelectedReadingTextSize()
    )
  }

  override fun onBackPressed() {
    val message = readingTextSizeActivityPresenter.getSelectedReadingTextSize()
    val intent = Intent()
    intent.putExtra(MESSAGE_READING_TEXT_SIZE_ARGUMENT_KEY, message)
    setResult(REQUEST_CODE_TEXT_SIZE, intent)
    finish()
  }
}
