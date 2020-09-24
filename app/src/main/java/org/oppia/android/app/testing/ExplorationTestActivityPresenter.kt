package org.oppia.app.testing

import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.home.RouteToExplorationListener
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.domain.topic.TEST_STORY_ID_0
import org.oppia.domain.topic.TEST_TOPIC_ID_0
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.ConsoleLogger
import javax.inject.Inject

private const val INTERNAL_PROFILE_ID = 0
private const val TOPIC_ID = TEST_TOPIC_ID_0
private const val STORY_ID = TEST_STORY_ID_0
private const val EXPLORATION_ID = TEST_EXPLORATION_ID_2
private const val TAG_EXPLORATION_TEST_ACTIVITY = "ExplorationTestActivity"

/** The presenter for [ExplorationTestActivityPresenter]. */
@ActivityScope
class ExplorationTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val logger: ConsoleLogger
) {

  private val routeToExplorationListener = activity as RouteToExplorationListener

  fun handleOnCreate() {
    activity.setContentView(R.layout.exploration_test_activity)
    activity.findViewById<Button>(R.id.play_exploration_button).setOnClickListener {
      playExplorationButton()
    }
  }

  private fun playExplorationButton() {
    explorationDataController.stopPlayingExploration()
    explorationDataController.startPlayingExploration(
      EXPLORATION_ID
    ).observe(
      activity,
      Observer<AsyncResult<Any?>> { result ->
        when {
          result.isPending() -> logger.d(TAG_EXPLORATION_TEST_ACTIVITY, "Loading exploration")
          result.isFailure() -> logger.e(
            TAG_EXPLORATION_TEST_ACTIVITY,
            "Failed to load exploration",
            result.getErrorOrNull()!!
          )
          else -> {
            logger.d(TAG_EXPLORATION_TEST_ACTIVITY, "Successfully loaded exploration")
            routeToExplorationListener.routeToExploration(
              INTERNAL_PROFILE_ID,
              TOPIC_ID,
              STORY_ID,
              EXPLORATION_ID, /* backflowScreen= */
              null
            )
          }
        }
      }
    )
  }
}
