package org.oppia.android.app.player.state.testing

import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StateFragmentTestActivityParams
import org.oppia.android.app.player.exploration.HintsAndSolutionExplorationManagerFragment
import org.oppia.android.app.player.exploration.TAG_HINTS_AND_SOLUTION_EXPLORATION_MANAGER
import org.oppia.android.app.player.state.StateFragment
import org.oppia.android.app.player.state.testing.StateFragmentTestActivity.Companion.STATE_FRAGMENT_TEST_ACTIVITY_PARAMS_KEY
import org.oppia.android.databinding.StateFragmentTestActivityBinding
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_0
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProtoExtra
import javax.inject.Inject

private const val TEST_ACTIVITY_TAG = "TestActivity"

/** The presenter for [StateFragmentTestActivity]. */
@ActivityScope
class StateFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val explorationDataController: ExplorationDataController,
  private val oppiaLogger: OppiaLogger,
  private val stateFragmentTestViewModel: StateFragmentTestViewModel
) {

  private var profileId: Int = 1
  private lateinit var classroomId: String
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
      viewModel = stateFragmentTestViewModel
    }

    val args = activity.intent.getProtoExtra(
      STATE_FRAGMENT_TEST_ACTIVITY_PARAMS_KEY,
      StateFragmentTestActivityParams.getDefaultInstance()
    )
    profileId = args?.internalProfileId ?: 1
    classroomId = args?.classroomId ?: TEST_CLASSROOM_ID_0
    topicId =
      args?.topicId ?: TEST_TOPIC_ID_0
    storyId =
      args?.storyId ?: TEST_STORY_ID_0
    explorationId =
      args?.explorationId
      ?: TEST_EXPLORATION_ID_2
    shouldSavePartialProgress = args?.shouldSavePartialProgress ?: false
    activity.findViewById<Button>(R.id.play_test_exploration_button)?.setOnClickListener {
      startPlayingExploration(
        profileId,
        classroomId,
        topicId,
        storyId,
        explorationId,
        shouldSavePartialProgress
      )
    }
  }

  fun stopExploration(isCompletion: Boolean) = finishExploration(isCompletion)

  fun scrollToTop() = getStateFragment()?.scrollToTop()

  fun revealHint(hintIndex: Int) = getStateFragment()?.revealHint(hintIndex)

  fun revealSolution() = getStateFragment()?.revealSolution()

  fun deleteCurrentProgressAndStopExploration(isCompletion: Boolean) {
    explorationDataController.deleteExplorationProgressById(
      ProfileId.newBuilder().setLoggedInInternalProfileId(profileId).build(),
      explorationId
    )
    stopExploration(isCompletion)
  }

  private fun startPlayingExploration(
    profileId: Int,
    classroomId: String,
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
        profileId, classroomId, topicId, storyId, explorationId
      )
    } else {
      explorationDataController.replayExploration(
        profileId, classroomId, topicId, storyId, explorationId
      )
    }
    startPlayingProvider.toLiveData().observe(
      activity,
      { result ->
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
    stateFragmentTestViewModel.hasExplorationStarted.set(true)

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

    stateFragmentTestViewModel.hasExplorationStarted.set(false)
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
}
