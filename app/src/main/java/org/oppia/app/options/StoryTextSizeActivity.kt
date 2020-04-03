package org.oppia.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

private const val KEY_STORY_TEXT_SIZE_PREFERENCE_TITLE = "STORY_TEXT_SIZE_PREFERENCE"
private const val KEY_STORY_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE =
  "STORY_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE"

/** The activity to change the Text size of the Story content in the app. */
class StoryTextSizeActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var storyTextSizeActivityPresenter: StoryTextSizeActivityPresenter
  private lateinit var prefKey: String
  private lateinit var prefSummaryValue: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    prefKey = intent.getStringExtra(KEY_STORY_TEXT_SIZE_PREFERENCE_TITLE)
    prefSummaryValue = (if (savedInstanceState != null) {
      savedInstanceState.get(KEY_STORY_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE)
    } else {
      intent.getStringExtra(KEY_STORY_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE)
    }) as String
    storyTextSizeActivityPresenter.handleOnCreate(prefSummaryValue)
  }

  companion object {
    /** Returns a new [Intent] to route to [StoryTextSizeActivity]. */
    fun createStoryTextSizeActivityIntent(
      context: Context,
      prefKey: String,
      summaryValue: String?
    ): Intent {
      val intent = Intent(context, StoryTextSizeActivity::class.java)
      intent.putExtra(KEY_STORY_TEXT_SIZE_PREFERENCE_TITLE, prefKey)
      intent.putExtra(KEY_STORY_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE, summaryValue)
      return intent
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(
      KEY_STORY_TEXT_SIZE_PREFERENCE_SUMMARY_VALUE,
      storyTextSizeActivityPresenter.geSelectedStoryTextSize()
    )
  }

  override fun onBackPressed() {
    val message = storyTextSizeActivityPresenter.geSelectedStoryTextSize()
    val intent = Intent()
    intent.putExtra(KEY_MESSAGE_STORY_TEXT_SIZE, message)
    setResult(REQUEST_CODE_TEXT_SIZE, intent)
    finish()
  }
}
