package org.oppia.app.testing

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.customview.interaction.FractionInputInteractionView
import org.oppia.app.customview.interaction.NumericInputInteractionView
import org.oppia.app.customview.interaction.TextInputInteractionView
import org.oppia.app.databinding.ActivityInputInteractionViewTestBinding
import org.oppia.app.model.Interaction
import org.oppia.app.player.state.answerhandling.AnswerErrorCategory
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.app.player.state.listener.StateKeyboardButtonListener

/**
 * This is a dummy activity to test input interaction views.
 * It contains [NumericInputInteractionView], [TextInputInteractionView],and [FractionInputInteractionView].
 */
class InputInteractionViewTestActivity : AppCompatActivity(), StateKeyboardButtonListener {

  val numericInputViewModel = NumericInputViewModel()
  val textInputViewModel = TextInputViewModel(
    interaction = Interaction.getDefaultInstance()
  )
  lateinit var fractionInteractionViewModel: FractionInteractionViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = DataBindingUtil.setContentView<ActivityInputInteractionViewTestBinding>(
      this, R.layout.activity_input_interaction_view_test
    )
    fractionInteractionViewModel = FractionInteractionViewModel(
      interaction = Interaction.getDefaultInstance(),
      context = this,
    null)
    binding.numericInputViewModel = numericInputViewModel
    binding.textInputViewModel = textInputViewModel
    binding.fractionInteractionViewModel = fractionInteractionViewModel
  }

  fun getPendingAnswerErrorOnSubmitClick(v: View) {
    fractionInteractionViewModel.setPendingAnswerError(AnswerErrorCategory.SUBMIT_TIME)
  }

  override fun onEditorAction(actionCode: Int) {
  }
}
