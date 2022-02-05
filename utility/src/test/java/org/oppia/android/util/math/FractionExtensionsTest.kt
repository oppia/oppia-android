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

    private val ONE_FRACTION = Fraction.newBuilder().apply {
      wholeNumber = 1
      denominator = 1
    }.build()

    private val NEGATIVE_ONE_FRACTION = Fraction.newBuilder().apply {
      isNegative = true
      wholeNumber = 1
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

    private val ONE_THIRD_FRACTION = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 3
    }.build()

    private val NEGATIVE_ONE_THIRD_FRACTION = Fraction.newBuilder().apply {
      isNegative = true
      numerator = 1
      denominator = 3
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
  fun testToWholeNumber_zeroFraction_returnsZero() {
    val result = ZERO_FRACTION.toWholeNumber()

    assertThat(result).isEqualTo(0)
  }

  @Test
  fun testToWholeNumber_negativeZeroFraction_returnsZero() {
    val result = NEGATIVE_ZERO_FRACTION.toWholeNumber()

    assertThat(result).isEqualTo(0)
  }

  @Test
  fun testToWholeNumber_two_returnsTwo() {
    val result = TWO_FRACTION.toWholeNumber()

    assertThat(result).isEqualTo(2)
  }

  @Test
  fun testToWholeNumber_negativeTwo_returnsNegativeTwo() {
    val result = NEGATIVE_TWO_FRACTION.toWholeNumber()

    assertThat(result).isEqualTo(-2)
  }

  @Test
  fun testToWholeNumber_oneHalf_returnsZero() {
    val result = ONE_HALF_FRACTION.toWholeNumber()

    assertThat(result).isEqualTo(0)
  }

  @Test
  fun testToWholeNumber_oneAndOneHalf_returnsOne() {
    val result = ONE_AND_ONE_HALF_FRACTION.toWholeNumber()

    assertThat(result).isEqualTo(1)
  }

  @Test
  fun testToWholeNumber_threeOnes_returnsZero() {
    val result = THREE_ONES_FRACTION.toWholeNumber()

    // Even though the fraction is technically equivalent to '3', it being in improper form results
    // in there not technically being a whole number component.
    assertThat(result).isEqualTo(0)
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
  fun testToProperForm_zeroFraction_returnsZero() {
    val result = ZERO_FRACTION.toProperForm()

    assertThat(result).isEqualTo(ZERO_FRACTION)
  }

  @Test
  fun testToProperForm_two_returnsTwo() {
    val result = TWO_FRACTION.toProperForm()

    assertThat(result).isEqualTo(TWO_FRACTION)
  }

  @Test
  fun testToProperForm_threeOnes_returnsThree() {
    val result = THREE_ONES_FRACTION.toProperForm()

    // Correctly extract the '3' numerator to being a whole number.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(3)
  }

  @Test
  fun testToProperForm_oneHalf_returnsOneHalf() {
    val result = ONE_HALF_FRACTION.toProperForm()

    assertThat(result).isEqualTo(ONE_HALF_FRACTION)
  }

  @Test
  fun testToProperForm_oneAndOneHalf_returnsOneAndOneHalf() {
    val result = ONE_AND_ONE_HALF_FRACTION.toProperForm()

    // 1 1/2 is already in proper form.
    assertThat(result).isEqualTo(ONE_AND_ONE_HALF_FRACTION)
  }

  @Test
  fun testToProperForm_threeHalves_returnsOneAndOneHalf() {
    val result = THREE_HALVES_FRACTION.toProperForm()

    // 3/2 -> 1 1/2.
    assertThat(result).isEqualTo(ONE_AND_ONE_HALF_FRACTION)
  }

  @Test
  fun testToProperForm_largeNegativeImproperFraction_reducesToSimplestProperFraction() {
    val largeImproperFraction = Fraction.newBuilder().apply {
      isNegative = true
      numerator = 1650
      denominator = 209
    }.build()

    val result = largeImproperFraction.toProperForm()

    // Unlike toSimplestForm, toProperForm also extracts a whole number after reducing to the
    // simplest denominator.
    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(17)
    assertThat(result).hasDenominatorThat().isEqualTo(19)
    assertThat(result).hasWholeNumberThat().isEqualTo(7)
  }

  @Test
  fun testToProperForm_zeroDenominator_throwsException() {
    val zeroDenominatorFraction = Fraction.getDefaultInstance()

    // Converting to simplest form results in a divide by zero in this case.
    assertThrows(ArithmeticException::class) { zeroDenominatorFraction.toProperForm() }
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

  @Test
  fun testPlus_zeroAndZero_returnsZero() {
    val lhsFraction = ZERO_FRACTION
    val rhsFraction = ZERO_FRACTION

    val result = lhsFraction + rhsFraction

    assertThat(result).isEqualTo(ZERO_FRACTION)
  }

  @Test
  fun testPlus_oneAndZero_returnsOne() {
    val lhsFraction = ZERO_FRACTION
    val rhsFraction = ONE_FRACTION

    val result = lhsFraction + rhsFraction

    assertThat(result).isEqualTo(ONE_FRACTION)
  }

  @Test
  fun testPlus_oneHalfAndOneHalf_returnsOne() {
    val lhsFraction = ONE_HALF_FRACTION
    val rhsFraction = ONE_HALF_FRACTION

    val result = lhsFraction + rhsFraction

    assertThat(result).isEqualTo(ONE_FRACTION)
  }

  @Test
  fun testPlus_oneHalfAndNegativeOneHalf_returnsZero() {
    val lhsFraction = ONE_HALF_FRACTION
    val rhsFraction = NEGATIVE_ONE_HALF_FRACTION

    val result = lhsFraction + rhsFraction

    assertThat(result).isEqualTo(ZERO_FRACTION)
  }

  @Test
  fun testPlus_oneThirdAndOneHalf_returnsFiveSixths() {
    val lhsFraction = ONE_THIRD_FRACTION
    val rhsFraction = ONE_HALF_FRACTION

    val result = lhsFraction + rhsFraction

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(5)
    assertThat(result).hasDenominatorThat().isEqualTo(6)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testPlus_oneHalfAndOneThird_returnsFiveSixths() {
    val lhsFraction = ONE_HALF_FRACTION
    val rhsFraction = ONE_THIRD_FRACTION

    val result = lhsFraction + rhsFraction

    // Demonstrate commutativity, i.e.: a+b=b+a.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(5)
    assertThat(result).hasDenominatorThat().isEqualTo(6)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testPlus_twentyFiveThirtiethsAndFiveSevenths_returnsOneAndTwentyThreeFortyTwos() {
    val lhsFraction = Fraction.newBuilder().apply {
      numerator = 25
      denominator = 30
    }.build()
    val rhsFraction = Fraction.newBuilder().apply {
      numerator = 5
      denominator = 7
    }.build()

    val result = lhsFraction + rhsFraction

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(23)
    assertThat(result).hasDenominatorThat().isEqualTo(42)
    assertThat(result).hasWholeNumberThat().isEqualTo(1)
  }

  @Test
  fun testPlus_negativeOneAndOneThird_returnsNegativeTwoThirds() {
    val lhsFraction = NEGATIVE_ONE_FRACTION
    val rhsFraction = ONE_THIRD_FRACTION

    val result = lhsFraction + rhsFraction

    // Effectively subtracting fractions via addition should work as expected.
    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(2)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testPlus_oneAndNegativeOneThird_returnsTwoThirds() {
    val lhsFraction = ONE_FRACTION
    val rhsFraction = NEGATIVE_ONE_THIRD_FRACTION

    val result = lhsFraction + rhsFraction

    // Effectively subtracting fractions via addition should work as expected.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(2)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testPlus_negativeOneAndNegativeOneThird_returnsNegativeOneAndOneThird() {
    val lhsFraction = NEGATIVE_ONE_FRACTION
    val rhsFraction = NEGATIVE_ONE_THIRD_FRACTION

    val result = lhsFraction + rhsFraction

    // Negative addition should work as expected.
    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(1)
  }

  @Test
  fun testMinus_zeroAndZero_returnsZero() {
    val lhsFraction = ZERO_FRACTION
    val rhsFraction = ZERO_FRACTION

    val result = lhsFraction - rhsFraction

    assertThat(result).isEqualTo(ZERO_FRACTION)
  }

  @Test
  fun testMinus_oneAndZero_returnsOne() {
    val lhsFraction = ONE_FRACTION
    val rhsFraction = ZERO_FRACTION

    val result = lhsFraction - rhsFraction

    assertThat(result).isEqualTo(ONE_FRACTION)
  }

  @Test
  fun testMinus_oneHalfAndOneHalf_returnsZero() {
    val lhsFraction = ONE_HALF_FRACTION
    val rhsFraction = ONE_HALF_FRACTION

    val result = lhsFraction - rhsFraction

    assertThat(result).isEqualTo(ZERO_FRACTION)
  }

  @Test
  fun testMinus_oneHalfAndNegativeOneHalf_returnsOne() {
    val lhsFraction = ONE_HALF_FRACTION
    val rhsFraction = NEGATIVE_ONE_HALF_FRACTION

    val result = lhsFraction - rhsFraction

    // Minus a negative fraction should turn into regular addition.
    assertThat(result).isEqualTo(ONE_FRACTION)
  }

  @Test
  fun testMinus_oneThirdAndOneHalf_returnsNegativeOneSixth() {
    val lhsFraction = ONE_THIRD_FRACTION
    val rhsFraction = ONE_HALF_FRACTION

    val result = lhsFraction - rhsFraction

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(6)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testMinus_oneHalfAndOneThird_returnsOneSixth() {
    val lhsFraction = ONE_HALF_FRACTION
    val rhsFraction = ONE_THIRD_FRACTION

    val result = lhsFraction - rhsFraction

    // Demonstrate anticommutativity, i.e.: a-b=-(b-a).
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(6)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testMinus_twentyFiveThirtiethsAndTwentyThreeSevenths_returnsNegTwoAndNineteenFortyTwos() {
    val lhsFraction = Fraction.newBuilder().apply {
      numerator = 25
      denominator = 30
    }.build()
    val rhsFraction = Fraction.newBuilder().apply {
      numerator = 23
      denominator = 7
    }.build()

    val result = lhsFraction - rhsFraction

    // Verify that the result of subtraction results in a properly formed fraction.
    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(19)
    assertThat(result).hasDenominatorThat().isEqualTo(42)
    assertThat(result).hasWholeNumberThat().isEqualTo(2)
  }

  @Test
  fun testMinus_negativeOneAndOneThird_returnsNegativeOneAndOneThird() {
    val lhsFraction = NEGATIVE_ONE_FRACTION
    val rhsFraction = ONE_THIRD_FRACTION

    val result = lhsFraction - rhsFraction

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(1)
  }

  @Test
  fun testMinus_oneAndNegativeOneThird_returnsOneAndOneThird() {
    val lhsFraction = ONE_FRACTION
    val rhsFraction = NEGATIVE_ONE_THIRD_FRACTION

    val result = lhsFraction - rhsFraction

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(1)
  }

  @Test
  fun testMinus_negativeOneAndNegativeOneThird_returnsNegativeTwoThirds() {
    val lhsFraction = NEGATIVE_ONE_FRACTION
    val rhsFraction = NEGATIVE_ONE_THIRD_FRACTION

    val result = lhsFraction - rhsFraction

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(2)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testTimes_zeroAndZero_returnsZero() {
    val lhsFraction = ZERO_FRACTION
    val rhsFraction = ZERO_FRACTION

    val result = lhsFraction * rhsFraction

    assertThat(result).isEqualTo(ZERO_FRACTION)
  }

  @Test
  fun testTimes_oneAndZero_returnsZero() {
    val lhsFraction = ONE_FRACTION
    val rhsFraction = ZERO_FRACTION

    val result = lhsFraction * rhsFraction

    assertThat(result).isEqualTo(ZERO_FRACTION)
  }

  @Test
  fun testTimes_oneAndOne_returnsOne() {
    val lhsFraction = ONE_FRACTION
    val rhsFraction = ONE_FRACTION

    val result = lhsFraction * rhsFraction

    assertThat(result).isEqualTo(ONE_FRACTION)
  }

  @Test
  fun testTimes_twoAndOne_returnsTwo() {
    val lhsFraction = TWO_FRACTION
    val rhsFraction = ONE_FRACTION

    val result = lhsFraction * rhsFraction

    assertThat(result).isEqualTo(TWO_FRACTION)
  }

  @Test
  fun testTimes_oneHalfAndOneThird_returnsOneSixth() {
    val lhsFraction = ONE_HALF_FRACTION
    val rhsFraction = ONE_THIRD_FRACTION

    val result = lhsFraction * rhsFraction

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(6)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testTimes_oneThirdAndOneHalf_returnsOneSixth() {
    val lhsFraction = ONE_THIRD_FRACTION
    val rhsFraction = ONE_HALF_FRACTION

    val result = lhsFraction * rhsFraction

    // Demonstrate commutativity, i.e.: a*b=b*a.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(6)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testTimes_sevenHalvesAndTwentyFifteenths_returnsFourAndTwoThirds() {
    val lhsFraction = Fraction.newBuilder().apply {
      numerator = 7
      denominator = 2
    }.build()
    val rhsFraction = Fraction.newBuilder().apply {
      numerator = 20
      denominator = 15
    }.build()

    val result = lhsFraction * rhsFraction

    // Demonstrate that the multiplied result is a fully properly form fraction.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(2)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(4)
  }

  @Test
  fun testTimes_negativeTwoAndOneThird_returnsNegativeTwoThirds() {
    val lhsFraction = NEGATIVE_TWO_FRACTION
    val rhsFraction = ONE_THIRD_FRACTION

    val result = lhsFraction * rhsFraction

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(2)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testTimes_twoAndNegativeOneThird_returnsNegativeTwoThirds() {
    val lhsFraction = TWO_FRACTION
    val rhsFraction = NEGATIVE_ONE_THIRD_FRACTION

    val result = lhsFraction * rhsFraction

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(2)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testTimes_negativeTwoAndNegativeOneThird_returnsTwoThirds() {
    val lhsFraction = NEGATIVE_TWO_FRACTION
    val rhsFraction = NEGATIVE_ONE_THIRD_FRACTION

    val result = lhsFraction * rhsFraction

    // The negatives cancel out during multiplication.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(2)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testDivides_zeroAndZero_throwsException() {
    val lhsFraction = ZERO_FRACTION
    val rhsFraction = ZERO_FRACTION

    assertThrows(Exception::class) { lhsFraction / rhsFraction }
  }

  @Test
  fun testDivides_oneAndZero_throwsException() {
    val lhsFraction = ONE_FRACTION
    val rhsFraction = ZERO_FRACTION

    assertThrows(Exception::class) { lhsFraction / rhsFraction }
  }

  @Test
  fun testDivides_twoAndZero_throwsException() {
    val lhsFraction = TWO_FRACTION
    val rhsFraction = ZERO_FRACTION

    assertThrows(Exception::class) { lhsFraction / rhsFraction }
  }

  @Test
  fun testDivides_twoAndOne_returnsTwo() {
    val lhsFraction = TWO_FRACTION
    val rhsFraction = ONE_FRACTION

    val result = lhsFraction / rhsFraction

    assertThat(result).isEqualTo(TWO_FRACTION)
  }

  @Test
  fun testDivides_oneHalfAndOneThird_returnsOneAndOneHalf() {
    val lhsFraction = ONE_HALF_FRACTION
    val rhsFraction = ONE_THIRD_FRACTION

    val result = lhsFraction / rhsFraction

    // (1/2)/(1/3)=3/2=1 1/2.
    assertThat(result).isEqualTo(ONE_AND_ONE_HALF_FRACTION)
  }

  @Test
  fun testDivides_oneThirdAndOneHalf_returnsTwoThirds() {
    val lhsFraction = ONE_THIRD_FRACTION
    val rhsFraction = ONE_HALF_FRACTION

    val result = lhsFraction / rhsFraction

    // Demonstrate anticommutativity, i.e.: a/b=1/(b/a).
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(2)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testDivides_fourThirdsAndTenThirtyFifths_returnsFourAndTwoThirds() {
    val lhsFraction = Fraction.newBuilder().apply {
      numerator = 4
      denominator = 3
    }.build()
    val rhsFraction = Fraction.newBuilder().apply {
      numerator = 10
      denominator = 35
    }.build()

    val result = lhsFraction / rhsFraction

    // Demonstrate that the divided result is a fully properly form fraction.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(2)
    assertThat(result).hasDenominatorThat().isEqualTo(3)
    assertThat(result).hasWholeNumberThat().isEqualTo(4)
  }

  @Test
  fun testDivides_negativeTwoAndOneThird_returnsNegativeSix() {
    val lhsFraction = NEGATIVE_TWO_FRACTION
    val rhsFraction = ONE_THIRD_FRACTION

    val result = lhsFraction / rhsFraction

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(6)
  }

  @Test
  fun testDivides_twoAndNegativeOneThird_returnsNegativeSix() {
    val lhsFraction = TWO_FRACTION
    val rhsFraction = NEGATIVE_ONE_THIRD_FRACTION

    val result = lhsFraction / rhsFraction

    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(6)
  }

  @Test
  fun testDivides_negativeTwoAndNegativeOneThird_returnsSix() {
    val lhsFraction = NEGATIVE_TWO_FRACTION
    val rhsFraction = NEGATIVE_ONE_THIRD_FRACTION

    val result = lhsFraction / rhsFraction

    // The negatives cancel out during division.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(6)
  }

  @Test
  fun testPow_zeroToZero_returnsOne() {
    val fraction = ZERO_FRACTION

    val result = fraction pow 0

    // See pow's documentation for specifics.
    assertThat(result).isEqualTo(ONE_FRACTION)
  }

  @Test
  fun testPow_oneToZero_returnsOne() {
    val fraction = ONE_FRACTION

    val result = fraction pow 0

    assertThat(result).isEqualTo(ONE_FRACTION)
  }

  @Test
  fun testPow_oneToOne_returnsOne() {
    val fraction = ONE_FRACTION

    val result = fraction pow 1

    assertThat(result).isEqualTo(ONE_FRACTION)
  }

  @Test
  fun testPow_twoToZero_returnsOne() {
    val fraction = TWO_FRACTION

    val result = fraction pow 0

    assertThat(result).isEqualTo(ONE_FRACTION)
  }

  @Test
  fun testPow_twoToOne_returnsTwo() {
    val fraction = TWO_FRACTION

    val result = fraction pow 1

    assertThat(result).isEqualTo(TWO_FRACTION)
  }

  @Test
  fun testPow_twoToTwo_returnsFour() {
    val fraction = TWO_FRACTION

    val result = fraction pow 2

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(4)
  }

  @Test
  fun testPow_oneThirdToTwo_returnsOneNinth() {
    val fraction = ONE_THIRD_FRACTION

    val result = fraction pow 2

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(9)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testPow_negativeOneThirdToTwo_returnsOneNinth() {
    val fraction = NEGATIVE_ONE_THIRD_FRACTION

    val result = fraction pow 2

    // The negative sign is lost since the power is even.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(9)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testPow_negativeOneThirdToThree_returnsNegativeOneTwentySeventh() {
    val fraction = NEGATIVE_ONE_THIRD_FRACTION

    val result = fraction pow 3

    // The negative sign is preserved since the power is odd.
    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(27)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testPow_twoToNegativeTwo_returnsOneFourth() {
    val fraction = TWO_FRACTION

    val result = fraction pow -2

    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(1)
    assertThat(result).hasDenominatorThat().isEqualTo(4)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testPow_oneThirdToNegativeThree_returnsTwentySeven() {
    val fraction = ONE_THIRD_FRACTION

    val result = fraction pow -3

    // The negative sign is preserved since the power is odd.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(27)
  }

  @Test
  fun testPow_negativeOneThirdToNegativeTwo_returnsNine() {
    val fraction = NEGATIVE_ONE_THIRD_FRACTION

    val result = fraction pow -2

    // The negative sign is lost since the power is even.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(9)
  }

  @Test
  fun testPow_negativeOneThirdToNegativeThree_returnsNegativeTwentySeven() {
    val fraction = NEGATIVE_ONE_THIRD_FRACTION

    val result = fraction pow -3

    // The negative sign is preserved since the power is odd.
    assertThat(result).hasNegativePropertyThat().isTrue()
    assertThat(result).hasNumeratorThat().isEqualTo(0)
    assertThat(result).hasDenominatorThat().isEqualTo(1)
    assertThat(result).hasWholeNumberThat().isEqualTo(27)
  }

  @Test
  fun testPow_fourSeventhsCubed_returnsSixtyFourThreeHundredFortyThirds() {
    val fraction = Fraction.newBuilder().apply {
      numerator = 4
      denominator = 7
    }.build()

    val result = fraction pow 3

    // Verify that the numerator is also correctly multiplied.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(64)
    assertThat(result).hasDenominatorThat().isEqualTo(343)
    assertThat(result).hasWholeNumberThat().isEqualTo(0)
  }

  @Test
  fun testPow_twentyOneTwelfthsToNegativeThree_returnsFiveAndTwentyThreeSixtyFourths() {
    val fraction = Fraction.newBuilder().apply {
      numerator = 12
      denominator = 21
    }.build()

    val result = fraction pow -3

    // Verify that the resulting value is in fully proper form.
    assertThat(result).hasNegativePropertyThat().isFalse()
    assertThat(result).hasNumeratorThat().isEqualTo(23)
    assertThat(result).hasDenominatorThat().isEqualTo(64)
    assertThat(result).hasWholeNumberThat().isEqualTo(5)
  }

  @Test
  fun testToWholeNumberFraction_zero_returnsZeroFraction() {
    val wholeNumber = 0

    val fraction = wholeNumber.toWholeNumberFraction()

    assertThat(fraction).isEqualTo(ZERO_FRACTION)
  }

  @Test
  fun testToWholeNumberFraction_one_returnsOneFraction() {
    val wholeNumber = 1

    val fraction = wholeNumber.toWholeNumberFraction()

    assertThat(fraction).isEqualTo(ONE_FRACTION)
  }

  @Test
  fun testToWholeNumberFraction_twentyThree_returnsTwentyThreeFraction() {
    val wholeNumber = 23

    val fraction = wholeNumber.toWholeNumberFraction()

    assertThat(fraction).hasNegativePropertyThat().isFalse()
    assertThat(fraction).hasNumeratorThat().isEqualTo(0)
    assertThat(fraction).hasDenominatorThat().isEqualTo(1)
    assertThat(fraction).hasWholeNumberThat().isEqualTo(23)
  }

  @Test
  fun testToWholeNumberFraction_negativeZero_returnsZeroFraction() {
    val wholeNumber = -0

    val fraction = wholeNumber.toWholeNumberFraction()

    assertThat(fraction).isEqualTo(ZERO_FRACTION)
  }

  @Test
  fun testToWholeNumberFraction_negativeOne_returnsNegativeOneFraction() {
    val wholeNumber = -1

    val fraction = wholeNumber.toWholeNumberFraction()

    assertThat(fraction).isEqualTo(NEGATIVE_ONE_FRACTION)
  }

  @Test
  fun testToWholeNumberFraction_negativeTwentyThree_returnsNegativeTwentyThreeFraction() {
    val wholeNumber = -23

    val fraction = wholeNumber.toWholeNumberFraction()

    assertThat(fraction).hasNegativePropertyThat().isTrue()
    assertThat(fraction).hasNumeratorThat().isEqualTo(0)
    assertThat(fraction).hasDenominatorThat().isEqualTo(1)
    assertThat(fraction).hasWholeNumberThat().isEqualTo(23)
  }
}
