package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.topic.TopicFragment
import javax.inject.Inject

/** The activity for testing [TopicFragment]. */
class ExplorationTestActivity : InjectableAppCompatActivity(), RouteToExplorationListener {
  @Inject lateinit var explorationTestActivityPresenter: ExplorationTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    explorationTestActivityPresenter.handleOnCreate()
  }

  override fun routeToExploration(internalProfileId: Int, topicId: String, storyId: String, explorationId: String, backflowScreen: Int) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        backflowScreen
      )
    )
  }
}
