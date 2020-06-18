package org.oppia.app.topic.questionplayer

import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.domain.question.QuestionTrainingController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

const val TAG_QUESTION_PLAYER_FRAGMENT = "TAG_QUESTION_PLAYER_FRAGMENT"

/** The presenter for [QuestionPlayerActivity]. */
@ActivityScope
class QuestionPlayerActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val questionTrainingController: QuestionTrainingController,
  private val logger: Logger
) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.question_player_activity)
    if (getQuestionPlayerFragment() == null) {
      startTrainingSessionWithCallback {
        activity.supportFragmentManager.beginTransaction().add(
          R.id.question_player_fragment_placeholder,
          QuestionPlayerFragment(),
          TAG_QUESTION_PLAYER_FRAGMENT
        ).commitNow()
      }
    }
  }

  fun stopTrainingSession() {
    stopTrainingSessionWithCallback {
      activity.finish()
    }
  }

  fun restartSession() {
    stopTrainingSessionWithCallback {
      getQuestionPlayerFragment()?.let { fragment ->
        activity.supportFragmentManager.beginTransaction().remove(fragment).commitNow()
      }
      startTrainingSessionWithCallback {
        // Re-add the player fragment when the new session is ready.
        activity.supportFragmentManager.beginTransaction().add(
          R.id.question_player_fragment_placeholder,
          QuestionPlayerFragment(),
          TAG_QUESTION_PLAYER_FRAGMENT
        ).commitNow()
      }
    }
  }

  private fun startTrainingSessionWithCallback(callback: () -> Unit) {
    val skillIds =
      activity.intent.getStringArrayListExtra(QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY)
    questionTrainingController.startQuestionTrainingSession(skillIds).observe(
      activity,
      Observer {
        when {
          it.isPending() -> logger.d(
            "QuestionPlayerActivity",
            "Starting training session"
          )
          it.isFailure() -> {
            logger.e(
              "QuestionPlayerActivity",
              "Failed to start training session",
              it.getErrorOrNull()!!
            )
            activity.finish() // Can't recover from the session failing to start.
          }
          else -> {
            logger.d("QuestionPlayerActivity", "Successfully started training session")
            callback()
          }
        }
      }
    )
  }

  private fun stopTrainingSessionWithCallback(callback: () -> Unit) {
    questionTrainingController.stopQuestionTrainingSession().observe(
      activity,
      Observer<AsyncResult<Any?>> {
        when {
          it.isPending() -> logger.d(
            "QuestionPlayerActivity",
            "Stopping training session"
          )
          it.isFailure() -> {
            logger.e(
              "QuestionPlayerActivity",
              "Failed to stop training session",
              it.getErrorOrNull()!!
            )
            activity.finish() // Can't recover from the session failing to stop.
          }
          else -> {
            logger.d("QuestionPlayerActivity", "Successfully stopped training session")
            callback()
          }
        }
      }
    )
  }

  fun onKeyboardAction(actionCode: Int) {
    if (actionCode == EditorInfo.IME_ACTION_DONE) {
      val questionPlayerFragment = activity
        .supportFragmentManager
        .findFragmentById(
          R.id.question_player_fragment_placeholder
        ) as? QuestionPlayerFragment
      questionPlayerFragment?.handleKeyboardAction()
    }
  }

  private fun getQuestionPlayerFragment(): QuestionPlayerFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.question_player_fragment_placeholder
    ) as QuestionPlayerFragment?
  }

  fun revealHint(saveUserChoice: Boolean, hintIndex: Int) {
    val questionPlayerFragment =
      activity.supportFragmentManager.findFragmentByTag(TAG_QUESTION_PLAYER_FRAGMENT) as QuestionPlayerFragment
    questionPlayerFragment.revealHint(saveUserChoice, hintIndex)
  }

  fun revealSolution(saveUserChoice: Boolean) {
    val questionPlayerFragment =
      activity.supportFragmentManager.findFragmentByTag(TAG_QUESTION_PLAYER_FRAGMENT) as QuestionPlayerFragment
    questionPlayerFragment.revealSolution(saveUserChoice)
  }
}
