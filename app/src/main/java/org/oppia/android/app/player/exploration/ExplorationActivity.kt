package org.oppia.android.app.player.exploration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import org.oppia.android.app.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.hintsandsolution.HintsAndSolutionDialogFragment
import org.oppia.android.app.hintsandsolution.HintsAndSolutionListener
import org.oppia.android.app.hintsandsolution.RevealHintListener
import org.oppia.android.app.hintsandsolution.RevealSolutionInterface
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.State
import org.oppia.android.app.player.audio.AudioButtonListener
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.player.stopplaying.StopExplorationDialogFragment
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionListener
import org.oppia.android.app.topic.conceptcard.ConceptCardListener
import javax.inject.Inject

private const val TAG_STOP_EXPLORATION_DIALOG = "STOP_EXPLORATION_DIALOG"
const val TAG_HINTS_AND_SOLUTION_DIALOG = "HINTS_AND_SOLUTION_DIALOG"

/** The starting point for exploration. */
class ExplorationActivity :
  InjectableAppCompatActivity(),
  StopStatePlayingSessionListener,
  StateKeyboardButtonListener,
  AudioButtonListener,
  HintsAndSolutionListener,
  RouteToHintsAndSolutionListener,
  RevealHintListener,
  RevealSolutionInterface,
  DefaultFontSizeStateListener,
  HintsAndSolutionExplorationManagerListener,
  ConceptCardListener {

  @Inject
  lateinit var explorationActivityPresenter: ExplorationActivityPresenter
  private var internalProfileId: Int = -1
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String
  private lateinit var state: State
  private var backflowScreen: Int? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent.getIntExtra(EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, -1)
    topicId = intent.getStringExtra(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)
    storyId = intent.getStringExtra(EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY)
    explorationId = intent.getStringExtra(EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY)
    backflowScreen = intent.getIntExtra(EXPLORATION_ACTIVITY_BACKFLOW_SCREEN_KEY, -1)
    explorationActivityPresenter.handleOnCreate(
      this,
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      backflowScreen
    )
  }

  // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
  companion object {
    /** Returns a new [Intent] to route to [ExplorationActivity] for a specified exploration. */

    const val EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY =
      "ExplorationActivity.profile_id"
    const val EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY = "ExplorationActivity.topic_id"
    const val EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY = "ExplorationActivity.story_id"
    const val EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY =
      "ExplorationActivity.exploration_id"
    const val EXPLORATION_ACTIVITY_BACKFLOW_SCREEN_KEY =
      "ExplorationActivity.backflow_screen"

    fun createExplorationActivityIntent(
      context: Context,
      profileId: Int,
      topicId: String,
      storyId: String,
      explorationId: String,
      backflowScreen: Int?
    ): Intent {
      val intent = Intent(context, ExplorationActivity::class.java)
      intent.putExtra(EXPLORATION_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, profileId)
      intent.putExtra(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
      intent.putExtra(EXPLORATION_ACTIVITY_STORY_ID_ARGUMENT_KEY, storyId)
      intent.putExtra(EXPLORATION_ACTIVITY_EXPLORATION_ID_ARGUMENT_KEY, explorationId)
      intent.putExtra(EXPLORATION_ACTIVITY_BACKFLOW_SCREEN_KEY, backflowScreen)
      return intent
    }
  }

  override fun onBackPressed() {
    showStopExplorationDialogFragment()
  }

  private fun showStopExplorationDialogFragment() {
    val previousFragment = supportFragmentManager.findFragmentByTag(TAG_STOP_EXPLORATION_DIALOG)
    if (previousFragment != null) {
      supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = StopExplorationDialogFragment.newInstance()
    dialogFragment.showNow(supportFragmentManager, TAG_STOP_EXPLORATION_DIALOG)
  }

  override fun stopSession() {
    explorationActivityPresenter.stopExploration()
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_reading_options, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    return explorationActivityPresenter.handleOnOptionsItemSelected(item)
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

  override fun revealHint(saveUserChoice: Boolean, hintIndex: Int) {
    explorationActivityPresenter.revealHint(saveUserChoice, hintIndex)
  }

  override fun revealSolution() = explorationActivityPresenter.revealSolution()

  private fun getHintsAndSolution(): HintsAndSolutionDialogFragment? {
    return supportFragmentManager.findFragmentByTag(
      TAG_HINTS_AND_SOLUTION_DIALOG
    ) as HintsAndSolutionDialogFragment?
  }

  override fun routeToHintsAndSolution(
    explorationId: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  ) {
    if (getHintsAndSolution() == null) {
      val hintsAndSolutionDialogFragment = HintsAndSolutionDialogFragment.newInstance(
        explorationId,
        newAvailableHintIndex,
        allHintsExhausted
      )
      hintsAndSolutionDialogFragment.loadState(state)
      hintsAndSolutionDialogFragment.showNow(supportFragmentManager, TAG_HINTS_AND_SOLUTION_DIALOG)
    }
  }

  override fun dismiss() {
    getHintsAndSolution()?.dismiss()
  }

  override fun onDefaultFontSizeLoaded(readingTextSize: ReadingTextSize) {
    explorationActivityPresenter.loadExplorationFragment(readingTextSize)
  }

  override fun onExplorationStateLoaded(state: State) {
    this.state = state
  }

  override fun dismissConceptCard() = explorationActivityPresenter.dismissConceptCard()
}
