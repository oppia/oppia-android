package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.customview.interaction.NumericInputInteractionView
import org.oppia.app.customview.interaction.TextInputInteractionView
import org.oppia.app.customview.interaction.FractionInputInteractionView

/**
 * This is a dummy activity to test input interaction views.
 * It contains [NumericInputInteractionView], [TextInputInteractionView] and [FractionInputInteractionView].
 */
class InputInteractionViewTestActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_numeric_input_interaction_view_test)
  }
}
