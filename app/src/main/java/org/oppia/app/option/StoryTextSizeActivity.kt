package org.oppia.app.option

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity to change the Text size of the Story content in the app. */
class StoryTextSizeActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var storyTextSizeActivityPresenter: StoryTextSizeActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    storyTextSizeActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns a new [Intent] to route to [StoryTextSizeActivity]. */
    fun createStoryTextSizeActivityIntent(
      context: Context,
      string: String
    ): Intent {
      val intent = Intent(context, StoryTextSizeActivity::class.java)
      return intent
    }
  }
}
