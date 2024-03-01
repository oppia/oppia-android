package org.oppia.android.util.locale

import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.RegionSupportDefinition
import java.util.Locale
import javax.inject.Inject

/**
 * A profile to represent an Android [Locale] object which can be used to easily compare different
 * locales (based on the properties the app cares about), or reconstruct a [Locale] object.
 *
 * Subclasses of this sealed class operate on a language code and/or region code. The language code
 * is an IETF BCP 47 or ISO 639-2/3 language code, or empty if unknown or not specified. The region
 * code is an IETF BCP 47 or ISO 3166 alpha-2 region code, or empty if unknown or not specified.
 *
 * New instances should be created using [Factory].
 */
sealed class AndroidLocaleProfile {
  /**
   * An IETF BCP 47-esque language tag that represents this locale profile. For profiles that have
   * valid IETF BCP 47 language & region codes, the returned tag should be a valid IETF BCP 47
   * language tag.
   */
  abstract val ietfLanguageTag: String

  /** Returns whether this profile matches the specified [otherProfile] for the given locale. */
  abstract fun matches(otherProfile: AndroidLocaleProfile): Boolean

  /** Returns an Android [Locale] compatible with this profile. */
  abstract fun computeAndroidLocale(): Locale

  /**
   * An [AndroidLocaleProfile] that provides both a non-empty language and region code.
   *
   * Note that, generally, this should never need to be created directly. Instead, [Factory] should
   * be used to create new instances of profiles.
   *
   * @property languageCode the lowercase two-letter language code in this profile
   * @property regionCode the lowercase two-letter region code in this profile
   * @property regionCodeUpperCase the uppercase version of [regionCode]
   */
  data class LanguageAndRegionProfile(
    val languageCode: String,
    val regionCode: String,
    private val regionCodeUpperCase: String
  ) : AndroidLocaleProfile() {
    // The region code is usually uppercase in IETF BCP-47 tags when extending a language code.
    override val ietfLanguageTag = "$languageCode-$regionCodeUpperCase"

    override fun matches(otherProfile: AndroidLocaleProfile): Boolean {
      return when (otherProfile) {
        is LanguageAndRegionProfile ->
          languageCode == otherProfile.languageCode && regionCode == otherProfile.regionCode
        is LanguageAndWildcardRegionProfile -> languageCode == otherProfile.languageCode
        is LanguageOnlyProfile, is RegionOnlyProfile, is RootProfile -> false
      }
    }

    override fun computeAndroidLocale(): Locale = Locale(languageCode, regionCode)
  }

  /**
   * An [AndroidLocaleProfile] that provides only a non-empty region code.
   *
   * Note that, generally, this should never need to be created directly. Instead, [Factory] should
   * be used to create new instances of profiles.
   *
   * @property regionCode the lowercase two-letter region code in this profile
   */
  data class RegionOnlyProfile(val regionCode: String) : AndroidLocaleProfile() {
    override val ietfLanguageTag = regionCode

    override fun matches(otherProfile: AndroidLocaleProfile): Boolean =
      otherProfile is RegionOnlyProfile && regionCode == otherProfile.regionCode

    override fun computeAndroidLocale(): Locale = Locale(/* language = */ "", regionCode)
  }

  /**
   * An [AndroidLocaleProfile] that provides only a non-empty language code.
   *
   * Note that, generally, this should never need to be created directly. Instead, [Factory] should
   * be used to create new instances of profiles.
   *
   * @property languageCode the lowercase two-letter language code in this profile
   */
  data class LanguageOnlyProfile(val languageCode: String) : AndroidLocaleProfile() {
    override val ietfLanguageTag = languageCode

    override fun matches(otherProfile: AndroidLocaleProfile): Boolean {
      return when (otherProfile) {
        is LanguageOnlyProfile -> languageCode == otherProfile.languageCode
        is LanguageAndWildcardRegionProfile -> languageCode == otherProfile.languageCode
        is LanguageAndRegionProfile, is RegionOnlyProfile, is RootProfile -> false
      }
    }

    override fun computeAndroidLocale(): Locale = Locale(languageCode)
  }

  /**
   * An [AndroidLocaleProfile] that provides only a non-empty language code, but matches (e.g. via
   * [matches]) with any profile that has the same language code.
   *
   * Note that, generally, this should never need to be created directly. Instead, [Factory] should
   * be used to create new instances of profiles.
   *
   * @property languageCode the lowercase two-letter language code in this profile
   */
  data class LanguageAndWildcardRegionProfile(val languageCode: String) : AndroidLocaleProfile() {
    override val ietfLanguageTag = languageCode

