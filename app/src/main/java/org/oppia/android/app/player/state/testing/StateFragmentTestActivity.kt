package org.oppia.android.app.player.state.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.hintsandsolution.HintsAndSolutionDialogFragment
import org.oppia.android.app.hintsandsolution.HintsAndSolutionListener
import org.oppia.android.app.hintsandsolution.RevealHintListener
import org.oppia.android.app.hintsandsolution.RevealSolutionInterface
import org.oppia.android.app.model.State
import org.oppia.android.app.player.audio.AudioButtonListener
import org.oppia.android.app.player.exploration.HintsAndSolutionExplorationManagerListener
import org.oppia.android.app.player.exploration.HINTS_AND_SOLUTION_DIALOG_EXTRA_KEY
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionListener
import javax.inject.Inject

internal const val TEST_ACTIVITY_PROFILE_ID_EXTRA = "StateFragmentTestActivity.profile_id"
internal const val TEST_ACTIVITY_TOPIC_ID_EXTRA = "StateFragmentTestActivity.topic_id"
internal const val TEST_ACTIVITY_STORY_ID_EXTRA = "StateFragmentTestActivity.story_id"
internal const val TEST_ACTIVITY_EXPLORATION_ID_EXTRA = "StateFragmentTestActivity.exploration_id"

/** Test Activity used for testing StateFragment */
class StateFragmentTestActivity :
  InjectableAppCompatActivity(),
  StopStatePlayingSessionListener,
  StateKeyboardButtonListener,
  AudioButtonListener,
  HintsAndSolutionListener,
  RouteToHintsAndSolutionListener,
  RevealHintListener,
  RevealSolutionInterface,
  HintsAndSolutionExplorationManagerListener {
  @Inject lateinit var stateFragmentTestActivityPresenter: StateFragmentTestActivityPresenter
  private lateinit var state: State

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    stateFragmentTestActivityPresenter.handleOnCreate()
  }

  override fun stopSession() = stateFragmentTestActivityPresenter.stopExploration()

  override fun onEditorAction(actionCode: Int) {}

  companion object {
    fun createTestActivityIntent(
      context: Context,
      profileId: Int,
      topicId: String,
      storyId: String,
      explorationId: String
    ): Intent {
      val intent = Intent(context, StateFragmentTestActivity::class.java)
      intent.putExtra(TEST_ACTIVITY_PROFILE_ID_EXTRA, profileId)
      intent.putExtra(TEST_ACTIVITY_TOPIC_ID_EXTRA, topicId)
      intent.putExtra(TEST_ACTIVITY_STORY_ID_EXTRA, storyId)
      intent.putExtra(TEST_ACTIVITY_EXPLORATION_ID_EXTRA, explorationId)
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

  override fun dismiss() {}

  override fun routeToHintsAndSolution(
    explorationId: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  ) {
    if (getHintsAndSolution() == null) {
      val hintsAndSolutionFragment =
        HintsAndSolutionDialogFragment.newInstance(
          explorationId,
          newAvailableHintIndex,
          allHintsExhausted
        )
      hintsAndSolutionFragment.loadState(state)
      hintsAndSolutionFragment.showNow(supportFragmentManager, HINTS_AND_SOLUTION_DIALOG_EXTRA_KEY)
    }
  }

  override fun revealHint(saveUserChoice: Boolean, hintIndex: Int) {
    stateFragmentTestActivityPresenter.revealHint(saveUserChoice, hintIndex)
  }

  override fun revealSolution() {
    stateFragmentTestActivityPresenter.revealSolution()
  }

  override fun onExplorationStateLoaded(state: State) {
    this.state = state
  }

  private fun getHintsAndSolution(): HintsAndSolutionDialogFragment? {
    return supportFragmentManager.findFragmentByTag(
      HINTS_AND_SOLUTION_DIALOG_EXTRA_KEY
    ) as HintsAndSolutionDialogFragment?
  }
}
