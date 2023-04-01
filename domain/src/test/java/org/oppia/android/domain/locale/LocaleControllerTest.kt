package org.oppia.android.domain.locale

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.app.model.OppiaLanguage.ENGLISH
import org.oppia.android.app.model.OppiaLanguage.HINDI
import org.oppia.android.app.model.OppiaLanguage.HINGLISH
import org.oppia.android.app.model.OppiaLanguage.LANGUAGE_UNSPECIFIED
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS
import org.oppia.android.app.model.OppiaRegion.INDIA
import org.oppia.android.app.model.OppiaRegion.REGION_UNSPECIFIED
import org.oppia.android.app.model.OppiaRegion.UNITED_STATES
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
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
import org.robolectric.shadows.ShadowLog
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [LocaleController].
 *
 * Note that these tests depend on real locales being present in the local environment
 * (Robolectric).
 */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = LocaleControllerTest.TestApplication::class)
class LocaleControllerTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var localeController: LocaleController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var context: Context

  @Mock
  lateinit var mockDisplayLocale: OppiaLocale.DisplayLocale

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  /* Tests for getLikelyDefaultAppStringLocaleContext & reconstituteDisplayLocale. */

  @Test
  fun testGetLikelyDefaultAppStringLocaleContext_returnsAppStringContextForEnglish() {
    val context = localeController.getLikelyDefaultAppStringLocaleContext()

    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    assertThat(context.languageDefinition.minAndroidSdkVersion).isEqualTo(1)
    assertThat(context.languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.regionDefinition.region).isEqualTo(UNITED_STATES)
    assertThat(context.regionDefinition.regionId.ietfRegionTag).isEqualTo("US")
    assertThat(context.usageMode).isEqualTo(OppiaLocaleContext.LanguageUsageMode.APP_STRINGS)
  }

  @Test
  fun testGetLikelyDefaultAppStringLocaleContext_twice_returnsSameContext() {
    val firstContext = localeController.getLikelyDefaultAppStringLocaleContext()
    val secondContext = localeController.getLikelyDefaultAppStringLocaleContext()

    assertThat(firstContext).isEqualTo(secondContext)
  }

  @Test
  fun testReconstituteDisplayLocale_defaultContext_returnsDisplayLocaleForContext() {
    val context = OppiaLocaleContext.getDefaultInstance()

    val locale = localeController.reconstituteDisplayLocale(context)

    assertThat(locale.localeContext).isEqualToDefaultInstance()
  }

  @Test
  fun testReconstituteDisplayLocale_nonDefaultContext_returnsDisplayLocaleForContext() {
    val context = localeController.getLikelyDefaultAppStringLocaleContext()

    val locale = localeController.reconstituteDisplayLocale(context)

    assertThat(locale.localeContext).isEqualTo(context)
  }

  /* Tests for retrieveAppStringDisplayLocale. */

  @Test
  fun testAppStringLocale_rootLocale_noConfigLocale_printsErrorForDefaulting() {
    context.applicationContext.resources.configuration.setLocale(null)
    Locale.setDefault(Locale.ROOT)

    monitorFactory.waitForNextSuccessfulResult(
      localeController.retrieveAppStringDisplayLocale(ENGLISH)
    )

    assertThat(retrieveLogcatLogs())
      .contains("No locales defined for application context. Defaulting to default Locale.")
  }

  @Test
  fun testAppStringLocale_rootLocale_english_hasCorrectLanguageAndFallbackDefinitions() {
    forceDefaultLocale(Locale.ROOT)

    val localeProvider = localeController.retrieveAppStringDisplayLocale(ENGLISH)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    assertThat(context.languageDefinition.fallbackMacroLanguage).isEqualTo(LANGUAGE_UNSPECIFIED)
    assertThat(context.languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
  }

  @Test
  fun testAppStringLocale_usLocale_english_hasMatchedUsRegion() {
    forceDefaultLocale(Locale.US)

    val localeProvider = localeController.retrieveAppStringDisplayLocale(ENGLISH)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    assertThat(locale.localeContext.regionDefinition.region).isEqualTo(UNITED_STATES)
  }

  @Test
  fun testAppStringLocale_monacoLocale_english_hasUnmatchedMonacoRegion() {
    forceDefaultLocale(MONACO_FRENCH_LOCALE)

    val localeProvider = localeController.retrieveAppStringDisplayLocale(ENGLISH)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    assertThat(locale.localeContext.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
  }

  @Test
  fun testAppStringLocale_indiaLocale_english_printsRegionLanguageMismatchError() {
    forceDefaultLocale(INDIA_HINDI_LOCALE)

    monitorFactory.waitForNextSuccessfulResult(
      localeController.retrieveAppStringDisplayLocale(ENGLISH)
    )

    assertThat(retrieveLogcatLogs())
      .contains(
        "Notice: selected language $ENGLISH is not part of the corresponding region matched to" +
          " this locale: $INDIA (ID: IN) (supported languages: [$HINDI, $HINGLISH]"
      )
  }

  @Test
  fun testAppStringLocale_englishUsLocale_defaultLang_returnsEnglishLocale() {
    forceDefaultLocale(Locale.US)

    val localeProvider = localeController.retrieveAppStringDisplayLocale(LANGUAGE_UNSPECIFIED)

    // English should be matched per the system locale.
    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    assertThat(context.languageDefinition.fallbackMacroLanguage).isEqualTo(LANGUAGE_UNSPECIFIED)
    assertThat(context.languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.regionDefinition.region).isEqualTo(UNITED_STATES)
  }

  @Test
  fun testAppStringLocale_englishUsLocale_defaultLang_printsError() {
    forceDefaultLocale(Locale.US)

    monitorFactory.waitForNextSuccessfulResult(
      localeController.retrieveAppStringDisplayLocale(LANGUAGE_UNSPECIFIED)
    )

    assertThat(retrieveLogcatLogs())
      .contains("Encountered unmatched language: $LANGUAGE_UNSPECIFIED")
  }

  @Test
  fun testAppStringLocale_frenchMonacoLocale_defaultLang_returnsFrenchDefaultLocale() {
    forceDefaultLocale(MONACO_FRENCH_LOCALE)

    val localeProvider = localeController.retrieveAppStringDisplayLocale(LANGUAGE_UNSPECIFIED)

    // The locale will be forced to fit Monaco & French despite not being directly supported by the
    // app.
    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    val languageDefinition = context.languageDefinition
    assertThat(languageDefinition.language).isEqualTo(LANGUAGE_UNSPECIFIED)
    assertThat(languageDefinition.fallbackMacroLanguage).isEqualTo(LANGUAGE_UNSPECIFIED)
    assertThat(languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("fr-MC")
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
    assertThat(context.regionDefinition.regionId.ietfRegionTag).isEqualTo("MC")
  }

  @Test
  fun testAppStringLocale_newSystemLocale_doesNotNotifyProvider() {
    forceDefaultLocale(Locale.US)
    val localeProvider = localeController.retrieveAppStringDisplayLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    forceDefaultLocale(Locale.CANADA)

    // Simply changing the locale shouldn't change the provider.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testAppStringLocale_notifyPotentialLocaleChange_doesNotNotifyProvider() {
    forceDefaultLocale(Locale.US)
    val localeProvider = localeController.retrieveAppStringDisplayLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    localeController.notifyPotentialLocaleChange()

    // Just notifying isn't sufficient to trigger a change in the provider.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testAppStringLocale_newSystemLocale_sameRegion_notify_doesNotNotifyProvider() {
    forceDefaultLocale(Locale.US)
    val localeProvider = localeController.retrieveAppStringDisplayLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    forceDefaultLocale(Locale.US)
    localeController.notifyPotentialLocaleChange()

    // Changing & notifying for the same locale should not change the provider.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testAppStringLocale_newSystemLocale_newRegion_notify_notifiesProvider() {
    forceDefaultLocale(Locale.US)
    val localeProvider = localeController.retrieveAppStringDisplayLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    forceDefaultLocale(Locale.CANADA)
    localeController.notifyPotentialLocaleChange()

    // Changing to a new region (but keeping the same language) should update the region.
    val locale = monitor.waitForNextSuccessResult()
    val context = locale.localeContext
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
    assertThat(context.regionDefinition.regionId.ietfRegionTag).isEqualTo("CA")
  }

  @Test
  fun testAppStringLocale_newSystemLocale_defLangMatching_notify_notifiesProvider() {
    forceDefaultLocale(Locale.ROOT)
    val localeProvider = localeController.retrieveAppStringDisplayLocale(LANGUAGE_UNSPECIFIED)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    forceDefaultLocale(INDIA_HINDI_LOCALE)
    localeController.notifyPotentialLocaleChange()

    // Changing to a matched language should update the provider result.
    val locale = monitor.waitForNextSuccessResult()
    val context = locale.localeContext
    assertThat(context.languageDefinition.language).isEqualTo(HINDI)
    assertThat(context.regionDefinition.region).isEqualTo(INDIA)
  }

  @Test
  fun testAppStringLocale_newSystemLocale_defLangUnmatching_notify_notifiesProvider() {
    forceDefaultLocale(Locale.ROOT)
    val localeProvider = localeController.retrieveAppStringDisplayLocale(LANGUAGE_UNSPECIFIED)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    forceDefaultLocale(MONACO_FRENCH_LOCALE)
    localeController.notifyPotentialLocaleChange()

    // Changing to an unmatched language should update the provider.
    val locale = monitor.waitForNextSuccessResult()
    val context = locale.localeContext
    val languageDefinition = context.languageDefinition
    assertThat(languageDefinition.language).isEqualTo(LANGUAGE_UNSPECIFIED)
    assertThat(languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("fr-MC")
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
    assertThat(context.regionDefinition.regionId.ietfRegionTag).isEqualTo("MC")
  }

  /* Tests for retrieveWrittenTranslationsLocale. */

  @Test
  fun testContentLocale_rootLocale_noConfigLocale_printsErrorForDefaulting() {
    context.applicationContext.resources.configuration.setLocale(null)
    Locale.setDefault(Locale.ROOT)

    monitorFactory.waitForNextSuccessfulResult(
      localeController.retrieveWrittenTranslationsLocale(ENGLISH)
    )

    assertThat(retrieveLogcatLogs())
      .contains("No locales defined for application context. Defaulting to default Locale.")
  }

  @Test
  fun testContentLocale_rootLocale_english_hasCorrectLanguageAndFallbackDefinitions() {
    forceDefaultLocale(Locale.ROOT)

    val localeProvider = localeController.retrieveWrittenTranslationsLocale(ENGLISH)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    assertThat(context.languageDefinition.fallbackMacroLanguage).isEqualTo(LANGUAGE_UNSPECIFIED)
    assertThat(context.languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
  }

  @Test
  fun testContentLocale_usLocale_english_hasMatchedUsRegion() {
    forceDefaultLocale(Locale.US)

    val localeProvider = localeController.retrieveWrittenTranslationsLocale(ENGLISH)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    assertThat(locale.localeContext.regionDefinition.region).isEqualTo(UNITED_STATES)
  }

  @Test
  fun testContentLocale_monacoLocale_english_hasUnmatchedMonacoRegion() {
    forceDefaultLocale(MONACO_FRENCH_LOCALE)

    val localeProvider = localeController.retrieveWrittenTranslationsLocale(ENGLISH)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    assertThat(locale.localeContext.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
  }

  @Test
  fun testContentLocale_indiaLocale_english_printsRegionLanguageMismatchError() {
    forceDefaultLocale(INDIA_HINDI_LOCALE)

    monitorFactory.waitForNextSuccessfulResult(
      localeController.retrieveWrittenTranslationsLocale(ENGLISH)
    )

    assertThat(retrieveLogcatLogs())
      .contains(
        "Notice: selected language $ENGLISH is not part of the corresponding region matched to" +
          " this locale: $INDIA (ID: IN) (supported languages: [$HINDI, $HINGLISH]"
      )
  }

  @Test
  fun testContentLocale_englishUsLocale_defaultLang_returnsDefaultBuiltin() {
    forceDefaultLocale(Locale.US)

    val localeProvider = localeController.retrieveWrittenTranslationsLocale(LANGUAGE_UNSPECIFIED)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    val languageDefinition = context.languageDefinition
    assertThat(languageDefinition.contentStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("builtin")
    assertThat(context.regionDefinition.region).isEqualTo(UNITED_STATES)
  }

  @Test
  fun testContentLocale_englishUsLocale_defaultLang_printsError() {
    forceDefaultLocale(Locale.US)

    val monitor = monitorFactory.createMonitor(
      localeController.retrieveWrittenTranslationsLocale(LANGUAGE_UNSPECIFIED)
    )
    monitor.waitForNextResult()

    assertThat(retrieveLogcatLogs())
      .contains("Encountered unmatched language: $LANGUAGE_UNSPECIFIED")
    assertThat(retrieveLogcatLogs())
      .contains("Falling back to the built-in content type due to mismatched configuration")
  }

  @Test
  fun testContentLocale_newSystemLocale_doesNotNotifyProvider() {
    forceDefaultLocale(Locale.US)
    val localeProvider = localeController.retrieveWrittenTranslationsLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    forceDefaultLocale(Locale.CANADA)

    // Simply changing the locale shouldn't change the provider.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testContentLocale_notifyPotentialLocaleChange_doesNotNotifyProvider() {
    forceDefaultLocale(Locale.US)
    val localeProvider = localeController.retrieveWrittenTranslationsLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    localeController.notifyPotentialLocaleChange()

    // Just notifying isn't sufficient to trigger a change in the provider.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testContentLocale_newSystemLocale_sameRegion_notify_doesNotNotifyProvider() {
    forceDefaultLocale(Locale.US)
    val localeProvider = localeController.retrieveWrittenTranslationsLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    forceDefaultLocale(Locale.US)
    localeController.notifyPotentialLocaleChange()

    // Changing & notifying for the same locale should not change the provider.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testContentLocale_newSystemLocale_newRegion_notify_notifiesProvider() {
    forceDefaultLocale(Locale.US)
    val localeProvider = localeController.retrieveWrittenTranslationsLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    forceDefaultLocale(Locale.CANADA)
    localeController.notifyPotentialLocaleChange()

    // Changing to a new region (but keeping the same language) should update the region.
    val locale = monitor.waitForNextSuccessResult()
    val context = locale.localeContext
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
    assertThat(context.regionDefinition.regionId.ietfRegionTag).isEqualTo("CA")
  }

  /* Tests for retrieveAudioTranslationsLocale. */

  @Test
  fun testAudioLocale_rootLocale_noConfigLocale_printsErrorForDefaulting() {
    context.applicationContext.resources.configuration.setLocale(null)
    Locale.setDefault(Locale.ROOT)

    monitorFactory.waitForNextSuccessfulResult(
      localeController.retrieveAudioTranslationsLocale(ENGLISH)
    )

    assertThat(retrieveLogcatLogs())
      .contains("No locales defined for application context. Defaulting to default Locale.")
  }

  @Test
  fun testAudioLocale_rootLocale_english_hasCorrectLanguageAndFallbackDefinitions() {
    forceDefaultLocale(Locale.ROOT)

    val localeProvider = localeController.retrieveAudioTranslationsLocale(ENGLISH)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    val context = locale.localeContext
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    assertThat(context.languageDefinition.fallbackMacroLanguage).isEqualTo(LANGUAGE_UNSPECIFIED)
    assertThat(context.languageDefinition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
    assertThat(context.hasFallbackLanguageDefinition()).isFalse()
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
  }

  @Test
  fun testAudioLocale_usLocale_english_hasMatchedUsRegion() {
    forceDefaultLocale(Locale.US)

    val localeProvider = localeController.retrieveAudioTranslationsLocale(ENGLISH)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    assertThat(locale.localeContext.regionDefinition.region).isEqualTo(UNITED_STATES)
  }

  @Test
  fun testAudioLocale_monacoLocale_english_hasUnmatchedMonacoRegion() {
    forceDefaultLocale(MONACO_FRENCH_LOCALE)

    val localeProvider = localeController.retrieveAudioTranslationsLocale(ENGLISH)

    val locale = monitorFactory.waitForNextSuccessfulResult(localeProvider)
    assertThat(locale.localeContext.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
  }

  @Test
  fun testAudioLocale_indiaLocale_english_printsRegionLanguageMismatchError() {
    forceDefaultLocale(INDIA_HINDI_LOCALE)

    monitorFactory.waitForNextSuccessfulResult(
      localeController.retrieveAudioTranslationsLocale(ENGLISH)
    )

    assertThat(retrieveLogcatLogs())
      .contains(
        "Notice: selected language $ENGLISH is not part of the corresponding region matched to" +
          " this locale: $INDIA (ID: IN) (supported languages: [$HINDI, $HINGLISH]"
      )
  }

  @Test
  fun testAudioLocale_englishUsLocale_defaultLang_returnsFailure() {
    forceDefaultLocale(Locale.US)

    val localeProvider = localeController.retrieveAudioTranslationsLocale(LANGUAGE_UNSPECIFIED)

    // English should be matched per the system locale.
    val error = monitorFactory.waitForNextFailureResult(localeProvider)
    assertThat(error)
      .hasMessageThat()
      .contains(
        "Language $LANGUAGE_UNSPECIFIED for usage $AUDIO_TRANSLATIONS doesn't match supported" +
          " language definitions"
      )
  }

  @Test
  fun testAudioLocale_englishUsLocale_defaultLang_printsError() {
    forceDefaultLocale(Locale.US)

    val monitor = monitorFactory.createMonitor(
      localeController.retrieveAudioTranslationsLocale(LANGUAGE_UNSPECIFIED)
    )
    monitor.waitForNextResult()

    assertThat(retrieveLogcatLogs())
      .contains("Encountered unmatched language: $LANGUAGE_UNSPECIFIED")
  }

  @Test
  fun testAudioLocale_newSystemLocale_doesNotNotifyProvider() {
    forceDefaultLocale(Locale.US)
    val localeProvider = localeController.retrieveAudioTranslationsLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    forceDefaultLocale(Locale.CANADA)

    // Simply changing the locale shouldn't change the provider.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testAudioLocale_notifyPotentialLocaleChange_doesNotNotifyProvider() {
    forceDefaultLocale(Locale.US)
    val localeProvider = localeController.retrieveAudioTranslationsLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    localeController.notifyPotentialLocaleChange()

    // Just notifying isn't sufficient to trigger a change in the provider.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testAudioLocale_newSystemLocale_sameRegion_notify_doesNotNotifyProvider() {
    forceDefaultLocale(Locale.US)
    val localeProvider = localeController.retrieveAudioTranslationsLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    forceDefaultLocale(Locale.US)
    localeController.notifyPotentialLocaleChange()

    // Changing & notifying for the same locale should not change the provider.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testAudioLocale_newSystemLocale_newRegion_notify_notifiesProvider() {
    forceDefaultLocale(Locale.US)
    val localeProvider = localeController.retrieveAudioTranslationsLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeProvider)
    monitor.waitForNextSuccessResult()

    forceDefaultLocale(Locale.CANADA)
    localeController.notifyPotentialLocaleChange()

    // Changing to a new region (but keeping the same language) should update the region.
    val locale = monitor.waitForNextSuccessResult()
    val context = locale.localeContext
    assertThat(context.languageDefinition.language).isEqualTo(ENGLISH)
    assertThat(context.regionDefinition.region).isEqualTo(REGION_UNSPECIFIED)
    assertThat(context.regionDefinition.regionId.ietfRegionTag).isEqualTo("CA")
  }

  /* Tests for getSystemLocaleProfile. */

  @Test
  fun testSystemLanguage_rootLocale_returnsUnspecifiedLanguage() {
    forceDefaultLocale(Locale.ROOT)

    val languageProvider = localeController.retrieveSystemLanguage()

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(LANGUAGE_UNSPECIFIED)
  }

  @Test
  fun testSystemLanguage_usEnglishLocale_returnsEnglish() {
    forceDefaultLocale(Locale.US)

    val languageProvider = localeController.retrieveSystemLanguage()

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testSystemLanguage_indiaEnglishLocale_returnsEnglish() {
    forceDefaultLocale(INDIA_ENGLISH_LOCALE)

    val languageProvider = localeController.retrieveSystemLanguage()

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testSystemLanguage_frenchLocale_returnsUnspecifiedLanguage() {
    forceDefaultLocale(Locale.FRENCH)

    val languageProvider = localeController.retrieveSystemLanguage()

    val language = monitorFactory.waitForNextSuccessfulResult(languageProvider)
    assertThat(language).isEqualTo(LANGUAGE_UNSPECIFIED)
  }

  @Test
  fun testSystemLanguage_notifyPotentialLocaleChange_doesNotNotifyProvider() {
    forceDefaultLocale(Locale.FRENCH)
    val languageProvider = localeController.retrieveSystemLanguage()
    val monitor = monitorFactory.createMonitor(languageProvider)
    monitor.waitForNextResult()

    localeController.notifyPotentialLocaleChange()

    // The provider shouldn't be updated just for a notification with no state change.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testSystemLanguage_changeLocale_doesNotNotifyProvider() {
    forceDefaultLocale(Locale.FRENCH)
    val languageProvider = localeController.retrieveSystemLanguage()
    val monitor = monitorFactory.createMonitor(languageProvider)
    monitor.waitForNextResult()

    forceDefaultLocale(Locale.ENGLISH)

    // Changing the locale isn't sufficient to update the provider without a notification (since
    // Android doesn't provide a way to monitor for locale without utilizing the configuration
    // change system).
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testSystemLanguage_englishToPortuguese_notify_notifiesProvider() {
    forceDefaultLocale(Locale.US)
    val languageProvider = localeController.retrieveSystemLanguage()
    val monitor = monitorFactory.createMonitor(languageProvider)
    monitor.waitForNextResult()

    forceDefaultLocale(BRAZIL_PORTUGUESE_LOCALE)
    localeController.notifyPotentialLocaleChange()

    // The notify + locale change should change the reported system language.
    val language = monitor.waitForNextSuccessResult()
    assertThat(language).isEqualTo(BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testSystemLanguage_englishToFrench_notify_notifiesProvider() {
    forceDefaultLocale(Locale.US)
    val languageProvider = localeController.retrieveSystemLanguage()
    val monitor = monitorFactory.createMonitor(languageProvider)
    monitor.waitForNextResult()

    forceDefaultLocale(Locale.FRENCH)
    localeController.notifyPotentialLocaleChange()

    // A known language (English) can change to an unspecified language.
    val language = monitor.waitForNextSuccessResult()
    assertThat(language).isEqualTo(LANGUAGE_UNSPECIFIED)
  }

  @Test
  fun testSystemLanguage_frenchToEnglish_notify_notifiesProvider() {
    forceDefaultLocale(Locale.FRENCH)
    val languageProvider = localeController.retrieveSystemLanguage()
    val monitor = monitorFactory.createMonitor(languageProvider)
    monitor.waitForNextResult()

    forceDefaultLocale(Locale.US)
    localeController.notifyPotentialLocaleChange()

    // An unspecified language can change to a known language (English).
    val language = monitor.waitForNextSuccessResult()
    assertThat(language).isEqualTo(ENGLISH)
  }

  @Test
  fun testSystemLanguage_frenchToGerman_notify_doesNotNotifyProvider() {
    forceDefaultLocale(Locale.FRENCH)
    val languageProvider = localeController.retrieveSystemLanguage()
    val monitor = monitorFactory.createMonitor(languageProvider)
    monitor.waitForNextResult()

    forceDefaultLocale(Locale.GERMAN)
    localeController.notifyPotentialLocaleChange()

    // Changing from one unspecified language to another shouldn't notify the provider since the
    // outcome is the same.
    monitor.verifyProviderIsNotUpdated()
  }

  /* Tests for setAsDefault. */

  @Test
  fun testSetAsDefault_customLocaleImpl_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      localeController.setAsDefault(mockDisplayLocale, Configuration())
    }

    assertThat(exception).hasMessageThat().contains("Invalid display locale type passed in")
  }

  @Test
  fun testSetAsDefault_englishDisplayLocale_changesLocaleInConfiguration() {
    forceDefaultLocale(Locale.ROOT)
    val configuration = Configuration()
    val locale = retrieveAppStringDisplayLocale(ENGLISH)
    // Sanity check (to validate the configuration was changed).
    assertThat(configuration.locales.hasLanguage("en")).isFalse()

    localeController.setAsDefault(locale, configuration)

    // Verify that the configuration's locale has changed.
    assertThat(configuration.locales.hasLanguage("en")).isTrue()
  }

  @Test
  fun testSetAsDefault_englishDisplayLocale_changesSystemDefaultLocale() {
    forceDefaultLocale(Locale.ROOT)
    val locale = retrieveAppStringDisplayLocale(ENGLISH)
    // Sanity check (to validate the system locale was changed).
    assertThat(Locale.getDefault().language).isNotEqualTo("en")

    localeController.setAsDefault(locale, Configuration())

    // Verify that the default locale has changed.
    assertThat(Locale.getDefault().language).isEqualTo("en")
  }

  @Test
  fun testSetAsDefault_englishDisplayLocale_doesNotTriggerChangeInSystemLanguageProvider() {
    forceDefaultLocale(Locale.ROOT)
    val locale = retrieveAppStringDisplayLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeController.retrieveSystemLanguage())
    // Sanity check (to validate the system language actually changes).
    assertThat(monitor.waitForNextSuccessResult()).isNotEqualTo(ENGLISH)

    localeController.setAsDefault(locale, Configuration())

    // Verify that the system language provider isn't notified since the system locale didn't
    // change.
    monitor.verifyProviderIsNotUpdated()
  }

  @Test
  fun testSetAsDefault_englishDisplayLocale_withNewSystemLocale_triggersChangeInSysLangProvider() {
    forceDefaultLocale(Locale.ROOT)
    val locale = retrieveAppStringDisplayLocale(ENGLISH)
    val monitor = monitorFactory.createMonitor(localeController.retrieveSystemLanguage())
    // Sanity check (to validate the system language actually changes).
    assertThat(monitor.waitForNextSuccessResult()).isNotEqualTo(ENGLISH)

    forceDefaultLocale(Locale.ENGLISH)
    localeController.setAsDefault(locale, Configuration())

    // Verify that the system language provider did change & was notified as part of the call to
    // setAsDefault.
    assertThat(monitor.waitForNextSuccessResult()).isEqualTo(ENGLISH)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun retrieveAppStringDisplayLocale(language: OppiaLanguage): OppiaLocale.DisplayLocale {
    val localeProvider = localeController.retrieveAppStringDisplayLocale(language)
    return monitorFactory.waitForNextSuccessfulResult(localeProvider)
  }

  private fun retrieveLogcatLogs(): List<String> = ShadowLog.getLogs().map { it.msg }

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

    fun inject(localeControllerTest: LocaleControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerLocaleControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(localeControllerTest: LocaleControllerTest) {
      component.inject(localeControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }

  private companion object {
    private val MONACO_FRENCH_LOCALE = Locale("fr", "MC")
    private val INDIA_HINDI_LOCALE = Locale("hi", "IN")
    private val INDIA_ENGLISH_LOCALE = Locale("en", "IN")
    private val BRAZIL_PORTUGUESE_LOCALE = Locale("pt", "BR")

    private fun LocaleList.toList(): List<Locale> = (0 until size()).map { this[it] }
    private fun LocaleList.hasLanguage(languageCode: String): Boolean =
      toList().any { it.language == languageCode }
  }
}
