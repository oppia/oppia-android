package org.oppia.app.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.customview.interaction.NumericInputInteractionView

/** This is a dummy activity to test [NumericInputInteractionView]. */
class InputInteractionViewTestActivity : AppCompatActivity() {
  private lateinit var contentComponent: NumericInputInteractionView
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_numeric_input_interaction_view_test)
    contentComponent = findViewById(R.id.test_number_input_interaction_view) as NumericInputInteractionView
  }
}
