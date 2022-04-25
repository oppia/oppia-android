package org.oppia.android.util.profile

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.runner.RunWith
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@OppiaParameterizedTestRunner.SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ProfileNameValidatorTest {
  @Inject
  lateinit var profileNameValidator: ProfileNameValidator

  @Before
  fun setup() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testIsNameValid_nameWithSpaces_returnsFalse() {
    val nameWithSpaces = "Ben Henning"
    assertThat(profileNameValidator.isNameValid(nameWithSpaces)).isFalse()
  }

  @Test
  fun testIsNameValid_nameWithNumber_returnsFalse() {
    val nameWithNumber = "Jishnu7"
    assertThat(profileNameValidator.isNameValid(nameWithNumber)).isFalse()
  }

  @Test
  fun testIsNameValid_nameWithDisallowedSymbol_returnsFalse() {
    val namesWithSymbols =
      listOf<String>("Ben#Henning", "Rajay@T", "जिष्णु**", "جيشنو^&&", "_Jishnu")
    namesWithSymbols.forEach {
      assertThat(profileNameValidator.isNameValid(it)).isFalse()
    }
  }

  @Test
  fun testIsNameValid_nameWithAllowedSymbols_returnsTrue() {
    val namesWithAllowedSymbol = listOf<String>("Ben-Henning", "Rajat.T", "G'Jishnu")
    namesWithAllowedSymbol.forEach {
      assertThat(profileNameValidator.isNameValid(it)).isTrue()
    }
  }

  @Test
  fun testIsNameValid_nameWithRepeatedAllowedSymbols_returnsFalse() {
    val namesWithRepeatedAllowedSymbol = listOf<String>("Ben-.Henning", "Rajat..T")
    namesWithRepeatedAllowedSymbol.forEach {
      assertThat(profileNameValidator.isNameValid(it)).isFalse()
    }
  }

  @Test
  fun testIsNameValid_nameWithEnglishLetters_returnsTrue() {
    val nameWithEnglishLetters = "Jerry"
    assertThat(profileNameValidator.isNameValid(nameWithEnglishLetters)).isTrue()
  }

  @Test
  fun testIsNameValid_nameWithHindiLetters_returnsTrue() {
    val nameWithHindiLetters = "जिष्णु"
    assertThat(profileNameValidator.isNameValid(nameWithHindiLetters)).isTrue()
  }

  @Test
  fun testIsNameValid_nameWithArabicLetters_returnsTrue() {
    val nameWithArabicLetters = "جيشنو"
    assertThat(profileNameValidator.isNameValid(nameWithArabicLetters)).isTrue()
  }

  private fun setUpTestApplicationComponent() {
    DaggerProfileNameValidatorTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
  }

  @Singleton
  @Component(modules = [RobolectricModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: ProfileNameValidatorTest)
  }
}
