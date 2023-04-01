package org.oppia.android.util.locale

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.LanguageSupportDefinition.AndroidLanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.IetfBcp47LanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.MacaronicLanguageId
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [AndroidLocaleFactory].
 *
 * Note that these tests depend on real locales being present in the local environment
 * (Robolectric).
 */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class AndroidLocaleFactoryTest {
  @Inject
  lateinit var androidLocaleFactory: AndroidLocaleFactory

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testCreateLocale_default_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      androidLocaleFactory.createAndroidLocale(OppiaLocaleContext.getDefaultInstance())
    }

    // The operation should fail since there's no language type defined.
    assertThat(exception).hasMessageThat().contains("Invalid language case")
  }

  /* Tests for app strings. */

  @Test
  fun testCreateLocale_appStrings_withAndroidId_compatible_returnsAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        appStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The context should be matched to a valid locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_withIetfId_compatible_returnsIetfLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        appStringId = createLanguageId(ietfBcp47LanguageId = PT_IETF_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The context should be matched to a valid locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_withRegionIetfId_compatible_returnsIetfLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        appStringId = createLanguageId(ietfBcp47LanguageId = PT_BR_IETF_LANGUAGE_ID),
        regionDefinition = REGION_US
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The context should be matched to a valid locale. Note that BR is matched since the IETF
    // language tag includes the region.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_withMacaronic_compatible_returnsMacaronicLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        appStringId = createLanguageId(macaronicLanguageId = PT_BR_MACARONIC_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The context should be matched to a valid locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_androidIdAndIetf_returnsAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.ENGLISH,
        appStringId = createLanguageId(
          androidLanguageId = EN_ANDROID_LANGUAGE_ID,
          ietfBcp47LanguageId = PT_IETF_LANGUAGE_ID,
        ),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The Android is preferred when both are present. Note no region is provided since the Android
    // language is missing a region definition.
    assertThat(locale.language).isEqualTo("en")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_appStrings_androidIdAndMacaronic_returnsAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.ENGLISH,
        appStringId = createLanguageId(
          androidLanguageId = EN_ANDROID_LANGUAGE_ID,
          macaronicLanguageId = PT_BR_MACARONIC_LANGUAGE_ID
        ),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The Android is preferred when both are present. Note no region is provided since the Android
    // language is missing a region definition.
    assertThat(locale.language).isEqualTo("en")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_appStrings_withAndroidId_incompatible_returnsFallbackAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        appStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        fallbackAppStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_withIetfId_incompatible_returnsFallbackAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        appStringId = createLanguageId(ietfBcp47LanguageId = QQ_ZZ_IETF_LANGUAGE_ID),
        fallbackAppStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_withRegionIetfId_incompat_returnsFallbackAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        appStringId = createLanguageId(ietfBcp47LanguageId = PT_ZZ_IETF_LANGUAGE_ID),
        fallbackAppStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language's region doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_withIetfId_incompRegion_returnsFallbackAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        appStringId = createLanguageId(ietfBcp47LanguageId = PT_IETF_LANGUAGE_ID),
        fallbackAppStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_ZZ
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the supplied region doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_withMacaronicId_incompat_returnsFallbackAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        appStringId = createLanguageId(macaronicLanguageId = QQ_ZZ_MACARONIC_LANGUAGE_ID),
        fallbackAppStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_withMacaronicId_invalid_returnsFallbackAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        appStringId = createLanguageId(macaronicLanguageId = INVALID_MACARONIC_LANGUAGE_ID),
        fallbackAppStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language is invalid.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_withSdkIncompat_returnsFallbackAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.ENGLISH,
        appStringId = createLanguageId(androidLanguageId = EN_ANDROID_LANGUAGE_ID),
        primaryMinSdkVersion = 99,
        fallbackAppStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language isn't compatible with the current SDK.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_incompat_androidAndIetfFallback_returnsAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        appStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        fallbackAppStringId = createLanguageId(
          androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID,
          ietfBcp47LanguageId = HI_IETF_LANGUAGE_ID
        ),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked since Android IDs take precedence among multiple fallback options.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_incompat_androidMacaronicFallbacks_returnsAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        appStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        fallbackAppStringId = createLanguageId(
          androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID,
          macaronicLanguageId = HI_IN_MACARONIC_LANGUAGE_ID
        ),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked since Android IDs take precedence among multiple fallback options.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_appStrings_androidId_allIncompat_returnsForcedAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        appStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        fallbackAppStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // 'qq' is the exact locale being requested.
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_appStrings_androidId_ietf_allIncompat_returnsForcedAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        appStringId = createLanguageId(
          androidLanguageId = QQ_ANDROID_LANGUAGE_ID,
          ietfBcp47LanguageId = QQ_IETF_LANGUAGE_ID
        ),
        fallbackAppStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // 'qq' takes precedence over the IETF language since Android IDs are picked first when
    // creating a forced locale.
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_appStrings_androidId_mac_allIncompat_returnsForcedAndroidIdLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        appStringId = createLanguageId(
          androidLanguageId = QQ_ANDROID_LANGUAGE_ID,
          macaronicLanguageId = HI_EN_MACARONIC_LANGUAGE_ID
        ),
        fallbackAppStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // 'qq' takes precedence over the macaronic language since Android IDs are picked first when
    // creating a forced locale.
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_appStrings_ietf_allIncompat_returnsForcedIetfLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        appStringId = createLanguageId(ietfBcp47LanguageId = QQ_IETF_LANGUAGE_ID),
        fallbackAppStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The IETF language ID is used for the forced locale (note that fallback languages are ignored
    // when computing the forced locale).
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEqualTo("IN")
  }

  @Test
  fun testCreateLocale_appStrings_mac_allIncompat_returnsForcedMacaronicLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        appStringId = createLanguageId(macaronicLanguageId = HI_EN_MACARONIC_LANGUAGE_ID),
        fallbackAppStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The Hinglish macaronic language ID is used for the forced locale (note that fallback
    // languages are ignored when computing the forced locale).
    assertThat(locale.language).isEqualTo("hi")
    assertThat(locale.country).isEqualTo("EN")
  }

  @Test
  fun testCreateLocale_appStrings_mac_allIncompat_invalidMac_throwsException() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.ENGLISH,
        appStringId = createLanguageId(macaronicLanguageId = INVALID_MACARONIC_LANGUAGE_ID),
        fallbackAppStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val exception = assertThrows(IllegalStateException::class) {
      androidLocaleFactory.createAndroidLocale(context)
    }

    assertThat(exception).hasMessageThat().contains("Invalid macaronic ID")
  }

  @Test
  fun testCreateLocale_appStrings_primaryAndFallbackSdkIncompat_returnsForcedLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        appStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        primaryMinSdkVersion = 99,
        fallbackAppStringId = createLanguageId(androidLanguageId = EN_ANDROID_LANGUAGE_ID),
        fallbackMinSdkVersion = 99,
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The 'qq' language should be matched as a forced profile since both language IDs are
    // SDK-incompatible (despite the fallback being a matchable language).
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_appStrings_forMacroAndroidIdLanguage_returnsLocale() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.ENGLISH,
        appStringId = createLanguageId(androidLanguageId = EN_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // Simple macro languages may not match any internal locales due to missing regions. They should
    // still become a valid locale (due to wildcard matching internally).
    assertThat(locale.language).isEqualTo("en")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_appStrings_allIncompat_invalidLangType_throwsException() {
    val context =
      createAppStringsContext(
        language = OppiaLanguage.ENGLISH,
        appStringId = LanguageId.getDefaultInstance(),
        regionDefinition = REGION_INDIA
      )

    val exception = assertThrows(IllegalStateException::class) {
      androidLocaleFactory.createAndroidLocale(context)
    }

    assertThat(exception).hasMessageThat().contains("Invalid language case")
  }

  /* Tests for written content strings. */

  @Test
  fun testCreateLocale_contentStrings_withAndroidId_compatible_returnsAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        contentStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The context should be matched to a valid locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_withIetfId_compatible_returnsIetfLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        contentStringId = createLanguageId(ietfBcp47LanguageId = PT_IETF_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The context should be matched to a valid locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_withRegionIetfId_compatible_returnsIetfLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        contentStringId = createLanguageId(ietfBcp47LanguageId = PT_BR_IETF_LANGUAGE_ID),
        regionDefinition = REGION_US
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The context should be matched to a valid locale. Note that BR is matched since the IETF
    // language tag includes the region.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_withMacaronic_compatible_returnsMacaronicLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        contentStringId = createLanguageId(macaronicLanguageId = PT_BR_MACARONIC_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The context should be matched to a valid locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_androidIdAndIetf_returnsAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.ENGLISH,
        contentStringId = createLanguageId(
          androidLanguageId = EN_ANDROID_LANGUAGE_ID,
          ietfBcp47LanguageId = PT_IETF_LANGUAGE_ID,
        ),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The Android is preferred when both are present. Note no region is provided since the Android
    // language is missing a region definition.
    assertThat(locale.language).isEqualTo("en")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_contentStrings_androidIdAndMacaronic_returnsAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.ENGLISH,
        contentStringId = createLanguageId(
          androidLanguageId = EN_ANDROID_LANGUAGE_ID,
          macaronicLanguageId = PT_BR_MACARONIC_LANGUAGE_ID
        ),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The Android is preferred when both are present. Note no region is provided since the Android
    // language is missing a region definition.
    assertThat(locale.language).isEqualTo("en")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_contentStrings_withAndroidId_incompatible_returnsFallbackAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        contentStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        fallbackContentStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_withIetfId_incompatible_returnsFallbackAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        contentStringId = createLanguageId(ietfBcp47LanguageId = QQ_ZZ_IETF_LANGUAGE_ID),
        fallbackContentStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_withRegionIetfId_incompat_returnsFallbackAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        contentStringId = createLanguageId(ietfBcp47LanguageId = PT_ZZ_IETF_LANGUAGE_ID),
        fallbackContentStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language's region doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_withIetfId_incompRegion_returnsFallbackAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        contentStringId = createLanguageId(ietfBcp47LanguageId = PT_IETF_LANGUAGE_ID),
        fallbackContentStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_ZZ
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the supplied region doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_withMacaronicId_incompat_returnsFallbackAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        contentStringId = createLanguageId(macaronicLanguageId = QQ_ZZ_MACARONIC_LANGUAGE_ID),
        fallbackContentStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_withMacaronicId_invalid_returnsFallbackAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        contentStringId = createLanguageId(macaronicLanguageId = INVALID_MACARONIC_LANGUAGE_ID),
        fallbackContentStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language is invalid.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_withSdkIncompat_returnsFallbackAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.ENGLISH,
        contentStringId = createLanguageId(androidLanguageId = EN_ANDROID_LANGUAGE_ID),
        primaryMinSdkVersion = 99,
        fallbackContentStringId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language isn't compatible with the current SDK.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_incompat_androidAndIetfFallback_returnsAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        contentStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        fallbackContentStringId = createLanguageId(
          androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID,
          ietfBcp47LanguageId = HI_IETF_LANGUAGE_ID
        ),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked since Android IDs take precedence among multiple fallback options.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_incompat_androidMacaronicFallbacks_returnsAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        contentStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        fallbackContentStringId = createLanguageId(
          androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID,
          macaronicLanguageId = HI_IN_MACARONIC_LANGUAGE_ID
        ),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked since Android IDs take precedence among multiple fallback options.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_contentStrings_androidId_allIncompat_returnsForcedAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        contentStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        fallbackContentStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // 'qq' is the exact locale being requested.
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_contentStrings_androidId_ietf_allIncompat_returnsForcedAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        contentStringId = createLanguageId(
          androidLanguageId = QQ_ANDROID_LANGUAGE_ID,
          ietfBcp47LanguageId = QQ_IETF_LANGUAGE_ID
        ),
        fallbackContentStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // 'qq' takes precedence over the IETF language since Android IDs are picked first when
    // creating a forced locale.
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_contentStrings_androidId_mac_allIncompat_returnsForcedAndroidIdLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        contentStringId = createLanguageId(
          androidLanguageId = QQ_ANDROID_LANGUAGE_ID,
          macaronicLanguageId = HI_EN_MACARONIC_LANGUAGE_ID
        ),
        fallbackContentStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // 'qq' takes precedence over the macaronic language since Android IDs are picked first when
    // creating a forced locale.
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_contentStrings_ietf_allIncompat_returnsForcedIetfLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        contentStringId = createLanguageId(ietfBcp47LanguageId = QQ_IETF_LANGUAGE_ID),
        fallbackContentStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The IETF language ID is used for the forced locale (note that fallback languages are ignored
    // when computing the forced locale).
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEqualTo("IN")
  }

  @Test
  fun testCreateLocale_contentStrings_mac_allIncompat_returnsForcedMacaronicLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        contentStringId = createLanguageId(macaronicLanguageId = HI_EN_MACARONIC_LANGUAGE_ID),
        fallbackContentStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The Hinglish macaronic language ID is used for the forced locale (note that fallback
    // languages are ignored when computing the forced locale).
    assertThat(locale.language).isEqualTo("hi")
    assertThat(locale.country).isEqualTo("EN")
  }

  @Test
  fun testCreateLocale_contentStrings_mac_allIncompat_invalidMac_throwsException() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.ENGLISH,
        contentStringId = createLanguageId(macaronicLanguageId = INVALID_MACARONIC_LANGUAGE_ID),
        fallbackContentStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val exception = assertThrows(IllegalStateException::class) {
      androidLocaleFactory.createAndroidLocale(context)
    }

    assertThat(exception).hasMessageThat().contains("Invalid macaronic ID")
  }

  @Test
  fun testCreateLocale_contentStrings_primaryAndFallbackSdkIncompat_returnsForcedLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        contentStringId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        primaryMinSdkVersion = 99,
        fallbackContentStringId = createLanguageId(androidLanguageId = EN_ANDROID_LANGUAGE_ID),
        fallbackMinSdkVersion = 99,
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The 'qq' language should be matched as a forced profile since both language IDs are
    // SDK-incompatible (despite the fallback being a matchable language).
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_contentStrings_forMacroAndroidIdLanguage_returnsLocale() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.ENGLISH,
        contentStringId = createLanguageId(androidLanguageId = EN_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // Simple macro languages may not match any internal locales due to missing regions. They should
    // still become a valid locale (due to wildcard matching internally).
    assertThat(locale.language).isEqualTo("en")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_contentStrings_allIncompat_invalidLangType_throwsException() {
    val context =
      createContentStringsContext(
        language = OppiaLanguage.ENGLISH,
        contentStringId = LanguageId.getDefaultInstance(),
        regionDefinition = REGION_INDIA
      )

    val exception = assertThrows(IllegalStateException::class) {
      androidLocaleFactory.createAndroidLocale(context)
    }

    assertThat(exception).hasMessageThat().contains("Invalid language case")
  }

  /* Tests for audio translations. */

  @Test
  fun testCreateLocale_audioSubs_withAndroidId_compatible_returnsAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        audioTranslationId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The context should be matched to a valid locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_withIetfId_compatible_returnsIetfLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        audioTranslationId = createLanguageId(ietfBcp47LanguageId = PT_IETF_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The context should be matched to a valid locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_withRegionIetfId_compatible_returnsIetfLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        audioTranslationId = createLanguageId(ietfBcp47LanguageId = PT_BR_IETF_LANGUAGE_ID),
        regionDefinition = REGION_US
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The context should be matched to a valid locale. Note that BR is matched since the IETF
    // language tag includes the region.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_withMacaronic_compatible_returnsMacaronicLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        audioTranslationId = createLanguageId(macaronicLanguageId = PT_BR_MACARONIC_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The context should be matched to a valid locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_androidIdAndIetf_returnsAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.ENGLISH,
        audioTranslationId = createLanguageId(
          androidLanguageId = EN_ANDROID_LANGUAGE_ID,
          ietfBcp47LanguageId = PT_IETF_LANGUAGE_ID,
        ),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The Android is preferred when both are present. Note no region is provided since the Android
    // language is missing a region definition.
    assertThat(locale.language).isEqualTo("en")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_audioSubs_androidIdAndMacaronic_returnsAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.ENGLISH,
        audioTranslationId = createLanguageId(
          androidLanguageId = EN_ANDROID_LANGUAGE_ID,
          macaronicLanguageId = PT_BR_MACARONIC_LANGUAGE_ID
        ),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The Android is preferred when both are present. Note no region is provided since the Android
    // language is missing a region definition.
    assertThat(locale.language).isEqualTo("en")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_audioSubs_withAndroidId_incompatible_returnsFallbackAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        audioXlationId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        fallbackAudioXlationId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_withIetfId_incompatible_returnsFallbackAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        audioXlationId = createLanguageId(ietfBcp47LanguageId = QQ_ZZ_IETF_LANGUAGE_ID),
        fallbackAudioXlationId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_withRegionIetfId_incompat_returnsFallbackAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        audioXlationId = createLanguageId(ietfBcp47LanguageId = PT_ZZ_IETF_LANGUAGE_ID),
        fallbackAudioXlationId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language's region doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_withIetfId_incompRegion_returnsFallbackAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.BRAZILIAN_PORTUGUESE,
        audioXlationId = createLanguageId(ietfBcp47LanguageId = PT_IETF_LANGUAGE_ID),
        fallbackAudioXlationId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_ZZ
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the supplied region doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_withMacaronicId_incompat_returnsFallbackAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        audioXlationId = createLanguageId(macaronicLanguageId = QQ_ZZ_MACARONIC_LANGUAGE_ID),
        fallbackAudioXlationId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language doesn't match a real locale.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_withMacaronicId_invalid_returnsFallbackAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        audioXlationId = createLanguageId(macaronicLanguageId = INVALID_MACARONIC_LANGUAGE_ID),
        fallbackAudioXlationId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language is invalid.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_withSdkIncompat_returnsFallbackAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.ENGLISH,
        audioXlationId = createLanguageId(androidLanguageId = EN_ANDROID_LANGUAGE_ID),
        primaryMinSdkVersion = 99,
        fallbackAudioXlationId = createLanguageId(androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_BRAZIL
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked because the primary language isn't compatible with the current SDK.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_incompat_androidAndIetfFallback_returnsAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        audioXlationId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        fallbackAudioXlationId = createLanguageId(
          androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID,
          ietfBcp47LanguageId = HI_IETF_LANGUAGE_ID
        ),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked since Android IDs take precedence among multiple fallback options.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_incompat_androidMacaronicFallbacks_returnsAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        audioXlationId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        fallbackAudioXlationId = createLanguageId(
          androidLanguageId = PT_BR_ANDROID_LANGUAGE_ID,
          macaronicLanguageId = HI_IN_MACARONIC_LANGUAGE_ID
        ),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // pt-BR should be picked since Android IDs take precedence among multiple fallback options.
    assertThat(locale.language).isEqualTo("pt")
    assertThat(locale.country).isEqualTo("BR")
  }

  @Test
  fun testCreateLocale_audioSubs_androidId_allIncompat_returnsForcedAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        audioXlationId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        fallbackAudioXlationId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // 'qq' is the exact locale being requested.
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_audioSubs_androidId_ietf_allIncompat_returnsForcedAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        audioXlationId = createLanguageId(
          androidLanguageId = QQ_ANDROID_LANGUAGE_ID,
          ietfBcp47LanguageId = QQ_IETF_LANGUAGE_ID
        ),
        fallbackAudioXlationId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // 'qq' takes precedence over the IETF language since Android IDs are picked first when
    // creating a forced locale.
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_audioSubs_androidId_mac_allIncompat_returnsForcedAndroidIdLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        audioXlationId = createLanguageId(
          androidLanguageId = QQ_ANDROID_LANGUAGE_ID,
          macaronicLanguageId = HI_EN_MACARONIC_LANGUAGE_ID
        ),
        fallbackAudioXlationId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // 'qq' takes precedence over the macaronic language since Android IDs are picked first when
    // creating a forced locale.
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_audioSubs_ietf_allIncompat_returnsForcedIetfLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        audioXlationId = createLanguageId(ietfBcp47LanguageId = QQ_IETF_LANGUAGE_ID),
        fallbackAudioXlationId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The IETF language ID is used for the forced locale (note that fallback languages are ignored
    // when computing the forced locale).
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEqualTo("IN")
  }

  @Test
  fun testCreateLocale_audioSubs_mac_allIncompat_returnsForcedMacaronicLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        audioXlationId = createLanguageId(macaronicLanguageId = HI_EN_MACARONIC_LANGUAGE_ID),
        fallbackAudioXlationId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The Hinglish macaronic language ID is used for the forced locale (note that fallback
    // languages are ignored when computing the forced locale).
    assertThat(locale.language).isEqualTo("hi")
    assertThat(locale.country).isEqualTo("EN")
  }

  @Test
  fun testCreateLocale_audioSubs_mac_allIncompat_invalidMac_throwsException() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.ENGLISH,
        audioXlationId = createLanguageId(macaronicLanguageId = INVALID_MACARONIC_LANGUAGE_ID),
        fallbackAudioXlationId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val exception = assertThrows(IllegalStateException::class) {
      androidLocaleFactory.createAndroidLocale(context)
    }

    assertThat(exception).hasMessageThat().contains("Invalid macaronic ID")
  }

  @Test
  fun testCreateLocale_audioSubs_primaryAndFallbackSdkIncompat_returnsForcedLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED,
        audioXlationId = createLanguageId(androidLanguageId = QQ_ANDROID_LANGUAGE_ID),
        primaryMinSdkVersion = 99,
        fallbackAudioXlationId = createLanguageId(androidLanguageId = EN_ANDROID_LANGUAGE_ID),
        fallbackMinSdkVersion = 99,
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // The 'qq' language should be matched as a forced profile since both language IDs are
    // SDK-incompatible (despite the fallback being a matchable language).
    assertThat(locale.language).isEqualTo("qq")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_audioSubs_forMacroAndroidIdLanguage_returnsLocale() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.ENGLISH,
        audioTranslationId = createLanguageId(androidLanguageId = EN_ANDROID_LANGUAGE_ID),
        regionDefinition = REGION_INDIA
      )

    val locale = androidLocaleFactory.createAndroidLocale(context)

    // Simple macro languages may not match any internal locales due to missing regions. They should
    // still become a valid locale (due to wildcard matching internally).
    assertThat(locale.language).isEqualTo("en")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testCreateLocale_audioSubs_allIncompat_invalidLangType_throwsException() {
    val context =
      createAudioTranslationContext(
        language = OppiaLanguage.ENGLISH,
        audioTranslationId = LanguageId.getDefaultInstance(),
        regionDefinition = REGION_INDIA
      )

    val exception = assertThrows(IllegalStateException::class) {
      androidLocaleFactory.createAndroidLocale(context)
    }

    assertThat(exception).hasMessageThat().contains("Invalid language case")
  }

  private fun createLanguageId(androidLanguageId: AndroidLanguageId): LanguageId {
    return LanguageId.newBuilder().apply {
      androidResourcesLanguageId = androidLanguageId
    }.build()
  }

  private fun createLanguageId(ietfBcp47LanguageId: IetfBcp47LanguageId): LanguageId {
    return LanguageId.newBuilder().apply {
      ietfBcp47Id = ietfBcp47LanguageId
    }.build()
  }

  private fun createLanguageId(
    androidLanguageId: AndroidLanguageId,
    ietfBcp47LanguageId: IetfBcp47LanguageId
  ): LanguageId {
    return createLanguageId(androidLanguageId).toBuilder()
      .mergeFrom(createLanguageId(ietfBcp47LanguageId))
      .build()
  }

  private fun createLanguageId(macaronicLanguageId: MacaronicLanguageId): LanguageId {
    return LanguageId.newBuilder().apply {
      macaronicId = macaronicLanguageId
    }.build()
  }

  private fun createLanguageId(
    androidLanguageId: AndroidLanguageId,
    macaronicLanguageId: MacaronicLanguageId
  ): LanguageId {
    return createLanguageId(androidLanguageId).toBuilder()
      .mergeFrom(createLanguageId(macaronicLanguageId))
      .build()
  }

  private fun createAppStringsContext(
    language: OppiaLanguage,
    appStringId: LanguageId,
    minSdkVersion: Int = 1,
    regionDefinition: RegionSupportDefinition
  ): OppiaLocaleContext {
    return OppiaLocaleContext.newBuilder().apply {
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        minAndroidSdkVersion = minSdkVersion
        this.appStringId = appStringId
      }.build()
      this.regionDefinition = regionDefinition
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
    }.build()
  }

  private fun createAppStringsContext(
    language: OppiaLanguage,
    appStringId: LanguageId,
    primaryMinSdkVersion: Int = 1,
    fallbackAppStringId: LanguageId,
    fallbackMinSdkVersion: Int = 1,
    regionDefinition: RegionSupportDefinition
  ): OppiaLocaleContext {
    return createAppStringsContext(
      language, appStringId, primaryMinSdkVersion, regionDefinition
    ).toBuilder().apply {
      fallbackLanguageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        minAndroidSdkVersion = fallbackMinSdkVersion
        this.appStringId = fallbackAppStringId
      }.build()
    }.build()
  }

  private fun createContentStringsContext(
    language: OppiaLanguage,
    contentStringId: LanguageId,
    minSdkVersion: Int = 1,
    regionDefinition: RegionSupportDefinition
  ): OppiaLocaleContext {
    return OppiaLocaleContext.newBuilder().apply {
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        minAndroidSdkVersion = minSdkVersion
        this.contentStringId = contentStringId
      }.build()
      this.regionDefinition = regionDefinition
      usageMode = OppiaLocaleContext.LanguageUsageMode.CONTENT_STRINGS
    }.build()
  }

  private fun createContentStringsContext(
    language: OppiaLanguage,
    contentStringId: LanguageId,
    primaryMinSdkVersion: Int = 1,
    fallbackContentStringId: LanguageId,
    fallbackMinSdkVersion: Int = 1,
    regionDefinition: RegionSupportDefinition
  ): OppiaLocaleContext {
    return createContentStringsContext(
      language, contentStringId, primaryMinSdkVersion, regionDefinition
    ).toBuilder().apply {
      fallbackLanguageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        minAndroidSdkVersion = fallbackMinSdkVersion
        this.contentStringId = fallbackContentStringId
      }.build()
    }.build()
  }

  private fun createAudioTranslationContext(
    language: OppiaLanguage,
    audioTranslationId: LanguageId,
    minSdkVersion: Int = 1,
    regionDefinition: RegionSupportDefinition
  ): OppiaLocaleContext {
    return OppiaLocaleContext.newBuilder().apply {
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        minAndroidSdkVersion = minSdkVersion
        this.audioTranslationId = audioTranslationId
      }.build()
      this.regionDefinition = regionDefinition
      usageMode = OppiaLocaleContext.LanguageUsageMode.AUDIO_TRANSLATIONS
    }.build()
  }

  private fun createAudioTranslationContext(
    language: OppiaLanguage,
    audioXlationId: LanguageId,
    primaryMinSdkVersion: Int = 1,
    fallbackAudioXlationId: LanguageId,
    fallbackMinSdkVersion: Int = 1,
    regionDefinition: RegionSupportDefinition
  ): OppiaLocaleContext {
    return createAudioTranslationContext(
      language, audioXlationId, primaryMinSdkVersion, regionDefinition
    ).toBuilder().apply {
      fallbackLanguageDefinition = LanguageSupportDefinition.newBuilder().apply {
        this.language = language
        minAndroidSdkVersion = fallbackMinSdkVersion
        this.audioTranslationId = fallbackAudioXlationId
      }.build()
    }.build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerAndroidLocaleFactoryTest_TestApplicationComponent.builder()
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
      TestModule::class, LocaleProdModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(androidLocaleFactoryTest: AndroidLocaleFactoryTest)
  }

  private companion object {
    private val REGION_BRAZIL = RegionSupportDefinition.newBuilder().apply {
      region = OppiaRegion.BRAZIL
      regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
        ietfRegionTag = "BR"
      }.build()
    }.build()

    private val REGION_US = RegionSupportDefinition.newBuilder().apply {
      region = OppiaRegion.UNITED_STATES
      regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
        ietfRegionTag = "US"
      }.build()
    }.build()

    private val REGION_INDIA = RegionSupportDefinition.newBuilder().apply {
      region = OppiaRegion.INDIA
      regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
        ietfRegionTag = "IN"
      }.build()
    }.build()

    private val REGION_ZZ = RegionSupportDefinition.newBuilder().apply {
      region = OppiaRegion.REGION_UNSPECIFIED
      regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
        ietfRegionTag = "ZZ"
      }.build()
    }.build()

    private val EN_ANDROID_LANGUAGE_ID = AndroidLanguageId.newBuilder().apply {
      languageCode = "en"
    }.build()

    private val PT_BR_ANDROID_LANGUAGE_ID = AndroidLanguageId.newBuilder().apply {
      languageCode = "pt"
      regionCode = "BR"
    }.build()

    private val QQ_ANDROID_LANGUAGE_ID = AndroidLanguageId.newBuilder().apply {
      languageCode = "qq"
    }.build()

    private val PT_IETF_LANGUAGE_ID = IetfBcp47LanguageId.newBuilder().apply {
      ietfLanguageTag = "pt"
    }.build()

    private val HI_IETF_LANGUAGE_ID = IetfBcp47LanguageId.newBuilder().apply {
      ietfLanguageTag = "hi"
    }.build()

    private val PT_BR_IETF_LANGUAGE_ID = IetfBcp47LanguageId.newBuilder().apply {
      ietfLanguageTag = "pt-BR"
    }.build()

    // Has a valid language, but unsupported region.
    private val PT_ZZ_IETF_LANGUAGE_ID = IetfBcp47LanguageId.newBuilder().apply {
      ietfLanguageTag = "pt-ZZ"
    }.build()

    private val QQ_IETF_LANGUAGE_ID = IetfBcp47LanguageId.newBuilder().apply {
      ietfLanguageTag = "qq"
    }.build()

    private val QQ_ZZ_IETF_LANGUAGE_ID = IetfBcp47LanguageId.newBuilder().apply {
      ietfLanguageTag = "qq-ZZ"
    }.build()

    private val PT_BR_MACARONIC_LANGUAGE_ID = MacaronicLanguageId.newBuilder().apply {
      // This is a loose definition for macaronic language that's being done to test compatible
      // cases (though in reality macaronic languages aren't expected to ever match with system
      // locales).
      combinedLanguageCode = "pt-br"
    }.build()

    private val HI_IN_MACARONIC_LANGUAGE_ID = MacaronicLanguageId.newBuilder().apply {
      combinedLanguageCode = "hi-IN"
    }.build()

    private val HI_EN_MACARONIC_LANGUAGE_ID = MacaronicLanguageId.newBuilder().apply {
      combinedLanguageCode = "hi-EN"
    }.build()

    private val QQ_ZZ_MACARONIC_LANGUAGE_ID = MacaronicLanguageId.newBuilder().apply {
      combinedLanguageCode = "qq-zz"
    }.build()

    private val INVALID_MACARONIC_LANGUAGE_ID = MacaronicLanguageId.newBuilder().apply {
      combinedLanguageCode = "languagewithoutregion"
    }.build()
  }
}
