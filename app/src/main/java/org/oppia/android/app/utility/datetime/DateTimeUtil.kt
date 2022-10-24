package org.oppia.android.app.utility.datetime

import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

/** Per-activity utility to manage date and time for user-facing strings. */
class DateTimeUtil @Inject constructor(
  private val machineLocale: OppiaLocale.MachineLocale,
  private val resourceHandler: AppLanguageResourceHandler
) {
  /**
   * Returns a user-readable string based on the time of day (to be concatenated as part of a
   * greeting for the user).
   */
  fun getGreetingMessage(): String {
    return when (machineLocale.getCurrentTimeOfDay()) {
      OppiaLocale.MachineLocale.TimeOfDay.MORNING ->
        resourceHandler.getStringInLocale(R.string.date_time_util_home_screen_good_morning_greeting_fragment)
      OppiaLocale.MachineLocale.TimeOfDay.AFTERNOON ->
        resourceHandler.getStringInLocale(R.string.date_time_util_home_screen_good_afternoon_greeting_fragment)
      OppiaLocale.MachineLocale.TimeOfDay.EVENING, OppiaLocale.MachineLocale.TimeOfDay.UNKNOWN ->
        resourceHandler.getStringInLocale(R.string.date_time_util_home_screen_good_evening_greeting_fragment)
    }
  }

  /** Injector to retrieve instances of [DateTimeUtil] from Dagger. */
  interface Injector {
    /** Returns [DateTimeUtil] for the current Dagger graph. */
    fun getDateTimeUtil(): DateTimeUtil
  }
}
