package org.oppia.app.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.customview.inputInteractionView.NumberInputInteractionView

private const val KEY_DIGIT_ID = "DIGIT_ID"

/** [NumericInputInteractionViewTestActivity] is a dummy activity to test [NumberInputInteractionView]. */
class NumericInputInteractionViewTestActivity : AppCompatActivity() {
  private lateinit var digit: String
  private lateinit var contentComponent: NumberInputInteractionView
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_numeric_input_interaction_view_test)
    contentComponent = findViewById(R.id.test_number_input_interaction_view) as NumberInputInteractionView
    if (savedInstanceState != null) {
      digit = savedInstanceState.getString(KEY_DIGIT_ID)
    } else {
      digit = ""
    }
    contentComponent.setText(digit)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(KEY_DIGIT_ID, getNumberTextInputText())
  }

  fun getNumberTextInputText(): String {
    return contentComponent.getPendingAnswer().normalizedString
  }
}
