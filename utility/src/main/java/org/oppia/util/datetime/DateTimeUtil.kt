package org.oppia.util.datetime

import android.annotation.SuppressLint
import android.content.Context
import org.oppia.util.R
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/** Utility to manage date and time. */
@Singleton
class DateTimeUtil @Inject constructor(private val context: Context) {

  fun displayGreeting(): String {
    val c = Calendar.getInstance()
    return when (c.get(Calendar.HOUR_OF_DAY)) {
      in 5..11 -> context.getString(R.string.good_morning)
      in 12..16 -> context.getString(R.string.good_afternoon)
      in 17 downTo 4 -> context.getString(R.string.good_evening)
      else -> context.getString(R.string.good_evening)
    }
  }

  @SuppressLint("NewApi")
  fun setFakeTime(dateTime: String){
    Instant.now(Clock.fixed(
      Instant.parse(dateTime),
      ZoneOffset.UTC))

  }
}
