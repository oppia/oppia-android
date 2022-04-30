package org.oppia.android.util.locale

import android.os.Build
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.util.locale.AndroidLocaleProfile
import java.util.Locale
import javax.inject.Inject

/**
 * Factory for creating new Android [Locale]s. This is meant only to be used within the locale
 * domain package.
 */
class AndroidLocaleFactory @Inject constructor(
  private val machineLocale: OppiaLocale.MachineLocale
) {
  /**
   * Returns a new [Locale] that matches the given [OppiaLocaleContext]. Note this will
   * automatically fail over to the context's backup fallback language if the primary language
   * doesn't match any available locales on the device. Further, if no locale can be found, the
   * returned [Locale] will be forced to match the specified context (which will result in some
   * default/root locale behavior in Android).
   */
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
    return Locale(selectedProfile.languageCode, selectedProfile.getNonWildcardRegionCode())
  }

  private fun computePotentialLanguageProfiles(
    localeContext: OppiaLocaleContext,
    languageId: LanguageId
  ): List<AndroidLocaleProfile> =
    computeLanguageProfiles(localeContext, localeContext.languageDefinition, languageId)

  private fun computePotentialFallbackLanguageProfiles(
    localeContext: OppiaLocaleContext,
    fallbackLanguageId: LanguageId
  ): List<AndroidLocaleProfile> {
    return computeLanguageProfiles(
      localeContext, localeContext.fallbackLanguageDefinition, fallbackLanguageId
    )
  }

  private fun computeLanguageProfiles(
    localeContext: OppiaLocaleContext,
    definition: LanguageSupportDefinition,
    languageId: LanguageId
  ): List<AndroidLocaleProfile> {
    return if (definition.minAndroidSdkVersion <= Build.VERSION.SDK_INT) {
      listOfNotNull(
        languageId.computeLocaleProfileFromAndroidId(),
        AndroidLocaleProfile.createFromIetfDefinitions(languageId, localeContext.regionDefinition),
        AndroidLocaleProfile.createFromMacaronicLanguage(languageId)
      )
    } else listOf()
  }

  private fun LanguageId.computeLocaleProfileFromAndroidId(): AndroidLocaleProfile? {
    return if (hasAndroidResourcesLanguageId()) {
      androidResourcesLanguageId.run {
        // Empty region codes are allowed for Android resource IDs since they should always be used
        // verbatim to ensure the correct Android resource string can be computed (such as for macro
        // languages).
        maybeConstructProfileWithWildcardSupport(languageCode, regionCode)
      }
    } else null
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
    if (hasAndroidResourcesLanguageId()) {
      // Create a locale exactly matching the Android ID profile.
      return AndroidLocaleProfile(
        androidResourcesLanguageId.languageCode, androidResourcesLanguageId.regionCode
      )
    }
    return when (languageTypeCase) {
      LanguageId.LanguageTypeCase.IETF_BCP47_ID -> {
        AndroidLocaleProfile(
          ietfBcp47Id.ietfLanguageTag, regionDefinition.regionId.ietfRegionTag
        )
      }
      LanguageId.LanguageTypeCase.MACARONIC_ID -> {
        AndroidLocaleProfile.createFromMacaronicLanguage(this)
          ?: error("Invalid macaronic ID: ${macaronicId.combinedLanguageCode}")
      }
      LanguageId.LanguageTypeCase.LANGUAGETYPE_NOT_SET, null ->
        error("Invalid language case: $languageTypeCase")
    }
  }

  private fun maybeConstructProfileWithWildcardSupport(
    languageCode: String,
    regionCode: String
  ): AndroidLocaleProfile? {
    return if (languageCode.isNotEmpty()) {
      val adjustedRegionCode = if (regionCode.isEmpty()) {
        AndroidLocaleProfile.REGION_WILDCARD
      } else regionCode
      AndroidLocaleProfile(languageCode, adjustedRegionCode)
    } else null
  }

  private fun List<AndroidLocaleProfile>.findFirstSupported(): AndroidLocaleProfile? = find {
    availableLocaleProfiles.any { availableProfile ->
      availableProfile.matches(machineLocale, it)
    }
  }

  private companion object {
    private val availableLocaleProfiles by lazy {
      Locale.getAvailableLocales().map(AndroidLocaleProfile::createFrom)
    }

    private fun AndroidLocaleProfile.getNonWildcardRegionCode(): String {
      return if (regionCode != AndroidLocaleProfile.REGION_WILDCARD) {
        regionCode
      } else ""
    }
  }
}
