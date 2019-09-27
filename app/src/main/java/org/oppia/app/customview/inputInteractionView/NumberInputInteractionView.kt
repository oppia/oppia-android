package org.oppia.app.customview.inputInteractionView

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.widget.EditText

import org.oppia.app.R

/** The customclass for [NumberInputInteractionView]. */
class NumberInputInteractionView : EditText {

  private var mContext: Context
  private var placeholder: String
  private var rows: Int

  constructor(context: Context, placeholder: String, rows: Int) : super(context) {
    this.mContext = context
    this.placeholder = placeholder
    this.rows = rows
    attributes()
  }

  /** This function contains default attributes of [NumberInputInteractionView].*/
  fun attributes() {
    setBackgroundResource(R.drawable.edit_text_background)
    val paddingPixel = 10
    val density = resources.displayMetrics.density
    val paddingDp = (paddingPixel * density).toInt()
    setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
    setHint(placeholder)
    setKeyListener(DigitsKeyListener.getInstance("0123456789."))
    setEditTextMaxLength(200)
  }

  fun setEditTextMaxLength(length: Int) {
    val filterArray = arrayOfNulls<InputFilter>(1)
    filterArray[0] = InputFilter.LengthFilter(length)
    setFilters(filterArray)
  }
}
