package org.oppia.android.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.topic.revision.TopicRevisionFragment
import javax.inject.Inject

/** The presenter for [TopicRevisionTestActivity]. */
@ActivityScope
class TopicRevisionTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.topic_revision_test_activity)
    val topicRevisionFragment =
      TopicRevisionFragment.newInstance(internalProfileId = 0, topicId = "")
    activity.supportFragmentManager.beginTransaction()
      .add(
        R.id.topic_revision_container,
        topicRevisionFragment,
        TopicRevisionFragment.TOPIC_REVISION_FRAGMENT_TAG
      ).commit()
  }
}