    override fun matches(otherProfile: AndroidLocaleProfile): Boolean {
      return when (otherProfile) {
        is LanguageAndRegionProfile -> languageCode == otherProfile.languageCode
        is LanguageAndWildcardRegionProfile -> languageCode == otherProfile.languageCode
        is LanguageOnlyProfile -> languageCode == otherProfile.languageCode
        is RegionOnlyProfile, is RootProfile -> false
      }
    }

    override fun computeAndroidLocale(): Locale = Locale(languageCode)
  }

  /**
   * An [AndroidLocaleProfile] that provides the system's root locale ([Locale.ROOT]).
   *
   * Note that, generally, this should never need to be used directly. Instead, [Factory] should be
   * used to create new instances of profiles.
   */
  object RootProfile : AndroidLocaleProfile() {
    override val ietfLanguageTag = ""

    override fun matches(otherProfile: AndroidLocaleProfile): Boolean = otherProfile is RootProfile

    override fun computeAndroidLocale(): Locale = Locale.ROOT
  }

  /** An application-injectable factory for creating new [AndroidLocaleProfile]s. */
  class Factory @Inject constructor(private val machineLocale: OppiaLocale.MachineLocale) {
    /** Returns a new [AndroidLocaleProfile] that represents the specified Android [Locale]. */
    fun createFrom(androidLocale: Locale): AndroidLocaleProfile {
      val languageCode = androidLocale.language
      val regionCode = androidLocale.country
      return when {
        languageCode.isNotEmpty() && regionCode.isNotEmpty() -> {
          LanguageAndRegionProfile(
            languageCode.asLowerCase(), regionCode.asLowerCase(), regionCode.asUpperCase()
          )
        }
        regionCode.isNotEmpty() -> RegionOnlyProfile(regionCode.asLowerCase())
        languageCode.isNotEmpty() -> LanguageOnlyProfile(languageCode.asLowerCase())
        else -> RootProfile
      }
    }

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
      return when {
        !languageId.hasIetfBcp47Id() -> null
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
    fun createFromMacaronicLanguage(languageId: LanguageId): AndroidLocaleProfile? {
      if (!languageId.hasMacaronicId()) return null
      val (languageCode, regionCode) =
        languageId.macaronicId.combinedLanguageCode.divide("-") ?: return null
      return maybeConstructProfile(languageCode, regionCode)
    }

    /**
     * Returns a new [AndroidLocaleProfile] using the provided [languageId]'s
     * [LanguageId.getAndroidResourcesLanguageId] as the basis of the profile, or null if none can
     * be created.
     *
     * This is meant to be used in cases when an [AndroidLocaleProfile] is needed to match a
     * specific Android-compatible [Locale] (e.g. via [AndroidLocaleProfile.computeAndroidLocale])
     * that can correctly match to specific Android app strings.
     */
    fun createFromAndroidResourcesLanguageId(languageId: LanguageId): AndroidLocaleProfile? {
      val languageCode = languageId.androidResourcesLanguageId.languageCode
      val regionCode = languageId.androidResourcesLanguageId.regionCode
      return when {
        !languageId.hasAndroidResourcesLanguageId() -> null
        languageCode.isEmpty() -> null
        // Empty region codes are allowed for Android resource IDs since they should always be used
        // verbatim to ensure the correct Android resource string can be computed (such as for macro
        // languages).
        regionCode.isEmpty() -> LanguageAndWildcardRegionProfile(languageCode.asLowerCase())
        else -> {
          LanguageAndRegionProfile(
            languageCode.asLowerCase(), regionCode.asLowerCase(), regionCode.asUpperCase()
          )
        }
      }
    }

    private fun maybeConstructProfile(
      languageCode: String,
      regionCode: String,
      emptyRegionAsWildcard: Boolean = false
    ): AndroidLocaleProfile? {
      return when {
        languageCode.isEmpty() -> null
        regionCode.isNotEmpty() -> {
          LanguageAndRegionProfile(
            languageCode.asLowerCase(), regionCode.asLowerCase(), regionCode.asUpperCase()
          )
        }
        emptyRegionAsWildcard -> LanguageAndWildcardRegionProfile(languageCode.asLowerCase())
        else -> null
      }
    }

    private fun String.divide(delimiter: String): Pair<String, String>? {
      val results = split(delimiter)
      return if (results.size == 2) {
        results[0] to results[1]
      } else null
    }

    private fun String.asLowerCase() = machineLocale.run { toMachineLowerCase() }

    private fun String.asUpperCase() = machineLocale.run { toMachineUpperCase() }
  }
}
