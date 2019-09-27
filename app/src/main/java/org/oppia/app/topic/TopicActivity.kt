package org.oppia.app.topic

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity for tabs in Topic. */
class TopicActivity : InjectableAppCompatActivity() {
  @Inject lateinit var topicActivityController: TopicActivityController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    topicActivityController.handleOnCreate()
  }
}
