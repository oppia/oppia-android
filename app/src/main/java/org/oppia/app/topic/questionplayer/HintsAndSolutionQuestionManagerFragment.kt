package org.oppia.app.topic.questionplayer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.EphemeralQuestion
import org.oppia.domain.question.QuestionAssessmentProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val KEY_QUESTION_ID = "QUESTION_ID"
private const val KEY_NEW_AVAILABLE_HINT_INDEX = "NEW_AVAILABLE_HINT_INDEX"
private const val KEY_ALL_HINTS_EXHAUSTED = "ALL_HINTS_EXHAUSTED"

/**
 * ManagerFragment of [QuestionFragment] that observes data provider that retrieve Question State.
 */
class HintsAndSolutionQuestionManagerFragment : InjectableFragment() {
  @Inject
  lateinit var hintsAndSolutionQuestionManagerFragmentPresenter: HintsAndSolutionQuestionManagerFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to HintsAndSolutionFragment" }
    val id =
      checkNotNull(args.getString(KEY_QUESTION_ID)) { "Expected id to be passed to HintsAndSolutionFragment" }
    val newAvailableHintIndex =
      checkNotNull(args.getInt(KEY_NEW_AVAILABLE_HINT_INDEX)) { "Expected hint index to be passed to HintsAndSolutionFragment" }
    val allHintsExhausted =
      checkNotNull(args.getBoolean(KEY_ALL_HINTS_EXHAUSTED)) { "Expected if hints exhausted to be passed to HintsAndSolutionFragment" }

    return hintsAndSolutionQuestionManagerFragmentPresenter.handleCreateView(
      id,
      newAvailableHintIndex,
      allHintsExhausted
    )
  }

  companion object {

    fun newInstance(
      QuestionId: String,
      newAvailableHintIndex: Int,
      allHintsExhausted: Boolean
    ): HintsAndSolutionQuestionManagerFragment {
      val questionManagerFragment = HintsAndSolutionQuestionManagerFragment()
      val args = Bundle()
      args.putString(KEY_QUESTION_ID, QuestionId)
      args.putInt(KEY_NEW_AVAILABLE_HINT_INDEX, newAvailableHintIndex)
      args.putBoolean(KEY_ALL_HINTS_EXHAUSTED, allHintsExhausted)
      questionManagerFragment.arguments = args
      return questionManagerFragment
    }
  }
}

@FragmentScope
class HintsAndSolutionQuestionManagerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val logger: Logger,
  private val questionProgressController: QuestionAssessmentProgressController
) {
  private var questionId = ""
  private var newAvailableHintIndex = 0
  private var allHintsExhausted = false

  private val ephemeralStateLiveData: LiveData<AsyncResult<EphemeralQuestion>> by lazy {
    questionProgressController.getCurrentQuestion()
  }

  fun handleCreateView(
    id: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  ): View? {
    this.questionId = id
    this.newAvailableHintIndex = newAvailableHintIndex
    this.allHintsExhausted = allHintsExhausted
    subscribeToCurrentQuestionState()

    return null // Headless fragment.
  }

  private fun subscribeToCurrentQuestionState() {
    ephemeralStateLiveData.observe(activity, Observer<AsyncResult<EphemeralQuestion>> { result ->
      processEphemeralStateResult(result)
    })
  }

  private fun processEphemeralStateResult(result: AsyncResult<EphemeralQuestion>) {
    if (result.isFailure()) {
      logger.e("StateFragment", "Failed to retrieve ephemeral state", result.getErrorOrNull()!!)
      return
    } else if (result.isPending()) {
      // Display nothing until a valid result is available.
      return
    }

    val ephemeralQuestionState = result.getOrThrow()

    // Check if hints are available for this state.
    if (ephemeralQuestionState.ephemeralState.state.interaction.hintList.size != 0) {
      (activity as HintsAndSolutionQuestionManagerListener).onQuestionStateLoaded(
        ephemeralQuestionState.ephemeralState.state,
        questionId,
        newAvailableHintIndex,
        allHintsExhausted
      )
    }
  }
}
