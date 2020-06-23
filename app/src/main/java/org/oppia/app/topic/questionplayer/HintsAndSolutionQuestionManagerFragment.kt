package org.oppia.app.topic.questionplayer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import javax.inject.Inject

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
      checkNotNull(args.getString(QUESTION_ID_ARGUMENT_KEY)) { "Expected id to be passed to HintsAndSolutionFragment" }
    val newAvailableHintIndex =
      checkNotNull(args.getInt(NEW_AVAILABLE_HINT_INDEX_ARGUMENT_KEY)) { "Expected hint index to be passed to HintsAndSolutionFragment" }
    val allHintsExhausted =
      checkNotNull(args.getBoolean(ALL_HINTS_EXHAUSTED_ARGUMENT_KEY)) { "Expected if hints exhausted to be passed to HintsAndSolutionFragment" }

    return hintsAndSolutionQuestionManagerFragmentPresenter.handleCreateView(
      id,
      newAvailableHintIndex,
      allHintsExhausted
    )
  }

  companion object {

    internal const val QUESTION_ID_ARGUMENT_KEY = "EXPLORATION_ID"
    internal const val NEW_AVAILABLE_HINT_INDEX_ARGUMENT_KEY = "NEW_AVAILABLE_HINT_INDEX"
    internal const val ALL_HINTS_EXHAUSTED_ARGUMENT_KEY = "ALL_HINTS_EXHAUSTED"
    
    fun newInstance(
      QuestionId: String,
      newAvailableHintIndex: Int,
      allHintsExhausted: Boolean
    ): HintsAndSolutionQuestionManagerFragment {
      val questionManagerFragment = HintsAndSolutionQuestionManagerFragment()
      val args = Bundle()
      args.putString(QUESTION_ID_ARGUMENT_KEY, QuestionId)
      args.putInt(NEW_AVAILABLE_HINT_INDEX_ARGUMENT_KEY, newAvailableHintIndex)
      args.putBoolean(ALL_HINTS_EXHAUSTED_ARGUMENT_KEY, allHintsExhausted)
      questionManagerFragment.arguments = args
      return questionManagerFragment
    }
  }
}

