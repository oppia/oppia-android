package org.oppia.android.app.player.state.testing

import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.player.exploration.HintsAndSolutionExplorationManagerFragment
import org.oppia.android.app.player.exploration.TAG_HINTS_AND_SOLUTION_EXPLORATION_MANAGER
import org.oppia.android.app.player.state.StateFragment
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.StateFragmentTestActivityBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

private const val TEST_ACTIVITY_TAG = "TestActivity"

/** The presenter for [StateFragmentTestActivity] */
@ActivityScope
class StateFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val logger: ConsoleLogger,
  private val viewModelProvider: ViewModelProvider<StateFragmentTestViewModel>
) {
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<StateFragmentTestActivityBinding>(
      activity,
      R.layout.state_fragment_test_activity
    )
    binding.apply {
      lifecycleOwner = activity
      viewModel = getStateFragmentTestViewModel()
    }

    val profileId = activity.intent.getIntExtra(TEST_ACTIVITY_PROFILE_ID_EXTRA_KEY, 1)
    val topicId = activity.intent.getStringExtra(TEST_ACTIVITY_TOPIC_ID_EXTRA_KEY) ?: TEST_TOPIC_ID_0
    val storyId = activity.intent.getStringExtra(TEST_ACTIVITY_STORY_ID_EXTRA_KEY) ?: TEST_STORY_ID_0
    val explorationId =
      activity.intent.getStringExtra(TEST_ACTIVITY_EXPLORATION_ID_EXTRA_KEY) ?: TEST_EXPLORATION_ID_2
    activity.findViewById<Button>(R.id.play_test_exploration_button)?.setOnClickListener {
      startPlayingExploration(profileId, topicId, storyId, explorationId)
    }

    if (getHintsAndSolutionManagerFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_fragment_placeholder,
        HintsAndSolutionExplorationManagerFragment(),
        TAG_HINTS_AND_SOLUTION_EXPLORATION_MANAGER
      ).commitNow()
    }
  }

  fun stopExploration() = finishExploration()

  fun scrollToTop() = getStateFragment()?.scrollToTop()

  fun revealHint(saveUserChoice: Boolean, hintIndex: Int) =
    getStateFragment()?.revealHint(saveUserChoice, hintIndex)

  fun revealSolution() = getStateFragment()?.revealSolution()

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
    getStateFragmentTestViewModel().hasExplorationStarted.set(true)

    val stateFragment = StateFragment.newInstance(profileId, topicId, storyId, explorationId)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.state_fragment_placeholder,
      stateFragment
    ).commitNow()
  }

  private fun finishExploration() {
    getStateFragment()?.let { fragment ->
      activity.supportFragmentManager.beginTransaction().remove(fragment).commitNow()
    }

    getStateFragmentTestViewModel().hasExplorationStarted.set(false)
  }

  private fun getStateFragment(): StateFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.state_fragment_placeholder
    ) as? StateFragment
  }

  private fun getHintsAndSolutionManagerFragment(): HintsAndSolutionExplorationManagerFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_HINTS_AND_SOLUTION_EXPLORATION_MANAGER
    ) as HintsAndSolutionExplorationManagerFragment?
  }

  private fun getStateFragmentTestViewModel(): StateFragmentTestViewModel {
    return viewModelProvider.getForActivity(activity, StateFragmentTestViewModel::class.java)
  }
}
