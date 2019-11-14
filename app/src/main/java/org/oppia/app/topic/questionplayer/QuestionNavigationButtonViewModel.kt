package org.oppia.app.topic.questionplayer

import android.content.Context
import android.widget.Button
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel

/** [ViewModel] for State navigation buttons. */
class QuestionNavigationButtonViewModel(
  val context: Context, val questionNavigationButtonListener: QuestionNavigationButtonListener
) : StateItemViewModel() {
  companion object {
    @JvmStatic
    @BindingAdapter("android:button")
    fun setBackgroundResource(button: Button, resource: Int) {
      button.setBackgroundResource(resource)
    }
  }

  private var currentContinuationNavigationButtonType: ContinuationNavigationButtonType =
    ContinuationNavigationButtonType.NO_CONTINUATION_BUTTON

  var isInteractionButtonActive = ObservableField<Boolean>(false)
  var isInteractionButtonVisible = ObservableField<Boolean>(false)
  var drawableResourceValue = ObservableField<Int>(R.drawable.state_button_primary_background)
  var interactionButtonName = ObservableField<String>()
  var isReplayButtonVisible = ObservableField(false)

  fun updateContinuationButton(
    continuationNavigationButtonType: ContinuationNavigationButtonType, isEnabled: Boolean
  ) {
    currentContinuationNavigationButtonType = continuationNavigationButtonType
    when (continuationNavigationButtonType) {
      ContinuationNavigationButtonType.SUBMIT_BUTTON -> {
        isReplayButtonVisible.set(false)
        isInteractionButtonActive.set(isEnabled)
        isInteractionButtonVisible.set(isEnabled)
        interactionButtonName.set(context.getString(R.string.state_submit_button))
        drawableResourceValue.set(R.drawable.state_button_primary_background)
      }
      ContinuationNavigationButtonType.CONTINUE_BUTTON -> {
        isReplayButtonVisible.set(false)
        isInteractionButtonActive.set(isEnabled)
        isInteractionButtonVisible.set(isEnabled)
        interactionButtonName.set(context.getString(R.string.state_continue_button))
        drawableResourceValue.set(R.drawable.state_button_primary_background)
      }
      ContinuationNavigationButtonType.RETURN_TO_TOPIC_BUTTON -> {
        isReplayButtonVisible.set(true)
        isInteractionButtonActive.set(isEnabled)
        isInteractionButtonVisible.set(isEnabled)
        interactionButtonName.set(context.getString(R.string.state_end_exploration_button))
        drawableResourceValue.set(R.drawable.state_button_primary_background)
      }
    }
  }

  fun triggerContinuationNavigationButtonCallback() {
    when (currentContinuationNavigationButtonType) {
      ContinuationNavigationButtonType.SUBMIT_BUTTON -> questionNavigationButtonListener.onSubmitButtonClicked()
      ContinuationNavigationButtonType.CONTINUE_BUTTON -> questionNavigationButtonListener.onContinueButtonClicked()
      ContinuationNavigationButtonType.RETURN_TO_TOPIC_BUTTON -> {
        questionNavigationButtonListener.onReturnToTopicButtonClicked()
      }
      else -> throw IllegalStateException(
        "Cannot trigger continuation for current button state: $currentContinuationNavigationButtonType"
      )
    }
  }

  fun triggerReplayNavigationButtonCallback() {
    questionNavigationButtonListener.onReplayButtonClicked()
  }

  /** The type of the state continue navigation button being shown. */
  enum class ContinuationNavigationButtonType {
    NO_CONTINUATION_BUTTON,
    SUBMIT_BUTTON,
    CONTINUE_BUTTON,
    RETURN_TO_TOPIC_BUTTON
  }
}
