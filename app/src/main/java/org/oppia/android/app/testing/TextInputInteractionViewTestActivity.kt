package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.databinding.ActivityTextInputInteractionViewTestBinding
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import javax.inject.Inject
import org.oppia.android.app.model.InputInteractionViewTestActivityParams
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext

class TextInputInteractionViewTestActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  StateKeyboardButtonListener,
  InteractionAnswerErrorOrAvailabilityCheckReceiver,
  InteractionAnswerReceiver {

  private lateinit var binding: ActivityTextInputInteractionViewTestBinding

  @Inject
  lateinit var textinputViewModelFactory: TextInputViewModel.FactoryImpl

  /** Gives access to the [TextInputViewModel]. */
  val textInputViewModel by lazy {
    textinputViewModelFactory.create<TextInputViewModel>()
  }

  /** Gives access to the translation context. */
  lateinit var writtenTranslationContext: WrittenTranslationContext

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    binding = DataBindingUtil.setContentView(
      this, R.layout.activity_text_input_interaction_view_test
    )

    val params = intent.getProtoExtra(
      TEST_ACTIVITY_PARAMS_ARGUMENT_KEY, InputInteractionViewTestActivityParams.getDefaultInstance()
    )
    writtenTranslationContext = params.writtenTranslationContext
    binding.textInputViewModel = textInputViewModel
  }

  /** Checks submit-time errors. */
  fun getPendingAnswerErrorOnSubmitClick(v: View) {
    textInputViewModel.checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
  }

  override fun onPendingAnswerErrorOrAvailabilityCheck(
    pendingAnswerError: String?,
    inputAnswerAvailable: Boolean
  ) {
  }

  override fun onAnswerReadyForSubmission(answer: UserAnswer) {
  }

  override fun onEditorAction(actionCode: Int) {
  }

  private inline fun <reified T : StateItemViewModel>
  StateItemViewModel.InteractionItemFactory.create(
    interaction: Interaction = Interaction.getDefaultInstance()
  ): T {
    return create(
      entityId = "fake_entity_id",
      hasConversationView = false,
      interaction = interaction,
      interactionAnswerReceiver = this@TextInputInteractionViewTestActivity,
      answerErrorReceiver = this@TextInputInteractionViewTestActivity,
      hasPreviousButton = false,
      isSplitView = false,
      writtenTranslationContext,
      timeToStartNoticeAnimationMs = null
    ) as T
  }

  companion object {
    private const val TEST_ACTIVITY_PARAMS_ARGUMENT_KEY =
      "TextInputInteractionViewTestActivity.params"

    /** Creates an intent to open this activity. */
    fun createIntent(
      context: Context,
      extras: InputInteractionViewTestActivityParams
    ): Intent {
      return Intent(context, TextInputInteractionViewTestActivity::class.java).also {
        it.putProtoExtra(TEST_ACTIVITY_PARAMS_ARGUMENT_KEY, extras)
      }
    }
  }
}
