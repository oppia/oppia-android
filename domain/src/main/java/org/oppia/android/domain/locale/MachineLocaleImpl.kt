package org.oppia.android.domain.locale

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.util.system.OppiaClock

// TODO: documentation. Explain that US locale is always used for machine-readable strings.
// TODO(#3766): Restrict to be 'internal'.
class MachineLocaleImpl(
  private val oppiaClock: OppiaClock
): OppiaLocale.MachineLocale(machineLocaleContext) {
  private val parsableDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", machineAndroidLocale) }

  override fun String.formatForMachines(vararg args: Any?): String =
    format(machineAndroidLocale, *args)

  override fun String.toMachineLowerCase(): String = toLowerCase(machineAndroidLocale)

  override fun String.toMachineUpperCase(): String = toUpperCase(machineAndroidLocale)

  override fun String.capitalizeForMachines(): String = capitalize(machineAndroidLocale)

  override fun String.decapitalizeForMachines(): String = decapitalize(machineAndroidLocale)

  override fun String?.equalsIgnoreCase(other: String?): Boolean =
    this?.toMachineLowerCase() == other?.toMachineLowerCase()

  override fun getCurrentTimeOfDay(): TimeOfDay? {
    return when (oppiaClock.getCurrentCalendar().get(Calendar.HOUR_OF_DAY)) {
      in 4..11 -> TimeOfDay.MORNING
      in 12..16 -> TimeOfDay.AFTERNOON
      in 17 downTo 3 -> TimeOfDay.EVENING
      else -> null
    }
  }

  override fun parseOppiaDate(dateString: String): OppiaDate? {
    val parsedDate = try {
      parsableDateFormat.parse(dateString)
    } catch (e: ParseException) {
      null
    }
    return parsedDate?.let { OppiaDateImpl(it, oppiaClock.getCurrentDate()) }
  }

  private class OppiaDateImpl(private val date: Date, private val today: Date): OppiaDate {
    override fun isBeforeToday(): Boolean = date.before(today)
  }

  private companion object {
    private val machineLocaleContext by lazy {
      OppiaLocaleContext.newBuilder().apply {
        languageDefinition = LanguageSupportDefinition.newBuilder().apply {
          language = OppiaLanguage.ENGLISH
        }.build()
        regionDefinition = RegionSupportDefinition.newBuilder().apply {
          region = OppiaRegion.UNITED_STATES
        }.build()
      }.build()
    }

    private val machineAndroidLocale by lazy { Locale.US }
  }
}
