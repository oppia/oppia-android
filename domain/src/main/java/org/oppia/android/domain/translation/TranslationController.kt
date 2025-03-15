package org.oppia.android.domain.translation

import com.google.protobuf.MessageLite
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.AudioTranslationLanguageSelection
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId.LanguageTypeCase.IETF_BCP47_ID
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId.LanguageTypeCase.LANGUAGETYPE_NOT_SET
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId.LanguageTypeCase.MACARONIC_ID
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.SubtitledUnicode
import org.oppia.android.app.model.TranslatableSetOfNormalizedString
import org.oppia.android.app.model.Translation
import org.oppia.android.app.model.TranslationMapping
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.locale.LanguageConfigRetriever
import org.oppia.android.domain.locale.LocaleController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.combineWithAsync
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject
import javax.inject.Singleton

private const val SYSTEM_LANGUAGE_LOCALE_DATA_PROVIDER_ID = "system_language_locale"
private const val APP_LANGUAGE_DATA_PROVIDER_ID = "app_language"
private const val APP_LANGUAGE_LOCALE_DATA_PROVIDER_ID = "app_language_locale"
private const val UPDATE_APP_LANGUAGE_DATA_PROVIDER_ID = "update_app_language"
private const val WRITTEN_TRANSLATION_CONTENT_DATA_PROVIDER_ID = "written_translation_content"
private const val WRITTEN_TRANSLATION_CONTENT_LOCALE_DATA_PROVIDER_ID =
  "written_translation_content_locale"
private const val WRITTEN_TRANSLATION_CONTENT_LANG_RES_DATA_PROVIDER_ID =
  "written_translation_content_language_resolution"
private const val WRITTEN_TRANSLATION_CONTENT_SELECTION_DATA_PROVIDER_ID =
  "written_translation_content_selection"
private const val UPDATE_WRITTEN_TRANSLATION_CONTENT_DATA_PROVIDER_ID =
  "update_written_translation_content"
private const val AUDIO_TRANSLATION_CONTENT_DATA_PROVIDER_ID = "audio_translation_content"
private const val AUDIO_TRANSLATION_CONTENT_LOCALE_DATA_PROVIDER_ID =
  "audio_translation_content_locale"
private const val AUDIO_TRANSLATION_CONTENT_LANG_RES_DATA_PROVIDER_ID =
  "audio_translation_content_language_resolution"
private const val AUDIO_TRANSLATION_CONTENT_SELECTION_DATA_PROVIDER_ID =
  "audio_translation_content_selection"
private const val UPDATE_AUDIO_TRANSLATION_CONTENT_DATA_PROVIDER_ID =
  "update_audio_translation_content"
private const val APP_LANGUAGE_CONTENT_DATABASE = "app_language_content_database"
private const val WRITTEN_TRANSLATION_LANGUAGE_CONTENT_DATABASE =
  "written_language_content_database"
private const val AUDIO_TRANSLATION_LANGUAGE_CONTENT_DATABASE =
  "audio_translation_language_content_database"
private const val RETRIEVED_CONTENT_LANGUAGE_DATA_PROVIDER_ID =
  "retrieved_content_language_data_provider_id"

/**
 * Domain controller for performing operations corresponding to translations.
 *
 * This controller is often used instead of [LocaleController] since it provides additional
 * functionality which simplifies interacting with the locales needed for various translation
 * scenarios, but it relies on locale controller as its source of truth.
 */
