package org.oppia.app.player.state

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.fragment.app.Fragment
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.audio.CellularDataInterface
import javax.inject.Inject

private const val CONTINUE = "Continue"
private const val END_EXPLORATION = "EndExploration"
private const val MULTIPLE_CHOICE_INPUT = "MultipleChoiceInput"
private const val ITEM_SELECT_INPUT = "ItemSelectionInput"
private const val TEXT_INPUT = "TextInput"
private const val FRACTION_INPUT = "FractionInput"
private const val NUMERIC_INPUT = "NumericInput"
private const val NUMERIC_WITH_UNITS = "NumberWithUnits"
private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"

/** Fragment that represents the current state of an exploration. */
class StateFragment : InjectableFragment(), CellularDataInterface {
  @Inject
  lateinit var stateFragmentPresenter: StateFragmentPresenter
  private lateinit var cellularDataInterface: CellularDataInterface
  // Control this boolean value from controllers in domain module.
  private var showCellularDataDialog = true
  var isAudioFragmentShowing = ObservableField<Boolean>(false)

  private lateinit var dummyStateIndex: ArrayList<Int>
  private lateinit var dummyInteractionId: ArrayList<String>

  val currentStateIndex = ObservableInt(0)
  // Indicates the maximum state-index learner has reached.
  // This variable helps in figuring out whether to show "Next" button or not
  private val maxLearnerProgressIndex = ObservableInt(0)
  var isNextButtonVisible = ObservableField<Boolean>(false)
  var isContinueButtonVisible = ObservableField<Boolean>(false)
  var isActiveSubmitButtonVisible = ObservableField<Boolean>(false)
  var isInactiveSubmitButtonVisible = ObservableField<Boolean>(false)
  var isLearnAgainButtonVisible = ObservableField<Boolean>(false)
  var isEndExplorationButtonVisible = ObservableField<Boolean>(false)

  init {
    // Add code to control the value of showCellularDataDialog using AudioController.
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    onAttachToParentFragment(this)
    createDummyDataForButtonCheck()
    setCurrentStateIndex(dummyStateIndex.get(0), dummyInteractionId.get(0))
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return stateFragmentPresenter.handleCreateView(inflater, container)
  }

  private fun onAttachToParentFragment(fragment: Fragment) {
    try {
      cellularDataInterface = fragment as CellularDataInterface
    } catch (e: ClassCastException) {
      throw ClassCastException(
        "$fragment must implement CellularDataInterface"
      )
    }
  }

  fun dummyButtonClicked() {
    if (showCellularDataDialog && !isAudioFragmentShowing.get()!!) {
      showCellularDataDialogFragment()
    } else {
      isAudioFragmentShowing.set(false)
    }
  }

  private fun showCellularDataDialogFragment() {
    val previousFragment = fragmentManager?.findFragmentByTag(TAG_CELLULAR_DATA_DIALOG)
    if (previousFragment != null) {
      fragmentManager?.beginTransaction()?.remove(previousFragment)?.commitNow()
    }
    val dialogFragment = CellularDataDialogFragment.newInstance(
      cellularDataInterface
    )
    dialogFragment.showNow(fragmentManager, TAG_CELLULAR_DATA_DIALOG)
  }

  override fun enableAudioWhileOnCellular(doNotShowAgain: Boolean) {
    isAudioFragmentShowing.set(true)
    // doNotShowAgain -> true -> save this preference
    // doNotShowAgain -> false -> do not save this preference
  }

  override fun disableAudioWhileOnCellular(doNotShowAgain: Boolean) {
    // doNotShowAgain -> true -> save this preference
    // doNotShowAgain -> false -> do not save this preference
  }

  // This method will be deleted in final implementation
  private fun createDummyDataForButtonCheck() {
    dummyStateIndex = ArrayList()
    dummyInteractionId = ArrayList()

    dummyStateIndex.add(0)
    dummyInteractionId.add("Continue")
    dummyStateIndex.add(1)
    dummyInteractionId.add("MultipleChoiceInput")
    dummyStateIndex.add(2)
    dummyInteractionId.add("TextInput")
    dummyStateIndex.add(3)
    dummyInteractionId.add("Continue")
    dummyStateIndex.add(4)
    dummyInteractionId.add("ItemSelectionInput")
    dummyStateIndex.add(5)
    dummyInteractionId.add("EndExploration")
  }

  private fun setCurrentStateIndex(stateIndex: Int, interactionId: String) {
    setAllButtonVisibilityGone()
    currentStateIndex.set(stateIndex)
    if (maxLearnerProgressIndex.get() > currentStateIndex.get()) {
      isNextButtonVisible.set(true)
    } else {
      maxLearnerProgressIndex.set(stateIndex)
      isNextButtonVisible.set(false)
      if (interactionId == CONTINUE) {
        isContinueButtonVisible.set(true)
      } else if (interactionId == MULTIPLE_CHOICE_INPUT || interactionId == ITEM_SELECT_INPUT
        || interactionId == TEXT_INPUT || interactionId == FRACTION_INPUT
        || interactionId == NUMERIC_INPUT || interactionId == NUMERIC_WITH_UNITS
      ) {
        isInactiveSubmitButtonVisible.set(true)
      } else if (interactionId == END_EXPLORATION) {
        isEndExplorationButtonVisible.set(true)
      } else {
        isInactiveSubmitButtonVisible.set(true)
      }
    }
  }

  private fun setAllButtonVisibilityGone() {
    isNextButtonVisible.set(false)
    isContinueButtonVisible.set(false)
    isActiveSubmitButtonVisible.set(false)
    isInactiveSubmitButtonVisible.set(false)
    isLearnAgainButtonVisible.set(false)
    isEndExplorationButtonVisible.set(false)
  }

  private fun nextState() {
    val nextStateIndex = currentStateIndex.get() + 1
    if (nextStateIndex < dummyStateIndex.size) {
      setCurrentStateIndex(dummyStateIndex[nextStateIndex], dummyInteractionId[nextStateIndex])
    }
  }

  private fun previousState() {
    val prevStateIndex = currentStateIndex.get() - 1
    if (prevStateIndex >= 0) {
      setCurrentStateIndex(dummyStateIndex[prevStateIndex], dummyInteractionId[prevStateIndex])
    }
  }

  fun continueButtonClicked() {
    nextState()
  }

  fun nextButtonClicked() {
    nextState()
  }

  fun previousButtonClicked() {
    previousState()
  }

  fun submitButtonClicked() {
    nextState()
  }

  fun endExplorationButtonClicked() {
  }

  fun learnAgainButtonClicked() {
  }

  fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    if (isActiveSubmitButtonVisible.get()!! || isInactiveSubmitButtonVisible.get()!!) {
      if (s.isNotEmpty()) {
        isInactiveSubmitButtonVisible.set(false)
        isActiveSubmitButtonVisible.set(true)
      } else {
        isInactiveSubmitButtonVisible.set(true)
        isActiveSubmitButtonVisible.set(false)
      }
    }
  }
}

