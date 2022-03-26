package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Polynomial
import org.oppia.android.app.model.Polynomial.Term
import org.oppia.android.app.model.Polynomial.Term.Variable
import org.oppia.android.app.model.Real
import org.oppia.android.testing.math.RealSubject.Companion.assertThat
import org.robolectric.annotation.LooperMode

/** Tests for [Polynomial] extensions. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class PolynomialExtensionsTest {
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

    private val ZERO_REAL = Real.newBuilder().apply {
      integer = 0
    }.build()

    private val ONE_REAL = Real.newBuilder().apply {
      integer = 1
    }.build()

    private val TWO_REAL = Real.newBuilder().apply {
      integer = 2
    }.build()

    private val ONE_HALF_REAL = Real.newBuilder().apply {
      rational = ONE_HALF_FRACTION
    }.build()

    private val ONE_AND_ONE_HALF_REAL = Real.newBuilder().apply {
      rational = ONE_AND_ONE_HALF_FRACTION
    }.build()

    private val PI_REAL = Real.newBuilder().apply {
      irrational = PI
    }.build()

    private val ZERO_POLYNOMIAL = createPolynomial(createTerm(coefficient = ZERO_REAL))

    private val TWO_POLYNOMIAL = createPolynomial(createTerm(coefficient = TWO_REAL))

    private val NEGATIVE_TWO_POLYNOMIAL = createPolynomial(createTerm(coefficient = -TWO_REAL))

    private val ONE_HALF_POLYNOMIAL = createPolynomial(createTerm(coefficient = ONE_HALF_REAL))

    private val NEGATIVE_ONE_HALF_POLYNOMIAL =
      createPolynomial(createTerm(coefficient = -ONE_HALF_REAL))

    private val ONE_AND_ONE_HALF_POLYNOMIAL =
      createPolynomial(createTerm(coefficient = ONE_AND_ONE_HALF_REAL))

    private val NEGATIVE_ONE_AND_ONE_HALF_POLYNOMIAL =
      createPolynomial(createTerm(coefficient = -ONE_AND_ONE_HALF_REAL))

    private val PI_POLYNOMIAL = createPolynomial(createTerm(coefficient = PI_REAL))

    private val NEGATIVE_PI_POLYNOMIAL = createPolynomial(createTerm(coefficient = -PI_REAL))

    private val ONE_X_POLYNOMIAL =
      createPolynomial(createTerm(coefficient = ONE_REAL, createVariable(name = "x", power = 1)))

    private val NEGATIVE_ONE_X_POLYNOMIAL =
      createPolynomial(createTerm(coefficient = -ONE_REAL, createVariable(name = "x", power = 1)))

    private val TWO_X_POLYNOMIAL =
      createPolynomial(createTerm(coefficient = TWO_REAL, createVariable(name = "x", power = 1)))

    private val ONE_PLUS_X_POLYNOMIAL =
      createPolynomial(
        createTerm(coefficient = ONE_REAL),
        createTerm(coefficient = ONE_REAL, createVariable(name = "x", power = 1))
      )
  }

  @Test
  fun testIsConstant_default_returnsFalse() {
    val defaultPolynomial = Polynomial.getDefaultInstance()

    val result = defaultPolynomial.isConstant()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsConstant_zero_returnsTrue() {
    val result = ZERO_POLYNOMIAL.isConstant()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsConstant_two_returnsTrue() {
    val result = TWO_POLYNOMIAL.isConstant()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsConstant_negativeTwo_returnsTrue() {
    val result = NEGATIVE_TWO_POLYNOMIAL.isConstant()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsConstant_oneHalf_returnsTrue() {
    val result = ONE_HALF_POLYNOMIAL.isConstant()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsConstant_negativeOneHalf_returnsTrue() {
    val result = NEGATIVE_ONE_HALF_POLYNOMIAL.isConstant()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsConstant_pi_returnsTrue() {
    val result = PI_POLYNOMIAL.isConstant()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsConstant_negativePi_returnsTrue() {
    val result = NEGATIVE_PI_POLYNOMIAL.isConstant()

    assertThat(result).isTrue()
  }

  @Test
  fun testIsConstant_x_returnsFalse() {
    val result = ONE_X_POLYNOMIAL.isConstant()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsConstant_2x_returnsFalse() {
    val result = TWO_X_POLYNOMIAL.isConstant()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsConstant_one_and_x_returnsFalse() {
    val result = ONE_PLUS_X_POLYNOMIAL.isConstant()

    assertThat(result).isFalse()
  }

  @Test
  fun testIsConstant_one_and_two_returnsFalse() {
    val onePlusTwoPolynomial =
      createPolynomial(createTerm(coefficient = ONE_REAL), createTerm(coefficient = TWO_REAL))

    val result = onePlusTwoPolynomial.isConstant()

    // While 1+2 is effectively a constant polynomial, it's not actually simplified and thus isn't
    // considered a constant polynomial.
    assertThat(result).isFalse()
  }

  @Test
  fun testGetConstant_zero_returnsZero() {
    val result = ZERO_POLYNOMIAL.getConstant()

    assertThat(result).isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testGetConstant_two_returnsTwo() {
    val result = TWO_POLYNOMIAL.getConstant()

    assertThat(result).isIntegerThat().isEqualTo(2)
  }

  @Test
  fun testGetConstant_negativeTwo_returnsNegativeTwo() {
    val result = NEGATIVE_TWO_POLYNOMIAL.getConstant()

    assertThat(result).isIntegerThat().isEqualTo(-2)
  }

  @Test
  fun testGetConstant_oneHalf_returnsOneHalf() {
    val result = ONE_HALF_POLYNOMIAL.getConstant()

    assertThat(result).isRationalThat().evaluatesToDoubleThat().isWithin(1e-5).of(0.5)
  }

  @Test
  fun testGetConstant_negativeOneHalf_returnsNegativeOneHalf() {
    val result = NEGATIVE_ONE_HALF_POLYNOMIAL.getConstant()

    assertThat(result).isRationalThat().evaluatesToDoubleThat().isWithin(1e-5).of(-0.5)
  }

  @Test
  fun testGetConstant_pi_returnsPi() {
    val result = PI_POLYNOMIAL.getConstant()

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(PI)
  }

  @Test
  fun testGetConstant_negativePi_returnsNegativePi() {
    val result = NEGATIVE_PI_POLYNOMIAL.getConstant()

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(-PI)
  }

  @Test
  fun testToPlainText_zero_returnsZeroString() {
    val result = ZERO_POLYNOMIAL.toPlainText()

    assertThat(result).isEqualTo("0")
  }

  @Test
  fun testToPlainText_two_returnsTwoString() {
    val result = TWO_POLYNOMIAL.toPlainText()

    assertThat(result).isEqualTo("2")
  }

  @Test
  fun testToPlainText_negativeTwo_returnsMinusTwoString() {
    val result = NEGATIVE_TWO_POLYNOMIAL.toPlainText()

    assertThat(result).isEqualTo("-2")
  }

  @Test
  fun testToPlainText_oneAndOneHalf_returnsThreeHalvesString() {
    val result = ONE_AND_ONE_HALF_POLYNOMIAL.toPlainText()

    assertThat(result).isEqualTo("3/2")
  }

  @Test
  fun testToPlainText_negativeOneAndOneHalf_returnsMinusThreeHalvesString() {
    val result = NEGATIVE_ONE_AND_ONE_HALF_POLYNOMIAL.toPlainText()

    assertThat(result).isEqualTo("-3/2")
  }

  @Test
  fun testToPlainText_pi_returnsPiString() {
    val result = PI_POLYNOMIAL.toPlainText()

    assertThat(result).isEqualTo("3.1415")
  }

  @Test
  fun testToPlainText_negativePi_returnsMinusPiString() {
    val result = NEGATIVE_PI_POLYNOMIAL.toPlainText()

    assertThat(result).isEqualTo("-3.1415")
  }

  @Test
  fun testToPlainText_2x_returns2XString() {
    val result = TWO_X_POLYNOMIAL.toPlainText()

    assertThat(result).isEqualTo("2x")
  }

  @Test
  fun testToPlainText_1x_returnsXString() {
    val result = ONE_X_POLYNOMIAL.toPlainText()

    assertThat(result).isEqualTo("x")
  }

  @Test
  fun testToPlainText_negativeX_returnsMinusXString() {
    val result = NEGATIVE_ONE_X_POLYNOMIAL.toPlainText()

    assertThat(result).isEqualTo("-x")
  }

  @Test
  fun testToPlainText_oneAndX_returnsOnePlusXString() {
    val result = ONE_PLUS_X_POLYNOMIAL.toPlainText()

    assertThat(result).isEqualTo("1 + x")
  }

  @Test
  fun testToPlainText_oneAndNegativeX_returnsOneMinusXString() {
    val oneMinusXPolynomial = createPolynomial(
      createTerm(coefficient = ONE_REAL),
      createTerm(coefficient = -ONE_REAL, createVariable(name = "x", power = 1))
    )

    val result = oneMinusXPolynomial.toPlainText()

    assertThat(result).isEqualTo("1 - x")
  }

  @Test
  fun testToPlainText_oneAndOneHalfXAndY_returnsThreeHalvesXPlusYString() {
    val oneMinusXPolynomial = createPolynomial(
      createTerm(coefficient = ONE_AND_ONE_HALF_REAL, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE_REAL, createVariable(name = "y", power = 1))
    )

    val result = oneMinusXPolynomial.toPlainText()

    assertThat(result).isEqualTo("(3/2)x + y")
  }

  @Test
  fun testToPlainText_oneAndXAndXSquared_returnsOnePlusXPlusXSquaredString() {
    val oneMinusXPolynomial = createPolynomial(
      createTerm(coefficient = ONE_REAL),
      createTerm(coefficient = ONE_REAL, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE_REAL, createVariable(name = "x", power = 2))
    )

    val result = oneMinusXPolynomial.toPlainText()

    assertThat(result).isEqualTo("1 + x + x^2")
  }

  @Test
  fun testToPlainText_xSquaredAndXAndOne_returnsXSquaredPlusXPlusOneString() {
    val oneMinusXPolynomial = createPolynomial(
      createTerm(coefficient = ONE_REAL, createVariable(name = "x", power = 2)),
      createTerm(coefficient = ONE_REAL, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE_REAL)
    )

    val result = oneMinusXPolynomial.toPlainText()

    // Compared with the test above, this shows that term order matters for string conversion.
    assertThat(result).isEqualTo("x^2 + x + 1")
  }

  @Test
  fun testToPlainText_xSquaredYCubedAndOne_returnsXSquaredYCubedPlusOneString() {
    val oneMinusXPolynomial = createPolynomial(
      createTerm(coefficient = ONE_REAL, createVariable(name = "x", power = 2)),
      createTerm(coefficient = ONE_REAL, createVariable(name = "y", power = 3)),
      createTerm(coefficient = ONE_REAL)
    )

    val result = oneMinusXPolynomial.toPlainText()

    assertThat(result).isEqualTo("x^2 + y^3 + 1")
  }
}

private fun createVariable(name: String, power: Int) = Variable.newBuilder().apply {
  this.name = name
  this.power = power
}.build()

private fun createTerm(coefficient: Real, vararg variables: Variable) = Term.newBuilder().apply {
  this.coefficient = coefficient
  addAllVariable(variables.toList())
}.build()

private fun createPolynomial(vararg terms: Term) = Polynomial.newBuilder().apply {
  addAllTerm(terms.toList())
}.build()
