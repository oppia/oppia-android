package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.Fraction
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.math.FractionSubject.Companion.assertThat
import org.robolectric.annotation.LooperMode
import java.lang.ArithmeticException

/** Tests for [Fraction] extensions. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class FractionExtensionsTest {
  private companion object {
    private val ZERO_FRACTION = Fraction.newBuilder().apply {
      denominator = 1
    }.build()

    private val NEGATIVE_ZERO_FRACTION = Fraction.newBuilder().apply {
      isNegative = true
      denominator = 1
    }.build()

    private val ONE_HALF_FRACTION = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 2
    }.build()

    private val NEGATIVE_ONE_HALF_FRACTION = Fraction.newBuilder().apply {
      isNegative = true
      numerator = 1
      denominator = 2
    }.build()

    private val ONE_AND_ONE_HALF_FRACTION = Fraction.newBuilder().apply {
      wholeNumber = 1
      numerator = 1
      denominator = 2
    }.build()

    private val NEGATIVE_ONE_AND_ONE_HALF_FRACTION = Fraction.newBuilder().apply {
      isNegative = true
      wholeNumber = 1
      numerator = 1
      denominator = 2
    }.build()

    private val THREE_HALVES_FRACTION = Fraction.newBuilder().apply {
      numerator = 3
      denominator = 2
    }.build()

    private val NEGATIVE_THREE_HALVES_FRACTION = Fraction.newBuilder().apply {
      isNegative = true
      numerator = 3
      denominator = 2
    }.build()

    private val THREE_ONES_FRACTION = Fraction.newBuilder().apply {
      numerator = 3
      denominator = 1
    }.build()

    private val NEGATIVE_THREE_ONES_FRACTION = Fraction.newBuilder().apply {
      isNegative = true
      numerator = 3
      denominator = 1
    }.build()

    private val TWO_FRACTION = Fraction.newBuilder().apply {
      wholeNumber = 2
      denominator = 1
    }.build()

    private val NEGATIVE_TWO_FRACTION = Fraction.newBuilder().apply {
      isNegative = true
      wholeNumber = 2
      denominator = 1
    }.build()
  }

  @Test
  fun testHasFractionalPart_zeroFraction_returnsFalse() {
    val result = ZERO_FRACTION.hasFractionalPart()

    assertThat(result).isFalse()
  }

  @Test
  fun testHasFractionalPart_oneHalf_returnsTrue() {
    val result = ONE_HALF_FRACTION.hasFractionalPart()

    assertThat(result).isTrue()
  }

  @Test
  fun testHasFractionalPart_negativeOneHalf_returnsTrue() {
    val result = NEGATIVE_ONE_HALF_FRACTION.hasFractionalPart()

    assertThat(result).isTrue()
  }

  @Test
  fun testHasFractionalPart_mixedFraction_returnsTrue() {
    val result = ONE_AND_ONE_HALF_FRACTION.hasFractionalPart()

    assertThat(result).isTrue()
  }

  @Test
  fun testHasFractionalPart_improperFraction_returnsTrue() {
    val result = THREE_HALVES_FRACTION.hasFractionalPart()

    assertThat(result).isTrue()
  }

  @Test
  fun testHasFractionalPart_threeOverOne_returnsTrue() {
    val result = THREE_ONES_FRACTION.hasFractionalPart()

    assertThat(result).isTrue()
  }

  @Test
  fun testHasFractionalPart_onlyWholeNumber_returnsFalse() {
    val result = TWO_FRACTION.hasFractionalPart()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsOnlyWholeNumber_zeroFraction_returnsTrue() {
    val result = ZERO_FRACTION.isOnlyWholeNumber()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsOnlyWholeNumber_oneHalf_returnsFalse() {
    val result = ONE_HALF_FRACTION.isOnlyWholeNumber()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsOnlyWholeNumber_negativeOneHalf_returnsFalse() {
    val result = NEGATIVE_ONE_HALF_FRACTION.isOnlyWholeNumber()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsOnlyWholeNumber_mixedFraction_returnsFalse() {
    val result = ONE_AND_ONE_HALF_FRACTION.isOnlyWholeNumber()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsOnlyWholeNumber_improperFraction_returnsFalse() {
    val result = THREE_HALVES_FRACTION.isOnlyWholeNumber()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsOnlyWholeNumber_threeOverOne_returnsFalse() {
    val result = THREE_ONES_FRACTION.isOnlyWholeNumber()

    // 3/1 is technically not a whole number since it's still in fractional form.
    assertThat(result).isFalse()
  }

  @Test
  fun testIsOnlyWholeNumber_onlyWholeNumber_returnsTrue() {
    val result = TWO_FRACTION.isOnlyWholeNumber()

    assertThat(result).isTrue()
  }

  @Test
  fun testToDouble_zeroFraction_returnsZero() {
    val result = ZERO_FRACTION.toDouble()

    assertThat(result).isWithin(1e-5).of(0.0)
  }

  @Test
  fun testToDouble_oneHalf_returnsPointFive() {
    val result = ONE_HALF_FRACTION.toDouble()

    assertThat(result).isWithin(1e-5).of(0.5)
  }

  @Test
  fun testToDouble_negativeOneHalf_returnsNegativePointFive() {
    val result = NEGATIVE_ONE_HALF_FRACTION.toDouble()

    assertThat(result).isWithin(1e-5).of(-0.5)
  }

  @Test
  fun testToDouble_one_and_one_half_returnsOnePointFive() {
    val result = ONE_AND_ONE_HALF_FRACTION.toDouble()

    assertThat(result).isWithin(1e-5).of(1.5)
  }

  @Test
  fun testToDouble_threeHalves_returnsOnePointFive() {
    val result = THREE_HALVES_FRACTION.toDouble()

    assertThat(result).isWithin(1e-5).of(1.5)
  }

  @Test
  fun testToDouble_two_returnsTwo() {
    val result = TWO_FRACTION.toDouble()

    assertThat(result).isWithin(1e-5).of(2.0)
  }

  @Test
  fun testToAnswerString_zero_returnsZeroString() {
    val result = ZERO_FRACTION.toAnswerString()

    assertThat(result).isEqualTo("0")
  }

  @Test
  fun testToAnswerString_negativeZero_returnsZeroString() {
    val result = NEGATIVE_ZERO_FRACTION.toAnswerString()

    assertThat(result).isEqualTo("0")
  }

  @Test
  fun testToAnswerString_two_returnsTwoString() {
    val result = TWO_FRACTION.toAnswerString()

    assertThat(result).isEqualTo("2")
  }

  @Test
  fun testToAnswerString_negativeTwo_returnsMinusTwoString() {
    val result = NEGATIVE_TWO_FRACTION.toAnswerString()

    assertThat(result).isEqualTo("-2")
  }

  @Test
  fun testToAnswerString_threeOverOne_returnsThreeString() {
    val result = THREE_ONES_FRACTION.toAnswerString()

    assertThat(result).isEqualTo("3")
  }

  @Test
  fun testToAnswerString_negativeThreeOverOne_returnsMinusThreeString() {
    val result = NEGATIVE_THREE_ONES_FRACTION.toAnswerString()

    assertThat(result).isEqualTo("-3")
  }

  @Test
  fun testToAnswerString_threeOverTwo_returnsThreeHalvesString() {
    val result = THREE_HALVES_FRACTION.toAnswerString()

    assertThat(result).isEqualTo("3/2")
  }

  @Test
  fun testToAnswerString_negativeThreeOverTwo_returnsMinusThreeHalvesString() {
    val result = NEGATIVE_THREE_HALVES_FRACTION.toAnswerString()

    assertThat(result).isEqualTo("-3/2")
  }

  @Test
  fun testToAnswerString_oneAndOneHalf_returnsMixedFractionString() {
    val result = ONE_AND_ONE_HALF_FRACTION.toAnswerString()

    assertThat(result).isEqualTo("1 1/2")
  }

  @Test
  fun testToAnswerString_negativeOneAndOneHalf_returnsMinusMixedFractionString() {
    val result = NEGATIVE_ONE_AND_ONE_HALF_FRACTION.toAnswerString()

    assertThat(result).isEqualTo("-1 1/2")
  }

  @Test
  fun testToSimplestForm_zero_returnsZeroFraction() {
    val result = ZERO_FRACTION.toSimplestForm()

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testToSimplestForm_two_returnsTwoFraction() {
    val result = TWO_FRACTION.toSimplestForm()

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(2)
  }

  @Test
  fun testToSimplestForm_oneHalf_returnsOneHalfFraction() {
    val result = ONE_HALF_FRACTION.toSimplestForm()

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(2)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testToSimplestForm_oneAndOneHalf_returnsOneAndOneHalfFraction() {
    val result = ONE_AND_ONE_HALF_FRACTION.toSimplestForm()

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(2)
    assertThat(result).hasWholeNumberThat().isEqualTo(1)
  }

  @Test
  fun testToSimplestForm_sixFourths_returnsThreeHalvesFraction() {
    val sixHalvesFraction = Fraction.newBuilder().apply {
      numerator = 6
      denominator = 4
    }.build()

    val result = sixHalvesFraction.toSimplestForm()

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(3)
    assertThat(result).hasDenominatorThat().isEqualTo(2)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testToSimplestForm_largeNegativeImproperFraction_reducesToSimplestImproperFraction() {
    val largeImproperFraction = Fraction.newBuilder().apply {
      isNegative = true
      numerator = 1650
      denominator = 209
    }.build()

    val result = largeImproperFraction.toSimplestForm()

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(150)
    assertThat(result).hasDenominatorThat().isEqualTo(19)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testToSimplestForm_zeroDenominator_throwsException() {
    val zeroDenominatorFraction = Fraction.getDefaultInstance()

    // Converting to simplest form results in a divide by zero in this case.
    assertThrows(ArithmeticException::class) { zeroDenominatorFraction.toSimplestForm() }
  }

  @Test
  fun testToImproperForm_zero_returnsZeroFraction() {
    val result = ZERO_FRACTION.toImproperForm()

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testToImproperForm_two_returnsTwoOnesFraction() {
    val result = TWO_FRACTION.toImproperForm()

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(2)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testToImproperForm_oneHalf_returnsOneHalfFraction() {
    val result = ONE_HALF_FRACTION.toImproperForm()

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(2)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testToImproperForm_oneAndOneHalf_returnsThreeHalvesFraction() {
    val result = ONE_AND_ONE_HALF_FRACTION.toImproperForm()

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(3)
    assertThat(result).hasDenominatorThat().isEqualTo(2)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testToImproperForm_threeHalves_returnsThreeHalvesFraction() {
    val result = THREE_HALVES_FRACTION.toImproperForm()

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(3)
    assertThat(result).hasDenominatorThat().isEqualTo(2)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testToImproperForm_negativeOneAndTwoThirds_returnsNegativeFiveThirdsFraction() {
    val negativeOneAndTwoThirds = Fraction.newBuilder().apply {
      isNegative = true
      numerator = 2
      denominator = 3
      wholeNumber = 1
    }.build()

    val result = negativeOneAndTwoThirds.toImproperForm()

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(5)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testToImproperForm_largeSimpleFormFraction_returnsLargeImproperFraction() {
    val negativeOneAndTwoThirds = Fraction.newBuilder().apply {
      isNegative = true
      numerator = 17
      denominator = 19
      wholeNumber = 7
    }.build()

    val result = negativeOneAndTwoThirds.toImproperForm()

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(150)
    assertThat(result).hasDenominatorThat().isEqualTo(19)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testUnaryMinus_zero_returnsNegativeZeroFraction() {
    val result = -ZERO_FRACTION

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testUnaryMinus_two_returnsNegativeTwoFraction() {
    val result = -TWO_FRACTION

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(2)
  }

  @Test
  fun testUnaryMinus_negativeTwo_returnsTwoFraction() {
    val result = -NEGATIVE_TWO_FRACTION

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(2)
  }

  @Test
  fun testUnaryMinus_oneHalf_returnsNegativeOneHalfFraction() {
    val result = -ONE_HALF_FRACTION

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(2)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testUnaryMinus_negativeOneHalf_returnsOneHalfFraction() {
    val result = -NEGATIVE_ONE_HALF_FRACTION

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(2)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testUnaryMinus_oneAndOneHalf_returnsNegativeOneAndOneHalfFraction() {
    val result = -ONE_AND_ONE_HALF_FRACTION

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(2)
    assertThat(result).hasWholeNumberThat().isEqualTo(1)
  }

  @Test
  fun testUnaryMinus_negativeOneAndOneHalf_returnsOneAndOneHalfFraction() {
    val result = -NEGATIVE_ONE_AND_ONE_HALF_FRACTION

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(2)
    assertThat(result).hasWholeNumberThat().isEqualTo(1)
  }
}
