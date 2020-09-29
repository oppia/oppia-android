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
import org.oppia.android.domain.classify.rules.numericinput.NumericInputIsGreaterThanRuleClassifierProvider
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [NumericInputIsGreaterThanRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NumericInputIsGreaterThanRuleClassifierProviderTest {

  private val POSITIVE_REAL_VALUE_1_5 = createReal(value = 1.5)
  private val POSITIVE_REAL_VALUE_3_5 = createReal(value = 3.5)
  private val NEGATIVE_REAL_VALUE_1_5 = createReal(value = -1.5)
  private val NEGATIVE_REAL_VALUE_3_5 = createReal(value = -3.5)
  private val STRING_VALUE = createString(value = "test")
  private val POSITIVE_INT_VALUE_3 = createInt(value = 3)
  private val POSITIVE_INT_VALUE_1 = createInt(value = 1)
  private val NEGATIVE_INT_VALUE_1 = createInt(value = -1)
  private val NEGATIVE_INT_VALUE_3 = createInt(value = -3)

  @Inject
  internal lateinit var numericInputIsGreaterThanRuleClassifierProvider:
    NumericInputIsGreaterThanRuleClassifierProvider

  private val inputIsGreaterThanRuleClassifier by lazy {
    numericInputIsGreaterThanRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testPositiveRealAnswer_positiveRealInput_sameExactValues_answerNotGreater() {
    val inputs = mapOf("x" to POSITIVE_REAL_VALUE_1_5)

    val matches =
      inputIsGreaterThanRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeRealAnswer_negativeRealInput_sameExactValues_answerNotGreater() {
    val inputs = mapOf("x" to NEGATIVE_REAL_VALUE_1_5)

    val matches =
      inputIsGreaterThanRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testPositiveRealAnswer_positiveRealInput_answerValueGreater_answerGreater() {
    val inputs = mapOf("x" to POSITIVE_REAL_VALUE_1_5)

    val matches =
      inputIsGreaterThanRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_3_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testPositiveRealAnswer_positiveRealInput_answerValueSmaller_answerNotGreater() {
    val inputs = mapOf("x" to POSITIVE_REAL_VALUE_3_5)

    val matches =
      inputIsGreaterThanRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeRealAnswer_negativeRealInput_answerValueGreater_answerGreater() {
    val inputs = mapOf("x" to NEGATIVE_REAL_VALUE_3_5)

    val matches =
      inputIsGreaterThanRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeRealAnswer_negativeRealInput_answerValueSmaller_answerNotGreater() {
    val inputs = mapOf("x" to NEGATIVE_REAL_VALUE_1_5)

    val matches =
      inputIsGreaterThanRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_3_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeRealAnswer_positiveRealInput_answerValueSmaller_answerNotGreater() {
    val inputs = mapOf("x" to POSITIVE_REAL_VALUE_1_5)

    val matches =
      inputIsGreaterThanRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_3_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testPositiveRealAnswer_negativeRealInput_answerValueGreater_answerGreater() {
    val inputs = mapOf("x" to NEGATIVE_REAL_VALUE_1_5)

    val matches =
      inputIsGreaterThanRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testPositiveIntAnswer_negativeIntInput_answerValueGreater_answerGreater() {
    val inputs = mapOf("x" to NEGATIVE_INT_VALUE_3)

    val matches =
      inputIsGreaterThanRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_1, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeIntAnswer_positiveIntInput_answerValueSmaller_answerNotGreater() {
    val inputs = mapOf("x" to POSITIVE_INT_VALUE_3)

    val matches =
      inputIsGreaterThanRuleClassifier
        .matches(answer = NEGATIVE_INT_VALUE_1, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testRealAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to POSITIVE_REAL_VALUE_1_5)

    val exception = assertThrows(IllegalStateException::class) {
      inputIsGreaterThanRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x'")
  }

  @Test
  fun testRealAnswer_stringInput_throwsException() {
    val inputs = mapOf("x" to STRING_VALUE)

    val exception = assertThrows(IllegalStateException::class) {
      inputIsGreaterThanRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
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
    DaggerNumericInputIsGreaterThanRuleClassifierProviderTest_TestApplicationComponent
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

    fun inject(test: NumericInputIsGreaterThanRuleClassifierProviderTest)
  }
}
