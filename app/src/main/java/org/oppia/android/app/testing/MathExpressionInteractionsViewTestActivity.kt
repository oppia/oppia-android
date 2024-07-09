package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.AnswerErrorCategory
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.MathExpressionInteractionsViewTestActivityParams
import org.oppia.android.app.model.MathExpressionInteractionsViewTestActivityParams.MathInteractionType
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.itemviewmodel.MathExpressionInteractionsViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.databinding.ActivityMathExpressionInteractionViewTestBinding
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import javax.inject.Inject

/**
 * This is a dummy activity to test input interaction views.
 * It contains [MathExpressionInteractionsView].
 */
class MathExpressionInteractionsViewTestActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  StateKeyboardButtonListener,
  InteractionAnswerErrorOrAvailabilityCheckReceiver,
  InteractionAnswerReceiver {

  private lateinit var binding:
    ActivityMathExpressionInteractionViewTestBinding

  /**
   * Injects the [MathExpressionInteractionsViewModel.FactoryImpl] for creating
   * [MathExpressionInteractionsViewModel] instances.
   */
  @Inject
  lateinit var mathExpViewModelFactoryFactory:
    MathExpressionInteractionsViewModel.FactoryImpl.FactoryFactoryImpl

  /** The [MathExpressionInteractionsViewModel] instance. */
  lateinit var mathExpressionViewModel: MathExpressionInteractionsViewModel

  /** Gives access to the translation context. */
  lateinit var writtenTranslationContext: WrittenTranslationContext

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    binding = DataBindingUtil.setContentView<ActivityMathExpressionInteractionViewTestBinding>(
      this, R.layout.activity_math_expression_interaction_view_test
    )
    val params =
      intent.getProtoExtra(
        TEST_ACTIVITY_PARAMS_ARGUMENT_KEY,
        MathExpressionInteractionsViewTestActivityParams.getDefaultInstance()
      )
    writtenTranslationContext = params.writtenTranslationContext
    when (params.mathInteractionType) {
      MathInteractionType.NUMERIC_EXPRESSION -> {
        mathExpressionViewModel =
          mathExpViewModelFactoryFactory
            .createFactoryForNumericExpression()
            .create(interaction = params.interaction)
      }
      MathInteractionType.ALGEBRAIC_EXPRESSION -> {
        mathExpressionViewModel =
          mathExpViewModelFactoryFactory
            .createFactoryForAlgebraicExpression()
            .create(interaction = params.interaction)
      }
      MathInteractionType.MATH_EQUATION -> {
        mathExpressionViewModel =
          mathExpViewModelFactoryFactory
            .createFactoryForMathEquation()
            .create(interaction = params.interaction)
      }
      MathInteractionType.MATH_INTERACTION_TYPE_UNSPECIFIED,
      MathInteractionType.UNRECOGNIZED, null -> {
        // Default to numeric expression arbitrarily (since something needs to be defined).
        mathExpressionViewModel =
          mathExpViewModelFactoryFactory
            .createFactoryForNumericExpression()
            .create(interaction = params.interaction)
      }
    }

    binding.mathExpressionInteractionsViewModel = mathExpressionViewModel
    binding.getPendingAnswerErrorOnSubmitClick = Runnable {
      mathExpressionViewModel.checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
    }
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

  private inline fun <reified T : StateItemViewModel> StateItemViewModel
  .InteractionItemFactory.create(
    interaction: Interaction = Interaction.getDefaultInstance()
  ): T {
    return create(
      entityId = "fake_entity_id",
      hasConversationView = false,
      interaction = interaction,
      interactionAnswerReceiver = this@MathExpressionInteractionsViewTestActivity,
      answerErrorReceiver = this@MathExpressionInteractionsViewTestActivity,
      hasPreviousButton = false,
      isSplitView = false,
      writtenTranslationContext,
      timeToStartNoticeAnimationMs = null
    ) as T
  }

  companion object {
    private const val TEST_ACTIVITY_PARAMS_ARGUMENT_KEY =
      "MathExpressionInteractionsViewTestActivity.params"

    /** Function to create intent for MathExpressionInteractionsViewTestActivity. */
    fun createIntent(
      context: Context,
      extras: MathExpressionInteractionsViewTestActivityParams
    ): Intent {
      return Intent(context, MathExpressionInteractionsViewTestActivity::class.java).also {
        it.putProtoExtra(TEST_ACTIVITY_PARAMS_ARGUMENT_KEY, extras)
      }
    }
  }
}
