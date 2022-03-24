package org.oppia.android.app.player.state.testing

import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.exploration.HintsAndSolutionExplorationManagerFragment
import org.oppia.android.app.player.exploration.TAG_HINTS_AND_SOLUTION_EXPLORATION_MANAGER
import org.oppia.android.app.player.state.StateFragment
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.StateFragmentTestActivityBinding
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

private const val TEST_ACTIVITY_TAG = "TestActivity"

/** The presenter for [StateFragmentTestActivity] */
@ActivityScope
class StateFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val oppiaLogger: OppiaLogger,
  private val viewModelProvider: ViewModelProvider<StateFragmentTestViewModel>
) {

  private var profileId: Int = 1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String
  private var shouldSavePartialProgress: Boolean = false

  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<StateFragmentTestActivityBinding>(
      activity,
      R.layout.state_fragment_test_activity
    )
    binding.apply {
      lifecycleOwner = activity
      viewModel = getStateFragmentTestViewModel()
    }

    profileId = activity.intent.getIntExtra(TEST_ACTIVITY_PROFILE_ID_EXTRA_KEY, 1)
    topicId =
      activity.intent.getStringExtra(TEST_ACTIVITY_TOPIC_ID_EXTRA_KEY) ?: TEST_TOPIC_ID_0
    storyId =
      activity.intent.getStringExtra(TEST_ACTIVITY_STORY_ID_EXTRA_KEY) ?: TEST_STORY_ID_0
    explorationId =
      activity.intent.getStringExtra(TEST_ACTIVITY_EXPLORATION_ID_EXTRA_KEY)
      ?: TEST_EXPLORATION_ID_2
    shouldSavePartialProgress =
      activity.intent.getBooleanExtra(TEST_ACTIVITY_SHOULD_SAVE_PARTIAL_PROGRESS_EXTRA_KEY, false)
    activity.findViewById<Button>(R.id.play_test_exploration_button)?.setOnClickListener {
      startPlayingExploration(profileId, topicId, storyId, explorationId, shouldSavePartialProgress)
    }
  }

  fun stopExploration(isCompletion: Boolean) = finishExploration(isCompletion)

  fun scrollToTop() = getStateFragment()?.scrollToTop()

  fun revealHint(hintIndex: Int) = getStateFragment()?.revealHint(hintIndex)

  fun revealSolution() = getStateFragment()?.revealSolution()

  fun deleteCurrentProgressAndStopExploration(isCompletion: Boolean) {
    explorationDataController.deleteExplorationProgressById(
      ProfileId.newBuilder().setInternalId(profileId).build(),
      explorationId
    )
    stopExploration(isCompletion)
  }

  private fun startPlayingExploration(
    profileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean
  ) {
    // TODO(#59): With proper test ordering & isolation, this hacky clean-up should not be necessary since each test
    //  should run with a new application instance.
    explorationDataController.stopPlayingExploration(isCompletion = false)
    val startPlayingProvider = if (shouldSavePartialProgress) {
      explorationDataController.startPlayingNewExploration(
        profileId, topicId, storyId, explorationId
      )
    } else {
      explorationDataController.replayExploration(profileId, topicId, storyId, explorationId)
    }
    startPlayingProvider.toLiveData().observe(
      activity,
      Observer<AsyncResult<Any?>> { result ->
        when (result) {
          is AsyncResult.Pending -> oppiaLogger.d(TEST_ACTIVITY_TAG, "Loading exploration")
          is AsyncResult.Failure ->
            oppiaLogger.e(TEST_ACTIVITY_TAG, "Failed to load exploration", result.error)
          is AsyncResult.Success -> {
            oppiaLogger.d(TEST_ACTIVITY_TAG, "Successfully loaded exploration")
            initializeExploration(profileId, topicId, storyId, explorationId)
          }
        }
      }
    )
  }

  /**
   * Initializes fragments that depend on ephemeral state (which isn't valid to do until the play
   * session is fully started).
   */
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

    if (getHintsAndSolutionManagerFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.exploration_fragment_placeholder,
        HintsAndSolutionExplorationManagerFragment(),
        TAG_HINTS_AND_SOLUTION_EXPLORATION_MANAGER
      ).commitNow()
    }
  }

  private fun finishExploration(isCompletion: Boolean) {
    explorationDataController.stopPlayingExploration(isCompletion)

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
