package org.oppia.android.app.topic.questionplayer

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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.State
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.exploration.TAG_HINTS_AND_SOLUTION_DIALOG
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.player.stopplaying.RestartPlayingSessionListener
import org.oppia.android.app.player.stopplaying.StopExplorationDialogFragment
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionListener
import org.oppia.android.app.topic.conceptcard.ConceptCardListener
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import javax.inject.Inject

private const val QUESTION_PLAYER_ACTIVITY_PROFILE_ID_ARGUMENT_KEY =
  "QuestionPlayerActivity.profile_id"
const val QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY =
  "QuestionPlayerActivity.skill_id_list"
private const val TAG_STOP_TRAINING_SESSION_DIALOG = "STOP_TRAINING_SESSION_DIALOG"

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
  private lateinit var writtenTranslationContext: WrittenTranslationContext

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    checkNotNull(intent.extras) { "Expected extras to be defined for QuestionPlayerActivity" }
    val profileId =
      intent.getProtoExtra(
        QUESTION_PLAYER_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, ProfileId.getDefaultInstance()
      )
    questionPlayerActivityPresenter.handleOnCreate(profileId)
  }

  override fun onBackPressed() {
    showStopExplorationDialogFragment()
  }

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
    /**
     * Returns a new [Intent] to route to [QuestionPlayerActivity] for a specified skill ID list and
     * profile.
     */
    fun createQuestionPlayerActivityIntent(
      context: Context,
      skillIdList: ArrayList<String>,
      profileId: ProfileId
    ): Intent {
      return Intent(context, QuestionPlayerActivity::class.java).apply {
        putProtoExtra(QUESTION_PLAYER_ACTIVITY_PROFILE_ID_ARGUMENT_KEY, profileId)
        putExtra(QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY, skillIdList)
      }
    }
  }

  override fun revealHint(hintIndex: Int) {
    questionPlayerActivityPresenter.revealHint(hintIndex)
  }

  override fun revealSolution() {
    questionPlayerActivityPresenter.revealSolution()
  }

  private fun getHintsAndSolution(): HintsAndSolutionDialogFragment? {
    return supportFragmentManager.findFragmentByTag(
      TAG_HINTS_AND_SOLUTION_DIALOG
    ) as HintsAndSolutionDialogFragment?
  }

  override fun routeToHintsAndSolution(
    id: String,
    helpIndex: HelpIndex
  ) {
    if (getHintsAndSolution() == null) {
      val hintsAndSolutionDialogFragment =
        HintsAndSolutionDialogFragment.newInstance(
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

  override fun onQuestionStateLoaded(
    state: State,
    writtenTranslationContext: WrittenTranslationContext
  ) {
    this.state = state
    this.writtenTranslationContext = writtenTranslationContext
  }

  override fun dismissConceptCard() {
    questionPlayerActivityPresenter.dismissConceptCard()
  }

  override fun stopSession() {
    questionPlayerActivityPresenter.stopTrainingSession()
  }
}
