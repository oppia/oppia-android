package org.oppia.app.story

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for stories. */
class StoryActivity : InjectableAppCompatActivity() {
  @Inject lateinit var storyActivityPresenter: StoryActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    storyActivityPresenter.handleOnCreate()
  }
}
