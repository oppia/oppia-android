package org.oppia.android.app.testing

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.customview.interaction.NumericInputInteractionView
import org.oppia.android.app.customview.interaction.TextInputInteractionView
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.SchemaObject
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.android.app.player.state.itemviewmodel.RatioExpressionInputInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.ActivityInputInteractionViewTestBinding
import org.oppia.android.domain.translation.TranslationController
import javax.inject.Inject

/**
 * This is a dummy activity to test input interaction views.
 * It contains [NumericInputInteractionView],and [TextInputInteractionView].
 */
class InputInteractionViewTestActivity :
  InjectableAppCompatActivity(),
  StateKeyboardButtonListener,
  InteractionAnswerErrorOrAvailabilityCheckReceiver {
  private lateinit var binding: ActivityInputInteractionViewTestBinding
  lateinit var ratioExpressionInputInteractionViewModel: RatioExpressionInputInteractionViewModel

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  @Inject
  lateinit var translationController: TranslationController

  val numericInputViewModel by lazy {
    NumericInputViewModel(
      hasConversationView = false,
      interactionAnswerErrorOrAvailabilityCheckReceiver = this,
      isSplitView = false,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance(),
      resourceHandler = resourceHandler
    )
  }

  val textInputViewModel by lazy {
    TextInputViewModel(
      interaction = Interaction.getDefaultInstance(),
      hasConversationView = false,
      interactionAnswerErrorOrAvailabilityCheckReceiver = this,
      isSplitView = false,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance(),
      translationController = translationController
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    binding = DataBindingUtil.setContentView<ActivityInputInteractionViewTestBinding>(
      this, R.layout.activity_input_interaction_view_test
    )
    ratioExpressionInputInteractionViewModel = RatioExpressionInputInteractionViewModel(
      interaction = Interaction.newBuilder().putCustomizationArgs(
        "numberOfTerms",
        SchemaObject.newBuilder().setSignedInt(3).build()
      ).build(),
      hasConversationView = false,
      isSplitView = false,
      errorOrAvailabilityCheckReceiver = this,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance(),
      resourceHandler = resourceHandler,
      translationController = translationController
    )
    binding.numericInputViewModel = numericInputViewModel
    binding.textInputViewModel = textInputViewModel
    binding.ratioInteractionInputViewModel = ratioExpressionInputInteractionViewModel
  }

  fun getPendingAnswerErrorOnSubmitClick(v: View) {
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
