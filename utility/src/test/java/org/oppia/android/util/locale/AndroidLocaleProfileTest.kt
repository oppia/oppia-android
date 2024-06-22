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
import org.oppia.android.app.model.LanguageSupportDefinition.AndroidLanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.IetfBcp47LanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.LanguageId
import org.oppia.android.app.model.LanguageSupportDefinition.MacaronicLanguageId
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.app.model.RegionSupportDefinition.IetfBcp47RegionId
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.AndroidLocaleProfile.LanguageAndRegionProfile
import org.oppia.android.util.locale.AndroidLocaleProfile.LanguageAndWildcardRegionProfile
import org.oppia.android.util.locale.AndroidLocaleProfile.LanguageOnlyProfile
import org.oppia.android.util.locale.AndroidLocaleProfile.RegionOnlyProfile
import org.oppia.android.util.locale.AndroidLocaleProfile.RootProfile
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
  @Inject lateinit var androidLocaleProfileFactory: AndroidLocaleProfile.Factory

  private val brazilianPortugueseLocale by lazy { Locale("pt", "BR") }
  private val kenyaOnlyLocale by lazy { Locale(/* language = */ "", "KE") }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  /* Tests for createFrom */

  @Test
  fun testCreateProfile_fromRootLocale_returnsRootProfile() {
    val profile = androidLocaleProfileFactory.createFrom(Locale.ROOT)

    assertThat(profile).isEqualTo(RootProfile)
  }

  @Test
  fun testCreateProfile_fromEnglishLocale_returnsLanguageOnlyProfile() {
    val profile = androidLocaleProfileFactory.createFrom(Locale.ENGLISH)

    val languageOnlyProfile = profile as? LanguageOnlyProfile
    assertThat(profile).isInstanceOf(LanguageOnlyProfile::class.java)
    assertThat(languageOnlyProfile?.languageCode).isEqualTo("en")
  }

  @Test
  fun testCreateProfile_fromBrazilianPortuguese_returnsProfileWithLanguageAndRegion() {
    val profile = androidLocaleProfileFactory.createFrom(brazilianPortugueseLocale)

    val languageAndRegionProfile = profile as? LanguageAndRegionProfile
    assertThat(profile).isInstanceOf(LanguageAndRegionProfile::class.java)
    assertThat(languageAndRegionProfile?.languageCode).isEqualTo("pt")
    assertThat(languageAndRegionProfile?.regionCode).isEqualTo("br")
  }

  @Test
  fun testCreateProfile_fromKenyaLocale_returnsRegionOnlyProfile() {
    val profile = androidLocaleProfileFactory.createFrom(kenyaOnlyLocale)

    val regionOnlyProfile = profile as? RegionOnlyProfile
    assertThat(profile).isInstanceOf(RegionOnlyProfile::class.java)
    assertThat(regionOnlyProfile?.regionCode).isEqualTo("ke")
  }

  /* Tests for createFromIetfDefinitions */

  @Test
  fun testCreateProfileFromIetf_defaultLanguageId_nullRegion_returnsNull() {
    val profile =
      androidLocaleProfileFactory.createFromIetfDefinitions(
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

    val profile =
      androidLocaleProfileFactory.createFromIetfDefinitions(languageWithoutIetf, REGION_INDIA)

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

    val profile =
      androidLocaleProfileFactory.createFromIetfDefinitions(languageWithIetf, REGION_INDIA)

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

    val profile =
      androidLocaleProfileFactory.createFromIetfDefinitions(languageWithIetf, REGION_INDIA)

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

    val profile =
      androidLocaleProfileFactory.createFromIetfDefinitions(languageWithIetf, REGION_INDIA)

    // The constituent language code should come from the language ID, and the region code from the
    // provided region definition.
    val languageAndRegionProfile = profile as? LanguageAndRegionProfile
    assertThat(profile).isInstanceOf(LanguageAndRegionProfile::class.java)
    assertThat(languageAndRegionProfile?.languageCode).isEqualTo("pt")
    assertThat(languageAndRegionProfile?.regionCode).isEqualTo("in")
  }

  @Test
  fun testCreateProfileFromIetf_withIetfLanguageRegionTag_withRegion_returnsIetfRegionProfile() {
    val languageWithIetf = LanguageId.newBuilder().apply {
      ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "pt-BR"
      }.build()
    }.build()

    val profile =
      androidLocaleProfileFactory.createFromIetfDefinitions(languageWithIetf, REGION_INDIA)

    // In this case, the region comes from the IETF language tag since it's included.
    val languageAndRegionProfile = profile as? LanguageAndRegionProfile
    assertThat(profile).isInstanceOf(LanguageAndRegionProfile::class.java)
    assertThat(languageAndRegionProfile?.languageCode).isEqualTo("pt")
    assertThat(languageAndRegionProfile?.regionCode).isEqualTo("br")
  }

  @Test
  fun testCreateProfileFromIetf_languageIdWithIetfLanguageCode_withDefaultRegion_returnsNull() {
    val languageWithIetf = LanguageId.newBuilder().apply {
      ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "pt"
      }.build()
    }.build()

    val profile =
      androidLocaleProfileFactory.createFromIetfDefinitions(
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
      androidLocaleProfileFactory.createFromIetfDefinitions(
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
      androidLocaleProfileFactory.createFromIetfDefinitions(
        languageWithIetf, regionDefinition = null
      )

    // A null region specifically means to use a wildcard match for regions.
    val languageAndWildcardRegionProfile = profile as? LanguageAndWildcardRegionProfile
    assertThat(profile).isInstanceOf(LanguageAndWildcardRegionProfile::class.java)
    assertThat(languageAndWildcardRegionProfile?.languageCode).isEqualTo("pt")
  }

  /* Tests for createFromMacaronicLanguage */

  @Test
  fun testCreateProfileFromMacaronic_defaultLanguageId_returnsNull() {
    val profile =
      androidLocaleProfileFactory.createFromMacaronicLanguage(
        languageId = LanguageId.getDefaultInstance()
      )

    assertThat(profile).isNull()
  }

  @Test
  fun testCreateProfileFromMacaronic_languageIdWithoutMacaronic_returnsNull() {
    val languageWithoutMacaronic = LanguageId.newBuilder().apply {
      ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
        ietfLanguageTag = "pt"
      }.build()
    }.build()

    val profile = androidLocaleProfileFactory.createFromMacaronicLanguage(languageWithoutMacaronic)

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

    val profile = androidLocaleProfileFactory.createFromMacaronicLanguage(languageWithMacaronic)

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

    val profile = androidLocaleProfileFactory.createFromMacaronicLanguage(languageWithMacaronic)

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

    val profile = androidLocaleProfileFactory.createFromMacaronicLanguage(languageWithMacaronic)

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

    val profile = androidLocaleProfileFactory.createFromMacaronicLanguage(languageWithMacaronic)

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

    val profile = androidLocaleProfileFactory.createFromMacaronicLanguage(languageWithMacaronic)

    // The macaronic ID was valid. Verify that both language IDs correctly populate the profile.
    val languageAndRegionProfile = profile as? LanguageAndRegionProfile
    assertThat(profile).isInstanceOf(LanguageAndRegionProfile::class.java)
    assertThat(languageAndRegionProfile?.languageCode).isEqualTo("hi")
    assertThat(languageAndRegionProfile?.regionCode).isEqualTo("en")
  }

  /* Tests for createFromAndroidResourcesLanguageId(). */

  @Test
  fun testCreateFromAndroidResourcesLanguageId_defaultLanguageId_returnsNull() {
    val profile =
      androidLocaleProfileFactory.createFromAndroidResourcesLanguageId(
        languageId = LanguageId.getDefaultInstance()
      )

    assertThat(profile).isNull()
  }

  @Test
  fun testCreateFromAndroidResourcesLanguageId_ietfBcp47LanguageId_returnsNull() {
    val profile =
      androidLocaleProfileFactory.createFromAndroidResourcesLanguageId(
        languageId = LanguageId.newBuilder().apply {
          ietfBcp47Id = IetfBcp47LanguageId.newBuilder().apply {
            ietfLanguageTag = "pt-BR"
          }.build()
        }.build()
      )

    // This method only creates a profile when provided with a valid Android resources language ID.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateFromAndroidResourcesLanguageId_macaronicLanguageId_returnsNull() {
    val profile =
      androidLocaleProfileFactory.createFromAndroidResourcesLanguageId(
        languageId = LanguageId.newBuilder().apply {
          macaronicId = MacaronicLanguageId.newBuilder().apply {
            combinedLanguageCode = "hi-en"
          }.build()
        }.build()
      )

    // This method only creates a profile when provided with a valid Android resources language ID.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateFromAndroidResourcesLanguageId_defaultAndroidLanguageId_returnsNull() {
    val profile =
      androidLocaleProfileFactory.createFromAndroidResourcesLanguageId(
        languageId = LanguageId.newBuilder().apply {
          androidResourcesLanguageId = AndroidLanguageId.getDefaultInstance()
        }.build()
      )

    // This method only creates a profile when provided with a valid Android resources language ID.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateFromAndroidResourcesLanguageId_androidLanguageId_regionOnly_returnsNull() {
    val profile =
      androidLocaleProfileFactory.createFromAndroidResourcesLanguageId(
        languageId = LanguageId.newBuilder().apply {
          androidResourcesLanguageId = AndroidLanguageId.newBuilder().apply {
            regionCode = "BR"
          }.build()
        }.build()
      )

    // A valid Android language ID must include at least a language code.
    assertThat(profile).isNull()
  }

  @Test
  fun testCreateFromAndroidResourcesLanguageId_androidLanguageId_langOnly_returnsLangWildcard() {
    val profile =
      androidLocaleProfileFactory.createFromAndroidResourcesLanguageId(
        languageId = LanguageId.newBuilder().apply {
          androidResourcesLanguageId = AndroidLanguageId.newBuilder().apply {
            languageCode = "pt"
          }.build()
        }.build()
      )

    // If no region is provided, match against all regions.
    val languageAndWildcardRegionProfile = profile as? LanguageAndWildcardRegionProfile
    assertThat(profile).isInstanceOf(LanguageAndWildcardRegionProfile::class.java)
    assertThat(languageAndWildcardRegionProfile?.languageCode).isEqualTo("pt")
  }

  @Test
  fun testCreateFromAndroidResourcesLanguageId_androidLanguageId_returnsLangAndRegionProfile() {
    val profile =
      androidLocaleProfileFactory.createFromAndroidResourcesLanguageId(
        languageId = LanguageId.newBuilder().apply {
          androidResourcesLanguageId = AndroidLanguageId.newBuilder().apply {
            languageCode = "pt"
            regionCode = "BR"
          }.build()
        }.build()
      )

    // Both the language & region codes should be represented in the profile.
    val languageAndRegionProfile = profile as? LanguageAndRegionProfile
    assertThat(profile).isInstanceOf(LanguageAndRegionProfile::class.java)
    assertThat(languageAndRegionProfile?.languageCode).isEqualTo("pt")
    assertThat(languageAndRegionProfile?.regionCode).isEqualTo("br")
  }

  /* Tests for matches() */

  @Test
  fun testMatchProfile_rootProfile_andRootProfile_matches() {
    val profile1 = RootProfile
    val profile2 = RootProfile

    val matches = profile1.matches(profile2)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_rootProfile_andPtLanguageOnly_doNotMatch() {
    val profile1 = RootProfile
    val profile2 = LanguageOnlyProfile(languageCode = "pt")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_rootProfile_andBrRegionOnly_doNotMatch() {
    val profile1 = RootProfile
    val profile2 = RegionOnlyProfile(regionCode = "br")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_rootProfile_andPtBrProfile_doNotMatch() {
    val profile1 = RootProfile
    val profile2 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_rootProfile_andPtWildcardProfile_doNotMatch() {
    val profile1 = RootProfile
    val profile2 = LanguageAndWildcardRegionProfile(languageCode = "pt")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptLanguageOnly_andRootProfile_doNotMatch() {
    val profile1 = LanguageOnlyProfile(languageCode = "pt")
    val profile2 = RootProfile

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptLanguageOnly_andPtLanguageOnly_matches() {
    val profile1 = LanguageOnlyProfile(languageCode = "pt")
    val profile2 = LanguageOnlyProfile(languageCode = "pt")

    val matches = profile1.matches(profile2)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_ptLanguageOnly_andSwLanguageOnly_doNotMatch() {
    val profile1 = LanguageOnlyProfile(languageCode = "pt")
    val profile2 = LanguageOnlyProfile(languageCode = "sw")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptLanguageOnly_andBrRegionOnly_doNotMatch() {
    val profile1 = LanguageOnlyProfile(languageCode = "pt")
    val profile2 = RegionOnlyProfile(regionCode = "br")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptLanguageOnly_andPtBrProfile_doNotMatch() {
    val profile1 = LanguageOnlyProfile(languageCode = "pt")
    val profile2 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptLanguageOnly_andSwWildcardProfile_doNotMatch() {
    val profile1 = LanguageOnlyProfile(languageCode = "pt")
    val profile2 = LanguageAndWildcardRegionProfile(languageCode = "sw")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_brRegionOnly_andRootProfile_doNotMatch() {
    val profile1 = RegionOnlyProfile(regionCode = "br")
    val profile2 = RootProfile

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_brRegionOnly_andPtLanguageOnly_doNotMatch() {
    val profile1 = RegionOnlyProfile(regionCode = "br")
    val profile2 = LanguageOnlyProfile(languageCode = "pt")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_brRegionOnly_andBrRegionOnly_matches() {
    val profile1 = RegionOnlyProfile(regionCode = "br")
    val profile2 = RegionOnlyProfile(regionCode = "br")

    val matches = profile1.matches(profile2)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_brRegionOnly_andKeRegionOnly_doNotMatch() {
    val profile1 = RegionOnlyProfile(regionCode = "br")
    val profile2 = RegionOnlyProfile(regionCode = "ke")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_brRegionOnly_andPtBrProfile_doNotMatch() {
    val profile1 = RegionOnlyProfile(regionCode = "br")
    val profile2 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_brRegionOnly_andPtWildcardProfile_doNotMatch() {
    val profile1 = RegionOnlyProfile(regionCode = "br")
    val profile2 = LanguageAndWildcardRegionProfile(languageCode = "pt")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptBrProfile_andRootProfile_doNotMatch() {
    val profile1 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")
    val profile2 = RootProfile

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptBrProfile_andPtLanguageOnly_doNotMatch() {
    val profile1 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")
    val profile2 = LanguageOnlyProfile(languageCode = "pt")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptBrProfile_andBrRegionOnly_doNotMatch() {
    val profile1 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")
    val profile2 = RegionOnlyProfile(regionCode = "br")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptBrProfile_andPtBrProfile_matches() {
    val profile1 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")
    val profile2 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")

    val matches = profile1.matches(profile2)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_ptBrProfile_andSwBrProfile_doNotMatch() {
    val profile1 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")
    val profile2 =
      LanguageAndRegionProfile(languageCode = "sw", regionCode = "br", regionCodeUpperCase = "BR")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptBrProfile_andPtKeProfile_doNotMatch() {
    val profile1 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")
    val profile2 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "ke", regionCodeUpperCase = "KE")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptBrProfile_andSwKeProfile_doNotMatch() {
    val profile1 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")
    val profile2 =
      LanguageAndRegionProfile(languageCode = "sw", regionCode = "ke", regionCodeUpperCase = "KE")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptBrProfile_andSwWildcardProfile_doNotMatch() {
    val profile1 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")
    val profile2 = LanguageAndWildcardRegionProfile(languageCode = "sw")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptWildcardProfile_andRootProfile_doNotMatch() {
    val profile1 = LanguageAndWildcardRegionProfile(languageCode = "pt")
    val profile2 = RootProfile

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptWildcardProfile_andPtLanguageOnly_matches() {
    val profile1 = LanguageAndWildcardRegionProfile(languageCode = "pt")
    val profile2 = LanguageOnlyProfile(languageCode = "pt")

    val matches = profile1.matches(profile2)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_ptWildcardProfile_andSwLanguageOnly_doNotMatch() {
    val profile1 = LanguageAndWildcardRegionProfile(languageCode = "pt")
    val profile2 = LanguageOnlyProfile(languageCode = "sw")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptWildcardProfile_andBrRegionOnly_doNotMatch() {
    val profile1 = LanguageAndWildcardRegionProfile(languageCode = "pt")
    val profile2 = RegionOnlyProfile(regionCode = "br")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptWildcardProfile_andPtBrProfile_matches() {
    val profile1 = LanguageAndWildcardRegionProfile(languageCode = "pt")
    val profile2 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")

    val matches = profile1.matches(profile2)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_ptWildcardProfile_andSwBrProfile_doNotMatch() {
    val profile1 = LanguageAndWildcardRegionProfile(languageCode = "pt")
    val profile2 =
      LanguageAndRegionProfile(languageCode = "sw", regionCode = "br", regionCodeUpperCase = "BR")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptWildcardProfile_andPtKeProfile_matches() {
    val profile1 = LanguageAndWildcardRegionProfile(languageCode = "pt")
    val profile2 =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "ke", regionCodeUpperCase = "KE")

    val matches = profile1.matches(profile2)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_ptWildcardProfile_andSwKeProfile_doNotMatch() {
    val profile1 = LanguageAndWildcardRegionProfile(languageCode = "pt")
    val profile2 =
      LanguageAndRegionProfile(languageCode = "sw", regionCode = "ke", regionCodeUpperCase = "KE")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  @Test
  fun testMatchProfile_ptWildcardProfile_andPtWildcardProfile_matches() {
    val profile1 = LanguageAndWildcardRegionProfile(languageCode = "pt")
    val profile2 = LanguageAndWildcardRegionProfile(languageCode = "pt")

    val matches = profile1.matches(profile2)

    assertThat(matches).isTrue()
  }

  @Test
  fun testMatchProfile_ptWildcardProfile_andSwWildcardProfile_doNotMatch() {
    val profile1 = LanguageAndWildcardRegionProfile(languageCode = "pt")
    val profile2 = LanguageAndWildcardRegionProfile(languageCode = "sw")

    val matches = profile1.matches(profile2)

    assertThat(matches).isFalse()
  }

  /* Tests for computeIetfLanguageTag */

  @Test
  fun testIetfLanguageTag_rootProfile_isEmptyString() {
    val profile = RootProfile

    val ietfLanguageTag = profile.ietfLanguageTag

    assertThat(ietfLanguageTag).isEmpty()
  }

  @Test
  fun testIetfLanguageTag_languageOnlyProfile_isLanguageCode() {
    val profile = LanguageOnlyProfile(languageCode = "pt")

    val ietfLanguageTag = profile.ietfLanguageTag

    assertThat(ietfLanguageTag).isEqualTo("pt")
  }

  @Test
  fun testIetfLanguageTag_regionOnlyProfile_isRegionCode() {
    val profile = RegionOnlyProfile(regionCode = "br")

    val ietfLanguageTag = profile.ietfLanguageTag

    assertThat(ietfLanguageTag).isEqualTo("br")
  }

  @Test
  fun testIetfLanguageTag_languageWithRegionProfile_isIetfBcp47CombinedLanguageTag() {
    val profile =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")

    val ietfLanguageTag = profile.ietfLanguageTag

    assertThat(ietfLanguageTag).isEqualTo("pt-BR")
  }

  @Test
  fun testIetfLanguageTag_languageWithWildcardProfile_isLanguageCode() {
    val profile = LanguageAndWildcardRegionProfile(languageCode = "pt")

    val ietfLanguageTag = profile.ietfLanguageTag

    // The wildcard shouldn't be part of the IETF BCP 47 tag since that standard doesn't define such
    // a concept.
    assertThat(ietfLanguageTag).isEqualTo("pt")
  }

  /* Tests for computeAndroidLocale() */

  @Test
  fun testComputeAndroidLocale_rootProfile_returnsRootLocale() {
    val profile = RootProfile

    val locale = profile.computeAndroidLocale()

    assertThat(locale).isEqualTo(Locale.ROOT)
  }

  @Test
  fun testComputeAndroidLocale_languageOnlyProfile_returnsLocaleWithLanguageAndEmptyCountry() {
    val profile = LanguageOnlyProfile(languageCode = "pt")

    val locale = profile.computeAndroidLocale()

    assertThat(locale.language).ignoringCase().isEqualTo("pt")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testComputeAndroidLocale_regionOnlyProfile_returnsLocaleWithCountryAndEmptyLanguage() {
    val profile = RegionOnlyProfile(regionCode = "br")

    val locale = profile.computeAndroidLocale()

    assertThat(locale.country).ignoringCase().isEqualTo("br")
    assertThat(locale.language).isEmpty()
  }

  @Test
  fun testComputeAndroidLocale_languageWithWildcardProfile_returnsLocaleWithLangAndEmptyCountry() {
    val profile = LanguageAndWildcardRegionProfile(languageCode = "pt")

    val locale = profile.computeAndroidLocale()

    assertThat(locale.language).ignoringCase().isEqualTo("pt")
    assertThat(locale.country).isEmpty()
  }

  @Test
  fun testComputeAndroidLocale_languageAndRegionProfile_returnsLocaleWithLanguageAndCountry() {
    val profile =
      LanguageAndRegionProfile(languageCode = "pt", regionCode = "br", regionCodeUpperCase = "BR")

    val locale = profile.computeAndroidLocale()

    assertThat(locale.language).ignoringCase().isEqualTo("pt")
    assertThat(locale.country).ignoringCase().isEqualTo("br")
  }

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
