package org.oppia.android.domain.translation

import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.AudioTranslationLanguageSelection
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.SubtitledUnicode
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.domain.locale.LocaleController
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.data.DataProviders.Companion.transformAsync

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

class TranslationController @Inject constructor(
  private val dataProviders: DataProviders,
  private val localeController: LocaleController,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager
) {
  // TODO(#52): Finish this implementation. The implementation below doesn't actually save/restore
  //  settings from the local filesystem since the UI has been currently disabled as part of #20.
  //  Also, there should be a proper default locale for pre-profile selection (either a default
  //  app-wide setting determined by the administrator, or the last locale used by a profile)--more
  //  product & UX thought is needed here.

  private val dataLock = ReentrantLock()
  private val appLanguageSettings = mutableMapOf<ProfileId, AppLanguageSelection>()
  private val writtenTranslationLanguageSettings =
    mutableMapOf<ProfileId, WrittenTranslationLanguageSelection>()
  private val audioVoiceoverLanguageSettings =
    mutableMapOf<ProfileId, AudioTranslationLanguageSelection>()

  fun getSystemLanguageLocale(): DataProvider<OppiaLocale.DisplayLocale> {
    return getSystemLanguage().transformAsync(SYSTEM_LANGUAGE_LOCALE_DATA_PROVIDER_ID) { language ->
      localeController.retrieveAppStringDisplayLocale(language).retrieveData()
    }
  }

  fun getAppLanguage(profileId: ProfileId): DataProvider<OppiaLanguage> {
    return getAppLanguageLocale(profileId).transform(APP_LANGUAGE_DATA_PROVIDER_ID) { locale ->
      locale.getCurrentLanguage()
    }
  }

  fun getAppLanguageLocale(profileId: ProfileId): DataProvider<OppiaLocale.DisplayLocale> {
    val providerId = APP_LANGUAGE_LOCALE_DATA_PROVIDER_ID
    return getSystemLanguage().transformAsync(providerId) { systemLanguage ->
      val language = computeAppLanguage(profileId, systemLanguage)
      return@transformAsync localeController.retrieveAppStringDisplayLocale(language).retrieveData()
    }
  }

  fun updateAppLanguage(profileId: ProfileId, selection: AppLanguageSelection): DataProvider<Any?> {
    return dataProviders.createInMemoryDataProviderAsync(UPDATE_APP_LANGUAGE_DATA_PROVIDER_ID) {
      updateAppLanguageSelection(profileId, selection)
      return@createInMemoryDataProviderAsync AsyncResult.success(null)
    }
  }

  fun getWrittenTranslationContentLanguage(profileId: ProfileId): DataProvider<OppiaLanguage> {
    val providerId = WRITTEN_TRANSLATION_CONTENT_DATA_PROVIDER_ID
    return getWrittenTranslationContentLocale(profileId).transform(providerId) { locale ->
      locale.getCurrentLanguage()
    }
  }

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

  fun updateWrittenTranslationContentLanguage(
    profileId: ProfileId,
    selection: WrittenTranslationLanguageSelection
  ): DataProvider<Any?> {
    val providerId = UPDATE_WRITTEN_TRANSLATION_CONTENT_DATA_PROVIDER_ID
    return dataProviders.createInMemoryDataProviderAsync(providerId) {
      updateWrittenTranslationContentLanguageSelection(profileId, selection)
      return@createInMemoryDataProviderAsync AsyncResult.success(null)
    }
  }

  fun getAudioTranslationContentLanguage(profileId: ProfileId): DataProvider<OppiaLanguage> {
    val providerId = AUDIO_TRANSLATION_CONTENT_DATA_PROVIDER_ID
    return getAudioTranslationContentLocale(profileId).transform(providerId) { locale ->
      locale.getCurrentLanguage()
    }
  }

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

  fun updateAudioTranslationContentLanguage(
    profileId: ProfileId,
    selection: AudioTranslationLanguageSelection
  ): DataProvider<Any?> {
    val providerId = UPDATE_AUDIO_TRANSLATION_CONTENT_DATA_PROVIDER_ID
    return dataProviders.createInMemoryDataProviderAsync(providerId) {
      updateAudioTranslationContentLanguageSelection(profileId, selection)
      return@createInMemoryDataProviderAsync AsyncResult.success(null)
    }
  }

  fun extractString(html: SubtitledHtml, context: WrittenTranslationContext): String {
    return context.translationsMap[html.contentId]?.html ?: html.html
  }

  fun extractString(unicode: SubtitledUnicode, context: WrittenTranslationContext): String {
    return context.translationsMap[unicode.contentId]?.html ?: unicode.unicodeStr
  }

  private fun computeAppLanguage(
    profileId: ProfileId,
    systemLanguage: OppiaLanguage
  ): OppiaLanguage {
    val languageSelection = retrieveAppLanguageSelection(profileId)
    return when (languageSelection.selectionTypeCase) {
      AppLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE -> languageSelection.selectedLanguage
      AppLanguageSelection.SelectionTypeCase.USE_SYSTEM_LANGUAGE_OR_APP_DEFAULT,
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
        computeAppLanguage(profileId, systemLanguage)
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
        computeAppLanguage(profileId, systemLanguage)
    }
  }

  private fun retrieveAppLanguageSelection(profileId: ProfileId): AppLanguageSelection {
    return dataLock.withLock {
      appLanguageSettings[profileId] ?: AppLanguageSelection.getDefaultInstance()
    }
  }

  private suspend fun updateAppLanguageSelection(
    profileId: ProfileId, selection: AppLanguageSelection
  ) {
    dataLock.withLock {
      appLanguageSettings[profileId] = selection
    }
    asyncDataSubscriptionManager.notifyChange(APP_LANGUAGE_LOCALE_DATA_PROVIDER_ID)
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
    profileId: ProfileId, selection: WrittenTranslationLanguageSelection
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
    profileId: ProfileId, selection: AudioTranslationLanguageSelection
  ) {
    dataLock.withLock {
      audioVoiceoverLanguageSettings[profileId] = selection
    }
    asyncDataSubscriptionManager.notifyChange(AUDIO_TRANSLATION_CONTENT_LOCALE_DATA_PROVIDER_ID)
  }

  private fun getSystemLanguage(): DataProvider<OppiaLanguage> =
    localeController.retrieveSystemLanguage()
}
