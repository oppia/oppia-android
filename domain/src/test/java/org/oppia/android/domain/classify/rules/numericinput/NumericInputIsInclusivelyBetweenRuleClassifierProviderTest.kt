package org.oppia.android.domain.classify.rules.numericinput

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.InteractionObject
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [NumericInputIsInclusivelyBetweenRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NumericInputIsInclusivelyBetweenRuleClassifierProviderTest {

  private val POSITIVE_REAL_VALUE_1_5 = createReal(value = 1.5)
  private val POSITIVE_REAL_VALUE_2_5 = createReal(value = 2.5)
  private val POSITIVE_REAL_VALUE_3_5 = createReal(value = 3.5)
  private val NEGATIVE_REAL_VALUE_1_5 = createReal(value = -1.5)
  private val NEGATIVE_REAL_VALUE_2_5 = createReal(value = -2.5)
  private val NEGATIVE_REAL_VALUE_3_5 = createReal(value = -3.5)
  private val STRING_VALUE_1 = createString(value = "test1")
  private val STRING_VALUE_2 = createString(value = "test2")
  private val POSITIVE_INT_VALUE_1 = createInt(value = 1)
  private val POSITIVE_INT_VALUE_2 = createInt(value = 2)
  private val POSITIVE_INT_VALUE_3 = createInt(value = 3)
  private val NEGATIVE_INT_VALUE_1 = createInt(value = -1)
  private val NEGATIVE_INT_VALUE_2 = createInt(value = -2)
  private val NEGATIVE_INT_VALUE_3 = createInt(value = -3)

  @Inject
  internal lateinit var numericInputIsInclusivelyBetweenRuleClassifierProvider:
    NumericInputIsInclusivelyBetweenRuleClassifierProvider

  private val inputIsInclusivelyBetweenRuleClassifier by lazy {
    numericInputIsInclusivelyBetweenRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testPositiveRealAnswer_positiveRealInputs_sameExactValues_answerInclusivelyBetween() {
    val inputs = mapOf(
      "a" to POSITIVE_REAL_VALUE_1_5,
      "b" to POSITIVE_REAL_VALUE_1_5
    )
    val matches =
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeRealAnswer_negativeRealInputs_sameExactValues_answerInclusivelyBetween() {
    val inputs = mapOf(
      "a" to NEGATIVE_REAL_VALUE_3_5,
      "b" to NEGATIVE_REAL_VALUE_3_5
    )
    val matches =
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_3_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testPositiveRealAnswer_negativeRealInput_positiveRealInput_answerInclusivelyBetween() {
    val inputs = mapOf(
      "a" to NEGATIVE_REAL_VALUE_1_5,
      "b" to POSITIVE_REAL_VALUE_3_5
    )
    val matches =
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_2_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testPositiveRealAnswer_positiveRealInput_negativeRealInput_answerInclusivelyBetween() {
    val inputs = mapOf(
      "a" to POSITIVE_REAL_VALUE_3_5,
      "b" to NEGATIVE_REAL_VALUE_3_5
    )
    val matches =
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_2_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeRealAnswer_negativeRealInput_positiveRealInput_answerInclusivelyBetween() {
    val inputs = mapOf(
      "a" to NEGATIVE_REAL_VALUE_3_5,
      "b" to POSITIVE_REAL_VALUE_1_5
    )
    val matches =
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_2_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeRealAnswer_positiveRealInput_negativeRealInput_answerInclusivelyBetween() {
    val inputs = mapOf(
      "a" to POSITIVE_REAL_VALUE_3_5,
      "b" to NEGATIVE_REAL_VALUE_3_5
    )
    val matches =
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_2_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testPositiveIntAnswer_negativeInput_positiveIntInput_answerInclusivelyBetween() {
    val inputs = mapOf(
      "a" to NEGATIVE_INT_VALUE_1,
      "b" to POSITIVE_INT_VALUE_3
    )
    val matches =
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testPositiveIntAnswer_positiveIntInput_negativeIntInput_answerNotInclusivelyBetween() {
    val inputs = mapOf(
      "a" to POSITIVE_INT_VALUE_3,
      "b" to NEGATIVE_INT_VALUE_1
    )
    val matches =
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeIntAnswer_negativeIntInput_positiveIntInput_answerInclusivelyBetween() {
    val inputs = mapOf(
      "a" to NEGATIVE_INT_VALUE_3,
      "b" to POSITIVE_INT_VALUE_1
    )
    val matches =
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = NEGATIVE_INT_VALUE_2, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeIntAnswer_positiveIntInput_negativeIntInput_answerNotInclusivelyBetween() {
    val inputs = mapOf(
      "a" to POSITIVE_REAL_VALUE_3_5,
      "b" to NEGATIVE_REAL_VALUE_3_5
    )
    val matches =
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_2_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testRealAnswer_firstMissingInput_throwsException() {
    val inputs = mapOf(
      "c" to NEGATIVE_REAL_VALUE_3_5,
      "b" to POSITIVE_REAL_VALUE_3_5
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'a' but had: [c, b]")
  }

  @Test
  fun testRealAnswer_secondMissingInput_throwsException() {
    val inputs = mapOf(
      "a" to NEGATIVE_REAL_VALUE_3_5,
      "c" to POSITIVE_REAL_VALUE_1_5
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'b' but had: [a, c]")
  }

  @Test
  fun testRealAnswer_firstStringInput_throwsException() {
    val inputs = mapOf(
      "a" to STRING_VALUE_1,
      "b" to NEGATIVE_REAL_VALUE_3_5
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type REAL not NORMALIZED_STRING")
  }

  @Test
  fun testRealAnswer_secondStringInput_throwsException() {
    val inputs = mapOf(
      "a" to POSITIVE_REAL_VALUE_1_5,
      "b" to STRING_VALUE_2
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type REAL not NORMALIZED_STRING")
  }

  @Test
  fun testIntAnswer_missingFirstInput_throwsException() {
    val inputs = mapOf(
      "c" to POSITIVE_INT_VALUE_1,
      "b" to POSITIVE_INT_VALUE_3
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'a' but had: [c, b]")
  }

  @Test
  fun testIntAnswer_missingSecondInput_throwsException() {
    val inputs = mapOf(
      "a" to POSITIVE_INT_VALUE_1,
      "c" to POSITIVE_INT_VALUE_3
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'b' but had: [a, c]")
  }

  @Test
  fun testIntAnswer_firstStringInput_throwsException() {
    val inputs = mapOf(
      "a" to STRING_VALUE_1,
      "b" to POSITIVE_INT_VALUE_3
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_1, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type REAL not NORMALIZED_STRING")
  }

  @Test
  fun testIntAnswer_secondStringInput_throwsException() {
    val inputs = mapOf(
      "a" to NEGATIVE_INT_VALUE_3,
      "b" to STRING_VALUE_2
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsInclusivelyBetweenRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type REAL not NORMALIZED_STRING")
  }

  private fun createReal(value: Double): InteractionObject {
    return InteractionObject.newBuilder().setReal(value).build()
  }

  private fun createString(value: String): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(value).build()
  }

  private fun createInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setReal(value.toDouble()).build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerNumericInputIsInclusivelyBetweenRuleClassifierProviderTest_TestApplicationComponent
      .builder()
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

    fun inject(test: NumericInputIsInclusivelyBetweenRuleClassifierProviderTest)
  }
}
