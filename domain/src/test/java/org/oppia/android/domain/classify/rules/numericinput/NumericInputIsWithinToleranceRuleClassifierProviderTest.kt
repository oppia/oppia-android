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

/** Tests for [NumericInputIsWithinToleranceRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class NumericInputIsWithinToleranceRuleClassifierProviderTest {

  private val POSITIVE_REAL_VALUE_1_5 = createReal(value = 1.5)
  private val POSITIVE_REAL_VALUE_2_5 = createReal(value = 2.5)
  private val POSITIVE_REAL_VALUE_3_5 = createReal(value = 3.5)
  private val ZERO_REAL_VALUE = createReal(value = 0.0)
  private val NEGATIVE_REAL_VALUE_1_5 = createReal(value = -1.5)
  private val NEGATIVE_REAL_VALUE_2_5 = createReal(value = -2.5)
  private val NEGATIVE_REAL_VALUE_3_5 = createReal(value = -3.5)
  private val STRING_VALUE_1 = createString(value = "test1")
  private val POSITIVE_INT_VALUE_1 = createInt(value = 1)
  private val POSITIVE_INT_VALUE_2 = createInt(value = 2)
  private val POSITIVE_INT_VALUE_3 = createInt(value = 3)
  private val ZERO_INT_VALUE = createInt(value = 0)
  private val NEGATIVE_INT_VALUE_1 = createInt(value = -1)
  private val NEGATIVE_INT_VALUE_2 = createInt(value = -2)
  private val NEGATIVE_INT_VALUE_3 = createInt(value = -3)

  @Inject
  internal lateinit var numericInputIsWithinToleranceRuleClassifierProvider:
    NumericInputIsWithinToleranceRuleClassifierProvider

  private val inputIsWithinToleranceRuleClassifier by lazy {
    numericInputIsWithinToleranceRuleClassifierProvider.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAnswer2pt5_input3pt5_tolerance1pt5_isWithinTolerance() {
    val inputs = mapOf(
      "x" to POSITIVE_REAL_VALUE_3_5,
      "tol" to POSITIVE_REAL_VALUE_1_5
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_2_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer1pt5_input3pt5_tolerance1pt5_isNotWithinTolerance() {
    val inputs = mapOf(
      "x" to POSITIVE_REAL_VALUE_3_5,
      "tol" to POSITIVE_REAL_VALUE_1_5
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer2pt5_input3pt5_tolerance1_isWithinTolerance() {
    val inputs = mapOf(
      "x" to POSITIVE_REAL_VALUE_3_5,
      "tol" to POSITIVE_INT_VALUE_1
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_2_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative2pt5_inputNegative3pt5_tolerance1_isWithinTolerance() {
    val inputs = mapOf(
      "x" to NEGATIVE_REAL_VALUE_3_5,
      "tol" to POSITIVE_INT_VALUE_1
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_2_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative2pt5_inputNegative3pt5_tolerance1pt5_isWithinTolerance() {
    val inputs = mapOf(
      "x" to NEGATIVE_REAL_VALUE_3_5,
      "tol" to POSITIVE_REAL_VALUE_1_5
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_2_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative1pt5_inputNegative3pt5_tolerance1pt5_isNotWithinTolerance() {
    val inputs = mapOf(
      "x" to NEGATIVE_REAL_VALUE_3_5,
      "tol" to POSITIVE_REAL_VALUE_1_5
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer1pt5_input1pt5_tolerance0_isWithinTolerance() {
    val inputs = mapOf(
      "x" to POSITIVE_REAL_VALUE_1_5,
      "tol" to ZERO_REAL_VALUE
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer2pt5_input3pt5_tolerance0_isNotWithinTolerance() {
    val inputs = mapOf(
      "x" to POSITIVE_REAL_VALUE_3_5,
      "tol" to ZERO_REAL_VALUE
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_2_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative1pt5_inputNegative1pt5_tolerance0_isWithinTolerance() {
    val inputs = mapOf(
      "x" to NEGATIVE_REAL_VALUE_1_5,
      "tol" to ZERO_REAL_VALUE
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_1_5, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative2pt5_inputNegative3pt5_tolerance0_isNotWithinTolerance() {
    val inputs = mapOf(
      "x" to NEGATIVE_REAL_VALUE_3_5,
      "tol" to ZERO_REAL_VALUE
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = NEGATIVE_REAL_VALUE_2_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer2pt5_input3pt5_toleranceNegative1pt5_isNotWithinTolerance() {
    val inputs = mapOf(
      "x" to POSITIVE_REAL_VALUE_3_5,
      "tol" to NEGATIVE_REAL_VALUE_1_5
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_2_5, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerInt2_inputInt3_toleranceInt2_isWithinTolerance() {
    val inputs = mapOf(
      "x" to POSITIVE_INT_VALUE_3,
      "tol" to POSITIVE_INT_VALUE_2
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerInt1_inputInt3_toleranceInt1_isNotWithinTolerance() {
    val inputs = mapOf(
      "x" to POSITIVE_INT_VALUE_3,
      "tol" to POSITIVE_INT_VALUE_1
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_1, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerInt2_inputInt3_toleranceInt1_isWithinTolerance() {
    val inputs = mapOf(
      "x" to POSITIVE_INT_VALUE_3,
      "tol" to POSITIVE_INT_VALUE_1
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegativeInt2_inputNegativeInt3_toleranceInt1_isWithinTolerance() {
    val inputs = mapOf(
      "x" to NEGATIVE_INT_VALUE_3,
      "tol" to POSITIVE_INT_VALUE_1
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = NEGATIVE_INT_VALUE_2, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegativeInt1_inputNegativeInt3_toleranceInt1_isNotWithinTolerance() {
    val inputs = mapOf(
      "x" to NEGATIVE_INT_VALUE_3,
      "tol" to POSITIVE_INT_VALUE_1
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = NEGATIVE_INT_VALUE_1, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerInt1_inputInt3_toleranceInt0_isWithinTolerance() {
    val inputs = mapOf(
      "x" to POSITIVE_INT_VALUE_1,
      "tol" to ZERO_INT_VALUE
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_1, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerInt2_inputInt3_toleranceInt0_isNotWithinTolerance() {
    val inputs = mapOf(
      "x" to POSITIVE_INT_VALUE_3,
      "tol" to ZERO_INT_VALUE
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegativeInt3_inputInt3_toleranceInt0_isWithinTolerance() {
    val inputs = mapOf(
      "x" to NEGATIVE_INT_VALUE_3,
      "tol" to ZERO_REAL_VALUE
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = NEGATIVE_INT_VALUE_3, inputs = inputs)

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegativeInt2_inputNegativeInt3_tolerance0_answerNotWithinTolerance() {
    val inputs = mapOf(
      "x" to NEGATIVE_INT_VALUE_3,
      "tol" to ZERO_REAL_VALUE
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = NEGATIVE_INT_VALUE_2, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerInt2_inputInt3_toleranceNegativeInt1_isNotWithinTolerance() {
    val inputs = mapOf(
      "x" to POSITIVE_INT_VALUE_3,
      "tol" to NEGATIVE_INT_VALUE_1
    )
    val matches =
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer1pt5_firstMissingInput_throwsException() {
    val inputs = mapOf(
      "tol" to POSITIVE_REAL_VALUE_3_5
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [tol]")
  }

  @Test
  fun testAnswer1pt5_secondMissingInput_throwsException() {
    val inputs = mapOf(
      "x" to NEGATIVE_REAL_VALUE_3_5
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'tol' but had: [x]")
  }

  @Test
  fun testAnswer1pt5_firstIncorrectInput_throwsException() {
    val inputs = mapOf(
      "c" to NEGATIVE_REAL_VALUE_3_5,
      "tol" to POSITIVE_REAL_VALUE_3_5
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [c, tol]")
  }

  @Test
  fun testAnswer1pt5_secondIncorrectInput_throwsException() {
    val inputs = mapOf(
      "x" to NEGATIVE_REAL_VALUE_3_5,
      "c" to POSITIVE_REAL_VALUE_1_5
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'tol' but had: [x, c]")
  }

  @Test
  fun testAnswer1pt5_firstStringInput_throwsException() {
    val inputs = mapOf(
      "x" to STRING_VALUE_1,
      "tol" to NEGATIVE_REAL_VALUE_3_5
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_REAL_VALUE_1_5, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type REAL not NORMALIZED_STRING")
  }

  @Test
  fun testAnswerInt2_firstMissingInput_throwsException() {
    val inputs = mapOf(
      "tol" to POSITIVE_INT_VALUE_3
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [tol]")
  }

  @Test
  fun testAnswerInt2_secondMissingInput_throwsException() {
    val inputs = mapOf(
      "x" to POSITIVE_INT_VALUE_1
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'tol' but had: [x]")
  }

  @Test
  fun testAnswerInt2_firstIncorrectInput_throwsException() {
    val inputs = mapOf(
      "c" to POSITIVE_INT_VALUE_1,
      "tol" to POSITIVE_INT_VALUE_3
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [c, tol]")
  }

  @Test
  fun testAnswerInt2_secondIncorrectInput_throwsException() {
    val inputs = mapOf(
      "x" to POSITIVE_INT_VALUE_1,
      "c" to POSITIVE_INT_VALUE_3
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_2, inputs = inputs)
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'tol' but had: [x, c]")
  }

  @Test
  fun testAnswerInt1_firstStringInput_throwsException() {
    val inputs = mapOf(
      "x" to STRING_VALUE_1,
      "tol" to POSITIVE_INT_VALUE_3
    )
    val exception = assertThrows(IllegalStateException::class) {
      inputIsWithinToleranceRuleClassifier
        .matches(answer = POSITIVE_INT_VALUE_1, inputs = inputs)
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
    DaggerNumericInputIsWithinToleranceRuleClassifierProviderTest_TestApplicationComponent.builder()
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

    fun inject(test: NumericInputIsWithinToleranceRuleClassifierProviderTest)
  }
}
