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
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.InteractionObject
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.test.fail

/** Tests for [FractionInputIsLessThanRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FractionInputIsLessThanRuleClassifierProviderTest {
  @Inject
  internal lateinit var fractionInputIsLessThanRuleClassifier:
    FractionInputIsLessThanRuleClassifierProvider

  private val inputLessThanRuleClassifier by lazy {
    fractionInputIsLessThanRuleClassifier.createRuleClassifier()
  }

  private val FRACTION_1_OVER_3 = createFraction(isNegative = false, numerator = 1, denominator = 3)
  private val FRACTION_1_OVER_2 = createFraction(isNegative = false, numerator = 1, denominator = 2)
  private val NEGATIVE_FRACTION_1_OVER_3 =
    createFraction(isNegative = true, numerator = 1, denominator = 3)
  private val NEGATIVE_FRACTION_1_OVER_2 =
    createFraction(isNegative = true, numerator = 1, denominator = 2)
  private val MIXED_NUMBER_123_1_OVER_2 =
    createMixedNumber(isNegative = false, wholeNumber = 123, numerator = 1, denominator = 2)
  private val MIXED_NUMBER_123_1_OVER_3 =
    createMixedNumber(isNegative = false, wholeNumber = 123, numerator = 1, denominator = 3)
  private val NEGATIVE_MIXED_NUMBER_123_1_OVER_2 =
    createMixedNumber(isNegative = true, wholeNumber = 123, numerator = 1, denominator = 2)
  private val NEGATIVE_MIXED_NUMBER_123_1_OVER_3 =
    createMixedNumber(isNegative = true, wholeNumber = 123, numerator = 1, denominator = 3)
  private val STRING_VALUE = createString(value = "test")

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAnswer1Over3_input1Over2_verifyAnswerLesser() {
    val inputs = mapOf("f" to FRACTION_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = FRACTION_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer1Over3_input1Over3_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to FRACTION_1_OVER_3)

    val matches = inputLessThanRuleClassifier.matches(
      answer = FRACTION_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer1Over2_input1Over3_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to FRACTION_1_OVER_3)

    val matches = inputLessThanRuleClassifier.matches(
      answer = FRACTION_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer1Over3_inputNegative1Over3_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to NEGATIVE_FRACTION_1_OVER_3)

    val matches = inputLessThanRuleClassifier.matches(
      answer = FRACTION_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer1Over2_input123_1Over2_verifyAnswerLesser() {
    val inputs = mapOf("f" to MIXED_NUMBER_123_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = FRACTION_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer1Over2_inputNegative123_1Over2_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to NEGATIVE_MIXED_NUMBER_123_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = FRACTION_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative1Over3_inputNegative1Over3_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to NEGATIVE_FRACTION_1_OVER_3)

    val matches = inputLessThanRuleClassifier.matches(
      answer = NEGATIVE_FRACTION_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative1Over3_input1Over2_verifyAnswerLesser() {
    val inputs = mapOf("f" to FRACTION_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = NEGATIVE_FRACTION_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative1Over3_inputNegative1Over2_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to NEGATIVE_FRACTION_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = NEGATIVE_FRACTION_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative1Over2_inputNegative1Over3_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to NEGATIVE_FRACTION_1_OVER_3)

    val matches = inputLessThanRuleClassifier.matches(
      answer = NEGATIVE_FRACTION_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative1Over2_inputNegative123_1Over2_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to NEGATIVE_MIXED_NUMBER_123_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = NEGATIVE_FRACTION_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative1Over2_input123_1Over2_verifyAnswerLesser() {
    val inputs = mapOf("f" to MIXED_NUMBER_123_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = NEGATIVE_FRACTION_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer123_1Over2_input123_1Over2_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to MIXED_NUMBER_123_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = MIXED_NUMBER_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative123_1Over2_input123_1Over3_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to MIXED_NUMBER_123_1_OVER_3)

    val matches = inputLessThanRuleClassifier.matches(
      answer = MIXED_NUMBER_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer123_1Over3_input123_1Over2_verifyAnswerLesser() {
    val inputs = mapOf("f" to MIXED_NUMBER_123_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = MIXED_NUMBER_123_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer123_1Over2_inputNegative123_1Over2_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to NEGATIVE_MIXED_NUMBER_123_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = MIXED_NUMBER_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer123_1Over2_input1Over2_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to FRACTION_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = MIXED_NUMBER_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer123_1Over2_inputNegative1Over3_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to NEGATIVE_FRACTION_1_OVER_3)

    val matches = inputLessThanRuleClassifier.matches(
      answer = MIXED_NUMBER_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative123_1Over2_inputNegative123_1Over2_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to NEGATIVE_MIXED_NUMBER_123_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = NEGATIVE_MIXED_NUMBER_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative123_1Over3_inputNegative123_1Over2_verifyAnswerNotLesser() {
    val inputs = mapOf("f" to NEGATIVE_MIXED_NUMBER_123_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = NEGATIVE_MIXED_NUMBER_123_1_OVER_3,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswerNegative123_1Over2_inputNegative123_1Over3_verifyAnswerLesser() {
    val inputs = mapOf("f" to NEGATIVE_MIXED_NUMBER_123_1_OVER_3)

    val matches = inputLessThanRuleClassifier.matches(
      answer = NEGATIVE_MIXED_NUMBER_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative123_1Over2_input123_1Over2_verifyAnswerLesser() {
    val inputs = mapOf("f" to MIXED_NUMBER_123_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = NEGATIVE_MIXED_NUMBER_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative123_1Over2_inputNegative1Over3_verifyAnswerLesser() {
    val inputs = mapOf("f" to NEGATIVE_FRACTION_1_OVER_3)

    val matches = inputLessThanRuleClassifier.matches(
      answer = NEGATIVE_MIXED_NUMBER_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative123_1Over2_input1Over2_verifyAnswerLesser() {
    val inputs = mapOf("f" to FRACTION_1_OVER_2)

    val matches = inputLessThanRuleClassifier.matches(
      answer = NEGATIVE_MIXED_NUMBER_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer1Over2_inputMissing_throwsException() {
    val inputs = mapOf("y" to FRACTION_1_OVER_2)

    val exception = assertThrows(IllegalStateException::class) {
      inputLessThanRuleClassifier
        .matches(
          answer = FRACTION_1_OVER_2,
          inputs = inputs
        )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'f' but had: [y]")
  }

  @Test
  fun testAnswer1Over2_inputString_throwsException() {
    val inputs = mapOf("x" to STRING_VALUE)

    val exception = assertThrows(IllegalStateException::class) {
      inputLessThanRuleClassifier
        .matches(
          answer = FRACTION_1_OVER_2,
          inputs = inputs
        )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'f' but had: [x]")
  }

  private fun createFraction(
    isNegative: Boolean,
    numerator: Int,
    denominator: Int
  ): InteractionObject {
    // Fraction-only numbers imply no whole number.
    return InteractionObject.newBuilder().setFraction(
      Fraction.newBuilder()
        .setIsNegative(isNegative)
        .setNumerator(numerator)
        .setDenominator(denominator)
        .build()
    ).build()
  }

  private fun createMixedNumber(
    isNegative: Boolean,
    wholeNumber: Int,
    numerator: Int,
    denominator: Int
  ): InteractionObject {
    return InteractionObject.newBuilder().setFraction(
      Fraction.newBuilder()
        .setIsNegative(isNegative)
        .setWholeNumber(wholeNumber)
        .setNumerator(numerator)
        .setDenominator(denominator)
        .build()
    ).build()
  }

  private fun createString(value: String): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(value).build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerFractionInputIsLessThanRuleClassifierProviderTest_TestApplicationComponent
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

    fun inject(test: FractionInputIsLessThanRuleClassifierProviderTest)
  }
}
