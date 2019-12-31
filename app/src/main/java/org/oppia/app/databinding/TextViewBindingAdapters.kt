package org.oppia.app.databinding

import android.util.Log
import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.oppia.app.R
import java.text.SimpleDateFormat
import java.util.*
import java.text.ParseException
import java.util.concurrent.TimeUnit

/** Binds date text with relative time. */
@BindingAdapter("profile:created")
fun setProfileDataText(textView: TextView, timestamp: Long) {
  // TODO(#555): Create one central utility file from where we should access date format or even convert date timestamp to string from that file.
  val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
  val time = sdf.format(Date(timestamp))
  textView.text = String.format(textView.context.getString(R.string.profile_edit_created, time))
}

@BindingAdapter("profile:lastVisited")
fun setProfileLastVisitedText(textView: TextView, timestamp: Long) {
  // TODO(#555): Create one central utility file from where we should access date format or even convert date timestamp to string from that file.
  textView.text =
    String.format(textView.context.getString(R.string.profile_last_used) + " " + getTimeAgo(Date(timestamp)))
}

private val SECOND_MILLIS = 1000
private val MINUTE_MILLIS = 60 * SECOND_MILLIS
private val HOUR_MILLIS = 60 * MINUTE_MILLIS
private val DAY_MILLIS = 24 * HOUR_MILLIS

private fun currentDate(): Date {
  val calendar = Calendar.getInstance()
  return calendar.time
}

fun getTimeAgo(date: Date): String {
  var time = date.time
  if (time < 1000000000000L) {
    time *= 1000
  }

  val now = currentDate().time
  if (time > now || time <= 0) {
    return "in the future"
  }

  val diff = now - time
  return if (diff < MINUTE_MILLIS) {
    "moments ago"
  } else if (diff < 2 * MINUTE_MILLIS) {
    "a minute ago"
  } else if (diff < 60 * MINUTE_MILLIS) {
    (diff / MINUTE_MILLIS).toString() + " minutes ago"
  } else if (diff < 2 * HOUR_MILLIS) {
    "an hour ago"
  } else if (diff < 24 * HOUR_MILLIS) {
    (diff / HOUR_MILLIS).toString() + " hours ago"
  } else if (diff < 48 * HOUR_MILLIS) {
    "yesterday"
  } else {
    (diff / DAY_MILLIS).toString() + " days ago"
  }
}
