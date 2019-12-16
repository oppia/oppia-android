package org.oppia.app.option

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

const val PREFERENCE_SUMMARY_VALUE = "PREFERENCE_SUMMARY_VALUE"

/** The activity to change the Text size of the Story content in the app. */
class StoryTextSizeActivity : InjectableAppCompatActivity() {

  var pref_summary_value: String = ""
  @Inject
  lateinit var storyTextSizeActivityPresenter: StoryTextSizeActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    pref_summary_value = intent.getStringExtra(PREFERENCE_SUMMARY_VALUE)
    storyTextSizeActivityPresenter.handleOnCreate(pref_summary_value)
  }

  companion object {
    /** Returns a new [Intent] to route to [StoryTextSizeActivity]. */
    fun createStoryTextSizeActivityIntent(
      context: Context,
      summaryValue: String
    ): Intent {
      val intent = Intent(context, StoryTextSizeActivity::class.java)
      intent.putExtra(PREFERENCE_SUMMARY_VALUE,summaryValue)
      return intent
    }
  }
}
