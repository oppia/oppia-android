package org.oppia.app.testing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.customview.interaction.FractionInputInteractionView
import org.oppia.app.customview.interaction.NumericInputInteractionView
import org.oppia.app.customview.interaction.TextInputInteractionView
import org.oppia.app.databinding.ActivityNumericInputInteractionViewTestBinding
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel

/**
 * This is a dummy activity to test input interaction views.
 * It contains [NumericInputInteractionView], [TextInputInteractionView], [FractionInputInteractionView] and [NumberWithUnitsInputInteractionView].
 */
class InputInteractionViewTestActivity : AppCompatActivity() {
  val numericInputViewModel = NumericInputViewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = DataBindingUtil.setContentView<ActivityNumericInputInteractionViewTestBinding>(
      this, R.layout.activity_numeric_input_interaction_view_test
    )
    binding.numericInputViewModel = numericInputViewModel
  }
}
