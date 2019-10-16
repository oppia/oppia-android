package org.oppia.app.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.customview.inputInteractionView.NumericInputInteractionView
import org.oppia.app.customview.inputInteractionView.TextInputInteractionView

private const val KEY_NUMERIC_INPUT_ID = "NUMERIC_INPUT_ID"
private const val KEY_TEXT_INPUT_ID = "TEXT_INPUT_ID"

/** This is a dummy activity to test [NumericInputInteractionView]. */
class NumericInputInteractionViewTestActivity : AppCompatActivity() {
  private var numericInput: String =""
  private var textInput: String =""
  private lateinit var contentNumericComponent: NumericInputInteractionView
  private lateinit var contentTextComponent: TextInputInteractionView
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_numeric_input_interaction_view_test)
    contentNumericComponent = findViewById(R.id.test_number_input_interaction_view) as NumericInputInteractionView
    contentTextComponent = findViewById(R.id.test_text_input_interaction_view) as TextInputInteractionView
    if (savedInstanceState != null) {
      numericInput = savedInstanceState.getString(KEY_NUMERIC_INPUT_ID)
      textInput = savedInstanceState.getString(KEY_TEXT_INPUT_ID)
    }
    contentNumericComponent.setText(numericInput)
    contentTextComponent.setText(textInput)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(KEY_NUMERIC_INPUT_ID, contentNumericComponent.getPendingAnswer().normalizedString)
    outState.putString(KEY_TEXT_INPUT_ID, contentTextComponent.getPendingAnswer().normalizedString)
  }
}
