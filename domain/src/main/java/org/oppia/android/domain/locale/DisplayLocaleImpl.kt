package org.oppia.android.domain.locale

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.text.BidiFormatter
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
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
  private val machineLocale: MachineLocale
): OppiaLocale.DisplayLocale(localeContext) {
  // TODO(#3766): Restrict to be 'internal'.
  val formattingLocale: Locale by lazy { computeLocale() }
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

  private fun computeLocale(): Locale {
    // Locale is always computed based on the Android resource app string identifier if that's
    // defined. If it isn't, the routine falls back to app language & region country codes (which
    // also provides interoperability with system-derived contexts). Note that if either identifier
    // is missing for the primary language, the fallback is used instead (if available), except that
    // IETF BCP 47 tags from the primary language are used before Android resource codes from the
    // fallback. Thus, the order of this list is important. Finally, a basic check is done here to
    // make sure this version of Android can actually render the target language.
    val potentialProfiles =
      computePotentialLanguageProfiles() + computePotentialFallbackLanguageProfiles()

    // Either find the first supported profile or force the locale to use the exact definition
    // values.
    val selectedProfile =
      potentialProfiles.findFirstSupported()
        ?: getLanguageId().computeForcedProfile(localeContext.regionDefinition)

    return Locale(selectedProfile.languageCode, selectedProfile.regionCode)
  }

  private fun computePotentialLanguageProfiles(): List<AndroidLocaleProfile?> =
    computeLanguageProfiles(localeContext.languageDefinition, getLanguageId())

  private fun computePotentialFallbackLanguageProfiles(): List<AndroidLocaleProfile?> =
    computeLanguageProfiles(localeContext.fallbackLanguageDefinition, getFallbackLanguageId())

  private fun computeLanguageProfiles(
    definition: LanguageSupportDefinition,
    languageId: LanguageId
  ): List<AndroidLocaleProfile?> {
    return if (definition.minAndroidSdkVersion <= Build.VERSION.SDK_INT) {
      listOf(
        languageId.computeLocaleProfileFromAndroidId(),
        languageId.computeLocaleProfileFromIetfDefinitions(localeContext.regionDefinition),
        languageId.computeLocaleProfileFromMacaronicLanguage()
      )
    } else listOf()
  }

  private fun LanguageId.computeLocaleProfileFromAndroidId(): AndroidLocaleProfile? {
    return if (hasAndroidResourcesLanguageId()) {
      androidResourcesLanguageId.run { maybeConstructProfile(languageCode, regionCode) }
    } else null
  }

  private fun LanguageId.computeLocaleProfileFromIetfDefinitions(
    regionDefinition: RegionSupportDefinition
  ): AndroidLocaleProfile? {
    if (!hasIetfBcp47Id()) return null
    if (!regionDefinition.hasRegionId()) return null
    return maybeConstructProfile(
      ietfBcp47Id.ietfLanguageTag, regionDefinition.regionId.ietfRegionTag
    )
  }

  private fun LanguageId.computeLocaleProfileFromMacaronicLanguage(): AndroidLocaleProfile? {
    if (!hasMacaronicId()) return null
    val (languageCode, regionCode) = macaronicId.combinedLanguageCode.divide("-") ?: return null
    return maybeConstructProfile(languageCode, regionCode)
  }

  /**
   * Returns an [AndroidLocaleProfile] for this [LanguageId] and the specified
   * [RegionSupportDefinition] based on the language's & region's IETF BCP 47 codes regardless of
   * whether they're defined (i.e. it's fine to default to empty string here since that will
   * leverage Android's own root locale behavior).
   */
  private fun LanguageId.computeForcedProfile(
    regionDefinition: RegionSupportDefinition
  ): AndroidLocaleProfile {
    return AndroidLocaleProfile(
      ietfBcp47Id.ietfLanguageTag, regionDefinition.regionId.ietfRegionTag
    )
  }

  private fun maybeConstructProfile(
    languageCode: String, regionCode: String
  ): AndroidLocaleProfile? {
    return if (languageCode.isNotEmpty() && regionCode.isNotEmpty()) {
      AndroidLocaleProfile(languageCode, regionCode)
    } else null
  }

  private fun List<AndroidLocaleProfile?>.findFirstSupported(): AndroidLocaleProfile? = find {
    it?.let { profileToMatch ->
      availableLocaleProfiles.any { availableProfile ->
        availableProfile.matches(machineLocale, profileToMatch)
      }
    } ?: false // Ignore null profiles.
  }

  private companion object {
    private const val DATE_FORMAT_LENGTH = DateFormat.LONG
    private const val TIME_FORMAT_LENGTH = DateFormat.SHORT

    private val availableLocaleProfiles by lazy {
      Locale.getAvailableLocales().map(AndroidLocaleProfile::createFrom)
    }
  }
}

private fun String.divide(delimiter: String): Pair<String, String>? {
  val results = split(delimiter)
  return if (results.size == 2) {
    results[0] to results[1]
  } else null
}
