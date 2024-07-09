package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.customview.interaction.RatioInputInteractionView
import org.oppia.android.app.model.AnswerErrorCategory
import org.oppia.android.app.model.InputInteractionViewTestActivityParams
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.SchemaObject
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.itemviewmodel.RatioExpressionInputInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.InteractionItemFactory
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.databinding.ActivityRatioInputInteractionViewTestBinding
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import javax.inject.Inject

/**
 * This is a dummy activity to test [RatioInputInteractionView].
 */
class RatioInputInteractionViewTestActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  StateKeyboardButtonListener,
  InteractionAnswerErrorOrAvailabilityCheckReceiver,
  InteractionAnswerReceiver {
  private lateinit var binding: ActivityRatioInputInteractionViewTestBinding

  @Inject
  lateinit var ratioViewModelFactory: RatioExpressionInputInteractionViewModel.FactoryImpl

  /**
   * Gives access to the [RatioExpressionInputInteractionViewModel].
   */
  val ratioExpressionInputInteractionViewModel by lazy {
    ratioViewModelFactory.create<RatioExpressionInputInteractionViewModel>(
      interaction = Interaction.newBuilder().putCustomizationArgs(
        "numberOfTerms",
        SchemaObject.newBuilder().setSignedInt(3).build()
      ).build()
    )
  }

  /**
   * Gives access to the translation context.
   */
  lateinit var writtenTranslationContext: WrittenTranslationContext

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    binding = DataBindingUtil.setContentView<ActivityRatioInputInteractionViewTestBinding>(
      this, R.layout.activity_ratio_input_interaction_view_test
    )

    val params =
      intent.getProtoExtra(
        TEST_ACTIVITY_PARAMS_ARGUMENT_KEY,
        InputInteractionViewTestActivityParams.getDefaultInstance()
      )
    writtenTranslationContext = params.writtenTranslationContext

    binding.ratioInteractionInputViewModel = ratioExpressionInputInteractionViewModel
    binding.getPendingAnswerErrorOnSubmitClick = Runnable {
      ratioExpressionInputInteractionViewModel
        .checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
    }
  }

  override fun onAnswerReadyForSubmission(answer: UserAnswer) { }

  override fun onEditorAction(actionCode: Int) { }

  private inline fun <reified T : StateItemViewModel> InteractionItemFactory.create(
    interaction: Interaction = Interaction.getDefaultInstance()
  ): T {
    return create(
      entityId = "fake_entity_id",
      hasConversationView = false,
      interaction = interaction,
      interactionAnswerReceiver = this@RatioInputInteractionViewTestActivity,
      answerErrorReceiver = this@RatioInputInteractionViewTestActivity,
      hasPreviousButton = false,
      isSplitView = false,
      writtenTranslationContext,
      timeToStartNoticeAnimationMs = null
    ) as T
  }

  companion object {
    private const val TEST_ACTIVITY_PARAMS_ARGUMENT_KEY =
      "RatioInputInteractionViewTestActivity.params"

    /**
     * Creates an intent to open [RatioInputInteractionViewTestActivity].
     */
    fun createIntent(
      context: Context,
      extras: InputInteractionViewTestActivityParams
    ): Intent {
      return Intent(context, RatioInputInteractionViewTestActivity::class.java).also {
        it.putProtoExtra(TEST_ACTIVITY_PARAMS_ARGUMENT_KEY, extras)
      }
    }
  }
}
