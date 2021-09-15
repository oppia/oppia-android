package org.oppia.android.domain.locale

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.text.BidiFormatter
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.core.text.TextUtilsCompat
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.util.locale.OppiaLocale

// TODO(#3766): Restrict to be 'internal'.
class DisplayLocaleImpl(
  localeContext: OppiaLocaleContext,
  private val machineLocale: MachineLocale,
  private val androidLocaleFactory: AndroidLocaleFactory
): OppiaLocale.DisplayLocale(localeContext) {
  // TODO(#3766): Restrict to be 'internal'.
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
  private val bidiFormatter by lazy { BidiFormatter.getInstance(formattingLocale) }

  // TODO(#3766): Restrict to be 'internal'.
  fun setAsDefault(configuration: Configuration) {
    configuration.setLocale(formattingLocale)
  }

  override fun computeDateString(timestampMillis: Long): String =
    dateFormat.format(Date(timestampMillis))

  override fun computeTimeString(timestampMillis: Long): String =
    timeFormat.format(Date(timestampMillis))

  override fun computeDateTimeString(timestampMillis: Long): String =
    dateTimeFormat.format(Date(timestampMillis))

  override fun getLayoutDirection(): Int {
    return TextUtilsCompat.getLayoutDirectionFromLocale(formattingLocale)
  }

  override fun String.formatInLocale(vararg args: Any?): String =
    format(formattingLocale, *args.map { arg ->
      if (arg is CharSequence) bidiFormatter.unicodeWrap(arg) else arg
    }.toTypedArray())

  override fun String.capitalizeForHumans(): String = capitalize(formattingLocale)

  override fun Resources.getStringInLocale(@StringRes id: Int): String = getString(id)

  override fun Resources.getStringInLocale(@StringRes id: Int, vararg formatArgs: Any?): String =
    getStringInLocale(id).formatInLocale(*formatArgs)

  override fun Resources.getStringArrayInLocale(@ArrayRes id: Int): List<String> =
    getStringArray(id).toList()

  override fun Resources.getQuantityStringInLocale(id: Int, quantity: Int): String =
    getQuantityTextInLocale(id, quantity).toString()

  override fun Resources.getQuantityStringInLocale(
    id: Int, quantity: Int, vararg formatArgs: Any?
  ): String = getQuantityStringInLocale(id, quantity).formatInLocale(*formatArgs)

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
