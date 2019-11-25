package org.oppia.app.databinding

import android.text.format.DateUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.util.Date

/** Converts time in ms to readable relative time string. */
@BindingAdapter("profile:date")
fun setTextWithDate(textView: TextView, timeMs: Long) {
  val dateText = "Last used " + DateUtils.getRelativeTimeSpanString(timeMs, Date().time, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
  textView.text = dateText
}
