package org.oppia.android.app.player.state.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.hintsandsolution.HintsAndSolutionDialogFragment
import org.oppia.android.app.hintsandsolution.HintsAndSolutionListener
import org.oppia.android.app.hintsandsolution.RevealHintListener
import org.oppia.android.app.hintsandsolution.RevealSolutionInterface
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.State
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.audio.AudioButtonListener
import org.oppia.android.app.player.exploration.HintsAndSolutionExplorationManagerListener
import org.oppia.android.app.player.exploration.TAG_HINTS_AND_SOLUTION_DIALOG
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionWithSavedProgressListener
import javax.inject.Inject

internal const val TEST_ACTIVITY_PROFILE_ID_EXTRA_KEY =
  "StateFragmentTestActivity.test_activity_profile_id"
internal const val TEST_ACTIVITY_TOPIC_ID_EXTRA_KEY =
  "StateFragmentTestActivity.test_activity_topic_id"
internal const val TEST_ACTIVITY_STORY_ID_EXTRA_KEY =
  "StateFragmentTestActivity.test_activity_story_id"
internal const val TEST_ACTIVITY_EXPLORATION_ID_EXTRA_KEY =
  "StateFragmentTestActivity.test_activity_exploration_id"
internal const val TEST_ACTIVITY_SHOULD_SAVE_PARTIAL_PROGRESS_EXTRA_KEY =
  "StateFragmentTestActivity.test_activity_should_save_partial_progress"

/** Test Activity used for testing StateFragment */
class StateFragmentTestActivity :
  InjectableAppCompatActivity(),
  StopStatePlayingSessionWithSavedProgressListener,
  StateKeyboardButtonListener,
  AudioButtonListener,
  HintsAndSolutionListener,
  RouteToHintsAndSolutionListener,
  RevealHintListener,
  RevealSolutionInterface,
  HintsAndSolutionExplorationManagerListener {
  @Inject
  lateinit var stateFragmentTestActivityPresenter: StateFragmentTestActivityPresenter
  private lateinit var state: State
  private lateinit var writtenTranslationContext: WrittenTranslationContext

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    stateFragmentTestActivityPresenter.handleOnCreate()
  }

  override fun deleteCurrentProgressAndStopSession(isCompletion: Boolean) {
    stateFragmentTestActivityPresenter.deleteCurrentProgressAndStopExploration(isCompletion)
  }

  override fun deleteOldestProgressAndStopSession() {}

  override fun onEditorAction(actionCode: Int) {}

  companion object {
    fun createTestActivityIntent(
      context: Context,
      profileId: Int,
      topicId: String,
      storyId: String,
      explorationId: String,
      shouldSavePartialProgress: Boolean
    ): Intent {
      val intent = Intent(context, StateFragmentTestActivity::class.java)
      intent.putExtra(TEST_ACTIVITY_PROFILE_ID_EXTRA_KEY, profileId)
      intent.putExtra(TEST_ACTIVITY_TOPIC_ID_EXTRA_KEY, topicId)
      intent.putExtra(TEST_ACTIVITY_STORY_ID_EXTRA_KEY, storyId)
      intent.putExtra(TEST_ACTIVITY_EXPLORATION_ID_EXTRA_KEY, explorationId)
      intent.putExtra(
        TEST_ACTIVITY_SHOULD_SAVE_PARTIAL_PROGRESS_EXTRA_KEY,
        shouldSavePartialProgress
      )
      return intent
    }
  }

  override fun showAudioButton() {}

  override fun hideAudioButton() {}

  override fun showAudioStreamingOn() {}

  override fun showAudioStreamingOff() {}

  override fun setAudioBarVisibility(isVisible: Boolean) {}

  override fun scrollToTop() {
    stateFragmentTestActivityPresenter.scrollToTop()
  }

  fun stopExploration(isCompletion: Boolean) {
    stateFragmentTestActivityPresenter.stopExploration(isCompletion)
  }

  override fun dismiss() {}

  override fun routeToHintsAndSolution(
    explorationId: String,
    helpIndex: HelpIndex
  ) {
    if (getHintsAndSolution() == null) {
      val hintsAndSolutionFragment =
        HintsAndSolutionDialogFragment.newInstance(
          explorationId,
          state,
          helpIndex,
          writtenTranslationContext
        )
      hintsAndSolutionFragment.showNow(supportFragmentManager, TAG_HINTS_AND_SOLUTION_DIALOG)
    }
  }

  override fun revealHint(hintIndex: Int) {
    stateFragmentTestActivityPresenter.revealHint(hintIndex)
  }

  override fun revealSolution() {
    stateFragmentTestActivityPresenter.revealSolution()
  }

  override fun onExplorationStateLoaded(
    state: State,
    writtenTranslationContext: WrittenTranslationContext
  ) {
    this.state = state
    this.writtenTranslationContext = writtenTranslationContext
  }

  private fun getHintsAndSolution(): HintsAndSolutionDialogFragment? {
    return supportFragmentManager.findFragmentByTag(
      TAG_HINTS_AND_SOLUTION_DIALOG
    ) as HintsAndSolutionDialogFragment?
  }
}
