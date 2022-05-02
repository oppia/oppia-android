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
import org.mockito.Mock
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [MachineLocaleImpl]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class MachineLocaleImplTest {
  @Inject
  lateinit var machineLocale: OppiaLocale.MachineLocale

  @Mock
  lateinit var mockDisplayLocale: OppiaLocale.DisplayLocale
  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
  }

  @Test
  fun testFormatForMachines_formatStringWithArgs_returnsCorrectlyFormattedString() {
    val formatted = machineLocale.run { "Test with %s and %d".formatForMachines("string", 11) }

    assertThat(formatted).isEqualTo("Test with string and 11")
  }

  @Test
  fun testToMachineLowerCase_lowerCaseString_returnsSameString() {
    val formatted = machineLocale.run { "lowercase string".toMachineLowerCase() }

    assertThat(formatted).isEqualTo("lowercase string")
  }

  @Test
  fun testToMachineLowerCase_mixedCaseString_returnsLowerCaseString() {
    val formatted = machineLocale.run { "MiXeD CaSe StriNG".toMachineLowerCase() }

    assertThat(formatted).isEqualTo("mixed case string")
  }

  @Test
  fun testToMachineUpperCase_upperCaseString_returnsSameString() {
    val formatted = machineLocale.run { "UPPERCASE STRING".toMachineUpperCase() }

    assertThat(formatted).isEqualTo("UPPERCASE STRING")
  }

  @Test
  fun testToMachineUpperCase_mixedCaseString_returnsUpperCaseString() {
    val formatted = machineLocale.run { "MiXeD CaSe StriNG".toMachineUpperCase() }

    assertThat(formatted).isEqualTo("MIXED CASE STRING")
  }

  @Test
  fun testToMachineLowerCase_englishLocale_localeSensitiveChar_returnsConvertedCase() {
    Locale.setDefault(Locale.ENGLISH)

    val formatted = machineLocale.run { "TITLE".toMachineLowerCase() }

    assertThat(formatted).isEqualTo("title")
  }

  @Test
  fun testToMachineLowerCase_turkishLocale_localeSensitiveChar_returnsSameConversion() {
    Locale.setDefault(Locale("tr", "tr"))

    val formatted = machineLocale.run { "TITLE".toMachineLowerCase() }

    // See https://stackoverflow.com/a/11063161 for context on why this is correct behavior. The
    // machine locale guarantees a consistent lowercase experience regardless of the current locale
    // default (as comparable with the display locale which will perform case changing based on the
    // locale).
    assertThat(formatted).isEqualTo("title")
  }

  @Test
  fun testCapitalizeForMachines_capitalizedString_returnsSameString() {
    val formatted = machineLocale.run { "Capital".capitalizeForMachines() }

    assertThat(formatted).isEqualTo("Capital")
  }

  @Test
  fun testCapitalizeForMachines_uncapitalizedString_returnsCapitalized() {
    val formatted = machineLocale.run { "uncapital".capitalizeForMachines() }

    assertThat(formatted).isEqualTo("Uncapital")
  }

  @Test
  fun testDecapitalizeForMachines_decapitalizedString_returnsSameString() {
    val formatted = machineLocale.run { "uncapital".decapitalizeForMachines() }

    assertThat(formatted).isEqualTo("uncapital")
  }

  @Test
  fun testDecapitalizeForMachines_capitalizedString_returnsDecapitalized() {
    val formatted = machineLocale.run { "Capital".decapitalizeForMachines() }

    assertThat(formatted).isEqualTo("capital")
  }

  @Test
  fun testEndsWithIgnoreCase_stringWithoutSuffix_returnsFalse() {
    val matches = machineLocale.run { "string".endsWithIgnoreCase("aw") }

    assertThat(matches).isFalse()
  }

  @Test
  fun testEndsWithIgnoreCase_stringWithSuffix_matchingCases_returnsTrue() {
    val matches = machineLocale.run { "straw".endsWithIgnoreCase("aw") }

    assertThat(matches).isTrue()
  }

  @Test
  fun testEndsWithIgnoreCase_stringWithSuffix_differingCases_returnsTrue() {
    val matches = machineLocale.run { "STrAw".endsWithIgnoreCase("aW") }

    assertThat(matches).isTrue()
  }

  @Test
  fun testEqualsIgnoreCase_differentString_returnsFalse() {
    val matches = machineLocale.run { "string".equalsIgnoreCase("other") }

    assertThat(matches).isFalse()
  }

  @Test
  fun testEqualsIgnoreCase_differentString_reversed_returnsFalse() {
    val matches = machineLocale.run { "other".equalsIgnoreCase("string") }

    assertThat(matches).isFalse()
  }

  @Test
  fun testEqualsIgnoreCase_sameString_matchingCases_returnsTrue() {
    val matches = machineLocale.run { "string".equalsIgnoreCase("string") }

    assertThat(matches).isTrue()
  }

  @Test
  fun testEqualsIgnoreCase_sameString_differingCases_returnsTrue() {
    val matches = machineLocale.run { "sTRInG".equalsIgnoreCase("StrINg") }

    assertThat(matches).isTrue()
  }

  @Test
  fun testEqualsIgnoreCase_sameString_differingCases_reversed_returnsTrue() {
    val matches = machineLocale.run { "StrINg".equalsIgnoreCase("sTRInG") }

    assertThat(matches).isTrue()
  }

  @Test
  fun testGetCurrentTimeOfDay_inMorningHour_returnsMorning() {
    fakeOppiaClock.setCurrentTimeMs(MORNING_UTC_TIMESTAMP_MILLIS)

    val timeOfDay = machineLocale.getCurrentTimeOfDay()

    assertThat(timeOfDay).isEqualTo(OppiaLocale.MachineLocale.TimeOfDay.MORNING)
  }

  @Test
  fun testGetCurrentTimeOfDay_inAfternoonHour_returnsAfternoon() {
    fakeOppiaClock.setCurrentTimeMs(AFTERNOON_UTC_TIMESTAMP_MILLIS)

    val timeOfDay = machineLocale.getCurrentTimeOfDay()

    assertThat(timeOfDay).isEqualTo(OppiaLocale.MachineLocale.TimeOfDay.AFTERNOON)
  }

  @Test
  fun testGetCurrentTimeOfDay_inEveningHour_returnsEvening() {
    fakeOppiaClock.setCurrentTimeMs(EVENING_UTC_TIMESTAMP_MILLIS)

    val timeOfDay = machineLocale.getCurrentTimeOfDay()

    assertThat(timeOfDay).isEqualTo(OppiaLocale.MachineLocale.TimeOfDay.EVENING)
  }

  @Test
  fun testParseOppiaDate_emptyString_returnsNull() {
    fakeOppiaClock.setCurrentTimeMs(MORNING_UTC_TIMESTAMP_MILLIS)

    val date = machineLocale.parseOppiaDate(dateString = "")

    assertThat(date).isNull()
  }

  @Test
  fun testParseOppiaDate_invalidDateFormat_returnsNull() {
    val date = machineLocale.parseOppiaDate(dateString = "24 April 2019")

    // The date is in an invalid/unexpected format.
    assertThat(date).isNull()
  }

  @Test
  fun testOppiaNumberFormatter_validNumberFormat_returnsString() {
    val number = mockDisplayLocale.run { 0.toHumanReadableString(1) }

    assertThat(number).isEqualTo("1")
  }

  @Test
  fun testParseOppiaDate_validDateFormat_returnsDateObject() {
    val date = machineLocale.parseOppiaDate(dateString = "2019-04-24")

    assertThat(date).isNotNull()
  }

  @Test
  fun testOppiaDateIsBeforeToday_validDateObject_forYesterday_returnsTrue() {
    fakeOppiaClock.setCurrentTimeMs(MORNING_UTC_TIMESTAMP_MILLIS)

    val date = machineLocale.parseOppiaDate(dateString = "2019-04-23")

    assertThat(date?.isBeforeToday()).isTrue()
  }

  @Test
  fun testOppiaDateIsBeforeToday_validDateObject_forTomorrow_returnsFalse() {
    fakeOppiaClock.setCurrentTimeMs(MORNING_UTC_TIMESTAMP_MILLIS)

    val date = machineLocale.parseOppiaDate(dateString = "2019-04-25")

    assertThat(date?.isBeforeToday()).isFalse()
  }

  @Test
  fun testComputeCurrentTimeString_forFixedTime_returnsHourMinuteSecondParts() {
    fakeOppiaClock.setCurrentTimeMs(MORNING_UTC_TIMESTAMP_MILLIS)

    val timeString = machineLocale.computeCurrentTimeString()

    // Verify that the individual hour, minute, and second components are present (though don't
    // actually verify formatting since that's implementation specific).
    val numbers = "\\d+".toRegex().findAll(timeString).flatMap { it.groupValues }.toList()
    assertThat(numbers).containsExactly("8", "22", "03")
  }

  private fun setUpTestApplicationComponent() {
    DaggerMachineLocaleImplTest_TestApplicationComponent.builder()
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

    fun inject(machineLocaleImplTest: MachineLocaleImplTest)
  }

  private companion object {
    // Date & time: Wed Apr 24 2019 08:22:03 GMT.
    private const val MORNING_UTC_TIMESTAMP_MILLIS = 1556094123000

    // Date & time: Tue Apr 23 2019 14:22:00 GMT.
    private const val AFTERNOON_UTC_TIMESTAMP_MILLIS = 1556029320000

    // Date & time: Tue Apr 23 2019 23:22:00 GMT.
    private const val EVENING_UTC_TIMESTAMP_MILLIS = 1556061720000
  }
}
