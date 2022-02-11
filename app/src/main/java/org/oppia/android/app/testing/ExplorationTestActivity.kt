package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.topic.TopicFragment
import javax.inject.Inject
import org.oppia.android.app.utility.SplitScreenManager

/** The activity for testing [TopicFragment]. */
class ExplorationTestActivity : InjectableAppCompatActivity(), RouteToExplorationListener {
  @Inject
  lateinit var presenter: ExplorationTestActivityPresenter

  val splitScreenManager: SplitScreenManager
    get() = getTestFragment().splitScreenManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    presenter.handleOnCreate()
  }

  override fun routeToExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?,
    isCheckpointingEnabled: Boolean
  ) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        backflowScreen,
        isCheckpointingEnabled
      )
    )
  }

  private fun getTestFragment() = checkNotNull(presenter.getTestFragment()) {
    "Expected TestFragment to be present in inflated test activity. Did you try to retrieve the" +
      " screen manager too early in the test?"
  }
}
