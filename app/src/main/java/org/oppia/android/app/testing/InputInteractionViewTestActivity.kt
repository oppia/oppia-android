package org.oppia.android.app.testing

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.customview.interaction.FractionInputInteractionView
import org.oppia.android.app.customview.interaction.NumericInputInteractionView
import org.oppia.android.app.customview.interaction.TextInputInteractionView
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.SchemaObject
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.android.app.player.state.itemviewmodel.RatioExpressionInputInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.ActivityInputInteractionViewTestBinding

/**
 * This is a dummy activity to test input interaction views.
 * It contains [FractionInputInteractionView], [NumericInputInteractionView],and [TextInputInteractionView].
 */
class InputInteractionViewTestActivity :
  InjectableAppCompatActivity(),
  StateKeyboardButtonListener,
  InteractionAnswerErrorOrAvailabilityCheckReceiver {
  private lateinit var binding: ActivityInputInteractionViewTestBinding
  lateinit var fractionInteractionViewModel: FractionInteractionViewModel
  lateinit var ratioExpressionInputInteractionViewModel: RatioExpressionInputInteractionViewModel

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  val numericInputViewModel = NumericInputViewModel(
    hasConversationView = false,
    interactionAnswerErrorOrAvailabilityCheckReceiver = this,
    isSplitView = false,
    resourceHandler = resourceHandler
  )

  val textInputViewModel = TextInputViewModel(
    interaction = Interaction.getDefaultInstance(),
    hasConversationView = false,
    interactionAnswerErrorOrAvailabilityCheckReceiver = this,
    isSplitView = false
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    binding = DataBindingUtil.setContentView<ActivityInputInteractionViewTestBinding>(
      this, R.layout.activity_input_interaction_view_test
    )
    fractionInteractionViewModel = FractionInteractionViewModel(
      interaction = Interaction.getDefaultInstance(),
      hasConversationView = false,
      isSplitView = false,
      errorOrAvailabilityCheckReceiver = this,
      resourceHandler = resourceHandler
    )

    ratioExpressionInputInteractionViewModel = RatioExpressionInputInteractionViewModel(
      interaction = Interaction.newBuilder().putCustomizationArgs(
        "numberOfTerms",
        SchemaObject.newBuilder().setSignedInt(3).build()
      ).build(),
      hasConversationView = false,
      isSplitView = false,
      errorOrAvailabilityCheckReceiver = this,
      resourceHandler = resourceHandler
    )
    binding.numericInputViewModel = numericInputViewModel
    binding.textInputViewModel = textInputViewModel
    binding.fractionInteractionViewModel = fractionInteractionViewModel
    binding.ratioInteractionInputViewModel = ratioExpressionInputInteractionViewModel
  }

  fun getPendingAnswerErrorOnSubmitClick(v: View) {
    fractionInteractionViewModel.checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
    numericInputViewModel.checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
    ratioExpressionInputInteractionViewModel
      .checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
  }

  override fun onPendingAnswerErrorOrAvailabilityCheck(
    pendingAnswerError: String?,
    inputAnswerAvailable: Boolean
  ) {
    binding.submitButton.isEnabled = pendingAnswerError == null
  }

  override fun onEditorAction(actionCode: Int) {
  }
}
