package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.exploration.ExplorationActivity
import org.oppia.android.app.topic.TopicFragment
import org.oppia.android.app.utility.SplitScreenManager
import javax.inject.Inject

/** The activity for testing [TopicFragment]. */
class ExplorationTestActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  RouteToExplorationListener {
  @Inject
  lateinit var presenter: ExplorationTestActivityPresenter

  /**
   * Exposes the [SplitScreenManager] corresponding to the fragment under test for tests to interact
   * with.
   */
  val splitScreenManager: SplitScreenManager
    get() = getTestFragment().splitScreenManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    presenter.handleOnCreate()
  }

  override fun routeToExploration(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    isCheckpointingEnabled: Boolean
  ) {
    startActivity(
      ExplorationActivity.createExplorationActivityIntent(
        this,
        profileId,
        classroomId,
        topicId,
        storyId,
        explorationId,
        parentScreen,
        isCheckpointingEnabled
      )
    )
  }

  private fun getTestFragment() = checkNotNull(presenter.getTestFragment()) {
    "Expected TestFragment to be present in inflated test activity. Did you try to retrieve the" +
      " screen manager too early in the test?"
  }
}
