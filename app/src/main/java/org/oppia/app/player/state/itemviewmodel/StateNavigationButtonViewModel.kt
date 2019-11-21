package org.oppia.app.player.state.itemviewmodel

import android.content.Context
import android.widget.Button
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.player.state.listener.StateNavigationButtonListener

/** [ViewModel] for State navigation buttons. */
class StateNavigationButtonViewModel(
  val context: Context, val stateNavigationButtonListener: StateNavigationButtonListener
) : StateItemViewModel(ViewType.STATE_NAVIGATION_BUTTON) {
  companion object {
    @JvmStatic
    @BindingAdapter("android:button")
    fun setBackgroundResource(button: Button, resource: Int) {
      button.setBackgroundResource(resource)
    }
  }

  private var currentContinuationNavigationButtonType: ContinuationNavigationButtonType =
    ContinuationNavigationButtonType.NO_CONTINUATION_BUTTON

  var isNextButtonVisible = ObservableField<Boolean>(false)
  var isPreviousButtonVisible = ObservableField<Boolean>(false)

  var isInteractionButtonActive = ObservableField<Boolean>(false)
  var isInteractionButtonVisible = ObservableField<Boolean>(false)
  var drawableResourceValue = ObservableField<Int>(R.drawable.state_button_primary_background)

  var interactionButtonName = ObservableField<String>()

  fun updatePreviousButton(isEnabled: Boolean) {
    isPreviousButtonVisible.set(isEnabled)
  }

  fun updateContinuationButton(
    continuationNavigationButtonType: ContinuationNavigationButtonType, isEnabled: Boolean
  ) {
    currentContinuationNavigationButtonType = continuationNavigationButtonType
    when (continuationNavigationButtonType) {
      ContinuationNavigationButtonType.NEXT_BUTTON -> {
        isInteractionButtonActive.set(false)
        isInteractionButtonVisible.set(false)
        isNextButtonVisible.set(isEnabled)
      }
      ContinuationNavigationButtonType.SUBMIT_BUTTON -> {
        isNextButtonVisible.set(false)
        isInteractionButtonActive.set(isEnabled)
        isInteractionButtonVisible.set(isEnabled)
        interactionButtonName.set(context.getString(R.string.state_submit_button))
        drawableResourceValue.set(R.drawable.state_button_primary_background)
      }
      ContinuationNavigationButtonType.CONTINUE_BUTTON -> {
        isNextButtonVisible.set(false)
        isInteractionButtonActive.set(isEnabled)
        isInteractionButtonVisible.set(isEnabled)
        interactionButtonName.set(context.getString(R.string.state_continue_button))
        drawableResourceValue.set(R.drawable.state_button_primary_background)
      }
      ContinuationNavigationButtonType.RETURN_TO_TOPIC_BUTTON -> {
        isNextButtonVisible.set(false)
        isInteractionButtonActive.set(isEnabled)
        isInteractionButtonVisible.set(isEnabled)
        interactionButtonName.set(context.getString(R.string.state_end_exploration_button))
        drawableResourceValue.set(R.drawable.state_button_primary_background)
      }
      ContinuationNavigationButtonType.NO_CONTINUATION_BUTTON -> {
        isInteractionButtonActive.set(false)
        isInteractionButtonVisible.set(false)
        isNextButtonVisible.set(false)
      }
    }
  }

  fun triggerContinuationNavigationButtonCallback() {
    when (currentContinuationNavigationButtonType) {
      ContinuationNavigationButtonType.NEXT_BUTTON -> stateNavigationButtonListener.onNextButtonClicked()
      ContinuationNavigationButtonType.SUBMIT_BUTTON -> stateNavigationButtonListener.onSubmitButtonClicked()
      ContinuationNavigationButtonType.CONTINUE_BUTTON -> stateNavigationButtonListener.onContinueButtonClicked()
      ContinuationNavigationButtonType.RETURN_TO_TOPIC_BUTTON -> {
        stateNavigationButtonListener.onReturnToTopicButtonClicked()
      }
      else -> throw IllegalStateException(
        "Cannot trigger continuation for current button state: $currentContinuationNavigationButtonType"
      )
    }
  }

  /** The type of the state continue navigation button being shown. */
  enum class ContinuationNavigationButtonType {
    NO_CONTINUATION_BUTTON,
    NEXT_BUTTON,
    SUBMIT_BUTTON,
    CONTINUE_BUTTON,
    RETURN_TO_TOPIC_BUTTON
  }
}
