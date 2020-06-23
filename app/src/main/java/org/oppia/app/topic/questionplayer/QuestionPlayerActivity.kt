package org.oppia.app.topic.questionplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.model.State
import org.oppia.app.player.exploration.TAG_HINTS_AND_SOLUTION_DIALOG
import org.oppia.app.hintsandsolution.HintsAndSolutionDialogFragment
import org.oppia.app.hintsandsolution.HintsAndSolutionListener
import org.oppia.app.hintsandsolution.RevealHintListener
import org.oppia.app.hintsandsolution.RevealSolutionInterface
import org.oppia.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.app.player.stopplaying.RestartPlayingSessionListener
import org.oppia.app.player.stopplaying.StopExplorationDialogFragment
import org.oppia.app.player.stopplaying.StopStatePlayingSessionListener
import javax.inject.Inject

const val QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY =
  "QuestionPlayerActivity.skill_id_list"
private const val TAG_STOP_TRAINING_SESSION_DIALOG = "STOP_TRAINING_SESSION_DIALOG"
private const val TAG_HINTS_AND_SOLUTION_QUESTION_MANAGER = "HINTS_AND_SOLUTION_QUESTION_MANAGER"

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
  HintsAndSolutionQuestionManagerListener {

  @Inject
  lateinit var questionPlayerActivityPresenter: QuestionPlayerActivityPresenter

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
      supportFragmentManager.findFragmentByTag(TAG_STOP_TRAINING_SESSION_DIALOG)
    if (previousFragment != null) {
      supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = StopExplorationDialogFragment.newInstance()
    dialogFragment.showNow(supportFragmentManager, TAG_STOP_TRAINING_SESSION_DIALOG)
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

  override fun revealSolution(saveUserChoice: Boolean) {
    questionPlayerActivityPresenter.revealSolution(saveUserChoice)
  }

  private fun getHintsAndSolution(): HintsAndSolutionDialogFragment? {
    return supportFragmentManager.findFragmentByTag(TAG_HINTS_AND_SOLUTION_DIALOG) as HintsAndSolutionDialogFragment?
  }

  override fun routeToHintsAndSolution(
    questionId: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  ) {
    if (getHintsAndSolutionExplorationManagerFragment() == null) {
      supportFragmentManager.beginTransaction().add(
        R.id.question_player_fragment_placeholder,
        HintsAndSolutionQuestionManagerFragment.newInstance(
          questionId,
          newAvailableHintIndex,
          allHintsExhausted
        )
      ).commitNow()
    }
  }

  private fun getHintsAndSolutionExplorationManagerFragment(): HintsAndSolutionQuestionManagerFragment? {
    return supportFragmentManager.findFragmentByTag(TAG_HINTS_AND_SOLUTION_QUESTION_MANAGER) as HintsAndSolutionQuestionManagerFragment?
  }

  override fun dismiss() {
    getHintsAndSolution()?.dismiss()
  }

  override fun onQuestionStateLoaded(
    state: State,
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
      hintsAndSolutionFragment.showNow(supportFragmentManager, TAG_HINTS_AND_SOLUTION_DIALOG)
    }
  }

}
