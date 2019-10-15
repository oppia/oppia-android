package org.oppia.app.customview.inputInteractionView

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputFilter
import android.widget.EditText
import org.oppia.app.R

/** The customclass for [TextInputInteractionView]. */
@SuppressLint("ViewConstructor")
class TextInputInteractionView(context: Context, private var placeholder: String) :
  EditText(context) {
  init {
    attributes()
  }

  /** This function contains default attributes of [TextInputInteractionView]. */
  fun attributes() {
    setBackgroundResource(R.drawable.edit_text_background)
    val paddingPixel = context.resources.getDimension(R.dimen.padding_8)
    val density = resources.displayMetrics.density
    val paddingDp = (paddingPixel * density).toInt()
    this.setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
    hint = placeholder
    this.setEditTextMaxLength(length = 200)
  }

  private fun setEditTextMaxLength(length: Int) {
    val filterArray = arrayOfNulls<InputFilter>(1)
    filterArray[0] = InputFilter.LengthFilter(length)
    filters = filterArray
  }
}
