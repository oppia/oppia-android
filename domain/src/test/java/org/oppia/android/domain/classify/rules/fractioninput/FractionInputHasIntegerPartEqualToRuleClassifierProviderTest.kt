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

/** Tests for [FractionInputHasIntegerPartEqualToRuleClassifierProvider]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(manifest = Config.NONE)
class FractionInputHasIntegerPartEqualToRuleClassifierProviderTest {
  @Inject
  internal lateinit var fractionInputHasIntegerPartEqualToRuleClassifier:
    FractionInputHasIntegerPartEqualToRuleClassifierProvider

  private val inputHasIntegerPartEqualToRuleClassifier by lazy {
    fractionInputHasIntegerPartEqualToRuleClassifier.createRuleClassifier()
  }

  private val FRACTION_1_OVER_2 =
    createFraction(isNegative = false, numerator = 1, denominator = 2)
  private val FRACTION_4_OVER_2 =
    createFraction(isNegative = false, numerator = 4, denominator = 2)
  private val FRACTION_5_OVER_2 =
    createFraction(isNegative = false, numerator = 5, denominator = 2)
  private val FRACTION_2_OVER_1 =
    createFraction(isNegative = false, numerator = 2, denominator = 1)
  private val NEGATIVE_FRACTION_1_OVER_2 =
    createFraction(isNegative = true, numerator = 1, denominator = 3)
  private val NEGATIVE_FRACTION_4_OVER_2 =
    createFraction(isNegative = true, numerator = 1, denominator = 2)
  private val MIXED_NUMBER_123_1_OVER_2 =
    createMixedNumber(isNegative = false, wholeNumber = 123, numerator = 1, denominator = 2)
  private val NEGATIVE_MIXED_NUMBER_123_1_OVER_2 =
    createMixedNumber(isNegative = true, wholeNumber = 123, numerator = 1, denominator = 2)
  private val STRING_VALUE = createString(value = "test")
  private val WHOLE_NUMBER_0 = createNonNegativeInt(value = 0)
  private val WHOLE_NUMBER_1 = createNonNegativeInt(value = 1)
  private val WHOLE_NUMBER_2 = createNonNegativeInt(value = 2)
  private val WHOLE_NUMBER_123 = createNonNegativeInt(value = 123)

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testAnswer1Over2_input0_HasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_0)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer5Over2_input2_HasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_2)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_5_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer4Over2_input2_HasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_2)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_4_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer2Over1_input2_HasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_2)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_2_OVER_1,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer123_1Over2_input123_HasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_123)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = MIXED_NUMBER_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative123_1Over2_input123_HasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_123)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = NEGATIVE_MIXED_NUMBER_123_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswerNegative1Over2_input0_HasIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_0)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = NEGATIVE_FRACTION_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isTrue()
  }

  @Test
  fun testAnswer1Over2_input1_HasNotIntegerPartEqual() {
    val inputs = mapOf("x" to WHOLE_NUMBER_1)

    val matches = inputHasIntegerPartEqualToRuleClassifier.matches(
      answer = FRACTION_1_OVER_2,
      inputs = inputs
    )

    assertThat(matches).isFalse()
  }

  @Test
  fun testAnswer1Over2_inputMissing_throwsException() {
    val inputs = mapOf("y" to WHOLE_NUMBER_0)

    val exception = assertThrows(IllegalStateException::class) {
      inputHasIntegerPartEqualToRuleClassifier
        .matches(
          answer = FRACTION_1_OVER_2,
          inputs = inputs
        )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected classifier inputs to contain parameter with name 'x' but had: [y]")
  }

  @Test
  fun testAnswer1Over2_inputString_throwsException() {
    val inputs = mapOf("x" to STRING_VALUE)

    val exception = assertThrows(IllegalStateException::class) {
      inputHasIntegerPartEqualToRuleClassifier
        .matches(
          answer = FRACTION_1_OVER_2,
          inputs = inputs
        )
    }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected input value to be of type NON_NEGATIVE_INT not NORMALIZED_STRING")
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

  private fun createNonNegativeInt(value: Int): InteractionObject {
    return InteractionObject.newBuilder().setNonNegativeInt(value).build()
  }

  private fun createWholeNumberFraction(isNegative: Boolean, value: Int): InteractionObject {
    // Whole number fractions imply '0/1' fractional parts.
    return InteractionObject.newBuilder().setFraction(
      Fraction.newBuilder()
        .setIsNegative(isNegative)
        .setWholeNumber(value)
        .setNumerator(0)
        .setDenominator(1)
        .build()
    ).build()
  }

  private fun createString(value: String): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(value).build()
  }

  private fun setUpTestApplicationComponent() {
    DaggerFractionInputHasIntegerPartEqualToRuleClassifierProviderTest_TestApplicationComponent
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

    fun inject(test: FractionInputHasIntegerPartEqualToRuleClassifierProviderTest)
  }
}
