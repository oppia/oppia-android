package org.oppia.android.domain.locale

import android.content.Context
import android.content.res.Configuration
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.app.model.SupportedLanguages
import org.oppia.android.app.model.SupportedRegions
import org.oppia.android.util.data.DataProvider
import java.util.Locale
import org.oppia.android.domain.oppialogger.OppiaLogger
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.UNRECOGNIZED
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.USAGE_MODE_UNSPECIFIED
import org.oppia.android.domain.locale.OppiaLocale.ContentLocale
import org.oppia.android.domain.locale.OppiaLocale.DisplayLocale
import org.oppia.android.domain.locale.OppiaLocale.MachineLocale
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.system.OppiaClock

// TODO: document how notifications work (everything is rooted from changing Locale).
private const val ANDROID_SYSTEM_LOCALE_DATA_PROVIDER_ID = "android_locale"
private const val APP_STRING_LOCALE_DATA_BASE_PROVIDER_ID = "app_string_locale."
private const val WRITTEN_TRANSLATION_LOCALE_BASE_DATA_PROVIDER_ID = "written_translation_locale."
private const val AUDIO_TRANSLATIONS_LOCALE_BASE_DATA_PROVIDER_ID = "audio_translations_locale."
private const val SYSTEM_LANGUAGE_DATA_PROVIDER_ID = "system_language"

