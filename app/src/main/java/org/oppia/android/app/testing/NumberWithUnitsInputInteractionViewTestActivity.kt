package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.customview.interaction.NumberWithUnitsInputInteractionView
import org.oppia.android.app.databinding.databinding.ActivityNumberWithUnitsInputInteractionViewTestBinding
import org.oppia.android.app.model.AnswerErrorCategory
import org.oppia.android.app.model.InputInteractionViewTestActivityParams
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.itemviewmodel.NumberWithUnitsInputViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.InteractionItemFactory
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.ui.R
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import javax.inject.Inject

/**
 * This is a dummy activity to test [NumberWithUnitsInputInteractionView].
 */
class NumberWithUnitsInputInteractionViewTestActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  StateKeyboardButtonListener,
  InteractionAnswerErrorOrAvailabilityCheckReceiver,
  InteractionAnswerReceiver {
  private lateinit var binding: ActivityNumberWithUnitsInputInteractionViewTestBinding

  @Inject
  lateinit var numberWithUnitsInputViewModelFactory: NumberWithUnitsInputViewModel.FactoryImpl

  /** Gives access to the [NumberWithUnitsInputViewModel]. */
  val numberWithUnitsInputViewModel by lazy {
    numberWithUnitsInputViewModelFactory.create<NumberWithUnitsInputViewModel>()
  }

  /** Gives access to the translation context. */
  lateinit var writtenTranslationContext: WrittenTranslationContext

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    binding = DataBindingUtil
      .setContentView<ActivityNumberWithUnitsInputInteractionViewTestBinding>(
        this, R.layout.activity_number_with_units_input_interaction_view_test
      )

    val params =
      intent.getProtoExtra(
        TEST_ACTIVITY_PARAMS_ARGUMENT_KEY,
        InputInteractionViewTestActivityParams.getDefaultInstance()
      )
    writtenTranslationContext = params.writtenTranslationContext

    binding.numberWithUnitsInputViewModel = numberWithUnitsInputViewModel
    binding.getPendingAnswerErrorOnSubmitClick = Runnable {
      numberWithUnitsInputViewModel.checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
    }
  }

  override fun onPendingAnswerErrorOrAvailabilityCheck(
    pendingAnswerError: String?,
    inputAnswerAvailable: Boolean
  ) {
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
      interactionAnswerReceiver = this@NumberWithUnitsInputInteractionViewTestActivity,
      answerErrorReceiver = this@NumberWithUnitsInputInteractionViewTestActivity,
      hasPreviousButton = false,
      isSplitView = false,
      writtenTranslationContext,
      timeToStartNoticeAnimationMs = null
    ) as T
  }

  companion object {
    private const val TEST_ACTIVITY_PARAMS_ARGUMENT_KEY =
      "NumberWithUnitsInputInteractionViewTestActivity.params"

    /** Creates an intent to open [NumberWithUnitsInputInteractionViewTestActivity]. */
    fun createIntent(
      context: Context,
      extras: InputInteractionViewTestActivityParams
    ): Intent {
      return Intent(context, NumberWithUnitsInputInteractionViewTestActivity::class.java).also {
        it.putProtoExtra(TEST_ACTIVITY_PARAMS_ARGUMENT_KEY, extras)
      }
    }
  }
}
