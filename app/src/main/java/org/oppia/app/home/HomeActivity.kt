package org.oppia.app.home

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.topic.TopicActivity
import javax.inject.Inject

/** The central activity for all users entering the app. */
class HomeActivity : InjectableAppCompatActivity(), RouteToExplorationListener, RouteToTopicListener {
  @Inject
  lateinit var homeActivityPresenter: HomeActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    homeActivityPresenter.handleOnCreate()
  }

  override fun routeToExploration(explorationId: String) {
    startActivity(ExplorationActivity.createExplorationActivityIntent(this, explorationId))
  }

  override fun routeToTopic(topicId: String) {
    startActivity(TopicActivity.createTopicActivityIntent(this, topicId))
  }
}
