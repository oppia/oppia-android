package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
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
    (activityComponent as Injector).inject(this)
    topicRevisionTestActivityPresenter.handleOnCreate()
  }

  override fun routeToRevisionCard(
    internalProfileId: Int,
    topicId: String,
    subtopicId: Int,
    subtopicListSize: Int
  ) {
    startActivity(
      RevisionCardActivity.createIntent(
        this,
        internalProfileId,
        topicId,
        subtopicId,
        subtopicListSize
      )
    )
  }

  interface Injector {
    fun inject(activity: TopicRevisionTestActivity)
  }

  companion object {
    fun createIntent(context: Context): Intent =
      Intent(context, TopicRevisionTestActivity::class.java)
  }
}
