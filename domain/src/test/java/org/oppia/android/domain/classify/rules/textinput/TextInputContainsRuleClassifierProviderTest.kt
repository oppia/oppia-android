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

/** Tests for [TextInputContainsRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class TextInputContainsRuleClassifierProviderTest {

  private val STRING_VALUE_TEST_CONTAINS_ANSWER =
    InteractionObjectTestBuilder.createString(value = "this is a test i will break")
  private val STRING_VALUE_TEST_AN_ANSWER =
    InteractionObjectTestBuilder.createString(value = "an answer")
  private val STRING_VALUE_TEST_A_TEST =
    InteractionObjectTestBuilder.createString(value = "a test")
  private val STRING_VALUE_TEST_IS_A =
    InteractionObjectTestBuilder.createString(value = "is a")
  private val STRING_VALUE_TEST_THIS_IS =
    InteractionObjectTestBuilder.createString(value = "this is")
  private val STRING_VALUE_TEST_NULL =
    InteractionObjectTestBuilder.createString(value = "")
  private val STRING_VALUE_TEST_ANSWER_NULL =
    InteractionObjectTestBuilder.createString(value = "")
  private val STRING_VALUE_TEST_ANSWER =
    InteractionObjectTestBuilder.createString(value = "this is a test")
  private val STRING_VALUE_TEST_EXTRA_SPACE =
    InteractionObjectTestBuilder.createString(value = " this   is  a  test ")
  private val STRING_VALUE_TEST_NO_SPACE =
    InteractionObjectTestBuilder.createString(value = "thisisatest")
  private val NON_NEGATIVE_VALUE_TEST_1 =
    InteractionObjectTestBuilder.createNonNegativeInt(value = 1)

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
  fun testStringAnswer_stringInput_sameString_bothValuesMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_ANSWER)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testEmptyStringAnswer_emptyStringInput_answerContainsInput_bothValuesMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NULL)

    val matches =
      inputContainsRuleClassifier.matches(answer = STRING_VALUE_TEST_ANSWER_NULL, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNonEmptyStringAnswer_emptyStringInput_answerContainsInput_bothValuesMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NULL)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_answerContainsInputAtBeginning_bothValuesMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_THIS_IS)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_answerContainsInputInMiddle_bothValuesMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_IS_A)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_answerContainsInputAtEnd_bothValuesMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_A_TEST)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringExtraSpacesInput_answerContainsInput_bothValuesMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_EXTRA_SPACE)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testStringAnswer_stringInput_inputNotInAnswer_valuesDoNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_AN_ANSWER)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testEmptyStringAnswer_nonEmptyStringInput_answerDoesNotContainInput_valuesDoNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_ANSWER)

    val matches =
      inputContainsRuleClassifier.matches(answer = STRING_VALUE_TEST_ANSWER_NULL, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringInput_answerPartiallyContainsInput_valuesDoNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_CONTAINS_ANSWER)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_stringNoSpacesInput_answerPartiallyContainsInput_valuesDoNotMatch() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_NO_SPACE)

    val matches = inputContainsRuleClassifier.matches(
      answer = STRING_VALUE_TEST_ANSWER,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testStringAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to STRING_VALUE_TEST_ANSWER)

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
      .contains("Expected input value to be of type NORMALIZED_STRING")
  }

  private fun setUpTestApplicationComponent() {
    DaggerTextInputContainsRuleClassifierProviderTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  // TODO(#89): Move to a common test library.
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
