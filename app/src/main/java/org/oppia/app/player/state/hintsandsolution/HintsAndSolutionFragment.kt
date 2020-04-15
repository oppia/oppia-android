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
private const val KEY_EXPLORATION_ID = "EXPLORATION_ID"
private const val KEY_NEW_AVAILABLE_HINT_INDEX = "NEW_AVAILABLE_HINT_INDEX"
private const val KEY_ALL_HINTS_EXHAUSTED = "ALL_HINTS_EXHAUSTED"

/* Fragment that displays a fullscreen dialog for Hints and Solutions. */
class HintsAndSolutionFragment : InjectableDialogFragment(), ExpandedHintListIndexListener,
  RevealSolutionInterface {

  @Inject lateinit var hintsAndSolutionFragmentPresenter: HintsAndSolutionFragmentPresenter

  private var currentExpandedHintListIndex: Int? = null

  companion object {
    /**
     * Creates a new instance of a DialogFragment to display hints and solution
     * @param explorationId Used in ExplorationController to get current state data.
     * @param newAvailableHintIndex Index of new available hint.
     * @param allHintsExhausted Boolean set to true when all hints are exhausted.
     * @return [HintsAndSolutionFragment]: DialogFragment
     */
    fun newInstance(
      explorationId: String,
      newAvailableHintIndex: Int,
      allHintsExhausted: Boolean
    ): HintsAndSolutionFragment {
      val hintsAndSolutionFrag = HintsAndSolutionFragment()
      val args = Bundle()
      args.putString(KEY_EXPLORATION_ID, explorationId)
      args.putInt(KEY_NEW_AVAILABLE_HINT_INDEX, newAvailableHintIndex)
      args.putBoolean(KEY_ALL_HINTS_EXHAUSTED, allHintsExhausted)
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
    val explorationId =
      checkNotNull(args.getString(KEY_EXPLORATION_ID)) { "Expected explorationId to be passed to HintsAndSolutionFragment" }
    val newAvailableHintIndex =
      checkNotNull(args.getInt(KEY_NEW_AVAILABLE_HINT_INDEX)) { "Expected explorationId to be passed to HintsAndSolutionFragment" }
    val allHintsExhausted =
      checkNotNull(args.getBoolean(KEY_ALL_HINTS_EXHAUSTED)) { "Expected explorationId to be passed to HintsAndSolutionFragment" }
    return hintsAndSolutionFragmentPresenter.handleCreateView(
      inflater,
      container,
      explorationId,
      currentExpandedHintListIndex,
      newAvailableHintIndex,
      allHintsExhausted,
      this as ExpandedHintListIndexListener
    )
  }

  override fun onStart() {
    super.onStart()
    dialog?.window?.setWindowAnimations(R.style.FullScreenDialogStyle)
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
