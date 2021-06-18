package org.oppia.android.domain.classify.rules.textinput

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createString
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createTranslatableSetOfNormalizedString
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/** Tests for [TextInputEqualsRuleClassifierProvider]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class TextInputEqualsRuleClassifierProviderTest {

  private val STRING_VALUE_TEST_UPPERCASE = createString(value = "TEST")
  private val STRING_VALUE_TEST_LOWERCASE = createString(value = "test")
  private val STRING_VALUE_TEST_DIFFERENT_VALUE = createString(value = "string")
  private val STRING_VALUE_TEST_SINGLE_SPACES = createString(value = "test a lot")
  private val STRING_VALUE_THIS = createString(value = "this")
  private val STRING_VALUE_A_TEST = createString(value = "a test")
  private val STRING_VALUE_NOT_A_TEST = createString(value = "not a test")
  private val INT_VALUE_TEST_NON_NEGATIVE = createUnsingnedInteger(value = 1)

  private val STRING_VALUE_TEST_UPPERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("TEST")
  private val STRING_VALUE_TEST_LOWERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("test")
  private val STRING_VALUE_TEST_EXTRA_SPACES_INPUT_SET =
    createTranslatableSetOfNormalizedString("test  a  lot  ")
  private val STRING_VALUE_TEST_NO_SPACES_INPUT_SET =
    createTranslatableSetOfNormalizedString("testalot")
  private val MULTIPLE_STRING_VALUE_INPUT_SET =
    createTranslatableSetOfNormalizedString("this", "is", "a test")

  @Inject
  internal lateinit var textInputEqualsRuleClassifierProvider:
    TextInputEqualsRuleClassifierProvider

  private val inputEqualsRuleClassifier by lazy {
    textInputEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testStringAnswer_stringInput_sameExactString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches =
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_UPPERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testCapitalStringAnswer_lowercaseStringInput_sameCaseInsensitiveString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches =
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_UPPERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowercaseStringAnswer_capitalStringInput_sameCaseInsensitiveString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches =
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_LOWERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_sameStringDifferentSpaces_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_EXTRA_SPACES_INPUT_SET)

    val matches =
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_SINGLE_SPACES, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_differentStrings_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches = inputEqualsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_DIFFERENT_VALUE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringNoSpacesInput_differentStrings_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NO_SPACES_INPUT_SET)

    val matches =
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_SINGLE_SPACES, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_multiStringInput_answerCorrespondsToOne_answerMatches() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputEqualsRuleClassifier.matches(
      answer = STRING_VALUE_THIS,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multiStringInput_answerCorrespondsToDifferentOne_answerMatches() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputEqualsRuleClassifier.matches(
      answer = STRING_VALUE_A_TEST,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multiStringInput_answerDoesNotCorrespondsToAny_answerDoesNotMatch() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputEqualsRuleClassifier.matches(
      answer = STRING_VALUE_NOT_A_TEST,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_LOWERCASE, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testStringAnswer_nonNegativeIntInput_throwsException() {
    val inputs = mapOf("x" to INT_VALUE_TEST_NON_NEGATIVE)

    val exception = assertThrows(IllegalStateException::class) {
      inputEqualsRuleClassifier.matches(answer = STRING_VALUE_TEST_UPPERCASE, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type TRANSLATABLE_SET_OF_NORMALIZED_STRING")
  }

  private fun setUpTestApplicationComponent() {
    DaggerTextInputEqualsRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: TextInputEqualsRuleClassifierProviderTest)
  }
}
