package org.oppia.app.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.customview.interaction.NumericInputInteractionView

private const val KEY_NUMERIC_INPUT_ID = "NUMERIC_INPUT_ID"

/**
 * This is a dummy activity to test input interaction views.
 * It contains [NumericInputInteractionView] .
 */
class InputInteractionViewTestActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_numeric_input_interaction_view_test)
  }
}
