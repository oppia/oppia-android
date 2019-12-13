package org.oppia.app.home

import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.topic.TopicActivity
import javax.inject.Inject

/** The central activity for all users entering the app. */
class HomeActivity : InjectableAppCompatActivity(), RouteToTopicListener {
  @Inject
  lateinit var homeActivityPresenter: HomeActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    homeActivityPresenter.handleOnCreate()
    title = getString(R.string.menu_home)
  }

  override fun routeToTopic(topicId: String) {
    startActivity(TopicActivity.createTopicActivityIntent(this, topicId))
  }
}
