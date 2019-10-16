package org.oppia.app.player.state.itemviewmodel

import android.content.Context
import android.widget.Button
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject
import org.oppia.app.R
import org.oppia.app.viewmodel.ObservableViewModel

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
class StateButtonViewModel @Inject constructor(val context: Context) : ObservableViewModel() {
  companion object {
    @JvmStatic
    @BindingAdapter("buttonDrawable")
    fun setBackgroundResource(button: Button, resource: Int) {
      button.setBackgroundResource(resource)
    }
  }

  var isAudioFragmentVisible = ObservableField<Boolean>(false)

  var isNextButtonVisible = ObservableField<Boolean>(false)
  var isPreviousButtonVisible = ObservableField<Boolean>(false)

  var observableInteractionId = ObservableField<String>()
  var isInteractionButtonActive = ObservableField<Boolean>(false)
  var isInteractionButtonVisible = ObservableField<Boolean>(false)
  var drawableResourceValue = ObservableField<Int>(R.drawable.state_button_primary_background)

  var name = ObservableField<String>()

  fun setObservableInteractionId(interactionId: String) {
    setNextButtonVisible(false)
    observableInteractionId.set(interactionId)
    when (interactionId) {
      CONTINUE -> {
        isInteractionButtonActive.set(true)
        isInteractionButtonVisible.set(true)
        name.set(context.getString(R.string.state_continue_button))
        drawableResourceValue.set(R.drawable.state_button_primary_background)
      }
      END_EXPLORATION -> {
        isInteractionButtonActive.set(true)
        isInteractionButtonVisible.set(true)
        name.set(context.getString(R.string.state_end_exploration_button))
        drawableResourceValue.set(R.drawable.state_button_primary_background)
      }
      LEARN_AGAIN -> {
        isInteractionButtonActive.set(true)
        isInteractionButtonVisible.set(true)
        name.set(context.getString(R.string.state_learn_again_button))
        drawableResourceValue.set(R.drawable.state_button_blue_background)
      }
      ITEM_SELECT_INPUT, MULTIPLE_CHOICE_INPUT -> {
        isInteractionButtonActive.set(true)
        isInteractionButtonVisible.set(false)
        name.set(context.getString(R.string.state_submit_button))
        drawableResourceValue.set(R.drawable.state_button_primary_background)
      }
      FRACTION_INPUT, NUMERIC_INPUT, NUMERIC_WITH_UNITS, TEXT_INPUT -> {
        isInteractionButtonActive.set(false)
        isInteractionButtonVisible.set(true)
        name.set(context.getString(R.string.state_submit_button))
        drawableResourceValue.set(R.drawable.state_button_transparent_background)
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
      drawableResourceValue.set(R.drawable.state_button_primary_background)
    } else {
      isInteractionButtonActive.set(false)
      drawableResourceValue.set(R.drawable.state_button_transparent_background)
    }
  }

  fun optionSelected(isOptionSelected: Boolean) {
    isInteractionButtonVisible.set(isOptionSelected)
  }
}