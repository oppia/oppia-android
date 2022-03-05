package org.oppia.android.domain.locale

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.UNRECOGNIZED
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.USAGE_MODE_UNSPECIFIED
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.app.model.SupportedLanguages
import org.oppia.android.app.model.SupportedRegions
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.locale.AndroidLocaleProfile
import org.oppia.android.util.locale.OppiaBidiFormatter
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.locale.OppiaLocale.ContentLocale
import org.oppia.android.util.locale.OppiaLocale.DisplayLocale
import org.oppia.android.util.locale.OppiaLocale.MachineLocale
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

private const val ANDROID_SYSTEM_LOCALE_DATA_PROVIDER_ID = "android_locale"
private const val APP_STRING_LOCALE_DATA_BASE_PROVIDER_ID = "app_string_locale"
private const val WRITTEN_TRANSLATION_LOCALE_BASE_DATA_PROVIDER_ID = "written_translation_locale"
private const val AUDIO_TRANSLATIONS_LOCALE_BASE_DATA_PROVIDER_ID = "audio_translations_locale"
private const val SYSTEM_LANGUAGE_DATA_PROVIDER_ID = "system_language"

/**
 * Represents the Oppia language code corresponding to the default language to use for written
 * translations if the user-selected language is unavailable. Note that this is intentionally an
 * invalid language code to force translation selection to fall back to the built-in strings (which
 * is a guaranteed fallback for written translations).
 */
private const val DEFAULT_WRITTEN_TRANSLATION_LANGUAGE_CODE = "builtin"

