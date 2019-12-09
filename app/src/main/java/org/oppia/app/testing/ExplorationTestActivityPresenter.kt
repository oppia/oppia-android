package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.exploration_test_activity.*
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_5
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val EXPLORATION_ID = TEST_EXPLORATION_ID_5
private const val TAG_EXPLORATION_TEST_ACTIVITY = "ExplorationTestActivity"

/** The presenter for [ExplorationTestActivityPresenter]. */
@ActivityScope
class ExplorationTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val logger: Logger
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener

  fun handleOnCreate() {
    activity.setContentView(R.layout.exploration_test_activity)

    activity.play_exploration_button.setOnClickListener {
      playExplorationButton()
    }
  }

  private fun playExplorationButton() {
    explorationDataController.stopPlayingExploration()
    explorationDataController.startPlayingExploration(
      EXPLORATION_ID
    ).observe(activity, Observer<AsyncResult<Any?>> { result ->
      when {
        result.isPending() -> logger.d(TAG_EXPLORATION_TEST_ACTIVITY, "Loading exploration")
        result.isFailure() -> logger.e(TAG_EXPLORATION_TEST_ACTIVITY, "Failed to load exploration", result.getErrorOrNull()!!)
        else -> {
          logger.d(TAG_EXPLORATION_TEST_ACTIVITY, "Successfully loaded exploration")
          routeToExplorationListener.routeToExploration(EXPLORATION_ID, null)
        }
      }
    })
  }
}
