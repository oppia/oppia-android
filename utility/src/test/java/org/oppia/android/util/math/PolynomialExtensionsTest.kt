package org.oppia.android.util.math

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Polynomial
import org.oppia.android.app.model.Polynomial.Term
import org.oppia.android.app.model.Polynomial.Term.Variable
import org.oppia.android.app.model.Real
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.RunParameterized
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedJunitTestRunner
import org.oppia.android.testing.math.PolynomialSubject.Companion.assertThat
import org.oppia.android.testing.math.RealSubject.Companion.assertThat
import org.robolectric.annotation.LooperMode

/** Tests for [Polynomial] extensions. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedJunitTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class PolynomialExtensionsTest {
  private companion object {
    private val ONE_THIRD_FRACTION = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 3
    }.build()

    private val ONE_AND_ONE_HALF_FRACTION = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 2
      wholeNumber = 1
    }.build()

    private val THREE_ONES_FRACTION = Fraction.newBuilder().apply {
      numerator = 3
      denominator = 1
    }.build()

    private val THREE_FRACTION = Fraction.newBuilder().apply {
      wholeNumber = 3
      denominator = 1
    }.build()

    private val TWO_REAL = Real.newBuilder().apply {
      integer = 2
    }.build()

    private val THREE_REAL = Real.newBuilder().apply {
      integer = 3
    }.build()

    private val FOUR_REAL = Real.newBuilder().apply {
      integer = 4
    }.build()

    private val FIVE_REAL = Real.newBuilder().apply {
      integer = 5
    }.build()

    private val SEVEN_REAL = Real.newBuilder().apply {
      integer = 7
    }.build()

    private val ONE_THIRD_REAL = Real.newBuilder().apply {
      rational = ONE_THIRD_FRACTION
    }.build()

    private val ONE_AND_ONE_HALF_REAL = Real.newBuilder().apply {
      rational = ONE_AND_ONE_HALF_FRACTION
    }.build()

    private val THREE_ONES_REAL = Real.newBuilder().apply {
      rational = THREE_ONES_FRACTION
    }.build()

    private val THREE_FRACTION_REAL = Real.newBuilder().apply {
      rational = THREE_FRACTION
    }.build()

    private val ONE_POINT_FIVE_REAL = Real.newBuilder().apply {
      irrational = 1.5
    }.build()

    private val TWO_DOUBLE_REAL = Real.newBuilder().apply {
      irrational = 2.0
    }.build()

    private val PI_REAL = Real.newBuilder().apply {
      irrational = 3.14
    }.build()

    private val TWO_POLYNOMIAL = createPolynomial(createTerm(coefficient = TWO_REAL))

    private val NEGATIVE_TWO_POLYNOMIAL = createPolynomial(createTerm(coefficient = -TWO_REAL))

    private val ONE_HALF_POLYNOMIAL = createPolynomial(createTerm(coefficient = ONE_HALF))

    private val NEGATIVE_ONE_HALF_POLYNOMIAL =
      createPolynomial(createTerm(coefficient = -ONE_HALF))

    private val ONE_AND_ONE_HALF_POLYNOMIAL =
      createPolynomial(createTerm(coefficient = ONE_AND_ONE_HALF_REAL))

    private val NEGATIVE_ONE_AND_ONE_HALF_POLYNOMIAL =
      createPolynomial(createTerm(coefficient = -ONE_AND_ONE_HALF_REAL))

    private val PI_POLYNOMIAL = createPolynomial(createTerm(coefficient = PI_REAL))

    private val NEGATIVE_PI_POLYNOMIAL = createPolynomial(createTerm(coefficient = -PI_REAL))

    private val ONE_X_POLYNOMIAL =
      createPolynomial(createTerm(coefficient = ONE, createVariable(name = "x", power = 1)))

    private val NEGATIVE_ONE_X_POLYNOMIAL =
      createPolynomial(createTerm(coefficient = -ONE, createVariable(name = "x", power = 1)))

    private val TWO_X_POLYNOMIAL =
      createPolynomial(createTerm(coefficient = TWO_REAL, createVariable(name = "x", power = 1)))

    private val ONE_PLUS_X_POLYNOMIAL =
      createPolynomial(
        createTerm(coefficient = ONE),
        createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
      )
  }

  @Parameter lateinit var var1: String
  @Parameter lateinit var var2: String
  @Parameter lateinit var var3: String

  @Test
  fun testZeroPolynomial_isEqualToZero() {
    val subject = ZERO_POLYNOMIAL

    assertThat(subject).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testOnePolynomial_isEqualToOne() {
    val subject = ONE_POLYNOMIAL

    assertThat(subject).isConstantThat().isIntegerThat().isEqualTo(1)
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
      createPolynomial(createTerm(coefficient = ONE), createTerm(coefficient = TWO_REAL))

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

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(3.14)
  }

  @Test
  fun testGetConstant_negativePi_returnsNegativePi() {
    val result = NEGATIVE_PI_POLYNOMIAL.getConstant()

    assertThat(result).isIrrationalThat().isWithin(1e-5).of(-3.14)
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

    assertThat(result).isEqualTo("3.14")
  }

  @Test
  fun testToPlainText_negativePi_returnsMinusPiString() {
    val result = NEGATIVE_PI_POLYNOMIAL.toPlainText()

    assertThat(result).isEqualTo("-3.14")
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
      createTerm(coefficient = ONE),
      createTerm(coefficient = -ONE, createVariable(name = "x", power = 1))
    )

    val result = oneMinusXPolynomial.toPlainText()

    assertThat(result).isEqualTo("1 - x")
  }

  @Test
  fun testToPlainText_oneAndOneHalfXAndY_returnsThreeHalvesXPlusYString() {
    val oneMinusXPolynomial = createPolynomial(
      createTerm(coefficient = ONE_AND_ONE_HALF_REAL, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "y", power = 1))
    )

    val result = oneMinusXPolynomial.toPlainText()

    assertThat(result).isEqualTo("(3/2)x + y")
  }

  @Test
  fun testToPlainText_oneAndXAndXSquared_returnsOnePlusXPlusXSquaredString() {
    val oneMinusXPolynomial = createPolynomial(
      createTerm(coefficient = ONE),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )

    val result = oneMinusXPolynomial.toPlainText()

    assertThat(result).isEqualTo("1 + x + x^2")
  }

  @Test
  fun testToPlainText_xSquaredAndXAndOne_returnsXSquaredPlusXPlusOneString() {
    val oneMinusXPolynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE)
    )

    val result = oneMinusXPolynomial.toPlainText()

    // Compared with the test above, this shows that term order matters for string conversion.
    assertThat(result).isEqualTo("x^2 + x + 1")
  }

  @Test
  fun testToPlainText_xSquaredYCubedAndOne_returnsXSquaredYCubedPlusOneString() {
    val oneMinusXPolynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = ONE, createVariable(name = "y", power = 3)),
      createTerm(coefficient = ONE)
    )

    val result = oneMinusXPolynomial.toPlainText()

    assertThat(result).isEqualTo("x^2 + y^3 + 1")
  }

  @Test
  fun testRemoveUnnecessaryVariables_zeroX_returnsZero() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ZERO, createVariable(name = "x", power = 1))
    )

    val result = polynomial.removeUnnecessaryVariables()

    // 0x becomes just 0.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testRemoveUnnecessaryVariables_xPlusZero_returnsX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ZERO)
    )

    val result = polynomial.removeUnnecessaryVariables()

    // x+0 is just x.
    assertThat(result).isEqualTo(ONE_X_POLYNOMIAL)
  }

  @Test
  fun testRemoveUnnecessaryVariables_zeroXPlusOne_returnsOne() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ZERO, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE)
    )

    val result = polynomial.removeUnnecessaryVariables()

    // 0x+1 is just 1.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(1)
  }

  @Test
  fun testRemoveUnnecessaryVariables_zeroXPlusZero_returnsZero() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ZERO, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ZERO)
    )

    val result = polynomial.removeUnnecessaryVariables()

    // 0x+0 is just 0.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testRemoveUnnecessaryVariables_zeroXSquaredPlusZeroXPlusTwo_returnsTwo() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ZERO, createVariable(name = "x", power = 2)),
      createTerm(coefficient = ZERO, createVariable(name = "x", power = 1)),
      createTerm(coefficient = TWO_REAL)
    )

    val result = polynomial.removeUnnecessaryVariables()

    // 0x^2+0x+2 is just 2.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(2)
  }

  @Test
  fun testRemoveUnnecessaryVariables_zeroPlusOnePlusZeroXPlusZero_returnsOne() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ZERO),
      createTerm(coefficient = ONE),
      createTerm(coefficient = ZERO, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ZERO)
    )

    val result = polynomial.removeUnnecessaryVariables()

    // 0+1+0x+0 is just 1.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(1)
  }

  @Test
  fun testSimplifyRationals_oneX_returnsOneX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result = polynomial.simplifyRationals()

    // x stays as x.
    assertThat(result).isEqualTo(ONE_X_POLYNOMIAL)
  }

  @Test
  fun testSimplifyRationals_oneHalfX_returnsOneHalfX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE_HALF, createVariable(name = "x", power = 1))
    )

    val result = polynomial.simplifyRationals()

    // (1/2)x stays as (1/2)x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasVariableCountThat().isEqualTo(1)
        hasCoefficientThat().isEqualTo(ONE_HALF)
        variable(0).hasNameThat().isEqualTo("x")
        variable(0).hasPowerThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testSimplifyRationals_threeOnesX_returnsThreeOnesX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = THREE_ONES_REAL, createVariable(name = "x", power = 1))
    )

    val result = polynomial.simplifyRationals()

    // (3/1)x stays as 3x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasVariableCountThat().isEqualTo(1)
        hasCoefficientThat().isIntegerThat().isEqualTo(3)
        variable(0).hasNameThat().isEqualTo("x")
        variable(0).hasPowerThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testSimplifyRationals_negativeThreeXAsFraction_returnsNegativeThreeXWithInteger() {
    val polynomial = createPolynomial(
      createTerm(coefficient = -THREE_FRACTION_REAL, createVariable(name = "x", power = 1))
    )

    val result = polynomial.simplifyRationals()

    // -3x (fraction) becomes -3x (integer).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasVariableCountThat().isEqualTo(1)
        hasCoefficientThat().isIntegerThat().isEqualTo(-3)
        variable(0).hasNameThat().isEqualTo("x")
        variable(0).hasPowerThat().isEqualTo(1)
      }
    }
  }

  @Test
  fun testSimplifyRationals_xPlusThreeFractionXSquared_returnsXPlusThreeXSquared() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = THREE_FRACTION_REAL, createVariable(name = "x", power = 2))
    )

    val result = polynomial.simplifyRationals()

    // x+3x (fraction) becomes x+3x (integer).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasVariableCountThat().isEqualTo(1)
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        variable(0).hasNameThat().isEqualTo("x")
        variable(0).hasPowerThat().isEqualTo(1)
      }
      term(1).apply {
        hasVariableCountThat().isEqualTo(1)
        hasCoefficientThat().isIntegerThat().isEqualTo(3)
        variable(0).hasNameThat().isEqualTo("x")
        variable(0).hasPowerThat().isEqualTo(2)
      }
    }
  }

  @Test
  fun testSort_one_returnsOne() {
    val polynomial = createPolynomial(createTerm(coefficient = ONE))

    val result = polynomial.sort()

    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(1)
  }

  @Test
  fun testSort_x_returnsX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result = polynomial.sort()

    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testSort_onePlusTwo_returnsTwoPlusOne() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE), createTerm(coefficient = TWO_REAL)
    )

    val result = polynomial.sort()

    // 1+2 becomes 2+1 (larger number sorted first).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(2)
        hasVariableCountThat().isEqualTo(0)
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testSort_twoPlusOne_returnsTwoPlusOne() {
    val polynomial = createPolynomial(
      createTerm(coefficient = TWO_REAL), createTerm(coefficient = ONE)
    )

    val result = polynomial.sort()

    // 2+1 stays as  2+1 (larger number sorted first).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(2)
        hasVariableCountThat().isEqualTo(0)
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testSort_xPlusX_returnsXPlusX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result = polynomial.sort()

    // x+x is symmetrical, so nothing changes.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testSort_xPlusOne_returnsXPlusOne() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE)
    )

    val result = polynomial.sort()

    // x+1 stays as x+1 (variables are sorted first).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testSort_onePlusX_returnsXPlusOne() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result = polynomial.sort()

    // 1+x becomes x+1 (variables are sorted first).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testSort_xPlusTwoX_returnsTwoXPlusX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = TWO_REAL, createVariable(name = "x", power = 1))
    )

    val result = polynomial.sort()

    // x+2x becomes 2x+x (larger coefficients are sorted first).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(2)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testSort_xPlusXSquared_returnsXSquaredPlusX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )

    val result = polynomial.sort()

    // x+x^2 becomes x^2+x (larger powers are sorted first).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testSort_xSquaredPlusX_returnsXSquaredPlusX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result = polynomial.sort()

    // x^2+x stays as x^2+x (larger powers are sorted first).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testSort_xMinusXSquared_returnsNegativeXSquaredPlusX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = -ONE, createVariable(name = "x", power = 2))
    )

    val result = polynomial.sort()

    // 1-x^2 becomes -x^2+1 (larger powers are sorted first).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testSort_negativeXSquaredPlusX_returnsNegativeXSquaredPlusX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = -ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result = polynomial.sort()

    // -x^2+1 stays as -x^2+1 (larger powers are sorted first).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testSort_yPlusXy_returnsXyPlusY() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "y", power = 1)),
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1)
      )
    )

    val result = polynomial.sort()

    // y+xy becomes xy+y (x variables are sorted first).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testSort_xPlusXy_returnsXyPlusX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1)
      )
    )

    val result = polynomial.sort()

    // x+xy becomes xy+x (more variables are sorted first)
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testSort_xyPlusZyx_returnsXyzPlusXy() {
    val polynomial = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1)
      ),
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1),
        createVariable(name = "z", power = 1)
      )
    )

    val result = polynomial.sort()

    // xy+zyx becomes xyz+xy (again, more variables are sorted first). Also, variables are
    // rearranged lexicographically.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(3)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
        variable(2).apply {
          hasNameThat().isEqualTo("z")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testSort_zyPlusYx_returnsXyPlusYz() {
    val polynomial = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "z", power = 1),
        createVariable(name = "y", power = 1)
      ),
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1)
      )
    )

    val result = polynomial.sort()

    // zy+yx becomes xy+yz (sorted lexicographically).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("z")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testSort_xyzPlusYXSquared_returnsXSquaredYPlusXyz() {
    val polynomial = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1),
        createVariable(name = "z", power = 1)
      ),
      createTerm(
        coefficient = ONE,
        createVariable(name = "y", power = 1),
        createVariable(name = "x", power = 2)
      )
    )

    val result = polynomial.sort()

    // xyz+yx^2 becomes x^2y+xyz (despite xyz having more variables, the higher power of x^2y
    // prioritizes it).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(3)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
        variable(2).apply {
          hasNameThat().isEqualTo("z")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testSort_xSquaredY_plusX_plusYCubed_plusXSquared_returnsYCubedPlusXSqYPlusXSqPlusX() {
    val polynomial = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      ),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "y", power = 3)),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )

    val result = polynomial.sort()

    // x^2y+x+y^3+x^2 becomes x^2y+x^2+x+y^3 per rules demonstrated in earlier tests. This test
    // brings more of them together in one example, plus note that x terms are always fully listed
    // first.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(4)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(2).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(3).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(3)
        }
      }
    }
  }

  @Test
  @RunParameterized(
    Iteration("x+y+z", "var1=x", "var2=y", "var3=z"),
    Iteration("x+z+y", "var1=x", "var2=z", "var3=y"),
    Iteration("y+x+z", "var1=y", "var2=x", "var3=z"),
    Iteration("y+z+x", "var1=y", "var2=z", "var3=x"),
    Iteration("z+x+y", "var1=z", "var2=x", "var3=y"),
    Iteration("z+y+x", "var1=z", "var2=y", "var3=x")
  )
  fun testSort_xPlusYPlusZ_inAnyOrder_returnsXPlusYPlusZ() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = var1, power = 1)),
      createTerm(coefficient = ONE, createVariable(name = var2, power = 1)),
      createTerm(coefficient = ONE, createVariable(name = var3, power = 1))
    )

    val result = polynomial.sort()

    // Regardless of what order x, y, and z are combined in a polynomial, the sorted result is
    // always x+y+z (per lexicographical sorting of the variable names themselves).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(3)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(2).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("z")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  /* Equality checks. Note that these are symmetrical to reduce the number of needed test cases. */

  @Test
  fun testIsApproximatelyEqualTo_firstIsDefault_secondIsDefault_returnsTrue() {
    val first = Polynomial.getDefaultInstance()
    val second = Polynomial.getDefaultInstance()

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsDefault_secondIsConstPolyOfInt2_returnsFalse() {
    val first = Polynomial.getDefaultInstance()
    val second = createPolynomial(createTerm(coefficient = TWO_REAL))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsConstPolyOfInt2_secondIsDefault_returnsFalse() {
    val first = createPolynomial(createTerm(coefficient = TWO_REAL))
    val second = Polynomial.getDefaultInstance()

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfInt2_secondIsPolyOfInt2_returnsTrue() {
    val first = createPolynomial(createTerm(coefficient = TWO_REAL))
    val second = createPolynomial(createTerm(coefficient = TWO_REAL))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfInt2_secondIsPolyOfInt3_returnsFalse() {
    val first = createPolynomial(createTerm(coefficient = TWO_REAL))
    val second = createPolynomial(createTerm(coefficient = THREE_REAL))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfInt3_secondIsPolyOfFrac3_returnsTrue() {
    val first = createPolynomial(createTerm(coefficient = THREE_REAL))
    val second = createPolynomial(createTerm(coefficient = THREE_FRACTION_REAL))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // These are equal since reals are fully evaluated for polynomials.
    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfInt3_secondIsPolyOfFrac3Ones_returnsTrue() {
    val first = createPolynomial(createTerm(coefficient = THREE_REAL))
    val second = createPolynomial(createTerm(coefficient = THREE_ONES_REAL))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // These are equal since reals are fully evaluated for polynomials.
    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfInt3_secondIsPolyOfFracOneAndOneHalf_returnsFalse() {
    val first = createPolynomial(createTerm(coefficient = THREE_REAL))
    val second = createPolynomial(createTerm(coefficient = ONE_AND_ONE_HALF_REAL))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfInt2_secondIsPolyOfDouble2_returnsTrue() {
    val first = createPolynomial(createTerm(coefficient = TWO_REAL))
    val second = createPolynomial(createTerm(coefficient = TWO_DOUBLE_REAL))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // These are equal since reals are fully evaluated for polynomials.
    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfInt2_secondIsPolyOfDouble2PlusMargin_returnsTrue() {
    val first = createPolynomial(createTerm(coefficient = TWO_REAL))
    val second = createPolynomial(
      createTerm(
        coefficient = Real.newBuilder().apply {
          irrational = 2.00000000000000001
        }.build()
      )
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // These are equal since reals are fully evaluated with a margin check.
    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfInt3_secondIsPolyOfDoublePi_returnsFalse() {
    val first = createPolynomial(createTerm(coefficient = THREE_REAL))
    val second = createPolynomial(createTerm(coefficient = PI_REAL))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsDoubleOnePointFive_secondIsFracOneAndOneHalf_returnsTrue() {
    val first = createPolynomial(createTerm(coefficient = ONE_POINT_FIVE_REAL))
    val second = createPolynomial(createTerm(coefficient = ONE_AND_ONE_HALF_REAL))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // These are equal since reals are fully evaluated for polynomials.
    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsDoublePointThrees_secondIsFracOneThird_returnsTrue() {
    val first = createPolynomial(
      createTerm(
        coefficient = Real.newBuilder().apply {
          irrational = 0.33333333333333333
        }.build()
      )
    )
    val second = createPolynomial(createTerm(coefficient = ONE_THIRD_REAL))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // These are equal since reals are fully evaluated with a margin check.
    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfVarX_secondIsPolyOfVarX_returnsTrue() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )
    val second = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfVarX_secondIsPolyOfVarY_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )
    val second = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "y", power = 1))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfVarX_secondIsPolyOfInt2_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )
    val second = createPolynomial(createTerm(coefficient = TWO_REAL))

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfVarsXy_secondIsPolyOfVarX_returnsFalse() {
    val first = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1)
      )
    )
    val second = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // A variable is missing.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfVarsXy_secondIsPolyOfVarY_returnsFalse() {
    val first = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1)
      )
    )
    val second = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "y", power = 1))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // A variable is missing.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfVarsXy_secondIsPolyOfVarsXy_returnsTrue() {
    val first = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1)
      )
    )
    val second = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1)
      )
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsPolyOfVarsXy_secondIsPolyOfVarsYx_returnsFalse() {
    val first = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1)
      )
    )
    val second = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "y", power = 1),
        createVariable(name = "x", power = 1)
      )
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // Order matters (which is why the function recommends only comparing sorted polynomials).
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquared_secondIsXSquared_returnsTrue() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )
    val second = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquared_secondIsNegativeXSquared_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )
    val second = createPolynomial(
      createTerm(coefficient = -ONE, createVariable(name = "x", power = 2))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // Coefficient sign differs.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquared_secondIsTwoXSquared_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )
    val second = createPolynomial(
      createTerm(coefficient = TWO_REAL, createVariable(name = "x", power = 2))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // Coefficient value is different.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquared_secondIsX_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )
    val second = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // The powers don't match.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquared_secondIsXCubed_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )
    val second = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 3))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // The powers don't match.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquared_secondIsXSquaredY_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )
    val second = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // There's an extra variable in one of the polynomials.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquaredY_secondIsXSquaredY_returnsTrue() {
    val first = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )
    val second = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquaredY_secondIsXy_returnsFalse() {
    val first = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )
    val second = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1)
      )
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // x's power isn't correct.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquaredY_secondIsXYSquared_returnsFalse() {
    val first = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )
    val second = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 2)
      )
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // The wrong variable is squared.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquaredY_secondIsXSquaredYSquared_returnsFalse() {
    val first = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )
    val second = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 2)
      )
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // y is incorrectly also squared.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquaredY_secondIsYXSquared_returnsFalse() {
    val first = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )
    val second = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "y", power = 1),
        createVariable(name = "x", power = 2)
      )
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // The terms are out of order.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquaredY_secondIsNegativeXSquaredY_returnsFalse() {
    val first = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )
    val second = createPolynomial(
      createTerm(
        coefficient = -ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // The sign is incorrect on the second polynomial.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXSquaredY_secondIsTwoXSquaredY_returnsFalse() {
    val first = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )
    val second = createPolynomial(
      createTerm(
        coefficient = TWO_REAL,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // The coefficient is incorrect on the second polynomial.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXPlusY_secondIsXPlusY_returnsTrue() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "y", power = 1))
    )
    val second = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "y", power = 1))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isTrue()
    assertThat(result2).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXPlusY_secondIsXPlusYSquared_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "y", power = 1))
    )
    val second = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "y", power = 2))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // The second polynomial's y power doesn't match.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXPlusY_secondIsXPlusFiveY_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "y", power = 1))
    )
    val second = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = FIVE_REAL, createVariable(name = "y", power = 1))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // The second polynomial's y coefficient doesn't match.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_firstIsXPlusY_secondIsNegativeXPlusY_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "y", power = 1))
    )
    val second = createPolynomial(
      createTerm(coefficient = -ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "y", power = 1))
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // The second polynomial's x coefficient negativity doesn't match.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_complexMultiTermMultiVarPolys_allTermsSame_returnsTrue() {
    val polynomial = createPolynomial(
      createTerm(coefficient = -ONE_AND_ONE_HALF_REAL, createVariable(name = "x", power = 1)),
      createTerm(
        coefficient = FIVE_REAL,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 3),
        createVariable(name = "z", power = 1)
      ),
      createTerm(
        coefficient = -PI_REAL,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      ),
      createTerm(coefficient = ONE, createVariable(name = "z", power = 1)),
      createTerm(coefficient = SEVEN_REAL)
    )

    val result = polynomial.isApproximatelyEqualTo(polynomial)

    assertThat(result).isTrue()
  }

  @Test
  fun testIsApproximatelyEqualTo_complexMultiTermMultiVarPolys_oneItemOutOfOrder_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = -ONE_AND_ONE_HALF_REAL, createVariable(name = "x", power = 1)),
      createTerm(
        coefficient = FIVE_REAL,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 3),
        createVariable(name = "z", power = 1)
      ),
      createTerm(
        coefficient = -PI_REAL,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      ),
      createTerm(coefficient = ONE, createVariable(name = "z", power = 1)),
      createTerm(coefficient = SEVEN_REAL)
    )
    val second = createPolynomial(
      createTerm(coefficient = -ONE_AND_ONE_HALF_REAL, createVariable(name = "x", power = 1)),
      createTerm(
        coefficient = FIVE_REAL,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 3),
        createVariable(name = "z", power = 1)
      ),
      createTerm(
        coefficient = -PI_REAL,
        createVariable(name = "y", power = 1),
        createVariable(name = "x", power = 2)
      ),
      createTerm(coefficient = ONE, createVariable(name = "z", power = 1)),
      createTerm(coefficient = SEVEN_REAL)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // One element is out of order in the second polynomial.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_complexMultiTermMultiVarPolys_oneItemDifferent_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = -ONE_AND_ONE_HALF_REAL, createVariable(name = "x", power = 1)),
      createTerm(
        coefficient = FIVE_REAL,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 3),
        createVariable(name = "z", power = 1)
      ),
      createTerm(
        coefficient = -PI_REAL,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      ),
      createTerm(coefficient = ONE, createVariable(name = "z", power = 1)),
      createTerm(coefficient = SEVEN_REAL)
    )
    val second = createPolynomial(
      createTerm(coefficient = -ONE_AND_ONE_HALF_REAL, createVariable(name = "x", power = 1)),
      createTerm(
        coefficient = FIVE_REAL - ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 3),
        createVariable(name = "z", power = 1)
      ),
      createTerm(
        coefficient = -PI_REAL,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      ),
      createTerm(coefficient = ONE, createVariable(name = "z", power = 1)),
      createTerm(coefficient = SEVEN_REAL)
    )

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    // One coefficient is different in the second polynomial.
    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  @Test
  fun testIsApproximatelyEqualTo_complexMultiTermMultiVarPolys_compareToDefault_returnsFalse() {
    val first = createPolynomial(
      createTerm(coefficient = -ONE_AND_ONE_HALF_REAL, createVariable(name = "x", power = 1)),
      createTerm(
        coefficient = FIVE_REAL,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 3),
        createVariable(name = "z", power = 1)
      ),
      createTerm(
        coefficient = -PI_REAL,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      ),
      createTerm(coefficient = ONE, createVariable(name = "z", power = 1)),
      createTerm(coefficient = SEVEN_REAL)
    )
    val second = Polynomial.getDefaultInstance()

    val result1 = first.isApproximatelyEqualTo(second)
    val result2 = second.isApproximatelyEqualTo(first)

    assertThat(result1).isFalse()
    assertThat(result2).isFalse()
  }

  /* Operator tests. */

  @Test
  fun testUnaryMinus_zero_returnsZero() {
    val polynomial = ZERO_POLYNOMIAL

    val result = -polynomial

    // negate(0) stays as 0.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testUnaryMinus_one_returnsNegativeOne() {
    val polynomial = ONE_POLYNOMIAL

    val result = -polynomial

    // negate(1) becomes -1.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(-1)
  }

  @Test
  fun testUnaryMinus_x_returnsNegativeX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result = -polynomial

    // negate(x) becomes -x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testUnaryMinus_negativeX_returnsX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = -ONE, createVariable(name = "x", power = 1))
    )

    val result = -polynomial

    // negate(-x) becomes x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testUnaryMinus_xSquaredPlusX_returnsNegativeXSquaredMinusX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result = -polynomial

    // negate(x^2+x) becomes -x^2-x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testUnaryMinus_oneMinusX_returnsNegativeOnePlusX() {
    val polynomial = createPolynomial(
      createTerm(coefficient = ONE),
      createTerm(coefficient = -ONE, createVariable(name = "x", power = 1))
    )

    val result = -polynomial

    // negate(1-x) becomes -1+x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(0)
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testPlus_zeroAndZero_returnsZero() {
    val polynomial1 = ZERO_POLYNOMIAL
    val polynomial2 = ZERO_POLYNOMIAL

    val result = polynomial1 + polynomial2

    // 0+0=0.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testPlus_zeroAndOne_returnsOne() {
    val polynomial1 = ZERO_POLYNOMIAL
    val polynomial2 = ONE_POLYNOMIAL

    val result = polynomial1 + polynomial2

    // 0+1=1.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(1)
  }

  @Test
  fun testPlus_zeroAndX_returnsX() {
    val polynomial1 = ZERO_POLYNOMIAL
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 + polynomial2

    // 0+x=x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testPlus_oneAndX_returnsOnePlusX() {
    val polynomial1 = ONE_POLYNOMIAL
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 + polynomial2

    // poly(1)+poly(x)=poly(1+x).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testPlus_xAndOne_returnsXPlusOne() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = ONE_POLYNOMIAL

    val result = polynomial1 + polynomial2

    // poly(x)+poly(1)=poly(x+1). Per sorting, this shows commutativity with the above operation.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testPlus_xAndX_returnsTwoX() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 + polynomial2

    // x+x=2x (shows combining like terms).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(2)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testPlus_xAndNegativeX_returnsZero() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = NEGATIVE_ONE_X_POLYNOMIAL

    val result = polynomial1 + polynomial2

    // x+-x=0 (term elimination).
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testPlus_xSquaredAndX_returnsXSquaredPlusX() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 + polynomial2

    // poly(x^2)+poly(x)=poly(x^+x).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testMinus_zeroAndZero_returnsZero() {
    val polynomial1 = ZERO_POLYNOMIAL
    val polynomial2 = ZERO_POLYNOMIAL

    val result = polynomial1 - polynomial2

    // 0-0=0 (term elimination).
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testMinus_oneAndZero_returnsOne() {
    val polynomial1 = ONE_POLYNOMIAL
    val polynomial2 = ZERO_POLYNOMIAL

    val result = polynomial1 - polynomial2

    // 1-0=1.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(1)
  }

  @Test
  fun testMinus_xAndZero_returnsX() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = ZERO_POLYNOMIAL

    val result = polynomial1 - polynomial2

    // x-0=x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testMinus_xAndOne_returnsXMinusOne() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = ONE_POLYNOMIAL

    val result = polynomial1 - polynomial2

    // poly(x)-poly(1)=poly(x-1).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testMinus_oneAndX_returnsOneMinusX() {
    val polynomial1 = ONE_POLYNOMIAL
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 - polynomial2

    // poly(1)-poly(x)=poly(1-x). Shows anticommutativity.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testMinus_twoXAndX_returnsX() {
    val polynomial1 = TWO_X_POLYNOMIAL
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 - polynomial2

    // 2x-x=x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testMinus_xAndX_returnsZero() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 - polynomial2

    // x-x=0.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testMinus_xAndNegativeX_returnsTwoX() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = NEGATIVE_ONE_X_POLYNOMIAL

    val result = polynomial1 - polynomial2

    // x - -x=2x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(2)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testMinus_negativeXAndX_returnsNegativeTwoX() {
    val polynomial1 = NEGATIVE_ONE_X_POLYNOMIAL
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 - polynomial2

    // -x - x=-2x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-2)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testMinus_negativeXAndNegativeX_returnsZero() {
    val polynomial1 = NEGATIVE_ONE_X_POLYNOMIAL
    val polynomial2 = NEGATIVE_ONE_X_POLYNOMIAL

    val result = polynomial1 - polynomial2

    // -x - -x=0.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testMinus_xSquaredAndX_returnsXSquaredMinusX() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 - polynomial2

    // poly(x^2)-poly(x)=poly(x^2-x).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testTimes_zeroAndZero_returnsZero() {
    val polynomial1 = ZERO_POLYNOMIAL
    val polynomial2 = ZERO_POLYNOMIAL

    val result = polynomial1 * polynomial2

    // 0*0=0.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testTimes_zeroAndOne_returnsZero() {
    val polynomial1 = ZERO_POLYNOMIAL
    val polynomial2 = ONE_POLYNOMIAL

    val result = polynomial1 * polynomial2

    // 0*1=0.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testTimes_oneAndOne_returnsOne() {
    val polynomial1 = ONE_POLYNOMIAL
    val polynomial2 = ONE_POLYNOMIAL

    val result = polynomial1 * polynomial2

    // 1*1=1.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(1)
  }

  @Test
  fun testTimes_twoAndThree_returnsSix() {
    val polynomial1 = TWO_POLYNOMIAL
    val polynomial2 = createPolynomial(createTerm(coefficient = THREE_REAL))

    val result = polynomial1 * polynomial2

    // 2*3=6.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(6)
  }

  @Test
  fun testTimes_xAndZero_returnsZero() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = ZERO_POLYNOMIAL

    val result = polynomial1 * polynomial2

    // x*0=0.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testTimes_xAndOne_returnsX() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = ONE_POLYNOMIAL

    val result = polynomial1 * polynomial2

    // x*1=x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testTimes_xAndX_returnsXSquared() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 * polynomial2

    // x*x=x^2.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
  }

  @Test
  fun testTimes_threeXSquaredAndTwoX_returnsSixXCubed() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = THREE_REAL, createVariable(name = "x", power = 2))
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = TWO_REAL, createVariable(name = "x", power = 1))
    )

    val result = polynomial1 * polynomial2

    // 3x^2*2x=6x^3.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(6)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(3)
        }
      }
    }
  }

  @Test
  fun testTimes_twoXAndThreeXSquared_returnsSixXCubed() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = TWO_REAL, createVariable(name = "x", power = 1))
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = THREE_REAL, createVariable(name = "x", power = 2))
    )

    val result = polynomial1 * polynomial2

    // 2x*3x^2=6x^3. This demonstrates multiplication commutativity.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(6)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(3)
        }
      }
    }
  }

  @Test
  fun testTimes_xAndNegativeX_returnsNegativeXSquared() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = NEGATIVE_ONE_X_POLYNOMIAL

    val result = polynomial1 * polynomial2

    // x*(-x)=-x^2.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
  }

  @Test
  fun testTimes_negativeXAndX_returnsNegativeXSquared() {
    val polynomial1 = NEGATIVE_ONE_X_POLYNOMIAL
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 * polynomial2

    // (-x)*x=-x^2.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
  }

  @Test
  fun testTimes_negativeXAndNegativeX_returnsXSquared() {
    val polynomial1 = NEGATIVE_ONE_X_POLYNOMIAL
    val polynomial2 = NEGATIVE_ONE_X_POLYNOMIAL

    val result = polynomial1 * polynomial2

    // (-x)*(-x)=x^2 (negatives cancel out).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
  }

  @Test
  fun testTimes_negativeFiveX_sevenX_returnsNegativeThirtyFiveXSquared() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = -FIVE_REAL, createVariable(name = "x", power = 1))
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = SEVEN_REAL, createVariable(name = "x", power = 1))
    )

    val result = polynomial1 * polynomial2

    // -5x*7x=-35x^2.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-35)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
  }

  @Test
  fun testTimes_onePlusX_onePlusX_returnsOnePlus2XPlusXSquared() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result = polynomial1 * polynomial2

    // (1+x)*(1+x)=1+2x+x^2 (like terms are combined).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(3)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(2)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(2).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
  }

  @Test
  fun testTimes_xPlusOne_xMinusOne_returnsXSquaredMinusOne() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE)
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = -ONE)
    )

    val result = polynomial1 * polynomial2

    // (x+1)*(x-1)=x^2-1 (negative terms are eliminated).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testTimes_xMinusOne_xPlusOne_returnsXSquaredMinusOne() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = -ONE)
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE)
    )

    val result = polynomial1 * polynomial2

    // (x-1)*(x+1)=x^2-1 (commutativity works for combining terms, too).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testTimes_twoXy_threeXSquaredY_returnsSixXCubedYSquared() {
    val polynomial1 = createPolynomial(
      createTerm(
        coefficient = TWO_REAL,
        createVariable(name = "x", power = 1),
        createVariable(name = "y", power = 1)
      )
    )
    val polynomial2 = createPolynomial(
      createTerm(
        coefficient = THREE_REAL,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )

    val result = polynomial1 * polynomial2

    // 2xy*3x^2y=6x^3y^2.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(6)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(3)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
  }

  @Test
  fun testDiv_oneAndZero_returnsNull() {
    val polynomial1 = ONE_POLYNOMIAL
    val polynomial2 = ZERO_POLYNOMIAL

    val result = polynomial1 / polynomial2

    // Cannot divide by zero.
    assertThat(result).isNotValidPolynomial()
  }

  @Test
  fun testDiv_threeAndTwo_returnsOneAndOneHalf() {
    val polynomial1 = createPolynomial(createTerm(coefficient = THREE_REAL))
    val polynomial2 = TWO_POLYNOMIAL

    val result = polynomial1 / polynomial2

    // 3/2=1 1/2 (fraction) to demonstrate fully constant division.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isRationalThat().apply {
          hasNegativePropertyThat().isFalse()
          hasWholeNumberThat().isEqualTo(1)
          hasNumeratorThat().isEqualTo(1)
          hasDenominatorThat().isEqualTo(2)
        }
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testDiv_piAndTwo_returnsHalfPi() {
    val polynomial1 = PI_POLYNOMIAL
    val polynomial2 = TWO_POLYNOMIAL

    val result = polynomial1 / polynomial2

    // 3.14/2=1.57 (irrational) to demonstrate fully constant division.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIrrationalThat().isWithin(1e-5).of(1.57)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testDiv_xAndOne_returnsX() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = ONE_POLYNOMIAL

    val result = polynomial1 / polynomial2

    // x/1=x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testDiv_oneAndX_returnsNull() {
    val polynomial1 = ONE_POLYNOMIAL
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 / polynomial2

    // 1/x fails (cannot have negative power terms in polynomials, and this also shows that division
    // is not commutative).
    assertThat(result).isNotValidPolynomial()
  }

  @Test
  fun testDiv_xSquared_x_returnsX() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 / polynomial2

    // x^2/x=x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testDiv_onePlus2XPlusXSquared_onePlusX_returnsOnePlusX() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE),
      createTerm(coefficient = TWO_REAL, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result = polynomial1 / polynomial2

    // (1+2x+x^2)/(1+x)=x+1 (full polynomial division).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testDiv_xSquaredPlus2XPlusOne_onePlusX_returnsOnePlusX() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = TWO_REAL, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE)
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result = polynomial1 / polynomial2

    // (x^2+2x+1)/(1+x)=x+1 (order of terms for the dividend doesn't matter).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testDiv_xSquaredPlus2XPlusOne_oneMinusX_returnsNull() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = TWO_REAL, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE)
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE),
      createTerm(coefficient = -ONE, createVariable(name = "x", power = 1))
    )

    val result = polynomial1 / polynomial2

    // (x^2+2x+1)/(1-x) fails (division doesn't result in a perfect polynomial).
    assertThat(result).isNotValidPolynomial()
  }

  @Test
  fun testDiv_negativeXCubed_xSquared_returnsNegativeX() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = -ONE, createVariable(name = "x", power = 3))
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )

    val result = polynomial1 / polynomial2

    // -x^3/x^2=-x (negatives are retained).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testDiv_xSquaredMinusOne_xPlusOne_returnsXMinusOne() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = -ONE)
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE)
    )

    val result = polynomial1 / polynomial2

    // (x^2-1)/(x+1)=x-1.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testDiv_xSquaredMinusOne_xMinusOne_returnsXPlusOne() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = -ONE)
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1)),
      createTerm(coefficient = -ONE)
    )

    val result = polynomial1 / polynomial2

    // (x^2-1)/(x-1)=x+1.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testDiv_xSquaredMinusOne_x_returnsNull() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = -ONE)
    )
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 / polynomial2

    // (x^2-1)/x fails.
    assertThat(result).isNotValidPolynomial()
  }

  @Test
  fun testDiv_xSquaredMinusOne_negativeOne_negativeXSquaredPlusOne() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = -ONE)
    )
    val polynomial2 = createPolynomial(createTerm(coefficient = -ONE))

    val result = polynomial1 / polynomial2

    // (x^2-1)/(-1)=-x^2+1 (reverses negative signs).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testDiv_xSquaredMinusOne_two_returnsNull() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = -ONE)
    )
    val polynomial2 = TWO_POLYNOMIAL

    val result = polynomial1 / polynomial2

    // (x^2-1)/2=(1/2)x^2-1/2 (since non-zero constants can always be factored).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isEqualTo(ONE_HALF)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(1).apply {
        hasCoefficientThat().isEqualTo(-ONE_HALF)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testDiv_negativeThreeXSquared_xSquared_returnsNegativeThree() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = -THREE_REAL, createVariable(name = "x", power = 2))
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )

    val result = polynomial1 / polynomial2

    // (-3x^2)/(x^2)=-3 (coefficient is retained).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-3)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testDiv_negativeThreeXSquared_negativeXSquared_returnsThree() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = -THREE_REAL, createVariable(name = "x", power = 2))
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = -ONE, createVariable(name = "x", power = 2))
    )

    val result = polynomial1 / polynomial2

    // (-3x^2)/(-x^2)=3 (negatives cancel during division).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(3)
        hasVariableCountThat().isEqualTo(0)
      }
    }
  }

  @Test
  fun testDiv_xSquaredY_y_returnsXSquared() {
    val polynomial1 = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "y", power = 1))
    )

    val result = polynomial1 / polynomial2

    // x^2y / y=x^2 (variable elimination).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
  }

  @Test
  fun testDiv_xSquaredY_x_returnsXTimesY() {
    val polynomial1 = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )

    val result = polynomial1 / polynomial2

    // x^2y / x=xy (variable power elimination).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testDiv_xSquaredY_xSquared_returnsY() {
    val polynomial1 = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )

    val result = polynomial1 / polynomial2

    // x^2y / x^2=y (variable elimination).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testDiv_xSquaredY_yXSquared_returnsOne() {
    val polynomial1 = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )
    val polynomial2 = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "y", power = 1),
        createVariable(name = "x", power = 2)
      )
    )

    val result = polynomial1 / polynomial2

    // x^2y / yx^2=1 (multi-variable elimination).
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(1)
  }

  @Test
  fun testDiv_xSquaredY_ySquared_returnsNull() {
    val polynomial1 = createPolynomial(
      createTerm(
        coefficient = ONE,
        createVariable(name = "x", power = 2),
        createVariable(name = "y", power = 1)
      )
    )
    val polynomial2 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "y", power = 2))
    )

    val result = polynomial1 / polynomial2

    // x^2y / y^2 fails (no polynomial exists).
    assertThat(result).isNotValidPolynomial()
  }

  @Test
  fun testPow_zeroAndZero_returnsOne() {
    val polynomial1 = ZERO_POLYNOMIAL
    val polynomial2 = ZERO_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // 0^0=1 (conventionally despite this power not existing in mathematics).
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(1)
  }

  @Test
  fun testPow_zeroAndOne_returnsZero() {
    val polynomial1 = ZERO_POLYNOMIAL
    val polynomial2 = ONE_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // 0^1=0.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(0)
  }

  @Test
  fun testPow_oneAndZero_returnsOne() {
    val polynomial1 = ONE_POLYNOMIAL
    val polynomial2 = ZERO_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // 1^0=1 (i.e. exponentiation is not commutative).
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(1)
  }

  @Test
  fun testPow_oneAndOne_returnsOne() {
    val polynomial1 = ONE_POLYNOMIAL
    val polynomial2 = ONE_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // 1^1=1.
    assertThat(result).isConstantThat().isIntegerThat().isEqualTo(1)
  }

  @Test
  fun testPow_xAndOne_returnsX() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = ONE_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // poly(x)^poly(1)=poly(x).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testPow_xAndX_returnsNull() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = ONE_X_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // x^x fails since polynomials can't have variable exponents.
    assertThat(result).isNotValidPolynomial()
  }

  @Test
  fun testPow_xAndTwo_returnsXSquared() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = TWO_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // poly(x)^poly(2)=poly(x^2).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
  }

  @Test
  fun testPow_onePlusX_two_onePlus2XPlusXSquared() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )
    val polynomial2 = TWO_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // (1+x)^2=1+2x+x^2 (binomial expansion).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(3)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(2)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(2).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
  }

  @Test
  fun testPow_x_negativeOne_returnsNull() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = createPolynomial(createTerm(coefficient = -ONE))

    val result = polynomial1 pow polynomial2

    // x^-1 fails since polynomials can't have negative powers.
    assertThat(result).isNotValidPolynomial()
  }

  @Test
  fun testPow_two_negativeOne_returnsOneHalf() {
    val polynomial1 = TWO_POLYNOMIAL
    val polynomial2 = createPolynomial(createTerm(coefficient = -ONE))

    val result = polynomial1 pow polynomial2

    // 2^-1=1/2 (this demonstrates constant-only powers, and that negative powers sometimes work).
    assertThat(result).isConstantThat().isEqualTo(ONE_HALF)
  }

  @Test
  fun testPow_four_negativeOneHalf_returnsOneHalf() {
    val polynomial1 = createPolynomial(createTerm(coefficient = FOUR_REAL))
    val polynomial2 = NEGATIVE_ONE_HALF_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // 4^(-1/2)=1/2 (this demonstrates constant-only powers, and that negative powers sometimes work).
    assertThat(result).isConstantThat().isEqualTo(ONE_HALF)
  }

  @Test
  fun testPow_onePlusX_oneHalf_returnsNull() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 1))
    )
    val polynomial2 = ONE_HALF_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // (1+x)^(1/2) fails since 1+x has no square root.
    assertThat(result).isNotValidPolynomial()
  }

  @Test
  fun testPow_onePlus2XPlusXSquared_oneHalf_returnsNull() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE),
      createTerm(coefficient = TWO_REAL, createVariable(name = "x", power = 1)),
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )
    val polynomial2 = ONE_HALF_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // (1+2x+x^2)^(1/2) fails since multi-term factoring is not currently supported.
    assertThat(result).isNotValidPolynomial()
  }

  @Test
  fun testPow_xSquaredMinusOne_oneHalf_returnsNull() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2)),
      createTerm(coefficient = -ONE)
    )
    val polynomial2 = ONE_HALF_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // (x^2-1)^(1/2) fails since multi-term factoring is not currently supported.
    assertThat(result).isNotValidPolynomial()
  }

  @Test
  fun testPow_xSquared_oneHalf_returnsX() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = ONE, createVariable(name = "x", power = 2))
    )
    val polynomial2 = ONE_HALF_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // (x^2)^(1/2)=x.
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testPow_fourXSquared_oneHalf_returnsTwoX() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = FOUR_REAL, createVariable(name = "x", power = 2))
    )
    val polynomial2 = ONE_HALF_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // (4x^2)^(1/2)=2x (demonstrates that coefficients can also be rooted).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(2)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testPow_xSquared_negativeOneHalf_returnsNull() {
    val polynomial1 = createPolynomial(
      createTerm(coefficient = -ONE, createVariable(name = "x", power = 2))
    )
    val polynomial2 = ONE_HALF_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // (-x^2)^(1/2) fails since a negative coefficient can't be square rooted.
    assertThat(result).isNotValidPolynomial()
  }

  @Test
  fun testPow_negativeTwentySevenXCubed_oneThird_returnsNegativeThreeX() {
    val twentySevenReal = checkNotNull(THREE_REAL pow THREE_REAL)
    val polynomial1 = createPolynomial(
      createTerm(coefficient = -twentySevenReal, createVariable(name = "x", power = 3))
    )
    val polynomial2 = createPolynomial(createTerm(coefficient = ONE_THIRD_REAL))

    val result = polynomial1 pow polynomial2

    // (-9x^3)^(1/3)=-3x (demonstrates real number rooting, i.e. support for negative coefficients
    // in certain cases).
    assertThat(result).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-3)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
  }

  @Test
  fun testPow_xSquared_oneThird_returnsNull() {
    val twentySevenReal = checkNotNull(THREE_REAL pow THREE_REAL)
    val polynomial1 = createPolynomial(
      createTerm(coefficient = twentySevenReal, createVariable(name = "x", power = 2))
    )
    val polynomial2 = createPolynomial(createTerm(coefficient = ONE_THIRD_REAL))

    val result = polynomial1 pow polynomial2

    // (27x^2)^(1/3) fails since the power '2' cannot be taken to the 1/3 (i.e. 2/3 is not a valid
    // polynomial power).
    assertThat(result).isNotValidPolynomial()
  }

  @Test
  fun testPow_xAndPi_returnsNull() {
    val polynomial1 = ONE_X_POLYNOMIAL
    val polynomial2 = PI_POLYNOMIAL

    val result = polynomial1 pow polynomial2

    // Cannot raise polynomials to non-integer powers.
    assertThat(result).isNotValidPolynomial()
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