@Singleton
class TranslationController @Inject constructor(
  private val dataProviders: DataProviders,
  private val localeController: LocaleController,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  private val machineLocale: OppiaLocale.MachineLocale,
  private val languageConfigRetriever: LanguageConfigRetriever,
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val oppiaLogger: OppiaLogger,
) {
  private val appLanguageCacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<AppLanguageSelection>>()
  private val writtenTranslationLanguageCacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<WrittenTranslationLanguageSelection>>()
  private val audioTranslationLanguageCacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<AudioTranslationLanguageSelection>>()

  /**
   * Returns a data provider for an app string [OppiaLocale.DisplayLocale] corresponding to the
   * current user-selected system language.
   */
  fun getSystemLanguageLocale(): DataProvider<OppiaLocale.DisplayLocale> {
    return getSystemLanguage().transformAsync(SYSTEM_LANGUAGE_LOCALE_DATA_PROVIDER_ID) { language ->
      localeController.retrieveAppStringDisplayLocale(language).retrieveData()
    }
  }

  /**
   * Returns a data provider for the current [OppiaLanguage] selected for app strings for the
   * specified user (per their [profileId]).
   *
   * This language can be updated via [updateAppLanguage].
   */
  fun getAppLanguage(profileId: ProfileId): DataProvider<OppiaLanguage> {
    return getAppLanguageLocale(profileId).transform(APP_LANGUAGE_DATA_PROVIDER_ID) { locale ->
      locale.getCurrentLanguage()
    }
  }

  /**
   * Returns a data provider for a list of [OppiaLanguage] which can be passed to [updateAppLanguage].
   */
  fun getSupportedAppLanguages(): DataProvider<List<OppiaLanguage>> {
    return dataProviders.createInMemoryDataProvider(RETRIEVED_CONTENT_LANGUAGE_DATA_PROVIDER_ID) {
      languageConfigRetriever.loadSupportedLanguages().languageDefinitionsList.filter {
        it.hasAppStringId()
      }.map { it.language }
    }
  }

  /**
   * Returns a data provider for a [OppiaLocale.DisplayLocale] corresponding to the user's selected
   * language for app strings (see [getAppLanguage]).
   */
  fun getAppLanguageLocale(profileId: ProfileId): DataProvider<OppiaLocale.DisplayLocale> {
    val providerId = APP_LANGUAGE_LOCALE_DATA_PROVIDER_ID
    return getSystemLanguage().combineWithAsync(
      getAppLanguageSelection(profileId), providerId
    ) { systemLanguage, appLanguageSelection ->
      localeController.retrieveAppStringDisplayLocale(
        language = computeAppLanguage(appLanguageSelection).resolveToLanguage(systemLanguage)
      ).retrieveData()
    }
  }

  /**
   * Returns a data provider for the [AppLanguageSelection] corresponding to the user's selected
   * language for app strings (see [getAppLanguage]).
   *
   * Note that providing the returned selection to [updateAppLanguage] should result in no change to
   * the underlying configured selection.
   */
  fun getAppLanguageSelection(profileId: ProfileId): DataProvider<AppLanguageSelection> =
    retrieveAppLanguageContentCacheStore(profileId)

  /**
   * Updates the language to be used by the specified user for app string translations. Note that
   * the provided [AppLanguageSelection] provides the user with the option of either selecting a
   * specific supported language for app strings, or to fall back to the system default.
   *
   * The app guarantees app language compatibility for any non-system language selected, and a
   * best-effort basis for translating strings for system languages (generally if the system
   * language matches a supported language, otherwise the app defaults to English).
   *
   * @return a [DataProvider] which succeeds only if the update succeeds, otherwise fails. The
   *     payload of the data provider is the *previous* selection state.
   */
  fun updateAppLanguage(
    profileId: ProfileId,
    selection: AppLanguageSelection
  ): DataProvider<AppLanguageSelection> {
    val cacheStore = retrieveAppLanguageContentCacheStore(profileId)
    return dataProviders.createInMemoryDataProviderAsync(UPDATE_APP_LANGUAGE_DATA_PROVIDER_ID) {
      AsyncResult.Success(cacheStore.readDataAsync().await()).also {
        cacheStore.storeDataAsync(updateInMemoryCache = true) { selection }.await()
      }
    }
  }

  /**
   * Returns a data provider for the current [OppiaLanguage] selected for written content strings
   * for the specified user (per their [profileId]).
   *
   * This language can be updated via [updateWrittenTranslationContentLanguage].
   */
  fun getWrittenTranslationContentLanguage(profileId: ProfileId): DataProvider<OppiaLanguage> {
    val providerId = WRITTEN_TRANSLATION_CONTENT_DATA_PROVIDER_ID
    return getWrittenTranslationContentLocale(profileId).transform(providerId) { locale ->
      locale.getCurrentLanguage()
    }
  }

  /**
   * Returns a data provider for a [OppiaLocale.ContentLocale] corresponding to the user's selected
   * language for written content strings (see [getWrittenTranslationContentLanguage]).
   */
  fun getWrittenTranslationContentLocale(
    profileId: ProfileId
  ): DataProvider<OppiaLocale.ContentLocale> {
    val resolvedLanguageProvider =
      getWrittenTranslationContentLanguageSelection(profileId).combineWith(
        getAppLanguageSelection(profileId), WRITTEN_TRANSLATION_CONTENT_LANG_RES_DATA_PROVIDER_ID
      ) { contentLanguageSelection, appLanguageSelection ->
        computeWrittenTranslationContentLanguage(appLanguageSelection, contentLanguageSelection)
      }
    return getSystemLanguage().combineWithAsync(
      resolvedLanguageProvider, WRITTEN_TRANSLATION_CONTENT_LOCALE_DATA_PROVIDER_ID
    ) { systemLanguage, resolutionStatus ->
      val language = resolutionStatus.resolveToLanguage(systemLanguage)
      val writtenTranslationLocale = localeController.retrieveWrittenTranslationsLocale(language)
      return@combineWithAsync writtenTranslationLocale.retrieveData()
    }
  }

  /**
   * Returns a data provider for the [WrittenTranslationLanguageSelection] corresponding to the
   * user's selected language for written content strings (see
   * [getWrittenTranslationContentLanguage]).
   *
   * Note that providing the returned selection to [updateWrittenTranslationContentLanguage] should
   * result in no change to the underlying configured selection.
   */
  fun getWrittenTranslationContentLanguageSelection(
    profileId: ProfileId
  ): DataProvider<WrittenTranslationLanguageSelection> =
    retrieveWrittenTranslationLanguageContentCacheStore(profileId)

  /**
   * Updates the language to be used by the specified user for written content string translations.
   * Note that the provided [WrittenTranslationLanguageSelection] provides the user with the option
   * of either selecting a specific supported language for content strings, or to fall back to
   * whatever the app language selection is (which may be the system default).
   *
   * Note that the app guarantees a list of languages to support for written translations as a
   * superset. The actual availability for a particular language is topic-dependent.
   *
   * @return a [DataProvider] which succeeds only if the update succeeds, otherwise fails (only one
   *     result is ever provided). The payload of the data provider is the *previous* selection
   *     state.
   */
  fun updateWrittenTranslationContentLanguage(
    profileId: ProfileId,
    selection: WrittenTranslationLanguageSelection
  ): DataProvider<WrittenTranslationLanguageSelection> {
    val cacheStore = retrieveWrittenTranslationLanguageContentCacheStore(profileId)
    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_WRITTEN_TRANSLATION_CONTENT_DATA_PROVIDER_ID
    ) {
      AsyncResult.Success(cacheStore.readDataAsync().await()).also {
        cacheStore.storeDataAsync(updateInMemoryCache = true) { selection }.await()
      }
    }
  }

  /**
   * Returns a data provider for the current [OppiaLanguage] selected for audio voiceovers for the
   * specified user (per their [profileId]).
   *
   * This language can be updated via [updateAudioTranslationContentLanguage].
   */
  fun getAudioTranslationContentLanguage(profileId: ProfileId): DataProvider<OppiaLanguage> {
    val providerId = AUDIO_TRANSLATION_CONTENT_DATA_PROVIDER_ID
    return getAudioTranslationContentLocale(profileId).transform(providerId) { locale ->
      locale.getCurrentLanguage()
    }
  }

  /**
   * Returns a data provider for a [OppiaLocale.ContentLocale] corresponding to the user's selected
   * language for audio voiceovers (see [getAudioTranslationContentLanguage]).
   */
  fun getAudioTranslationContentLocale(
    profileId: ProfileId
  ): DataProvider<OppiaLocale.ContentLocale> {
    val resolvedLanguageProvider =
      getAudioTranslationContentLanguageSelection(profileId).combineWith(
        getAppLanguageSelection(profileId), AUDIO_TRANSLATION_CONTENT_LANG_RES_DATA_PROVIDER_ID
      ) { audioLanguageSelection, appLanguageSelection ->
        computeAudioTranslationContentLanguage(appLanguageSelection, audioLanguageSelection)
      }
    return getSystemLanguage().combineWithAsync(
      resolvedLanguageProvider, AUDIO_TRANSLATION_CONTENT_LOCALE_DATA_PROVIDER_ID
    ) { systemLanguage, resolutionStatus ->
      val language = resolutionStatus.resolveToLanguage(systemLanguage)
      val audioTranslationLocale = localeController.retrieveAudioTranslationsLocale(language)
      return@combineWithAsync audioTranslationLocale.retrieveData()
    }
  }

  /**
   * Returns a data provider for the [AudioTranslationLanguageSelection] corresponding to the user's
   * selected language for audio voiceovers (see [getAudioTranslationContentLanguage]).
   *
   * Note that providing the returned selection to [updateAudioTranslationContentLanguage] should
   * result in no change to the underlying configured selection.
   */
  fun getAudioTranslationContentLanguageSelection(
    profileId: ProfileId
  ): DataProvider<AudioTranslationLanguageSelection> =
    retrieveAudioTranslationLanguageContentCacheStore(profileId)

  /**
   * Updates the language to be used by the specified user for audio voiceover selection. Note that
   * the provided [AudioTranslationLanguageSelection] provides the user with the option of either
   * selecting a specific supported language for audio voiceovers, or to fall back to whatever the
   * app language selection is (which may be the system default).
   *
   * Note that the app guarantees a list of languages to support for audio voiceovers as a superset.
   * The actual availability for a particular language is topic-dependent.
   *
   * @return a [DataProvider] which succeeds only if the update succeeds, otherwise fails (only one
   *     result is ever provided). The payload of the data provider is the *previous* selection
   *     state.
   */
  fun updateAudioTranslationContentLanguage(
    profileId: ProfileId,
    selection: AudioTranslationLanguageSelection
  ): DataProvider<AudioTranslationLanguageSelection> {
    val cacheStore = retrieveAudioTranslationLanguageContentCacheStore(profileId)
    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_AUDIO_TRANSLATION_CONTENT_DATA_PROVIDER_ID
    ) {
      AsyncResult.Success(cacheStore.readDataAsync().await()).also {
        cacheStore.storeDataAsync(updateInMemoryCache = true) { selection }.await()
      }
    }
  }

  /**
   * Returns a potentially translated HTML string for the given [SubtitledHtml] to display to the
   * user, considering the specified [WrittenTranslationContext].
   *
   * Generally, this method will attempt to return the translated string corresponding to this
   * [SubtitledHtml] given the translation context, but will fall back to the default string
   * contents if no translation exists for the subtitle.
   */
  fun extractString(html: SubtitledHtml, context: WrittenTranslationContext): String {
    return context.translationsMap[html.contentId]?.extractHtml() ?: html.html
  }

  /**
   * Returns a potentially translated Unicode string for the given [SubtitledUnicode] to display to
   * the user, with the same considerations and behavior as the [extractString] ([SubtitledHtml]
   * variant).
   */
  fun extractString(unicode: SubtitledUnicode, context: WrittenTranslationContext): String {
    return context.translationsMap[unicode.contentId]?.extractHtml() ?: unicode.unicodeStr
  }

  /**
   * Returns a potentially translated list of strings extracted from the provided
   * [TranslatableSetOfNormalizedString] based on the provided [WrittenTranslationContext].
   */
  fun extractStringList(
    translatableSetOfNormalizedString: TranslatableSetOfNormalizedString,
    context: WrittenTranslationContext
  ): List<String> {
    return context.translationsMap[translatableSetOfNormalizedString.contentId]?.extractHtmlList()
      ?: translatableSetOfNormalizedString.normalizedStringsList
  }

  /**
   * Returns a new [WrittenTranslationContext] based on the specific written translation map and
   * [OppiaLocale.ContentLocale] to translate content strings into.
   *
   * The returned context is meant to be used to translate content-specific strings using
   * [extractString] and [extractStringList].
   */
  fun computeWrittenTranslationContext(
    writtenTranslationsMap: Map<String, TranslationMapping>,
    writtenTranslationContentLocale: OppiaLocale.ContentLocale
  ): WrittenTranslationContext = WrittenTranslationContext.newBuilder().apply {
    val languageCode = writtenTranslationContentLocale.getLanguageId().getOppiaLanguageCode()
    val fallbackLanguageCode =
      writtenTranslationContentLocale.getFallbackLanguageId().getOppiaLanguageCode()
    val contentMapping = writtenTranslationsMap.mapValuesNotNull { (_, mapping) ->
      mapping.selectTranslation(languageCode, fallbackLanguageCode)
    }
    // Translations that don't match this context are excluded (so app layer code is expected to
    // default to the base HTML translation).
    putAllTranslations(contentMapping)
    language = writtenTranslationContentLocale.getCurrentLanguage()
  }.build()

  private fun computeAppLanguage(
    languageSelection: AppLanguageSelection
  ): LanguageResolutionStatus {
    return when (languageSelection.selectionTypeCase) {
      AppLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE ->
        LanguageResolutionStatus.Resolved(languageSelection.selectedLanguage)
      AppLanguageSelection.SelectionTypeCase.USE_SYSTEM_LANGUAGE_OR_APP_DEFAULT,
      AppLanguageSelection.SelectionTypeCase.SELECTIONTYPE_NOT_SET, null ->
        LanguageResolutionStatus.UseSystemLanguage
    }
  }

  private fun computeWrittenTranslationContentLanguage(
    appLanguageSelection: AppLanguageSelection,
    contentLanguageSelection: WrittenTranslationLanguageSelection
  ): LanguageResolutionStatus {
    return when (contentLanguageSelection.selectionTypeCase) {
      WrittenTranslationLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE ->
        LanguageResolutionStatus.Resolved(contentLanguageSelection.selectedLanguage)
      WrittenTranslationLanguageSelection.SelectionTypeCase.USE_APP_LANGUAGE,
      WrittenTranslationLanguageSelection.SelectionTypeCase.SELECTIONTYPE_NOT_SET, null ->
        computeAppLanguage(appLanguageSelection)
    }
  }

  private fun computeAudioTranslationContentLanguage(
    appLanguageSelection: AppLanguageSelection,
    audioLanguageSelection: AudioTranslationLanguageSelection
  ): LanguageResolutionStatus {
    return when (audioLanguageSelection.selectionTypeCase) {
      AudioTranslationLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE ->
        LanguageResolutionStatus.Resolved(audioLanguageSelection.selectedLanguage)
      AudioTranslationLanguageSelection.SelectionTypeCase.USE_APP_LANGUAGE,
      AudioTranslationLanguageSelection.SelectionTypeCase.SELECTIONTYPE_NOT_SET, null ->
        computeAppLanguage(appLanguageSelection)
    }
  }

  private fun retrieveAppLanguageContentCacheStore(
    profileId: ProfileId
  ): PersistentCacheStore<AppLanguageSelection> {
    return retrieveContentCacheStore(
      profileId,
      APP_LANGUAGE_CONTENT_DATABASE,
      AppLanguageSelection.getDefaultInstance(),
      appLanguageCacheStoreMap
    )
  }

  private fun retrieveWrittenTranslationLanguageContentCacheStore(
    profileId: ProfileId
  ): PersistentCacheStore<WrittenTranslationLanguageSelection> {
    return retrieveContentCacheStore(
      profileId,
      WRITTEN_TRANSLATION_LANGUAGE_CONTENT_DATABASE,
      WrittenTranslationLanguageSelection.getDefaultInstance(),
      writtenTranslationLanguageCacheStoreMap
    )
  }

  private fun retrieveAudioTranslationLanguageContentCacheStore(
    profileId: ProfileId
  ): PersistentCacheStore<AudioTranslationLanguageSelection> {
    return retrieveContentCacheStore(
      profileId,
      AUDIO_TRANSLATION_LANGUAGE_CONTENT_DATABASE,
      AudioTranslationLanguageSelection.getDefaultInstance(),
      audioTranslationLanguageCacheStoreMap
    )
  }

  private fun <T : MessageLite> retrieveContentCacheStore(
    profileId: ProfileId,
    databaseName: String,
    defaultCacheValue: T,
    cacheMap: MutableMap<ProfileId, PersistentCacheStore<T>>
  ): PersistentCacheStore<T> {
    return cacheMap.getOrPut(profileId) {
      cacheStoreFactory.createPerProfile(
        databaseName, defaultCacheValue, profileId
      ).also<PersistentCacheStore<T>> {
        it.primeInMemoryAndDiskCacheAsync(
          updateMode = PersistentCacheStore.UpdateMode.UPDATE_IF_NEW_CACHE,
          publishMode = PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
        ).invokeOnCompletion { throwable ->
          throwable?.let { error ->
            oppiaLogger.e(
              "TranslationController",
              "Failed to prime cache ahead of data retrieval for TranslationController.",
              error
            )
          }
        }
      }
    }
  }

  private fun getSystemLanguage(): DataProvider<OppiaLanguage> =
    localeController.retrieveSystemLanguage()

  private fun TranslationMapping.selectTranslation(
    languageCode: String?,
    fallbackLanguageCode: String?
  ): Translation? {
    val mappingMapLowercaseKeys =
      translationMappingMap.mapKeys { (key, _) ->
        machineLocale.run { key.toMachineLowerCase() }
      }
    return languageCode?.let { mappingMapLowercaseKeys[it] }
      ?: fallbackLanguageCode?.let { mappingMapLowercaseKeys[it] }
  }

  private fun LanguageSupportDefinition.LanguageId.getOppiaLanguageCode(): String? {
    return when (languageTypeCase) {
      IETF_BCP47_ID -> machineLocale.run { ietfBcp47Id.ietfLanguageTag.toMachineLowerCase() }
      MACARONIC_ID -> machineLocale.run { macaronicId.combinedLanguageCode.toMachineLowerCase() }
      LANGUAGETYPE_NOT_SET, null -> null
    }
  }

  /**
   * A helper class to provide complete language resolution for cases where there may be multiple
   * levels of indirection (such as written language translations falling back to the app language
   * and the app language falling back to the system language).
   */
  private sealed class LanguageResolutionStatus {
    /** Indicates a fully resolved [OppiaLanguage] that may be used for localization. */
    data class Resolved(val language: OppiaLanguage) : LanguageResolutionStatus()

    /**
     * Indicates that no explicit [OppiaLanguage] can be resolved and that, instead, the system
     * language must be used.
     */
    object UseSystemLanguage : LanguageResolutionStatus()
  }

  private companion object {
    private fun Translation.extractHtml(): String? = takeIf {
      it.dataFormatCase == Translation.DataFormatCase.HTML
    }?.html

    private fun Translation.extractHtmlList(): List<String>? = takeIf {
      it.dataFormatCase == Translation.DataFormatCase.HTML_LIST
    }?.htmlList?.htmlList

    private fun <K, I, O> Map<K, I>.mapValuesNotNull(map: (Map.Entry<K, I>) -> O?): Map<K, O> {
      // The force-non-null operator is safe here since nulls are filtered out.
      return mapValues(map).filterValues { it != null }.mapValues { (_, value) -> value!! }
    }

    private fun LanguageResolutionStatus.resolveToLanguage(
      systemLanguage: OppiaLanguage
    ): OppiaLanguage {
      return when (this) {
        is LanguageResolutionStatus.Resolved -> language
        LanguageResolutionStatus.UseSystemLanguage -> systemLanguage
      }
    }
  }
}
