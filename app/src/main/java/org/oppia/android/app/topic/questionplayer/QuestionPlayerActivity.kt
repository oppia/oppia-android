package org.oppia.android.app.topic.questionplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.hintsandsolution.HintsAndSolutionListener
import org.oppia.android.app.hintsandsolution.RevealHintListener
import org.oppia.android.app.hintsandsolution.RevealSolutionInterface
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.QuestionPlayerActivityParams
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.ScreenName.QUESTION_PLAYER_ACTIVITY
import org.oppia.android.app.model.State
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.exploration.DefaultFontSizeStateListener
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.player.stopplaying.RestartPlayingSessionListener
import org.oppia.android.app.player.stopplaying.StopExplorationDialogFragment
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionListener
import org.oppia.android.app.topic.conceptcard.ConceptCardListener
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

const val QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY =
  "QuestionPlayerActivity.skill_id_list"
private const val TAG_STOP_TRAINING_SESSION_DIALOG = "STOP_TRAINING_SESSION_DIALOG"

/** Activity for QuestionPlayer in train mode. */
class QuestionPlayerActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  StopStatePlayingSessionListener,
  RestartPlayingSessionListener,
  StateKeyboardButtonListener,
  HintsAndSolutionListener,
  RouteToHintsAndSolutionListener,
  RevealHintListener,
  RevealSolutionInterface,
  HintsAndSolutionQuestionManagerListener,
  DefaultFontSizeStateListener,
  ConceptCardListener {

  @Inject
  lateinit var questionPlayerActivityPresenter: QuestionPlayerActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    checkNotNull(intent.extras) { "Expected extras to be defined for QuestionPlayerActivity" }
    val profileId =
      intent.extractCurrentUserProfileId()
    questionPlayerActivityPresenter.handleOnCreate(profileId)

    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(/* enabled = */ true) {
        override fun handleOnBackPressed() {
          showStopExplorationDialogFragment()
          questionPlayerActivityPresenter.setReadingTextSizeNormal()
        }
      }
    )
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
    /** Params key for QuestionPlayerActivity. */
    const val QUESTION_PLAYER_ACTIVITY_PARAMS_KEY = "QuestionPlayerActivity.params"

    /**
     * Returns a new [Intent] to route to [QuestionPlayerActivity] for a specified skill ID list and
     * profile.
     */
    fun createQuestionPlayerActivityIntent(
      context: Context,
      skillIdList: ArrayList<String>,
      profileId: ProfileId
    ): Intent {

      val args = QuestionPlayerActivityParams.newBuilder().apply {
        addAllSkillIds(skillIdList)
      }
        .build()
      return Intent(context, QuestionPlayerActivity::class.java).apply {
        putProtoExtra(QUESTION_PLAYER_ACTIVITY_PARAMS_KEY, args)
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(QUESTION_PLAYER_ACTIVITY)
      }
    }
  }

  override fun revealHint(hintIndex: Int) {
    questionPlayerActivityPresenter.revealHint(hintIndex)
  }

  override fun revealSolution() {
    questionPlayerActivityPresenter.revealSolution()
  }

  override fun routeToHintsAndSolution(
    id: String,
    helpIndex: HelpIndex
  ) {
    questionPlayerActivityPresenter.routeToHintsAndSolution(id, helpIndex)
  }

  override fun dismiss() = questionPlayerActivityPresenter.dismissHintsAndSolutionDialog()

  override fun onQuestionStateLoaded(
    state: State,
    writtenTranslationContext: WrittenTranslationContext
  ) = questionPlayerActivityPresenter.loadQuestionState(state, writtenTranslationContext)

  override fun dismissConceptCard() {
    questionPlayerActivityPresenter.dismissConceptCard()
  }

  override fun stopSession() {
    questionPlayerActivityPresenter.stopTrainingSession()
  }

  override fun onDefaultFontSizeLoaded(readingTextSize: ReadingTextSize) {
    questionPlayerActivityPresenter.loadFragments(readingTextSize)
  }
}
