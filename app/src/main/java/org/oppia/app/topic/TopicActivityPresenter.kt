package org.oppia.app.topic

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [TopicActivity]. */
@ActivityScope
class TopicActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.topic_activity)
    if (getTopicFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.topic_fragment_placeholder,
        TopicFragment()
      ).commitNow()
    }
  }

  private fun getTopicFragment(): TopicFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.topic_fragment_placeholder) as TopicFragment?
  }
}
