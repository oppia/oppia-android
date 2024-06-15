package org.oppia.android.util.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Real
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedJunitTestRunner
import org.oppia.android.testing.math.RealSubject.Companion.assertThat
import org.robolectric.annotation.LooperMode

/**
 * Tests for [Real] extensions.
 *
 * Note that this suite makes special use of parameterized tests to significantly reduce the length
 * of the suite, even partially at the expensive of good testing practices (since many of the
 * parameterized tests are actually verifying multiple behaviors). Given the generally trivial
 * nature of these behaviors, this trade-off is considered acceptable. That being said, this pattern
 * should only be replicated elsewhere in the codebase after thorough consideration.
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedJunitTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class RealExtensionsTest {
  private companion object {
    private const val PI = 3.1415

    private val ZERO_FRACTION = Fraction.newBuilder().apply {
      numerator = 0
      denominator = 1
    }.build()

    private val ONE_HALF_FRACTION = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 2
    }.build()

    private val ONE_FOURTH_FRACTION = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 4
    }.build()

    private val ONE_AND_ONE_HALF_FRACTION = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 2
      wholeNumber = 1
    }.build()

    private val THREE_FRACTION = Fraction.newBuilder().apply {
      wholeNumber = 3
      denominator = 1
    }.build()

    private val THREE_ONES_FRACTION = Fraction.newBuilder().apply {
      numerator = 3
      denominator = 1
    }.build()

    private val ZERO_REAL = createIntegerReal(0)
    private val TWO_REAL = createIntegerReal(2)
    private val NEGATIVE_TWO_REAL = createIntegerReal(-2)

    private val ONE_HALF_REAL = createRationalReal(ONE_HALF_FRACTION)
    private val NEGATIVE_ONE_HALF_REAL = createRationalReal(-ONE_HALF_FRACTION)
    private val ONE_AND_ONE_HALF_REAL = createRationalReal(ONE_AND_ONE_HALF_FRACTION)
    private val NEGATIVE_ONE_AND_ONE_HALF_REAL = createRationalReal(-ONE_AND_ONE_HALF_FRACTION)
    private val THREE_FRACTION_REAL = createRationalReal(THREE_FRACTION)
    private val NEGATIVE_THREE_FRACTION_REAL = createRationalReal(-THREE_FRACTION)
    private val THREE_ONES_REAL = createRationalReal(THREE_ONES_FRACTION)

    private val PI_REAL = createIrrationalReal(PI)
    private val NEGATIVE_PI_REAL = createIrrationalReal(-PI)
  }

  private val fractionParser by lazy { FractionParser() }

  @Parameter var lhsInt: Int = Int.MIN_VALUE
  @Parameter lateinit var lhsFrac: String
  @Parameter var lhsDouble: Double = Double.MIN_VALUE
  @Parameter var rhsInt: Int = Int.MIN_VALUE
  @Parameter lateinit var rhsFrac: String
  @Parameter var rhsDouble: Double = Double.MIN_VALUE
  @Parameter var expInt: Int = Int.MIN_VALUE
  @Parameter lateinit var expFrac: String
  @Parameter var expDouble: Double = Double.MIN_VALUE

  @Test
  fun testZero_isZeroInteger() {
    val subject = ZERO

    assertThat(subject).isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testOne_isOneInteger() {
    val subject = ONE

    assertThat(subject).isIntegerThat().isEqualTo(1)
  }

  @Test
  fun testOneHalf_isOneHalfRational() {
    val subject = ONE_HALF

    assertThat(subject).isRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(0)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(2)
    }
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
  fun testIsInteger_default_returnsFalse() {
    val defaultReal = Real.getDefaultInstance()

    val result = defaultReal.isInteger()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsInteger_twoInteger_returnsTrue() {
    val result = TWO_REAL.isInteger()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsInteger_oneHalfFraction_returnsFalse() {
    val result = ONE_HALF_REAL.isInteger()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsInteger_piIrrational_returnsFalse() {
    val result = PI_REAL.isInteger()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsWholeNumber_default_returnsFalse() {
    val defaultReal = Real.getDefaultInstance()

    val result = defaultReal.isWholeNumber()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsWholeNumber_twoInteger_returnsTrue() {
    val result = TWO_REAL.isWholeNumber()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsWholeNumber_negativeTwoInteger_returnsTrue() {
    val result = NEGATIVE_TWO_REAL.isWholeNumber()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsWholeNumber_oneHalfFraction_returnsFalse() {
    val result = ONE_HALF_REAL.isWholeNumber()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsWholeNumber_threeOnesFraction_returnsFalse() {
    val result = THREE_ONES_REAL.isWholeNumber()

    // 3/1 is treated as a fraction despite being numerically equivalent to a whole number.
    assertThat(result).isFalse()
  }

  @Test
  fun testIsWholeNumber_threeFraction_returnsTrue() {
    val result = THREE_FRACTION_REAL.isWholeNumber()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsWholeNumber_negativeThreeFraction_returnsTrue() {
    val result = NEGATIVE_THREE_FRACTION_REAL.isWholeNumber()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsWholeNumber_piIrrational_returnsFalse() {
    val result = PI_REAL.isWholeNumber()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsWholeNumber_threeIrrational_returnsFalse() {
    val real = createIrrationalReal(3.0)

    val result = real.isWholeNumber()

    // Despite 3.0 being approximately a whole number, it isn't considered one since it's a double
    // (and thus can have precision loss).
    assertThat(result).isFalse()
  }

  @Test
  fun testIsNegative_default_throwsException() {
    val defaultReal = Real.getDefaultInstance()

    val exception = assertThrows<IllegalStateException>() { defaultReal.isNegative() }

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

  /*
   * Approximate equality checks between reals. Note that all of these tests are symmetrical to
   * reduce the number of test cases.
   */

  @Test
  fun testIsApproximatelyEqualTo_firstIsDefault_secondIsInt2_throwsException() {
    val first = Real.getDefaultInstance()
    val second = TWO_REAL

    val exception = assertThrows<IllegalStateException>() {
      first.isApproximatelyEqualTo(second)
    }

    assertThat(exception).hasMessageThat().contains("Invalid real")
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsInt2_secondIsDefault_throwsException() {
    val first = TWO_REAL
    val second = Real.getDefaultInstance()

    val exception = assertThrows<IllegalStateException>() {
      first.isApproximatelyEqualTo(second)
    }

    assertThat(exception).hasMessageThat().contains("Invalid real")
  }

  @Test
  @Iteration("0==0", "lhsInt=0", "rhsInt=0")
  @Iteration("1==1", "lhsInt=1", "rhsInt=1")
  @Iteration("2==2", "lhsInt=2", "rhsInt=2")
  @Iteration("-2==-2", "lhsInt=-2", "rhsInt=-2")
  fun testIsApproximatelyEqualTo_oneIsInt_otherIsSameInt_returnsTrue() {
    val first = createIntegerReal(lhsInt)
    val second = createIntegerReal(rhsInt)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // Verify both correctness and the symmetric of equality.
    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  @Iteration("0!=1", "lhsInt=0", "rhsInt=1")
  @Iteration("0!=2", "lhsInt=0", "rhsInt=2")
  @Iteration("-2!=2", "lhsInt=-2", "rhsInt=2")
  @Iteration("-2!=-1", "lhsInt=-2", "rhsInt=-1")
  fun testIsApproximatelyEqualTo_oneIsInt_otherIsDifferentInt_returnsFalse() {
    val first = createIntegerReal(lhsInt)
    val second = createIntegerReal(rhsInt)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = first.isApproximatelyEqualTo(second)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  @Iteration("0==0", "lhsInt=0", "rhsFrac=0")
  @Iteration("2==2", "lhsInt=2", "rhsFrac=2")
  @Iteration("2==2/1", "lhsInt=2", "rhsFrac=2/1")
  @Iteration("2==4/2", "lhsInt=2", "rhsFrac=4/2")
  @Iteration("-2==-2", "lhsInt=-2", "rhsFrac=-2")
  @Iteration("-2==-2/1", "lhsInt=-2", "rhsFrac=-2/1")
  @Iteration("-2==-4/2", "lhsInt=-2", "rhsFrac=-4/2")
  fun testIsApproximatelyEqualTo_oneIsInt_otherIsSameFraction_returnsTrue() {
    val first = createIntegerReal(lhsInt)
    val second = createRationalReal(rhsFrac)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // Verify both correctness and the symmetric of equality.
    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  @Iteration("0!=2", "lhsInt=0", "rhsFrac=2")
  @Iteration("2!=4", "lhsInt=2", "rhsFrac=4")
  @Iteration("2!=3/2", "lhsInt=2", "rhsFrac=3/2")
  @Iteration("2!=-2", "lhsInt=2", "rhsFrac=-2")
  fun testIsApproximatelyEqualTo_oneIsInt_otherIsDifferentFraction_returnsFalse() {
    val first = createIntegerReal(lhsInt)
    val second = createRationalReal(rhsFrac)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = first.isApproximatelyEqualTo(second)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  @Iteration("0==0.0", "lhsInt=0", "rhsDouble=0.0")
  @Iteration("1==1.0", "lhsInt=1", "rhsDouble=1.0")
  @Iteration("2==2.0", "lhsInt=2", "rhsDouble=2.0")
  @Iteration("2==2.000000000000001", "lhsInt=2", "rhsDouble=2.000000000000001")
  @Iteration("2==1.999999999999999", "lhsInt=2", "rhsDouble=1.999999999999999")
  @Iteration("-2==-2.0", "lhsInt=-2", "rhsDouble=-2.0")
  @Iteration("-2==-2.00000000000001", "lhsInt=-2", "rhsDouble=-2.00000000000001")
  @Iteration("-2==-1.999999999999999", "lhsInt=-2", "rhsDouble=-1.999999999999999")
  fun testIsApproximatelyEqualTo_oneIsInt_otherIsSimilarDouble_returnsTrue() {
    val first = createIntegerReal(lhsInt)
    val second = createIrrationalReal(rhsDouble)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // Verify both correctness and the symmetric of equality.
    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  @Iteration("0!=2.0", "lhsInt=0", "rhsDouble=2.0")
  @Iteration("2!=0.0", "lhsInt=2", "rhsDouble=0.0")
  @Iteration("2!=4.0", "lhsInt=2", "rhsDouble=4.0")
  @Iteration("3!=3.14", "lhsInt=3", "rhsDouble=3.14")
  @Iteration("2!=2.001", "lhsInt=2", "rhsDouble=2.001")
  @Iteration("2!=1.999", "lhsInt=2", "rhsDouble=1.999")
  @Iteration("2!=-2.0", "lhsInt=2", "rhsDouble=-2.0")
  @Iteration("-2!=2.0", "lhsInt=-2", "rhsDouble=2.0")
  @Iteration("-2!=-2.001", "lhsInt=-2", "rhsDouble=-2.001")
  @Iteration("-2!=-1.999", "lhsInt=-2", "rhsDouble=-1.999")
  fun testIsApproximatelyEqualTo_oneIsInt_otherIsDifferentDouble_returnsFalse() {
    val first = createIntegerReal(lhsInt)
    val second = createIrrationalReal(rhsDouble)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = first.isApproximatelyEqualTo(second)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  @Iteration("0==0", "lhsFrac=0", "rhsFrac=0")
  @Iteration("2==2", "lhsFrac=2", "rhsFrac=2")
  @Iteration("2==4/2", "lhsFrac=2", "rhsFrac=4/2")
  @Iteration("3/2==1 1/2", "lhsFrac=3/2", "rhsFrac=1 1/2")
  @Iteration("-2==-2", "lhsFrac=-2", "rhsFrac=-2")
  @Iteration("-2==-4/2", "lhsFrac=-2", "rhsFrac=-4/2")
  @Iteration("-3/2==-1 1/2", "lhsFrac=-3/2", "rhsFrac=-1 1/2")
  @Iteration("1/3==3/9", "lhsFrac=1/3", "rhsFrac=3/9")
  fun testIsApproximatelyEqualTo_oneIsFraction_otherIsSameFraction_returnsTrue() {
    val first = createRationalReal(lhsFrac)
    val second = createRationalReal(rhsFrac)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // Verify both correctness and the symmetric of equality.
    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  @Iteration("0!=2", "lhsFrac=0", "rhsFrac=2")
  @Iteration("3/2!=1/2", "lhsFrac=3/2", "rhsFrac=1/2")
  @Iteration("3/2!=1", "lhsFrac=3/2", "rhsFrac=1")
  @Iteration("3/2!=-1 1/2", "lhsFrac=3/2", "rhsFrac=-1 1/2")
  @Iteration("-3/2!=1 1/2", "lhsFrac=-3/2", "rhsFrac=1 1/2")
  @Iteration("-3/2!=-1/2", "lhsFrac=-3/2", "rhsFrac=-1/2")
  @Iteration("1/3!=2/3", "lhsFrac=1/3", "rhsFrac=2/3")
  fun testIsApproximatelyEqualTo_oneIsFraction_otherIsDifferentFraction_returnsFalse() {
    val first = createRationalReal(lhsFrac)
    val second = createRationalReal(rhsFrac)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = first.isApproximatelyEqualTo(second)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  @Iteration("0==0.0", "lhsFrac=0", "rhsDouble=0.0")
  @Iteration("2==2.0", "lhsFrac=2", "rhsDouble=2.0")
  @Iteration("2/1==2.0", "lhsFrac=2/1", "rhsDouble=2.0")
  @Iteration("3/2==1.5", "lhsFrac=3/2", "rhsDouble=1.5")
  @Iteration("1/3==0.33333333333333333", "lhsFrac=1/3", "rhsDouble=0.33333333333333333")
  @Iteration("1 2/3==1.66666666666666666", "lhsFrac=1 2/3", "rhsDouble=1.66666666666666666")
  @Iteration("-2==-2.0", "lhsFrac=-2", "rhsDouble=-2.0")
  @Iteration("-3/2==-1.5", "lhsFrac=-3/2", "rhsDouble=-1.5")
  fun testIsApproximatelyEqualTo_oneIsFraction_otherIsSimilarDouble_returnsTrue() {
    val first = createRationalReal(lhsFrac)
    val second = createIrrationalReal(rhsDouble)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // Verify both correctness and the symmetric of equality.
    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  @Iteration("0!=2.0", "lhsFrac=0", "rhsDouble=2.0")
  @Iteration("2!=0.0", "lhsFrac=2", "rhsDouble=0.0")
  @Iteration("2/2!=2.0", "lhsFrac=2/2", "rhsDouble=2.0")
  @Iteration("1/3!=0.333", "lhsFrac=1/3", "rhsDouble=0.333")
  @Iteration("1 2/3!=1.667", "lhsFrac=1 2/3", "rhsDouble=1.667")
  @Iteration("22/7!=3.14", "lhsFrac=22/7", "rhsDouble=3.14")
  @Iteration("-2!=2.0", "lhsFrac=-2", "rhsDouble=2.0")
  @Iteration("2!=-2.0", "lhsFrac=2", "rhsDouble=-2.0")
  @Iteration("-2/2!=-2.0", "lhsFrac=-2/2", "rhsDouble=-2.0")
  @Iteration("-1/3!=-0.333", "lhsFrac=-1/3", "rhsDouble=-0.333")
  @Iteration("-1 2/3!=-1.667", "lhsFrac=-1 2/3", "rhsDouble=-1.667")
  fun testIsApproximatelyEqualTo_oneIsFraction_firstIsDifferentDouble_returnsFalse() {
    val first = createRationalReal(lhsFrac)
    val second = createIrrationalReal(rhsDouble)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = first.isApproximatelyEqualTo(second)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  @Iteration("0.0==0.0", "lhsDouble=0.0", "rhsDouble=0.0")
  @Iteration("2.0==2.0", "lhsDouble=2.0", "rhsDouble=2.0")
  @Iteration(
    "2.000000000000001==1.999999999999999",
    "lhsDouble=2.000000000000001",
    "rhsDouble=1.999999999999999"
  )
  @Iteration("3.14==3.14", "lhsDouble=3.14", "rhsDouble=3.14")
  @Iteration("-2.0==-2.0", "lhsDouble=-2.0", "rhsDouble=-2.0")
  @Iteration(
    "-2.000000000000001==-1.999999999999999",
    "lhsDouble=-2.000000000000001",
    "rhsDouble=-1.999999999999999"
  )
  @Iteration("-3.14==-3.14", "lhsDouble=-3.14", "rhsDouble=-3.14")
  fun testIsApproximatelyEqualTo_oneIsDouble_otherIsSimilarDouble_returnsTrue() {
    val first = createIrrationalReal(lhsDouble)
    val second = createIrrationalReal(rhsDouble)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // Verify both correctness and the symmetric of equality.
    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  @Iteration("0.0!=2.0", "lhsDouble=0.0", "rhsDouble=2.0")
  @Iteration("2.001!=1.999", "lhsDouble=2.001", "rhsDouble=1.999")
  @Iteration("2.7!=3.14", "lhsDouble=2.7", "rhsDouble=3.14")
  @Iteration("2.7!=-3.14", "lhsDouble=2.7", "rhsDouble=-3.14")
  @Iteration("-2.7!=3.14", "lhsDouble=-2.7", "rhsDouble=3.14")
  @Iteration("-2.0!=2.0", "lhsDouble=-2.0", "rhsDouble=2.0")
  @Iteration("-3.14!=3.14", "lhsDouble=-3.14", "rhsDouble=3.14")
  fun testIsApproximatelyEqualTo_oneIsDouble_otherIsDifferentDouble_returnsFalse() {
    val first = createIrrationalReal(lhsDouble)
    val second = createIrrationalReal(rhsDouble)

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = first.isApproximatelyEqualTo(second)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_zeroAndOne_returnsFalse() {
    val result = ZERO_REAL.isApproximatelyEqualTo(1.0)

    assertThat(result).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_twoAndTwoWithinThreshold_returnsTrue() {
    val result = TWO_REAL.isApproximatelyEqualTo(2.0 + DOUBLE_EQUALITY_EPSILON / 2.0)

    assertThat(result).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_twoAndTwoOutsideThreshold_returnsFalse() {
    val result = TWO_REAL.isApproximatelyEqualTo(2.0 + DOUBLE_EQUALITY_EPSILON * 2.0)

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
  fun testIsApproximatelyZero_default_throwsException() {
    val defaultReal = Real.getDefaultInstance()

    val exception = assertThrows<IllegalStateException>() { defaultReal.isApproximatelyZero() }

    assertThat(exception).hasMessageThat().contains("Invalid real")
  }

  @Test
  fun testIsApproximatelyZero_zeroInteger_returnsTrue() {
    val result = ZERO_REAL.isApproximatelyZero()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsApproximatelyZero_twoInteger_returnsFalse() {
    val result = TWO_REAL.isApproximatelyZero()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsApproximatelyZero_zeroFraction_returnsTrue() {
    val real = createRationalReal(ZERO_FRACTION)

    val result = real.isApproximatelyZero()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsApproximatelyZero_negativeZeroFraction_returnsTrue() {
    val real = createRationalReal(-ZERO_FRACTION)

    val result = real.isApproximatelyZero()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsApproximatelyZero_oneHalfFraction_returnsFalse() {
    val result = ONE_HALF_REAL.isApproximatelyZero()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsApproximatelyZero_zeroIrrational_returnsTrue() {
    val real = createIrrationalReal(0.0)

    val result = real.isApproximatelyZero()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsApproximatelyZero_irrationalCloseToZero_returnsTrue() {
    val real = createIrrationalReal(0.00000000000000001)

    val result = real.isApproximatelyZero()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsApproximatelyZero_piIrrational_returnsFalse() {
    val result = PI_REAL.isApproximatelyZero()

    assertThat(result).isFalse()
  }

  @Test
  fun testToDouble_default_returnsZeroDouble() {
    val defaultReal = Real.getDefaultInstance()

    val exception = assertThrows<IllegalStateException>() { defaultReal.toDouble() }

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
  fun testAsWholeNumber_default_throwsException() {
    val defaultReal = Real.getDefaultInstance()

    val exception = assertThrows<IllegalStateException>() { defaultReal.asWholeNumber() }

    assertThat(exception).hasMessageThat().contains("Invalid real")
  }

  @Test
  fun testAsWholeNumber_twoInteger_returnsTwo() {
    val result = TWO_REAL.asWholeNumber()

    assertThat(result).isEqualTo(2)
  }

  @Test
  fun testAsWholeNumber_negativeTwoInteger_returnsNegativeTwo() {
    val result = NEGATIVE_TWO_REAL.asWholeNumber()

    assertThat(result).isEqualTo(-2)
  }

  @Test
  fun testAsWholeNumber_oneHalfFraction_returnsNull() {
    val result = ONE_HALF_REAL.asWholeNumber()

    assertThat(result).isNull()
  }

  @Test
  fun testAsWholeNumber_threeOnesFraction_returnsNull() {
    val result = THREE_ONES_REAL.asWholeNumber()

    // 3/1 is treated as a fraction despite being numerically equivalent to a whole number.
    assertThat(result).isNull()
  }

  @Test
  fun testAsWholeNumber_threeFraction_returnsThree() {
    val result = THREE_FRACTION_REAL.asWholeNumber()

    assertThat(result).isEqualTo(3)
  }

  @Test
  fun testAsWholeNumber_negativeThreeFraction_returnsNegativeThree() {
    val result = NEGATIVE_THREE_FRACTION_REAL.asWholeNumber()

    assertThat(result).isEqualTo(-3)
  }

  @Test
  fun testAsWholeNumber_piIrrational_returnsNull() {
    val result = PI_REAL.asWholeNumber()

    assertThat(result).isNull()
  }

  @Test
  fun testAsWholeNumber_threeIrrational_returnsNull() {
    val real = createIrrationalReal(3.0)

    val result = real.asWholeNumber()

    // Despite 3.0 being approximately a whole number, it isn't considered one since it's a double
    // (and thus can have precision loss).
    assertThat(result).isNull()
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
  fun testUnaryMinus_default_throwsException() {
    val defaultReal = Real.getDefaultInstance()

    val exception = assertThrows<IllegalStateException>() { -defaultReal }

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

  /*
   * Begin operator tests.
   *
   * Note that parameterized tests are used here to reduce the length of the overall test despite it
   * being not best practice (since each parameterized test is actually verifying multiple
   * behaviors).
   *
   * For a reference on the iteration names:
   * - 'identity' refers to an operator identity (i.e. a value which doesn't result in a change to
   *   the other operand of the operation)
   * - commutativity refers to verifying commutativity, e.g.: a+b=b+a or a*b=b*a
   * - noncommutativity refers to verifying that commutativity doesn't hold, e.g.: 2^3 != 3^2
   * - anticommutativity refers to verifying that commutativity is operationally reversed, e.g.:
   *     a-b=-(b-a) and a/b=1/(b/a).
   */

  // Addition tests.

  @Test
  @Iteration("identity+identity", "lhsInt=0", "rhsInt=0", "expInt=0")
  @Iteration("int+identity", "lhsInt=1", "rhsInt=0", "expInt=1")
  @Iteration("int+int", "lhsInt=1", "rhsInt=2", "expInt=3")
  @Iteration("commutativity", "lhsInt=2", "rhsInt=1", "expInt=3")
  @Iteration("int+-int", "lhsInt=1", "rhsInt=-2", "expInt=-1")
  @Iteration("-int+int", "lhsInt=-1", "rhsInt=2", "expInt=1")
  @Iteration("-int+-int", "lhsInt=-1", "rhsInt=-2", "expInt=-3")
  fun testPlus_intAndInt_returnsInt() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal + rhsReal

    assertThat(result).isIntegerThat().isEqualTo(expInt)
  }

  @Test
  @Iteration("identity+identity", "lhsInt=0", "rhsFrac=0/1", "expFrac=0/1")
  @Iteration("int+identity", "lhsInt=1", "rhsFrac=0/1", "expFrac=1")
  @Iteration("int+fraction", "lhsInt=2", "rhsFrac=1/3", "expFrac=2 1/3")
  @Iteration("int+wholeNumberFraction", "lhsInt=2", "rhsFrac=3/1", "expFrac=5")
  @Iteration("commutativity", "lhsInt=3", "rhsFrac=2/1", "expFrac=5")
  @Iteration("int+-fraction", "lhsInt=2", "rhsFrac=-1/3", "expFrac=1 2/3")
  @Iteration("-int+fraction", "lhsInt=-2", "rhsFrac=1/3", "expFrac=-1 2/3")
  @Iteration("-int+-fraction", "lhsInt=-2", "rhsFrac=-1/3", "expFrac=-2 1/3")
  fun testPlus_intAndFraction_returnsFraction() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal + rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity+identity", "lhsInt=0", "rhsDouble=0.0", "expDouble=0.0")
  @Iteration("int+identity", "lhsInt=1", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("int+double", "lhsInt=1", "rhsDouble=3.14", "expDouble=4.14")
  @Iteration("int+wholeNumberDouble", "lhsInt=1", "rhsDouble=3.0", "expDouble=4.0")
  @Iteration("commutativity", "lhsInt=3", "rhsDouble=1.0", "expDouble=4.0")
  @Iteration("int+-double", "lhsInt=1", "rhsDouble=-3.14", "expDouble=-2.14")
  @Iteration("-int+double", "lhsInt=-1", "rhsDouble=3.14", "expDouble=2.14")
  @Iteration("-int+-double", "lhsInt=-1", "rhsDouble=-3.14", "expDouble=-4.14")
  fun testPlus_intAndDouble_returnsDouble() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal + rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity+identity", "lhsFrac=0/1", "rhsInt=0", "expFrac=0/1")
  @Iteration("fraction+identity", "lhsFrac=1/1", "rhsInt=0", "expFrac=1")
  @Iteration("fraction+int", "lhsFrac=1/3", "rhsInt=2", "expFrac=2 1/3")
  @Iteration("wholeNumberFraction+int", "lhsFrac=3/1", "rhsInt=2", "expFrac=5")
  @Iteration("commutativity", "lhsFrac=2/1", "rhsInt=3", "expFrac=5")
  @Iteration("fraction+-int", "lhsFrac=1/3", "rhsInt=-2", "expFrac=-1 2/3")
  @Iteration("-fraction+int", "lhsFrac=-1/3", "rhsInt=2", "expFrac=1 2/3")
  @Iteration("-fraction+-int", "lhsFrac=-1/3", "rhsInt=-2", "expFrac=-2 1/3")
  fun testPlus_fractionAndInt_returnsFraction() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal + rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity+identity", "lhsFrac=0/1", "rhsFrac=0/1", "expFrac=0/1")
  @Iteration("fraction+identity", "lhsFrac=3/2", "rhsFrac=0/1", "expFrac=1 1/2")
  @Iteration("fraction+fraction", "lhsFrac=3/2", "rhsFrac=1/3", "expFrac=1 5/6")
  @Iteration("commutativity", "lhsFrac=1/3", "rhsFrac=3/2", "expFrac=1 5/6")
  @Iteration("fraction+-fraction", "lhsFrac=1/2", "rhsFrac=-1/3", "expFrac=1/6")
  @Iteration("-fraction+fraction", "lhsFrac=-1/2", "rhsFrac=1/3", "expFrac=-1/6")
  @Iteration("-fraction+-fraction", "lhsFrac=-1/2", "rhsFrac=-1/3", "expFrac=-5/6")
  fun testPlus_fractionAndFraction_returnsFraction() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal + rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity+identity", "lhsFrac=0/1", "rhsDouble=0.0", "expDouble=0.0")
  @Iteration("fraction+identity", "lhsFrac=3/2", "rhsDouble=0.0", "expDouble=1.5")
  @Iteration("fraction+double", "lhsFrac=3/2", "rhsDouble=3.14", "expDouble=4.64")
  @Iteration("wholeNumberFraction+double", "lhsFrac=3/1", "rhsDouble=2.0", "expDouble=5.0")
  @Iteration("commutativity", "lhsFrac=2/1", "rhsDouble=3.0", "expDouble=5.0")
  @Iteration("fraction+-double", "lhsFrac=3/2", "rhsDouble=-3.14", "expDouble=-1.64")
  @Iteration("-fraction+double", "lhsFrac=-3/2", "rhsDouble=3.14", "expDouble=1.64")
  @Iteration("-fraction+-double", "lhsFrac=-3/2", "rhsDouble=-3.14", "expDouble=-4.64")
  fun testPlus_fractionAndDouble_returnsDouble() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal + rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity+identity", "lhsDouble=0.0", "rhsInt=0", "expDouble=0.0")
  @Iteration("double+identity", "lhsDouble=1.0", "rhsInt=0", "expDouble=1.0")
  @Iteration("double+int", "lhsDouble=3.14", "rhsInt=1", "expDouble=4.14")
  @Iteration("wholeNumberDouble+int", "lhsDouble=3.0", "rhsInt=1", "expDouble=4.0")
  @Iteration("commutativity", "lhsDouble=1.0", "rhsInt=3", "expDouble=4.0")
  @Iteration("double+-int", "lhsDouble=3.14", "rhsInt=-1", "expDouble=2.14")
  @Iteration("-double+int", "lhsDouble=-3.14", "rhsInt=1", "expDouble=-2.14")
  @Iteration("-double+-int", "lhsDouble=-3.14", "rhsInt=-1", "expDouble=-4.14")
  fun testPlus_doubleAndInt_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal + rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity+identity", "lhsDouble=0.0", "rhsFrac=0/1", "expDouble=0.0")
  @Iteration("double+identity", "lhsDouble=3.14", "rhsFrac=0/1", "expDouble=3.14")
  @Iteration("double+fraction", "lhsDouble=3.14", "rhsFrac=3/2", "expDouble=4.64")
  @Iteration("double+wholeNumberFraction", "lhsDouble=2.0", "rhsFrac=3/1", "expDouble=5.0")
  @Iteration("commutativity", "lhsDouble=3.0", "rhsFrac=2/1", "expDouble=5.0")
  @Iteration("double+-fraction", "lhsDouble=3.14", "rhsFrac=-3/2", "expDouble=1.64")
  @Iteration("-double+fraction", "lhsDouble=-3.14", "rhsFrac=3/2", "expDouble=-1.64")
  @Iteration("-double+-fraction", "lhsDouble=-3.14", "rhsFrac=-3/2", "expDouble=-4.64")
  fun testPlus_doubleAndFraction_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal + rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity+identity", "lhsDouble=0.0", "rhsDouble=0.0", "expDouble=0.0")
  @Iteration("double+identity", "lhsDouble=1.0", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("double+double", "lhsDouble=3.14", "rhsDouble=2.7", "expDouble=5.84")
  @Iteration("commutativity", "lhsDouble=2.7", "rhsDouble=3.14", "expDouble=5.84")
  @Iteration("double+-double", "lhsDouble=3.14", "rhsDouble=-2.7", "expDouble=0.44")
  @Iteration("-double+double", "lhsDouble=-3.14", "rhsDouble=2.7", "expDouble=-0.44")
  @Iteration("-double+-double", "lhsDouble=-3.14", "rhsDouble=-2.7", "expDouble=-5.84")
  fun testPlus_doubleAndDouble_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal + rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  // Subtraction tests.

  @Test
  @Iteration("identity-identity", "lhsInt=0", "rhsInt=0", "expInt=0")
  @Iteration("int-identity", "lhsInt=1", "rhsInt=0", "expInt=1")
  @Iteration("int-int", "lhsInt=1", "rhsInt=2", "expInt=-1")
  @Iteration("anticommutativity", "lhsInt=2", "rhsInt=1", "expInt=1")
  @Iteration("int--int", "lhsInt=1", "rhsInt=-2", "expInt=3")
  @Iteration("-int-int", "lhsInt=-1", "rhsInt=2", "expInt=-3")
  @Iteration("-int--int", "lhsInt=-1", "rhsInt=-2", "expInt=1")
  fun testMinus_intAndInt_returnsInt() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal - rhsReal

    assertThat(result).isIntegerThat().isEqualTo(expInt)
  }

  @Test
  @Iteration("identity-identity", "lhsInt=0", "rhsFrac=0/1", "expFrac=0/1")
  @Iteration("int-identity", "lhsInt=1", "rhsFrac=0/1", "expFrac=1")
  @Iteration("int-fraction", "lhsInt=2", "rhsFrac=1/3", "expFrac=1 2/3")
  @Iteration("int-wholeNumberFraction", "lhsInt=2", "rhsFrac=3/1", "expFrac=-1")
  @Iteration("anticommutativity", "lhsInt=3", "rhsFrac=2/1", "expFrac=1")
  @Iteration("int--fraction", "lhsInt=2", "rhsFrac=-1/3", "expFrac=2 1/3")
  @Iteration("-int-fraction", "lhsInt=-2", "rhsFrac=1/3", "expFrac=-2 1/3")
  @Iteration("-int--fraction", "lhsInt=-2", "rhsFrac=-1/3", "expFrac=-1 2/3")
  fun testMinus_intAndFraction_returnsFraction() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal - rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity-identity", "lhsInt=0", "rhsDouble=0.0", "expDouble=0.0")
  @Iteration("int-identity", "lhsInt=1", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("int-double", "lhsInt=1", "rhsDouble=3.14", "expDouble=-2.14")
  @Iteration("int-wholeNumberDouble", "lhsInt=1", "rhsDouble=3.0", "expDouble=-2.0")
  @Iteration("anticommutativity", "lhsInt=3", "rhsDouble=1.0", "expDouble=2.0")
  @Iteration("int--double", "lhsInt=1", "rhsDouble=-3.14", "expDouble=4.14")
  @Iteration("-int-double", "lhsInt=-1", "rhsDouble=3.14", "expDouble=-4.14")
  @Iteration("-int--double", "lhsInt=-1", "rhsDouble=-3.14", "expDouble=2.14")
  fun testMinus_intAndDouble_returnsDouble() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal - rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity-identity", "lhsFrac=0/1", "rhsInt=0", "expFrac=0/1")
  @Iteration("fraction-identity", "lhsFrac=1/1", "rhsInt=0", "expFrac=1")
  @Iteration("fraction-int", "lhsFrac=1/3", "rhsInt=2", "expFrac=-1 2/3")
  @Iteration("wholeNumberFraction-int", "lhsFrac=3/1", "rhsInt=2", "expFrac=1")
  @Iteration("anticommutativity", "lhsFrac=2/1", "rhsInt=3", "expFrac=-1")
  @Iteration("fraction--int", "lhsFrac=1/3", "rhsInt=-2", "expFrac=2 1/3")
  @Iteration("-fraction-int", "lhsFrac=-1/3", "rhsInt=2", "expFrac=-2 1/3")
  @Iteration("-fraction--int", "lhsFrac=-1/3", "rhsInt=-2", "expFrac=1 2/3")
  fun testMinus_fractionAndInt_returnsFraction() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal - rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity-identity", "lhsFrac=0/1", "rhsFrac=0/1", "expFrac=0/1")
  @Iteration("fraction-identity", "lhsFrac=3/2", "rhsFrac=0/1", "expFrac=1 1/2")
  @Iteration("fraction-fraction", "lhsFrac=3/2", "rhsFrac=1/3", "expFrac=1 1/6")
  @Iteration("anticommutativity", "lhsFrac=1/3", "rhsFrac=3/2", "expFrac=-1 1/6")
  @Iteration("fraction--fraction", "lhsFrac=1/2", "rhsFrac=-1/3", "expFrac=5/6")
  @Iteration("-fraction-fraction", "lhsFrac=-1/2", "rhsFrac=1/3", "expFrac=-5/6")
  @Iteration("-fraction--fraction", "lhsFrac=-1/2", "rhsFrac=-1/3", "expFrac=-1/6")
  fun testMinus_fractionAndFraction_returnsFraction() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal - rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity-identity", "lhsFrac=0/1", "rhsDouble=0.0", "expDouble=0.0")
  @Iteration("fraction-identity", "lhsFrac=3/2", "rhsDouble=0.0", "expDouble=1.5")
  @Iteration("fraction-double", "lhsFrac=3/2", "rhsDouble=3.14", "expDouble=-1.64")
  @Iteration("wholeNumberFraction-double", "lhsFrac=3/1", "rhsDouble=2.0", "expDouble=1.0")
  @Iteration("anticommutativity", "lhsFrac=2/1", "rhsDouble=3.0", "expDouble=-1.0")
  @Iteration("fraction--double", "lhsFrac=3/2", "rhsDouble=-3.14", "expDouble=4.64")
  @Iteration("-fraction-double", "lhsFrac=-3/2", "rhsDouble=3.14", "expDouble=-4.64")
  @Iteration("-fraction--double", "lhsFrac=-3/2", "rhsDouble=-3.14", "expDouble=1.64")
  fun testMinus_fractionAndDouble_returnsDouble() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal - rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity-identity", "lhsDouble=0.0", "rhsInt=0", "expDouble=0.0")
  @Iteration("double-identity", "lhsDouble=1.0", "rhsInt=0", "expDouble=1.0")
  @Iteration("double-int", "lhsDouble=3.14", "rhsInt=1", "expDouble=2.14")
  @Iteration("wholeNumberDouble-int", "lhsDouble=3.0", "rhsInt=1", "expDouble=2.0")
  @Iteration("anticommutativity", "lhsDouble=1.0", "rhsInt=3", "expDouble=-2.0")
  @Iteration("double--int", "lhsDouble=3.14", "rhsInt=-1", "expDouble=4.14")
  @Iteration("-double-int", "lhsDouble=-3.14", "rhsInt=1", "expDouble=-4.14")
  @Iteration("-double--int", "lhsDouble=-3.14", "rhsInt=-1", "expDouble=-2.14")
  fun testMinus_doubleAndInt_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal - rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity-identity", "lhsDouble=0.0", "rhsFrac=0/1", "expDouble=0.0")
  @Iteration("double-identity", "lhsDouble=3.14", "rhsFrac=0/1", "expDouble=3.14")
  @Iteration("double-fraction", "lhsDouble=3.14", "rhsFrac=3/2", "expDouble=1.64")
  @Iteration("double-wholeNumberFraction", "lhsDouble=2.0", "rhsFrac=3/1", "expDouble=-1.0")
  @Iteration("anticommutativity", "lhsDouble=3.0", "rhsFrac=2/1", "expDouble=1.0")
  @Iteration("double--fraction", "lhsDouble=3.14", "rhsFrac=-3/2", "expDouble=4.64")
  @Iteration("-double-fraction", "lhsDouble=-3.14", "rhsFrac=3/2", "expDouble=-4.64")
  @Iteration("-double--fraction", "lhsDouble=-3.14", "rhsFrac=-3/2", "expDouble=-1.64")
  fun testMinus_doubleAndFraction_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal - rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity-identity", "lhsDouble=0.0", "rhsDouble=0.0", "expDouble=0.0")
  @Iteration("double-identity", "lhsDouble=1.0", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("double-double", "lhsDouble=3.14", "rhsDouble=2.7", "expDouble=0.44")
  @Iteration("anticommutativity", "lhsDouble=2.7", "rhsDouble=3.14", "expDouble=-0.44")
  @Iteration("double--double", "lhsDouble=3.14", "rhsDouble=-2.7", "expDouble=5.84")
  @Iteration("-double-double", "lhsDouble=-3.14", "rhsDouble=2.7", "expDouble=-5.84")
  @Iteration("-double--double", "lhsDouble=-3.14", "rhsDouble=-2.7", "expDouble=-0.44")
  fun testMinus_doubleAndDouble_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal - rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  // Multiplication tests.

  @Test
  @Iteration("identity*identity", "lhsInt=1", "rhsInt=1", "expInt=1")
  @Iteration("int*identity", "lhsInt=2", "rhsInt=1", "expInt=2")
  @Iteration("int*int", "lhsInt=3", "rhsInt=2", "expInt=6")
  @Iteration("commutativity", "lhsInt=2", "rhsInt=3", "expInt=6")
  @Iteration("int*-int", "lhsInt=3", "rhsInt=-2", "expInt=-6")
  @Iteration("-int*int", "lhsInt=-3", "rhsInt=2", "expInt=-6")
  @Iteration("-int*-int", "lhsInt=-3", "rhsInt=-2", "expInt=6")
  fun testTimes_intAndInt_returnsInt() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal * rhsReal

    assertThat(result).isIntegerThat().isEqualTo(expInt)
  }

  @Test
  @Iteration("identity*identity", "lhsInt=1", "rhsFrac=1", "expFrac=1")
  @Iteration("int*identity", "lhsInt=2", "rhsFrac=1", "expFrac=2")
  @Iteration("int*fraction", "lhsInt=2", "rhsFrac=1/3", "expFrac=2/3")
  @Iteration("int*wholeNumberFraction", "lhsInt=2", "rhsFrac=3/1", "expFrac=6")
  @Iteration("commutativity", "lhsInt=3", "rhsFrac=2/1", "expFrac=6")
  @Iteration("int*-fraction", "lhsInt=2", "rhsFrac=-1/3", "expFrac=-2/3")
  @Iteration("-int*fraction", "lhsInt=-2", "rhsFrac=1/3", "expFrac=-2/3")
  @Iteration("-int*-fraction", "lhsInt=-2", "rhsFrac=-1/3", "expFrac=2/3")
  fun testTimes_intAndFraction_returnsFraction() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal * rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity*identity", "lhsInt=1", "rhsDouble=1.0", "expDouble=1.0")
  @Iteration("int*identity", "lhsInt=2", "rhsDouble=1.0", "expDouble=2.0")
  @Iteration("int*double", "lhsInt=2", "rhsDouble=3.14", "expDouble=6.28")
  @Iteration("int*wholeNumberDouble", "lhsInt=2", "rhsDouble=3.0", "expDouble=6.0")
  @Iteration("commutativity", "lhsInt=3", "rhsDouble=2.0", "expDouble=6.0")
  @Iteration("int*-double", "lhsInt=2", "rhsDouble=-3.14", "expDouble=-6.28")
  @Iteration("-int*double", "lhsInt=-2", "rhsDouble=3.14", "expDouble=-6.28")
  @Iteration("-int*-double", "lhsInt=-2", "rhsDouble=-3.14", "expDouble=6.28")
  fun testTimes_intAndDouble_returnsDouble() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal * rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity*identity", "lhsFrac=1/1", "rhsInt=1", "expFrac=1")
  @Iteration("fraction*identity", "lhsFrac=2/1", "rhsInt=1", "expFrac=2")
  @Iteration("fraction*int", "lhsFrac=1/3", "rhsInt=2", "expFrac=2/3")
  @Iteration("wholeNumberFraction*int", "lhsFrac=3/1", "rhsInt=2", "expFrac=6")
  @Iteration("commutativity", "lhsFrac=2/1", "rhsInt=3", "expFrac=6")
  @Iteration("fraction*-int", "lhsFrac=1/3", "rhsInt=-2", "expFrac=-2/3")
  @Iteration("-fraction*int", "lhsFrac=-1/3", "rhsInt=2", "expFrac=-2/3")
  @Iteration("-fraction*-int", "lhsFrac=-1/3", "rhsInt=-2", "expFrac=2/3")
  fun testTimes_fractionAndInt_returnsFraction() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal * rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity*identity", "lhsFrac=1/1", "rhsFrac=1/1", "expFrac=1")
  @Iteration("fraction*identity", "lhsFrac=3/2", "rhsFrac=1/1", "expFrac=1 1/2")
  @Iteration("fraction*fraction", "lhsFrac=3/2", "rhsFrac=4/7", "expFrac=6/7")
  @Iteration("commutativity", "lhsFrac=4/7", "rhsFrac=3/2", "expFrac=6/7")
  @Iteration("fraction*-fraction", "lhsFrac=1 3/9", "rhsFrac=-8/11", "expFrac=-32/33")
  @Iteration("-fraction*fraction", "lhsFrac=-1 3/9", "rhsFrac=8/11", "expFrac=-32/33")
  @Iteration("-fraction*-fraction", "lhsFrac=-1 3/9", "rhsFrac=-8/11", "expFrac=32/33")
  fun testTimes_fractionAndFraction_returnsFraction() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal * rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity*identity", "lhsFrac=1/1", "rhsDouble=1.0", "expDouble=1.0")
  @Iteration("fraction*identity", "lhsFrac=3/2", "rhsDouble=1.0", "expDouble=1.5")
  @Iteration("fraction*double", "lhsFrac=3/2", "rhsDouble=3.14", "expDouble=4.71")
  @Iteration("wholeNumberFraction*double", "lhsFrac=3/1", "rhsDouble=2.0", "expDouble=6.0")
  @Iteration("commutativity", "lhsFrac=2/1", "rhsDouble=3.0", "expDouble=6.0")
  @Iteration("fraction*-double", "lhsFrac=1 3/2", "rhsDouble=-3.14", "expDouble=-7.85")
  @Iteration("-fraction*double", "lhsFrac=-1 3/2", "rhsDouble=3.14", "expDouble=-7.85")
  @Iteration("-fraction*-double", "lhsFrac=-1 3/2", "rhsDouble=-3.14", "expDouble=7.85")
  fun testTimes_fractionAndDouble_returnsDouble() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal * rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity*identity", "lhsDouble=1.0", "rhsInt=1", "expDouble=1.0")
  @Iteration("double*identity", "lhsDouble=2.0", "rhsInt=1", "expDouble=2.0")
  @Iteration("double*int", "lhsDouble=3.14", "rhsInt=2", "expDouble=6.28")
  @Iteration("wholeNumberDouble*int", "lhsDouble=3.0", "rhsInt=2", "expDouble=6")
  @Iteration("commutativity", "lhsDouble=2.0", "rhsInt=3", "expDouble=6.0")
  @Iteration("double*-int", "lhsDouble=3.14", "rhsInt=-2", "expDouble=-6.28")
  @Iteration("-double*int", "lhsDouble=-3.14", "rhsInt=2", "expDouble=-6.28")
  @Iteration("-double*-int", "lhsDouble=-3.14", "rhsInt=-2", "expDouble=6.28")
  fun testTimes_doubleAndInt_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal * rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity*identity", "lhsDouble=1.0", "rhsFrac=1/1", "expDouble=1.0")
  @Iteration("double*identity", "lhsDouble=2.0", "rhsFrac=1/1", "expDouble=2.0")
  @Iteration("double*fraction", "lhsDouble=3.14", "rhsFrac=3/2", "expDouble=4.71")
  @Iteration("double*wholeNumberFraction", "lhsDouble=2.0", "rhsFrac=3/1", "expDouble=6.0")
  @Iteration("commutativity", "lhsDouble=3.0", "rhsFrac=2/1", "expDouble=6.0")
  @Iteration("double*-fraction", "lhsDouble=3.14", "rhsFrac=-1 3/2", "expDouble=-7.85")
  @Iteration("-double*fraction", "lhsDouble=-3.14", "rhsFrac=1 3/2", "expDouble=-7.85")
  @Iteration("-double*-fraction", "lhsDouble=-3.14", "rhsFrac=-1 3/2", "expDouble=7.85")
  fun testTimes_doubleAndFraction_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal * rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity*identity", "lhsDouble=1.0", "rhsDouble=1.0", "expDouble=1.0")
  @Iteration("double*identity", "lhsDouble=2.0", "rhsDouble=1.0", "expDouble=2.0")
  @Iteration("double*double", "lhsDouble=3.14", "rhsDouble=2.7", "expDouble=8.478")
  @Iteration("commutativity", "lhsDouble=2.7", "rhsDouble=3.14", "expDouble=8.478")
  @Iteration("double*-double", "lhsDouble=3.14", "rhsDouble=-2.7", "expDouble=-8.478")
  @Iteration("-double*double", "lhsDouble=-3.14", "rhsDouble=2.7", "expDouble=-8.478")
  @Iteration("-double*-double", "lhsDouble=-3.14", "rhsDouble=-2.7", "expDouble=8.478")
  fun testTimes_doubleAndDouble_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal * rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  // Division tests.

  @Test
  @Iteration("identity/identity", "lhsInt=1", "rhsInt=1", "expInt=1")
  @Iteration("int/identity", "lhsInt=2", "rhsInt=1", "expInt=2")
  @Iteration("int/int", "lhsInt=8", "rhsInt=2", "expInt=4")
  @Iteration("int/-int", "lhsInt=8", "rhsInt=-2", "expInt=-4")
  @Iteration("-int/int", "lhsInt=-8", "rhsInt=2", "expInt=-4")
  @Iteration("-int/-int", "lhsInt=-8", "rhsInt=-2", "expInt=4")
  fun testDiv_intAndInt_divides_returnsInt() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal / rhsReal

    // If the divisor divides the dividend, the result is an integer.
    assertThat(result).isIntegerThat().isEqualTo(expInt)
  }

  @Test
  @Iteration("int/int", "lhsInt=7", "rhsInt=2", "expFrac=3 1/2")
  @Iteration("anticommutativity", "lhsInt=2", "rhsInt=7", "expFrac=2/7")
  @Iteration("int/-int", "lhsInt=7", "rhsInt=-2", "expFrac=-3 1/2")
  @Iteration("-int/int", "lhsInt=-7", "rhsInt=2", "expFrac=-3 1/2")
  @Iteration("-int/-int", "lhsInt=-7", "rhsInt=-2", "expFrac=3 1/2")
  fun testDiv_intAndInt_doesNotDivide_returnsFraction() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal / rhsReal

    // If the divisor doesn't divide the dividend, the result is a fraction.
    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity/identity", "lhsInt=1", "rhsFrac=1", "expFrac=1")
  @Iteration("int/identity", "lhsInt=2", "rhsFrac=1", "expFrac=2")
  @Iteration("int/fraction", "lhsInt=4", "rhsFrac=1/3", "expFrac=12")
  @Iteration("int/wholeNumberFraction", "lhsInt=2", "rhsFrac=3/1", "expFrac=2/3")
  @Iteration("anticommutativity", "lhsInt=3", "rhsFrac=2/1", "expFrac=1 1/2")
  @Iteration("int/-fraction", "lhsInt=5", "rhsFrac=-2/3", "expFrac=-7 1/2")
  @Iteration("-int/fraction", "lhsInt=-5", "rhsFrac=2/3", "expFrac=-7 1/2")
  @Iteration("-int/-fraction", "lhsInt=-5", "rhsFrac=-2/3", "expFrac=7 1/2")
  fun testDiv_intAndFraction_returnsFraction() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal / rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity/identity", "lhsInt=1", "rhsDouble=1.0", "expDouble=1.0")
  @Iteration("int/identity", "lhsInt=2", "rhsDouble=1.0", "expDouble=2.0")
  @Iteration("int/double", "lhsInt=2", "rhsDouble=3.14", "expDouble=0.636942675")
  @Iteration("int/wholeNumberDouble", "lhsInt=2", "rhsDouble=3.0", "expDouble=0.666666667")
  @Iteration("anticommutativity", "lhsInt=3", "rhsDouble=2.0", "expDouble=1.5")
  @Iteration("int/-double", "lhsInt=2", "rhsDouble=-3.14", "expDouble=-0.636942675")
  @Iteration("-int/double", "lhsInt=-2", "rhsDouble=3.14", "expDouble=-0.636942675")
  @Iteration("-int/-double", "lhsInt=-2", "rhsDouble=-3.14", "expDouble=0.636942675")
  fun testDiv_intAndDouble_returnsDouble() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal / rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity/identity", "lhsFrac=1/1", "rhsInt=1", "expFrac=1")
  @Iteration("fraction/identity", "lhsFrac=2/1", "rhsInt=1", "expFrac=2")
  @Iteration("fraction/int", "lhsFrac=1/3", "rhsInt=2", "expFrac=1/6")
  @Iteration("wholeNumberFraction/int", "lhsFrac=3/1", "rhsInt=2", "expFrac=1 1/2")
  @Iteration("anticommutativity", "lhsFrac=2/1", "rhsInt=3", "expFrac=2/3")
  @Iteration("fraction/-int", "lhsFrac=-1 1/3", "rhsInt=2", "expFrac=-2/3")
  @Iteration("-fraction/int", "lhsFrac=1 1/3", "rhsInt=-2", "expFrac=-2/3")
  @Iteration("-fraction/-int", "lhsFrac=-1 1/3", "rhsInt=-2", "expFrac=2/3")
  fun testDiv_fractionAndInt_returnsFraction() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal / rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity/identity", "lhsFrac=1/1", "rhsFrac=1/1", "expFrac=1")
  @Iteration("fraction/identity", "lhsFrac=3/2", "rhsFrac=1/1", "expFrac=1 1/2")
  @Iteration("fraction/fraction", "lhsFrac=3/2", "rhsFrac=4/7", "expFrac=2 5/8")
  @Iteration("anticommutativity", "lhsFrac=4/7", "rhsFrac=3/2", "expFrac=8/21")
  @Iteration("fraction/-fraction", "lhsFrac=1 3/9", "rhsFrac=-8/11", "expFrac=-1 5/6")
  @Iteration("-fraction/fraction", "lhsFrac=-1 3/9", "rhsFrac=8/11", "expFrac=-1 5/6")
  @Iteration("-fraction/-fraction", "lhsFrac=-1 3/9", "rhsFrac=-8/11", "expFrac=1 5/6")
  fun testDiv_fractionAndFraction_returnsFraction() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal / rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("identity/identity", "lhsFrac=1/1", "rhsDouble=1.0", "expDouble=1.0")
  @Iteration("fraction/identity", "lhsFrac=3/2", "rhsDouble=1.0", "expDouble=1.5")
  @Iteration("fraction/double", "lhsFrac=3/2", "rhsDouble=3.14", "expDouble=0.477707006")
  @Iteration("wholeNumberFraction/double", "lhsFrac=3/1", "rhsDouble=2.0", "expDouble=1.5")
  @Iteration("anticommutativity", "lhsFrac=2/1", "rhsDouble=3.0", "expDouble=0.666666667")
  @Iteration("fraction/-double", "lhsFrac=1 3/2", "rhsDouble=-3.14", "expDouble=-0.796178344")
  @Iteration("-fraction/double", "lhsFrac=-1 3/2", "rhsDouble=3.14", "expDouble=-0.796178344")
  @Iteration("-fraction/-double", "lhsFrac=-1 3/2", "rhsDouble=-3.14", "expDouble=0.796178344")
  fun testDiv_fractionAndDouble_returnsDouble() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal / rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity/identity", "lhsDouble=1.0", "rhsInt=1", "expDouble=1.0")
  @Iteration("double/identity", "lhsDouble=2.0", "rhsInt=1", "expDouble=2.0")
  @Iteration("double/int", "lhsDouble=3.14", "rhsInt=2", "expDouble=1.57")
  @Iteration("wholeNumberDouble/int", "lhsDouble=3.0", "rhsInt=2", "expDouble=1.5")
  @Iteration("anticommutativity", "lhsDouble=2.0", "rhsInt=3", "expDouble=0.666666667")
  @Iteration("double/-int", "lhsDouble=3.14", "rhsInt=-2", "expDouble=-1.57")
  @Iteration("-double/int", "lhsDouble=-3.14", "rhsInt=2", "expDouble=-1.57")
  @Iteration("-double/-int", "lhsDouble=-3.14", "rhsInt=-2", "expDouble=1.57")
  fun testDiv_doubleAndInt_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal / rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity/identity", "lhsDouble=1.0", "rhsFrac=1/1", "expDouble=1.0")
  @Iteration("double/identity", "lhsDouble=2.0", "rhsFrac=1/1", "expDouble=2.0")
  @Iteration("double/fraction", "lhsDouble=3.14", "rhsFrac=3/2", "expDouble=2.093333333")
  @Iteration("double/wholeNumberFraction", "lhsDouble=2.0", "rhsFrac=3/1", "expDouble=0.66666667")
  @Iteration("anticommutativity", "lhsDouble=3.0", "rhsFrac=2/1", "expDouble=1.5")
  @Iteration("double/-fraction", "lhsDouble=3.14", "rhsFrac=-1 3/2", "expDouble=-1.256")
  @Iteration("-double/fraction", "lhsDouble=-3.14", "rhsFrac=1 3/2", "expDouble=-1.256")
  @Iteration("-double/-fraction", "lhsDouble=-3.14", "rhsFrac=-1 3/2", "expDouble=1.256")
  fun testDiv_doubleAndFraction_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal / rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("identity/identity", "lhsDouble=1.0", "rhsDouble=1.0", "expDouble=1.0")
  @Iteration("double/identity", "lhsDouble=2.0", "rhsDouble=1.0", "expDouble=2.0")
  @Iteration("double/double", "lhsDouble=3.14", "rhsDouble=2.7", "expDouble=1.162962963")
  @Iteration("anticommutativity", "lhsDouble=2.7", "rhsDouble=3.14", "expDouble=0.859872611")
  @Iteration("double/-double", "lhsDouble=3.14", "rhsDouble=-2.7", "expDouble=-1.162962963")
  @Iteration("-double/double", "lhsDouble=-3.14", "rhsDouble=2.7", "expDouble=-1.162962963")
  @Iteration("-double/-double", "lhsDouble=-3.14", "rhsDouble=-2.7", "expDouble=1.162962963")
  fun testDiv_doubleAndDouble_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal / rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  fun testDiv_intDividedByZeroInt_throwsException() {
    val lhsReal = createIntegerReal(2)
    val rhsReal = createIntegerReal(0)

    assertThrows<ArithmeticException>() { lhsReal / rhsReal }
  }

  @Test
  fun testDiv_intDividedByZeroFraction_throwsException() {
    val lhsReal = createIntegerReal(2)
    val rhsReal = createRationalReal(ZERO_FRACTION)

    assertThrows<ArithmeticException>() { lhsReal / rhsReal }
  }

  @Test
  fun testDiv_intDividedByZeroDouble_returnsInfinityDouble() {
    val lhsReal = createIntegerReal(2)
    val rhsReal = createIrrationalReal(0.0)

    val result = lhsReal / rhsReal

    assertThat(result).isEqualTo(createIrrationalReal(Double.POSITIVE_INFINITY))
  }

  @Test
  fun testDiv_fractionDividedByZeroInt_throwsException() {
    val lhsReal = ONE_AND_ONE_HALF_REAL
    val rhsReal = createIntegerReal(0)

    assertThrows<ArithmeticException>() { lhsReal / rhsReal }
  }

  @Test
  fun testDiv_fractionDividedByZeroFraction_throwsException() {
    val lhsReal = ONE_AND_ONE_HALF_REAL
    val rhsReal = createRationalReal(ZERO_FRACTION)

    assertThrows<ArithmeticException>() { lhsReal / rhsReal }
  }

  @Test
  fun testDiv_fractionDividedByZeroDouble_returnsInfinityDouble() {
    val lhsReal = ONE_AND_ONE_HALF_REAL
    val rhsReal = createIrrationalReal(0.0)

    val result = lhsReal / rhsReal

    assertThat(result).isEqualTo(createIrrationalReal(Double.POSITIVE_INFINITY))
  }

  @Test
  fun testDiv_doubleDividedByZeroInt_returnsInfinityDouble() {
    val lhsReal = createIrrationalReal(3.14)
    val rhsReal = createIntegerReal(0)

    val result = lhsReal / rhsReal

    assertThat(result).isEqualTo(createIrrationalReal(Double.POSITIVE_INFINITY))
  }

  @Test
  fun testDiv_doubleDividedByZeroFraction_returnsInfinityDouble() {
    val lhsReal = createIrrationalReal(3.14)
    val rhsReal = createRationalReal(ZERO_FRACTION)

    val result = lhsReal / rhsReal

    assertThat(result).isEqualTo(createIrrationalReal(Double.POSITIVE_INFINITY))
  }

  @Test
  fun testDiv_doubleDividedByZeroDouble_returnsInfinityDouble() {
    val lhsReal = createIrrationalReal(3.14)
    val rhsReal = createIrrationalReal(0.0)

    val result = lhsReal / rhsReal

    assertThat(result).isEqualTo(createIrrationalReal(Double.POSITIVE_INFINITY))
  }

  // Exponentiation tests.

  @Test
  @Iteration("0^0", "lhsInt=0", "rhsInt=0", "expInt=1")
  @Iteration("identity^0", "lhsInt=1", "rhsInt=0", "expInt=1")
  @Iteration("identity^identity", "lhsInt=1", "rhsInt=1", "expInt=1")
  @Iteration("int^0", "lhsInt=2", "rhsInt=0", "expInt=1")
  @Iteration("int^identity", "lhsInt=2", "rhsInt=1", "expInt=2")
  @Iteration("int^int", "lhsInt=2", "rhsInt=3", "expInt=8")
  @Iteration("noncommutativity", "lhsInt=3", "rhsInt=2", "expInt=9")
  @Iteration("-int^even int", "lhsInt=-2", "rhsInt=4", "expInt=16")
  @Iteration("-int^odd int", "lhsInt=-2", "rhsInt=3", "expInt=-8")
  fun testPow_intAndInt_positivePower_returnsInt() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal pow rhsReal

    // Integer raised to positive (or zero) integers always results in another integer.
    assertThat(result).isIntegerThat().isEqualTo(expInt)
  }

  @Test
  @Iteration("int^-int", "lhsInt=2", "rhsInt=-3", "expFrac=1/8")
  @Iteration("-int^-even int", "lhsInt=-2", "rhsInt=-4", "expFrac=1/16")
  @Iteration("-int^-odd int", "lhsInt=-2", "rhsInt=-3", "expFrac=-1/8")
  fun testPow_intAndInt_negativePower_returnsFraction() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal pow rhsReal

    // Integers raised to a negative integer yields a fraction since x^-y=1/(x^y).
    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("0^0", "lhsInt=0", "rhsFrac=0/1", "expFrac=1")
  @Iteration("identity^0", "lhsInt=1", "rhsFrac=0/1", "expFrac=1")
  @Iteration("identity^identity", "lhsInt=1", "rhsFrac=1", "expFrac=1")
  @Iteration("int^0", "lhsInt=2", "rhsFrac=0/1", "expFrac=1")
  @Iteration("int^identity", "lhsInt=2", "rhsFrac=1", "expFrac=2")
  @Iteration("int^fraction", "lhsInt=16", "rhsFrac=3/2", "expFrac=64")
  @Iteration("int^wholeNumberFraction", "lhsInt=2", "rhsFrac=3/1", "expFrac=8")
  @Iteration("noncommutativity", "lhsInt=3", "rhsFrac=2/1", "expFrac=9")
  @Iteration("int^odd fraction", "lhsInt=8", "rhsFrac=5/3", "expFrac=32")
  @Iteration("int^-fraction", "lhsInt=8", "rhsFrac=-4/2", "expFrac=1/64")
  @Iteration("-int^odd fraction", "lhsInt=-8", "rhsFrac=5/3", "expFrac=-32")
  @Iteration("-int^-fraction", "lhsInt=-4", "rhsFrac=-4/2", "expFrac=1/16")
  @Iteration("-int^-odd fraction", "lhsInt=-8", "rhsFrac=-5/3", "expFrac=-1/32")
  fun testPow_intAndFraction_denominatorCanRootInt_returnsFraction() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal pow rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("int^fraction", "lhsInt=3", "rhsFrac=2/3", "expDouble=2.080083823")
  @Iteration("-int^fraction", "lhsInt=-4", "rhsFrac=2/3", "expDouble=2.5198421")
  @Iteration("int^-fraction", "lhsInt=2", "rhsFrac=-2/3", "expDouble=0.629960525")
  @Iteration("-int^-fraction", "lhsInt=-4", "rhsFrac=-2/3", "expDouble=0.396850263")
  fun testPow_intAndFraction_denominatorCannotRootInt_returnsDouble() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal pow rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("0^0", "lhsInt=0", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("identity^0", "lhsInt=1", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("identity^identity", "lhsInt=1", "rhsDouble=1.0", "expDouble=1.0")
  @Iteration("int^0", "lhsInt=2", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("int^identity", "lhsInt=2", "rhsDouble=1.0", "expDouble=2.0")
  @Iteration("int^double", "lhsInt=2", "rhsDouble=3.14", "expDouble=8.815240927")
  @Iteration("int^wholeNumberDouble", "lhsInt=2", "rhsDouble=3.0", "expDouble=8.0")
  @Iteration("noncommutativity", "lhsInt=3", "rhsDouble=2.0", "expDouble=9.0")
  @Iteration("int^-double", "lhsInt=2", "rhsDouble=-3.14", "expDouble=0.113439894")
  fun testPow_intAndDouble_returnsDouble() {
    val lhsReal = createIntegerReal(lhsInt)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal pow rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("0^0", "lhsFrac=0", "rhsInt=0", "expFrac=1")
  @Iteration("identity^0", "lhsFrac=1", "rhsInt=0", "expFrac=1")
  @Iteration("identity^identity", "lhsFrac=1", "rhsInt=1", "expFrac=1")
  @Iteration("fraction^0", "lhsFrac=1/3", "rhsInt=0", "expFrac=1")
  @Iteration("fraction^identity", "lhsFrac=1/3", "rhsInt=1", "expFrac=1/3")
  @Iteration("fraction^int", "lhsFrac=2/3", "rhsInt=3", "expFrac=8/27")
  @Iteration("wholeNumberFraction^int", "lhsFrac=3", "rhsInt=2", "expFrac=9")
  @Iteration("noncommutativity", "lhsFrac=2", "rhsInt=3", "expFrac=8")
  @Iteration("fraction^-int", "lhsFrac=4/3", "rhsInt=-2", "expFrac=9/16")
  @Iteration("-fraction^int", "lhsFrac=-4/3", "rhsInt=2", "expFrac=1 7/9")
  @Iteration("-fraction^-int", "lhsFrac=-4/3", "rhsInt=-2", "expFrac=9/16")
  fun testPow_fractionAndInt_returnsFraction() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal pow rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("0^0", "lhsFrac=0", "rhsFrac=0", "expFrac=1")
  @Iteration("identity^0", "lhsFrac=1", "rhsFrac=0", "expFrac=1")
  @Iteration("identity^identity", "lhsFrac=1", "rhsFrac=1", "expFrac=1")
  @Iteration("fraction^0", "lhsFrac=3/2", "rhsFrac=0", "expFrac=1")
  @Iteration("fraction^identity", "lhsFrac=3/2", "rhsFrac=1", "expFrac=1 1/2")
  @Iteration("fraction^fraction", "lhsFrac=32/243", "rhsFrac=3/5", "expFrac=8/27")
  @Iteration("fraction^wholeNumberFraction", "lhsFrac=3", "rhsFrac=2", "expFrac=9")
  @Iteration("noncommutativity", "lhsFrac=2", "rhsFrac=3", "expFrac=8")
  @Iteration("fraction^-fraction", "lhsFrac=32/243", "rhsFrac=-3/5", "expFrac=3 3/8")
  fun testPow_fractionAndFraction_denominatorCanRootFraction_returnsFraction() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal pow rhsReal

    val expectedFraction = fractionParser.parseFraction(expFrac)
    assertThat(result).isRationalThat().isEqualTo(expectedFraction)
  }

  @Test
  @Iteration("fraction^fraction", "lhsFrac=3/2", "rhsFrac=2/3", "expDouble=1.310370697")
  @Iteration("noncommutativity", "lhsFrac=2/3", "rhsFrac=3/2", "expDouble=0.544331054")
  @Iteration("fraction^-fraction", "lhsFrac=3/2", "rhsFrac=-2/3", "expDouble=0.763142828")
  fun testPow_fractionAndFraction_denominatorCannotRootFraction_returnsDouble() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal pow rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("0^0", "lhsFrac=0", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("identity^0", "lhsFrac=1", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("identity^identity", "lhsFrac=1", "rhsDouble=1.0", "expDouble=1.0")
  @Iteration("fraction^0", "lhsFrac=3/2", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("fraction^identity", "lhsFrac=3/2", "rhsDouble=1.0", "expDouble=1.5")
  @Iteration("fraction^double", "lhsFrac=3/2", "rhsDouble=3.14", "expDouble=3.572124224")
  @Iteration("wholeNumberFraction^double", "lhsFrac=3", "rhsDouble=2.0", "expDouble=9.0")
  @Iteration("noncommutativity", "lhsFrac=2", "rhsDouble=3.0", "expDouble=8.0")
  @Iteration("fraction^-double", "lhsFrac=1 3/2", "rhsDouble=-3.14", "expDouble=0.056294812")
  fun testPow_fractionAndDouble_returnsDouble() {
    val lhsReal = createRationalReal(lhsFrac)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal pow rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("0^0", "lhsDouble=0.0", "rhsInt=0", "expDouble=1.0")
  @Iteration("identity^0", "lhsDouble=1.0", "rhsInt=0", "expDouble=1.0")
  @Iteration("identity^identity", "lhsDouble=1.0", "rhsInt=1", "expDouble=1.0")
  @Iteration("double^0", "lhsDouble=3.14", "rhsInt=0", "expDouble=1.0")
  @Iteration("double^identity", "lhsDouble=3.14", "rhsInt=1", "expDouble=3.14")
  @Iteration("double^int", "lhsDouble=3.14", "rhsInt=2", "expDouble=9.8596")
  @Iteration("wholeNumberDouble^int", "lhsDouble=3.0", "rhsInt=2", "expDouble=9.0")
  @Iteration("noncommutativity", "lhsDouble=2.0", "rhsInt=3", "expDouble=8.0")
  @Iteration("double^-int", "lhsDouble=3.14", "rhsInt=-3", "expDouble=0.032300635")
  @Iteration("-double^int", "lhsDouble=-3.14", "rhsInt=3", "expDouble=-30.959144")
  @Iteration("-double^-int", "lhsDouble=-3.14", "rhsInt=-3", "expDouble=-0.032300635")
  fun testPow_doubleAndInt_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createIntegerReal(rhsInt)

    val result = lhsReal pow rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("0^0", "lhsDouble=0.0", "rhsFrac=0/1", "expDouble=1.0")
  @Iteration("identity^0", "lhsDouble=1.0", "rhsFrac=0/1", "expDouble=1.0")
  @Iteration("identity^identity", "lhsDouble=1.0", "rhsFrac=1", "expDouble=1.0")
  @Iteration("double^0", "lhsDouble=3.14", "rhsFrac=0/1", "expDouble=1.0")
  @Iteration("double^identity", "lhsDouble=3.14", "rhsFrac=1", "expDouble=3.14")
  @Iteration("double^fraction", "lhsDouble=3.14", "rhsFrac=3/2", "expDouble=5.564094176")
  @Iteration("double^wholeNumberFraction", "lhsDouble=2.0", "rhsFrac=3/1", "expDouble=8.0")
  @Iteration("noncommutativity", "lhsDouble=3.0", "rhsFrac=2/1", "expDouble=9.0")
  @Iteration("double^-fraction", "lhsDouble=3.14", "rhsFrac=-3/2", "expDouble=0.179723773")
  fun testPow_doubleAndFraction_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createRationalReal(rhsFrac)

    val result = lhsReal pow rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  @Iteration("0^0", "lhsDouble=0.0", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("identity^0", "lhsDouble=1.0", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("identity^identity", "lhsDouble=1.0", "rhsDouble=1.0", "expDouble=1.0")
  @Iteration("double^0", "lhsDouble=3.14", "rhsDouble=0.0", "expDouble=1.0")
  @Iteration("double^identity", "lhsDouble=3.14", "rhsDouble=1.0", "expDouble=3.14")
  @Iteration("double^double", "lhsDouble=3.14", "rhsDouble=2.7", "expDouble=21.963929943")
  @Iteration("noncommutativity", "lhsDouble=2.7", "rhsDouble=3.14", "expDouble=22.619459311")
  @Iteration("double^-double", "lhsDouble=3.14", "rhsDouble=-2.7", "expDouble=0.045529193")
  fun testPow_doubleAndDouble_returnsDouble() {
    val lhsReal = createIrrationalReal(lhsDouble)
    val rhsReal = createIrrationalReal(rhsDouble)

    val result = lhsReal pow rhsReal

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(expDouble)
  }

  @Test
  fun testPow_negativeIntToOneHalfFraction_returnsNull() {
    val lhsReal = createIntegerReal(-3)
    val rhsReal = createRationalReal(ONE_HALF_FRACTION)

    val result = lhsReal pow rhsReal

    // Cannot take the square root of a negative number.
    assertThat(result).isNull()
  }

  @Test
  fun testPow_negativeIntToNonzeroDouble_returnsNotANumber() {
    val lhsReal = createIntegerReal(-3)
    val rhsReal = createIrrationalReal(3.14)

    val result = lhsReal pow rhsReal

    assertThat(result).isIrrationalThat().isNaN()
  }

  @Test
  fun testPow_negativeFractionToOneHalfFraction_returnsNull() {
    val lhsReal = NEGATIVE_ONE_AND_ONE_HALF_REAL
    val rhsReal = createRationalReal(ONE_HALF_FRACTION)

    val result = lhsReal pow rhsReal

    // Cannot take the square root of a negative number.
    assertThat(result).isNull()
  }

  @Test
  fun testPow_negativeFractionToNegativeFractionWithOddNumerator_returnsNull() {
    val lhsReal = createRationalReal((-4).toWholeNumberFraction())
    val rhsReal = createRationalReal(-ONE_AND_ONE_HALF_FRACTION)

    val result = lhsReal pow rhsReal

    // Cannot take an even root of a negative number.
    assertThat(result).isNull()
  }

  @Test
  fun testPow_negativeFractionToNonzeroDouble_returnsNotANumber() {
    val lhsReal = NEGATIVE_ONE_AND_ONE_HALF_REAL
    val rhsReal = createIrrationalReal(3.14)

    val result = lhsReal pow rhsReal

    assertThat(result).isIrrationalThat().isNaN()
  }

  @Test
  fun testPow_negativeDoubleToOneHalfFraction_returnsNotANumber() {
    val lhsReal = createIrrationalReal(-2.7)
    val rhsReal = createRationalReal(ONE_HALF_FRACTION)

    val result = lhsReal pow rhsReal

    assertThat(result).isIrrationalThat().isNaN()
  }

  @Test
  fun testPow_negativeDoubleToNonzeroDouble_returnsNotANumber() {
    val lhsReal = createIrrationalReal(-2.7)
    val rhsReal = createIrrationalReal(3.14)

    val result = lhsReal pow rhsReal

    assertThat(result).isIrrationalThat().isNaN()
  }

  /* End operator tests. */

  @Test
  fun testSqrt_defaultReal_throwsException() {
    val real = Real.getDefaultInstance()

    val exception = assertThrows<IllegalStateException>() { sqrt(real) }

    assertThat(exception).hasMessageThat().contains("Invalid real")
  }

  @Test
  fun testSqrt_negativeInteger_returnsNull() {
    val real = createIntegerReal(-2)

    val result = sqrt(real)

    // Cannot take square root of a negative number.
    assertThat(result).isNull()
  }

  @Test
  fun testSqrt_zeroInteger_returnsZeroInteger() {
    val real = createIntegerReal(0)

    val result = sqrt(real)

    assertThat(result).isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testSqrt_fourInteger_returnsTwoInteger() {
    val real = createIntegerReal(4)

    val result = sqrt(real)

    assertThat(result).isIntegerThat().isEqualTo(2)
  }

  @Test
  fun testSqrt_fourTwo_returnsSqrtTwoDouble() {
    val real = createIntegerReal(2)

    val result = sqrt(real)

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(1.414213562)
  }

  @Test
  fun testSqrt_negativeFraction_returnsNull() {
    val real = createRationalReal((-2).toWholeNumberFraction())

    val result = sqrt(real)

    // Cannot take the square root of a negative number.
    assertThat(result).isNull()
  }

  @Test
  fun testSqrt_zeroFraction_returnZeroFraction() {
    val real = createRationalReal(ZERO_FRACTION)

    val result = sqrt(real)

    assertThat(result).isRationalThat().isEqualTo(ZERO_FRACTION)
  }

  @Test
  fun testSqrt_fourFraction_returnsTwoFraction() {
    val real = createRationalReal(4.toWholeNumberFraction())

    val result = sqrt(real)

    assertThat(result).isRationalThat().isEqualTo(2.toWholeNumberFraction())
  }

  @Test
  fun testSqrt_oneFourthFraction_returnsOneHalfFraction() {
    val real = createRationalReal(ONE_FOURTH_FRACTION)

    val result = sqrt(real)

    assertThat(result).isRationalThat().isEqualTo(ONE_HALF_FRACTION)
  }

  @Test
  fun testSqrt_sixteenthNinthsFraction_returnsOneAndOneThirdFraction() {
    val real = createRationalReal(createFraction(numerator = 16, denominator = 9))

    val result = sqrt(real)

    // Verify that both the numerator and denominator are properly rooted, and that a proper
    // fraction is returned.
    assertThat(result).isRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(1)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(3)
    }
  }

  @Test
  fun testSqrt_twoThirdsFraction_returnsComputedDouble() {
    val real = createRationalReal(createFraction(numerator = 2, denominator = 3))

    val result = sqrt(real)

    // sqrt(2/3) can't be computed perfectly, so a double must be computed, instead.
    assertThat(result).isIrrationalThat().isWithin(1e-5).of(0.816496581)
  }

  @Test
  fun testSqrt_negativeDouble_returnsNotANumber() {
    val real = createIrrationalReal(-2.7)

    val result = sqrt(real)

    assertThat(result).isIrrationalThat().isNaN()
  }

  @Test
  fun testSqrt_zeroDouble_returnsZeroDouble() {
    val real = createIrrationalReal(0.0)

    val result = sqrt(real)

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(0.0)
  }

  @Test
  fun testSqrt_fourDouble_returnsTwoDouble() {
    val real = createIrrationalReal(4.0)

    val result = sqrt(real)

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(2.0)
  }

  @Test
  fun testSqrt_twoDouble_returnsRootTwoDouble() {
    val real = createIrrationalReal(2.0)

    val result = sqrt(real)

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(1.414213562)
  }

  @Test
  fun testSqrt_nonWholeDouble_returnsCorrectSquareRootDouble() {
    val real = createIrrationalReal(3.14)

    val result = sqrt(real)

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(1.772004515)
  }

  /*
   * Tests for REAL_COMPARATOR.
   *
   * Note that these specifically don't try to compare against negative doubles since the comparison
   * logic is a bit unexpected (see https://stackoverflow.com/a/45544483).
   */

  @Test
  @Iteration("0==0", "lhsInt=0", "rhsInt=0", "expInt=0")
  @Iteration("-2<0", "lhsInt=-2", "rhsInt=0", "expInt=-1")
  @Iteration("-5<-2", "lhsInt=-5", "rhsInt=-2", "expInt=-1")
  @Iteration("-2>-5", "lhsInt=-2", "rhsInt=-5", "expInt=1")
  @Iteration("2>0", "lhsInt=2", "rhsInt=0", "expInt=1")
  @Iteration("5>2", "lhsInt=5", "rhsInt=2", "expInt=1")
  @Iteration("2<5", "lhsInt=2", "rhsInt=5", "expInt=-1")
  @Iteration("-2<5", "lhsInt=-2", "rhsInt=5", "expInt=-1")
  @Iteration("5>-2", "lhsInt=5", "rhsInt=-2", "expInt=1")
  fun testComparator_intAndInt_returnsCorrectComparisonInt() {
    val lhsValue = createIntegerReal(lhsInt)
    val rhsValue = createIntegerReal(rhsInt)

    val comparison = REAL_COMPARATOR.compare(lhsValue, rhsValue)

    assertThat(comparison).isEqualTo(expInt)
  }

  @Test
  @Iteration("0==0", "lhsInt=0", "rhsFrac=0", "expInt=0")
  @Iteration("-2<0", "lhsInt=-2", "rhsFrac=0", "expInt=-1")
  @Iteration("-5<-2", "lhsInt=-5", "rhsFrac=-2", "expInt=-1")
  @Iteration("-5<-1/2", "lhsInt=-5", "rhsFrac=-1/2", "expInt=-1")
  @Iteration("-2>-5", "lhsInt=-2", "rhsFrac=-5", "expInt=1")
  @Iteration("-1>-3/2", "lhsInt=-1", "rhsFrac=-3/2", "expInt=1")
  @Iteration("2>0", "lhsInt=2", "rhsFrac=0", "expInt=1")
  @Iteration("5>2", "lhsInt=5", "rhsFrac=2", "expInt=1")
  @Iteration("2<5", "lhsInt=2", "rhsFrac=5", "expInt=-1")
  @Iteration("2<7/2", "lhsInt=2", "rhsFrac=7/2", "expInt=-1")
  @Iteration("5>3/2", "lhsInt=5", "rhsFrac=3/2", "expInt=1")
  @Iteration("-2<5", "lhsInt=-2", "rhsFrac=5", "expInt=-1")
  @Iteration("-2<3/2", "lhsInt=-2", "rhsFrac=3/2", "expInt=-1")
  @Iteration("5>-2", "lhsInt=5", "rhsFrac=-2", "expInt=1")
  @Iteration("5>-3/2", "lhsInt=5", "rhsFrac=-3/2", "expInt=1")
  fun testComparator_intAndFraction_returnsCorrectComparisonInt() {
    val lhsValue = createIntegerReal(lhsInt)
    val rhsValue = createRationalReal(rhsFrac)

    val comparison = REAL_COMPARATOR.compare(lhsValue, rhsValue)

    assertThat(comparison).isEqualTo(expInt)
  }

  @Test
  @Iteration("0==0", "lhsInt=0", "rhsDouble=0.0", "expInt=0")
  @Iteration("-2<0", "lhsInt=-2", "rhsDouble=0.0", "expInt=-1")
  @Iteration("-5<-3.14", "lhsInt=-5", "rhsDouble=-3.14", "expInt=-1")
  @Iteration("-2>-6.28", "lhsInt=-2", "rhsDouble=-6.28", "expInt=1")
  @Iteration("2>0", "lhsInt=2", "rhsDouble=0.0", "expInt=1")
  @Iteration("5>3.14", "lhsInt=5", "rhsDouble=3.14", "expInt=1")
  @Iteration("2<6.28", "lhsInt=2", "rhsDouble=6.28", "expInt=-1")
  @Iteration("-2<3.14", "lhsInt=-2", "rhsDouble=3.14", "expInt=-1")
  @Iteration("2>-3.14", "lhsInt=2", "rhsDouble=-3.14", "expInt=1")
  fun testComparator_intAndDouble_returnsCorrectComparisonInt() {
    val lhsValue = createIntegerReal(lhsInt)
    val rhsValue = createIrrationalReal(rhsDouble)

    val comparison = REAL_COMPARATOR.compare(lhsValue, rhsValue)

    assertThat(comparison).isEqualTo(expInt)
  }

  @Test
  @Iteration("0==0", "lhsFrac=0", "rhsInt=0", "expInt=0")
  @Iteration("-3/2<0", "lhsFrac=-3/2", "rhsInt=0", "expInt=-1")
  @Iteration("-7/2<-3", "lhsFrac=-7/2", "rhsInt=-3", "expInt=-1")
  @Iteration("-3/2>-5", "lhsFrac=-3/2", "rhsInt=-5", "expInt=1")
  @Iteration("3/2>0", "lhsFrac=3/2", "rhsInt=0", "expInt=1")
  @Iteration("7/2>3", "lhsFrac=7/2", "rhsInt=3", "expInt=1")
  @Iteration("3/2<5", "lhsFrac=3/2", "rhsInt=5", "expInt=-1")
  @Iteration("-3/2<3", "lhsFrac=-3/2", "rhsInt=3", "expInt=-1")
  @Iteration("3/2>-3", "lhsFrac=3/2", "rhsInt=-3", "expInt=1")
  fun testComparator_fractionAndInt_returnsCorrectComparisonInt() {
    val lhsValue = createRationalReal(lhsFrac)
    val rhsValue = createIntegerReal(rhsInt)

    val comparison = REAL_COMPARATOR.compare(lhsValue, rhsValue)

    assertThat(comparison).isEqualTo(expInt)
  }

  @Test
  @Iteration("0==0", "lhsFrac=0", "rhsFrac=0", "expInt=0")
  @Iteration("-3/2<0", "lhsFrac=-3/2", "rhsFrac=0", "expInt=-1")
  @Iteration("-7/2<-3/2", "lhsFrac=-7/2", "rhsFrac=-3/2", "expInt=-1")
  @Iteration("3/2>0", "lhsFrac=3/2", "rhsFrac=0", "expInt=1")
  @Iteration("7/2>3/2", "lhsFrac=7/2", "rhsFrac=3/2", "expInt=1")
  @Iteration("3/2<7/2", "lhsFrac=3/2", "rhsFrac=7/2", "expInt=-1")
  @Iteration("-3/2<3/2", "lhsFrac=-3/2", "rhsFrac=3/2", "expInt=-1")
  @Iteration("3/2>-3/2", "lhsFrac=3/2", "rhsFrac=-3/2", "expInt=1")
  fun testComparator_fractionAndFraction_returnsCorrectComparisonInt() {
    val lhsValue = createRationalReal(lhsFrac)
    val rhsValue = createRationalReal(rhsFrac)

    val comparison = REAL_COMPARATOR.compare(lhsValue, rhsValue)

    assertThat(comparison).isEqualTo(expInt)
  }

  @Test
  @Iteration("0==0", "lhsFrac=0", "rhsDouble=0.0", "expInt=0")
  @Iteration("-3/2<0", "lhsFrac=-3/2", "rhsDouble=0.0", "expInt=-1")
  @Iteration("-7/2<-3.14", "lhsFrac=-7/2", "rhsDouble=-3.14", "expInt=-1")
  @Iteration("3/2>0", "lhsFrac=3/2", "rhsDouble=0.0", "expInt=1")
  @Iteration("7/2>3.14", "lhsFrac=7/2", "rhsDouble=3.14", "expInt=1")
  @Iteration("3/2<3.14", "lhsFrac=3/2", "rhsDouble=3.14", "expInt=-1")
  @Iteration("-3/2<3.14", "lhsFrac=-3/2", "rhsDouble=3.14", "expInt=-1")
  @Iteration("3/2>-3.14", "lhsFrac=3/2", "rhsDouble=-3.14", "expInt=1")
  fun testComparator_fractionAndDouble_returnsCorrectComparisonInt() {
    val lhsValue = createRationalReal(lhsFrac)
    val rhsValue = createIrrationalReal(rhsDouble)

    val comparison = REAL_COMPARATOR.compare(lhsValue, rhsValue)

    assertThat(comparison).isEqualTo(expInt)
  }

  @Test
  @Iteration("0==0", "lhsDouble=0.0", "rhsInt=0", "expInt=0")
  @Iteration("-3.14<0", "lhsDouble=-3.14", "rhsInt=0", "expInt=-1")
  @Iteration("-6.28<-4", "lhsDouble=-6.28", "rhsInt=-4", "expInt=-1")
  @Iteration("3.14>0", "lhsDouble=3.14", "rhsInt=0", "expInt=1")
  @Iteration("6.28>4", "lhsDouble=6.28", "rhsInt=4", "expInt=1")
  @Iteration("3.14<4", "lhsDouble=3.14", "rhsInt=4", "expInt=-1")
  @Iteration("-3.14<4", "lhsDouble=-3.14", "rhsInt=4", "expInt=-1")
  @Iteration("3.14>-4", "lhsDouble=3.14", "rhsInt=-4", "expInt=1")
  fun testComparator_doubleAndInt_returnsCorrectComparisonInt() {
    val lhsValue = createIrrationalReal(lhsDouble)
    val rhsValue = createIntegerReal(rhsInt)

    val comparison = REAL_COMPARATOR.compare(lhsValue, rhsValue)

    assertThat(comparison).isEqualTo(expInt)
  }

  @Test
  @Iteration("0==0", "lhsDouble=0.0", "rhsFrac=0", "expInt=0")
  @Iteration("-3.14<0", "lhsDouble=-3.14", "rhsFrac=0", "expInt=-1")
  @Iteration("-6.28<-7/2", "lhsDouble=-6.28", "rhsFrac=-7/2", "expInt=-1")
  @Iteration("3.14>0", "lhsDouble=3.14", "rhsFrac=0", "expInt=1")
  @Iteration("6.28>7/2", "lhsDouble=6.28", "rhsFrac=7/2", "expInt=1")
  @Iteration("3.14<7/2", "lhsDouble=3.14", "rhsFrac=7/2", "expInt=-1")
  @Iteration("-3.14<7/2", "lhsDouble=-3.14", "rhsFrac=7/2", "expInt=-1")
  @Iteration("3.14>-7/2", "lhsDouble=3.14", "rhsFrac=-7/2", "expInt=1")
  fun testComparator_doubleAndFraction_returnsCorrectComparisonInt() {
    val lhsValue = createIrrationalReal(lhsDouble)
    val rhsValue = createRationalReal(rhsFrac)

    val comparison = REAL_COMPARATOR.compare(lhsValue, rhsValue)

    assertThat(comparison).isEqualTo(expInt)
  }

  @Test
  @Iteration("0==0", "lhsDouble=0.0", "rhsDouble=0.0", "expInt=0")
  @Iteration("-3.14<0", "lhsDouble=-3.14", "rhsDouble=0.0", "expInt=-1")
  @Iteration("-6.28<-3.14", "lhsDouble=-6.28", "rhsDouble=-3.14", "expInt=-1")
  @Iteration("3.14>0", "lhsDouble=3.14", "rhsDouble=0.0", "expInt=1")
  @Iteration("6.28>3.14", "lhsDouble=6.28", "rhsDouble=3.14", "expInt=1")
  @Iteration("3.14<6.28", "lhsDouble=3.14", "rhsDouble=6.28", "expInt=-1")
  @Iteration("-3.14<6.28", "lhsDouble=-3.14", "rhsDouble=6.28", "expInt=-1")
  @Iteration("3.14>-6.28", "lhsDouble=3.14", "rhsDouble=-6.28", "expInt=1")
  fun testComparator_doubleAndDouble_returnsCorrectComparisonInt() {
    val lhsValue = createIrrationalReal(lhsDouble)
    val rhsValue = createIrrationalReal(rhsDouble)

    val comparison = REAL_COMPARATOR.compare(lhsValue, rhsValue)

    assertThat(comparison).isEqualTo(expInt)
  }

  private fun createRationalReal(rawFractionExpression: String) =
    createRationalReal(fractionParser.parseFractionFromString(rawFractionExpression))
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

private fun createFraction(numerator: Int, denominator: Int) = Fraction.newBuilder().apply {
  this.numerator = numerator
  this.denominator = denominator
}.build()
