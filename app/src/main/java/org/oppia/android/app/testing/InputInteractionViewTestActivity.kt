package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.customview.interaction.FractionInputInteractionView
import org.oppia.android.app.customview.interaction.NumericInputInteractionView
import org.oppia.android.app.customview.interaction.TextInputInteractionView
import org.oppia.android.app.model.InputInteractionViewTestActivityParams
import org.oppia.android.app.model.InputInteractionViewTestActivityParams.MathInteractionType.ALGEBRAIC_EXPRESSION
import org.oppia.android.app.model.InputInteractionViewTestActivityParams.MathInteractionType.MATH_EQUATION
import org.oppia.android.app.model.InputInteractionViewTestActivityParams.MathInteractionType.MATH_INTERACTION_TYPE_UNSPECIFIED
import org.oppia.android.app.model.InputInteractionViewTestActivityParams.MathInteractionType.NUMERIC_EXPRESSION
import org.oppia.android.app.model.InputInteractionViewTestActivityParams.MathInteractionType.UNRECOGNIZED
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.SchemaObject
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.MathExpressionInteractionsViewModel
import org.oppia.android.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.android.app.player.state.itemviewmodel.RatioExpressionInputInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.InteractionItemFactory
import org.oppia.android.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.databinding.ActivityInputInteractionViewTestBinding
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import javax.inject.Inject
import org.oppia.android.app.model.RawUserAnswer
import org.oppia.android.app.player.state.itemviewmodel.MathExpressionInteractionsViewModel.FactoryImpl.FactoryFactoryImpl as MathExpViewModelFactoryFactoryImpl

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

  @Inject
  lateinit var mathExpViewModelFactoryFactory: MathExpViewModelFactoryFactoryImpl

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

  lateinit var mathExpressionViewModel: MathExpressionInteractionsViewModel
  lateinit var writtenTranslationContext: WrittenTranslationContext

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    binding = DataBindingUtil.setContentView<ActivityInputInteractionViewTestBinding>(
      this, R.layout.activity_input_interaction_view_test
    )

    val params =
      intent.getProtoExtra(
        TEST_ACTIVITY_PARAMS_ARGUMENT_KEY,
        InputInteractionViewTestActivityParams.getDefaultInstance()
      )
    writtenTranslationContext = params.writtenTranslationContext
    when (params.mathInteractionType) {
      NUMERIC_EXPRESSION -> {
        mathExpressionViewModel =
          mathExpViewModelFactoryFactory
            .createFactoryForNumericExpression()
            .create(interaction = params.interaction)
      }
      ALGEBRAIC_EXPRESSION -> {
        mathExpressionViewModel =
          mathExpViewModelFactoryFactory
            .createFactoryForAlgebraicExpression()
            .create(interaction = params.interaction)
      }
      MATH_EQUATION -> {
        mathExpressionViewModel =
          mathExpViewModelFactoryFactory
            .createFactoryForMathEquation()
            .create(interaction = params.interaction)
      }
      MATH_INTERACTION_TYPE_UNSPECIFIED, UNRECOGNIZED, null -> {
        // Default to numeric expression arbitrarily (since something needs to be defined).
        mathExpressionViewModel =
          mathExpViewModelFactoryFactory
            .createFactoryForNumericExpression()
            .create(interaction = params.interaction)
      }
    }

    binding.numericInputViewModel = numericInputViewModel
    binding.textInputViewModel = textInputViewModel
    binding.fractionInteractionViewModel = fractionInteractionViewModel
    binding.ratioInteractionInputViewModel = ratioExpressionInputInteractionViewModel
    binding.mathExpressionInteractionsViewModel = mathExpressionViewModel
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

  private inline fun <reified T : StateItemViewModel> InteractionItemFactory.create(
    interaction: Interaction = Interaction.getDefaultInstance()
  ): T {
    return create(
      entityId = "fake_entity_id",
      hasConversationView = false,
      rawUserAnswer = RawUserAnswer.getDefaultInstance(),
      interaction = interaction,
      interactionAnswerReceiver = this@InputInteractionViewTestActivity,
      answerErrorReceiver = this@InputInteractionViewTestActivity,
      hasPreviousButton = false,
      isSplitView = false,
      writtenTranslationContext
    ) as T
  }

  companion object {
    private const val TEST_ACTIVITY_PARAMS_ARGUMENT_KEY = "InputInteractionViewTestActivity.params"

    fun createIntent(
      context: Context,
      extras: InputInteractionViewTestActivityParams
    ): Intent {
      return Intent(context, InputInteractionViewTestActivity::class.java).also {
        it.putProtoExtra(TEST_ACTIVITY_PARAMS_ARGUMENT_KEY, extras)
      }
    }
  }
}
