package org.oppia.android.app.topic.questionplayer

import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.databinding.QuestionPlayerActivityBinding
import org.oppia.android.domain.question.QuestionTrainingController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

const val QUESTION_PLAYER_FRAGMENT_EXTRA_KEY =
  "QuestionPlayerActivityPresenter_question_player_fragment"
private const val HINTS_AND_SOLUTION_QUESTION_MANAGER_EXTRA_KEY =
  "QuestionPlayerActivityPresenter_hints_and_solution_question_manager"

/** The presenter for [QuestionPlayerActivity]. */
@ActivityScope
class QuestionPlayerActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val questionTrainingController: QuestionTrainingController,
  private val logger: ConsoleLogger
) {
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<QuestionPlayerActivityBinding>(
      activity,
      R.layout.question_player_activity
    )

    binding.apply {
      lifecycleOwner = activity
    }

    activity.setSupportActionBar(binding.questionPlayerToolbar)

    binding.questionPlayerToolbar.setNavigationOnClickListener {
      activity.onBackPressed()
    }

    if (getQuestionPlayerFragment() == null) {
      startTrainingSessionWithCallback {
        activity.supportFragmentManager.beginTransaction().add(
          R.id.question_player_fragment_placeholder,
          QuestionPlayerFragment(),
          QUESTION_PLAYER_FRAGMENT_EXTRA_KEY
        ).commitNow()
      }
    }

    if (getHintsAndSolutionExplorationManagerFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.question_player_fragment_placeholder,
        HintsAndSolutionQuestionManagerFragment()
      ).commitNow()
    }
  }

  private fun getHintsAndSolutionExplorationManagerFragment(): HintsAndSolutionQuestionManagerFragment? { // ktlint-disable max-line-length
    return activity.supportFragmentManager.findFragmentByTag(
      HINTS_AND_SOLUTION_QUESTION_MANAGER_EXTRA_KEY
    ) as HintsAndSolutionQuestionManagerFragment?
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
          QUESTION_PLAYER_FRAGMENT_EXTRA_KEY
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
        .findFragmentByTag(
          QUESTION_PLAYER_FRAGMENT_EXTRA_KEY
        ) as? QuestionPlayerFragment
      questionPlayerFragment?.handleKeyboardAction()
    }
  }

  private fun getQuestionPlayerFragment(): QuestionPlayerFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      QUESTION_PLAYER_FRAGMENT_EXTRA_KEY
    ) as QuestionPlayerFragment?
  }

  fun revealHint(saveUserChoice: Boolean, hintIndex: Int) {
    val questionPlayerFragment =
      activity.supportFragmentManager.findFragmentByTag(
        QUESTION_PLAYER_FRAGMENT_EXTRA_KEY
      ) as QuestionPlayerFragment
    questionPlayerFragment.revealHint(saveUserChoice, hintIndex)
  }

  fun revealSolution() {
    val questionPlayerFragment =
      activity.supportFragmentManager.findFragmentByTag(
        QUESTION_PLAYER_FRAGMENT_EXTRA_KEY
      ) as QuestionPlayerFragment
    questionPlayerFragment.revealSolution()
  }

  fun dismissConceptCard() = getQuestionPlayerFragment()?.dismissConceptCard()
}
