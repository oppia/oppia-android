package org.oppia.android.util.locale

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

/** Tests for [OppiaLocaleContext] extensions. */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class OppiaLocaleContextExtensionsTest {
  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  /* Tests for getLanguageId() */

  @Test
  fun testGetLanguageId_defaultInstance_returnsDefaultInstance() {
    val localeContext = OppiaLocaleContext.getDefaultInstance()

    val languageId = localeContext.getLanguageId()

    assertThat(languageId).isEqualToDefaultInstance()
  }

  @Test
  fun testGetLanguageId_appStringUsage_noAppStringId_returnsDefaultInstance() {
    val localeContext = createContextWithoutLanguageDefinition(
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
    )

    val languageId = localeContext.getLanguageId()

    assertThat(languageId).isEqualToDefaultInstance()
  }

  @Test
  fun testGetLanguageId_appStringUsage_definedAppStringId_returnsAppStringId() {
    val localeContext = createAppStringsContext(
      language = OppiaLanguage.ENGLISH,
      appStringId = ENGLISH_LANGUAGE_ID
    )

    val languageId = localeContext.getLanguageId()

    assertThat(languageId).isEqualTo(ENGLISH_LANGUAGE_ID)
  }

  @Test
  fun testGetLanguageId_contentStringUsage_noContentStringId_returnsDefaultInstance() {
    val localeContext = createContextWithoutLanguageDefinition(
      usageMode = OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS
    )

    val languageId = localeContext.getLanguageId()

    assertThat(languageId).isEqualToDefaultInstance()
  }

  @Test
  fun testGetLanguageId_contentStringUsage_definedContentStringId_returnsContentId() {
    val localeContext = createContentStringsContext(
      language = OppiaLanguage.ENGLISH,
      contentStringId = ENGLISH_LANGUAGE_ID
    )

    val languageId = localeContext.getLanguageId()

    assertThat(languageId).isEqualTo(ENGLISH_LANGUAGE_ID)
  }

  @Test
  fun testGetLanguageId_audioSubsUsage_noAudioSubId_returnsDefaultInstance() {
    val localeContext = createContextWithoutLanguageDefinition(
      usageMode = OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS
    )

    val languageId = localeContext.getLanguageId()

    assertThat(languageId).isEqualToDefaultInstance()
  }

  @Test
  fun testGetLanguageId_audioSubsUsage_definedAudioSubId_returnsAudioSubId() {
    val localeContext = createAudioSubContext(
      language = OppiaLanguage.ENGLISH,
      audioTranslationId = ENGLISH_LANGUAGE_ID
    )

    val languageId = localeContext.getLanguageId()

    assertThat(languageId).isEqualTo(ENGLISH_LANGUAGE_ID)
  }

  @Test
  fun testGetLanguageId_fullDefinition_appStringUsage_returnsAppStringId() {
    val localeContext = createCompleteLocaleContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS,
      language = OppiaLanguage.ENGLISH,
      appStringId = ENGLISH_LANGUAGE_ID,
      contentStringId = HINGLISH_LANGUAGE_ID,
      audioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID
    )

    val languageId = localeContext.getLanguageId()

    assertThat(languageId).isEqualTo(ENGLISH_LANGUAGE_ID)
  }

  @Test
  fun testGetLanguageId_fullDefinition_contentStringUsage_returnsContentStringId() {
    val localeContext = createCompleteLocaleContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS,
      language = OppiaLanguage.ENGLISH,
      appStringId = ENGLISH_LANGUAGE_ID,
      contentStringId = HINGLISH_LANGUAGE_ID,
      audioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID
    )

    val languageId = localeContext.getLanguageId()

    assertThat(languageId).isEqualTo(HINGLISH_LANGUAGE_ID)
  }

  @Test
  fun testGetLanguageId_fullDefinition_audioSubUsage_returnsAudioSubId() {
    val localeContext = createCompleteLocaleContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS,
      language = OppiaLanguage.ENGLISH,
      appStringId = ENGLISH_LANGUAGE_ID,
      contentStringId = HINGLISH_LANGUAGE_ID,
      audioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID
    )

    val languageId = localeContext.getLanguageId()

    assertThat(languageId).isEqualTo(BRAZILIAN_PORTUGUESE_LANGUAGE_ID)
  }

  @Test
  fun testGetLanguageId_fullDefinition_unspecifiedUsage_returnsDefaultInstance() {
    val localeContext = createCompleteLocaleContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.USAGE_MODE_UNSPECIFIED,
      language = OppiaLanguage.ENGLISH,
      appStringId = ENGLISH_LANGUAGE_ID,
      contentStringId = HINGLISH_LANGUAGE_ID,
      audioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID
    )

    val languageId = localeContext.getLanguageId()

    assertThat(languageId).isEqualToDefaultInstance()
  }

  @Test
  fun testGetLanguageId_fullDefinitionWithFallback_appStringUsage_returnsAppStringId() {
    val localeContext = createCompleteLocaleWithFallbackContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS,
      language = OppiaLanguage.ENGLISH,
      appStringId = ENGLISH_LANGUAGE_ID,
      contentStringId = HINGLISH_LANGUAGE_ID,
      audioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID,
      fallbackAppStringId = QQ_LANGUAGE_ID,
      fallbackContentStringId = ZZ_LANGUAGE_ID,
      fallbackAudioTranslationId = FAKE_LANGUAGE_ID
    )

    val languageId = localeContext.getLanguageId()

    // The fallback language should be ignored.
    assertThat(languageId).isEqualTo(ENGLISH_LANGUAGE_ID)
  }

  @Test
  fun testGetLanguageId_fullDefinitionWithFallback_contentStringUsage_returnsContentStringId() {
    val localeContext = createCompleteLocaleWithFallbackContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS,
      language = OppiaLanguage.ENGLISH,
      appStringId = ENGLISH_LANGUAGE_ID,
      contentStringId = HINGLISH_LANGUAGE_ID,
      audioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID,
      fallbackAppStringId = QQ_LANGUAGE_ID,
      fallbackContentStringId = ZZ_LANGUAGE_ID,
      fallbackAudioTranslationId = FAKE_LANGUAGE_ID
    )

    val languageId = localeContext.getLanguageId()

    // The fallback language should be ignored.
    assertThat(languageId).isEqualTo(HINGLISH_LANGUAGE_ID)
  }

  @Test
  fun testGetLanguageId_fullDefinitionWithFallback_audioSubUsage_returnsAudioSubId() {
    val localeContext = createCompleteLocaleWithFallbackContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS,
      language = OppiaLanguage.ENGLISH,
      appStringId = ENGLISH_LANGUAGE_ID,
      contentStringId = HINGLISH_LANGUAGE_ID,
      audioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID,
      fallbackAppStringId = QQ_LANGUAGE_ID,
      fallbackContentStringId = ZZ_LANGUAGE_ID,
      fallbackAudioTranslationId = FAKE_LANGUAGE_ID
    )

    val languageId = localeContext.getLanguageId()

    // The fallback language should be ignored.
    assertThat(languageId).isEqualTo(BRAZILIAN_PORTUGUESE_LANGUAGE_ID)
  }

  /* Tests for getFallbackLanguageId() */

  @Test
  fun testGetFallbackId_defaultInstance_returnsDefaultInstance() {
    val localeContext = OppiaLocaleContext.getDefaultInstance()

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    assertThat(fallbackLanguageId).isEqualToDefaultInstance()
  }

  @Test
  fun testGetFallbackId_appStringUsage_noAppStringId_returnsDefaultInstance() {
    val localeContext = createContextWithoutLanguageDefinition(
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    assertThat(fallbackLanguageId).isEqualToDefaultInstance()
  }

  @Test
  fun testGetFallbackId_appStringUsage_definedFallbackAppStringId_returnsAppStringId() {
    val localeContext = createAppStringsWithFallbackContext(
      language = OppiaLanguage.ENGLISH,
      appStringId = QQ_LANGUAGE_ID,
      fallbackStringId = ENGLISH_LANGUAGE_ID
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    // The fallback ID should be selected.
    assertThat(fallbackLanguageId).isEqualTo(ENGLISH_LANGUAGE_ID)
  }

  @Test
  fun testGetFallbackId_contentStringUsage_noContentStringId_returnsDefaultInstance() {
    val localeContext = createContextWithoutLanguageDefinition(
      usageMode = OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    assertThat(fallbackLanguageId).isEqualToDefaultInstance()
  }

  @Test
  fun testGetFallbackId_contentStringUsage_definedFallbackContentStringId_returnsContentId() {
    val localeContext = createContentStringsWithFallbackContext(
      language = OppiaLanguage.ENGLISH,
      contentStringId = QQ_LANGUAGE_ID,
      fallbackContentStringId = ENGLISH_LANGUAGE_ID
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    // The fallback ID should be selected.
    assertThat(fallbackLanguageId).isEqualTo(ENGLISH_LANGUAGE_ID)
  }

  @Test
  fun testGetFallbackId_audioSubsUsage_noAudioSubId_returnsDefaultInstance() {
    val localeContext = createContextWithoutLanguageDefinition(
      usageMode = OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    assertThat(fallbackLanguageId).isEqualToDefaultInstance()
  }

  @Test
  fun testGetFallbackId_audioSubsUsage_definedFallbackAudioSubId_returnsAudioSubId() {
    val localeContext = createAudioSubWithFallbackContext(
      language = OppiaLanguage.ENGLISH,
      audioTranslationId = QQ_LANGUAGE_ID,
      fallbackAudioTranslationId = ENGLISH_LANGUAGE_ID
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    // The fallback ID should be selected.
    assertThat(fallbackLanguageId).isEqualTo(ENGLISH_LANGUAGE_ID)
  }

  @Test
  fun testGetFallbackId_fullDefinition_appStringUsage_returnsAppStringId() {
    val localeContext = createCompleteLocaleWithFallbackContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS,
      language = OppiaLanguage.ENGLISH,
      appStringId = QQ_LANGUAGE_ID,
      contentStringId = ZZ_LANGUAGE_ID,
      audioTranslationId = FAKE_LANGUAGE_ID,
      fallbackAppStringId = ENGLISH_LANGUAGE_ID,
      fallbackContentStringId = HINGLISH_LANGUAGE_ID,
      fallbackAudioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    // The fallback ID should match the specified usage mode.
    assertThat(fallbackLanguageId).isEqualTo(ENGLISH_LANGUAGE_ID)
  }

  @Test
  fun testGetFallbackId_fullDefinition_contentStringUsage_returnsContentStringId() {
    val localeContext = createCompleteLocaleWithFallbackContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS,
      language = OppiaLanguage.ENGLISH,
      appStringId = QQ_LANGUAGE_ID,
      contentStringId = ZZ_LANGUAGE_ID,
      audioTranslationId = FAKE_LANGUAGE_ID,
      fallbackAppStringId = ENGLISH_LANGUAGE_ID,
      fallbackContentStringId = HINGLISH_LANGUAGE_ID,
      fallbackAudioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    // The fallback ID should match the specified usage mode.
    assertThat(fallbackLanguageId).isEqualTo(HINGLISH_LANGUAGE_ID)
  }

  @Test
  fun testGetFallbackId_fullDefinition_audioSubUsage_returnsAudioSubId() {
    val localeContext = createCompleteLocaleWithFallbackContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS,
      language = OppiaLanguage.ENGLISH,
      appStringId = QQ_LANGUAGE_ID,
      contentStringId = ZZ_LANGUAGE_ID,
      audioTranslationId = FAKE_LANGUAGE_ID,
      fallbackAppStringId = ENGLISH_LANGUAGE_ID,
      fallbackContentStringId = HINGLISH_LANGUAGE_ID,
      fallbackAudioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    // The fallback ID should match the specified usage mode.
    assertThat(fallbackLanguageId).isEqualTo(BRAZILIAN_PORTUGUESE_LANGUAGE_ID)
  }

  @Test
  fun testGetFallbackId_fullDefinition_unspecifiedUsage_returnsDefaultInstance() {
    val localeContext = createCompleteLocaleWithFallbackContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.USAGE_MODE_UNSPECIFIED,
      language = OppiaLanguage.ENGLISH,
      appStringId = QQ_LANGUAGE_ID,
      contentStringId = ZZ_LANGUAGE_ID,
      audioTranslationId = FAKE_LANGUAGE_ID,
      fallbackAppStringId = ENGLISH_LANGUAGE_ID,
      fallbackContentStringId = HINGLISH_LANGUAGE_ID,
      fallbackAudioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    assertThat(fallbackLanguageId).isEqualToDefaultInstance()
  }

  @Test
  fun testGetFallbackId_fullDefinitionNoFallback_appStringUsage_returnsDefaultInstance() {
    val localeContext = createCompleteLocaleContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS,
      language = OppiaLanguage.ENGLISH,
      appStringId = ENGLISH_LANGUAGE_ID,
      contentStringId = HINGLISH_LANGUAGE_ID,
      audioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    // No fallback is defined.
    assertThat(fallbackLanguageId).isEqualToDefaultInstance()
  }

  @Test
  fun testGetFallbackId_fullDefinitionNoFallback_contentStringUsage_returnsDefaultInstance() {
    val localeContext = createCompleteLocaleContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS,
      language = OppiaLanguage.ENGLISH,
      appStringId = ENGLISH_LANGUAGE_ID,
      contentStringId = HINGLISH_LANGUAGE_ID,
      audioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    // No fallback is defined.
    assertThat(fallbackLanguageId).isEqualToDefaultInstance()
  }

  @Test
  fun testGetFallbackId_fullDefinitionNoFallback_audioSubUsage_returnsDefaultInstance() {
    val localeContext = createCompleteLocaleContext(
      usageMode = OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS,
      language = OppiaLanguage.ENGLISH,
      appStringId = ENGLISH_LANGUAGE_ID,
      contentStringId = HINGLISH_LANGUAGE_ID,
      audioTranslationId = BRAZILIAN_PORTUGUESE_LANGUAGE_ID
    )

    val fallbackLanguageId = localeContext.getFallbackLanguageId()

    // No fallback is defined.
    assertThat(fallbackLanguageId).isEqualToDefaultInstance()
  }

  private fun createContextWithoutLanguageDefinition(
    usageMode: OppiaLocaleContext.LanguageUsageMode
  ): OppiaLocaleContext {
    return OppiaLocaleContext.newBuilder().apply {
      this.usageMode = usageMode
    }.build()
  }

  private fun createAppStringsContext(
    language: OppiaLanguage,
    appStringId: LanguageId
  ): OppiaLocaleContext {
    return createContextWithoutLanguageDefinition(
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
    ).toBuilder().apply {
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        this.appStringId = appStringId
      }.build()
    }.build()
  }

  private fun createAppStringsWithFallbackContext(
    language: OppiaLanguage,
    appStringId: LanguageId,
    fallbackStringId: LanguageId
  ): OppiaLocaleContext {
    return createAppStringsContext(language, appStringId).toBuilder().apply {
      fallbackLanguageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        this.appStringId = fallbackStringId
      }.build()
    }.build()
  }

  private fun createContentStringsContext(
    language: OppiaLanguage,
    contentStringId: LanguageId
  ): OppiaLocaleContext {
    return createContextWithoutLanguageDefinition(
      usageMode = OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS
    ).toBuilder().apply {
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        this.contentStringId = contentStringId
      }.build()
    }.build()
  }

  private fun createContentStringsWithFallbackContext(
    language: OppiaLanguage,
    contentStringId: LanguageId,
    fallbackContentStringId: LanguageId
  ): OppiaLocaleContext {
    return createContentStringsContext(language, contentStringId).toBuilder().apply {
      fallbackLanguageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        this.contentStringId = fallbackContentStringId
      }.build()
    }.build()
  }

  private fun createAudioSubContext(
    language: OppiaLanguage,
    audioTranslationId: LanguageId
  ): OppiaLocaleContext {
    return createContextWithoutLanguageDefinition(
      usageMode = OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS
    ).toBuilder().apply {
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        this.audioTranslationId = audioTranslationId
      }.build()
    }.build()
  }

  private fun createAudioSubWithFallbackContext(
    language: OppiaLanguage,
    audioTranslationId: LanguageId,
    fallbackAudioTranslationId: LanguageId
  ): OppiaLocaleContext {
    return createAudioSubContext(language, audioTranslationId).toBuilder().apply {
      fallbackLanguageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        this.audioTranslationId = fallbackAudioTranslationId
      }.build()
    }.build()
  }

  private fun createCompleteLocaleContext(
    usageMode: OppiaLocaleContext.LanguageUsageMode,
    language: OppiaLanguage,
    appStringId: LanguageId,
    contentStringId: LanguageId,
    audioTranslationId: LanguageId
  ): OppiaLocaleContext {
    return createContextWithoutLanguageDefinition(usageMode).toBuilder().apply {
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        this.appStringId = appStringId
        this.contentStringId = contentStringId
        this.audioTranslationId = audioTranslationId
      }.build()
    }.build()
  }

  private fun createCompleteLocaleWithFallbackContext(
    usageMode: OppiaLocaleContext.LanguageUsageMode,
    language: OppiaLanguage,
    appStringId: LanguageId,
    contentStringId: LanguageId,
    audioTranslationId: LanguageId,
    fallbackAppStringId: LanguageId,
    fallbackContentStringId: LanguageId,
    fallbackAudioTranslationId: LanguageId
  ): OppiaLocaleContext {
    return createCompleteLocaleContext(
      usageMode, language, appStringId, contentStringId, audioTranslationId
    ).toBuilder().apply {
      fallbackLanguageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        this.appStringId = fallbackAppStringId
        this.contentStringId = fallbackContentStringId
        this.audioTranslationId = fallbackAudioTranslationId
      }.build()
    }.build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerOppiaLocaleContextExtensionsTest_TestApplicationComponent.builder()
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
      TestModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(oppiaLocaleContextExtensionsTest: OppiaLocaleContextExtensionsTest)
  }

  private companion object {
    private val ENGLISH_LANGUAGE_ID = LanguageId.newBuilder().apply {
      ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "en"
      }.build()
      androidResourcesLanguageId = LanguageSupportDefinition.AndroidLanguageId.newBuilder().apply {
        languageCode = "en"
      }.build()
    }.build()

    private val HINGLISH_LANGUAGE_ID = LanguageId.newBuilder().apply {
      macaronicId = LanguageSupportDefinition.MacaronicLanguageId.newBuilder().apply {
        combinedLanguageCode = "hi-en"
      }.build()
    }.build()

    private val BRAZILIAN_PORTUGUESE_LANGUAGE_ID = LanguageId.newBuilder().apply {
      ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "pt-BR"
      }.build()
      androidResourcesLanguageId = LanguageSupportDefinition.AndroidLanguageId.newBuilder().apply {
        languageCode = "pt"
        regionCode = "BR"
      }.build()
    }.build()

    private val QQ_LANGUAGE_ID = LanguageId.newBuilder().apply {
      ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "qq"
      }.build()
    }.build()

    private val ZZ_LANGUAGE_ID = LanguageId.newBuilder().apply {
      ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "zz"
      }.build()
    }.build()

    private val FAKE_LANGUAGE_ID = LanguageId.newBuilder().apply {
      ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "fake"
      }.build()
    }.build()
  }
}
