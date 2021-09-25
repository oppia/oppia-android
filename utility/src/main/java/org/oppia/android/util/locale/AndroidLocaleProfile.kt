package org.oppia.android.util.locale

import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.RegionSupportDefinition
import java.util.Locale

/**
 * A profile to represent an Android [Locale] object which can be used to easily compare different
 * locales (based on the properties the app cares about), or reconstruct a [Locale] object.
 *
 * @property languageCode the IETF BCP 47 or ISO 639-2 language code
 * @property regionCode the IETF BCP 47 or ISO 3166 alpha-2 region code
 */
data class AndroidLocaleProfile(val languageCode: String, val regionCode: String) {
  /** Returns whether this profile matches the specified [otherProfile] for the given locale. */
  fun matches(
    machineLocale: OppiaLocale.MachineLocale,
    otherProfile: AndroidLocaleProfile,
  ): Boolean {
    return machineLocale.run {
      languageCode.equalsIgnoreCase(otherProfile.languageCode)
    } && machineLocale.run {
      val regionsAreEqual = regionCode.equalsIgnoreCase(otherProfile.regionCode)
      val eitherRegionIsWildcard =
        regionCode == REGION_WILDCARD || otherProfile.regionCode == REGION_WILDCARD
      return@run regionsAreEqual || eitherRegionIsWildcard
    }
  }

  fun computeIetfLanguageTag(): String {
    return if (regionCode.isNotEmpty()) {
      "$languageCode-$regionCode"
    } else languageCode
  }

  companion object {
    /** A wildcard that will match against any region when provided. */
    const val REGION_WILDCARD = "*"

    /** Returns a new [AndroidLocaleProfile] that represents the specified Android [Locale]. */
    fun createFrom(androidLocale: Locale): AndroidLocaleProfile =
      AndroidLocaleProfile(androidLocale.language, androidLocale.country)

    /**
     * Returns a new [AndroidLocaleProfile] using the IETF BCP 47 tag in the provided [LanguageId].
     *
     * This will return null in a number of scenarios:
     * - If the provided [LanguageId] doesn't have an IETF BCP 47 ID
     * - If the IETF BCP 47 tag is malformed
     * - If the provided [RegionSupportDefinition] doesn't have an IETF BCP 47 region ID
     *
     * Further, this method will only use the provided [regionDefinition] if the IETF BCP 47
     * language tag doesn't include a region component. If the [regionDefinition] is null then the
     * returned [AndroidLocaleProfile] will have a wildcard match against any region (meaning only
     * the language code needs to match).
     */
    fun createFromIetfDefinitions(
      languageId: LanguageId,
      regionDefinition: RegionSupportDefinition?
    ): AndroidLocaleProfile? {
      if (!languageId.hasIetfBcp47Id()) return null
      return when {
        "-" in languageId.ietfBcp47Id.ietfLanguageTag -> {
          val (languageCode, regionCode) =
            languageId.ietfBcp47Id.ietfLanguageTag.divide("-") ?: return null
          maybeConstructProfile(languageCode, regionCode)
        }
        regionDefinition != null -> {
          if (!regionDefinition.hasRegionId()) return null
          maybeConstructProfile(
            languageId.ietfBcp47Id.ietfLanguageTag, regionDefinition.regionId.ietfRegionTag
          )
        }
        else -> {
          maybeConstructProfile(
            languageId.ietfBcp47Id.ietfLanguageTag, regionCode = "", emptyRegionAsWildcard = true
          )
        }
      }
    }

    /**
     * Returns a new [AndroidLocaleProfile] using the macaronic ID in the provided [LanguageId].
     *
     * This will return null if the [LanguageId] either doesn't have a macaronic ID defined, or if
     * it's malformed. Macaronic IDs are always expected to include language and region components,
     * so both fields are guaranteed to be populated in a returned [AndroidLocaleProfile].
     */
    fun createFromMacaronicLanguage(
      languageId: LanguageId
    ): AndroidLocaleProfile? {
      if (!languageId.hasMacaronicId()) return null
      val (languageCode, regionCode) =
        languageId.macaronicId.combinedLanguageCode.divide("-") ?: return null
      return maybeConstructProfile(languageCode, regionCode)
    }

    private fun maybeConstructProfile(
      languageCode: String,
      regionCode: String,
      emptyRegionAsWildcard: Boolean = false
    ): AndroidLocaleProfile? {
      return if (languageCode.isNotEmpty() && (regionCode.isNotEmpty() || emptyRegionAsWildcard)) {
        val adjustedRegionCode = if (emptyRegionAsWildcard && regionCode.isEmpty()) {
          REGION_WILDCARD
        } else regionCode
        AndroidLocaleProfile(languageCode, adjustedRegionCode)
      } else null
    }

    private fun String.divide(delimiter: String): Pair<String, String>? {
      val results = split(delimiter)
      return if (results.size == 2) {
        results[0] to results[1]
      } else null
    }
  }
}
