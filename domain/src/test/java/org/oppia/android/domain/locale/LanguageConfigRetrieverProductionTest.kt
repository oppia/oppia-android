package org.oppia.android.domain.locale

import android.app.Application
import android.content.Context
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
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.app.model.SupportedLanguages
import org.oppia.android.app.model.SupportedRegions
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [LanguageConfigRetriever] that is restricted to only production-supported languages &
 * regions.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class LanguageConfigRetrieverProductionTest {
  @Inject lateinit var languageConfigRetriever: LanguageConfigRetriever

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testLoadSupportedLanguages_loadsNonDefaultProtoFromAssets() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    assertThat(supportedLanguages).isNotEqualToDefaultInstance()
  }

  @Test
  fun testLoadSupportedLanguages_hasThreeSupportedLanguages() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    // Change detector test to ensure changes to the configuration are reflected in tests since
    // changes to the configuration can have a major impact on the app (and may require additional
    // work to be done to support the changes).
    assertThat(supportedLanguages.languageDefinitionsCount).isEqualTo(3)
  }

  @Test
  fun testLoadSupportedLanguages_arabic_isNotSupported() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val allLanguages = supportedLanguages.languageDefinitionsList.map { it.language }
    assertThat(allLanguages).doesNotContain(OppiaLanguage.ARABIC)
  }

  @Test
  fun testLoadSupportedLanguages_english_isSupportedForAppContentAudioTranslations() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val definition = supportedLanguages.lookUpLanguage(OppiaLanguage.ENGLISH)
    assertThat(definition.hasAppStringId()).isTrue()
    assertThat(definition.hasContentStringId()).isTrue()
    assertThat(definition.hasAudioTranslationId()).isTrue()
    assertThat(definition.fallbackMacroLanguage).isEqualTo(OppiaLanguage.LANGUAGE_UNSPECIFIED)
    assertThat(definition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
    assertThat(definition.appStringId.androidResourcesLanguageId.languageCode).isEqualTo("en")
    assertThat(definition.appStringId.androidResourcesLanguageId.regionCode).isEmpty()
    assertThat(definition.contentStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
    assertThat(definition.audioTranslationId.ietfBcp47Id.ietfLanguageTag).isEqualTo("en")
  }

  @Test
  fun testLoadSupportedLanguages_hindi_isNotSupported() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val allLanguages = supportedLanguages.languageDefinitionsList.map { it.language }
    assertThat(allLanguages).doesNotContain(OppiaLanguage.HINDI)
  }

  @Test
  fun testLoadSupportedLanguages_hinglish_isNotSupported() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val allLanguages = supportedLanguages.languageDefinitionsList.map { it.language }
    assertThat(allLanguages).doesNotContain(OppiaLanguage.HINGLISH)
  }

  @Test
  fun testLoadSupportedLanguages_portuguese_hasOnlyContentStringSupport() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val definition = supportedLanguages.lookUpLanguage(OppiaLanguage.PORTUGUESE)
    assertThat(definition.hasAppStringId()).isFalse()
    assertThat(definition.hasContentStringId()).isTrue()
    assertThat(definition.hasAudioTranslationId()).isFalse()
    assertThat(definition.fallbackMacroLanguage).isEqualTo(OppiaLanguage.LANGUAGE_UNSPECIFIED)
  }

  @Test
  fun testLoadSupportedLangs_brazilianPortuguese_supportsAppContentAudioTranslationsWithFallback() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val definition = supportedLanguages.lookUpLanguage(OppiaLanguage.BRAZILIAN_PORTUGUESE)
    assertThat(definition.hasAppStringId()).isTrue()
    assertThat(definition.hasContentStringId()).isTrue()
    assertThat(definition.hasAudioTranslationId()).isTrue()
    assertThat(definition.fallbackMacroLanguage).isEqualTo(OppiaLanguage.PORTUGUESE)
    assertThat(definition.appStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("pt-BR")
    assertThat(definition.appStringId.androidResourcesLanguageId.languageCode).isEqualTo("pt")
    assertThat(definition.appStringId.androidResourcesLanguageId.regionCode).isEqualTo("BR")
    assertThat(definition.contentStringId.ietfBcp47Id.ietfLanguageTag).isEqualTo("pt-BR")
    assertThat(definition.audioTranslationId.ietfBcp47Id.ietfLanguageTag).isEqualTo("pt-BR")
  }

  @Test
  fun testLoadSupportedLangs_swahili_isNotSupported() {
    val supportedLanguages = languageConfigRetriever.loadSupportedLanguages()

    val allLanguages = supportedLanguages.languageDefinitionsList.map { it.language }
    assertThat(allLanguages).doesNotContain(OppiaLanguage.SWAHILI)
  }

  @Test
  fun testLoadSupportedRegions_loadsNonDefaultProtoFromAssets() {
    val supportedRegions = languageConfigRetriever.loadSupportedRegions()

    assertThat(supportedRegions).isNotEqualToDefaultInstance()
  }

  @Test
  fun testLoadSupportedRegions_hasThreeSupportedRegions() {
    val supportedRegions = languageConfigRetriever.loadSupportedRegions()

    // Change detector test to ensure changes to the configuration are reflected in tests since
    // changes to the configuration can have a major impact on the app (and may require additional
    // work to be done to support the changes).
    assertThat(supportedRegions.regionDefinitionsCount).isEqualTo(3)
  }

  @Test
  fun testLoadSupportedRegions_brazil_hasCorrectRegionIdAndSupportedLanguages() {
    val supportedRegions = languageConfigRetriever.loadSupportedRegions()

    val definition = supportedRegions.lookUpRegion(OppiaRegion.BRAZIL)
    assertThat(definition.regionId.ietfRegionTag).isEqualTo("BR")
    assertThat(definition.languagesList)
      .containsExactly(OppiaLanguage.PORTUGUESE, OppiaLanguage.BRAZILIAN_PORTUGUESE)
  }

  @Test
  fun testLoadSupportedRegions_india_isNotSupported() {
    val supportedRegions = languageConfigRetriever.loadSupportedRegions()

    val allRegions = supportedRegions.regionDefinitionsList.map { it.region }
    assertThat(allRegions).doesNotContain(OppiaRegion.INDIA)
  }

  @Test
  fun testLoadSupportedRegions_unitedStates_hasCorrectRegionIdAndSupportedLanguages() {
    val supportedRegions = languageConfigRetriever.loadSupportedRegions()

    val definition = supportedRegions.lookUpRegion(OppiaRegion.UNITED_STATES)
    assertThat(definition.regionId.ietfRegionTag).isEqualTo("US")
    assertThat(definition.languagesList).containsExactly(OppiaLanguage.ENGLISH)
  }

  @Test
  fun testLoadSupportedRegions_kenya_hasCorrectRegionIdAndSupportedLanguages() {
    val supportedRegions = languageConfigRetriever.loadSupportedRegions()

    val definition = supportedRegions.lookUpRegion(OppiaRegion.KENYA)
    assertThat(definition.regionId.ietfRegionTag).isEqualTo("KE")
    assertThat(definition.languagesList).containsExactly(OppiaLanguage.ENGLISH)
  }

  private fun SupportedLanguages.lookUpLanguage(
    language: OppiaLanguage
  ): LanguageSupportDefinition {
    val definition = languageDefinitionsList.find { it.language == language }
    // Sanity check.
    assertThat(definition).isNotNull()
    return definition!!
  }

  private fun SupportedRegions.lookUpRegion(region: OppiaRegion): RegionSupportDefinition {
    val definition = regionDefinitionsList.find { it.region == region }
    // Sanity check.
    assertThat(definition).isNotNull()
    return definition!!
  }

  private fun setUpTestApplicationComponent() {
    DaggerLanguageConfigRetrieverProductionTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
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
      TestModule::class, LoggerModule::class, TestDispatcherModule::class, RobolectricModule::class,
      AssetModule::class, LocaleProdModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: LanguageConfigRetrieverProductionTest)
  }
}
