package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.topic.RouteToRevisionCardListener
import org.oppia.app.topic.revisioncard.RevisionCardActivity
import javax.inject.Inject

class TopicRevisionTestActivity : InjectableAppCompatActivity(), RouteToRevisionCardListener {

  @Inject
  lateinit var topicRevisionTestActivityPresenter: TopicRevisionTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    topicRevisionTestActivityPresenter.handleOnCreate()
  }

  override fun routeToRevisionCard(topicId: String, subtopicId: String) {
    startActivity(
      RevisionCardActivity.createRevisionCardActivityIntent(
        this,
        topicId,
        subtopicId
      )
    )
  }
}
