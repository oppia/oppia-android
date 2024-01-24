package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.InputInteractionViewTestActivityParams
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
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
class MathExpressionInteractionViewTestActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  StateKeyboardButtonListener,
  InteractionAnswerErrorOrAvailabilityCheckReceiver,
  InteractionAnswerReceiver {

  private lateinit var binding:
    ActivityMathExpressionInteractionViewTestBinding

  @Inject
  lateinit var mathExpViewModelFactoryFactory:
    MathExpressionInteractionsViewModel.FactoryImpl.FactoryFactoryImpl

  lateinit var mathExpressionViewModel: MathExpressionInteractionsViewModel
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
        InputInteractionViewTestActivityParams.getDefaultInstance()
      )
    writtenTranslationContext = params.writtenTranslationContext
    when (params.mathInteractionType) {
      InputInteractionViewTestActivityParams.MathInteractionType.NUMERIC_EXPRESSION -> {
        mathExpressionViewModel =
          mathExpViewModelFactoryFactory
            .createFactoryForNumericExpression()
            .create(interaction = params.interaction)
      }
      InputInteractionViewTestActivityParams.MathInteractionType.ALGEBRAIC_EXPRESSION -> {
        mathExpressionViewModel =
          mathExpViewModelFactoryFactory
            .createFactoryForAlgebraicExpression()
            .create(interaction = params.interaction)
      }
      InputInteractionViewTestActivityParams.MathInteractionType.MATH_EQUATION -> {
        mathExpressionViewModel =
          mathExpViewModelFactoryFactory
            .createFactoryForMathEquation()
            .create(interaction = params.interaction)
      }
      InputInteractionViewTestActivityParams.MathInteractionType.MATH_INTERACTION_TYPE_UNSPECIFIED,
      InputInteractionViewTestActivityParams.MathInteractionType.UNRECOGNIZED, null -> {
        // Default to numeric expression arbitrarily (since something needs to be defined).
        mathExpressionViewModel =
          mathExpViewModelFactoryFactory
            .createFactoryForNumericExpression()
            .create(interaction = params.interaction)
      }
    }

    binding.mathExpressionInteractionsViewModel = mathExpressionViewModel
  }

  fun getPendingAnswerErrorOnSubmitClick(v: View) {
    mathExpressionViewModel.checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
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
      interactionAnswerReceiver = this@MathExpressionInteractionViewTestActivity,
      answerErrorReceiver = this@MathExpressionInteractionViewTestActivity,
      hasPreviousButton = false,
      isSplitView = false,
      writtenTranslationContext,
      timeToStartNoticeAnimationMs = null
    ) as T
  }

  companion object {
    private const val TEST_ACTIVITY_PARAMS_ARGUMENT_KEY =
      "MathExpressionInteractionViewTestActivity.params"

    fun createIntent(
      context: Context,
      extras: InputInteractionViewTestActivityParams
    ): Intent {
      return Intent(context, MathExpressionInteractionViewTestActivity::class.java).also {
        it.putProtoExtra(TEST_ACTIVITY_PARAMS_ARGUMENT_KEY, extras)
      }
    }
  }
}
