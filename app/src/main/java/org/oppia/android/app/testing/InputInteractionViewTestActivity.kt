package org.oppia.android.app.testing

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
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
import org.oppia.android.databinding.ActivityInputInteractionViewTestBinding
import javax.inject.Inject
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.InteractionItemFactory

/**
 * This is a dummy activity to test input interaction views.
 * It contains [FractionInputInteractionView], [NumericInputInteractionView],and [TextInputInteractionView].
 */
class InputInteractionViewTestActivity :
  InjectableAppCompatActivity(),
  StateKeyboardButtonListener,
  InteractionAnswerErrorOrAvailabilityCheckReceiver,
  InteractionAnswerReceiver {
  private lateinit var binding: ActivityInputInteractionViewTestBinding

  @Inject
  lateinit var numericInputViewModelFactory: NumericInputViewModel.FactoryImpl

  @Inject
  lateinit var textInputViewModelFactory: TextInputViewModel.FactoryImpl

  @Inject
  lateinit var fractionInteractionViewModelFactory: FractionInteractionViewModel.FactoryImpl

  @Inject
  lateinit var ratioViewModelFactory: RatioExpressionInputInteractionViewModel.FactoryImpl

  val numericInputViewModel by lazy { numericInputViewModelFactory.create<NumericInputViewModel>() }

  val textInputViewModel by lazy { textInputViewModelFactory.create<TextInputViewModel>() }

  val fractionInteractionViewModel by lazy {
    fractionInteractionViewModelFactory.create<FractionInteractionViewModel>()
  }

  val ratioExpressionInputInteractionViewModel by lazy {
    ratioViewModelFactory.create<RatioExpressionInputInteractionViewModel>(
      interaction = Interaction.newBuilder().putCustomizationArgs(
        "numberOfTerms",
        SchemaObject.newBuilder().setSignedInt(3).build()
      ).build()
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    binding = DataBindingUtil.setContentView<ActivityInputInteractionViewTestBinding>(
      this, R.layout.activity_input_interaction_view_test
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

  override fun onAnswerReadyForSubmission(answer: UserAnswer) {
  }

  override fun onEditorAction(actionCode: Int) {
  }

  private inline fun <reified T: StateItemViewModel> InteractionItemFactory.create(
    interaction: Interaction = Interaction.getDefaultInstance()
  ): T {
    return create(
      entityId = "fake_entity_id",
      hasConversationView = false,
      interaction = interaction,
      interactionAnswerReceiver = this@InputInteractionViewTestActivity,
      answerErrorReceiver = this@InputInteractionViewTestActivity,
      hasPreviousButton = false,
      isSplitView = false,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance()
    ) as T
  }
}
