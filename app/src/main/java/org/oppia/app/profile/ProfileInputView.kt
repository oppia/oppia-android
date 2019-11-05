package org.oppia.app.profile

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import org.oppia.app.R

class ProfileInputView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
  private var errorText: TextView
  private var input: EditText

  init {
    View.inflate(context, R.layout.profile_input_view, this)
    val attributes = context.obtainStyledAttributes(attrs, R.styleable.ProfileInputView)
    findViewById<TextView>(R.id.label_text).text = attributes.getString(R.styleable.ProfileInputView_label)
    input = findViewById(R.id.input)
    errorText = findViewById(R.id.error_text)
    orientation = VERTICAL
    if (attributes.getBoolean(R.styleable.ProfileInputView_isPasswordInput, /** defVal= */ false)) {
      input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
    }
    val inputLength = attributes.getInt(R.styleable.ProfileInputView_inputLength, -1)
    if (inputLength > 0) {
      input.filters = arrayOf(InputFilter.LengthFilter(inputLength))
    }
    attributes.recycle()
  }
}
