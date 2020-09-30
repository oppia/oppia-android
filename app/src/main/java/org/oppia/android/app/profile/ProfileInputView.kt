package org.oppia.android.app.profile

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
import org.oppia.android.R
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.shim.ViewBindingShim
import javax.inject.Inject

/** Custom view that is used for name or pin input with error messages. */
class ProfileInputView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

  @Inject
  lateinit var bindingInterface: ViewBindingShim

  companion object {
    @JvmStatic
    @BindingAdapter("profile:label")
    fun setLabel(profileInputView: ProfileInputView, label: String) {
      profileInputView.label.text = label
    }

    /** Binding adapter for setting a [TextWatcher] as a change listener for an [EditText]. */
    @BindingAdapter("android:addTextChangedListener")
    fun bindTextWatcher(editText: EditText, textWatcher: TextWatcher) {
      editText.addTextChangedListener(textWatcher)
    }
  }

  private var label: TextView
  private var errorText: TextView
  private var input: EditText

  init {
    (context.applicationContext as ApplicationInjectorProvider).getApplicationInjector()
      .inject(this)

    val profileInputView = bindingInterface.inflateProfileInputView(
      LayoutInflater.from(context),
      parent = this,
      attachToParent = true
    )
    val attributes = context.obtainStyledAttributes(attrs, R.styleable.ProfileInputView)
    label = bindingInterface.provideProfileInputViewBindingLabelText(profileInputView)
    label.text = attributes.getString(R.styleable.ProfileInputView_label)
    input = bindingInterface.provideProfileInputViewBindingInput(profileInputView)
    errorText = bindingInterface.provideProfileInputViewBindingErrorText(profileInputView)
    orientation = VERTICAL
    if (
      attributes.getBoolean(
          R.styleable.ProfileInputView_isPasswordInput,
          /** defVal= */ false
        )
    ) {
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

  /** Sets the input of editText. */
  fun setInput(text: String) = input.setText(text)

  fun setSelection(length: Int) = input.setSelection(length)

  /** Allows editText to be watched. */
  fun addTextChangedListener(textWatcher: TextWatcher) = input.addTextChangedListener(textWatcher)

  /** Allows editText actions to be listened to.*/
  fun addEditorActionListener(editorActionListener: TextView.OnEditorActionListener) =
    input.setOnEditorActionListener(editorActionListener)

  /** Clears red border and error text. */
  fun clearErrorText() {
    input.background = context.resources.getDrawable(R.drawable.add_profile_edit_text_background)
    errorText.text = ""
  }

  /** Sets red border and error text. */
  fun setErrorText(errorMessage: String) {
    input.background = context.resources.getDrawable(R.drawable.edit_text_red_border)
    errorText.text = errorMessage
  }

  fun setSingleLine(type: Boolean) {
    input.setSingleLine(type)
  }

  fun setInputLength(inputLength: Int) {
    input.filters = arrayOf(InputFilter.LengthFilter(inputLength))
  }

  fun setLabelMargin(dimen: Float) {
    val layoutParams = label.layoutParams as MarginLayoutParams
    layoutParams.marginStart = dimen.toInt()
    label.layoutParams = layoutParams
  }

  fun setError(errorMessage: String) {
    val errMessage: String = errorMessage ?: ""
    if (errMessage.isEmpty()) {
      clearErrorText()
    } else {
      setErrorText(errMessage)
    }
  }

  fun setLabel(labelText: String) {
    label.text = labelText
  }
}
