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
import org.oppia.android.domain.classify.InteractionObjectTestBuilder
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class TextInputFuzzyEqualsRuleClassifierProviderTest {

  private val STRING_ANSWER_TEST_UPPERCASE =
    InteractionObjectTestBuilder.createString(value = "TEST")
  private val STRING_INPUT_TEST_UPPERCASE =
    InteractionObjectTestBuilder.createString(value = "TEST")
  private val STRING_INPUT_TEST_LOWERCASE =
    InteractionObjectTestBuilder.createString(value = "test")
  private val STRING_ANSWER_TEST_LOWERCASE =
    InteractionObjectTestBuilder.createString(value = "test")
  private val STRING_ANSWER_DIFF_LOWERCASE =
    InteractionObjectTestBuilder.createString(value = "diff")
  private val STRING_INPUT_DIFF_LOWERCASE =
    InteractionObjectTestBuilder.createString(value = "diff")
  private val STRING_INPUT_DIFF_UPPERCASE =
    InteractionObjectTestBuilder.createString(value = "DIFF")
  private val STRING_FUZZY_INPUT =
    InteractionObjectTestBuilder.createString(value = "This Is a TesT")
  private val STRING_ANSWER =
    InteractionObjectTestBuilder.createString(value = "this is a test")
  private val STRING_WITH_WHITESPACE =
    InteractionObjectTestBuilder.createString(value = "  test   ")
  private val NON_NEGATIVE_TEST_VALUE_1 =
    InteractionObjectTestBuilder.createNonNegativeInt(value = 1)

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
  fun testUpperCaseAnswer_testUpperCaseInput_sameString_verifyBothValuesMatch() {
    val inputs = mapOf("x" to STRING_INPUT_TEST_UPPERCASE)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(answer = STRING_ANSWER_TEST_UPPERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testUpperCaseAnswer_testLowerCaseInput_sameString_verifyBothValuesMatch() {
    val inputs = mapOf("x" to STRING_INPUT_TEST_LOWERCASE)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(answer = STRING_ANSWER_TEST_UPPERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testUpperCaseAnswer_testLowercaseInput_differentString_verifyBothValuesDoNotMatch() {
    val inputs = mapOf("x" to STRING_INPUT_DIFF_LOWERCASE)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(answer = STRING_ANSWER_TEST_UPPERCASE, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testUpperCaseAnswer_testUpperCaseInput_differentString_verifyBothValuesDoNotMatch() {
    val inputs = mapOf("x" to STRING_INPUT_TEST_UPPERCASE)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(answer = STRING_INPUT_DIFF_UPPERCASE, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowerCaseAnswer_testUpperCaseInput_sameString_verifyBothValuesMatch() {
    val inputs = mapOf("x" to STRING_INPUT_TEST_UPPERCASE)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(answer = STRING_ANSWER_TEST_LOWERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowerCaseAnswer_testLowerCaseInput_sameString_verifyBothValuesMatch() {
    val inputs = mapOf("x" to STRING_INPUT_TEST_LOWERCASE)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(answer = STRING_ANSWER_TEST_LOWERCASE, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testLowerCaseAnswer_testLowerCaseInput_differentString_verifyBothValuesDoNotMatch() {
    val inputs = mapOf("x" to STRING_INPUT_TEST_LOWERCASE)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(answer = STRING_ANSWER_DIFF_LOWERCASE, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testLowerCaseAnswer_testUpperCaseInput_differentString_verifyBothValuesDoNotMatch() {
    val inputs = mapOf("x" to STRING_INPUT_TEST_UPPERCASE)

    val matches =
      inputFuzzyEqualsRuleClassifier.matches(answer = STRING_ANSWER_DIFF_LOWERCASE, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringFuzzyInput_answerFuzzyEqualsInput_verifyValuesMatch() {
    val inputs = mapOf("x" to STRING_FUZZY_INPUT)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = STRING_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringWithWhitespacesInput_answerEqualsInput_verifyValuesMatch() {
    val inputs = mapOf("x" to STRING_INPUT_TEST_LOWERCASE)

    val matches = inputFuzzyEqualsRuleClassifier.matches(
      answer = STRING_WITH_WHITESPACE,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_nonNegativeIntInput_verifyThrowsException() {
    val inputs = mapOf("x" to NON_NEGATIVE_TEST_VALUE_1)

    val exception = assertThrows(IllegalStateException::class) {
      inputFuzzyEqualsRuleClassifier.matches(answer = STRING_ANSWER_TEST_UPPERCASE, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type NORMALIZED_STRING")
  }

  private fun setUpTestApplicationComponent() {
    DaggerTextInputFuzzyEqualsRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun <T : Throwable> assertThrows(type: KClass<T>, operation: () -> Unit): T {
    try {
      operation()
      fail("Expected to encounter exception of $type")
    } catch (t: Throwable) {
      if (type.isInstance(t)) {
        return type.cast(t)
      }
      // Unexpected exception; throw it.
      throw t
    }
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
