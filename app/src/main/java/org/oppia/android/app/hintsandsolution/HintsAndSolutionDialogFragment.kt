package org.oppia.android.app.hintsandsolution

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.R
import org.oppia.android.app.fragment.InjectableDialogFragment
import org.oppia.android.app.model.State
import javax.inject.Inject

private const val CURRENT_EXPANDED_LIST_INDEX_SAVED_KEY =
  "HintsAndSolutionDialogFragment.current_expanded_list_index"
private const val STATE_SAVED_KEY = "HintsAndSolutionDialogFragment.state"
private const val HINT_INDEX_SAVED_KEY = "HintsAndSolutionDialogFragment.hint_index"
private const val IS_HINT_REVEALED_SAVED_KEY = "HintsAndSolutionDialogFragment.is_hint_revealed"
private const val SOLUTION_INDEX_SAVED_KEY = "HintsAndSolutionDialogFragment.solution_index"
private const val IS_SOLUTION_REVEALED_SAVED_KEY =
  "HintsAndSolutionDialogFragment.is_solution_revealed"

/* Fragment that displays a fullscreen dialog for Hints and Solutions. */
class HintsAndSolutionDialogFragment :
  InjectableDialogFragment(),
  ExpandedHintListIndexListener,
  RevealSolutionInterface {

  @Inject
  lateinit var hintsAndSolutionDialogFragmentPresenter: HintsAndSolutionDialogFragmentPresenter

  private lateinit var state: State

  private var currentExpandedHintListIndex: Int? = null

  private var index: Int? = null
  private var isHintRevealed: Boolean? = null
  private var solutionIndex: Int? = null
  private var isSolutionRevealed: Boolean? = null

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
      currentExpandedHintListIndex =
        savedInstanceState.getInt(CURRENT_EXPANDED_LIST_INDEX_SAVED_KEY, -1)
      if (currentExpandedHintListIndex == -1) {
        currentExpandedHintListIndex = null
      }
      state = State.parseFrom(savedInstanceState.getByteArray(STATE_SAVED_KEY)!!)
      index = savedInstanceState.getInt(HINT_INDEX_SAVED_KEY, -1)
      if (index == -1) index = null
      isHintRevealed = savedInstanceState.getBoolean(IS_HINT_REVEALED_SAVED_KEY, false)
      solutionIndex = savedInstanceState.getInt(SOLUTION_INDEX_SAVED_KEY, -1)
      if (solutionIndex == -1) solutionIndex = null
      isSolutionRevealed = savedInstanceState.getBoolean(IS_SOLUTION_REVEALED_SAVED_KEY, false)
    }
    val args =
      checkNotNull(
        arguments
      ) { "Expected arguments to be passed to HintsAndSolutionDialogFragment" }
    val id =
      checkNotNull(
        args.getString(ID_ARGUMENT_KEY)
      ) { "Expected id to be passed to HintsAndSolutionDialogFragment" }
    val newAvailableHintIndex =
      checkNotNull(
        args.getInt(NEW_AVAILABLE_HINT_INDEX_ARGUMENT_KEY)
      ) { "Expected hint index to be passed to HintsAndSolutionDialogFragment" }
    val allHintsExhausted =
      checkNotNull(
        args.getBoolean(ALL_HINTS_EXHAUSTED_ARGUMENT_KEY)
      ) { "Expected if hints exhausted to be passed to HintsAndSolutionDialogFragment" }

    return hintsAndSolutionDialogFragmentPresenter.handleCreateView(
      inflater,
      container,
      state,
      id,
      currentExpandedHintListIndex,
      newAvailableHintIndex,
      allHintsExhausted,
      this as ExpandedHintListIndexListener,
      index,
      isHintRevealed,
      solutionIndex,
      isSolutionRevealed
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
    if (index != null) {
      outState.putInt(HINT_INDEX_SAVED_KEY, index!!)
    }
    if (isHintRevealed != null) {
      outState.putBoolean(IS_HINT_REVEALED_SAVED_KEY, isHintRevealed!!)
    }
    if (solutionIndex != null) {
      outState.putInt(SOLUTION_INDEX_SAVED_KEY, solutionIndex!!)
    }
    if (isSolutionRevealed != null) {
      outState.putBoolean(IS_SOLUTION_REVEALED_SAVED_KEY, isSolutionRevealed!!)
    }
    outState.putByteArray(STATE_SAVED_KEY, state.toByteArray())
  }

  override fun onExpandListIconClicked(index: Int?) {
    currentExpandedHintListIndex = index
    hintsAndSolutionDialogFragmentPresenter.onExpandClicked(index)
  }

  override fun revealSolution() {
    hintsAndSolutionDialogFragmentPresenter.handleRevealSolution()
  }

  override fun onRevealHintClicked(index: Int?, isHintRevealed: Boolean?) {
    this.index = index
    this.isHintRevealed = isHintRevealed
    hintsAndSolutionDialogFragmentPresenter.onRevealHintClicked(index, isHintRevealed)
  }

  override fun onRevealSolutionClicked(solutionIndex: Int?, isSolutionRevealed: Boolean?) {
    this.solutionIndex = solutionIndex
    this.isSolutionRevealed = isSolutionRevealed
    hintsAndSolutionDialogFragmentPresenter.onRevealSolutionClicked(
      solutionIndex,
      isSolutionRevealed
    )
  }

  fun loadState(state: State) {
    this.state = state
  }
}
