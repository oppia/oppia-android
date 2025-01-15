package org.oppia.android.testing.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.oppia.android.app.model.Fraction

/** Tests for [FractionSubject]. */
@RunWith(JUnit4::class)
class FractionSubjectTest {

  @Test
  fun testHasNegativeProperty_withNegativeFraction_matchesTrue() {
    val fraction = Fraction.newBuilder()
      .setIsNegative(true)
      .build()

    FractionSubject.assertThat(fraction).hasNegativePropertyThat().isTrue()
  }

  @Test
  fun testHasNegativeProperty_withPositiveFraction_matchesFalse() {
    val fraction = Fraction.newBuilder()
      .setIsNegative(false)
      .build()

    FractionSubject.assertThat(fraction).hasNegativePropertyThat().isFalse()
  }

  @Test
  fun testHasNegativeProperty_defaultValue_matchesFalse() {
    val fraction = Fraction.newBuilder().build()

    FractionSubject.assertThat(fraction).hasNegativePropertyThat().isFalse()
  }

  @Test
  fun testHasWholeNumber_withPositiveValue_matchesValue() {
    val fraction = Fraction.newBuilder()
      .setWholeNumber(5)
      .build()

    FractionSubject.assertThat(fraction).hasWholeNumberThat().isEqualTo(5)
  }

  @Test
  fun testHasWholeNumber_withMaxUint32_matchesValue() {
    val fraction = Fraction.newBuilder()
      .setWholeNumber(Int.MAX_VALUE)
      .build()

    FractionSubject.assertThat(fraction).hasWholeNumberThat().isEqualTo(Int.MAX_VALUE)
  }

  @Test
  fun testHasWholeNumber_defaultValue_matchesZero() {
    val fraction = Fraction.newBuilder().build()

    FractionSubject.assertThat(fraction).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testHasNumerator_withPositiveValue_matchesValue() {
    val fraction = Fraction.newBuilder()
      .setNumerator(3)
      .build()

    FractionSubject.assertThat(fraction).hasNumeratorThat().isEqualTo(3)
  }

  @Test
  fun testHasNumerator_withMaxUint32_matchesValue() {
    val fraction = Fraction.newBuilder()
      .setNumerator(Int.MAX_VALUE)
      .build()

    FractionSubject.assertThat(fraction).hasNumeratorThat().isEqualTo(Int.MAX_VALUE)
  }

  @Test
  fun testHasNumerator_defaultValue_matchesZero() {
    val fraction = Fraction.newBuilder().build()

    FractionSubject.assertThat(fraction).hasNumeratorThat().isEqualTo(0)
  }

  @Test
  fun testHasDenominator_withPositiveValue_matchesValue() {
    val fraction = Fraction.newBuilder()
      .setDenominator(4)
      .build()

    FractionSubject.assertThat(fraction).hasDenominatorThat().isEqualTo(4)
  }

  @Test
  fun testHasDenominator_withMaxUint32_matchesValue() {
    val fraction = Fraction.newBuilder()
      .setDenominator(Int.MAX_VALUE)
      .build()

    FractionSubject.assertThat(fraction).hasDenominatorThat().isEqualTo(Int.MAX_VALUE)
  }

  @Test
  fun testHasDenominator_defaultValue_matchesZero() {
    val fraction = Fraction.newBuilder().build()

    FractionSubject.assertThat(fraction).hasDenominatorThat().isEqualTo(0)
  }

  @Test
  fun testEvaluatesToDouble_withProperFraction_matchesExpectedValue() {
    val fraction = Fraction.newBuilder()
      .setNumerator(3)
      .setDenominator(4)
      .build()

    FractionSubject.assertThat(fraction).evaluatesToDoubleThat().isEqualTo(0.75)
  }

  @Test
  fun testEvaluatesToDouble_withImproperFraction_matchesExpectedValue() {
    val fraction = Fraction.newBuilder()
      .setNumerator(5)
      .setDenominator(2)
      .build()

    FractionSubject.assertThat(fraction).evaluatesToDoubleThat().isEqualTo(2.5)
  }

  @Test
  fun testEvaluatesToDouble_withMixedNumber_matchesExpectedValue() {
    val fraction = Fraction.newBuilder()
      .setWholeNumber(2)
      .setNumerator(3)
      .setDenominator(4)
      .build()

    FractionSubject.assertThat(fraction).evaluatesToDoubleThat().isEqualTo(2.75)
  }

  @Test
  fun testEvaluatesToDouble_withNegativeValue_matchesExpectedValue() {
    val fraction = Fraction.newBuilder()
      .setIsNegative(true)
      .setWholeNumber(2)
      .setNumerator(1)
      .setDenominator(2)
      .build()

    FractionSubject.assertThat(fraction).evaluatesToDoubleThat().isEqualTo(-2.5)
  }

  @Test
  fun testProtoEquality_withIdenticalValues_areEqual() {
    val fraction1 = Fraction.newBuilder()
      .setIsNegative(true)
      .setWholeNumber(3)
      .setNumerator(2)
      .setDenominator(5)
      .build()

    val fraction2 = Fraction.newBuilder()
      .setIsNegative(true)
      .setWholeNumber(3)
      .setNumerator(2)
      .setDenominator(5)
      .build()

    assertThat(fraction1).isEqualTo(fraction2)
  }

  @Test
  fun testProtoSerialization_withComplexFraction_maintainsValues() {
    val originalFraction = Fraction.newBuilder()
      .setIsNegative(true)
      .setWholeNumber(3)
      .setNumerator(2)
      .setDenominator(5)
      .build()

    val bytes = originalFraction.toByteArray()
    val deserializedFraction = Fraction.parseFrom(bytes)

    FractionSubject.assertThat(deserializedFraction).apply {
      hasNegativePropertyThat().isTrue()
      hasWholeNumberThat().isEqualTo(3)
      hasNumeratorThat().isEqualTo(2)
      hasDenominatorThat().isEqualTo(5)
    }
  }

  @Test
  fun testDefaultInstance_hasDefaultValues() {
    val defaultFraction = Fraction.getDefaultInstance()

    FractionSubject.assertThat(defaultFraction).apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(0)
      hasNumeratorThat().isEqualTo(0)
      hasDenominatorThat().isEqualTo(0)
    }
  }
}
