package org.oppia.android.app.topic.questionplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.hintsandsolution.HintsAndSolutionDialogFragment
import org.oppia.android.app.hintsandsolution.HintsAndSolutionListener
import org.oppia.android.app.hintsandsolution.RevealHintListener
import org.oppia.android.app.hintsandsolution.RevealSolutionInterface
import org.oppia.android.app.model.State
import org.oppia.android.app.player.exploration.HINTS_AND_SOLUTION_DIALOG_EXTRA_KEY
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.player.stopplaying.RestartPlayingSessionListener
import org.oppia.android.app.player.stopplaying.StopExplorationDialogFragment
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionListener
import org.oppia.android.app.topic.conceptcard.ConceptCardListener
import javax.inject.Inject

const val QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY =
  "QuestionPlayerActivity.skill_id_list"
private const val STOP_TRAINING_SESSION_DIALOG_EXTRA_KEY = "QuestionPlayerActivity.stop_training_session_dialog"

/** Activity for QuestionPlayer in train mode. */
class QuestionPlayerActivity :
  InjectableAppCompatActivity(),
  StopStatePlayingSessionListener,
  RestartPlayingSessionListener,
  StateKeyboardButtonListener,
  HintsAndSolutionListener,
  RouteToHintsAndSolutionListener,
  RevealHintListener,
  RevealSolutionInterface,
  HintsAndSolutionQuestionManagerListener,
  ConceptCardListener {

  @Inject
  lateinit var questionPlayerActivityPresenter: QuestionPlayerActivityPresenter
  private lateinit var state: State

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    questionPlayerActivityPresenter.handleOnCreate()
  }

  override fun onBackPressed() {
    showStopExplorationDialogFragment()
  }

  override fun stopSession() = questionPlayerActivityPresenter.stopTrainingSession()

  override fun restartSession() = questionPlayerActivityPresenter.restartSession()

  override fun onEditorAction(actionCode: Int) {
    questionPlayerActivityPresenter.onKeyboardAction(actionCode)
  }

  private fun showStopExplorationDialogFragment() {
    val previousFragment =
      supportFragmentManager.findFragmentByTag(STOP_TRAINING_SESSION_DIALOG_EXTRA_KEY)
    if (previousFragment != null) {
      supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = StopExplorationDialogFragment.newInstance()
    dialogFragment.showNow(supportFragmentManager, STOP_TRAINING_SESSION_DIALOG_EXTRA_KEY)
  }

  companion object {
    /** Returns a new [Intent] to route to [QuestionPlayerActivity] for a specified skill ID list. */
    fun createQuestionPlayerActivityIntent(
      context: Context,
      skillIdList: ArrayList<String>
    ): Intent {
      val intent = Intent(context, QuestionPlayerActivity::class.java)
      intent.putExtra(QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY, skillIdList)
      return intent
    }

    fun getIntentKey(): String {
      return QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY
    }
  }

  override fun revealHint(saveUserChoice: Boolean, hintIndex: Int) {
    questionPlayerActivityPresenter.revealHint(saveUserChoice, hintIndex)
  }

  override fun revealSolution() {
    questionPlayerActivityPresenter.revealSolution()
  }

  private fun getHintsAndSolution(): HintsAndSolutionDialogFragment? {
    return supportFragmentManager.findFragmentByTag(
      HINTS_AND_SOLUTION_DIALOG_EXTRA_KEY
    ) as HintsAndSolutionDialogFragment?
  }

  override fun routeToHintsAndSolution(
    questionId: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  ) {
    if (getHintsAndSolution() == null) {
      val hintsAndSolutionDialogFragment =
        HintsAndSolutionDialogFragment.newInstance(
          questionId,
          newAvailableHintIndex,
          allHintsExhausted
        )
      hintsAndSolutionDialogFragment.loadState(state)
      hintsAndSolutionDialogFragment.showNow(supportFragmentManager, HINTS_AND_SOLUTION_DIALOG_EXTRA_KEY)
    }
  }

  override fun dismiss() {
    getHintsAndSolution()?.dismiss()
  }

  override fun onQuestionStateLoaded(state: State) {
    this.state = state
  }

  override fun dismissConceptCard() {
    questionPlayerActivityPresenter.dismissConceptCard()
  }
}
