package org.oppia.android.app.player.state.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.hintsandsolution.HintsAndSolutionDialogFragment
import org.oppia.android.app.hintsandsolution.HintsAndSolutionListener
import org.oppia.android.app.hintsandsolution.RevealHintListener
import org.oppia.android.app.hintsandsolution.RevealSolutionInterface
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.State
import org.oppia.android.app.model.StateFragmentTestActivityParams
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.audio.AudioButtonListener
import org.oppia.android.app.player.exploration.HintsAndSolutionExplorationManagerListener
import org.oppia.android.app.player.exploration.TAG_HINTS_AND_SOLUTION_DIALOG
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionWithSavedProgressListener
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import javax.inject.Inject

/** Test Activity used for testing StateFragment. */
class StateFragmentTestActivity :
  InjectableAutoLocalizedAppCompatActivity(),
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
  private lateinit var profileId: ProfileId

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val args = intent.getProtoExtra(
      STATE_FRAGMENT_TEST_ACTIVITY_PARAMS_KEY,
      StateFragmentTestActivityParams.getDefaultInstance()
    )

    profileId = ProfileId.newBuilder().apply {
      internalId = args?.internalProfileId ?: -1
    }.build()
    stateFragmentTestActivityPresenter.handleOnCreate()
  }

  override fun deleteCurrentProgressAndStopSession(isCompletion: Boolean) {
    stateFragmentTestActivityPresenter.deleteCurrentProgressAndStopExploration(isCompletion)
  }

  override fun deleteOldestProgressAndStopSession() {}

  override fun onEditorAction(actionCode: Int) {}

  companion object {

    /** Params key for StateFragmentTestActivity. */
    const val STATE_FRAGMENT_TEST_ACTIVITY_PARAMS_KEY = "StateFragmentTestActivity.params"

    fun createTestActivityIntent(
      context: Context,
      profileId: Int,
      classroomId: String,
      topicId: String,
      storyId: String,
      explorationId: String,
      shouldSavePartialProgress: Boolean
    ): Intent {
      val args = StateFragmentTestActivityParams.newBuilder().apply {
        this.internalProfileId = profileId
        this.classroomId = classroomId
        this.topicId = topicId
        this.storyId = storyId
        this.explorationId = explorationId
        this.shouldSavePartialProgress = shouldSavePartialProgress
      }.build()
      val intent = Intent(context, StateFragmentTestActivity::class.java)
      intent.putProtoExtra(STATE_FRAGMENT_TEST_ACTIVITY_PARAMS_KEY, args)
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

  override fun routeToHintsAndSolution(id: String, helpIndex: HelpIndex) {
    if (getHintsAndSolution() == null) {
      val hintsAndSolutionFragment =
        HintsAndSolutionDialogFragment.newInstance(
          id,
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
