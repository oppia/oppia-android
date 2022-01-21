package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Real
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.math.RealSubject.Companion.assertThat
import org.robolectric.annotation.LooperMode

/** Tests for [Real] extensions. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class RealExtensionsTest {
  private companion object {
    private const val PI = 3.1415

    private val ONE_HALF_FRACTION = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 2
    }.build()

    private val ONE_AND_ONE_HALF_FRACTION = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 2
      wholeNumber = 1
    }.build()

    private val ZERO_REAL = createIntegerReal(0)
    private val TWO_REAL = createIntegerReal(2)
    private val NEGATIVE_TWO_REAL = createIntegerReal(-2)

    private val ONE_HALF_REAL = createRationalReal(ONE_HALF_FRACTION)
    private val NEGATIVE_ONE_HALF_REAL = createRationalReal(-ONE_HALF_FRACTION)
    private val ONE_AND_ONE_HALF_REAL = createRationalReal(ONE_AND_ONE_HALF_FRACTION)
    private val NEGATIVE_ONE_AND_ONE_HALF_REAL = createRationalReal(-ONE_AND_ONE_HALF_FRACTION)

    private val PI_REAL = createIrrationalReal(PI)
    private val NEGATIVE_PI_REAL = createIrrationalReal(-PI)
  }

  @Test
  fun testIsRational_default_returnsFalse() {
    val defaultReal = Real.getDefaultInstance()

    val result = defaultReal.isRational()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsRational_twoInteger_returnsFalse() {
    val result = TWO_REAL.isRational()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsRational_oneHalfFraction_returnsTrue() {
    val result = ONE_HALF_REAL.isRational()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsRational_piIrrational_returnsFalse() {
    val result = PI_REAL.isRational()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsNegative_default_throwsException() {
    val defaultReal = Real.getDefaultInstance()

    val exception = assertThrows(IllegalStateException::class) { defaultReal.isNegative() }

    assertThat(exception).hasMessageThat().contains("Invalid real")
  }

  @Test
  fun testIsNegative_twoInteger_returnsFalse() {
    val result = TWO_REAL.isNegative()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsNegative_negativeTwoInteger_returnsTrue() {
    val result = NEGATIVE_TWO_REAL.isNegative()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsNegative_oneHalfFraction_returnsFalse() {
    val result = ONE_HALF_REAL.isNegative()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsNegative_negativeOneHalfFraction_returnsTrue() {
    val result = NEGATIVE_ONE_HALF_REAL.isNegative()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsNegative_piIrrational_returnsFalse() {
    val result = PI_REAL.isNegative()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsNegative_negativePiIrrational_returnsTrue() {
    val result = NEGATIVE_PI_REAL.isNegative()

    assertThat(result).isTrue()
  }

  @Test
  fun testToDouble_default_returnsZeroDouble() {
    val defaultReal = Real.getDefaultInstance()

    val exception = assertThrows(IllegalStateException::class) { defaultReal.toDouble() }

    assertThat(exception).hasMessageThat().contains("Invalid real")
  }

  @Test
  fun testToDouble_twoInteger_returnsTwoDouble() {
    val result = TWO_REAL.toDouble()

    assertThat(result).isWithin(1e-5).of(2.0)
  }

  @Test
  fun testToDouble_negativeTwoInteger_returnsNegativeTwoDouble() {
    val result = NEGATIVE_TWO_REAL.toDouble()

    assertThat(result).isWithin(1e-5).of(-2.0)
  }

  @Test
  fun testToDouble_oneHalfFraction_returnsPointFive() {
    val result = ONE_HALF_REAL.toDouble()

    assertThat(result).isWithin(1e-5).of(0.5)
  }

  @Test
  fun testToDouble_negativeOneHalfFraction_returnsNegativePointFive() {
    val result = NEGATIVE_ONE_HALF_REAL.toDouble()

    assertThat(result).isWithin(1e-5).of(-0.5)
  }

  @Test
  fun testToDouble_piIrrational_returnsPi() {
    val result = PI_REAL.toDouble()

    assertThat(result).isWithin(1e-5).of(PI)
  }

  @Test
  fun testToDouble_negativePiIrrational_returnsNegativePi() {
    val result = NEGATIVE_PI_REAL.toDouble()

    assertThat(result).isWithin(1e-5).of(-PI)
  }

  @Test
  fun testToPlainText_default_returnsEmptyString() {
    val defaultReal = Real.getDefaultInstance()

    val result = defaultReal.toPlainText()

    assertThat(result).isEmpty()
  }

  @Test
  fun testToPlainText_twoInteger_returnsTwoString() {
    val result = TWO_REAL.toPlainText()

    assertThat(result).isEqualTo("2")
  }

  @Test
  fun testToPlainText_negativeTwoInteger_returnsMinusTwoString() {
    val result = NEGATIVE_TWO_REAL.toPlainText()

    assertThat(result).isEqualTo("-2")
  }

  @Test
  fun testToPlainText_oneHalfFraction_returnsOneHalfString() {
    val result = ONE_HALF_REAL.toPlainText()

    assertThat(result).isEqualTo("1/2")
  }

  @Test
  fun testToPlainText_negativeOneHalfFraction_returnsMinusOneHalfString() {
    val result = NEGATIVE_ONE_HALF_REAL.toPlainText()

    assertThat(result).isEqualTo("-1/2")
  }

  @Test
  fun testToPlainText_oneAndOneHalfFraction_returnsThreeHalvesString() {
    val result = ONE_AND_ONE_HALF_REAL.toPlainText()

    assertThat(result).isEqualTo("3/2")
  }

  @Test
  fun testToPlainText_negativeOneAndOneHalfFraction_returnsMinusThreeHalvesString() {
    val result = NEGATIVE_ONE_AND_ONE_HALF_REAL.toPlainText()

    assertThat(result).isEqualTo("-3/2")
  }

  @Test
  fun testToPlainText_piIrrational_returnsPiString() {
    val result = PI_REAL.toPlainText()

    assertThat(result).isEqualTo("3.1415")
  }

  @Test
  fun testToPlainText_negativePiIrrational_returnsMinusPiString() {
    val result = NEGATIVE_PI_REAL.toPlainText()

    assertThat(result).isEqualTo("-3.1415")
  }

  @Test
  fun testIsApproximatelyEqualTo_zeroIntegerAndZero_returnsTrue() {
    val result = ZERO_REAL.isApproximatelyEqualTo(0.0)

    assertThat(result).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_zeroAndOne_returnsFalse() {
    val result = ZERO_REAL.isApproximatelyEqualTo(1.0)

    assertThat(result).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_twoAndTwoWithinThreshold_returnsTrue() {
    val result = TWO_REAL.isApproximatelyEqualTo(2.0 + FLOAT_EQUALITY_INTERVAL / 2.0)

    assertThat(result).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_twoAndTwoOutsideThreshold_returnsFalse() {
    val result = TWO_REAL.isApproximatelyEqualTo(2.0 + FLOAT_EQUALITY_INTERVAL * 2.0)

    assertThat(result).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_oneHalfAndOne_returnsFalse() {
    val result = ONE_HALF_REAL.isApproximatelyEqualTo(1.0)

    assertThat(result).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_oneHalfAndPointFive_returnsTrue() {
    val result = ONE_HALF_REAL.isApproximatelyEqualTo(0.5)

    assertThat(result).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_oneHalfAndPointSix_returnsFalse() {
    val result = ONE_HALF_REAL.isApproximatelyEqualTo(0.6)

    assertThat(result).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_pointFiveAndPointFive_returnsTrue() {
    val pointFiveReal = createIrrationalReal(0.5)

    val result = pointFiveReal.isApproximatelyEqualTo(0.5)

    assertThat(result).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_pointFiveAndPointSix_returnsFalse() {
    val pointFiveReal = createIrrationalReal(0.5)

    val result = pointFiveReal.isApproximatelyEqualTo(0.6)

    assertThat(result).isFalse()
  }

  @Test
  fun testUnaryMinus_default_throwsException() {
    val defaultReal = Real.getDefaultInstance()

    val exception = assertThrows(IllegalStateException::class) { -defaultReal }

    assertThat(exception).hasMessageThat().contains("Invalid real")
  }

  @Test
  fun testUnaryMinus_twoInteger_returnsNegativeTwoInteger() {
    val result = -TWO_REAL

    assertThat(result).isIntegerThat().isEqualTo(-2)
  }

  @Test
  fun testUnaryMinus_negativeTwoInteger_returnsTwoInteger() {
    val result = -NEGATIVE_TWO_REAL

    assertThat(result).isIntegerThat().isEqualTo(2)
  }

  @Test
  fun testUnaryMinus_twoOneHalf_returnsNegativeOneHalf() {
    val result = -ONE_HALF_REAL

    assertThat(result).isRationalThat().evaluatesToDoubleThat().isWithin(1e-5).of(-0.5)
  }

  @Test
  fun testUnaryMinus_negativeOneHalf_returnsOneHalf() {
    val result = -NEGATIVE_ONE_HALF_REAL

    assertThat(result).isRationalThat().evaluatesToDoubleThat().isWithin(1e-5).of(0.5)
  }

  @Test
  fun testUnaryMinus_pi_returnsNegativePi() {
    val result = -PI_REAL

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(-PI)
  }

  @Test
  fun testUnaryMinus_negativePi_returnsPi() {
    val result = -NEGATIVE_PI_REAL

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(PI)
  }

  @Test
  fun testAbs_twoInteger_returnsTwoInteger() {
    val result = abs(TWO_REAL)

    assertThat(result).isIntegerThat().isEqualTo(2)
  }

  @Test
  fun testAbs_negativeTwoInteger_returnsTwoInteger() {
    val result = abs(NEGATIVE_TWO_REAL)

    assertThat(result).isIntegerThat().isEqualTo(2)
  }

  @Test
  fun testAbs_oneHalf_returnsOneHalf() {
    val result = abs(ONE_HALF_REAL)

    assertThat(result).isRationalThat().evaluatesToDoubleThat().isWithin(1e-5).of(0.5)
  }

  @Test
  fun testAbs_negativeOneHalf_returnsOneHalf() {
    val result = abs(NEGATIVE_ONE_HALF_REAL)

    assertThat(result).isRationalThat().evaluatesToDoubleThat().isWithin(1e-5).of(0.5)
  }

  @Test
  fun testAbs_pi_returnsPi() {
    val result = abs(PI_REAL)

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(PI)
  }

  @Test
  fun testAbs_negativePi_returnsPi() {
    val result = abs(NEGATIVE_PI_REAL)

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(PI)
  }
}

private fun createIntegerReal(value: Int) = Real.newBuilder().apply {
  integer = value
}.build()

private fun createRationalReal(value: Fraction) = Real.newBuilder().apply {
  rational = value
}.build()

private fun createIrrationalReal(value: Double) = Real.newBuilder().apply {
  irrational = value
}.build()
