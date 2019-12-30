package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.HomeActivityPresenter
import org.oppia.app.home.RouteToTopicListener
import org.oppia.app.topic.TopicActivity
import javax.inject.Inject

class NavigationDrawerTestActivity : InjectableAppCompatActivity(), RouteToTopicListener {
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
