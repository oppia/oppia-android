package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.topic.info.TopicInfoFragment
import org.oppia.domain.topic.RATIOS_TOPIC_ID
import javax.inject.Inject

/** The presenter for [TopicInfoTestActivity]. */
@ActivityScope
class TopicInfoTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.topic_info_test_activity)
    val topicInfoFragment =
      TopicInfoFragment.newInstance(internalProfileId = 0, topicId = RATIOS_TOPIC_ID)
    activity.supportFragmentManager.beginTransaction()
      .add(R.id.topic_info_container, topicInfoFragment, TopicInfoFragment.TOPIC_INFO_FRAGMENT_TAG)
      .commitNow()
  }
}
