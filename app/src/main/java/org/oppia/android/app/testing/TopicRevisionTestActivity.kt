package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.topic.RouteToRevisionCardListener
import org.oppia.android.app.topic.revision.TopicRevisionFragment
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity
import javax.inject.Inject

/** Test Activity used for testing [TopicRevisionFragment] */
class TopicRevisionTestActivity : InjectableAppCompatActivity(), RouteToRevisionCardListener {

  @Inject
  lateinit var topicRevisionTestActivityPresenter: TopicRevisionTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    topicRevisionTestActivityPresenter.handleOnCreate()
  }

  override fun routeToRevisionCard(internalProfileId: Int, topicId: String, subtopicId: Int) {
    startActivity(
      RevisionCardActivity.createRevisionCardActivityIntent(
        this,
        internalProfileId,
        topicId,
        subtopicId
      )
    )
  }
}
