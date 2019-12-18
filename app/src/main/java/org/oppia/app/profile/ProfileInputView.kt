package org.oppia.app.profile

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.databinding.ProfileInputViewBinding

/** Custom view that is used for name or pin input with error messages. */
class ProfileInputView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
  companion object {
    @JvmStatic
    @BindingAdapter("profile:label")
    fun setLabel(profileInputView: ProfileInputView, label: String) {
      profileInputView.label.text = label
    }

    @JvmStatic
    @BindingAdapter("profile:inputLength")
    fun setInputLength(profileInputView: ProfileInputView, inputLength: Int) {
      profileInputView.input.filters = arrayOf(InputFilter.LengthFilter(inputLength))
    }

    @JvmStatic
    @BindingAdapter("profile:error")
    fun setProfileImage(profileInputView: ProfileInputView, errorMessage: String) {
      if (errorMessage.isEmpty()) {
        profileInputView.clearErrorText()
      } else {
        profileInputView.setErrorText(errorMessage)
      }
    }
  }

  private var label: TextView
  private var errorText: TextView
  private var input: EditText

  init {
    val binding = DataBindingUtil.inflate<ProfileInputViewBinding>(
      LayoutInflater.from(context),
      R.layout.profile_input_view, this,
      /* attachToRoot= */ true
    )
    val attributes = context.obtainStyledAttributes(attrs, R.styleable.ProfileInputView)
    binding.labelText.text = attributes.getString(R.styleable.ProfileInputView_label)
    label = binding.labelText
    input = binding.input
    errorText = binding.errorText
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

  /** Gets input of editText. */
  fun getInput() = input.text.toString()

  /** Allows editText to be watched. */
  fun addTextChangedListener(textWatcher: TextWatcher) = input.addTextChangedListener(textWatcher)

  /** Clears red border and error text. */
  fun clearErrorText() {
    input.background = context.resources.getDrawable(R.drawable.edit_text_black_border)
    errorText.text = ""
  }

  /** Sets red border and error text. */
  fun setErrorText(errorMessage: String) {
    input.background = context.resources.getDrawable(R.drawable.edit_text_red_border)
    errorText.text = errorMessage
  }

  fun setLabel(labelText: String) {
    label.text = labelText
  }
}
