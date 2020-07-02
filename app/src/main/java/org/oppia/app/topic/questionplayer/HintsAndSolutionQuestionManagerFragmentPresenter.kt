package org.oppia.app.topic.questionplayer

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.EphemeralQuestion
import org.oppia.domain.question.QuestionAssessmentProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.ConsoleLogger
import javax.inject.Inject

/** The presenter for [HintsAndSolutionQuestionManagerFragment]. */
@FragmentScope
class HintsAndSolutionQuestionManagerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val logger: ConsoleLogger,
  private val questionProgressController: QuestionAssessmentProgressController
) {

  private val ephemeralStateLiveData: LiveData<AsyncResult<EphemeralQuestion>> by lazy {
    questionProgressController.getCurrentQuestion()
  }

  fun handleCreateView(): View? {
    subscribeToCurrentQuestionState()

    return null // Headless fragment.
  }

  private fun subscribeToCurrentQuestionState() {
    ephemeralStateLiveData.observe(
      activity,
      Observer<AsyncResult<EphemeralQuestion>> { result ->
        processEphemeralStateResult(result)
      }
    )
  }

  private fun processEphemeralStateResult(result: AsyncResult<EphemeralQuestion>) {
    if (result.isFailure()) {
      logger.e(
        "HintsAndSolutionQuestionManagerFragmentPresenter",
        "Failed to retrieve ephemeral state", result.getErrorOrNull()!!
      )
      return
    } else if (result.isPending()) {
      // Display nothing until a valid result is available.
      return
    }

    val ephemeralQuestionState = result.getOrThrow()

    // Check if hints are available for this state.
    if (ephemeralQuestionState.ephemeralState.state.interaction.hintList.size != 0) {
      (activity as HintsAndSolutionQuestionManagerListener).onQuestionStateLoaded(
        ephemeralQuestionState.ephemeralState.state
      )
    }
  }
}
