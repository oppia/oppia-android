package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.topic.revision.TopicRevisionFragment
import javax.inject.Inject

@ActivityScope
class TopicRevisionTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.activity_topic_revision_test)
    val topicRevisionFragment = TopicRevisionFragment.newInstance(0, "")
    activity.supportFragmentManager.beginTransaction()
      .add(
        R.id.topic_revision_container,
        topicRevisionFragment,
        TopicRevisionFragment.TOPIC_REVISION_FRAGMENT_TAG
      ).commit()
  }
}
