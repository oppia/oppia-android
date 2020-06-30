package org.oppia.app.hintsandsolution

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import javax.inject.Inject
import org.oppia.app.R
import org.oppia.app.fragment.InjectableDialogFragment
import org.oppia.app.model.State

private const val CURRENT_EXPANDED_LIST_INDEX_SAVED_KEY = "CURRENT_EXPANDED_LIST_INDEX"

/* Fragment that displays a fullscreen dialog for Hints and Solutions. */
class HintsAndSolutionDialogFragment : InjectableDialogFragment(), ExpandedHintListIndexListener,
  RevealSolutionInterface {

  @Inject
  lateinit var hintsAndSolutionDialogFragmentPresenter: HintsAndSolutionDialogFragmentPresenter

  private lateinit var state: State

  private var currentExpandedHintListIndex: Int? = null

  companion object {

    internal const val ID_ARGUMENT_KEY = "ID"
    internal const val NEW_AVAILABLE_HINT_INDEX_ARGUMENT_KEY = "NEW_AVAILABLE_HINT_INDEX"
    internal const val ALL_HINTS_EXHAUSTED_ARGUMENT_KEY = "ALL_HINTS_EXHAUSTED"
    /**
     * Creates a new instance of a DialogFragment to display hints and solution
     * @param id Used in ExplorationController/QuestionAssessmentProgressController to get current state data.
     * @param newAvailableHintIndex Index of new available hint.
     * @param allHintsExhausted Boolean set to true when all hints are exhausted.
     * @return [HintsAndSolutionDialogFragment]: DialogFragment
     */
    fun newInstance(
      id: String,
      newAvailableHintIndex: Int,
      allHintsExhausted: Boolean
    ): HintsAndSolutionDialogFragment {
      val hintsAndSolutionFrag = HintsAndSolutionDialogFragment()
      val args = Bundle()
      args.putString(ID_ARGUMENT_KEY, id)
      args.putInt(NEW_AVAILABLE_HINT_INDEX_ARGUMENT_KEY, newAvailableHintIndex)
      args.putBoolean(ALL_HINTS_EXHAUSTED_ARGUMENT_KEY, allHintsExhausted)
      hintsAndSolutionFrag.arguments = args
      return hintsAndSolutionFrag
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.FullScreenHintDialogStyle)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    if (savedInstanceState != null) {
      currentExpandedHintListIndex = savedInstanceState.getInt(CURRENT_EXPANDED_LIST_INDEX_SAVED_KEY, -1)
      if (currentExpandedHintListIndex == -1) {
        currentExpandedHintListIndex = null
      }
    }
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to HintsAndSolutionDialogFragment" }
    val id =
      checkNotNull(args.getString(ID_ARGUMENT_KEY)) { "Expected id to be passed to HintsAndSolutionDialogFragment" }
    val newAvailableHintIndex =
      checkNotNull(args.getInt(NEW_AVAILABLE_HINT_INDEX_ARGUMENT_KEY)) { "Expected hint index to be passed to HintsAndSolutionDialogFragment" }
    val allHintsExhausted =
      checkNotNull(args.getBoolean(ALL_HINTS_EXHAUSTED_ARGUMENT_KEY)) { "Expected if hints exhausted to be passed to HintsAndSolutionDialogFragment" }

    return hintsAndSolutionDialogFragmentPresenter.handleCreateView(
      inflater,
      container,
      state,
      id,
      currentExpandedHintListIndex,
      newAvailableHintIndex,
      allHintsExhausted,
      this as ExpandedHintListIndexListener
    )
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setWindowAnimations(R.style.FullScreenHintDialogStyle)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (currentExpandedHintListIndex != null) {
      outState.putInt(CURRENT_EXPANDED_LIST_INDEX_SAVED_KEY, currentExpandedHintListIndex!!)
    }
  }

  override fun onExpandListIconClicked(index: Int?) {
    currentExpandedHintListIndex = index
    hintsAndSolutionDialogFragmentPresenter.onExpandClicked(index)
  }

  override fun revealSolution(saveUserChoice: Boolean) {
    hintsAndSolutionDialogFragmentPresenter.handleRevealSolution(saveUserChoice)
  }

  fun loadState(state: State) {
    this.state = state
  }
}
