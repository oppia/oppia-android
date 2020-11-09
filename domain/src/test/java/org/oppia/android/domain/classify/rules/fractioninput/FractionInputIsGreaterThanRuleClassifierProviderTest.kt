package org.oppia.android.domain.classify.rules.fractioninput

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

/** Tests for [FractionInputIsGreaterThanRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FractionInputIsGreaterThanRuleClassifierProviderTest {

  private val FRACTION_VALUE_TEST_1_OVER_3 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false,
      numerator = 1,
      denominator = 3
    )
  private val FRACTION_VALUE_TEST_1_OVER_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = false,
      numerator = 1,
      denominator = 2
    )
  private val FRACTION_VALUE_TEST_NEGATIVE_1_OVER_3 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = true,
      numerator = 1,
      denominator = 3
    )
  private val FRACTION_VALUE_TEST_NEGATIVE_1_OVER_2 =
    InteractionObjectTestBuilder.createFraction(
      isNegative = true,
      numerator = 1,
      denominator = 2
    )
  private val MIXED_NUMBER_VALUE_TEST_123_1_OVER_2 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = false,
      wholeNumber = 123,
      numerator = 1,
      denominator = 2
    )
  private val MIXED_NUMBER_VALUE_TEST_123_1_OVER_3 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = false,
      wholeNumber = 123,
      numerator = 1,
      denominator = 3
    )
  private val MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = true,
      wholeNumber = 123,
      numerator = 1,
      denominator = 2
    )
  private val MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_3 =
    InteractionObjectTestBuilder.createMixedNumber(
      isNegative = true,
      wholeNumber = 123,
      numerator = 1,
      denominator = 3
    )
  private val STRING_VALUE_TEST_LOWERCASE =
    InteractionObjectTestBuilder.createString(
      value = "test"
    )

  @Inject
  internal lateinit var fractionInputIsGreaterThanRuleClassifier:
    FractionInputIsGreaterThanRuleClassifierProvider

  private val inputGreaterThanRuleClassifier by lazy {
    fractionInputIsGreaterThanRuleClassifier.createRuleClassifier()
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testPositiveFractionalAnswer_positiveFractionalInput_exactSameValue_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_1_OVER_3)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testPositiveFractionalAnswer_positiveFractionalInput_answerGreater_verifyAnswerGreater() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_1_OVER_3)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testPositiveFractionalAnswer_positiveFractionalInput_answerSmaller_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testPositiveFractionalAnswer_negativeFractionalInput_answerGreater_verifyAnswerGreater() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_NEGATIVE_1_OVER_3)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testPositiveFractionalAnswer_positiveMixedInput_answerSmaller_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_123_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testPositiveFractionalAnswer_negativeMixedInput_answerGreater_verifyAnswerGreater() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeFractionalAnswer_negativeFractionalInput_exactSameValue_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_NEGATIVE_1_OVER_3)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_NEGATIVE_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeFractionalAnswer_positiveFractionalInput_answerSmaller_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_NEGATIVE_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeFractionalAnswer_negativeFractionalInput_answerGreater_verifyAnswerGreater() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_NEGATIVE_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_NEGATIVE_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeFractionalAnswer_negativeFractionalInput_answerSmaller_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_NEGATIVE_1_OVER_3)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_NEGATIVE_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeFractionalAnswer_negativeMixedInput_answerGreater_verifyAnswerGreater() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_NEGATIVE_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeFractionalAnswer_positiveMixedInput_answerSmaller_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_123_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = FRACTION_VALUE_TEST_NEGATIVE_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testPositiveMixedAnswer_positiveMixedInput_exactSameValue_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_123_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testPositiveMixedAnswer_positiveMixedInput_answerGreater_verifyAnswerGreater() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_123_1_OVER_3)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testPositiveMixedAnswer_positiveMixedInput_answerSmaller_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_123_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_123_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testPositiveMixedAnswer_negativeMixedInput_answerGreater_verifyAnswerGreater() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testPositiveMixedAnswer_positiveFractionalInput_answerGreater_verifyAnswerGreater() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testPositiveMixedAnswer_negativeFractionalInput_answerGreater_verifyAnswerGreater() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_NEGATIVE_1_OVER_3)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeMixedAnswer_negativeMixedInput_exactSameValue_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeMixedAnswer_negativeMixedInput_answerGreater_verifyAnswerGreater() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testNegativeMixedAnswer_negativeMixedInput_answerSmaller_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_3)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeMixedAnswer_positiveMixedInput_answerSmaller_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to MIXED_NUMBER_VALUE_TEST_123_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeMixedAnswer_negativeFractionalInput_answerSmaller_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_NEGATIVE_1_OVER_3)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testNegativeMixedAnswer_positiveFractionalInput_answerSmaller_verifyAnswerNotGreater() {
    val inputs = mapOf("f" to FRACTION_VALUE_TEST_1_OVER_2)

    val matches = inputGreaterThanRuleClassifier.matches(
      answer = MIXED_NUMBER_VALUE_TEST_NEGATIVE_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testFractionalAnswer_missingInput_throwsException() {
    val inputs = mapOf("y" to FRACTION_VALUE_TEST_1_OVER_2)

    val exception = assertThrows(IllegalStateException::class) {
      inputGreaterThanRuleClassifier
        .matches(
          answer = FRACTION_VALUE_TEST_1_OVER_2,
          inputs = inputs
        )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'f' but had: [y]")
  }

  @Test
  fun testFractionalAnswer_stringInput_throwsException() {
    val inputs = mapOf("x" to STRING_VALUE_TEST_LOWERCASE)

    val exception = assertThrows(IllegalStateException::class) {
      inputGreaterThanRuleClassifier
        .matches(
          answer = FRACTION_VALUE_TEST_1_OVER_2,
          inputs = inputs
        )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'f' but had: [x]")
  }

  private fun setUpTestApplicationComponent() {
    DaggerFractionInputIsGreaterThanRuleClassifierProviderTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
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

    fun inject(test: FractionInputIsGreaterThanRuleClassifierProviderTest)
  }
}
