package org.oppia.app.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import org.oppia.app.R
import org.oppia.app.customview.inputInteractionView.NumberInputInteractionView

private const val KEY_DIGIT_ID = "DIGIT_ID"
private const val KEY_FETCHED_ID = "FETCHED_ID"

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
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(KEY_DIGIT_ID, getNumberTextInputText())
  }

  fun getNumberTextInputText(): String {
    return contentComponent.getPendingAnswer().normalizedString
  }

}
