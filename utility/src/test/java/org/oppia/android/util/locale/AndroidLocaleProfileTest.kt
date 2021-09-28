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
import org.oppia.android.app.model.LanguageSupportDefinition.IetfBcp47LanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.MacaronicLanguageId
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.app.model.RegionSupportDefinition.IetfBcp47RegionId
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [AndroidLocaleProfile]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class AndroidLocaleProfileTest {
  @Inject
  lateinit var machineLocale: OppiaLocale.MachineLocale

  private val portugueseLocale by lazy { Locale("pt") }
  private val brazilianPortugueseLocale by lazy { Locale("pt", "BR") }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  /* Tests for createFrom */

  @Test
  fun testCreateProfile_fromRootLocale_returnsProfileWithoutLanguageAndRegionCode() {
    val profile = AndroidLocaleProfile.createFrom(Locale.ROOT)

    assertThat(profile.languageCode).isEmpty()
    assertThat(profile.regionCode).isEmpty()
  }

  @Test
  fun testCreateProfile_fromEnglishLocale_returnsProfileWithLanguageAndWithoutRegion() {
    val profile = AndroidLocaleProfile.createFrom(Locale.ENGLISH)

    assertThat(profile.languageCode).isEqualTo("en")
    assertThat(profile.regionCode).isEmpty()
  }

  @Test
  fun testCreateProfile_fromBrazilianPortuguese_returnsProfileWithLanguageAndRegion() {
    val profile = AndroidLocaleProfile.createFrom(brazilianPortugueseLocale)

    assertThat(profile.languageCode).isEqualTo("pt")
    assertThat(profile.regionCode).isEqualTo("BR")
  }

  /* Tests for createFromIetfDefinitions */

  @Test
  fun testCreateProfileFromIetf_defaultLanguageId_nullRegion_returnsNull() {
    val profile =
      AndroidLocaleProfile.createFromIetfDefinitions(
        languageId = LanguageId.getDefaultInstance(), regionDefinition = null
      )

    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromIetf_languageIdWithoutIetf_withRegion_returnsNull() {
    val languageWithoutIetf = LanguageId.newBuilder().apply {
      macaronicId = MacaronicLanguageId.newBuilder().apply {
        combinedLanguageCode = "hi-en"
      }.build()
    }.build()

    val profile = AndroidLocaleProfile.createFromIetfDefinitions(languageWithoutIetf, REGION_INDIA)

    // The language ID needs to have an IETF BCP 47 ID defined.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromIetf_languageIdWithEmptyIetf_withRegion_returnsNull() {
    val languageWithIetf = LanguageId.newBuilder().apply {
      ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = ""
      }.build()
    }.build()

    val profile = AndroidLocaleProfile.createFromIetfDefinitions(languageWithIetf, REGION_INDIA)

    // The language ID needs to have an IETF BCP 47 ID defined.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromIetf_languageIdWithMalformedIetf_withRegion_returnsNull() {
    val languageWithIetf = LanguageId.newBuilder().apply {
      ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "mal-form-ed"
      }.build()
    }.build()

    val profile = AndroidLocaleProfile.createFromIetfDefinitions(languageWithIetf, REGION_INDIA)

    // The language ID needs to have a well-formed IETF BCP 47 ID defined.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromIetf_languageIdWithIetfLanguageCode_withRegion_returnsCombinedProfile() {
    val languageWithIetf = LanguageId.newBuilder().apply {
      ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "pt"
      }.build()
    }.build()

    val profile = AndroidLocaleProfile.createFromIetfDefinitions(languageWithIetf, REGION_INDIA)

    // The constituent language code should come from the language ID, and the region code from the
    // provided region definition.
    assertThat(profile?.languageCode).isEqualTo("pt")
    assertThat(profile?.regionCode).isEqualTo("IN")
  }

  @Test
  fun testCreateProfileFromIetf_withIetfLanguageRegionTag_withRegion_returnsIetfRegionProfile() {
    val languageWithIetf = LanguageId.newBuilder().apply {
      ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "pt-BR"
      }.build()
    }.build()

    val profile = AndroidLocaleProfile.createFromIetfDefinitions(languageWithIetf, REGION_INDIA)

    // In this case, the region comes from the IETF language tag since it's included.
    assertThat(profile?.languageCode).isEqualTo("pt")
    assertThat(profile?.regionCode).isEqualTo("BR")
  }

  @Test
  fun testCreateProfileFromIetf_languageIdWithIetfLanguageCode_withDefaultRegion_returnsNull() {
    val languageWithIetf = LanguageId.newBuilder().apply {
      ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "pt"
      }.build()
    }.build()

    val profile =
      AndroidLocaleProfile.createFromIetfDefinitions(
        languageWithIetf, regionDefinition = RegionSupportDefinition.getDefaultInstance()
      )

    // The region is needed in this case, so it needs to be provided.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromIetf_languageIdWithIetfLanguageCode_withEmptyRegion_returnsNull() {
    val languageWithIetf = LanguageId.newBuilder().apply {
      ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "pt"
      }.build()
    }.build()

    val profile =
      AndroidLocaleProfile.createFromIetfDefinitions(
        languageWithIetf, regionDefinition = RegionSupportDefinition.getDefaultInstance()
      )

    // The region is needed in this case, so a valid one needs to be provided.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromIetf_withIetfLanguageCode_withNullRegion_returnsWildcardProfile() {
    val languageWithIetf = LanguageId.newBuilder().apply {
      ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "pt"
      }.build()
    }.build()

    val profile =
      AndroidLocaleProfile.createFromIetfDefinitions(languageWithIetf, regionDefinition = null)

    // A null region specifically means to use a wildcard match for regions.
    assertThat(profile?.languageCode).isEqualTo("pt")
    assertThat(profile?.regionCode).isEqualTo(AndroidLocaleProfile.REGION_WILDCARD)
  }

  /* Tests for createFromMacaronicLanguage */

  @Test
  fun testCreateProfileFromMacaronic_defaultLanguageId_returnsNull() {
    val profile =
      AndroidLocaleProfile.createFromMacaronicLanguage(languageId = LanguageId.getDefaultInstance())

    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromMacaronic_languageIdWithoutMacaronic_returnsNull() {
    val languageWithoutMacaronic = LanguageId.newBuilder().apply {
      ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "pt"
      }.build()
    }.build()

    val profile =
      AndroidLocaleProfile.createFromMacaronicLanguage(languageWithoutMacaronic)

    // The provided language ID must have a macaronic ID defined.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromMacaronic_languageIdWithEmptyMacaronic_returnsNull() {
    val languageWithMacaronic = LanguageId.newBuilder().apply {
      macaronicId = MacaronicLanguageId.newBuilder().apply {
        combinedLanguageCode = ""
      }.build()
    }.build()

    val profile =
      AndroidLocaleProfile.createFromMacaronicLanguage(languageWithMacaronic)

    // The provided language ID must have a macaronic ID defined.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromMacaronic_languageIdWithMalformedMacaronic_extraFields__returnsNull() {
    val languageWithMacaronic = LanguageId.newBuilder().apply {
      macaronicId = MacaronicLanguageId.newBuilder().apply {
        combinedLanguageCode = "mal-form-ed"
      }.build()
    }.build()

    val profile =
      AndroidLocaleProfile.createFromMacaronicLanguage(languageWithMacaronic)

    // The provided language ID must have a well-formed macaronic ID defined.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromMacaronic_languageId_malformedMacaronic_missingSecondLang_returnsNull() {
    val languageWithMacaronic = LanguageId.newBuilder().apply {
      macaronicId = MacaronicLanguageId.newBuilder().apply {
        combinedLanguageCode = "hi"
      }.build()
    }.build()

    val profile =
      AndroidLocaleProfile.createFromMacaronicLanguage(languageWithMacaronic)

    // The provided language ID must have a well-formed macaronic ID defined, that is, it must have
    // two language parts defined.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromMacaronic_languageId_emptyMacaronicRegion_returnsNull() {
    val languageWithMacaronic = LanguageId.newBuilder().apply {
      macaronicId = MacaronicLanguageId.newBuilder().apply {
        combinedLanguageCode = "hi-"
      }.build()
    }.build()

    val profile =
      AndroidLocaleProfile.createFromMacaronicLanguage(languageWithMacaronic)

    // The macaronic ID has two parts as expected, but the second language ID must be filled in.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromMacaronic_languageIdWithValidMacaronic_returnsProfile() {
    val languageWithMacaronic = LanguageId.newBuilder().apply {
      macaronicId = MacaronicLanguageId.newBuilder().apply {
        combinedLanguageCode = "hi-en"
      }.build()
    }.build()

    val profile =
      AndroidLocaleProfile.createFromMacaronicLanguage(languageWithMacaronic)

    // The macaronic ID was valid. Verify that both language IDs correctly populate the profile.
    assertThat(profile?.languageCode).isEqualTo("hi")
    assertThat(profile?.regionCode).isEqualTo("en")
  }

  /* Tests for matches() */

  @Test
  fun testMatchProfile_rootProfile_withItself_match() {
    val profile = AndroidLocaleProfile.createFrom(Locale.ROOT)

    val matches = profile.matches(machineLocale, profile)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_englishProfile_withItself_match() {
    val profile = AndroidLocaleProfile.createFrom(Locale.ENGLISH)

    val matches = profile.matches(machineLocale, profile)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_brazilianPortuguese_withItself_match() {
    val profile = AndroidLocaleProfile.createFrom(brazilianPortugueseLocale)

    val matches = profile.matches(machineLocale, profile)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_englishProfile_withItselfInDifferentCase_match() {
    val englishProfileLowercase = AndroidLocaleProfile(languageCode = "en", regionCode = "")
    val englishProfileUppercase = AndroidLocaleProfile(languageCode = "EN", regionCode = "")

    val matches = englishProfileLowercase.matches(machineLocale, englishProfileUppercase)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_englishProfile_withItselfInDifferentCase_reversed_match() {
    val englishProfileLowercase = AndroidLocaleProfile(languageCode = "en", regionCode = "")
    val englishProfileUppercase = AndroidLocaleProfile(languageCode = "EN", regionCode = "")

    val matches = englishProfileUppercase.matches(machineLocale, englishProfileLowercase)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_brazilianPortuguese_withItselfInDifferentCase_match() {
    val brazilianPortugueseProfileLowercase =
      AndroidLocaleProfile(languageCode = "pt", regionCode = "br")
    val brazilianPortugueseProfileUppercase =
      AndroidLocaleProfile(languageCode = "PT", regionCode = "BR")

    val matches =
      brazilianPortugueseProfileLowercase.matches(
        machineLocale, brazilianPortugueseProfileUppercase
      )

    assertThat(matches).isTrue()
  }

  fun testMatchProfile_brazilianPortuguese_withItselfInDifferentCase_reversed_match() {
    val brazilianPortugueseProfileLowercase =
      AndroidLocaleProfile(languageCode = "pt", regionCode = "br")
    val brazilianPortugueseProfileUppercase =
      AndroidLocaleProfile(languageCode = "PT", regionCode = "BR")

    val matches =
      brazilianPortugueseProfileUppercase.matches(
        machineLocale, brazilianPortugueseProfileLowercase
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_rootProfile_english_doNotMatch() {
    val rootProfile = AndroidLocaleProfile.createFrom(Locale.ROOT)
    val englishProfile = AndroidLocaleProfile.createFrom(Locale.ENGLISH)

    val matches = rootProfile.matches(machineLocale, englishProfile)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_rootProfile_brazilianPortuguese_doNotMatch() {
    val rootProfile = AndroidLocaleProfile.createFrom(Locale.ROOT)
    val brazilianPortugueseProfile = AndroidLocaleProfile.createFrom(brazilianPortugueseLocale)

    val matches = rootProfile.matches(machineLocale, brazilianPortugueseProfile)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_english_brazilianPortuguese_doNotMatch() {
    val englishProfile = AndroidLocaleProfile.createFrom(Locale.ENGLISH)
    val brazilianPortugueseProfile = AndroidLocaleProfile.createFrom(brazilianPortugueseLocale)

    val matches = englishProfile.matches(machineLocale, brazilianPortugueseProfile)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_rootProfile_englishWithWildcard_doNotMatch() {
    val rootProfile = AndroidLocaleProfile.createFrom(Locale.ROOT)
    val englishWithWildcardProfile = createProfileWithWildcard(languageCode = "en")

    val matches = rootProfile.matches(machineLocale, englishWithWildcardProfile)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_rootProfile_rootProfileWithWildcard_match() {
    val rootProfile = AndroidLocaleProfile.createFrom(Locale.ROOT)
    val rootProfileWithWildcardProfile = createProfileWithWildcard(languageCode = "")

    val matches = rootProfile.matches(machineLocale, rootProfileWithWildcardProfile)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_rootProfileWithWildcard_rootProfile_match() {
    val rootProfile = AndroidLocaleProfile.createFrom(Locale.ROOT)
    val rootProfileWithWildcardProfile = createProfileWithWildcard(languageCode = "")

    val matches = rootProfileWithWildcardProfile.matches(machineLocale, rootProfile)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_englishProfile_rootProfileWithWildcard_doNotMatch() {
    val englishProfile = AndroidLocaleProfile.createFrom(Locale.ENGLISH)
    val rootProfileWithWildcardProfile = createProfileWithWildcard(languageCode = "")

    val matches = englishProfile.matches(machineLocale, rootProfileWithWildcardProfile)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_englishWithWildcard_brazilianPortuguese_doNotMatch() {
    val englishWithWildcardProfile = createProfileWithWildcard(languageCode = "en")
    val brazilianPortugueseProfile = AndroidLocaleProfile.createFrom(brazilianPortugueseLocale)

    val matches = englishWithWildcardProfile.matches(machineLocale, brazilianPortugueseProfile)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_brazilianPortuguese_portuguese_doNotMatch() {
    val portugueseProfile = AndroidLocaleProfile.createFrom(portugueseLocale)
    val brazilianPortugueseProfile = AndroidLocaleProfile.createFrom(brazilianPortugueseLocale)

    val matches = portugueseProfile.matches(machineLocale, brazilianPortugueseProfile)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_brazilianPortuguese_portugueseWithWildcard_match() {
    val portugueseWithWildcardProfile = createProfileWithWildcard(languageCode = "pt")
    val brazilianPortugueseProfile = AndroidLocaleProfile.createFrom(brazilianPortugueseLocale)

    val matches = brazilianPortugueseProfile.matches(machineLocale, portugueseWithWildcardProfile)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_portugueseWithWildcard_brazilianPortuguese_match() {
    val portugueseWithWildcardProfile = createProfileWithWildcard(languageCode = "pt")
    val brazilianPortugueseProfile = AndroidLocaleProfile.createFrom(brazilianPortugueseLocale)

    val matches = portugueseWithWildcardProfile.matches(machineLocale, brazilianPortugueseProfile)

    assertThat(matches).isTrue()
  }

  private fun createProfileWithWildcard(languageCode: String): AndroidLocaleProfile =
    AndroidLocaleProfile(languageCode, regionCode = AndroidLocaleProfile.REGION_WILDCARD)

  private fun setUpTestApplicationComponent() {
    DaggerAndroidLocaleProfileTest_TestApplicationComponent.builder()
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

    fun inject(androidLocaleProfileTest: AndroidLocaleProfileTest)
  }

  private companion object {
    private val REGION_INDIA = RegionSupportDefinition.newBuilder().apply {
      region = OppiaRegion.INDIA
      regionId = IetfBcp47RegionId.newBuilder().apply {
        ietfRegionTag = "IN"
      }.build()
    }.build()
  }
}
