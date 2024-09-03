package org.oppia.android.app.player.exploration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.hintsandsolution.HintsAndSolutionDialogFragment
import org.oppia.android.app.hintsandsolution.HintsAndSolutionListener
import org.oppia.android.app.hintsandsolution.RevealHintListener
import org.oppia.android.app.hintsandsolution.RevealSolutionInterface
import org.oppia.android.app.hintsandsolution.ViewHintListener
import org.oppia.android.app.hintsandsolution.ViewSolutionInterface
import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.ScreenName.EXPLORATION_ACTIVITY
import org.oppia.android.app.model.State
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.audio.AudioButtonListener
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionWithSavedProgressListener
import org.oppia.android.app.topic.conceptcard.ConceptCardListener
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

const val TAG_HINTS_AND_SOLUTION_DIALOG = "HINTS_AND_SOLUTION_DIALOG"

/** The starting point for exploration. */
class ExplorationActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  StopStatePlayingSessionWithSavedProgressListener,
  StateKeyboardButtonListener,
  AudioButtonListener,
  HintsAndSolutionListener,
  RouteToHintsAndSolutionListener,
  RevealHintListener,
  ViewHintListener,
  RevealSolutionInterface,
  ViewSolutionInterface,
  DefaultFontSizeStateListener,
  HintsAndSolutionExplorationManagerListener,
  ConceptCardListener,
  BottomSheetOptionsMenuItemClickListener,
  RequestVoiceOverIconSpotlightListener {

  @Inject
  lateinit var explorationActivityPresenter: ExplorationActivityPresenter
  private lateinit var state: State
  private lateinit var writtenTranslationContext: WrittenTranslationContext

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val params = intent.extractParams()
    explorationActivityPresenter.handleOnCreate(
      this,
      params.profileId,
      params.classroomId,
      params.topicId,
      params.storyId,
      params.explorationId,
      params.parentScreen,
      params.isCheckpointingEnabled
    )
    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(/* enabled = */ true) {
        override fun handleOnBackPressed() {
          explorationActivityPresenter.backButtonPressed()
        }
      }
    )
  }

  // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
  companion object {
    private const val PARAMS_KEY = "ExplorationActivity.params"

    /**
     * A convenience function for creating a new [ExplorationActivity] intent by prefilling common
     * params needed by the activity.
     */
    fun createExplorationActivityIntent(
      context: Context,
      profileId: ProfileId,
      classroomId: String,
      topicId: String,
      storyId: String,
      explorationId: String,
      parentScreen: ExplorationActivityParams.ParentScreen,
      isCheckpointingEnabled: Boolean
    ): Intent {
      val params = ExplorationActivityParams.newBuilder().apply {
        this.profileId = profileId
        this.classroomId = classroomId
        this.topicId = topicId
        this.storyId = storyId
        this.explorationId = explorationId
        this.parentScreen = parentScreen
        this.isCheckpointingEnabled = isCheckpointingEnabled
      }.build()
      return createExplorationActivityIntent(context, params)
    }

    /** Returns a new [Intent] open an [ExplorationActivity] with the specified [params]. */
    fun createExplorationActivityIntent(
      context: Context,
      params: ExplorationActivityParams
    ): Intent {
      return Intent(context, ExplorationActivity::class.java).apply {
        putProtoExtra(PARAMS_KEY, params)
        decorateWithScreenName(EXPLORATION_ACTIVITY)
      }
    }

    private fun Intent.extractParams() =
      getProtoExtra(PARAMS_KEY, ExplorationActivityParams.getDefaultInstance())
  }

  override fun deleteCurrentProgressAndStopSession(isCompletion: Boolean) {
    explorationActivityPresenter.deleteCurrentProgressAndStopExploration(isCompletion)
  }

  override fun deleteOldestProgressAndStopSession() {
    explorationActivityPresenter.deleteOldestSavedProgressAndStopExploration()
  }

  override fun handleOnOptionsItemSelected(itemId: Int) {
    explorationActivityPresenter.handleOnOptionsItemSelected(itemId)
  }

  override fun showAudioButton() = explorationActivityPresenter.showAudioButton()

  override fun hideAudioButton() = explorationActivityPresenter.hideAudioButton()

  override fun showAudioStreamingOn() = explorationActivityPresenter.showAudioStreamingOn()

  override fun showAudioStreamingOff() = explorationActivityPresenter.showAudioStreamingOff()

  override fun setAudioBarVisibility(isVisible: Boolean) {
    explorationActivityPresenter.setAudioBarVisibility(isVisible)
  }

  override fun scrollToTop() {
    explorationActivityPresenter.scrollToTop()
  }

  override fun onEditorAction(actionCode: Int) {
    explorationActivityPresenter.onKeyboardAction(actionCode)
  }

  override fun revealHint(hintIndex: Int) {
    explorationActivityPresenter.revealHint(hintIndex)
  }

  override fun revealSolution() = explorationActivityPresenter.revealSolution()

  private fun getHintsAndSolution(): HintsAndSolutionDialogFragment? {
    return supportFragmentManager.findFragmentByTag(
      TAG_HINTS_AND_SOLUTION_DIALOG
    ) as HintsAndSolutionDialogFragment?
  }

  override fun routeToHintsAndSolution(id: String, helpIndex: HelpIndex) {
    if (getHintsAndSolution() == null) {
      val hintsAndSolutionDialogFragment = HintsAndSolutionDialogFragment.newInstance(
        id,
        state,
        helpIndex,
        writtenTranslationContext
      )
      hintsAndSolutionDialogFragment.showNow(supportFragmentManager, TAG_HINTS_AND_SOLUTION_DIALOG)
    }
  }

  override fun dismiss() {
    getHintsAndSolution()?.dismiss()
  }

  override fun onDefaultFontSizeLoaded(readingTextSize: ReadingTextSize) {
    explorationActivityPresenter.loadExplorationFragment(readingTextSize)
  }

  override fun onExplorationStateLoaded(
    state: State,
    writtenTranslationContext: WrittenTranslationContext
  ) {
    this.state = state
    this.writtenTranslationContext = writtenTranslationContext
  }

  override fun dismissConceptCard() = explorationActivityPresenter.dismissConceptCard()

  override fun requestVoiceOverIconSpotlight(numberOfLogins: Int) {
    explorationActivityPresenter.requestVoiceOverIconSpotlight(numberOfLogins)
  }

  override fun viewHint(hintIndex: Int) {
    explorationActivityPresenter.viewHint(hintIndex)
  }

  override fun viewSolution() {
    explorationActivityPresenter.viewSolution()
  }
}
