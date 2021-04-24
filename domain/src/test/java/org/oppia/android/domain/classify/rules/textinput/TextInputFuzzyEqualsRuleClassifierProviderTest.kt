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

/** Tests for [TextInputFuzzyEqualsRuleClassifierProvider]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class TextInputFuzzyEqualsRuleClassifierProviderTest {

  private val STRING_VALUE_TEST_ANSWER_UPPERCASE = createString(value = "TEST")
  private val STRING_VALUE_TEST_ANSWER_LOWERCASE = createString(value = "test")
  private val STRING_VALUE_TEST_ANSWER_DIFF_LOWERCASE = createString(value = "diff")
  private val STRING_VALUE_TEST_DIFF_UPPERCASE = createString(value = "DIFF")
  private val STRING_VALUE_TEST_ANSWER = createString(value = "this is a test")
  private val STRING_VALUE_TEST_WITH_WHITESPACE = createString(value = "  test   ")
  private val STRING_VALUE_THIS = createString(value = "this")
  private val STRING_VALUE_TEST = createString(value = "test")
  private val STRING_VALUE_TESTING = createString(value = "testing")
  private val NON_NEGATIVE_TEST_VALUE_1 = createNonNegativeInt(value = 1)

  private val STRING_VALUE_TEST_UPPERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("TEST")
  private val STRING_VALUE_TEST_LOWERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("test")
  private val STRING_VALUE_TEST_DIFF_LOWERCASE_INPUT_SET =
    createTranslatableSetOfNormalizedString("diff")
  private val STRING_VALUE_TEST_FUZZY_INPUT_INPUT_SET =
    createTranslatableSetOfNormalizedString("This Is a TesT")
  private val MULTIPLE_STRING_VALUE_INPUT_SET =
    createTranslatableSetOfNormalizedString("Thiss", "Iis", "TesTt")

  @Inject
  internal lateinit var textInputFuzzyEqualsRuleClassifierProvider:
    TextInputFuzzyEqualsRuleClassifierProvider

  private val inputFuzzyEqualsRuleClassifier by lazy {
    textInputFuzzyEqualsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testUpperCaseAnswer_testUpperCaseInput_sameString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_UPPERCASE,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testUpperCaseAnswer_testLowerCaseInput_sameString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_UPPERCASE,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testUpperCaseAnswer_testLowercaseInput_differentString_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_DIFF_LOWERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_UPPERCASE,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testUpperCaseAnswer_testUpperCaseInput_differentString_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_DIFF_UPPERCASE,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowerCaseAnswer_testUpperCaseInput_sameString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_LOWERCASE,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowerCaseAnswer_testLowerCaseInput_sameString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_LOWERCASE,
        inputs = inputs
      )

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowerCaseAnswer_testLowerCaseInput_differentString_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_DIFF_LOWERCASE,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowerCaseAnswer_testUpperCaseInput_differentString_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_UPPERCASE_INPUT_SET)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_DIFF_LOWERCASE,
        inputs = inputs
      )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringFuzzyInput_answerFuzzyEqualsInput_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_FUZZY_INPUT_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringWithWhitespacesInput_answerEqualsInput_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_WITH_WHITESPACE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multiStringInput_answerCorrespondsFuzzilyToOne_answerMatches() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = STRING_VALUE_THIS,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multiStringInput_answerCorrespondsFuzzilyToAnotherOne_answerMatches() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = STRING_VALUE_TEST,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_multiStringInput_answerCorrespondsToNone_answerDoesNotMatch() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = STRING_VALUE_TESTING,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_nonNegativeIntInput_verifyThrowsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_TEST_VALUE_1)

    val exception = assertThrows(IllegalStateException::class) {
      inputFuzzyEqualsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER_UPPERCASE,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type TRANSLATABLE_SET_OF_NORMALIZED_STRING")
  }

  private fun setUpTestApplicationComponent() {
    DaggerTextInputFuzzyEqualsRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Singleton
  @Component(modules = [])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: TextInputFuzzyEqualsRuleClassifierProviderTest)
  }
}
