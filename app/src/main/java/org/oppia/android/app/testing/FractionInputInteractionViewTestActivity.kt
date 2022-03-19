package org.oppia.android.app.testing

import android.view.View
import org.oppia.android.app.activity.InjectableAppCompatActivity
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

class FractionInputInteractionViewTestActivity :
  InjectableAppCompatActivity(),
  StateKeyboardButtonListener,
  InteractionAnswerErrorOrAvailabilityCheckReceiver {
  private lateinit var binding: ActivityFractionInputInteractionViewTestBinding

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  @Inject
  lateinit var translationController: TranslationController

  val fractionInteractionViewModel by lazy {
    FractionInteractionViewModel(
      interaction = Interaction.getDefaultInstance(),
      hasConversationView = false,
      isSplitView = false,
      errorOrAvailabilityCheckReceiver = this,
      writtenTranslationContext = WrittenTranslationContext.getDefaultInstance(),
      resourceHandler = resourceHandler,
      translationController = translationController
    )
  }

  fun getPendingAnswerErrorOnSubmitClick(v: View) {
    fractionInteractionViewModel.checkPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
  }

  override fun onEditorAction(actionCode: Int) {
  }
}