/** Controller for creating & retrieving user-specified [OppiaLocale]s. */
@Singleton
class LocaleController @Inject constructor(
  private val applicationContext: Context,
  private val dataProviders: DataProviders,
  private val languageConfigRetriever: LanguageConfigRetriever,
  private val oppiaLogger: OppiaLogger,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  private val machineLocale: MachineLocale,
  private val androidLocaleFactory: AndroidLocaleFactory,
  private val formatterFactory: OppiaBidiFormatter.Factory
) {
  private val definitionsLock = ReentrantLock()
  private lateinit var supportedLanguages: SupportedLanguages
  private lateinit var supportedRegions: SupportedRegions

  /**
   * Returns the [OppiaLocaleContext] which provides a reasonable default context when building a
   * locale for app strings. Generally this should be used in conjunction with
   * [reconstituteDisplayLocale], and only during locale bootstrapping. App layer classes should
   * rely on a consistent locale fetched via one of the data providers below rather than creating an
   * immediate locale using these methods.
   *
   * Note that the returned locale cannot be assumed to follow any particular locale, only that it's
   * likely to correspond to a valid Android locale when setting up an activity's configuration for
   * string resource selection and layout arrangement.
   */
  fun getLikelyDefaultAppStringLocaleContext(): OppiaLocaleContext {
    return OppiaLocaleContext.newBuilder().apply {
      // Assume English for the default language since it has the highest chance of being
      // successful. Note that this theoretically could differ from the language definitions
      // since it's hardcoded, but that should be fine. Also, only assume app language
      // support.
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        language = OppiaLanguage.ENGLISH
        minAndroidSdkVersion = 1
        appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
          ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
            ietfLanguageTag = "en"
          }.build()
        }.build()
      }.build()
      regionDefinition = RegionSupportDefinition.newBuilder().apply {
        region = OppiaRegion.UNITED_STATES
        regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
          ietfRegionTag = "US"
        }.build()
        addLanguages(OppiaLanguage.ENGLISH)
      }.build()
      usageMode = APP_STRINGS
    }.build()
  }

  /**
   * Returns a new [DisplayLocale] corresponding to the specified [OppiaLocaleContext]. This is
   * meant to be used in cases when a context needs to be saved (e.g. in a bundle) and later
   * restored without needing to synchronously re-fetch the locale via a data provider.
   *
   * [DisplayLocale] implementations guarantee functional equivalence between different instances so
   * long as they have the same context, so the fetch is only needed for eventual consistency (i.e.
   * for cases in which the user changed their selected language).
   */
  fun reconstituteDisplayLocale(oppiaLocaleContext: OppiaLocaleContext): DisplayLocale {
    return DisplayLocaleImpl(
      oppiaLocaleContext, machineLocale, androidLocaleFactory, formatterFactory
    )
  }

  /**
   * Notifies that the default Android locale may have changed.
   *
   * Since there's no way to reliably get notified when the default system locale is changed, it's
   * the expected responsibility of calling infrastructure to ensure detected changes to the system
   * locale (such as per an activity configuration change) result in downstream data providers being
   * recomputed in case they rely on the system locale & the locale has indeed changed. See
   * [retrieveSystemLanguage] & other provider methods in this class.
   *
   * This generally shouldn't be called broadly.
   */
  fun notifyPotentialLocaleChange() {
    // There may not actually be a change in the default locale, but assume there is & trigger an
    // update for data providers.
    asyncDataSubscriptionManager.notifyChangeAsync(ANDROID_SYSTEM_LOCALE_DATA_PROVIDER_ID)
  }

  /**
   * Returns the [DisplayLocale] corresponding to the specified [OppiaLanguage], or potentially a
   * best-match locale that should be compatible with both the supported languages of the app and
   * the system (though this isn't guaranteed). Cases in which no match can be determined will
   * result in a failed result.
   *
   * Note that the returned [DataProvider] may be dependent on the system locale in which case its
   * subscribers may be notified whenever callers notify this controller of potential system locale
   * changes (i.e. via [notifyPotentialLocaleChange].
   *
   * The returned [OppiaLocale] will have an [OppiaLocaleContext] tied to app strings. Further, no
   * assumptions can be made about the region information within [OppiaLocaleContext]. This
   * controller makes little attempt to actually determine the region the user is in, and oftentimes
   * will default to an unspecified region. This is largely because the main signal the app has for
   * determining the user's region is the system locale (which is configurable by the user and not
   * at all dependent on their geolocation). Other measures could be taken to try and get the user's
   * location (such as using the telephony services), but these are limited (require cellular
   * functionality & connectivity) and generally aren't needed for any decision making in the app
   * (language is sufficient). Finally, it's possible for the region to change based on selecting
   * different languages since the app may actually force the system locale into a specific region
   * for properly app string localization (such as Brazil for Brazilian Portuguese).
   */
  fun retrieveAppStringDisplayLocale(language: OppiaLanguage): DataProvider<DisplayLocale> {
    val providerId = "$APP_STRING_LOCALE_DATA_BASE_PROVIDER_ID.${language.name}"
    return getSystemLocaleProfile().transformAsync(providerId) { systemLocaleProfile ->
      computeLocaleResult(language, systemLocaleProfile, APP_STRINGS)
    }
  }

  /**
   * Returns the [ContentLocale] corresponding to the specified [OppiaLanguage] (which is
   * guaranteed to be supported by the app) to be used for written translations, or a failure if for
   * some reason that's not possible (such as if the loaded language configuration doesn't include
   * the specified language).
   *
   * The returned [DataProvider] has the same notification caveat as
   * [retrieveAppStringDisplayLocale].
   *
   * The returned [OppiaLocale] will have an [OppiaLocaleContext] tied to content strings. The
   * returned locale has the same region caveats as [retrieveAppStringDisplayLocale].
   */
  fun retrieveWrittenTranslationsLocale(language: OppiaLanguage): DataProvider<ContentLocale> {
    val providerId = "$WRITTEN_TRANSLATION_LOCALE_BASE_DATA_PROVIDER_ID.${language.name}"
    return getSystemLocaleProfile().transformAsync(providerId) { systemLocaleProfile ->
      computeLocaleResult(language, systemLocaleProfile, CONTENT_STRINGS)
    }
  }

  /**
   * Returns the [ContentLocale] corresponding to the specified [OppiaLanguage] for audio
   * translations with the same failure stipulation as [retrieveWrittenTranslationsLocale].
   *
   * The returned [DataProvider] has the same notification caveat as
   * [retrieveAppStringDisplayLocale].
   *
   * The returned [OppiaLocale] will have an [OppiaLocaleContext] tied to audio translations. The
   * returned locale has the same region caveats as [retrieveAppStringDisplayLocale].
   */
  fun retrieveAudioTranslationsLocale(language: OppiaLanguage): DataProvider<ContentLocale> {
    val providerId = "$AUDIO_TRANSLATIONS_LOCALE_BASE_DATA_PROVIDER_ID.${language.name}"
    return getSystemLocaleProfile().transformAsync(providerId) { systemLocaleProfile ->
      computeLocaleResult(language, systemLocaleProfile, AUDIO_TRANSLATIONS)
    }
  }

  /**
   * Returns the [OppiaLanguage] best matching the current system locale (based on the list of
   * supported languages by the app), or [OppiaLanguage.LANGUAGE_UNSPECIFIED] if the current system
   * locale does not correspond to a supported language.
   *
   * Note that the system locale is only ever matched against app language definitions, never
   * written or audio content translations.
   *
   * The returned [DataProvider]'s subscribers may be notified upon calls to
   * [notifyPotentialLocaleChange] if there's actually a change in the system locale, though note
   * that this data provider aims to always represent the actual current system locale's language.
   */
  fun retrieveSystemLanguage(): DataProvider<OppiaLanguage> {
    val providerId = SYSTEM_LANGUAGE_DATA_PROVIDER_ID
    return getSystemLocaleProfile().transformAsync(providerId) { systemLocaleProfile ->
      AsyncResult.Success(retrieveLanguageDefinitionFromSystemCode(systemLocaleProfile)?.language
        ?: OppiaLanguage.LANGUAGE_UNSPECIFIED
      )
    }
  }

  /**
   * Updates both the system and the specified [Configuration] to use the [DisplayLocale] as the
   * default locale for the current app process (which will affect string resource retrieval).
   *
   * Note that this may result in data providers returned by this class being notified of changes if
   * any depend on the current system locale, but will likely not change the result of the data
   * provider returned by [retrieveSystemLanguage] unless the system locale has been updated prior
   * to this method being called (since it triggers a notification for potential changes on that
   * data provider).
   */
  fun setAsDefault(displayLocale: DisplayLocale, configuration: Configuration) {
    (displayLocale as? DisplayLocaleImpl)?.let { locale ->
      locale.setAsDefault(configuration)

      // Note that this seemingly causes an infinite loop since notification happens in response to
      // the upstream locale data provider changing, but it should terminate since the
      // DataProvider->LiveData bridge only notifies if data changes (i.e. if the locale is actually
      // new). Note also that this controller intentionally doesn't cache the system locale since
      // there's no way to actually observe changes to it, so the controller aims to have eventual
      // consistency by always retrieving the latest state when requested. This does mean locale
      // changes can be missed if they aren't accompanied by a configuration change or activity
      // recreation. Note that the app intentionally does not overwrite the application context's
      // locale. Besides the fact that this seems unnecessary, it also makes it difficult to track
      // the actual current system locale (which is necessary in order to determine when to recreate
      // the app to apply a new language configuration).
      Locale.setDefault(locale.formattingLocale)

      notifyPotentialLocaleChange()
    } ?: error("Invalid display locale type passed in: $displayLocale")
  }

  private fun getSystemLocaleProfile(): DataProvider<AndroidLocaleProfile> {
    return dataProviders.createInMemoryDataProvider(ANDROID_SYSTEM_LOCALE_DATA_PROVIDER_ID) {
      AndroidLocaleProfile.createFrom(getSystemLocale())
    }
  }

  /**
   * Returns the current system [Locale], as specified by the user or system. Note that this
   * generally prefers pulling from the application context since the app overwrites the static
   * singleton Locale for the app.
   */
  @SuppressLint("ObsoleteSdkInt") // Incorrect warning since the app has a lower min sdk.
  private fun getSystemLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      getDefaultLocaleApi24(applicationContext.resources.configuration)
    } else getDefaultLocale(applicationContext.resources.configuration)
  }

  @TargetApi(Build.VERSION_CODES.N)
  private fun getDefaultLocaleApi24(configuration: Configuration): Locale {
    val locales = configuration.locales
    // Note that if this ever defaults to Locale.getDefault() it will break language switching when
    // the user indicates that the app should use the system language (without restarting the app).
    // Also, this only matches against the first locale. In the future, some effort could be made to
    // try and pick the best matching system locale (per the user's preferences) rather than the
    // "first or nothing" currently implemented here.
    return if (locales.isEmpty) {
      oppiaLogger.e(
        "LocaleController",
        "No locales defined for application context. Defaulting to default Locale."
      )
      Locale.getDefault()
    } else locales[0]
  }

  @Suppress("DEPRECATION") // Old API is needed for SDK versions < N.
  private fun getDefaultLocale(configuration: Configuration): Locale = configuration.locale

  private suspend fun <T : OppiaLocale> computeLocaleResult(
    language: OppiaLanguage,
    systemLocaleProfile: AndroidLocaleProfile,
    usageMode: LanguageUsageMode
  ): AsyncResult<T> {
    // The safe-cast here is meant to ensure a strongly typed public API with robustness against
    // internal weirdness that would lead to a wrong type being produced from the generic helpers.
    // This shouldn't actually ever happen in practice, but this code gracefully fails to a null
    // (and thus a failure).
    @Suppress("UNCHECKED_CAST") // as? should always be a safe cast, even if unchecked.
    val locale = computeLocale(language, systemLocaleProfile, usageMode) as? T
    return locale?.let {
      AsyncResult.Success(it)
    } ?: AsyncResult.Failure(IllegalStateException(
      "Language $language for usage $usageMode doesn't match supported language definitions"
    )
    )
  }

  private suspend fun computeLocale(
    language: OppiaLanguage,
    systemLocaleProfile: AndroidLocaleProfile,
    usageMode: LanguageUsageMode
  ): OppiaLocale? {
    val localeContext = OppiaLocaleContext.newBuilder().apply {
      languageDefinition =
        computeLanguageDefinition(language, systemLocaleProfile, usageMode) ?: return null
      retrieveLanguageDefinition(languageDefinition.fallbackMacroLanguage)?.let {
        fallbackLanguageDefinition = it
      }
      regionDefinition = retrieveRegionDefinition(systemLocaleProfile.regionCode)
      this.usageMode = usageMode
    }.build()

    // Check whether the selected language is actually expected for the user's region (it might not
    // be, but the app should generally still behave correctly).
    val selectedLanguage = localeContext.languageDefinition.language
    val matchedRegion = localeContext.regionDefinition
    if (selectedLanguage !in matchedRegion.languagesList) {
      oppiaLogger.w(
        "LocaleController",
        "Notice: selected language $selectedLanguage is not part of the corresponding region" +
          " matched to this locale: ${matchedRegion.region} (ID:" +
          " ${matchedRegion.regionId.ietfRegionTag}) (supported languages:" +
          " ${matchedRegion.languagesList}"
      )
    }

    return when (usageMode) {
      APP_STRINGS ->
        DisplayLocaleImpl(localeContext, machineLocale, androidLocaleFactory, formatterFactory)
      CONTENT_STRINGS, AUDIO_TRANSLATIONS -> ContentLocaleImpl(localeContext)
      USAGE_MODE_UNSPECIFIED, UNRECOGNIZED -> null
    }
  }

  private suspend fun computeLanguageDefinition(
    language: OppiaLanguage,
    systemLocaleProfile: AndroidLocaleProfile,
    usageMode: LanguageUsageMode
  ): LanguageSupportDefinition? {
    // Content strings & audio translations only perform step 1 since there's no reasonable
    // fallback.
    val matchedDefinition = retrieveLanguageDefinition(language)
    return when (usageMode) {
      APP_STRINGS -> {
        // Matching behaves as follows:
        // 1. Try to find a matching definition directly for the language.
        // 2. If that fails, try falling back to the current system language.
        // 3. If that fails, create a basic definition to represent the system language.
        matchedDefinition
          ?: retrieveLanguageDefinitionFromSystemCode(systemLocaleProfile)
          ?: computeDefaultLanguageDefinitionForSystemLanguage(systemLocaleProfile)
      }
      // Content strings can always fall back to default built-in content strings.
      CONTENT_STRINGS -> matchedDefinition ?: computeDefaultLanguageDefinitionForContentStrings()
      // Audio translations have no possible fallback since the corresponding audio subtitles aren't
      // guaranteed to exist.
      AUDIO_TRANSLATIONS -> matchedDefinition
      USAGE_MODE_UNSPECIFIED, UNRECOGNIZED -> null
    }
  }

  /**
   * Returns the [LanguageSupportDefinition] corresponding to the specified language, if it exists.
   * In general, a definition should always exist unless the language is unspecified.
   */
  private suspend fun retrieveLanguageDefinition(
    language: OppiaLanguage
  ): LanguageSupportDefinition? {
    val definitions = retrieveAllLanguageDefinitions()
    return definitions.languageDefinitionsList.find {
      it.language == language
    }.also {
      if (it == null) {
        oppiaLogger.w("LocaleController", "Encountered unmatched language: $language")
      }
    }
  }

  /**
   * Returns the [LanguageSupportDefinition] corresponding to the specified locale profile, or null
   * if none match.
   *
   * This only matches against app string IDs since content & audio translations never fall back to
   * system languages.
   */
  private suspend fun retrieveLanguageDefinitionFromSystemCode(
    localeProfile: AndroidLocaleProfile
  ): LanguageSupportDefinition? {
    val definitions = retrieveAllLanguageDefinitions()
    // Attempt to find a matching definition. Note that while Locale's language code is expected to
    // be an ISO 639-1/2/3 code, it not necessarily match the IETF BCP 47 tag defined for this
    // language. If a language is unknown, return a definition that attempts to be interoperable
    // with Android.
    return definitions.languageDefinitionsList.mapNotNull { definition ->
      return@mapNotNull definition.retrieveAppLanguageProfile()?.let { profile ->
        profile to definition
      }
    }.find { (profile, _) ->
      localeProfile.matches(machineLocale, profile)
    }?.let { (_, definition) -> definition }
  }

  private suspend fun retrieveRegionDefinition(countryCode: String): RegionSupportDefinition {
    val definitions = retrieveAllRegionDefinitions()
    // Attempt to find a matching definition. Note that while Locale's country code can either be
    // an ISO 3166 alpha-2 or UN M.49 numeric-3 code, that may not necessarily match the IETF BCP
    // 47 tag defined for this region. If a region doesn't match, return unknown & just use the
    // country code directly for the formatting locale.
    return definitions.regionDefinitionsList.find {
      machineLocale.run {
        it.regionId.ietfRegionTag.equalsIgnoreCase(countryCode)
      }
    } ?: RegionSupportDefinition.newBuilder().apply {
      region = OppiaRegion.REGION_UNSPECIFIED
      regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
        ietfRegionTag = countryCode
      }.build()
    }.build()
  }

  @Suppress("RedundantSuspendModifier") // Keep to force calls to background threads.
  private suspend fun retrieveAllLanguageDefinitions() = definitionsLock.withLock {
    if (!::supportedLanguages.isInitialized) {
      supportedLanguages = languageConfigRetriever.loadSupportedLanguages()
    }
    return@withLock supportedLanguages
  }

  @Suppress("RedundantSuspendModifier") // Keep to force calls to background threads.
  private suspend fun retrieveAllRegionDefinitions() = definitionsLock.withLock {
    if (!::supportedRegions.isInitialized) {
      supportedRegions = languageConfigRetriever.loadSupportedRegions()
    }
    return@withLock supportedRegions
  }

  private fun LanguageSupportDefinition.retrieveAppLanguageProfile(): AndroidLocaleProfile? {
    return when (appStringId.languageTypeCase) {
      LanguageSupportDefinition.LanguageId.LanguageTypeCase.IETF_BCP47_ID ->
        AndroidLocaleProfile.createFromIetfDefinitions(appStringId, regionDefinition = null)
      LanguageSupportDefinition.LanguageId.LanguageTypeCase.MACARONIC_ID -> {
        // Likely won't match against system languages.
        AndroidLocaleProfile.createFromMacaronicLanguage(appStringId)
      }
      LanguageSupportDefinition.LanguageId.LanguageTypeCase.LANGUAGETYPE_NOT_SET, null -> null
    }
  }

  private fun computeDefaultLanguageDefinitionForSystemLanguage(
    systemLocaleProfile: AndroidLocaleProfile
  ) = LanguageSupportDefinition.newBuilder().apply {
    language = OppiaLanguage.LANGUAGE_UNSPECIFIED
    minAndroidSdkVersion = 1 // Assume it's supported on the current version.
    // Only app strings can be supported since this is a system language. Content & audio languages
    // must be part of the language definitions. Support for app strings is exposed so that a locale
    // can be constructed from it.
    appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
      ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = systemLocaleProfile.computeIetfLanguageTag()
      }.build()
    }.build()
  }.build()

  private fun computeDefaultLanguageDefinitionForContentStrings(): LanguageSupportDefinition {
    oppiaLogger.w(
      "LocaleController",
      "Falling back to the built-in content type due to mismatched configuration"
    )
    return LanguageSupportDefinition.newBuilder().apply {
      language = OppiaLanguage.LANGUAGE_UNSPECIFIED
      minAndroidSdkVersion = 1 // Assume it's supported on the current version.
      contentStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
        ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
          ietfLanguageTag = DEFAULT_WRITTEN_TRANSLATION_LANGUAGE_CODE
        }.build()
      }.build()
    }.build()
  }
}
