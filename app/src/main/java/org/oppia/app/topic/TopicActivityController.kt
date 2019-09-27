package org.oppia.app.topic

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The controller for [TopicActivity]. */
@ActivityScope
class TopicActivityController @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.topic_activity)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.topic_fragment_placeholder,
      TopicFragment()
    ).commitNow()
  }
}
