package org.oppia.android.domain.translation

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
import org.oppia.android.util.data.DataProviders.Companion.combineWithAsync
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.locale.OppiaLocale
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

private const val SYSTEM_LANGUAGE_LOCALE_DATA_PROVIDER_ID = "system_language_locale"
private const val APP_LANGUAGE_DATA_PROVIDER_ID = "app_language"
private const val APP_LANGUAGE_LOCALE_DATA_PROVIDER_ID = "app_language_locale"
private const val UPDATE_APP_LANGUAGE_DATA_PROVIDER_ID = "update_app_language"
private const val WRITTEN_TRANSLATION_CONTENT_DATA_PROVIDER_ID = "written_translation_content"
private const val WRITTEN_TRANSLATION_CONTENT_LOCALE_DATA_PROVIDER_ID =
  "written_translation_content_locale"
private const val UPDATE_WRITTEN_TRANSLATION_CONTENT_DATA_PROVIDER_ID =
  "update_written_translation_content"
private const val AUDIO_TRANSLATION_CONTENT_DATA_PROVIDER_ID = "audio_translation_content"
private const val AUDIO_TRANSLATION_CONTENT_LOCALE_DATA_PROVIDER_ID =
  "audio_translation_content_locale"
private const val UPDATE_AUDIO_TRANSLATION_CONTENT_DATA_PROVIDER_ID =
  "update_audio_translation_content"
