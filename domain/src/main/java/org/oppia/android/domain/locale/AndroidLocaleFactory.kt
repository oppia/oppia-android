package org.oppia.android.domain.locale

import android.os.Build
import java.util.Locale
import javax.inject.Inject
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.util.locale.OppiaLocale

class AndroidLocaleFactory @Inject constructor(
  private val machineLocale: OppiaLocale.MachineLocale
) {
  fun createAndroidLocale(localeContext: OppiaLocaleContext): Locale {
    val languageId = localeContext.getLanguageId()
    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    // Locale is always computed based on the Android resource app string identifier if that's
    // defined. If it isn't, the routine falls back to app language & region country codes (which
    // also provides interoperability with system-derived contexts). Note that if either identifier
    // is missing for the primary language, the fallback is used instead (if available), except that
    // IETF BCP 47 tags from the primary language are used before Android resource codes from the
    // fallback. Thus, the order of this list is important. Finally, a basic check is done here to
    // make sure this version of Android can actually render the target language.
    val potentialProfiles =
      computePotentialLanguageProfiles(localeContext, languageId) +
        computePotentialFallbackLanguageProfiles(localeContext, fallbackLanguageId)

    // Either find the first supported profile or force the locale to use the exact definition
    // values, depending on whether to fail over to a forced locale.
    val firstSupportedProfile = potentialProfiles.findFirstSupported()
    val selectedProfile = firstSupportedProfile
      ?: languageId.computeForcedProfile(localeContext.regionDefinition)
    return Locale(selectedProfile.languageCode, selectedProfile.regionCode)
  }

  private fun computePotentialLanguageProfiles(
    localeContext: OppiaLocaleContext,
    languageId: LanguageId
  ): List<AndroidLocaleProfile?> =
    computeLanguageProfiles(localeContext, localeContext.languageDefinition, languageId)

  private fun computePotentialFallbackLanguageProfiles(
    localeContext: OppiaLocaleContext,
    fallbackLanguageId: LanguageId
  ): List<AndroidLocaleProfile?> {
    return computeLanguageProfiles(
      localeContext, localeContext.fallbackLanguageDefinition, fallbackLanguageId
    )
  }

  private fun computeLanguageProfiles(
    localeContext: OppiaLocaleContext,
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
    private val availableLocaleProfiles by lazy {
      Locale.getAvailableLocales().map(AndroidLocaleProfile::createFrom)
    }

    private fun String.divide(delimiter: String): Pair<String, String>? {
      val results = split(delimiter)
      return if (results.size == 2) {
        results[0] to results[1]
      } else null
    }
  }
}
