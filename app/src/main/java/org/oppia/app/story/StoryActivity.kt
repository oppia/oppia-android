package org.oppia.app.story

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity for stories */
class StoryActivity : InjectableAppCompatActivity() {
  @Inject lateinit var storyActivityController: StoryActivityController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    storyActivityController.handleOnCreate()
  }
}
