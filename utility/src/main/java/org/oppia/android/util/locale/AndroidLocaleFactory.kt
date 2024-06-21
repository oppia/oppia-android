package org.oppia.android.util.locale

import android.os.Build
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId.LanguageTypeCase.IETF_BCP47_ID
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId.LanguageTypeCase.LANGUAGETYPE_NOT_SET
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId.LanguageTypeCase.MACARONIC_ID
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
import org.oppia.android.util.threading.BlockingDispatcher
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for creating new Android [Locale]s. This is meant only to be used within the locale
 * domain package.
 */
@Singleton
class AndroidLocaleFactory @Inject constructor(
  private val profileChooserSelector: ProposalChooser.Selector,
  @BlockingDispatcher private val blockingDispatcher: CoroutineDispatcher,
  private val androidLocaleProfileFactory: AndroidLocaleProfile.Factory
) {
  private val memoizedLocales = mutableMapOf<OppiaLocaleContext, Locale>()

  /**
   * Creates and returns a new [Locale] that matches the given [OppiaLocaleContext].
   *
   * See [createAndroidLocaleAsync] for specifics. Note that this function, unlike the async
   * version, does **not** cache or try to load a pre-created [Locale] for the given context.
   * Creating new [Locale]s can be expensive, so it's always preferred to use
   * [createAndroidLocaleAsync] except in cases where that isn't an option.
   */
  fun createOneOffAndroidLocale(localeContext: OppiaLocaleContext): Locale {
    val chooser = profileChooserSelector.findBestChooser(localeContext)
    val primaryLocaleSource =
      LocaleSource.createFromPrimary(localeContext, androidLocaleProfileFactory)
    val fallbackLocaleSource =
      LocaleSource.createFromFallback(localeContext, androidLocaleProfileFactory)
    return chooser.findBestProposal(primaryLocaleSource, fallbackLocaleSource).computedLocale
  }

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
   * The returned [Locale] will never match a supported app language that is not supported on the
   * current running version of Android (from a rendering perspective).
   *
   * 'Forced locale' means an app-constructed [Locale] is used instead of one of the available
   * system [Locale]s. Using this [Locale] will have one of two effects depending on how it's used:
   * - For resource selection, Android will respect the custom [Locale] iff it includes a region
   *   code (e.g. resources in a "values-hi-rEN/" directory will be used if the activity's
   *   configured [Locale] has a language code of "hi" and region code of "en").
   * - For other locale-based operations, the forced [Locale] will behave like the system's
   *   [Locale.ROOT].
   *
   * Note that the returned [Locale] may be cached within the factory for performance reasons, so
   * the returned value uses a [Deferred] to ensure that this method can guarantee thread-safe
   * access.
   *
   * @param localeContext the [OppiaLocaleContext] to use as a basis for finding a similar [Locale]
   * @return the best [Locale] to match the provided [localeContext]
   */
  fun createAndroidLocaleAsync(localeContext: OppiaLocaleContext): Deferred<Locale> {
    // A blocking dispatcher is used to ensure thread safety when updating the locales map.
    return CoroutineScope(blockingDispatcher).async {
      memoizedLocales.getOrPut(localeContext) { createOneOffAndroidLocale(localeContext) }
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
    val computedLocale: Locale by lazy { profile.computeAndroidLocale() }

    /**
     * Determines whether the [AndroidLocaleProfile] of this proposal is a viable choice for using
     * to compute a [Locale] (e.g. via [computedLocale]).
     *
     * @param localeProfileRepository the [LocaleProfileRepository]s representing the system's
     *     available locales
     * @return whether this proposal has a viable profile for creating a [Locale]
     */
    abstract fun isViable(localeProfileRepository: LocaleProfileRepository): Boolean

    /**
     * A [LocaleProfileProposal] that is only viable if its [profile] is among the available system
     * locales and its [minAndroidSdkVersion] is below, or at, the current system's SDK version.
     */
    data class SystemProposal(
      override val profile: AndroidLocaleProfile,
      val minAndroidSdkVersion: Int
    ) : LocaleProfileProposal() {
      override fun isViable(
        localeProfileRepository: LocaleProfileRepository
      ): Boolean {
        return localeProfileRepository.availableLocaleProfiles.any { it.matches(profile) } &&
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
      override fun isViable(localeProfileRepository: LocaleProfileRepository): Boolean =
        minAndroidSdkVersion <= Build.VERSION.SDK_INT
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
    private val languageId: LanguageId,
    private val androidLocaleProfileFactory: AndroidLocaleProfile.Factory
  ) {
    private val regionDefinition by lazy {
      localeContext.regionDefinition.takeIf { localeContext.hasRegionDefinition() }
    }

    /**
     * Returns all [LocaleProfileProposal]s which require matching against system locales for
     * viability (see [LocaleProfileProposal.SystemProposal]) for this source's configured language
     * context, or an empty list if there are none.
     */
    fun computeSystemMatchingProposals(): List<LocaleProfileProposal> {
      return listOfNotNull(
        createAndroidResourcesProfile()?.toSystemProposal(),
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
      createAndroidResourcesProfile()?.toForcedProposal()

    /**
     * Returns a [LocaleProfileProposal] representing a [LocaleProfileProposal.ForcedProposal] that
     * is guaranteed to match best to the language context of this source.
     *
     * Note that the returned proposal will prioritize its Android ID configuration over
     * alternatives (such as IETF BCP 47 or a macaronic language configuration).
     *
     * @param fallBackToRootProfile whether to return a [AndroidLocaleProfile.RootProfile] for cases
     *     when a valid proposal cannot be determined rather than throwing an exception
     */
    fun computeForcedProposal(fallBackToRootProfile: Boolean): LocaleProfileProposal =
      computeForcedAndroidProposal() ?: toForcedProposal(fallBackToRootProfile)

    private fun toForcedProposal(fallBackToRootProfile: Boolean): LocaleProfileProposal {
      return when (val languageTypeCase = languageId.languageTypeCase) {
        IETF_BCP47_ID -> createIetfProfile().expectedProfile()
        MACARONIC_ID -> createMacaronicProfile().expectedProfile()
        LANGUAGETYPE_NOT_SET, null -> {
          if (fallBackToRootProfile) {
            AndroidLocaleProfile.RootProfile
          } else error("Invalid language case: $languageTypeCase.")
        }
      }.toForcedProposal()
    }

    private fun createIetfProfile(): AndroidLocaleProfile? =
      androidLocaleProfileFactory.createFromIetfDefinitions(languageId, regionDefinition)

    private fun createMacaronicProfile(): AndroidLocaleProfile? =
      androidLocaleProfileFactory.createFromMacaronicLanguage(languageId)

    private fun createAndroidResourcesProfile(): AndroidLocaleProfile? =
      androidLocaleProfileFactory.createFromAndroidResourcesLanguageId(languageId)

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
      fun createFromPrimary(
        localeContext: OppiaLocaleContext,
        androidLocaleProfileFactory: AndroidLocaleProfile.Factory
      ): LocaleSource {
        return LocaleSource(
          localeContext,
          localeContext.languageDefinition,
          localeContext.getLanguageId(),
          androidLocaleProfileFactory
        )
      }

      /**
       * Return a new [LocaleSource] that maps to [localeContext]'s fallback (secondary) language
       * configuration (i.e. primary language details will be ignored).
       */
      fun createFromFallback(
        localeContext: OppiaLocaleContext,
        androidLocaleProfileFactory: AndroidLocaleProfile.Factory
      ): LocaleSource {
        return LocaleSource(
          localeContext,
          localeContext.fallbackLanguageDefinition,
          localeContext.getFallbackLanguageId(),
          androidLocaleProfileFactory
        )
      }
    }
  }

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
    private val localeProfileRepository: LocaleProfileRepository
  ) : ProposalChooser {
    override fun findBestProposal(
      primarySource: LocaleSource,
      fallbackSource: LocaleSource
    ): LocaleProfileProposal {
      return primarySource.computeSystemMatchingProposals().findFirstViable(localeProfileRepository)
        ?: fallbackSource.computeSystemMatchingProposals().findFirstViable(localeProfileRepository)
        ?: primarySource.computeForcedProposal(fallBackToRootProfile = false)
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
    private val localeProfileRepository: LocaleProfileRepository
  ) : ProposalChooser {
    override fun findBestProposal(
      primarySource: LocaleSource,
      fallbackSource: LocaleSource
    ): LocaleProfileProposal {
      // Note that defaulting to the root locale only makes sense for app strings (since app strings
      // are picked based on the configured system locale).
      return primarySource.computeSystemMatchingProposals().findFirstViable(localeProfileRepository)
        ?: primarySource.computeForcedAndroidProposal()?.takeOnlyIfViable(localeProfileRepository)
        ?: fallbackSource.computeSystemMatchingProposals().findFirstViable(localeProfileRepository)
        ?: fallbackSource.computeForcedAndroidProposal()?.takeOnlyIfViable(localeProfileRepository)
        ?: primarySource.computeForcedProposal(fallBackToRootProfile = true)
    }
  }

  /**
   * An application-injectable repository storing all possible [AndroidLocaleProfile]s available to
   * use on the local system for the lifetime of the current app instance.
   */
  @Singleton
  class LocaleProfileRepository @Inject constructor(
    private val androidLocaleProfileFactory: AndroidLocaleProfile.Factory
  ) {
    /**
     * All available [AndroidLocaleProfile]s that represent locales on the current running system.
     */
    val availableLocaleProfiles: List<AndroidLocaleProfile> by lazy {
      Locale.getAvailableLocales().map { androidLocaleProfileFactory.createFrom(it) }
    }
  }

  private companion object {
    private fun List<LocaleProfileProposal>.findFirstViable(repository: LocaleProfileRepository) =
      firstOrNull { it.isViable(repository) }

    private fun LocaleProfileProposal.takeOnlyIfViable(repository: LocaleProfileRepository) =
      takeIf { isViable(repository) }
  }
}
