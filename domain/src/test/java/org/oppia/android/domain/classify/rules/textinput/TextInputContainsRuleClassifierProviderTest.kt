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

/** Tests for [TextInputContainsRuleClassifierProvider]. */
@Suppress("PrivatePropertyName") // Truly immutable constants can be named in CONSTANT_CASE.
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class TextInputContainsRuleClassifierProviderTest {

  private val STRING_VALUE_TEST_ANSWER_NULL = createString(value = "")
  private val STRING_VALUE_IS_ANSWER = createString(value = "is")
  private val STRING_VALUE_NOT_ANSWER = createString(value = "not")
  private val STRING_VALUE_TEST_ANSWER = createString(value = "this is a test")
  private val NON_NEGATIVE_VALUE_TEST_1 = createNonNegativeInt(value = 1)

  private val STRING_VALUE_TEST_CONTAINS_ANSWER_INPUT_SET =
    createTranslatableSetOfNormalizedString("this is a test i will break")
  private val STRING_VALUE_TEST_AN_ANSWER_INPUT_SET =
    createTranslatableSetOfNormalizedString("an answer")
  private val STRING_VALUE_TEST_A_TEST_INPUT_SET =
    createTranslatableSetOfNormalizedString("a test")
  private val STRING_VALUE_TEST_IS_A_INPUT_SET = createTranslatableSetOfNormalizedString("is a")
  private val STRING_VALUE_TEST_THIS_IS_INPUT_SET =
    createTranslatableSetOfNormalizedString("this is")
  private val STRING_VALUE_TEST_NULL_INPUT_SET = createTranslatableSetOfNormalizedString("")
  private val STRING_VALUE_TEST_ANSWER_INPUT_SET =
    createTranslatableSetOfNormalizedString("this is a test")
  private val STRING_VALUE_TEST_EXTRA_SPACE_INPUT_SET =
    createTranslatableSetOfNormalizedString(" this   is  a  test ")
  private val STRING_VALUE_TEST_NO_SPACE_INPUT_SET =
    createTranslatableSetOfNormalizedString("thisisatest")
  private val MULTIPLE_STRING_VALUE_INPUT_SET =
    createTranslatableSetOfNormalizedString("this", "is", "a test")

  @Inject
  internal lateinit var textInputContainsRuleClassifierProvider:
    TextInputContainsRuleClassifierProvider

  private val inputContainsRuleClassifier by lazy {
    textInputContainsRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testStringAnswer_stringInput_sameString_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_ANSWER_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEmptyStringAnswer_emptyStringInput_answerContainsInput_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NULL_INPUT_SET)

    val matches =
      inputContainsRuleClassifier.matches(answer = STRING_VALUE_TEST_ANSWER_NULL, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNonEmptyStringAnswer_emptyStringInput_answerContainsInput_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NULL_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNonEmptyStringAnswer_wordStringAnswer_inputWithMultipleMatches_answerMatches() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_IS_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNonEmptyStringAnswer_wordStringAnswer_inputUnmatchingStrings_answerDoesNotMatch() {
    val inputs = mapOf("x" to MULTIPLE_STRING_VALUE_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_NOT_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringInput_answerContainsInputAtBeginning_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_THIS_IS_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_answerContainsInputInMiddle_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_IS_A_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_answerContainsInputAtEnd_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_A_TEST_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringExtraSpacesInput_answerContainsInput_answerMatches() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_EXTRA_SPACE_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_inputNotInAnswer_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEmptyStringAnswer_nonEmptyStringInput_answerDoesNotContainInput_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_ANSWER_INPUT_SET)

    val matches =
      inputContainsRuleClassifier.matches(answer = STRING_VALUE_TEST_ANSWER_NULL, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringInput_answerPartiallyContainsInput_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_CONTAINS_ANSWER_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringNoSpacesInput_answerPartiallyContainsInput_answerDoesNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NO_SPACE_INPUT_SET)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to STRING_VALUE_TEST_ANSWER_INPUT_SET)

    val exception = assertThrows(IllegalStateException::class) {
      inputContainsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testStringAnswer_nonNegativeIntInput_throwsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_VALUE_TEST_1)

    val exception = assertThrows(IllegalStateException::class) {
      inputContainsRuleClassifier.matches(
        answer = STRING_VALUE_TEST_ANSWER,
        inputs = inputs
      )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type TRANSLATABLE_SET_OF_NORMALIZED_STRING")
  }

  private fun setUpTestApplicationComponent() {
    DaggerTextInputContainsRuleClassifierProviderTest_TestApplicationComponent.builder()
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

    fun inject(test: TextInputContainsRuleClassifierProviderTest)
  }
}
