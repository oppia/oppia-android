package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.topic.info.TopicInfoFragment
import javax.inject.Inject

/** Test Activity used for testing [TopicInfoFragment] */
class TopicInfoTestActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var topicInfoTestActivityPresenter: TopicInfoTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    topicInfoTestActivityPresenter.handleOnCreate()
  }
}
