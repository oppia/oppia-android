package org.oppia.android.domain.classify.rules.textinput

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createNonNegativeInt
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createString
import org.oppia.android.domain.classify.InteractionObjectTestBuilder.createTranslatableSetOfNormalizedString
import org.oppia.android.testing.assertThrows
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TextInputStartsWithRuleClassifierProvider]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class TextInputStartsWithRuleClassifierProviderTest {

  private val STRING_VALUE_TEST_STRING_LOWERCASE = createString(value = "test string")
  private val STRING_VALUE_TEST_LOWERCASE = createString(value = "test")
  private val STRING_VALUE_TEST_STRING_UPPERCASE = createString(value = "TEST STRING")
  private val STRING_VALUE_TEST_UPPERCASE = createString(value = "TEST")
  private val STRING_VALUE_TEST_NULL = createString(value = "")
  private val STRING_VALUE_ANTIDERIVATIVE = createString(value = "antiderivative")
  private val STRING_VALUE_PREFIX = createString(value = "prefix")
  private val STRING_VALUE_SOMETHING_ELSE = createString(value = "something else")
  private val NON_NEGATIVE_TEST_VALUE_1 = createNonNegativeInt(value = 1)

  private val STRING_VALUE_TEST_STRING_LOWERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("test string")
  private val STRING_VALUE_TEST_STRING_LOWERCASE_EXTRA_SPACES_INPUT_SET =
    createTranslatableSetOfNormalizedString("test  string")
  private val STRING_VALUE_TEST_LOWERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("test")
  private val STRING_VALUE_STRING_LOWERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("string")
  private val STRING_VALUE_TEST_STRING_UPPERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("TEST STRING")
  private val STRING_VALUE_TEST_STRING_UPPERCASE_NO_SPACES_INPUT_SET =
    createTranslatableSetOfNormalizedString("TESTSTRING")
  private val STRING_VALUE_TEST_UPPERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("TEST")
  private val STRING_VALUE_TEST_NULL_INPUT_SET =
    createTranslatableSetOfNormalizedString("")
  private val MULTIPLE_STRING_VALUE_INPUT_SET =
    createTranslatableSetOfNormalizedString("anti", "pre")

  @Inject
  internal lateinit var textInputStartsWithRuleClassifierProvider:
    TextInputStartsWithRuleClassifierProvider

  private val inputStartsWithRuleClassifier by lazy {
    textInputStartsWithRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testLowercaseStringAns_lowercaseStringInput_differentStrings_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_STRING_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowercaseStringAns_lowercaseStringInput_differentString_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_STRING_LOWERCASE_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_STRING_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowercaseStringAns_lowercaseStringInput_inputStartsWithAns_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_STRING_LOWERCASE_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowercaseStringAns_uppercaseStringInput_sameCaseInsensitive_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_STRING_UPPERCASE_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_STRING_LOWERCASE,
      inputs = inputs
    )

    // The check should be case-insensitive.
    assertThat(matches).isTrue()
  }

  @Test
  fun testLowercaseStringAns_lowercaseStringInput_extraSpaces_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_STRING_LOWERCASE_EXTRA_SPACES_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_STRING_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowercaseStringAns_uppercaseStringInput_differentStrings_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_STRING_LOWERCASE,
      inputs = inputs
    )

    // The check should be case-insensitive.
    assertThat(matches).isTrue()
  }

  @Test
  fun testLowercaseStringAns_emptyStringInput_differentStrings_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NULL_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_STRING_LOWERCASE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testUppercaseStringAns_uppercaseStringInput_inputStartWithAns_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_STRING_UPPERCASE_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_UPPERCASE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUppercaseStringAns_uppercaseStringInput_noSpaces_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_STRING_UPPERCASE_NO_SPACES_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_UPPERCASE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUppercaseStringAns_emptyStringInput_differentStrings_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NULL_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_STRING_UPPERCASE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEmptyStringAns_lowercaseStringInput_differentStrings_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_STRING_LOWERCASE_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_NULL,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEmptyStringAns_emptyStringInput_exactSameStrings_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NULL_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_TEST_NULL,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multipleInputs_answerStartsWithOne_answerMatches() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_ANTIDERIVATIVE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multipleInputs_answerStartsWithAnotherOne_answerMatches() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_PREFIX,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multipleInputs_answerStartsWithWithNone_answerDoesNotMatch() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputStartsWithRuleClassifier.matches(
      answer = STRING_VALUE_SOMETHING_ELSE,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAns_missingInput_throwsException() {
    val inputs = mapOf("y" to STRING_VALUE_TEST_STRING_LOWERCASE_INPUT_SET)

    val exception = assertThrows(IllegalStateException::class) {
      inputStartsWithRuleClassifier.matches(
        answer = STRING_VALUE_TEST_STRING_LOWERCASE,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testStringAns_nonNegativeIntInput_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_TEST_VALUE_1)

    val exception = assertThrows(IllegalStateException::class) {
      inputStartsWithRuleClassifier.matches(
        answer = STRING_VALUE_TEST_STRING_LOWERCASE,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type TRANSLATABLE_SET_OF_NORMALIZED_STRING")
  }

  private fun setUpTestApplicationComponent() {
    DaggerTextInputStartsWithRuleClassifierProviderTest_TestApplicationComponent.builder()
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

    fun inject(test: TextInputStartsWithRuleClassifierProviderTest)
  }
}
