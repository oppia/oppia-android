package org.oppia.android.domain.translation

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.AppLanguageSelection.SelectionTypeCase.USE_SYSTEM_LANGUAGE_OR_APP_DEFAULT
import org.oppia.android.app.model.AudioTranslationLanguageSelection
import org.oppia.android.app.model.HtmlTranslationList
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId.LanguageTypeCase.IETF_BCP47_ID
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId.LanguageTypeCase.LANGUAGETYPE_NOT_SET
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.ARABIC
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.HINDI
import org.oppia.android.app.model.OppiaLanguage.LANGUAGE_UNSPECIFIED
import org.oppia.android.app.model.OppiaLanguage.NIGERIAN_PIDGIN
import org.oppia.android.app.model.OppiaLanguage.PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.SWAHILI
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.OppiaRegion.BRAZIL
import org.oppia.android.app.model.OppiaRegion.INDIA
import org.oppia.android.app.model.OppiaRegion.REGION_UNSPECIFIED
import org.oppia.android.app.model.OppiaRegion.UNITED_STATES
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.SubtitledUnicode
import org.oppia.android.app.model.TranslatableSetOfNormalizedString
import org.oppia.android.app.model.Translation
import org.oppia.android.app.model.TranslationMapping
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.model.WrittenTranslationLanguageSelection
import org.oppia.android.domain.locale.LocaleController
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.app.model.AppLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE as SELECTED_APP_LANGUAGE
import org.oppia.android.app.model.AudioTranslationLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE as SELECTED_AUDIO_LANGUAGE
import org.oppia.android.app.model.AudioTranslationLanguageSelection.SelectionTypeCase.USE_APP_LANGUAGE as USE_APP_AUDIO_LANGUAGE
import org.oppia.android.app.model.WrittenTranslationLanguageSelection.SelectionTypeCase.SELECTED_LANGUAGE as SELECTED_WRITTEN_LANGUAGE
import org.oppia.android.app.model.WrittenTranslationLanguageSelection.SelectionTypeCase.USE_APP_LANGUAGE as USE_APP_WRITTEN_LANGUAGE

/** Tests for [TranslationController]. */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = TranslationControllerTest.TestApplication::class)
class TranslationControllerTest {
  @Inject
  lateinit var translationController: TranslationController

  @Inject
  lateinit var localeController: LocaleController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  /* Tests for getSystemLanguageLocale */

