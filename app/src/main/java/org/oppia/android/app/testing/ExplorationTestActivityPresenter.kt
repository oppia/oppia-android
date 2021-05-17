package org.oppia.android.app.testing

import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.util.data.AsyncResult
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
  private val oppiaLogger: OppiaLogger
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
          result.isPending() -> oppiaLogger.d(TAG_EXPLORATION_TEST_ACTIVITY, "Loading exploration")
          result.isFailure() -> oppiaLogger.e(
            TAG_EXPLORATION_TEST_ACTIVITY,
            "Failed to load exploration",
            result.getErrorOrNull()!!
          )
          else -> {
            oppiaLogger.d(TAG_EXPLORATION_TEST_ACTIVITY, "Successfully loaded exploration")
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