private const val CACHE_NAME = "content_language_database"
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
  // TODO(#52): Finish this implementation. The implementation below doesn't actually save/restore
  //  settings from the local filesystem since the UI has been currently disabled as part of #20.
  //  Also, there should be a proper default locale for pre-profile selection (either a default
  //  app-wide setting determined by the administrator, or the last locale used by a profile)--more
  //  product & UX thought is needed here. Further, extra work is likely needed to handle the case
  //  when the user switches between a system & non-system language since the existing system
  //  defaulting behavior breaks down when the system locale is overwritten (such as when a
  //  different language is selected).

  private val dataLock = ReentrantLock()
  private val writtenTranslationLanguageSettings =
    mutableMapOf<ProfileId, WrittenTranslationLanguageSelection>()
  private val audioVoiceoverLanguageSettings =
    mutableMapOf<ProfileId, AudioTranslationLanguageSelection>()

  private val cacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<AppLanguageSelection>>()
  private val appLanguageSettings = mutableMapOf<ProfileId, AppLanguageSelection>()

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
   * Returns List data for [OppiaLanguage] which is filtered to exclude languages without appStringId tag.
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
      retrieveLanguageContentCacheStore(profileId),
      providerId
    ) { systemLanguage, oppiaLanguageSelection ->
      val language = computeAppLanguage(systemLanguage, oppiaLanguageSelection)
      return@combineWithAsync localeController.retrieveAppStringDisplayLocale(language)
        .retrieveData()
    }
  }

  /**
   * Updates the language to be used by the specified user for app string translations. Note that
   * the provided [AppLanguageSelection] provides the user with the option of either selecting a
   * specific supported language for app strings, or to fall back to the system default.
   *
   * The app guarantees app language compatibility for any non-system language selected, and a
   * best-effort basis for translating strings for system languages (generally if the system
   * language matches a supported language, otherwise the app defaults to English).
   *
   * @return a [DataProvider] which succeeds only if the update succeeds, otherwise fails (only one
   *     result is ever provided)
   */
  fun updateAppLanguage(profileId: ProfileId, selection: AppLanguageSelection): DataProvider<Any> {
    val deferred = retrieveLanguageContentCacheStore(profileId).storeDataAsync(
      updateInMemoryCache = true
    ) {
      AppLanguageSelection.newBuilder().apply {
        selectedLanguage = selection.selectedLanguage
        selectedLanguageValue = selection.selectedLanguage.number
      }.build()
    }

    return dataProviders.createInMemoryDataProviderAsync(
      UPDATE_APP_LANGUAGE_DATA_PROVIDER_ID
    ) {
      updateAppLanguageSelection(profileId, selection)
      try {
        return@createInMemoryDataProviderAsync AsyncResult.Success(deferred.await())
      } catch (e: Exception) {
        return@createInMemoryDataProviderAsync AsyncResult.Failure(e)
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
    val providerId = WRITTEN_TRANSLATION_CONTENT_LOCALE_DATA_PROVIDER_ID
    return getSystemLanguage().transformAsync(providerId) { systemLanguage ->
      val language = computeWrittenTranslationContentLanguage(profileId, systemLanguage)
      val writtenTranslationLocale = localeController.retrieveWrittenTranslationsLocale(language)
      return@transformAsync writtenTranslationLocale.retrieveData()
    }
  }

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
   *     result is ever provided)
   */
  fun updateWrittenTranslationContentLanguage(
    profileId: ProfileId,
    selection: WrittenTranslationLanguageSelection
  ): DataProvider<Any> {
    val providerId = UPDATE_WRITTEN_TRANSLATION_CONTENT_DATA_PROVIDER_ID
    return dataProviders.createInMemoryDataProviderAsync(providerId) {
      updateWrittenTranslationContentLanguageSelection(profileId, selection)
      return@createInMemoryDataProviderAsync AsyncResult.Success(Unit)
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
    val providerId = AUDIO_TRANSLATION_CONTENT_LOCALE_DATA_PROVIDER_ID
    return getSystemLanguage().transformAsync(providerId) { systemLanguage ->
      val language = computeAudioTranslationContentLanguage(profileId, systemLanguage)
      val audioTranslationLocale = localeController.retrieveAudioTranslationsLocale(language)
      return@transformAsync audioTranslationLocale.retrieveData()
    }
  }

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
   *     result is ever provided)
   */
  fun updateAudioTranslationContentLanguage(
    profileId: ProfileId,
    selection: AudioTranslationLanguageSelection
  ): DataProvider<Any> {
    val providerId = UPDATE_AUDIO_TRANSLATION_CONTENT_DATA_PROVIDER_ID
    return dataProviders.createInMemoryDataProviderAsync(providerId) {
      updateAudioTranslationContentLanguageSelection(profileId, selection)
      return@createInMemoryDataProviderAsync AsyncResult.Success(Unit)
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
    systemLanguage: OppiaLanguage,
    appLanguageSelection: AppLanguageSelection
  ): OppiaLanguage {

    return when (appLanguageSelection.selectionTypeCase) {
      AppLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE ->
        appLanguageSelection.selectedLanguage
      AppLanguageSelection.SelectionTypeCase.USE_SYSTEM_LANGUAGE_OR_APP_DEFAULT -> systemLanguage
      AppLanguageSelection.SelectionTypeCase.SELECTIONTYPE_NOT_SET, null -> systemLanguage
    }
  }

  private fun computeWrittenTranslationContentLanguage(
    profileId: ProfileId,
    systemLanguage: OppiaLanguage
  ): OppiaLanguage {
    val languageSelection = retrieveWrittenTranslationContentLanguageSelection(profileId)
    return when (languageSelection.selectionTypeCase) {
      WrittenTranslationLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE ->
        languageSelection.selectedLanguage
      WrittenTranslationLanguageSelection.SelectionTypeCase.USE_APP_LANGUAGE,
      WrittenTranslationLanguageSelection.SelectionTypeCase.SELECTIONTYPE_NOT_SET, null ->
        computeAppLanguage(systemLanguage, loadAppLanguageSelection(profileId))
    }
  }

  private fun computeAudioTranslationContentLanguage(
    profileId: ProfileId,
    systemLanguage: OppiaLanguage
  ): OppiaLanguage {
    val languageSelection = retrieveAudioTranslationContentLanguageSelection(profileId)
    return when (languageSelection.selectionTypeCase) {
      AudioTranslationLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE ->
        languageSelection.selectedLanguage
      AudioTranslationLanguageSelection.SelectionTypeCase.USE_APP_LANGUAGE,
      AudioTranslationLanguageSelection.SelectionTypeCase.SELECTIONTYPE_NOT_SET, null ->
        computeAppLanguage(systemLanguage, loadAppLanguageSelection(profileId))
    }
  }

  private fun loadAppLanguageSelection(profileId: ProfileId): AppLanguageSelection {
    return dataLock.withLock {
      appLanguageSettings[profileId] ?: AppLanguageSelection.getDefaultInstance()
    }
  }

  private suspend fun updateAppLanguageSelection(
    profileId: ProfileId,
    selection: AppLanguageSelection
  ) {
    dataLock.withLock {
      appLanguageSettings[profileId] = selection
    }
    asyncDataSubscriptionManager.notifyChange(APP_LANGUAGE_LOCALE_DATA_PROVIDER_ID)
  }

  private fun retrieveLanguageContentCacheStore(
    profileId: ProfileId
  ): PersistentCacheStore<AppLanguageSelection> {
    return cacheStoreMap.getOrPut(profileId) {
      cacheStoreFactory.createPerProfile(
        CACHE_NAME,
        AppLanguageSelection.getDefaultInstance(),
        profileId
      ).also<PersistentCacheStore<AppLanguageSelection>> {
        it.primeInMemoryAndDiskCacheAsync(
          updateMode = PersistentCacheStore.UpdateMode.UPDATE_IF_NEW_CACHE,
          publishMode = PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
        ).invokeOnCompletion { throwable ->
          throwable?.let {
            oppiaLogger.e(
              "TranslationController",
              "Failed to prime cache ahead of data retrieval for TranslationController.",
              it
            )
          }
        }
      }
    }
  }

  private fun retrieveWrittenTranslationContentLanguageSelection(
    profileId: ProfileId
  ): WrittenTranslationLanguageSelection {
    return dataLock.withLock {
      writtenTranslationLanguageSettings[profileId]
        ?: WrittenTranslationLanguageSelection.getDefaultInstance()
    }
  }

  private suspend fun updateWrittenTranslationContentLanguageSelection(
    profileId: ProfileId,
    selection: WrittenTranslationLanguageSelection
  ) {
    dataLock.withLock {
      writtenTranslationLanguageSettings[profileId] = selection
    }
    asyncDataSubscriptionManager.notifyChange(WRITTEN_TRANSLATION_CONTENT_LOCALE_DATA_PROVIDER_ID)
  }

  private fun retrieveAudioTranslationContentLanguageSelection(
    profileId: ProfileId
  ): AudioTranslationLanguageSelection {
    return dataLock.withLock {
      audioVoiceoverLanguageSettings[profileId]
        ?: AudioTranslationLanguageSelection.getDefaultInstance()
    }
  }

  private suspend fun updateAudioTranslationContentLanguageSelection(
    profileId: ProfileId,
    selection: AudioTranslationLanguageSelection
  ) {
    dataLock.withLock {
      audioVoiceoverLanguageSettings[profileId] = selection
    }
    asyncDataSubscriptionManager.notifyChange(AUDIO_TRANSLATION_CONTENT_LOCALE_DATA_PROVIDER_ID)
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
  }
}