  @Test
  fun testGetSystemLanguageLocale_rootLocale_returnsLocaleWithBlankContext() {
    forceDefaultLocale(Locale.ROOT)

    val localeProvider = translationController.getSystemLanguageLocale()

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    val appStringId = context.languageDefinition.appStringId
    assertThat(context.usageMode).isEqualTo(APP_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(LANGUAGE_UNSPECIFIED)
    assertThat(appStringId.languageTypeCase).isEqualTo(LANGUAGETYPE_NOT_SET)
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
  }

  @Test
  fun testGetSystemLanguageLocale_usLocale_returnsLocaleWithEnglishContext() {
    forceDefaultLocale(Locale.US)

    val localeProvider = translationController.getSystemLanguageLocale()

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(APP_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    assertThat(context.regionDefinition.region).isEqualTo(UNITED_STATES)
  }

  @Test
  fun testGetSystemLanguageLocale_updateLocaleToIndia_doesNotUpdateProviderWithNewLocale() {
    forceDefaultLocale(Locale.US)
    val localeProvider = translationController.getSystemLanguageLocale()
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    localeController.setAsDefault(createDisplayLocaleForLanguage(HINDI), Configuration())

    // Verify that the provider hasn't changed since simply calling setAsDefault isn't sufficient to
    // trigger a system provider change.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testGetSystemLanguageLocale_updateDisplayAndSysLocaleToIndia_updatesProviderWithNewLocale() {
    forceDefaultLocale(Locale.US)
    val localeProvider = translationController.getSystemLanguageLocale()
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    forceDefaultLocale(INDIA_HINDI_LOCALE)
    localeController.setAsDefault(createDisplayLocaleForLanguage(HINDI), Configuration())

    // Verify that the provider has changed since the system & display locales were updated.
    val locale = monitor.waitForNextSuccessResult()
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(APP_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(HINDI)
    assertThat(context.regionDefinition.region).isEqualTo(INDIA)
  }

  /* Tests for app language functions */

  @Test
  fun testUpdateAppLanguage_returnsSuccess() {
    forceDefaultLocale(Locale.ROOT)

    val resultProvider =
      translationController.updateAppLanguage(PROFILE_ID_0, createAppLanguageSelection(ENGLISH))

    monitorFactory.waitForNextSuccessfulResult(resultProvider)
  }

  @Test
  fun testUpdateAppLanguage_notifiesProviderWithChange() {
    forceDefaultLocale(Locale.ROOT)
    val languageProvider = translationController.getAppLanguage(PROFILE_ID_0)
    val langMonitor = monitorFactory.createMonitor(languageProvider)
    langMonitor.waitForNextSuccessResult()

    // The result must be observed immediately otherwise it won't execute (which will result in the
    // language not being updated).
    val resultProvider =
      translationController.updateAppLanguage(PROFILE_ID_0, createAppLanguageSelection(ENGLISH))
    val updateMonitor = monitorFactory.createMonitor(resultProvider)

    updateMonitor.waitForNextSuccessResult()
    langMonitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testUpdateAppLanguage_getAppLanguage_returnsUpdatedLanguage() {
    forceDefaultLocale(Locale.ROOT)

    val resultProvider = translationController.updateAppLanguage(
      PROFILE_ID_0, createAppLanguageSelection(BRAZILIAN_PORTUGUESE)
    )
    val updateMonitor = monitorFactory.createMonitor(resultProvider)
    updateMonitor.waitForNextSuccessResult()

    val languageProvider = translationController.getAppLanguage(PROFILE_ID_0)
    val langMonitor = monitorFactory.createMonitor(languageProvider)
    val getAppLanguageResults = langMonitor.waitForNextSuccessResult()

    assertThat(getAppLanguageResults).isEqualTo(BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testGetAppLanguage_uninitialized_returnsSystemLanguage() {
    forceDefaultLocale(Locale.ROOT)

    val languageProvider = translationController.getAppLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(LANGUAGE_UNSPECIFIED)
  }

  @Test
  fun testGetAppLanguage_updateLanguageToEnglish_returnsEnglish() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)

    val languageProvider = translationController.getAppLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAppLanguage_updateLanguageToHindi_returnsHindi() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)

    val languageProvider = translationController.getAppLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(HINDI)
  }

  @Test
  fun testGetAppLanguage_updateLanguageToUseSystem_returnsSystemLanguage() {
    forceDefaultLocale(Locale.ENGLISH)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)

    val languageProvider = translationController.getAppLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAppLanguage_useSystemLang_updateLocale_doesNotNotifyProviderWithNewLanguage() {
    forceDefaultLocale(Locale.ENGLISH)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    val languageProvider = translationController.getAppLanguage(PROFILE_ID_0)
    val monitor = monitorFactory.createMonitor(languageProvider)
    monitor.waitForNextSuccessResult()

    localeController.setAsDefault(createDisplayLocaleForLanguage(HINDI), Configuration())

    // The data provider shouldn't be updated. English will continue to be reported for the data
    // provider, but the actual app strings are allowed to use Hindi per the override.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testGetAppLanguage_updateLanguageToEnglish_differentProfile_returnsDifferentLang() {
    forceDefaultLocale(Locale.ENGLISH)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)

    val languageProvider = translationController.getAppLanguage(PROFILE_ID_1)

    // English is returned since the language is being fetched for a different profile.
    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAppLanguageLocale_uninitialized_returnsLocaleWithSystemLanguage() {
    forceDefaultLocale(Locale.ROOT)

    val localeProvider = translationController.getAppLanguageLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    val appStringId = context.languageDefinition.appStringId
    assertThat(context.usageMode).isEqualTo(APP_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(LANGUAGE_UNSPECIFIED)
    assertThat(appStringId.languageTypeCase).isEqualTo(LANGUAGETYPE_NOT_SET)
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
  }

  @Test
  fun testGetAppLanguageLocale_ptBrDefLocale_returnsLocaleWithIetfAndAndroidResourcesLangIds() {
    forceDefaultLocale(BRAZIL_PORTUGUESE_LOCALE)

    val localeProvider = translationController.getAppLanguageLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    val appStringId = context.languageDefinition.appStringId
    assertThat(context.usageMode).isEqualTo(APP_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
    assertThat(appStringId.languageTypeCase).isEqualTo(IETF_BCP47_ID)
    assertThat(appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("pt-BR")
    assertThat(appStringId.androidResourcesLanguageId.languageCode).isEqualTo("pt")
    assertThat(appStringId.androidResourcesLanguageId.regionCode).isEqualTo("BR")
    assertThat(context.regionDefinition.region).isEqualTo(BRAZIL)
  }

  @Test
  fun testGetAppLanguageLocale_updateLanguageToEnglish_returnsEnglishLocale() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)

    val localeProvider = translationController.getAppLanguageLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(APP_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    // This region comes from the default locale.
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
  }

  @Test
  fun testGetAppLanguageLocale_updateLanguageToPortuguese_returnsPortugueseLocale() {
    forceDefaultLocale(BRAZIL_ENGLISH_LOCALE)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)

    val localeProvider = translationController.getAppLanguageLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(APP_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
    // This region comes from the default locale.
    assertThat(context.regionDefinition.region).isEqualTo(BRAZIL)
  }

  @Test
  fun testGetAppLanguageLocale_updateLanguageToKiswahili_returnsKiswahiliLocale() {
    forceDefaultLocale(KENYA_KISWAHILI_LOCALE)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, SWAHILI)

    val localeProvider = translationController.getAppLanguageLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(APP_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(SWAHILI)
    // This region comes from the default locale.
    assertThat(context.regionDefinition.region).isEqualTo(OppiaRegion.KENYA)
  }

  @Test
  fun testGetAppLanguageLocale_updateLanguagePerProfile_returnsLanguageForProfile() {
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, SWAHILI)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_1, BRAZILIAN_PORTUGUESE)

    val localeProviderOne = translationController.getAppLanguageLocale(PROFILE_ID_0)
    val localeProviderTwo = translationController.getAppLanguageLocale(PROFILE_ID_1)

    val localeOne = monitorFactory.waitForNextSuccessfulResult(localeProviderOne)
    val localeTwo = monitorFactory.waitForNextSuccessfulResult(localeProviderTwo)
    val contextOne = localeOne.localeContext
    val contextTwo = localeTwo.localeContext

    assertThat(contextOne.languageDefinition.language).isEqualTo(SWAHILI)
    assertThat(contextTwo.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testGetAppLanguageLocale_updateLanguageToUseSystem_returnsSystemLanguageLocale() {
    forceDefaultLocale(Locale.ENGLISH)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)

    val localeProvider = translationController.getAppLanguageLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(APP_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAppLanguageLocale_useSystemLang_updateLocale_doesNotNotifyProviderWithNewLocale() {
    forceDefaultLocale(Locale.ENGLISH)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    val localeProvider = translationController.getAppLanguageLocale(PROFILE_ID_0)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    localeController.setAsDefault(createDisplayLocaleForLanguage(HINDI), Configuration())

    // The data provider shouldn't be updated. English will continue to be reported for the data
    // provider, but the actual app strings are allowed to use Hindi per the override.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testGetAppLanguageLocale_updateLangToEnglish_differentProfile_returnsDifferentLocale() {
    forceDefaultLocale(Locale.ENGLISH)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)

    val localeProvider = translationController.getAppLanguageLocale(PROFILE_ID_1)

    // English is returned since the language is being fetched for a different profile.
    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(APP_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAppLanguageSelection_uninitialized_returnsDefaultSelection() {
    forceDefaultLocale(Locale.ROOT)

    val selectionProvider = translationController.getAppLanguageSelection(PROFILE_ID_0)

    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection).isEqualToDefaultInstance()
  }

  @Test
  fun testGetAppLanguageSelection_updateLanguageToEnglish_returnsEnglishSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)

    val selectionProvider = translationController.getAppLanguageSelection(PROFILE_ID_0)

    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(SELECTED_APP_LANGUAGE)
    assertThat(selection.selectedLanguage).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAppLanguageSelection_updateLanguageToPortuguese_returnsPortugueseSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)

    val selectionProvider = translationController.getAppLanguageSelection(PROFILE_ID_0)

    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(SELECTED_APP_LANGUAGE)
    assertThat(selection.selectedLanguage).isEqualTo(BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testGetAppLanguageSelection_updateLanguageToUseSystem_returnsSystemSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)

    val selectionProvider = translationController.getAppLanguageSelection(PROFILE_ID_0)

    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(USE_SYSTEM_LANGUAGE_OR_APP_DEFAULT)
  }

  @Test
  fun testGetAppLanguageSelection_useSystemLang_updateLocale_doesNotNotifyProviderWithNewLocale() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    val selectionProvider = translationController.getAppLanguageSelection(PROFILE_ID_0)
    val monitor = monitorFactory.createMonitor(selectionProvider)
    monitor.waitForNextSuccessResult()

    localeController.setAsDefault(createDisplayLocaleForLanguage(HINDI), Configuration())

    // The data provider shouldn't be updated. English will continue to be reported for the data
    // provider, but the actual app strings are allowed to use Hindi per the override.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testGetAppLanguageSelection_updateLangToEnglish_differentProfile_returnsDifferentSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_1, ENGLISH)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)

    val selectionProvider = translationController.getAppLanguageSelection(PROFILE_ID_1)

    // English is returned since the selection is being fetched for a different profile.
    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(SELECTED_APP_LANGUAGE)
    assertThat(selection.selectedLanguage).isEqualTo(ENGLISH)
  }

  @Test
  fun testUpdateAppLanguage_uninitializedToSystem_returnsUninitialized() {
    forceDefaultLocale(Locale.ROOT)

    val languageSelection = AppLanguageSelection.newBuilder().apply {
      useSystemLanguageOrAppDefault = true
    }.build()
    val updateProvider = translationController.updateAppLanguage(
      PROFILE_ID_0,
      languageSelection
    )

    // The previous selection was uninitialized.
    val selection = monitorFactory.waitForNextSuccessfulResult(updateProvider)
    assertThat(selection).isEqualToDefaultInstance()
  }

  @Test
  fun testUpdateAppLanguage_uninitializedToEnglish_returnsUninitialized() {
    forceDefaultLocale(Locale.ROOT)

    val expectedLanguageSelection = AppLanguageSelection.newBuilder().apply {
      selectedLanguage = ENGLISH
    }.build()
    val updateProvider = translationController.updateAppLanguage(
      PROFILE_ID_0,
      expectedLanguageSelection
    )

    // The previous selection was uninitialized.
    val selection = monitorFactory.waitForNextSuccessfulResult(updateProvider)
    assertThat(selection).isEqualToDefaultInstance()
  }

  @Test
  fun testUpdateAppLanguage_systemToEnglish_returnsSystemSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)

    val updateProvider = translationController.updateAppLanguage(
      PROFILE_ID_0,
      AppLanguageSelection.newBuilder().apply { selectedLanguage = ENGLISH }.build()
    )

    // The previous selection was system language.
    val selection = monitorFactory.waitForNextSuccessfulResult(updateProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(USE_SYSTEM_LANGUAGE_OR_APP_DEFAULT)
  }

  @Test
  fun testUpdateAppLanguage_englishToPortuguese_returnsEnglishSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)

