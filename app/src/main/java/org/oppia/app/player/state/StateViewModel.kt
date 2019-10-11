package org.oppia.app.player.state

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

private const val CONTINUE = "Continue"
private const val END_EXPLORATION = "EndExploration"
private const val LEARN_AGAIN = "LearnAgain"
private const val MULTIPLE_CHOICE_INPUT = "MultipleChoiceInput"
private const val ITEM_SELECT_INPUT = "ItemSelectionInput"
private const val TEXT_INPUT = "TextInput"
private const val FRACTION_INPUT = "FractionInput"
private const val NUMERIC_INPUT = "NumericInput"
private const val NUMERIC_WITH_UNITS = "NumberWithUnits"

/** [ViewModel] for state-fragment. */
@FragmentScope
class StateViewModel @Inject constructor(val context: Context) : ViewModel() {
  var isAudioFragmentVisible = ObservableField<Boolean>(false)

  var isNextButtonVisible = ObservableField<Boolean>(false)
  var isPreviousButtonVisible = ObservableField<Boolean>(false)

  var observableInteractionId = ObservableField<String>()
  var isInteractionButtonActive = ObservableField<Boolean>(false)
  var isInteractionButtonVisible = ObservableField<Boolean>(false)

  var name = ObservableField<String>()

  fun setObservableInteractionId(interactionId: String) {
    observableInteractionId.set(interactionId)
    when (interactionId) {
      CONTINUE -> {
        isInteractionButtonActive.set(true)
        name.set(context.getString(R.string.state_continue_button))
      }
      END_EXPLORATION -> {
        isInteractionButtonActive.set(true)
        name.set(context.getString(R.string.state_end_exploration_button))
      }
      LEARN_AGAIN -> {
        isInteractionButtonActive.set(true)
        name.set(context.getString(R.string.state_learn_again_button))
      }
      ITEM_SELECT_INPUT, MULTIPLE_CHOICE_INPUT -> {
        isInteractionButtonActive.set(true)
        name.set(context.getString(R.string.state_submit_button))
      }
      FRACTION_INPUT, NUMERIC_INPUT, NUMERIC_WITH_UNITS, TEXT_INPUT -> {
        isInteractionButtonActive.set(false)
        name.set(context.getString(R.string.state_submit_button))
      }
    }
  }

  fun clearObservableInteractionId() {
    observableInteractionId.set("")
    isInteractionButtonVisible.set(false)
    isInteractionButtonActive.set(false)
  }

  fun setAudioFragmentVisible(isVisible: Boolean) {
    isAudioFragmentVisible.set(isVisible)
  }

  fun setNextButtonVisible(isVisible: Boolean) {
    isNextButtonVisible.set(isVisible)
  }

  fun setPreviousButtonVisible(isVisible: Boolean) {
    isPreviousButtonVisible.set(isVisible)
  }

  fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    if (s.isNotEmpty()) {
      isInteractionButtonActive.set(true)
    } else {
      isInteractionButtonActive.set(false)
    }
  }

  fun optionSelected(isOptionSelected: Boolean) {
    isInteractionButtonVisible.set(isOptionSelected)
  }
}
