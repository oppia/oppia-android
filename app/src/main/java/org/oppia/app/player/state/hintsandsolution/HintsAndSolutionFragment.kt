package org.oppia.app.player.state.hintsandsolution

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.R
import org.oppia.app.fragment.InjectableDialogFragment
import org.oppia.app.model.State
import javax.inject.Inject

private const val KEY_CURRENT_EXPANDED_LIST_INDEX = "CURRENT_EXPANDED_LIST_INDEX"
private const val KEY_ON_ORIENTATION_CHANGE = "ON_ORIENTATION_CHANGE"

/* Fragment that displays a fullscreen dialog for Hints and Solutions. */
class HintsAndSolutionFragment : InjectableDialogFragment(), ExpandedHintListIndexListener, RevealSolutionInterface {

  @Inject lateinit var hintsAndSolutionFragmentPresenter: HintsAndSolutionFragmentPresenter

  private  var currentState: State? = null
  private  var explorationId: String? = ""
  private var newAvailableHintIndex: Int = -1
  private var allHintsExhausted: Boolean = false
  private var isOrientationChanged: Boolean = false

  private var currentExpandedHintListIndex: Int? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.FullScreenHintDialogStyle)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    if (savedInstanceState != null) {
      currentExpandedHintListIndex = savedInstanceState.getInt(KEY_CURRENT_EXPANDED_LIST_INDEX, -1)
      isOrientationChanged = savedInstanceState.getBoolean(KEY_ON_ORIENTATION_CHANGE, false)
      if (currentExpandedHintListIndex == -1) {
        currentExpandedHintListIndex = null
      }
    }
    return hintsAndSolutionFragmentPresenter.handleCreateView(
      inflater,
      container,
      isOrientationChanged,
      currentState,
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

  fun setStateAndExplorationId(
    newState: State,
    explorationId: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  ) {
    this.currentState = newState
    this.explorationId = explorationId
    this.newAvailableHintIndex = newAvailableHintIndex
    this.allHintsExhausted = allHintsExhausted
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (currentExpandedHintListIndex != null) {
      outState.putInt(KEY_CURRENT_EXPANDED_LIST_INDEX, currentExpandedHintListIndex!!)
    }
    outState.putBoolean(KEY_ON_ORIENTATION_CHANGE, true)
  }

  override fun onExpandListIconClicked(index: Int?) {
    currentExpandedHintListIndex = index
  }

  override fun revealSolution(saveUserChoice: Boolean) {
    hintsAndSolutionFragmentPresenter.handleRevealSolution(saveUserChoice)
  }
}
