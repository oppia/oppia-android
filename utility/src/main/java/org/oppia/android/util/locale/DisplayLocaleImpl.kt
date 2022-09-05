package org.oppia.android.util.locale

import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.core.text.TextUtilsCompat
import org.oppia.android.app.model.OppiaLocaleContext
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

// TODO(#3766): Restrict to be 'internal'.
/** Implementation of [OppiaLocale.DisplayLocale]. */
class DisplayLocaleImpl(
  localeContext: OppiaLocaleContext,
  private val machineLocale: MachineLocale,
  private val androidLocaleFactory: AndroidLocaleFactory,
  private val formatterFactory: OppiaBidiFormatter.Factory
) : OppiaLocale.DisplayLocale(localeContext) {
  // TODO(#3766): Restrict to be 'internal'.
  /** The [Locale] used for user-facing string formatting in this display locale. */
  val formattingLocale: Locale by lazy { androidLocaleFactory.createAndroidLocale(localeContext) }
  private val dateFormat by lazy {
    DateFormat.getDateInstance(DATE_FORMAT_LENGTH, formattingLocale)
  }
  private val timeFormat by lazy {
    DateFormat.getTimeInstance(TIME_FORMAT_LENGTH, formattingLocale)
  }
  private val dateTimeFormat by lazy {
    DateFormat.getDateTimeInstance(DATE_FORMAT_LENGTH, TIME_FORMAT_LENGTH, formattingLocale)
  }
  private val numberFormat by lazy { NumberFormat.getNumberInstance(formattingLocale) }
  private val bidiFormatter by lazy { formatterFactory.createFormatter(formattingLocale) }

  // TODO(#3766): Restrict to be 'internal'.
  /**
   * Updates the specified [Configuration] to reference the formatting locale backing this display
   * locale. Note that this may not be sufficient for actually updating the configuration--it may
   * need to be done during activity initialization and in some cases an activity recreation may be
   * necessary.
   */
  fun setAsDefault(configuration: Configuration) {
    configuration.setLocale(formattingLocale)
  }

  override fun formatLong(value: Long): String = numberFormat.format(value)

  override fun formatDouble(value: Double): String = numberFormat.format(value)

  override fun toHumanReadableString(number: Int): String? = numberFormat.format(number)

  override fun computeDateString(timestampMillis: Long): String =
    dateFormat.format(Date(timestampMillis))

  override fun computeTimeString(timestampMillis: Long): String =
    timeFormat.format(Date(timestampMillis))

  override fun computeDateTimeString(timestampMillis: Long): String =
    dateTimeFormat.format(Date(timestampMillis))

  override fun getLayoutDirection(): Int {
    return TextUtilsCompat.getLayoutDirectionFromLocale(formattingLocale)
  }

  override fun String.formatInLocaleWithWrapping(vararg args: CharSequence): String {
    return formatInLocaleWithoutWrapping(
      *args.map { arg -> bidiFormatter.wrapText(arg) }.toTypedArray()
    )
  }

  override fun String.formatInLocaleWithoutWrapping(vararg args: CharSequence): String =
    format(formattingLocale, *args)

  override fun String.capitalizeForHumans(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(formattingLocale) else it.toString() }

  override fun Resources.getStringInLocale(@StringRes id: Int): String = getString(id)

  override fun Resources.getStringInLocaleWithWrapping(
    id: Int,
    vararg formatArgs: CharSequence
  ): String = getStringInLocale(id).formatInLocaleWithWrapping(*formatArgs)

  override fun Resources.getStringInLocaleWithoutWrapping(
    id: Int,
    vararg formatArgs: CharSequence
  ): String = getStringInLocale(id).formatInLocaleWithoutWrapping(*formatArgs)

  override fun Resources.getStringArrayInLocale(@ArrayRes id: Int): List<String> =
    getStringArray(id).toList()

  override fun Resources.getQuantityStringInLocale(id: Int, quantity: Int): String =
    getQuantityTextInLocale(id, quantity).toString()

  override fun Resources.getQuantityStringInLocaleWithWrapping(
    id: Int,
    quantity: Int,
    vararg formatArgs: CharSequence
  ): String = getQuantityStringInLocale(id, quantity).formatInLocaleWithWrapping(*formatArgs)

  override fun Resources.getQuantityStringInLocaleWithoutWrapping(
    id: Int,
    quantity: Int,
    vararg formatArgs: CharSequence
  ): String = getQuantityStringInLocale(id, quantity).formatInLocaleWithoutWrapping(*formatArgs)

  override fun Resources.getQuantityTextInLocale(id: Int, quantity: Int): CharSequence =
    getQuantityText(id, quantity)

  override fun toString(): String = "DisplayLocaleImpl[context=$localeContext]"

  override fun equals(other: Any?): Boolean {
    return (other as? DisplayLocaleImpl)?.let { locale ->
      localeContext == locale.localeContext && machineLocale == locale.machineLocale
    } ?: false
  }

  override fun hashCode(): Int = Objects.hash(localeContext, machineLocale)

  private companion object {
    private const val DATE_FORMAT_LENGTH = DateFormat.LONG
    private const val TIME_FORMAT_LENGTH = DateFormat.SHORT
  }
}