    val updateProvider = translationController.updateAppLanguage(
      PROFILE_ID_0,
      AppLanguageSelection.newBuilder().apply { selectedLanguage = BRAZILIAN_PORTUGUESE }.build()
    )

    // The previous selection was English.
    val selection = monitorFactory.waitForNextSuccessfulResult(updateProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(SELECTED_APP_LANGUAGE)
    assertThat(selection.selectedLanguage).isEqualTo(ENGLISH)
  }

  /* Tests for written translation content functions */

  @Test
  fun testUpdateWrittenContentLanguage_returnsSuccess() {
    forceDefaultLocale(Locale.ROOT)

    val resultProvider =
      translationController.updateWrittenTranslationContentLanguage(
        PROFILE_ID_0, createWrittenTranslationLanguageSelection(ENGLISH)
      )

    monitorFactory.waitForNextSuccessfulResult(resultProvider)
  }

  @Test
  fun testUpdateWrittenContentLanguage_notifiesProviderWithChange() {
    forceDefaultLocale(Locale.US)
    val languageProvider = translationController.getWrittenTranslationContentLanguage(PROFILE_ID_0)
    val langMonitor = monitorFactory.createMonitor(languageProvider)
    langMonitor.waitForNextSuccessResult()

    // The result must be observed immediately otherwise it won't execute (which will result in the
    // language not being updated).
    val resultProvider =
      translationController.updateWrittenTranslationContentLanguage(
        PROFILE_ID_0, createWrittenTranslationLanguageSelection(BRAZILIAN_PORTUGUESE)
      )
    val updateMonitor = monitorFactory.createMonitor(resultProvider)

    updateMonitor.waitForNextSuccessResult()
    langMonitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testGetWrittenContentLang_uninitialized_rootLocale_returnsUnspecifiedLanguage() {
    forceDefaultLocale(Locale.ROOT)

    val languageProvider = translationController.getWrittenTranslationContentLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(LANGUAGE_UNSPECIFIED)
  }

  @Test
  fun testGetWrittenContentLang_uninitialized_englishLocale_returnsSystemLanguage() {
    forceDefaultLocale(Locale.US)

    val languageProvider = translationController.getWrittenTranslationContentLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetWrittenContentLang_updateLanguageToEnglish_returnsEnglish() {
    forceDefaultLocale(Locale.ROOT)
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)

    val languageProvider = translationController.getWrittenTranslationContentLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetWrittenContentLang_updateLanguageToHindi_returnsHindi() {
    forceDefaultLocale(Locale.ROOT)
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)

    val languageProvider = translationController.getWrittenTranslationContentLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(HINDI)
  }

  @Test
  fun testGetWrittenContentLang_updateLanguageToUseApp_returnsAppLanguage() {
    // First, initialize the language to Hindi before overwriting to use the app language.
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureWrittenTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    val languageProvider = translationController.getWrittenTranslationContentLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetWrittenContentLang_useAppLang_updateAppLanguage_notifiesProviderWithNewLang() {
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureWrittenTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)
    val languageProvider = translationController.getWrittenTranslationContentLanguage(PROFILE_ID_0)

    // Changing the app language should change the provided language since this provider depends on
    // the app strings language.
    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testGetWrittenContentLang_useSysLangForApp_updateLocale_doesNotNotifyProviderWithNewLang() {
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureWrittenTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    localeController.setAsDefault(createDisplayLocaleForLanguage(HINDI), Configuration())
    val languageProvider = translationController.getWrittenTranslationContentLanguage(PROFILE_ID_0)

    // Changing the locale isn't sufficient unless the system locale also changes.
    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetWrittenContentLang_updateLanguageToEnglish_differentProfile_returnsDifferentLang() {
    forceDefaultLocale(Locale.ENGLISH)
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)

    val languageProvider = translationController.getWrittenTranslationContentLanguage(PROFILE_ID_1)

    // English is returned since the language is being fetched for a different profile.
    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetWrittenContentLocale_uninitialized_rootLocale_returnsBuiltinLocale() {
    forceDefaultLocale(Locale.ROOT)

    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    val languageDefinition = context.languageDefinition
    assertThat(context.usageMode).isEqualTo(CONTENT_STRINGS)
    assertThat(languageDefinition.language).isEqualTo(LANGUAGE_UNSPECIFIED)
    assertThat(languageDefinition.contentStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("builtin")
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
  }

  @Test
  fun testGetWrittenContentLocale_uninitialized_englishLocale_returnsLocaleWithSystemLanguage() {
    forceDefaultLocale(Locale.US)

    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(CONTENT_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    assertThat(context.regionDefinition.region).isEqualTo(UNITED_STATES)
  }

  @Test
  fun testGetWrittenContentLocale_updateLanguageToEnglish_returnsEnglishLocale() {
    forceDefaultLocale(Locale.ROOT)
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)

    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(CONTENT_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    // This region comes from the default locale.
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
  }

  @Test
  fun testGetWrittenContentLocale_updateLanguageToPortuguese_returnsPortugueseLocale() {
    forceDefaultLocale(BRAZIL_ENGLISH_LOCALE)
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)

    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(CONTENT_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
    // This region comes from the default locale.
    assertThat(context.regionDefinition.region).isEqualTo(BRAZIL)
  }

  @Test
  fun testGetWrittenContentLocale_updateLanguageToUseApp_returnsAppLanguage() {
    // First, initialize the language to Hindi before overwriting to use the app language.
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureWrittenTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(CONTENT_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetWrittenContentLocale_useAppLang_updateAppLanguage_notifiesProviderWithNewLang() {
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureWrittenTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)
    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)

    // Changing the app language should change the provided language since this provider depends on
    // the app strings language.
    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(CONTENT_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testGetWrittenContentLocale_useSysLangForApp_updateLocale_doesNotNotifyProviderWithNewLang() {
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureWrittenTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    localeController.setAsDefault(createDisplayLocaleForLanguage(HINDI), Configuration())
    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)

    // Changing the locale isn't sufficient unless the system locale also changes.
    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(CONTENT_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetWrittenContentLocale_updateLangToEnglish_differentProfile_returnsDifferentLocale() {
    forceDefaultLocale(Locale.ENGLISH)
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)

    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_1)

    // English is returned since the language is being fetched for a different profile.
    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(CONTENT_STRINGS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetWrittenContentSelection_uninitialized_englishLocale_returnsDefaultSelection() {
    forceDefaultLocale(Locale.ROOT)

    val selectionProvider =
      translationController.getWrittenTranslationContentLanguageSelection(PROFILE_ID_0)

    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection).isEqualToDefaultInstance()
  }

  @Test
  fun testGetWrittenContentSelection_updateLanguageToEnglish_returnsEnglishSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, ARABIC)
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)

    val selectionProvider =
      translationController.getWrittenTranslationContentLanguageSelection(PROFILE_ID_0)

    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(SELECTED_WRITTEN_LANGUAGE)
    assertThat(selection.selectedLanguage).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetWrittenContentSelection_updateLanguageToPortuguese_returnsPortugueseSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, ARABIC)
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)

    val selectionProvider =
      translationController.getWrittenTranslationContentLanguageSelection(PROFILE_ID_0)

    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(SELECTED_WRITTEN_LANGUAGE)
    assertThat(selection.selectedLanguage).isEqualTo(BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testGetWrittenContentSelection_updateLanguageToUseApp_returnsAppLangSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, ARABIC)
    ensureWrittenTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    val selectionProvider =
      translationController.getWrittenTranslationContentLanguageSelection(PROFILE_ID_0)

    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(USE_APP_WRITTEN_LANGUAGE)
  }

  @Test
  fun testGetWrittenContentSelection_useAppLang_updateAppLanguage_notifiesProviderWithNewLang() {
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureWrittenTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)
    val selectionProvider =
      translationController.getWrittenTranslationContentLanguageSelection(PROFILE_ID_0)

    // Changing the app language should change the provided selection since this provider depends on
    // the app strings language.
    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(USE_APP_WRITTEN_LANGUAGE)
  }

  @Test
  fun testGetWrittenContentSelection_useSysLangForApp_updateLocale_doesNotNotifyProvWithNewLang() {
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureWrittenTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    localeController.setAsDefault(createDisplayLocaleForLanguage(HINDI), Configuration())
    val selectionProvider =
      translationController.getWrittenTranslationContentLanguageSelection(PROFILE_ID_0)

    // Changing the locale isn't sufficient unless the system locale also changes.
    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(USE_APP_WRITTEN_LANGUAGE)
  }

  @Test
  fun testGetWrittenContentSelection_updateLangToEnglish_diffProfile_returnsDifferentSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_1, ENGLISH)
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)

    val selectionProvider =
      translationController.getWrittenTranslationContentLanguageSelection(PROFILE_ID_1)

    // English is returned since the selection is being fetched for a different profile.
    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(SELECTED_WRITTEN_LANGUAGE)
    assertThat(selection.selectedLanguage).isEqualTo(ENGLISH)
  }

  @Test
  fun testUpdateWrittenContentLanguage_uninitializedToUseApp_returnsUninitializedSelection() {
    forceDefaultLocale(Locale.ROOT)

    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      PROFILE_ID_0,
      WrittenTranslationLanguageSelection.newBuilder().apply { useAppLanguage = true }.build()
    )

    // The previous selection was uninitialized.
    val selection = monitorFactory.waitForNextSuccessfulResult(updateProvider)
    assertThat(selection).isEqualToDefaultInstance()
  }

  @Test
  fun testUpdateWrittenContentLanguage_uninitializedToEnglish_returnsUninitializedSelection() {
    forceDefaultLocale(Locale.ROOT)

    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      PROFILE_ID_0,
      WrittenTranslationLanguageSelection.newBuilder().apply { selectedLanguage = ENGLISH }.build()
    )

    // The previous selection was uninitialized.
    val selection = monitorFactory.waitForNextSuccessfulResult(updateProvider)
    assertThat(selection).isEqualToDefaultInstance()
  }

  @Test
  fun testUpdateWrittenContentLanguage_useAppToEnglish_returnsUseAppSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureWrittenTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      PROFILE_ID_0,
      WrittenTranslationLanguageSelection.newBuilder().apply { selectedLanguage = ENGLISH }.build()
    )

    // The previous selection was to use the app language.
    val selection = monitorFactory.waitForNextSuccessfulResult(updateProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(USE_APP_WRITTEN_LANGUAGE)
  }

  @Test
  fun testUpdateWrittenContentLanguage_englishToHindi_returnsEnglishSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)

    val updateProvider = translationController.updateWrittenTranslationContentLanguage(
      PROFILE_ID_0,
      WrittenTranslationLanguageSelection.newBuilder().apply { selectedLanguage = HINDI }.build()
    )

    // The previous selection was English.
    val selection = monitorFactory.waitForNextSuccessfulResult(updateProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(SELECTED_WRITTEN_LANGUAGE)
    assertThat(selection.selectedLanguage).isEqualTo(ENGLISH)
  }

  /* Tests for audio translation content functions */

  @Test
  fun testUpdateAudioLanguage_returnsSuccess() {
    forceDefaultLocale(Locale.ROOT)

    val resultProvider =
      translationController.updateAudioTranslationContentLanguage(
        PROFILE_ID_0, createAudioTranslationLanguageSelection(ENGLISH)
      )

    monitorFactory.waitForNextSuccessfulResult(resultProvider)
  }

  @Test
  fun testUpdateAudioLanguage_notifiesProviderWithChange() {
    forceDefaultLocale(Locale.US)
    val languageProvider = translationController.getAudioTranslationContentLanguage(PROFILE_ID_0)
    val langMonitor = monitorFactory.createMonitor(languageProvider)
    langMonitor.waitForNextSuccessResult()

    // The result must be observed immediately otherwise it won't execute (which will result in the
    // language not being updated).
    val resultProvider =
      translationController.updateAudioTranslationContentLanguage(
        PROFILE_ID_0, createAudioTranslationLanguageSelection(BRAZILIAN_PORTUGUESE)
      )
    val updateMonitor = monitorFactory.createMonitor(resultProvider)

    updateMonitor.waitForNextSuccessResult()
    langMonitor.ensureNextResultIsSuccess()
  }

  @Test
  fun testGetAudioLanguage_uninitialized_rootLocale_returnsFailure() {
    forceDefaultLocale(Locale.ROOT)

    val languageProvider = translationController.getAudioTranslationContentLanguage(PROFILE_ID_0)

    val error = monitorFactory.waitForNextFailureResult(languageProvider)
    assertThat(error).hasMessageThat().contains("doesn't match supported language definitions")
  }

  @Test
  fun testGetAudioLanguage_uninitialized_englishLocale_returnsSystemLanguage() {
    forceDefaultLocale(Locale.US)

    val languageProvider = translationController.getAudioTranslationContentLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAudioLanguage_updateLanguageToEnglish_returnsEnglish() {
    forceDefaultLocale(Locale.ROOT)
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)

    val languageProvider = translationController.getAudioTranslationContentLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAudioLanguage_updateLanguageToHindi_returnsHindi() {
    forceDefaultLocale(Locale.ROOT)
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)

    val languageProvider = translationController.getAudioTranslationContentLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(HINDI)
  }

  @Test
  fun testGetAudioLanguage_updateLanguageToUseApp_returnsAppLanguage() {
    // First, initialize the language to Hindi before overwriting to use the app language.
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureAudioTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    val languageProvider = translationController.getAudioTranslationContentLanguage(PROFILE_ID_0)

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAudioLanguage_useAppLang_updateAppLanguage_notifiesProviderWithNewLang() {
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureAudioTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)
    val languageProvider = translationController.getAudioTranslationContentLanguage(PROFILE_ID_0)

    // Changing the app language should change the provided language since this provider depends on
    // the app strings language.
    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testGetAudioLanguage_useSystemLangForApp_updateLocale_doesNotNotifyProviderWithNewLang() {
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureAudioTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    localeController.setAsDefault(createDisplayLocaleForLanguage(HINDI), Configuration())
    val languageProvider = translationController.getAudioTranslationContentLanguage(PROFILE_ID_0)

    // Changing the locale isn't sufficient unless the system locale also changes.
    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAudioLanguage_updateLanguageToEnglish_differentProfile_returnsDifferentLang() {
    forceDefaultLocale(Locale.ENGLISH)
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)

    val languageProvider = translationController.getAudioTranslationContentLanguage(PROFILE_ID_1)

    // English is returned since the language is being fetched for a different profile.
    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAudioLocale_uninitialized_rootLocale_returnsFailure() {
    forceDefaultLocale(Locale.ROOT)

    val localeProvider = translationController.getAudioTranslationContentLocale(PROFILE_ID_0)

    val error = monitorFactory.waitForNextFailureResult(localeProvider)
    assertThat(error).hasMessageThat().contains("doesn't match supported language definitions")
  }

  @Test
  fun testGetAudioLocale_uninitialized_englishLocale_returnsLocaleWithSystemLanguage() {
    forceDefaultLocale(Locale.US)

    val localeProvider = translationController.getAudioTranslationContentLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(AUDIO_TRANSLATIONS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    assertThat(context.regionDefinition.region).isEqualTo(UNITED_STATES)
  }

  @Test
  fun testGetAudioLocale_updateLanguageToEnglish_returnsEnglishLocale() {
    forceDefaultLocale(Locale.ROOT)
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)

    val localeProvider = translationController.getAudioTranslationContentLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(AUDIO_TRANSLATIONS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    // This region comes from the default locale.
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
  }

  @Test
  fun testGetAudioLocale_updateLanguageToPortuguese_returnsPortugueseLocale() {
    forceDefaultLocale(BRAZIL_ENGLISH_LOCALE)
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)

    val localeProvider = translationController.getAudioTranslationContentLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(AUDIO_TRANSLATIONS)
    assertThat(context.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
    // This region comes from the default locale.
    assertThat(context.regionDefinition.region).isEqualTo(BRAZIL)
  }

  @Test
  fun testGetAudioLocale_updateLanguageToUseApp_returnsAppLanguage() {
    // First, initialize the language to Hindi before overwriting to use the app language.
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureAudioTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    val localeProvider = translationController.getAudioTranslationContentLocale(PROFILE_ID_0)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(AUDIO_TRANSLATIONS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAudioLocale_useAppLang_updateAppLanguage_notifiesProviderWithNewLang() {
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureAudioTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)
    val localeProvider = translationController.getAudioTranslationContentLocale(PROFILE_ID_0)

    // Changing the app language should change the provided language since this provider depends on
    // the app strings language.
    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(AUDIO_TRANSLATIONS)
    assertThat(context.languageDefinition.language).isEqualTo(BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testGetAudioLocale_useSystemLangForApp_updateLocale_doesNotNotifyProviderWithNewLang() {
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureAudioTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    localeController.setAsDefault(createDisplayLocaleForLanguage(HINDI), Configuration())
    val localeProvider = translationController.getAudioTranslationContentLocale(PROFILE_ID_0)

    // Changing the locale isn't sufficient unless the system locale also changes.
    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(AUDIO_TRANSLATIONS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAudioLocale_updateLangToEnglish_differentProfile_returnsDifferentLocale() {
    forceDefaultLocale(Locale.ENGLISH)
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)

    val localeProvider = translationController.getAudioTranslationContentLocale(PROFILE_ID_1)

    // English is returned since the language is being fetched for a different profile.
    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.usageMode).isEqualTo(AUDIO_TRANSLATIONS)
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAudioContentSelection_uninitialized_englishLocale_returnsDefaultSelection() {
    forceDefaultLocale(Locale.ROOT)

    val selectionProvider =
      translationController.getAudioTranslationContentLanguageSelection(PROFILE_ID_0)

    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection).isEqualToDefaultInstance()
  }

  @Test
  fun testGetAudioContentSelection_updateLanguageToEnglish_returnsEnglishSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, ARABIC)
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)

    val selectionProvider =
      translationController.getAudioTranslationContentLanguageSelection(PROFILE_ID_0)

    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(SELECTED_AUDIO_LANGUAGE)
    assertThat(selection.selectedLanguage).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAudioContentSelection_updateLanguageToPortuguese_returnsPortugueseSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, ARABIC)
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)

    val selectionProvider =
      translationController.getAudioTranslationContentLanguageSelection(PROFILE_ID_0)

    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(SELECTED_AUDIO_LANGUAGE)
    assertThat(selection.selectedLanguage).isEqualTo(BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testGetAudioContentSelection_updateLanguageToUseApp_returnsAppLangSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, ARABIC)
    ensureAudioTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    val selectionProvider =
      translationController.getAudioTranslationContentLanguageSelection(PROFILE_ID_0)

    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(USE_APP_AUDIO_LANGUAGE)
  }

  @Test
  fun testGetAudioContentSelection_useAppLang_updateAppLanguage_notifiesProviderWithNewLang() {
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureAudioTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    ensureAppLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)
    val selectionProvider =
      translationController.getAudioTranslationContentLanguageSelection(PROFILE_ID_0)

    // Changing the app language should change the provided selection since this provider depends on
    // the app strings language.
    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(USE_APP_AUDIO_LANGUAGE)
  }

  @Test
  fun testGetAudioContentSelection_useSysLangForApp_updateLocale_doesNotNotifyProvWithNewLang() {
    ensureAppLanguageIsUpdatedToUseSystem(PROFILE_ID_0)
    forceDefaultLocale(Locale.US)
    ensureAudioTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    localeController.setAsDefault(createDisplayLocaleForLanguage(HINDI), Configuration())
    val selectionProvider =
      translationController.getAudioTranslationContentLanguageSelection(PROFILE_ID_0)

    // Changing the locale isn't sufficient unless the system locale also changes.
    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(USE_APP_AUDIO_LANGUAGE)
  }

  @Test
  fun testGetAudioContentSelection_updateLangToEnglish_diffProfile_returnsDifferentSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_1, ENGLISH)
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, HINDI)

    val selectionProvider =
      translationController.getAudioTranslationContentLanguageSelection(PROFILE_ID_1)

    // English is returned since the selection is being fetched for a different profile.
    val selection = monitorFactory.waitForNextSuccessfulResult(selectionProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(SELECTED_AUDIO_LANGUAGE)
    assertThat(selection.selectedLanguage).isEqualTo(ENGLISH)
  }

  @Test
  fun testGetAudioContentSelection_uninitializedToUseApp_returnsUninitializedSelection() {
    forceDefaultLocale(Locale.ROOT)

    val updateProvider = translationController.updateAudioTranslationContentLanguage(
      PROFILE_ID_0,
      AudioTranslationLanguageSelection.newBuilder().apply { useAppLanguage = true }.build()
    )

    // The previous selection was uninitialized.
    val selection = monitorFactory.waitForNextSuccessfulResult(updateProvider)
    assertThat(selection).isEqualToDefaultInstance()
  }

  @Test
  fun testGetAudioContentSelection_uninitializedToEnglish_returnsUninitializedSelection() {
    forceDefaultLocale(Locale.ROOT)

    val updateProvider = translationController.updateAudioTranslationContentLanguage(
      PROFILE_ID_0,
      AudioTranslationLanguageSelection.newBuilder().apply { selectedLanguage = ENGLISH }.build()
    )

    // The previous selection was uninitialized.
    val selection = monitorFactory.waitForNextSuccessfulResult(updateProvider)
    assertThat(selection).isEqualToDefaultInstance()
  }

  @Test
  fun testGetAudioContentSelection_useAppToEnglish_returnsUseAppSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAudioTranslationsLanguageIsUpdatedToUseApp(PROFILE_ID_0)

    val updateProvider = translationController.updateAudioTranslationContentLanguage(
      PROFILE_ID_0,
      AudioTranslationLanguageSelection.newBuilder().apply { selectedLanguage = ENGLISH }.build()
    )

    // The previous selection was to use the app language.
    val selection = monitorFactory.waitForNextSuccessfulResult(updateProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(USE_APP_AUDIO_LANGUAGE)
  }

  @Test
  fun testGetAudioContentSelection_englishToHindi_returnsEnglishSelection() {
    forceDefaultLocale(Locale.ROOT)
    ensureAudioTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)

    val updateProvider = translationController.updateAudioTranslationContentLanguage(
      PROFILE_ID_0,
      AudioTranslationLanguageSelection.newBuilder().apply { selectedLanguage = HINDI }.build()
    )

    // The previous selection was English.
    val selection = monitorFactory.waitForNextSuccessfulResult(updateProvider)
    assertThat(selection.selectionTypeCase).isEqualTo(SELECTED_AUDIO_LANGUAGE)
    assertThat(selection.selectedLanguage).isEqualTo(ENGLISH)
  }

  /* Tests for string extraction functions */

  @Test
  fun testExtractString_defaultSubtitledHtml_defaultContext_returnsEmptyString() {
    val extracted =
      translationController.extractString(
        SubtitledHtml.getDefaultInstance(), WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(extracted).isEmpty()
  }

  @Test
  fun testExtractString_defaultSubtitledHtml_validContext_returnsEmptyString() {
    val context = WrittenTranslationContext.newBuilder().apply {
      putTranslations(
        "other_content_id",
        Translation.newBuilder().apply {
          html = "Translated string"
        }.build()
      )
    }.build()

    val extracted = translationController.extractString(SubtitledHtml.getDefaultInstance(), context)

    assertThat(extracted).isEmpty()
  }

  @Test
  fun testExtractString_subtitledHtml_defaultContext_returnsUntranslatedHtml() {
    val subtitledHtml = SubtitledHtml.newBuilder().apply {
      contentId = "content_id"
      html = "default html"
    }.build()

    val extracted =
      translationController.extractString(
        subtitledHtml, WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(extracted).isEqualTo("default html")
  }

  @Test
  fun testExtractString_subtitledHtml_validContext_missingContentId_returnsUntranslatedHtml() {
    val subtitledHtml = SubtitledHtml.newBuilder().apply {
      contentId = "content_id"
      html = "default html"
    }.build()
    val context = WrittenTranslationContext.newBuilder().apply {
      putTranslations(
        "other_content_id",
        Translation.newBuilder().apply {
          html = "Translated string"
        }.build()
      )
    }.build()

    val extracted = translationController.extractString(subtitledHtml, context)

    // The content ID doesn't match in the context.
    assertThat(extracted).isEqualTo("default html")
  }

  @Test
  fun testExtractString_subtitledHtml_validContext_includesContentId_returnsTranslatedHtml() {
    val subtitledHtml = SubtitledHtml.newBuilder().apply {
      contentId = "content_id"
      html = "default html"
    }.build()
    val context = WrittenTranslationContext.newBuilder().apply {
      putTranslations(
        "content_id",
        Translation.newBuilder().apply {
          html = "Translated string"
        }.build()
      )
    }.build()

    val extracted = translationController.extractString(subtitledHtml, context)

    // The context ID does match, so the matching string is extracted.
    assertThat(extracted).isEqualTo("Translated string")
  }

  @Test
  fun testExtractString_defaultSubtitledUnicode_defaultContext_returnsEmptyString() {
    val extracted =
      translationController.extractString(
        SubtitledUnicode.getDefaultInstance(), WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(extracted).isEmpty()
  }

  @Test
  fun testExtractString_defaultSubtitledUnicode_validContext_returnsEmptyString() {
    val context = WrittenTranslationContext.newBuilder().apply {
      putTranslations(
        "other_content_id",
        Translation.newBuilder().apply {
          html = "Translated string"
        }.build()
      )
    }.build()

    val extracted =
      translationController.extractString(SubtitledUnicode.getDefaultInstance(), context)

    assertThat(extracted).isEmpty()
  }

  @Test
  fun testExtractString_subtitledUnicode_defaultContext_returnsUntranslatedUnicode() {
    val subtitledUnicode = SubtitledUnicode.newBuilder().apply {
      contentId = "content_id"
      unicodeStr = "default str"
    }.build()

    val extracted =
      translationController.extractString(
        subtitledUnicode, WrittenTranslationContext.getDefaultInstance()
      )

    assertThat(extracted).isEqualTo("default str")
  }

  @Test
  fun testExtractString_subtitledUnicode_validContext_missingContentId_returnsUnxlatedUnicode() {
    val subtitledUnicode = SubtitledUnicode.newBuilder().apply {
      contentId = "content_id"
      unicodeStr = "default str"
    }.build()
    val context = WrittenTranslationContext.newBuilder().apply {
      putTranslations(
        "other_content_id",
        Translation.newBuilder().apply {
          html = "Translated string"
        }.build()
      )
    }.build()

    val extracted = translationController.extractString(subtitledUnicode, context)

    // The content ID doesn't match in the context.
    assertThat(extracted).isEqualTo("default str")
  }

  @Test
  fun testExtractString_subtitledUnicode_validContext_includesContentId_returnsTranslatedUnicode() {
    val subtitledUnicode = SubtitledUnicode.newBuilder().apply {
      contentId = "content_id"
      unicodeStr = "default str"
    }.build()
    val context = WrittenTranslationContext.newBuilder().apply {
      putTranslations(
        "content_id",
        Translation.newBuilder().apply {
          html = "Translated string"
        }.build()
      )
    }.build()

    val extracted = translationController.extractString(subtitledUnicode, context)

    // The context ID does match, so the matching string is extracted.
    assertThat(extracted).isEqualTo("Translated string")
  }

  @Test
  fun testExtractStringList_defaultSet_defaultContext_returnsEmptyList() {
    val stringList = TranslatableSetOfNormalizedString.getDefaultInstance()
    val context = WrittenTranslationContext.getDefaultInstance()

    val extracted = translationController.extractStringList(stringList, context)

    assertThat(extracted).isEmpty()
  }

  @Test
  fun testExtractStringList_defaultSet_validContext_returnsEmptyList() {
    val stringList = TranslatableSetOfNormalizedString.getDefaultInstance()
    val context = WrittenTranslationContext.newBuilder().apply {
      putTranslations(
        "content_id",
        Translation.newBuilder().apply {
          htmlList = HtmlTranslationList.newBuilder().apply {
            addHtml("First translated string")
            addHtml("Second translated string")
          }.build()
        }.build()
      )
    }.build()

    val extracted = translationController.extractStringList(stringList, context)

    assertThat(extracted).isEmpty()
  }

  @Test
  fun testExtractStringList_defaultContext_returnsUntranslatedList() {
    val stringList = TranslatableSetOfNormalizedString.newBuilder().apply {
      contentId = "content_id"
      addAllNormalizedStrings(listOf("First string", "Second string"))
    }.build()
    val context = WrittenTranslationContext.getDefaultInstance()

    val extracted = translationController.extractStringList(stringList, context)

    assertThat(extracted).containsExactly("First string", "Second string")
  }

  @Test
  fun testExtractStringList_validContext_emptyList_returnsTranslatedList() {
    val stringList = TranslatableSetOfNormalizedString.newBuilder().apply {
      contentId = "content_id"
    }.build()
    val context = WrittenTranslationContext.newBuilder().apply {
      putTranslations(
        "content_id",
        Translation.newBuilder().apply {
          htmlList = HtmlTranslationList.newBuilder().apply {
            addHtml("First translated string")
            addHtml("Second translated string")
          }.build()
        }.build()
      )
    }.build()

    val extracted = translationController.extractStringList(stringList, context)

    // The translated strings are returned since there's a match, even though the original list is
    // empty.
    assertThat(extracted).containsExactly("First translated string", "Second translated string")
  }

  @Test
  fun testExtractStringList_validContext_doesNotMatchContentId_returnsUntranslatedList() {
    val stringList = TranslatableSetOfNormalizedString.newBuilder().apply {
      contentId = "content_id"
      addAllNormalizedStrings(listOf("First string", "Second string"))
    }.build()
    val context = WrittenTranslationContext.newBuilder().apply {
      putTranslations(
        "different_content_id",
        Translation.newBuilder().apply {
          htmlList = HtmlTranslationList.newBuilder().apply {
            addHtml("First translated string")
            addHtml("Second translated string")
          }.build()
        }.build()
      )
    }.build()

    val extracted = translationController.extractStringList(stringList, context)

    assertThat(extracted).containsExactly("First string", "Second string")
  }

  @Test
  fun testExtractStringList_validContext_matchesContentId_returnsTranslatedList() {
    val stringList = TranslatableSetOfNormalizedString.newBuilder().apply {
      contentId = "content_id"
      addAllNormalizedStrings(listOf("First string", "Second string"))
    }.build()
    val context = WrittenTranslationContext.newBuilder().apply {
      putTranslations(
        "content_id",
        Translation.newBuilder().apply {
          htmlList = HtmlTranslationList.newBuilder().apply {
            addHtml("First translated string")
            addHtml("Second translated string")
          }.build()
        }.build()
      )
    }.build()

    val extracted = translationController.extractStringList(stringList, context)

    assertThat(extracted).containsExactly("First translated string", "Second translated string")
  }

  @Test
  fun testExtractStringList_validContextWithoutList_matchesContentId_returnsUntranslatedList() {
    val stringList = TranslatableSetOfNormalizedString.newBuilder().apply {
      contentId = "content_id"
      addNormalizedStrings("First string")
    }.build()
    val context = WrittenTranslationContext.newBuilder().apply {
      putTranslations(
        "content_id",
        Translation.newBuilder().apply {
          html = "First translated string"
        }.build()
      )
    }.build()

    val extracted = translationController.extractStringList(stringList, context)

    // Even though the translation matches, a single HTML entry can't be matched against an expected
    // list.
    assertThat(extracted).containsExactly("First string")
  }

  @Test
  fun testComputeTranslationContext_englishLocale_emptyMap_returnsContextWithEngAndNoXlations() {
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)
    val writtenTranslationsMap = mapOf<String, TranslationMapping>()
    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)
    val contentLocale = monitorFactory.waitForNextSuccessfulResult(localeProvider)

    val translationContext =
      translationController.computeWrittenTranslationContext(writtenTranslationsMap, contentLocale)

    val expectedContext = WrittenTranslationContext.newBuilder().apply {
      language = ENGLISH
    }.build()
    assertThat(translationContext).isEqualTo(expectedContext)
  }

  @Test
  fun testComputeTranslationContext_englishLocale_returnsContextWithEnglishAndNoTranslations() {
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, ENGLISH)
    val writtenTranslationsMap = TEST_TRANSLATION_MAPPING_MULTIPLE_LANGUAGES
    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)
    val contentLocale = monitorFactory.waitForNextSuccessfulResult(localeProvider)

    val translationContext =
      translationController.computeWrittenTranslationContext(writtenTranslationsMap, contentLocale)

    val expectedContext = WrittenTranslationContext.newBuilder().apply {
      language = ENGLISH
    }.build()
    assertThat(translationContext).isEqualTo(expectedContext)
  }

  @Test
  fun testComputeTranslationContext_defaultMismatchedLocale_returnsContextWithEngAndNoXlations() {
    val writtenTranslationsMap = TEST_TRANSLATION_MAPPING_MULTIPLE_LANGUAGES
    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)
    val contentLocale = monitorFactory.waitForNextSuccessfulResult(localeProvider)

    val translationContext =
      translationController.computeWrittenTranslationContext(writtenTranslationsMap, contentLocale)

    val expectedContext = WrittenTranslationContext.newBuilder().apply {
      language = ENGLISH
    }.build()
    assertThat(translationContext).isEqualTo(expectedContext)
  }

  @Test
  fun testComputeTranslationContext_arabicLocale_emptyXlationsMap_returnsArabicContextNoXlations() {
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, ARABIC)
    val writtenTranslationsWithoutArabicMap = createTranslationMappingWithout("ar")
    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)
    val contentLocale = monitorFactory.waitForNextSuccessfulResult(localeProvider)

    val translationContext =
      translationController.computeWrittenTranslationContext(
        writtenTranslationsWithoutArabicMap, contentLocale
      )

    val expectedContext = WrittenTranslationContext.newBuilder().apply {
      language = ARABIC
    }.build()
    assertThat(translationContext).isEqualTo(expectedContext)
  }

  @Test
  fun testComputeTranslationContext_arabicLocale_withXlations_returnsContextWithXlations() {
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, ARABIC)
    val writtenTranslationsMap = TEST_TRANSLATION_MAPPING_MULTIPLE_LANGUAGES
    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)
    val contentLocale = monitorFactory.waitForNextSuccessfulResult(localeProvider)

    val translationContext =
      translationController.computeWrittenTranslationContext(writtenTranslationsMap, contentLocale)

    val extractedTranslationMap = translationContext.translationsMap
    assertThat(extractedTranslationMap).containsKey(TEST_CONTENT_ID)
    assertThat(extractedTranslationMap[TEST_CONTENT_ID]?.html).isEqualTo(TEST_AR_TRANSLATION)
  }

  @Test
  fun testComputeTranslationContext_portugueseLocale_withXlations_returnsContextWithXlations() {
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, PORTUGUESE)
    val writtenTranslationsMap = TEST_TRANSLATION_MAPPING_MULTIPLE_LANGUAGES
    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)
    val contentLocale = monitorFactory.waitForNextSuccessfulResult(localeProvider)

    val translationContext =
      translationController.computeWrittenTranslationContext(writtenTranslationsMap, contentLocale)

    val extractedTranslationMap = translationContext.translationsMap
    assertThat(extractedTranslationMap).containsKey(TEST_CONTENT_ID)
    assertThat(extractedTranslationMap[TEST_CONTENT_ID]?.html).isEqualTo(TEST_PT_TRANSLATION)
  }

  @Test
  fun testComputeTranslationContext_brazilianPortugueseLocale_withXlations_returnsXlatedContext() {
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)
    val writtenTranslationsMap = TEST_TRANSLATION_MAPPING_MULTIPLE_LANGUAGES

    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)
    val contentLocale = monitorFactory.waitForNextSuccessfulResult(localeProvider)

    val translationContext =
      translationController.computeWrittenTranslationContext(writtenTranslationsMap, contentLocale)

    val extractedTranslationMap = translationContext.translationsMap
    assertThat(extractedTranslationMap).containsKey(TEST_CONTENT_ID)
    assertThat(extractedTranslationMap[TEST_CONTENT_ID]?.html).isEqualTo(TEST_PT_BR_TRANSLATION)
  }

  @Test
  fun testComputeTranslationContext_brazilianPortugueseLocale_ptXlations_returnsCorrectContext() {
    ensureWrittenTranslationsLanguageIsUpdatedTo(PROFILE_ID_0, BRAZILIAN_PORTUGUESE)
    val writtenTranslationsWithoutBrazilianPortugueseMap = createTranslationMappingWithout("pt-BR")

    val localeProvider = translationController.getWrittenTranslationContentLocale(PROFILE_ID_0)
    val contentLocale = monitorFactory.waitForNextSuccessfulResult(localeProvider)

    val translationContext =
      translationController.computeWrittenTranslationContext(
        writtenTranslationsWithoutBrazilianPortugueseMap, contentLocale
      )

    // Without Brazilian Portuguese translations, the context should fall back to Portuguese.
    val extractedTranslationMap = translationContext.translationsMap
    assertThat(extractedTranslationMap).containsKey(TEST_CONTENT_ID)
    assertThat(extractedTranslationMap[TEST_CONTENT_ID]?.html).isEqualTo(TEST_PT_TRANSLATION)
  }

  @Test
  fun testLoadAvailableLanguageDefinitions_returnsAvailableLanguageDefinitions() {
    val languageListProvider = translationController.getSupportedAppLanguages()
    val languageListData = monitorFactory.waitForNextSuccessfulResult(languageListProvider)

    // All developer languages should be available. This is a change detector test to ensure that
    // the language selection system provides exactly the list of intended languages.
    assertThat(languageListData)
      .containsExactly(ARABIC, ENGLISH, HINDI, BRAZILIAN_PORTUGUESE, SWAHILI, NIGERIAN_PIDGIN)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun createDisplayLocaleForLanguage(language: OppiaLanguage): OppiaLocale.DisplayLocale {
    val localeProvider = localeController.retrieveAppStringDisplayLocale(language)
    return monitorFactory.waitForNextSuccessfulResult(localeProvider)
  }

  private fun ensureAppLanguageIsUpdatedToUseSystem(profileId: ProfileId) {
    val resultProvider =
      translationController.updateAppLanguage(profileId, APP_LANGUAGE_SELECTION_SYSTEM)
    monitorFactory.waitForNextSuccessfulResult(resultProvider)
  }

  private fun ensureAppLanguageIsUpdatedTo(profileId: ProfileId, language: OppiaLanguage) {
    val resultProvider =
      translationController.updateAppLanguage(profileId, createAppLanguageSelection(language))
    monitorFactory.waitForNextSuccessfulResult(resultProvider)
  }

  private fun createAppLanguageSelection(language: OppiaLanguage): AppLanguageSelection {
    return AppLanguageSelection.newBuilder().apply {
      selectedLanguage = language
    }.build()
  }

  private fun ensureWrittenTranslationsLanguageIsUpdatedTo(
    profileId: ProfileId,
    language: OppiaLanguage
  ) {
    val resultProvider =
      translationController.updateWrittenTranslationContentLanguage(
        profileId, createWrittenTranslationLanguageSelection(language)
      )
    monitorFactory.waitForNextSuccessfulResult(resultProvider)
  }

  private fun ensureWrittenTranslationsLanguageIsUpdatedToUseApp(profileId: ProfileId) {
    val resultProvider =
      translationController.updateWrittenTranslationContentLanguage(
        profileId, WRITTEN_TRANSLATION_LANGUAGE_SELECTION_APP_LANGUAGE
      )
    monitorFactory.waitForNextSuccessfulResult(resultProvider)
  }

  private fun createWrittenTranslationLanguageSelection(
    language: OppiaLanguage
  ): WrittenTranslationLanguageSelection {
    return WrittenTranslationLanguageSelection.newBuilder().apply {
      selectedLanguage = language
    }.build()
  }

  private fun ensureAudioTranslationsLanguageIsUpdatedTo(
    profileId: ProfileId,
    language: OppiaLanguage
  ) {
    val resultProvider =
      translationController.updateAudioTranslationContentLanguage(
        profileId, createAudioTranslationLanguageSelection(language)
      )
    monitorFactory.waitForNextSuccessfulResult(resultProvider)
  }

  private fun ensureAudioTranslationsLanguageIsUpdatedToUseApp(profileId: ProfileId) {
    val resultProvider =
      translationController.updateAudioTranslationContentLanguage(
        profileId, AUDIO_TRANSLATION_LANGUAGE_SELECTION_APP_LANGUAGE
      )
    monitorFactory.waitForNextSuccessfulResult(resultProvider)
  }

  private fun createAudioTranslationLanguageSelection(
    language: OppiaLanguage
  ): AudioTranslationLanguageSelection {
    return AudioTranslationLanguageSelection.newBuilder().apply {
      selectedLanguage = language
    }.build()
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, LogStorageModule::class, NetworkConnectionUtilDebugModule::class,
      TestLogReportingModule::class, LoggerModule::class, TestDispatcherModule::class,
      LocaleProdModule::class, FakeOppiaClockModule::class, RobolectricModule::class,
      AssetModule::class, LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
      PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(translationControllerTest: TranslationControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTranslationControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(translationControllerTest: TranslationControllerTest) {
      component.inject(translationControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }

  private companion object {
    private val BRAZIL_ENGLISH_LOCALE = Locale("en", "BR")
    private val BRAZIL_PORTUGUESE_LOCALE = Locale("pt", "BR")
    private val INDIA_HINDI_LOCALE = Locale("hi", "IN")
    private val KENYA_KISWAHILI_LOCALE = Locale("sw", "KE")

    private val PROFILE_ID_0 = ProfileId.newBuilder().apply {
      loggedInInternalProfileId = 0
    }.build()

    private val PROFILE_ID_1 = ProfileId.newBuilder().apply {
      loggedInInternalProfileId = 1
    }.build()

    private val APP_LANGUAGE_SELECTION_SYSTEM = AppLanguageSelection.newBuilder().apply {
      useSystemLanguageOrAppDefault = true
    }.build()

    private val WRITTEN_TRANSLATION_LANGUAGE_SELECTION_APP_LANGUAGE =
      WrittenTranslationLanguageSelection.newBuilder().apply {
        useAppLanguage = true
      }.build()

    private val AUDIO_TRANSLATION_LANGUAGE_SELECTION_APP_LANGUAGE =
      AudioTranslationLanguageSelection.newBuilder().apply {
        useAppLanguage = true
      }.build()

    private const val TEST_CONTENT_ID = "content_id"
    private const val TEST_AR_TRANSLATION = "test ar translation string"
    private const val TEST_PT_TRANSLATION = "test pt translation string"
    private const val TEST_PT_BR_TRANSLATION = "test pt-BR translation string"
    private val TEST_TRANSLATION_MAPPING_MULTIPLE_LANGUAGES =
      mapOf(
        TEST_CONTENT_ID to TranslationMapping.newBuilder().apply {
          putTranslationMapping("ar", createSingleTranslation(TEST_AR_TRANSLATION))
          // Note that this language code is intentionally capitalized to help ensure that the
          // controller can perform case-insensitive matching.
          putTranslationMapping("PT", createSingleTranslation(TEST_PT_TRANSLATION))
          putTranslationMapping("pt-BR", createSingleTranslation(TEST_PT_BR_TRANSLATION))
        }.build()
      )

    private fun createSingleTranslation(translation: String) = Translation.newBuilder().apply {
      html = translation
    }.build()

    private fun createTranslationMappingWithout(
      languageCode: String
    ): Map<String, TranslationMapping> {
      return TEST_TRANSLATION_MAPPING_MULTIPLE_LANGUAGES.toMutableMap().also {
        it[TEST_CONTENT_ID] = it[TEST_CONTENT_ID]?.toBuilder().apply {
          this?.removeTranslationMapping(languageCode)
        }?.build()
      }
    }
  }
}
