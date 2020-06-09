package org.oppia.app.player.state.testing

import android.content.Intent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.player.state.StateFragment
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val TEST_ACTIVITY_TAG = "TestActivity"

/** The presenter for [StateFragmentTestActivity] */
@ActivityScope
class StateFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val logger: Logger
) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.state_fragment_test_activity)

    /* ktlint-disable max-line-length */
    val profileId = checkNotNull(activity.intent.getIntExtra(TEST_ACTIVITY_PROFILE_ID_EXTRA)) {
      "Use intent from StateFragmentTestActivity.createTestActivityIntent() to navigate to test activity."
    }
    val topicId = checkNotNull(activity.intent.getStringExtra(TEST_ACTIVITY_TOPIC_ID_EXTRA)) {
      "Use intent from StateFragmentTestActivity.createTestActivityIntent() to navigate to test activity."
    }
    val storyId = checkNotNull(activity.intent.getStringExtra(TEST_ACTIVITY_STORY_ID_EXTRA)) {
      "Use intent from StateFragmentTestActivity.createTestActivityIntent() to navigate to test activity."
    }
    val explorationId =
      checkNotNull(activity.intent.getStringExtra(TEST_ACTIVITY_EXPLORATION_ID_EXTRA)) {
        "Use intent from StateFragmentTestActivity.createTestActivityIntent() to navigate to test activity."
      }
    /* ktlint-enable max-line-length */
    activity.findViewById<Button>(R.id.play_test_exploration_button)?.setOnClickListener {
      startPlayingExploration(profileId, topicId, storyId, explorationId)
    }
  }

  fun stopExploration() = finishExploration()

  fun scrollToTop() = getStateFragment()?.scrollToTop()

  private fun startPlayingExploration(
    profileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String
  ) {
    // TODO(#59): With proper test ordering & isolation, this hacky clean-up should not be necessary since each test
    //  should run with a new application instance.
    explorationDataController.stopPlayingExploration()
    explorationDataController.startPlayingExploration(explorationId)
      .observe(
        activity,
        Observer<AsyncResult<Any?>> { result ->
          when {
            result.isPending() -> logger.d(TEST_ACTIVITY_TAG, "Loading exploration")
            result.isFailure() -> logger.e(
              TEST_ACTIVITY_TAG,
              "Failed to load exploration",
              result.getErrorOrNull()!!
            )
            else -> {
              logger.d(TEST_ACTIVITY_TAG, "Successfully loaded exploration")
              initializeExploration(profileId, topicId, storyId, explorationId)
            }
          }
        }
      )
  }

  private fun initializeExploration(
    profileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String
  ) {
    activity.findViewById<Button>(R.id.play_test_exploration_button)?.visibility = View.GONE

    val stateFragment = StateFragment
      .newInstance(
        profileId,
        topicId,
        storyId,
        explorationId
      )
    activity.supportFragmentManager.beginTransaction().add(
      R.id.state_fragment_placeholder,
      stateFragment
    ).commitNow()
  }

  private fun finishExploration() {
    getStateFragment()?.let { fragment ->
      activity.supportFragmentManager.beginTransaction().remove(fragment).commitNow()
    }

    activity.findViewById<Button>(R.id.play_test_exploration_button)?.visibility = View.VISIBLE
  }

  private fun getStateFragment(): StateFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.state_fragment_placeholder
      ) as? StateFragment
  }
}

/**
 * A version of [Intent.getIntExtra] that returns a null integer if the value is absent rather than
 * a default value.
 */
private fun Intent.getIntExtra(name: String): Int? {
  return getIntExtra(name, Int.MIN_VALUE).takeIf { it != Int.MIN_VALUE }
}
