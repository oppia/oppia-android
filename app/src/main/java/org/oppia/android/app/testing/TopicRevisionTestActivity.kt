package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.topic.RouteToRevisionCardListener
import org.oppia.android.app.topic.revision.TopicRevisionFragment
import org.oppia.android.app.topic.revisioncard.RevisionCardActivity
import javax.inject.Inject

/** Test Activity used for testing [TopicRevisionFragment]. */
class TopicRevisionTestActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToRevisionCardListener {

  @Inject
  lateinit var topicRevisionTestActivityPresenter: TopicRevisionTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    topicRevisionTestActivityPresenter.handleOnCreate()
  }

  override fun routeToRevisionCard(
    profileId: ProfileId,
    topicId: String,
    subtopicId: Int,
    subtopicListSize: Int
  ) {
    startActivity(
      RevisionCardActivity.createRevisionCardActivityIntent(
        this,
        profileId,
        topicId,
        subtopicId,
        subtopicListSize
      )
    )
  }
}
