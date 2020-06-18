package org.oppia.app.player.state.hintsandsolution

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.R
import org.oppia.app.fragment.InjectableDialogFragment
import javax.inject.Inject

private const val KEY_CURRENT_EXPANDED_LIST_INDEX = "CURRENT_EXPANDED_LIST_INDEX"
private const val KEY_ID = "ID"
private const val KEY_NEW_AVAILABLE_HINT_INDEX = "NEW_AVAILABLE_HINT_INDEX"
private const val KEY_ALL_HINTS_EXHAUSTED = "ALL_HINTS_EXHAUSTED"
private const val KEY_IS_IN_TRAIN_MODE = "IS_IN_TRAIN_MODE"

/* Fragment that displays a fullscreen dialog for Hints and Solutions. */
class HintsAndSolutionFragment : InjectableDialogFragment(), ExpandedHintListIndexListener,
  RevealSolutionInterface {

  @Inject
  lateinit var hintsAndSolutionFragmentPresenter: HintsAndSolutionFragmentPresenter

  private var currentExpandedHintListIndex: Int? = null

  companion object {
    /**
     * Creates a new instance of a DialogFragment to display hints and solution
     * @param id Used in ExplorationController/QuestionAssessmentProgressController to get current state data.
     * @param newAvailableHintIndex Index of new available hint.
     * @param allHintsExhausted Boolean set to true when all hints are exhausted.
     * @return [HintsAndSolutionFragment]: DialogFragment
     */
    fun newInstance(
      id: String,
      newAvailableHintIndex: Int,
      allHintsExhausted: Boolean,
      isInTrainMode: Boolean
    ): HintsAndSolutionFragment {
      val hintsAndSolutionFrag = HintsAndSolutionFragment()
      val args = Bundle()
      args.putString(KEY_ID, id)
      args.putInt(KEY_NEW_AVAILABLE_HINT_INDEX, newAvailableHintIndex)
      args.putBoolean(KEY_ALL_HINTS_EXHAUSTED, allHintsExhausted)
      args.putBoolean(KEY_IS_IN_TRAIN_MODE, isInTrainMode)
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
      currentExpandedHintListIndex = savedInstanceState.getInt(KEY_CURRENT_EXPANDED_LIST_INDEX, -1)
      if (currentExpandedHintListIndex == -1) {
        currentExpandedHintListIndex = null
      }
    }
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to HintsAndSolutionFragment" }
    val id =
      checkNotNull(args.getString(KEY_ID)) { "Expected id to be passed to HintsAndSolutionFragment" }
    val newAvailableHintIndex =
      checkNotNull(args.getInt(KEY_NEW_AVAILABLE_HINT_INDEX)) { "Expected hint index to be passed to HintsAndSolutionFragment" }
    val allHintsExhausted =
      checkNotNull(args.getBoolean(KEY_ALL_HINTS_EXHAUSTED)) { "Expected if hints exhausted to be passed to HintsAndSolutionFragment" }
    val isInTrainMode =
      checkNotNull(args.getBoolean(KEY_IS_IN_TRAIN_MODE)) { "Expected if it is in Train mode to be passed to HintsAndSolutionFragment" }
    return hintsAndSolutionFragmentPresenter.handleCreateView(
      inflater,
      container,
      id,
      currentExpandedHintListIndex,
      newAvailableHintIndex,
      allHintsExhausted,
      isInTrainMode,
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
      outState.putInt(KEY_CURRENT_EXPANDED_LIST_INDEX, currentExpandedHintListIndex!!)
    }
  }

  override fun onExpandListIconClicked(index: Int?) {
    currentExpandedHintListIndex = index
    hintsAndSolutionFragmentPresenter.onExpandClicked(index)
  }

  override fun revealSolution(saveUserChoice: Boolean) {
    hintsAndSolutionFragmentPresenter.handleRevealSolution(saveUserChoice)
  }
}
