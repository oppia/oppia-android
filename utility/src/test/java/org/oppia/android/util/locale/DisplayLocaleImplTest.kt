package org.oppia.android.util.locale

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.core.view.ViewCompat
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
import org.oppia.android.app.model.OppiaLocaleContext
import org.oppia.android.app.model.OppiaRegion
import org.oppia.android.app.model.RegionSupportDefinition
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.locale.testing.TestOppiaBidiFormatter
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [DisplayLocaleImpl]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class DisplayLocaleImplTest {
  @Inject
  lateinit var machineLocale: OppiaLocale.MachineLocale

  @Inject
  lateinit var androidLocaleFactory: AndroidLocaleFactory

  @Inject
  lateinit var formatterFactory: OppiaBidiFormatter.Factory

  @Inject
  lateinit var wrapperChecker: TestOppiaBidiFormatter.Checker

  @Inject
  lateinit var context: Context

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testCreateDisplayLocaleImpl_defaultInstance_hasDefaultInstanceContext() {
    val impl = createDisplayLocaleImpl(OppiaLocaleContext.getDefaultInstance())

    assertThat(impl.localeContext).isEqualToDefaultInstance()
  }

  @Test
  fun testCreateDisplayLocaleImpl_forProvidedContext_hasCorrectInstanceContext() {
    val impl = createDisplayLocaleImpl(EGYPT_ARABIC_CONTEXT)

    assertThat(impl.localeContext).isEqualTo(EGYPT_ARABIC_CONTEXT)
  }

  @Test
  fun testToString_returnsNonDefaultString() {
    val impl = createDisplayLocaleImpl(OppiaLocaleContext.getDefaultInstance())

    val str = impl.toString()

    // Verify that the string includes some details about the implementation (this is a potentially
    // fragile test).
    assertThat(str).contains("OppiaLocaleContext")
  }

  @Test
  fun testEquals_withNullValue_returnsFalse() {
    val impl = createDisplayLocaleImpl(OppiaLocaleContext.getDefaultInstance())

    assertThat(impl).isNotEqualTo(null)
  }

  @Test
  fun testEquals_withSameObject_returnsTrue() {
    val impl = createDisplayLocaleImpl(OppiaLocaleContext.getDefaultInstance())

    assertThat(impl).isEqualTo(impl)
  }

  @Test
  fun testEquals_twoDifferentInstancesWithDifferentContexts_returnsFalse() {
    val impl1 = createDisplayLocaleImpl(EGYPT_ARABIC_CONTEXT)
    val impl2 = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    assertThat(impl1).isNotEqualTo(impl2)
  }

  @Test
  fun testEquals_twoDifferentInstancesWithDifferentContexts_reversed_returnsFalse() {
    val impl1 = createDisplayLocaleImpl(EGYPT_ARABIC_CONTEXT)
    val impl2 = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    assertThat(impl2).isNotEqualTo(impl1)
  }

  @Test
  fun testEquals_twoDifferentInstancesWithSameContexts_returnsTrue() {
    val impl1 = createDisplayLocaleImpl(EGYPT_ARABIC_CONTEXT)
    // Create a copy of the proto, too.
    val impl2 = createDisplayLocaleImpl(EGYPT_ARABIC_CONTEXT.toBuilder().build())

    // This is somewhat testing the implementation of data classes, but it's important to verify
    // that the implementation correctly satisfies the contract outlined in OppiaLocale.
    assertThat(impl1).isEqualTo(impl2)
  }

  @Test
  fun testEquals_twoDifferentInstancesWithSameContexts_reversed_returnsTrue() {
    val impl1 = createDisplayLocaleImpl(EGYPT_ARABIC_CONTEXT)
    val impl2 = createDisplayLocaleImpl(EGYPT_ARABIC_CONTEXT.toBuilder().build())

    assertThat(impl2).isEqualTo(impl1)
  }

  @Test
  fun testFormatLong_forLargeLong_returnsStringWithExactDigits() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val formattedString = impl.formatLong(123456789)

    assertThat(formattedString.filter { it.isDigit() }).isEqualTo("123456789")
  }

  @Test
  fun testFormatLong_forDouble_returnsStringWithExactDigits() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val formattedString = impl.formatDouble(454545456.123)

    val digitsOnly = formattedString.filter { it.isDigit() }
    assertThat(digitsOnly).contains("454545456")
    assertThat(digitsOnly).contains("123")
  }

  @Test
  fun testFormatLong_forDouble_returnsStringWithPeriodsOrCommas() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val formattedString = impl.formatDouble(123456789.123)

    // Depending on formatting, commas and/or periods are used for large doubles.
    assertThat(formattedString).containsMatch("[,.]")
  }

  @Test
  fun testToHumanReadableString_forInt_returnsStringWithExactNumber() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val formattedString = impl.toHumanReadableString(1)

    assertThat(formattedString).contains("1")
  }

  @Test
  fun testComputeDateString_forFixedTime_returnMonthDayYearParts() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val dateString = impl.computeDateString(MORNING_UTC_TIMESTAMP_MILLIS)

    assertThat(dateString.extractNumbers()).containsExactly("24", "2019")
    assertThat(dateString).contains("Apr")
  }

  @Test
  fun testComputeTimeString_forFixedTime_returnMinuteHourParts() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val timeString = impl.computeTimeString(MORNING_UTC_TIMESTAMP_MILLIS)

    assertThat(timeString.extractNumbers()).containsExactly("22", "8")
  }

  @Test
  fun testComputeDateTimeString_forFixedTime_returnsMinHourMonthDayYearParts() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val dateTimeString = impl.computeDateTimeString(MORNING_UTC_TIMESTAMP_MILLIS)

    assertThat(dateTimeString.extractNumbers()).containsExactly("22", "8", "24", "2019")
    assertThat(dateTimeString).contains("Apr")
  }

  @Test
  fun testGetLayoutDirection_englishContext_returnsLeftToRight() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val layoutDirection = impl.getLayoutDirection()

    assertThat(layoutDirection).isEqualTo(ViewCompat.LAYOUT_DIRECTION_LTR)
  }

  @Test
  fun testGetLayoutDirection_arabicContext_returnsRightToLeft() {
    val impl = createDisplayLocaleImpl(EGYPT_ARABIC_CONTEXT)

    val layoutDirection = impl.getLayoutDirection()

    assertThat(layoutDirection).isEqualTo(ViewCompat.LAYOUT_DIRECTION_RTL)
  }

  @Test
  fun testFormatInLocaleWithWrapping_formatStringWithArgs_returnsCorrectlyFormattedString() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val formatted = impl.run { "Test with %s and %s".formatInLocaleWithWrapping("string", "11") }

    assertThat(formatted).isEqualTo("Test with string and 11")
  }

  @Test
  fun testFormatInLocaleWithWrapping_properlyWrapsArguments() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    impl.run { "Test with %s and %s".formatInLocaleWithWrapping("string", "11") }

    // Verify that both arguments were wrapped.
    assertThat(wrapperChecker.getAllWrappedUnicodeTexts()).containsExactly("string", "11")
  }

  @Test
  fun testFormatInLocaleWithoutWrapping_formatStringWithArgs_returnsCorrectlyFormattedString() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val formatted = impl.run { "Test with %s and %s".formatInLocaleWithoutWrapping("string", "11") }

    assertThat(formatted).isEqualTo("Test with string and 11")
  }

  @Test
  fun testFormatInLocaleWithoutWrapping_doesNotWrapArguments() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    impl.run { "Test with %s and %s".formatInLocaleWithoutWrapping("string", "11") }

    // Verify that none of the arguments were wrapped.
    assertThat(wrapperChecker.getAllWrappedUnicodeTexts()).isEmpty()
  }

  @Test
  fun testCapitalizeForHumans_capitalizedString_returnsSameString() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val capitalized = impl.run { "Title String".capitalizeForHumans() }

    assertThat(capitalized).isEqualTo("Title String")
  }

  @Test
  fun testCapitalizeForHumans_uncapitalizedString_returnsCapitalized() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val capitalized = impl.run { "lowercased string".capitalizeForHumans() }

    assertThat(capitalized).isEqualTo("Lowercased string")
  }

  @Test
  fun testNumbersForHumans_egyptArabicLocale_numberFormat_returnsHumanReadableString() {
    val impl = createDisplayLocaleImpl(EGYPT_ARABIC_CONTEXT)

    val localizedNumber = impl.toHumanReadableString(1)

    assertThat(localizedNumber).isEqualTo("١")
  }

  @Test
  fun testNumbersForHumans_turkishLocale_numberFormat_returnsHumanReadableString() {
    val impl = createDisplayLocaleImpl(TURKEY_TURKISH_CONTEXT)

    val localizedNumber = impl.toHumanReadableString(1)

    assertThat(localizedNumber).isEqualTo("1")
  }

  @Test
  fun testNumbersForHumans_englishLocale_numberFormat_returnsHumanReadableString() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val localizedNumber = impl.toHumanReadableString(1)

    assertThat(localizedNumber).isEqualTo("1")
  }

  @Test
  fun testCapitalizeForHumans_englishLocale_localeSensitiveCharAtStart_returnsConvertedCase() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)

    val capitalized = impl.run { "igloo".capitalizeForHumans() }

    assertThat(capitalized).isEqualTo("Igloo")
  }

  @Test
  fun testCapitalizeForHumans_turkishLocale_localeSensitiveCharAtStart_returnsIncorrectCase() {
    val impl = createDisplayLocaleImpl(TURKEY_TURKISH_CONTEXT)

    val capitalized = impl.run { "igloo".capitalizeForHumans() }

    // Note that the starting letter differs when being capitalized with a Turkish context (as
    // compared with the English version of this test). See https://stackoverflow.com/a/11063161 for
    // context on how casing behaviors differ based on Locales in Java.
    assertThat(capitalized).isEqualTo("İgloo")
  }

  @Test
  fun testGetStringInLocale_validId_returnsResourceStringForId() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    val str = impl.run { resources.getStringInLocale(R.string.test_basic_string) }

    assertThat(str).isEqualTo("Basic string")
  }

  @Test
  fun testGetStringInLocale_nonExistentId_throwsException() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    assertThrows(Resources.NotFoundException::class) {
      impl.run { resources.getStringInLocale(-1) }
    }
  }

  @Test
  fun testGetStringInLocaleWithWrapping_formatStringResourceWithArgs_returnsFormattedString() {
    val impl = createDisplayLocaleImpl(HEBREW_CONTEXT)
    val resources = context.resources

    val str = impl.run {
      resources.getStringInLocaleWithWrapping(
        R.string.test_string_with_arg_hebrew, "123 Some Street, Mountain View, CA"
      )
    }

    // This is based on the example here:
    // https://developer.android.com/training/basics/supporting-devices/languages#FormatTextExplanationSolution.
    assertThat(str)
      .isEqualTo("האם התכוונת ל \u200F\u202A123 Some Street, Mountain View, CA\u202C\u200F")
  }

  @Test
  fun testGetStringInLocaleWithWrapping_properlyWrapsArguments() {
    val impl = createDisplayLocaleImpl(HEBREW_CONTEXT)
    val resources = context.resources

    impl.run {
      resources.getStringInLocaleWithWrapping(
        R.string.test_string_with_arg_hebrew, "123 Some Street, Mountain View, CA"
      )
    }

    // Verify that the argument was wrapped.
    assertThat(wrapperChecker.getAllWrappedUnicodeTexts())
      .containsExactly("123 Some Street, Mountain View, CA")
  }

  @Test
  fun testGetStringInLocaleWithWrapping_nonExistentId_throwsException() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    assertThrows(Resources.NotFoundException::class) {
      impl.run { resources.getStringInLocaleWithWrapping(-1) }
    }
  }

  @Test
  fun testGetStringInLocaleWithoutWrapping_formatStringResourceWithArgs_returnsFormattedString() {
    val impl = createDisplayLocaleImpl(HEBREW_CONTEXT)
    val resources = context.resources

    val str = impl.run {
      resources.getStringInLocaleWithoutWrapping(
        R.string.test_string_with_arg_hebrew, "123 Some Street, Mountain View, CA"
      )
    }

    // This is based on the example here:
    // https://developer.android.com/training/basics/supporting-devices/languages#FormatTextExplanationSolution.
    // Note that the string is formatted, but due to no bidirectional wrapping the address ends up
    // incorrectly formatted.
    assertThat(str).isEqualTo("האם התכוונת ל 123 Some Street, Mountain View, CA")
  }

  @Test
  fun testGetStringInLocaleWithoutWrapping_doesNotWrapArguments() {
    val impl = createDisplayLocaleImpl(HEBREW_CONTEXT)
    val resources = context.resources

    impl.run {
      resources.getStringInLocaleWithoutWrapping(
        R.string.test_string_with_arg_hebrew, "123 Some Street, Mountain View, CA"
      )
    }

    // Verify that no arguments were wrapped.
    assertThat(wrapperChecker.getAllWrappedUnicodeTexts()).isEmpty()
  }

  @Test
  fun testGetStringInLocaleWithoutWrapping_nonExistentId_throwsException() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    assertThrows(Resources.NotFoundException::class) {
      impl.run { resources.getStringInLocaleWithoutWrapping(-1) }
    }
  }

  @Test
  fun testGetStringArrayInLocale_validId_returnsArrayAsStringList() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    val strList = impl.run { resources.getStringArrayInLocale(R.array.test_str_array) }

    assertThat(strList).containsExactly("Basic string", "Basic string2").inOrder()
  }

  @Test
  fun testGetStringArrayInLocale_nonExistentId_throwsException() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    assertThrows(Resources.NotFoundException::class) {
      impl.run { resources.getStringArrayInLocale(-1) }
    }
  }

  @Test
  fun testGetQuantityStringInLocale_validId_oneItem_returnsQuantityStringForSingleItem() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    val str = impl.run {
      resources.getQuantityStringInLocale(R.plurals.test_plural_string_no_args, 1)
    }

    assertThat(str).isEqualTo("1 item")
  }

  @Test
  fun testGetQuantityStringInLocale_validId_twoItems_returnsQuantityStringForMultipleItems() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    val str = impl.run {
      resources.getQuantityStringInLocale(R.plurals.test_plural_string_no_args, 2)
    }

    // Note that the 'other' case covers most scenarios in English (per
    // https://issuetracker.google.com/issues/36917255).
    assertThat(str).isEqualTo("2 items")
  }

  @Test
  fun testGetQuantityStringInLocale_nonExistentId_throwsException() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    assertThrows(Resources.NotFoundException::class) {
      impl.run { resources.getQuantityStringInLocale(-1, 0) }
    }
  }

  @Test
  fun testGetQuantityStringInLocaleWithWrapping_formatStrResourceWithArgs_returnsFormattedStr() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    val str = impl.run {
      resources.getQuantityStringInLocaleWithWrapping(
        R.plurals.test_plural_string_with_args, 2, "Two"
      )
    }

    assertThat(str).isEqualTo("Two items")
  }

  @Test
  fun testGetQuantityStringInLocaleWithWrapping_properlyWrapsArguments() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    impl.run {
      resources.getQuantityStringInLocaleWithWrapping(
        R.plurals.test_plural_string_with_args, 2, "Two"
      )
    }

    // Verify that the argument was wrapped.
    assertThat(wrapperChecker.getAllWrappedUnicodeTexts()).containsExactly("Two")
  }

  @Test
  fun testGetQuantityStringInLocaleWithWrapping_nonExistentId_throwsException() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    assertThrows(Resources.NotFoundException::class) {
      impl.run { resources.getQuantityStringInLocaleWithWrapping(-1, 0) }
    }
  }

  @Test
  fun testGetQuantityStringInLocaleWithoutWrapping_formatStrResourceWithArgs_returnsFormattedStr() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    val str = impl.run {
      resources.getQuantityStringInLocaleWithoutWrapping(
        R.plurals.test_plural_string_with_args, 2, "Two"
      )
    }

    assertThat(str).isEqualTo("Two items")
  }

  @Test
  fun testGetQuantityStringInLocaleWithoutWrapping_doesNotWrapArguments() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    impl.run {
      resources.getQuantityStringInLocaleWithoutWrapping(
        R.plurals.test_plural_string_with_args, 2, "Two"
      )
    }

    // Verify that no arguments were wrapped.
    assertThat(wrapperChecker.getAllWrappedUnicodeTexts()).isEmpty()
  }

  @Test
  fun testGetQuantityStringInLocaleWithoutWrapping_nonExistentId_throwsException() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    assertThrows(Resources.NotFoundException::class) {
      impl.run { resources.getQuantityStringInLocaleWithoutWrapping(-1, 0) }
    }
  }

  @Test
  fun testGetQuantityTextInLocale_validId_oneItem_returnsQuantityStringForSingleItem() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    val str = impl.run {
      resources.getQuantityTextInLocale(R.plurals.test_plural_string_no_args, 1)
    }

    assertThat(str).isEqualTo("1 item")
  }

  @Test
  fun testGetQuantityTextInLocale_validId_twoItems_returnsQuantityStringForMultipleItems() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    val str = impl.run {
      resources.getQuantityTextInLocale(R.plurals.test_plural_string_no_args, 2)
    }

    // Note that the 'other' case covers most scenarios in English (per
    // https://issuetracker.google.com/issues/36917255).
    assertThat(str).isEqualTo("2 items")
  }

  @Test
  fun testGetQuantityTextInLocale_nonExistentId_throwsException() {
    val impl = createDisplayLocaleImpl(US_ENGLISH_CONTEXT)
    val resources = context.resources

    assertThrows(Resources.NotFoundException::class) {
      impl.run { resources.getQuantityTextInLocale(-1, 0) }
    }
  }

  private fun createDisplayLocaleImpl(context: OppiaLocaleContext): DisplayLocaleImpl =
    DisplayLocaleImpl(context, machineLocale, androidLocaleFactory, formatterFactory)

  private fun setUpTestApplicationComponent() {
    DaggerDisplayLocaleImplTest_TestApplicationComponent.builder()
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
      TestModule::class, LocaleTestModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(displayLocaleImplTest: DisplayLocaleImplTest)
  }

  private companion object {
    // Date & time: Wed Apr 24 2019 08:22:03 GMT.
    private const val MORNING_UTC_TIMESTAMP_MILLIS = 1556094123000

    private val US_ENGLISH_CONTEXT = OppiaLocaleContext.newBuilder().apply {
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        language = OppiaLanguage.ENGLISH
        minAndroidSdkVersion = 1
        appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
          ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
            ietfLanguageTag = "en"
          }.build()
        }.build()
      }.build()
      regionDefinition = RegionSupportDefinition.newBuilder().apply {
        region = OppiaRegion.UNITED_STATES
        regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
          ietfRegionTag = "US"
        }.build()
      }.build()
    }.build()

    private val EGYPT_ARABIC_CONTEXT = OppiaLocaleContext.newBuilder().apply {
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        language = OppiaLanguage.ARABIC
        minAndroidSdkVersion = 1
        appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
          ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
            ietfLanguageTag = "ar"
          }.build()
        }.build()
      }.build()
      regionDefinition = RegionSupportDefinition.newBuilder().apply {
        region = OppiaRegion.REGION_UNSPECIFIED
        regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
          ietfRegionTag = "EG"
        }.build()
      }.build()
    }.build()

    private val TURKEY_TURKISH_CONTEXT = OppiaLocaleContext.newBuilder().apply {
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        language = OppiaLanguage.LANGUAGE_UNSPECIFIED
        minAndroidSdkVersion = 1
        appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
          ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
            ietfLanguageTag = "tr"
          }.build()
        }.build()
      }.build()
      regionDefinition = RegionSupportDefinition.newBuilder().apply {
        region = OppiaRegion.REGION_UNSPECIFIED
        regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
          ietfRegionTag = "TR"
        }.build()
      }.build()
    }.build()

    private val HEBREW_CONTEXT = OppiaLocaleContext.newBuilder().apply {
      usageMode = OppiaLocaleContext.LanguageUsageMode.APP_STRINGS
      languageDefinition = LanguageSupportDefinition.newBuilder().apply {
        language = OppiaLanguage.ARABIC
        minAndroidSdkVersion = 1
        appStringId = LanguageSupportDefinition.LanguageId.newBuilder().apply {
          ietfBcp47Id = LanguageSupportDefinition.IetfBcp47LanguageId.newBuilder().apply {
            ietfLanguageTag = "he"
          }.build()
        }.build()
      }.build()
      regionDefinition = RegionSupportDefinition.newBuilder().apply {
        region = OppiaRegion.UNITED_STATES
        regionId = RegionSupportDefinition.IetfBcp47RegionId.newBuilder().apply {
          ietfRegionTag = "US"
        }.build()
      }.build()
    }.build()

    private fun String.extractNumbers(): List<String> =
      "\\d+".toRegex().findAll(this).flatMap { it.groupValues }.toList()
  }
}
