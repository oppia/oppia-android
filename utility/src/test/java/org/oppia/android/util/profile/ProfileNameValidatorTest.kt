package org.oppia.android.util.profile

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.robolectric.RobolectricModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ProfileNameValidatorTest {
  @Inject
  lateinit var profileNameValidator: ProfileNameValidator

  @Parameter
  lateinit var name: String

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testIsNameValid_nameWithSpaces_returnsTrue() {
    val nameWithSpaces = "Ben Henning"
    assertThat(profileNameValidator.isNameValid(nameWithSpaces)).isTrue()
  }

  @Test
  fun testIsNameValid_nameWithNumber_returnsFalse() {
    val nameWithNumber = "Jishnu7"
    assertThat(profileNameValidator.isNameValid(nameWithNumber)).isFalse()
  }

  @Test
  @RunParameterized(
    Iteration("Ben#Henning", "name=Ben#Henning"),
    Iteration("Rajay@T", "name=Rajay@T"),
    Iteration("جيشنو^&&", "name=جيشنو^&&"),
    Iteration("_Jishnu", "name=_Jishnu")
  )
  fun testIsNameValid_nameWithDisallowedSymbol_returnsFalse() {
    assertThat(profileNameValidator.isNameValid(name)).isFalse()
  }

  @Test
  @RunParameterized(
    Iteration("Ben-Henning", "name=Ben-Henning"),
    Iteration("Rajat.T", "name=Rajat.T"),
    Iteration("G'Jishnu", "name=G'Jishnu")
  )
  fun testIsNameValid_nameWithAllowedSymbols_returnsTrue() {
    assertThat(profileNameValidator.isNameValid(name)).isTrue()
  }

  @Test
  @RunParameterized(
    Iteration("Ben-.Henning", "name=Ben-.Henning"),
    Iteration("Rajat..T", "name=Rajat..T"),
    Iteration("Name   WithTooManySpaces", "name=Name   WithTooManySpaces")
  )
  fun testIsNameValid_nameWithRepeatedAllowedSymbols_returnsFalse() {
    assertThat(profileNameValidator.isNameValid(name)).isFalse()
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
