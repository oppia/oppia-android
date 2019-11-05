package org.oppia.app.testing

import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.player.state.StateFragment
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_5
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_7
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val EXPLORATION_ID_1 = TEST_EXPLORATION_ID_5
private const val EXPLORATION_ID_2 = TEST_EXPLORATION_ID_7

/** The presenter for [ContentCardTestActivity]. */
@ActivityScope
class ContentCardTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val logger: Logger
) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.content_card_test_activity)
    val playExplorationButton1 = activity.findViewById(R.id.play_exploration_button_1) as Button
    val playExplorationButton2 = activity.findViewById(R.id.play_exploration_button_2) as Button

    playExplorationButton1.setOnClickListener(View.OnClickListener {
      explorationDataController.stopPlayingExploration()
      explorationDataController.startPlayingExploration(
        EXPLORATION_ID_1
      ).observe(activity, Observer<AsyncResult<Any?>> { result ->
        when {
          result.isPending() -> logger.d("ContentCardTest", "Loading exploration")
          result.isFailure() -> logger.e(
            "ContentCardTest",
            "Failed to load exploration",
            result.getErrorOrNull()!!
          )
          else -> {
            logger.d("ContentCardTest", "Successfully loaded exploration")

            if (getStateFragment() == null) {
              val stateFragment = StateFragment.newInstance(EXPLORATION_ID_1)
              activity.supportFragmentManager.beginTransaction().add(
                R.id.state_fragment_placeholder,
                stateFragment
              ).commitNow()
            }
          }
        }
      })
    })

    playExplorationButton2.setOnClickListener(View.OnClickListener {
      explorationDataController.stopPlayingExploration()
      explorationDataController.startPlayingExploration(
        EXPLORATION_ID_2
      ).observe(activity, Observer<AsyncResult<Any?>> { result ->
        when {
          result.isPending() -> logger.d("ContentCardTest", "Loading exploration")
          result.isFailure() -> logger.e(
            "ContentCardTest",
            "Failed to load exploration",
            result.getErrorOrNull()!!
          )
          else -> {
            logger.d("ContentCardTest", "Successfully loaded exploration")

            if (getStateFragment() == null) {
              val stateFragment = StateFragment.newInstance(EXPLORATION_ID_2)
              activity.supportFragmentManager.beginTransaction().add(
                R.id.state_fragment_placeholder,
                stateFragment
              ).commitNow()
            }
          }
        }
      })
    })
  }

  private fun getStateFragment(): StateFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.state_fragment_placeholder) as StateFragment?
  }
}
