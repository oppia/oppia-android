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
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.robolectric.RobolectricModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@OppiaParameterizedTestRunner.SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class ProfileNameValidatorTest {
  @Inject
  lateinit var profileNameValidator: ProfileNameValidator

  @OppiaParameterizedTestRunner.Parameter
  lateinit var name: String

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
  @OppiaParameterizedTestRunner.RunParameterized(
    OppiaParameterizedTestRunner.Iteration("Ben#Henning", "name=Ben#Henning"),
    OppiaParameterizedTestRunner.Iteration("Rajay@T", "name=Rajay@T"),
    OppiaParameterizedTestRunner.Iteration("جيشنو^&&", "name=جيشنو^&&"),
    OppiaParameterizedTestRunner.Iteration("_Jishnu", "name=_Jishnu"),
  )
  fun testIsNameValid_nameWithDisallowedSymbol_returnsFalse() {
    assertThat(profileNameValidator.isNameValid(name)).isFalse()
  }

  @Test
  @OppiaParameterizedTestRunner.RunParameterized(
    OppiaParameterizedTestRunner.Iteration("Ben-Henning", "name=Ben-Henning"),
    OppiaParameterizedTestRunner.Iteration("Rajat.T", "name=Rajat.T"),
    OppiaParameterizedTestRunner.Iteration("G'Jishnu", "name=G'Jishnu"),
  )
  fun testIsNameValid_nameWithAllowedSymbols_returnsTrue() {
    assertThat(profileNameValidator.isNameValid(name)).isTrue()
  }

  @Test
  @OppiaParameterizedTestRunner.RunParameterized(
    OppiaParameterizedTestRunner.Iteration("Ben-.Henning", "name=Ben-.Henning"),
    OppiaParameterizedTestRunner.Iteration("Rajat..T", "name=Rajat..T"),
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
