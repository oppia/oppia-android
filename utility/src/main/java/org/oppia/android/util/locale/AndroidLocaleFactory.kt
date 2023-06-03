package org.oppia.android.util.locale

import android.os.Build
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId.LanguageTypeCase.IETF_BCP47_ID
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId.LanguageTypeCase.LANGUAGETYPE_NOT_SET
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId.LanguageTypeCase.MACARONIC_ID
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for creating new Android [Locale]s. This is meant only to be used within the locale
 * domain package.
 */
@Singleton
class AndroidLocaleFactory @Inject constructor(
  private val profileChooserSelector: ProposalChooser.Selector
) {
  private val memoizedLocales by lazy { ConcurrentHashMap<OppiaLocaleContext, Locale>() }

  /**
   * Creates a new [Locale] that matches the given [OppiaLocaleContext].
   *
   * This function uses the following prioritization algorithm when trying to create an
   * Android-compatible [Locale] (steps are executed in order from top to bottom):
   * 1. Try to find a system [Locale] that matches the primary language code & region.
   * 2. If (1) fails and the primary language is configured for Android, create a forced [Locale].
   * 3. If (2) fails, try to find a system [Locale] that matches the secondary language code/region.
   * 4. If (3) fails and the secondary language is configured for Android, create a forced [Locale].
   * 5. If (4) fails, compute a forced [Locale] for the primary language.
   *
   * Note that steps (2) and (4) are only used in cases when the provided [localeContext] has
   * [LanguageUsageMode.APP_STRINGS] usage since prioritizing Android ID matching only affects
   * resource selection (and is based on the [Locale] used).
   *
   * 'Forced locale' means an app-constructed [Locale] is used instead of one of the available
   * system [Locale]s. Using this [Locale] will have one of two effects depending on how it's used:
   * - For resource selection, Android will respect the custom [Locale] iff it includes a region
   *   code (e.g. resources in a "values-hi-rEN/" directory will be used if the activity's
   *   configured [Locale] has a language code of "hi" and region code of "en").
   * - For other locale-based operations, the forced [Locale] will behave like the system's
   *   [Locale.ROOT].
   *
   * @param localeContext the [OppiaLocaleContext] to use as a basis for finding a similar [Locale]
   * @return the best [Locale] to match the provided [localeContext]
   */
  fun createAndroidLocale(localeContext: OppiaLocaleContext): Locale {
    // Note: computeIfAbsent is used here instead of getOrPut to ensure atomicity across multiple
    // threads calling into this create function.
    return memoizedLocales.computeIfAbsent(localeContext) {
      val chooser = profileChooserSelector.findBestChooser(localeContext)
      val primaryLocaleSource = LocaleSource.createFromPrimary(localeContext)
      val fallbackLocaleSource = LocaleSource.createFromFallback(localeContext)
      val proposal = chooser.findBestProposal(primaryLocaleSource, fallbackLocaleSource)
      return@computeIfAbsent proposal.computedLocale
    }
  }

  /**
   * A proposal of a [AndroidLocaleProfile] that may potentially be used to create a [Locale]. See
   * [isViable].
   */
  sealed class LocaleProfileProposal {
    /** The [AndroidLocaleProfile] being considered in this proposal. */
    protected abstract val profile: AndroidLocaleProfile

    /**
     * A computed [Locale] that most closely represents the [AndroidLocaleProfile] of this proposal.
     */
    val computedLocale: Locale
      get() = Locale(profile.languageCode, profile.getNonWildcardRegionCode())

    /**
     * Determines whether the [AndroidLocaleProfile] of this proposal is a viable choice for using
     * to compute a [Locale] (e.g. via [computedLocale]).
     *
     * @param machineLocale the app's [OppiaLocale.MachineLocale]
     * @param systemProfiles [AndroidLocaleProfile]s representing the system's available locales
     * @return whether this proposal has a viable profile for creating a [Locale]
     */
    abstract fun isViable(
      machineLocale: OppiaLocale.MachineLocale,
      systemProfiles: List<AndroidLocaleProfile>
    ): Boolean

    /**
     * A [LocaleProfileProposal] that is only viable if its [profile] is among the available system
     * locales and its [minAndroidSdkVersion] is below, or at, the current system's SDK version.
     */
    data class SystemProposal(
      override val profile: AndroidLocaleProfile,
      val minAndroidSdkVersion: Int
    ) : LocaleProfileProposal() {
      override fun isViable(
        machineLocale: OppiaLocale.MachineLocale,
        systemProfiles: List<AndroidLocaleProfile>
      ): Boolean {
        return systemProfiles.any { it.matches(machineLocale, profile) } &&
          minAndroidSdkVersion <= Build.VERSION.SDK_INT
      }
    }

    /**
     * A [LocaleProfileProposal] that is only viable if its [minAndroidSdkVersion] is below, or at,
     * the current system's SDK version.
     *
     * This proposal ignores system locales when considering viability.
     */
    data class ForcedProposal(
      override val profile: AndroidLocaleProfile,
      val minAndroidSdkVersion: Int
    ) : LocaleProfileProposal() {
      override fun isViable(
        machineLocale: OppiaLocale.MachineLocale,
        systemProfiles: List<AndroidLocaleProfile>
      ): Boolean = minAndroidSdkVersion <= Build.VERSION.SDK_INT
    }

    private companion object {
      private fun AndroidLocaleProfile.getNonWildcardRegionCode(): String =
        regionCode.takeIf { it != AndroidLocaleProfile.REGION_WILDCARD } ?: ""
    }
  }

  /**
   * A producer of [LocaleProfileProposal]s for a given context and for various situations.
   *
   * New instances should be created using [createFromPrimary] or [createFromFallback].
   *
   * @property localeContext the broader [OppiaLocaleContext] from which to source profiles
   * @property definition the specific language definition to consider for possible profiles
   * @property languageId the specific language ID to consider for possible profiles
   */
  class LocaleSource private constructor(
    private val localeContext: OppiaLocaleContext,
    private val definition: LanguageSupportDefinition,
    private val languageId: LanguageId
  ) {
    /**
     * Returns all [LocaleProfileProposal]s which require matching against system locales for
     * viability (see [LocaleProfileProposal.SystemProposal]) for this source's configured language
     * context, or an empty list if there are none.
     */
    fun computeSystemMatchingProposals(): List<LocaleProfileProposal> {
      return listOfNotNull(
        computeLocaleProfileFromAndroidId()?.toSystemProposal(),
        createIetfProfile()?.toSystemProposal(),
        createMacaronicProfile()?.toSystemProposal()
      )
    }

    /**
     * Returns a [LocaleProfileProposal] representing a [LocaleProfileProposal.ForcedProposal]
     * specifically for this source's Android language context (e.g.
     * [LanguageSupportDefinition.AndroidLanguageId]), or null if there is no such Android ID
     * configured for this source's context.
     */
    fun computeForcedAndroidProposal(): LocaleProfileProposal? =
      computeLocaleProfileFromAndroidId()?.toForcedProposal()

    /**
     * Returns a [LocaleProfileProposal] representing a [LocaleProfileProposal.ForcedProposal] that
     * is guaranteed to match best to the language context of this source.
     *
     * Note that the returned proposal will prioritize its Android ID configuration over
     * alternatives (such as IETF BCP 47 or a macaronic language configuration).
     */
    fun computeForcedProposal(): LocaleProfileProposal =
      computeForcedAndroidProposal() ?: languageId.toForcedProposal()

    private fun computeLocaleProfileFromAndroidId(): AndroidLocaleProfile? {
      return languageId.androidResourcesLanguageId.takeIf {
        languageId.hasAndroidResourcesLanguageId() && it.languageCode.isNotEmpty()
      }?.let {
        // Empty region codes are allowed for Android resource IDs since they should always be used
        // verbatim to ensure the correct Android resource string can be computed (such as for macro
        // languages).
        AndroidLocaleProfile(
          it.languageCode,
          regionCode = it.regionCode.ifEmpty { AndroidLocaleProfile.REGION_WILDCARD }
        )
      }
    }

    private fun LanguageId.toForcedProposal(): LocaleProfileProposal {
      return when (languageId.languageTypeCase) {
        IETF_BCP47_ID -> createIetfProfile().expectedProfile()
        MACARONIC_ID -> createMacaronicProfile().expectedProfile()
        LANGUAGETYPE_NOT_SET, null -> error("Invalid language case: $languageTypeCase.")
      }.toForcedProposal()
    }

    private fun createIetfProfile(): AndroidLocaleProfile? =
      AndroidLocaleProfile.createFromIetfDefinitions(languageId, localeContext.regionDefinition)

    private fun createMacaronicProfile(): AndroidLocaleProfile? =
      AndroidLocaleProfile.createFromMacaronicLanguage(languageId)

    private fun AndroidLocaleProfile?.expectedProfile() = this ?: error("Invalid ID: $languageId.")

    private fun AndroidLocaleProfile.toSystemProposal() =
      LocaleProfileProposal.SystemProposal(profile = this, definition.minAndroidSdkVersion)

    private fun AndroidLocaleProfile.toForcedProposal() =
      LocaleProfileProposal.ForcedProposal(profile = this, definition.minAndroidSdkVersion)

    companion object {
      /**
       * Return a new [LocaleSource] that maps to [localeContext]'s primary language configuration
       * (i.e. fallback language details will be ignored).
       */
      fun createFromPrimary(localeContext: OppiaLocaleContext): LocaleSource =
        LocaleSource(localeContext, localeContext.languageDefinition, localeContext.getLanguageId())

      /**
       * Return a new [LocaleSource] that maps to [localeContext]'s fallback (secondary) language
       * configuration (i.e. primary language details will be ignored).
       */
      fun createFromFallback(localeContext: OppiaLocaleContext): LocaleSource {
        return LocaleSource(
          localeContext,
          localeContext.fallbackLanguageDefinition,
          localeContext.getFallbackLanguageId()
        )
      }
    }
  }

  // Locale is always computed based on the Android resource app string identifier if that's
  // defined. If it isn't, the routine falls back to app language & region country codes (which also
  // provides interoperability with system-derived contexts). Android-compatible IDs will result in
  // a guaranteed forced locale since it's assumed that compatibility will have the desired behavior
  // (but only for app strings). Note that if either identifier is missing for the primary language,
  // the fallback is used instead (if available), except that IETF BCP 47 tags from the primary
  // language are used before Android resource codes from the fallback. Thus, the order of this list
  // is important. Finally, a basic check is done here to make sure this version of Android can
  // actually render the target language.

  /**
   * A chooser for finding [LocaleProfileProposal]s that best matches a specific
   * [OppiaLocaleContext].
   *
   * See [findBestProposal] for details on the selection process.
   *
   * Instances of this interface can be retrieved via an application-injected [Selector].
   */
  interface ProposalChooser {
    /**
     * Finds the [LocaleProfileProposal] that *best* matches the contexts represented by the
     * provided sources.
     *
     * Note that the returned proposal is not guaranteed to produce a [Locale] that matches existing
     * system locales (and, in fact, it may not even if there are such proposals available among the
     * provided sources depending on the behavior of the implementation).
     *
     * @param primarySource the [LocaleSource] whose profiles should take priority
     * @param fallbackSource the [LocaleSource] whose profiles should only be considered if no
     *     profiles from [primarySource] are viable
     * @return the best matching [LocaleProfileProposal]
     */
    fun findBestProposal(
      primarySource: LocaleSource,
      fallbackSource: LocaleSource
    ): LocaleProfileProposal

    /** Application-level selector for [ProposalChooser]s. See [findBestChooser]. */
    class Selector @Inject constructor(
      private val localePreferred: MatchedLocalePreferredChooser,
      private val androidResourcePreferred: AndroidResourceCompatibilityPreferredChooser
    ) {
      /**
       * Returns the [ProposalChooser] that best matches the provided [localeContext].
       *
       * Generally, [MatchedLocalePreferredChooser] is used in most cases. In circumstances where
       * app strings may use the computed [Locale], [AndroidResourceCompatibilityPreferredChooser]
       * may be returned, instead.
       */
      fun findBestChooser(localeContext: OppiaLocaleContext): ProposalChooser =
        if (localeContext.usageMode == APP_STRINGS) androidResourcePreferred else localePreferred
    }
  }

  /**
   * A [ProposalChooser] that prioritizes finding [LocaleProfileProposal]s which match available
   * system locales.
   */
  class MatchedLocalePreferredChooser @Inject constructor(
    private val machineLocale: OppiaLocale.MachineLocale
  ) : ProposalChooser {
    override fun findBestProposal(
      primarySource: LocaleSource,
      fallbackSource: LocaleSource
    ): LocaleProfileProposal {
      return primarySource.computeSystemMatchingProposals().findFirstViable(machineLocale)
        ?: fallbackSource.computeSystemMatchingProposals().findFirstViable(machineLocale)
        ?: primarySource.computeForcedProposal()
    }
  }

  /**
   * A [ProposalChooser] that prioritizes finding [LocaleProfileProposal]s which match available
   * system locales first, and secondarily a [LocaleSource]'s strongly Android compatible proposal
   * (see [LocaleSource.computeForcedAndroidProposal]). Note that Android ID proposals take priority
   * over fallbacks in this chooser since it's assumed that the Android system can properly handle
   * [Locale]s produced by such profiles in order to correctly produce app UI strings.
   */
  class AndroidResourceCompatibilityPreferredChooser @Inject constructor(
    private val machineLocale: OppiaLocale.MachineLocale
  ) : ProposalChooser {
    override fun findBestProposal(
      primarySource: LocaleSource,
      fallbackSource: LocaleSource
    ): LocaleProfileProposal {
      return primarySource.computeSystemMatchingProposals().findFirstViable(machineLocale)
        ?: primarySource.computeForcedAndroidProposal()?.takeOnlyIfViable(machineLocale)
        ?: fallbackSource.computeSystemMatchingProposals().findFirstViable(machineLocale)
        ?: fallbackSource.computeForcedAndroidProposal()?.takeOnlyIfViable(machineLocale)
        ?: primarySource.computeForcedProposal()
    }
  }

  private companion object {
    private val availableLocaleProfiles by lazy {
      Locale.getAvailableLocales().map(AndroidLocaleProfile::createFrom)
    }

    private fun List<LocaleProfileProposal>.findFirstViable(
      machineLocale: OppiaLocale.MachineLocale
    ) = firstOrNull { it.isViable(machineLocale, availableLocaleProfiles) }

    private fun LocaleProfileProposal.takeOnlyIfViable(
      machineLocale: OppiaLocale.MachineLocale
    ): LocaleProfileProposal? = takeIf { isViable(machineLocale, availableLocaleProfiles) }
  }
}
