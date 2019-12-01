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
      val skillIds = activity.intent.getStringArrayListExtra(QUESTION_PLAYER_ACTIVITY_SKILL_ID_LIST_ARGUMENT_KEY)
      questionTrainingController.startQuestionTrainingSession(skillIds).observe(activity, Observer {
        if (it.isSuccess()) {
          activity.supportFragmentManager.beginTransaction().add(
            R.id.question_player_fragment_placeholder,
            QuestionPlayerFragment()
          ).commitNow()
        }
      })
    }
  }

  fun stopTrainingSession() {
    questionTrainingController.stopQuestionTrainingSession().observe(activity, Observer<AsyncResult<Any?>> {
      when {
        it.isPending() -> logger.d("QuestionPlayerActivity", "Stopping training session")
        it.isFailure() -> logger.e("QuestionPlayerActivity", "Failed to stop training session", it.getErrorOrNull()!!)
        else -> {
          logger.d("QuestionPlayerActivity", "Successfully stopped training session")
          activity.finish()
        }
      }
    })
  }

  fun onKeyboardAction(actionCode: Int) {
    if (actionCode == EditorInfo.IME_ACTION_DONE) {
      val questionPlayerFragment = activity.supportFragmentManager.findFragmentById(
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
}
