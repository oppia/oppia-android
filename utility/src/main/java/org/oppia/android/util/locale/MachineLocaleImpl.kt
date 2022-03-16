package org.oppia.android.util.locale

import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.util.system.OppiaClock
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// TODO(#3766): Restrict to be 'internal'.
/**
 * Implementation of [OppiaLocale.MachineLocale].
 *
 * Note that this implementation is backed by the Android US [Locale] for consistency among
 * machine-used strings across runtimes. This is per the advice documented here:
 * https://developer.android.com/reference/java/util/Locale#default_locale and since the Oppia
 * Android team generally uses the US locale for identifiers & code.
 */
class MachineLocaleImpl @Inject constructor(
  private val oppiaClock: OppiaClock
) : OppiaLocale.MachineLocale(machineLocaleContext) {
  private val parsableDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd", machineAndroidLocale) }
  private val timeFormat by lazy {
    DateFormat.getTimeInstance(DateFormat.MEDIUM, machineAndroidLocale)
  }

  override fun String.formatForMachines(vararg args: Any?): String =
    format(machineAndroidLocale, *args)

  override fun String.toMachineLowerCase(): String = toLowerCase(machineAndroidLocale)

  override fun String.toMachineUpperCase(): String = toUpperCase(machineAndroidLocale)

  override fun String.capitalizeForMachines(): String = capitalize(machineAndroidLocale)

  override fun String.decapitalizeForMachines(): String = decapitalize(machineAndroidLocale)

  override fun String.endsWithIgnoreCase(suffix: String): Boolean =
    toMachineLowerCase().endsWith(suffix.toMachineLowerCase())

  override fun String?.equalsIgnoreCase(other: String?): Boolean =
    this?.toMachineLowerCase() == other?.toMachineLowerCase()

  override fun getCurrentTimeOfDay(): TimeOfDay {
    return when (oppiaClock.getCurrentCalendar().get(Calendar.HOUR_OF_DAY)) {
      in 4..11 -> TimeOfDay.MORNING
      in 12..16 -> TimeOfDay.AFTERNOON
      in 0..3, in 17..23 -> TimeOfDay.EVENING
      else -> TimeOfDay.UNKNOWN
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

  override fun computeCurrentTimeString(): String =
    timeFormat.format(Date(oppiaClock.getCurrentTimeMs()))

  override fun numberFormatter(number: Int): String {
      val numberFormat: NumberFormat = DecimalFormat("##")
      return numberFormat.format(number)
  }

  override fun toString(): String = "MachineLocaleImpl[context=$machineLocaleContext]"

  override fun equals(other: Any?): Boolean {
    return (other as? MachineLocaleImpl)?.let { locale ->
      localeContext == locale.localeContext
    } ?: false
  }

  override fun hashCode(): Int = localeContext.hashCode()

  private class OppiaDateImpl(private val date: Date, private val today: Date) : OppiaDate {
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
