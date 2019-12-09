package org.oppia.app.databinding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.oppia.app.R
import java.text.SimpleDateFormat
import java.util.*

/** Binds date text with relative time. */
@BindingAdapter("profile:created")
fun setProfileDataText(textView: TextView, timestamp: Long) {
  val sdf = SimpleDateFormat("dd MMM yyyy")
  val time = sdf.format(Date(timestamp))
  textView.text = String.format(textView.context.getString(R.string.profile_edit_created, time))
}