@Singleton
class LocaleController @Inject constructor(
  private val applicationContext: Context,
  private val dataProviders: DataProviders,
  private val languageConfigRetriever: LanguageConfigRetriever,
  private val oppiaLogger: OppiaLogger,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  private val oppiaClock: OppiaClock
) {
  private val definitionsLock = ReentrantLock()
  private lateinit var supportedLanguages: SupportedLanguages
  private lateinit var supportedRegions: SupportedRegions

  private val machineLocaleImpl: MachineLocale by lazy { MachineLocaleImpl(oppiaClock) }

  // TODO: explain what this is & how/when to use it.
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

  // TODO: this should also work in cases when the process dies.
  fun reconstituteDisplayLocale(oppiaLocaleContext: OppiaLocaleContext): DisplayLocale {
    return DisplayLocaleImpl(oppiaClock, oppiaLocaleContext, machineLocaleImpl)
  }

  // TODO: document
  fun notifyPotentialLocaleChange() {
    // There may not actually be a change in the default locale, but assume there is & trigger an
    // update for data providers.
    asyncDataSubscriptionManager.notifyChangeAsync(ANDROID_SYSTEM_LOCALE_DATA_PROVIDER_ID)
  }

  // TODO: document that retrieving country is a fixed thing. Explain usage mode.
  fun retrieveAppStringDisplayLocale(language: OppiaLanguage): DataProvider<DisplayLocale> {
    val providerId = "$APP_STRING_LOCALE_DATA_BASE_PROVIDER_ID.${language.name}"
    return getSystemLocaleProfile().transformAsync(providerId) { systemLocaleProfile ->
      computeLocaleResult(language, systemLocaleProfile, APP_STRINGS)
    }
  }

  fun retrieveWrittenTranslationsLocale(language: OppiaLanguage): DataProvider<ContentLocale> {
    val providerId = "$WRITTEN_TRANSLATION_LOCALE_BASE_DATA_PROVIDER_ID.${language.name}"
    return getSystemLocaleProfile().transformAsync(providerId) { systemLocaleProfile ->
      computeLocaleResult(language, systemLocaleProfile, CONTENT_STRINGS)
    }
  }

  fun retrieveAudioTranslationsLocale(language: OppiaLanguage): DataProvider<ContentLocale> {
    val providerId = "$AUDIO_TRANSLATIONS_LOCALE_BASE_DATA_PROVIDER_ID.${language.name}"
    return getSystemLocaleProfile().transformAsync(providerId) { systemLocaleProfile ->
      computeLocaleResult(language, systemLocaleProfile, AUDIO_TRANSLATIONS)
    }
  }

  fun getMachineLocale(): MachineLocale = machineLocaleImpl

  // TODO: document only matches to app language definitions.
  fun retrieveSystemLanguage(): DataProvider<OppiaLanguage> {
    val providerId = SYSTEM_LANGUAGE_DATA_PROVIDER_ID
    return getSystemLocaleProfile().transformAsync(providerId) { systemLocaleProfile ->
      // TODO: fix failover
      AsyncResult.success(
        retrieveLanguageDefinitionFromSystemCode(systemLocaleProfile.languageCode)?.language
          ?: OppiaLanguage.LANGUAGE_UNSPECIFIED
      )
    }
  }


  // TODO: document that this can't be called due to Locale being prohibited broadly in the
  //  codebase. Might be nice to find a more private signal mechanism.
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
      // recreation.
      // TODO: add regex prohibiting this
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
  private fun getSystemLocale(): Locale {
    val locales = applicationContext.resources.configuration.locales
    // Note that if this ever defaults to Locale.getDefault() it will break language switching when
    // the user indicates that the app should use the system language (without restarting the app).
    // Also, this only matches against the first locale. In the future, some effort could be made to
    // try and pick the best matching system locale (per the user's preferences) rather than the
    // "first or nothing" currently implemented here.
    return if (!locales.isEmpty) {
      oppiaLogger.e(
        "LocaleController",
        "No locales defined for application context. Defaulting to default Locale."
      )
      locales[0]
    } else Locale.getDefault()
  }

  private suspend fun <T : OppiaLocale> computeLocaleResult(
    language: OppiaLanguage, systemLocaleProfile: AndroidLocaleProfile, usageMode: LanguageUsageMode
  ): AsyncResult<T> {
    // The safe-cast here is meant to ensure a strongly typed public API with robustness against
    // internal weirdness that would lead to a wrong type being produced from the generic helpers.
    // This shouldn't actually ever happen in practice, but this code gracefully fails to a null
    // (and thus a failure).
    @Suppress("UNCHECKED_CAST") // as? should always be a safe cast, even if unchecked.
    val locale = computeLocale(language, systemLocaleProfile, usageMode) as? T
    return locale?.let {
      AsyncResult.success(it)
    } ?: AsyncResult.failed(
      IllegalStateException(
        "Language $language for usage $usageMode doesn't match supported language definitions"
      )
    )
  }

  private suspend fun computeLocale(
    language: OppiaLanguage, systemLocaleProfile: AndroidLocaleProfile, usageMode: LanguageUsageMode
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
      APP_STRINGS -> DisplayLocaleImpl(oppiaClock, localeContext, machineLocaleImpl)
      CONTENT_STRINGS, AUDIO_TRANSLATIONS -> ContentLocale(localeContext)
      USAGE_MODE_UNSPECIFIED, UNRECOGNIZED -> null
    }
  }

  private suspend fun computeLanguageDefinition(
    language: OppiaLanguage,
    systemLocaleProfile: AndroidLocaleProfile,
    usageMode: LanguageUsageMode
  ): LanguageSupportDefinition? {
    // Matching behaves as follows (for app strings):
    // 1. Try to find a matching definition directly for the language.
    // 2. If that fails, try falling back to the current system language.
    // 3. If that fails, create a basic definition to represent the system language.
    // Content strings & audio translations only perform step 1 since there's no reasonable
    // fallback.
    val currentSystemLanguageCode by lazy { systemLocaleProfile.languageCode }
    val matchedDefinition = retrieveLanguageDefinition(language)
    return if (usageMode == APP_STRINGS) {
      matchedDefinition
        ?: retrieveLanguageDefinitionFromSystemCode(currentSystemLanguageCode)
        ?: computeDefaultLanguageDefinitionForSystemLanguage(currentSystemLanguageCode)
    } else matchedDefinition
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
   * Returns the [LanguageSupportDefinition] corresponding to the specified language code, or null
   * if none match.
   *
   * This only matches against app string IDs since content & audio translations never fall back to
   * system languages.
   */
  private suspend fun retrieveLanguageDefinitionFromSystemCode(
    languageCode: String
  ): LanguageSupportDefinition? {
    val definitions = retrieveAllLanguageDefinitions()
    // Attempt to find a matching definition. Note that while Locale's language code is expected to
    // be an ISO 639-1/2/3 code, it not necessarily match the IETF BCP 47 tag defined for this
    // language. If a language is unknown, return a definition that attempts to be interoperable
    // with Android.
    return definitions.languageDefinitionsList.find {
      machineLocaleImpl.run {
        languageCode.equalsIgnoreCase(it.retrieveAppLanguageCode())
      }
    }
  }

  private suspend fun retrieveRegionDefinition(countryCode: String): RegionSupportDefinition {
    val definitions = retrieveAllRegionDefinitions()
    // Attempt to find a matching definition. Note that while Locale's country code can either be
    // an ISO 3166 alpha-2 or UN M.49 numeric-3 code, that may not necessarily match the IETF BCP
    // 47 tag defined for this region. If a region doesn't match, return unknown & just use the
    // country code directly for the formatting locale.
    return definitions.regionDefinitionsList.find {
      machineLocaleImpl.run {
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

  private fun LanguageSupportDefinition.retrieveAppLanguageCode(): String? {
    return when (appStringId.languageTypeCase) {
      LanguageSupportDefinition.LanguageId.LanguageTypeCase.IETF_BCP47_ID ->
        appStringId.ietfBcp47Id.ietfLanguageTag
      LanguageSupportDefinition.LanguageId.LanguageTypeCase.MACARONIC_ID ->
        appStringId.macaronicId.combinedLanguageCode // Likely won't match against system languages.
      LanguageSupportDefinition.LanguageId.LanguageTypeCase.LANGUAGETYPE_NOT_SET, null -> null
    }
  }

  private fun computeDefaultLanguageDefinitionForSystemLanguage(
    languageCode: String
  ) = LanguageSupportDefinition.newBuilder().apply {
    language = OppiaLanguage.LANGUAGE_UNSPECIFIED
    minAndroidSdkVersion = 1 // Assume it's supported on the current version.
    // Only app strings can be supported since this is a system language. Content & audio languages
    // must be part of the language definitions. Support for app strings is exposed so that a locale
    // can be constructed from it.
    appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
      ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = languageCode
      }.build()
    }.build()
  }.build()
}
