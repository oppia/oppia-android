package org.oppia.android.app.testing

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.customview.interaction.FractionInputInteractionView
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.android.app.player.state.listener.StateKeyboardButtonListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.ActivityFractionInputInteractionViewTestBinding
import org.oppia.android.domain.translation.TranslationController
import javax.inject.Inject

/**
 * This is a dummy activity to test input interaction views.
 * It contains [FractionInputInteractionView]
 */
class FractionInputInteractionViewTestActivity :
  InjectableAppCompatActivity(),
  StateKeyboardButtonListener,
  InteractionAnswerErrorOrAvailabilityCheckReceiver {
  private lateinit var binding: ActivityFractionInputInteractionViewTestBinding
  lateinit var fractionInteractionViewModel: FractionInteractionViewModel

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  @Inject
  lateinit var translationController: TranslationController

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    binding = DataBindingUtil.setContentView<ActivityFractionInputInteractionViewTestBinding>(
      this, R.layout.activity_fraction_input_interaction_view_test
    )
    fractionInteractionViewModel = FractionInteractionViewModel(
      interaction = Interaction.getDefaultInstance(),
      hasConversationView = false,
      isSplitView = false,
      errorOrAvailabilityCheckReceiver = this,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance(),
      resourceHandler = resourceHandler,
      translationController = translationController
    )
    binding.fractionInteractionViewModel = fractionInteractionViewModel
  }

  fun getPendingAnswerErrorOnSubmitClick(v: View) {
    fractionInteractionViewModel.checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
  }

  override fun onEditorAction(actionCode: Int) {
  }
}