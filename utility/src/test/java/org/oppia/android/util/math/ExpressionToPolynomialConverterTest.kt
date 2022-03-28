package org.oppia.android.util.math

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.MathExpression
import org.oppia.android.testing.math.PolynomialSubject.Companion.assertThat
import org.oppia.android.util.math.ExpressionToPolynomialConverter.Companion.reduceToPolynomial
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.ALL_ERRORS
import org.oppia.android.util.math.MathExpressionParser.Companion.ErrorCheckingMode.REQUIRED_ONLY
import org.oppia.android.util.math.MathExpressionParser.Companion.MathParsingResult
import org.robolectric.annotation.LooperMode

/**
 * Tests for [ExpressionToPolynomialConverter].
 *
 * Note that this suite only tests with algebraic expressions since numeric expressions are never
 * considered to be polynomials (despite numeric expression evaluation and the constant term of
 * polynomials being expected to always result in the same value).
 */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ExpressionToPolynomialConverterTest {
  @Test
  fun testReduce_integerConstantExpression_returnsConstantPolynomial() {
    val expression = parseAlgebraicExpression("2")

    val polynomial = expression.reduceToPolynomial()

    assertThat(polynomial).isConstantThat().isIntegerThat().isEqualTo(2)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("2")
  }

  @Test
  fun testReduce_decimalConstantExpression_returnsConstantPolynomial() {
    val expression = parseAlgebraicExpression("3.14")

    val polynomial = expression.reduceToPolynomial()

    assertThat(polynomial).isConstantThat().isIrrationalThat().isWithin(1e-5).of(3.14)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("3.14")
  }

  @Test
  fun testReduce_variableConstantExpression_returnsSingleTermPolynomial() {
    val expression = parseAlgebraicExpression("x")

    val polynomial = expression.reduceToPolynomial()

    assertThat(polynomial).hasTermCountThat().isEqualTo(1)
    assertThat(polynomial).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(1)
    assertThat(polynomial).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(polynomial).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(polynomial).term(0).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x")
  }

  @Test
  fun testReduce_intTimesVariable_returnsPolynomialWithCoefficient() {
    val expression = parseAlgebraicExpression("7*x")

    val polynomial = expression.reduceToPolynomial()

    assertThat(polynomial).hasTermCountThat().isEqualTo(1)
    assertThat(polynomial).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(7)
    assertThat(polynomial).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(polynomial).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(polynomial).term(0).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("7x")
  }

  @Test
  fun testReduce_negativeDecimalTimesVariable_returnsPolynomialWithNegativeCoefficient() {
    val expression = parseAlgebraicExpression("-3.14*x")

    val polynomial = expression.reduceToPolynomial()

    assertThat(polynomial).hasTermCountThat().isEqualTo(1)
    assertThat(polynomial).term(0).hasCoefficientThat().isIrrationalThat().isWithin(1e-5).of(-3.14)
    assertThat(polynomial).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(polynomial).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(polynomial).term(0).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("-3.14x")
  }

  @Test
  fun testReduce_twoTimesXImplicitly_returnsPolynomialWithOneTermAndCoefficient() {
    val expression = parseAlgebraicExpression("2x")

    val polynomial = expression.reduceToPolynomial()

    assertThat(polynomial).hasTermCountThat().isEqualTo(1)
    assertThat(polynomial).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(2)
    assertThat(polynomial).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(polynomial).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(polynomial).term(0).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("2x")
  }

  @Test
  fun testReduce_zeroX_returnsZeroPolynomial() {
    val expression = parseAlgebraicExpression("0x")

    val polynomial = expression.reduceToPolynomial()

    // 0x just becomes 0 (the 'x' is removed).
    assertThat(polynomial).isConstantThat().isIntegerThat().isEqualTo(0)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("0")
  }

  @Test
  fun testReduce_onePlusTwo_returnsConstantThreePolynomial() {
    val expression = parseAlgebraicExpression("1+2")

    val polynomial = expression.reduceToPolynomial()

    // The '1+2' is reduced to a single '3' constant.
    assertThat(polynomial).isConstantThat().isIntegerThat().isEqualTo(3)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("3")
  }

  @Test
  fun testReduce_xPlusX_returnTwoXPolynomial() {
    val expression = parseAlgebraicExpression("x+x")

    val polynomial = expression.reduceToPolynomial()

    // x+x is combined to 2x (like terms are combined).
    assertThat(polynomial).hasTermCountThat().isEqualTo(1)
    assertThat(polynomial).term(0).hasCoefficientThat().isIntegerThat().isEqualTo(2)
    assertThat(polynomial).term(0).hasVariableCountThat().isEqualTo(1)
    assertThat(polynomial).term(0).variable(0).hasNameThat().isEqualTo("x")
    assertThat(polynomial).term(0).variable(0).hasPowerThat().isEqualTo(1)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("2x")
  }

  @Test
  fun testReduce_xPlusOne_returnsXPlusOnePolynomial() {
    val expression = parseAlgebraicExpression("x+1")

    val polynomial = expression.reduceToPolynomial()

    // x+1 leads to a two-term polynomial.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x + 1")
  }

  @Test
  fun testReduce_onePlusX_returnsXPlusOnePolynomial() {
    val expression = parseAlgebraicExpression("1+x")

    val polynomial = expression.reduceToPolynomial()

    // 1+x leads to a two-term polynomial (with 'x' sorted first).
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x + 1")
  }

  @Test
  fun testReduce_xMinusOne_returnsXMinusOnePolynomial() {
    val expression = parseAlgebraicExpression("x-1")

    val polynomial = expression.reduceToPolynomial()

    // x-1 leads to a two-term polynomial.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x - 1")
  }

  @Test
  fun testReduce_oneMinusX_returnsNegativeXPlusOne() {
    val expression = parseAlgebraicExpression("1-x")

    val polynomial = expression.reduceToPolynomial()

    // 1-x leads to a two-term polynomial (note that 'x' is listed first due to sort priority).
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("-x + 1")
  }

  @Test
  fun testReduce_xPlusTwoX_returnsThreeXPolynomial() {
    val expression = parseAlgebraicExpression("x+2x")

    val polynomial = expression.reduceToPolynomial()

    // x+2x combines to 3x (since like terms are combined). This also verifies that coefficients are
    // correctly combined.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(3)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("3x")
  }

  @Test
  fun testReduce_xYPlusYzMinusXzMinusYzPlusThreeXy_returnsFourXyMinusXzPolynomial() {
    val expression = parseAlgebraicExpression("xy+yz-xz-yz+3xy")

    val polynomial = expression.reduceToPolynomial()

    // xy+yz-xz-yz+3xy combines to 4xy-xz (eliminated terms are removed).
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(4)
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
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("z")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("4xy - xz")
  }

  @Test
  fun testReduce_xy_returnsXTimesYPolynomial() {
    val expression = parseAlgebraicExpression("xy")

    val polynomial = expression.reduceToPolynomial()

    // xy is a single-term, two-variable polynomial.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("xy")
  }

  @Test
  fun testReduce_four_timesXPlusTwo_returnsEightXPlusEightPolynomial() {
    val expression = parseAlgebraicExpression("4*(x+2)")

    val polynomial = expression.reduceToPolynomial()

    // 4*(x+2) becomes 4x+8 (the constant distributes to each term's coefficient).
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(4)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(8)
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("4x + 8")
  }

  @Test
  fun testReduce_x_timesOnePlusX_returnsXSquaredPlusXPolynomial() {
    val expression = parseAlgebraicExpression("x(1+x)")

    val polynomial = expression.reduceToPolynomial()

    // x(1+x) is expanded to x^2+x.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2 + x")
  }

  @Test
  fun testReduce_y_timesOnePlusX_returnsXyPlusYPolynomial() {
    val expression = parseAlgebraicExpression("y(1+x)")

    val polynomial = expression.reduceToPolynomial()

    // y(1+x) is expanded to xy+y.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("xy + y")
  }

  @Test
  fun testReduce_xPlusOne_timesXMinusOne_returnsXSquaredMinusOnePolynomial() {
    val expression = parseAlgebraicExpression("(x+1)(x-1)")

    val polynomial = expression.reduceToPolynomial()

    // (x+1)(x-1) expands to x^2-1.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2 - 1")
  }

  @Test
  fun testReduce_xMinusOne_timesXPlusOne_returnsXSquaredMinusOnePolynomial() {
    val expression = parseAlgebraicExpression("(x-1)(x+1)")

    val polynomial = expression.reduceToPolynomial()

    // (x-1)(x+1) expands to x^2-1 (demonstrating multiplication commutativity).
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2 - 1")
  }

  @Test
  fun testReduce_xPlusOne_timesXPlusOne_returnsXSquaredPlusTwoXPlusOnePolynomial() {
    val expression = parseAlgebraicExpression("(x+1)(x+1)")

    val polynomial = expression.reduceToPolynomial()

    // (x+1)(x+1) expands to x^2+2x+1 (binomial multiplication).
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(3)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
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
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2 + 2x + 1")
  }

  @Test
  fun testReduce_twoMinusX_timesThreeXPlusSeven_returnsMinusThreeXSqPlusXPlusFourteenPolynomial() {
    val expression = parseAlgebraicExpression("(2-x)(3x+7)")

    val polynomial = expression.reduceToPolynomial()

    // (2-x)(3x+7) expands to -3x^2-x+14 (shows multiplication with x coefficients).
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(3)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-3)
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
      term(2).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(14)
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("-3x^2 - x + 14")
  }

  @Test
  fun testReduce_xRaisedToTwo_returnsXSquaredPolynomial() {
    val expression = parseAlgebraicExpression("x^2")

    val polynomial = expression.reduceToPolynomial()

    // x^2 is treated as the variable 'x' with power 2.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2")
  }

  @Test
  fun testReduce_xSquaredPlusXPlusOne_returnsXSquaredPlusXPlusOnePolynomial() {
    val expression = parseAlgebraicExpression("x^2+x+1")

    val polynomial = expression.reduceToPolynomial()

    // x^2+x+1 stays the same since no terms can be combined, eliminated, or reordered.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(3)
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
      term(2).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2 + x + 1")
  }

  @Test
  fun testReduce_xSquaredPlusXPlusXY_returnsXSquaredPlusXyPlusXPolynomial() {
    val expression = parseAlgebraicExpression("x^2+x+xy")

    val polynomial = expression.reduceToPolynomial()

    // x^2+x+xy is treated as the same polynomial, though 'xy' comes before 'x' per sorting rules.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(3)
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
      term(2).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2 + xy + x")
  }

  @Test
  fun testReduce_x_timesXSquared_returnsXCubedPolynomial() {
    val expression = parseAlgebraicExpression("xx^2")

    val polynomial = expression.reduceToPolynomial()

    // xx^2 becomes x^3 since like terms are multiplied and simplified.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(3)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^3")
  }

  @Test
  fun testReduce_xSquared_plusXSquaredY_returnsXSquaredYPlusXSquared() {
    val expression = parseAlgebraicExpression("x^2 + x^2y")

    val polynomial = expression.reduceToPolynomial()

    // x^2+x^2y becomes x^2y+x^2 (terms reordered, but nothing should be combined).
    assertThat(polynomial).apply {
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
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2y + x^2")
  }

  @Test
  fun testReduce_constant_division_returnsFractionalPolynomial() {
    val expression = parseAlgebraicExpression("1/2")

    val polynomial = expression.reduceToPolynomial()

    // Division of constants is actually computed.
    assertThat(polynomial).isConstantThat().isEqualTo(ONE_HALF)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("1/2")
  }

  @Test
  fun testReduce_decimalConstant_division_returnsIrrationalPolynomial() {
    val expression = parseAlgebraicExpression("3.14/2")

    val polynomial = expression.reduceToPolynomial()

    // Division of constants is actually computed.
    assertThat(polynomial).isConstantThat().isIrrationalThat().isWithin(1e-5).of(1.57)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("1.57")
  }

  @Test
  fun testReduce_x_dividedByZero_returnsNullPolynomial() {
    // Dividing by zero is an optional error that needs to be disabled for this check.
    val expression = parseAlgebraicExpression("x/0", errorCheckingMode = REQUIRED_ONLY)

    val polynomial = expression.reduceToPolynomial()

    // Cannot divide by zero.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_x_dividedByOneMinusOne_returnsNullPolynomial() {
    val expression = parseAlgebraicExpression("x/(1-1)")

    val polynomial = expression.reduceToPolynomial()

    // Cannot divide by zero, even in cases when the denominator needs to be evaluated.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_x_dividedByXMinusX_returnsNullPolynomial() {
    val expression = parseAlgebraicExpression("x/(x-x)")

    val polynomial = expression.reduceToPolynomial()

    // Another division by zero, but more complex.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_two_dividedByZero_returnsNullPolynomial() {
    // Dividing by zero is an optional error that needs to be disabled for this check.
    val expression = parseAlgebraicExpression("2/0", errorCheckingMode = REQUIRED_ONLY)

    val polynomial = expression.reduceToPolynomial()

    // Division by zero is not allowed for purely constant polynomials, either.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_x_dividedByTwo_returnsOneHalfXPolynomial() {
    val expression = parseAlgebraicExpression("x/2")

    val polynomial = expression.reduceToPolynomial()

    // x/2 is treated as (1/2)x (that is, the variable 'x' with coefficient '1/2').
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isEqualTo(ONE_HALF)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("(1/2)x")
  }

  @Test
  fun testReduce_one_dividedByX_returnsNullPolynomial() {
    val expression = parseAlgebraicExpression("1/x")

    val polynomial = expression.reduceToPolynomial()

    // Polynomials cannot have negative powers, so dividing by a polynomial isn't valid.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_x_dividedByX_returnsOnePolynomial() {
    val expression = parseAlgebraicExpression("x/x")

    val polynomial = expression.reduceToPolynomial()

    // x/x is just '1'.
    assertThat(polynomial).isConstantThat().isIntegerThat().isEqualTo(1)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("1")
  }

  @Test
  fun testReduce_x_dividedByNegativeTwo_returnsNegativeOneHalfXPolynomial() {
    val expression = parseAlgebraicExpression("x/-2")

    val polynomial = expression.reduceToPolynomial()

    // x/-2 is treated as (-1/2)x (that is, the variable 'x' with coefficient '-1/2').
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isEqualTo(-ONE_HALF)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("(-1/2)x")
  }

  @Test
  fun testReduce_xPlusOne_dividedByTwo_returnsOneHalfXPlusOneHalfPolynomial() {
    val expression = parseAlgebraicExpression("(x+1)/2")

    val polynomial = expression.reduceToPolynomial()

    // (x+1)/2 expands to (1/2)x+(1/2), a two-term polynomial.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isEqualTo(ONE_HALF)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isEqualTo(ONE_HALF)
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("(1/2)x + 1/2")
  }

  @Test
  fun testReduce_xSquaredPlusX_dividedByX_returnsXPlusOnePolynomial() {
    val expression = parseAlgebraicExpression("(x^2+x)/x")

    val polynomial = expression.reduceToPolynomial()

    // (x^2+x)/x becomes x+1 ('x' is factored out).
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x + 1")
  }

  @Test
  fun testReduce_xyPlusY_dividedByX_returnsNullPolynomial() {
    val expression = parseAlgebraicExpression("(xy+y)/x")

    val polynomial = expression.reduceToPolynomial()

    // 'x' cannot be fully factored out of 'xy+y'.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_xyPlusY_dividedByY_returnsXPlusOnePolynomial() {
    val expression = parseAlgebraicExpression("(xy+y)/y")

    val polynomial = expression.reduceToPolynomial()

    // (xy+y)/y becomes x+1 ('y' is factored out).
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x + 1")
  }

  @Test
  fun testReduce_xyPlusY_dividedByXy_returnsNullPolynomial() {
    val expression = parseAlgebraicExpression("(xy+y)/(xy)")

    val polynomial = expression.reduceToPolynomial()

    // 'xy' cannot be cleanly factored out of 'xy+y'.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_xYMinusFiveY_dividedByY_returnsXMinusFivePolynomial() {
    val expression = parseAlgebraicExpression("(xy-5y)/y")

    val polynomial = expression.reduceToPolynomial()

    // (xy-5y)/y becomes x-5 (demonstrates that variables become coefficients in such cases).
    assertThat(polynomial).apply {
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
        hasCoefficientThat().isIntegerThat().isEqualTo(-5)
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x - 5")
  }

  @Test
  fun testReduce_xSquaredMinusOne_dividedByXPlusOne_returnsXMinusOnePolynomial() {
    val expression = parseAlgebraicExpression("(x^2-1)/(x+1)")

    val polynomial = expression.reduceToPolynomial()

    // (x^2-1)/(x+1)=x-1.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x - 1")
  }

  @Test
  fun testReduce_xSquaredMinusOne_dividedByXMinusOne_returnsXPlusOnePolynomial() {
    val expression = parseAlgebraicExpression("(x^2-1)/(x-1)")

    val polynomial = expression.reduceToPolynomial()

    // (x^2-1)/(x-1)=x+1.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x + 1")
  }

  @Test
  fun testReduce_xSquaredPlusTwoXPlusOne_dividedByXPlusOne_returnsXPlusOnePolynomial() {
    val expression = parseAlgebraicExpression("(x^2+2x+1)/(x+1)")

    val polynomial = expression.reduceToPolynomial()

    // (x^2+2x+1)/(x+1)=x+1.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x + 1")
  }

  @Test
  fun testReduce_negThreeXSqAddTwentyThreeXSubFourteen_dividedBySevenSubX_retsThreeXSubTwoPoly() {
    val expression = parseAlgebraicExpression("(-3x^2+23x-14)/(7-x)")

    val polynomial = expression.reduceToPolynomial()

    // (-3x^2+23x-14)/(7-x)=3x-2 (demonstrates both deriving a non-one coefficient in the quotient,
    // and dividing with negative leading terms).
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(3)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-2)
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("3x - 2")
  }

  @Test
  fun testReduce_xSquaredMinusTwoXyPlusYSquared_dividedByXMinusY_returnsXMinusYPolynomial() {
    val expression = parseAlgebraicExpression("(x^2-2xy+y^2)/(x-y)")

    val polynomial = expression.reduceToPolynomial()

    // (x^2-2xy+y^2)/(x-y)=x-y (demonstrates factoring out both 'x' and 'y' terms).
    assertThat(polynomial).apply {
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
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x - y")
  }

  @Test
  fun testReduce_xCubedMinusYCubed_dividedByXMinusY_returnsXSquaredPlusXyPlusYSquaredPolynomial() {
    val expression = parseAlgebraicExpression("(x^3-y^3)/(x-y)")

    val polynomial = expression.reduceToPolynomial()

    // (x^3-y^3)/(x-y)=x^2+xy+y^2. This demonstrates a more complex case where a new term can appear
    // due to the division. This example comes from:
    // https://www.kristakingmath.com/blog/predator-prey-systems-ghtcp-5e2r4-427ab.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(3)
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
      term(2).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2 + xy + y^2")
  }

  @Test
  fun testReduce_xCubedMinusThreeXSqYPlusXySqMinusYCubed_dividedByXMinusYSq_retsXMinusYPoly() {
    val expression = parseAlgebraicExpression("(x^3-3x^2y+3xy^2-y^3)/(x-y)^2")

    val polynomial = expression.reduceToPolynomial()

    // (x^3-3x^2y+3xy^2-y^3)/(x-y)^2=x-y (demonstrates dividing a variable term with a power larger
    // than 1).
    assertThat(polynomial).apply {
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
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x - y")
  }

  @Test
  fun testReduce_zeroRaisedToZero_returnsOne() {
    val expression = parseAlgebraicExpression("0^0")

    val polynomial = expression.reduceToPolynomial()

    // 0^0=1 (for consistency with other 'pow' functions).
    assertThat(polynomial).isConstantThat().isIntegerThat().isEqualTo(1)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("1")
  }

  @Test
  fun testReduce_zeroRaisedToOne_returnsZero() {
    val expression = parseAlgebraicExpression("0^1")

    val polynomial = expression.reduceToPolynomial()

    // 0^1=0.
    assertThat(polynomial).isConstantThat().isIntegerThat().isEqualTo(0)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("0")
  }

  @Test
  fun testReduce_oneRaisedToZero_returnsOne() {
    val expression = parseAlgebraicExpression("1^0")

    val polynomial = expression.reduceToPolynomial()

    // 1^0=1.
    assertThat(polynomial).isConstantThat().isIntegerThat().isEqualTo(1)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("1")
  }

  @Test
  fun testReduce_twoRaisedToZero_returnsOne() {
    val expression = parseAlgebraicExpression("2^0")

    val polynomial = expression.reduceToPolynomial()

    // 2^0=1.
    assertThat(polynomial).isConstantThat().isIntegerThat().isEqualTo(1)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("1")
  }

  @Test
  fun testReduce_xRaisedToZero_returnsOnePolynomial() {
    val expression = parseAlgebraicExpression("x^0")

    val polynomial = expression.reduceToPolynomial()

    // x^0 is just 1 since anything raised to '1' is 1.
    assertThat(polynomial).isConstantThat().isIntegerThat().isEqualTo(1)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("1")
  }

  @Test
  fun testReduce_xRaisedToOne_returnsXPolynomial() {
    val expression = parseAlgebraicExpression("x^1")

    val polynomial = expression.reduceToPolynomial()

    // x^1 is just 'x' (i.e. a polynomial with a variable term 'x' with power '1').
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x")
  }

  @Test
  fun testReduce_xRaisedToNegativeOne_returnsNullPolynomial() {
    val expression = parseAlgebraicExpression("x^-1")

    val polynomial = expression.reduceToPolynomial()

    // Polynomials cannot have negative powers.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_twoRaisedToQuantityThreeMinusSix_returnsOneEighthPolynomial() {
    val expression = parseAlgebraicExpression("2^(3-6)")

    val polynomial = expression.reduceToPolynomial()

    // 2^(3-6) evaluates to 1/8 (i.e. constants can be raised to negative powers).
    assertThat(polynomial).isConstantThat().isRationalThat().apply {
      hasNegativePropertyThat().isFalse()
      hasWholeNumberThat().isEqualTo(0)
      hasNumeratorThat().isEqualTo(1)
      hasDenominatorThat().isEqualTo(8)
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("1/8")
  }

  @Test
  fun testReduce_xRaisedToQuantityThreeMinusSix_returnsNullPolynomial() {
    val expression = parseAlgebraicExpression("x^(3-6)")

    val polynomial = expression.reduceToPolynomial()

    // Polynomials cannot have negative powers.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_negativeTwoXQuantityRaisedToTwo_returnsFourXSquaredPolynomial() {
    val expression = parseAlgebraicExpression("(-2x)^2")

    val polynomial = expression.reduceToPolynomial()

    // (-2x)^2=4x^2 (negative term goes away and coefficient is multiplied).
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(4)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("4x^2")
  }

  @Test
  fun testReduce_negativeTwoXQuantityRaisedToThree_returnsNegativeEightXCubedPolynomial() {
    val expression = parseAlgebraicExpression("(-2x)^3")

    val polynomial = expression.reduceToPolynomial()

    // (-2x)^3=-8x^3 (the negative is kept due to an odd power.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-8)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(3)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("-8x^3")
  }

  @Test
  fun testReduce_xYRaisedToTwo_returnsXYSquaredPolynomial() {
    val expression = parseAlgebraicExpression("xy^2")

    val polynomial = expression.reduceToPolynomial()

    // For 'xy^2' the 'y' will have power '2' and 'x' will have power '1'. This and related tests
    // help to verify that exponentiation assigns the power to the correct variable when parsing
    // polynomial syntax.
    assertThat(polynomial).apply {
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
          hasPowerThat().isEqualTo(2)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("xy^2")
  }

  @Test
  fun testReduce_yXRaisedToTwo_returnsXSquaredYPolynomial() {
    val expression = parseAlgebraicExpression("yx^2")

    val polynomial = expression.reduceToPolynomial()

    // For 'x^2y' the 'x' will have power '2' and 'y' will have power '1'.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(1)
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
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2y")
  }

  @Test
  fun testReduce_xRaisedToTwoYRaisedToTwo_returnsXSquaredYSquaredPolynomial() {
    val expression = parseAlgebraicExpression("x^2y^2")

    val polynomial = expression.reduceToPolynomial()

    // For 'x^2y^2' both variables have power '2'.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(2)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2y^2")
  }

  @Test
  fun testReduce_twoRaisedToX_returnsNullPolynomial() {
    // Raising to a variable term is an optional error that needs to be disabled for this check.
    val expression = parseAlgebraicExpression("2^x", errorCheckingMode = REQUIRED_ONLY)

    val polynomial = expression.reduceToPolynomial()

    // 2^x is not a polynomial since powers must be positive integers.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_xRaisedToX_returnsNullPolynomial() {
    // Raising to a variable term is an optional error that needs to be disabled for this check.
    val expression = parseAlgebraicExpression("x^x", errorCheckingMode = REQUIRED_ONLY)

    val polynomial = expression.reduceToPolynomial()

    // x^x is not a polynomial since powers must be positive integers.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_squareRootX_returnsNullPolynomial() {
    val expression = parseAlgebraicExpression("sqrt(x)")

    val polynomial = expression.reduceToPolynomial()

    // sqrt(x) is not a polynomial.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_squareRootXQuantitySquared_returnsXPolynomial() {
    val expression = parseAlgebraicExpression("sqrt(x)^2")

    val polynomial = expression.reduceToPolynomial()

    // This doesn't currently evaluate correctly due to a limitation in the reduction algorithm (it
    // can't represent sub-polynomials with fractional powers).
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_squareRootOfXSquared_returnsXPolynomial() {
    val expression = parseAlgebraicExpression("sqrt(x^2)")

    val polynomial = expression.reduceToPolynomial()

    // sqrt(x^2) is simplified to 'x'.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x")
  }

  @Test
  fun testReduce_squareRootOfOnePlusX_returnsNullPolynomial() {
    val expression = parseAlgebraicExpression("sqrt(1+x)")

    val polynomial = expression.reduceToPolynomial()

    // 1+x has no square root.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_squareRootOfFourXSquared_returnsTwoXPolynomial() {
    val expression = parseAlgebraicExpression("sqrt(4x^2)")

    val polynomial = expression.reduceToPolynomial()

    // sqrt(4x^2)=2x.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("2x")
  }

  @Test
  fun testReduce_squareRootOfNegativeFourXSquared_returnsNullPolynomial() {
    val expression = parseAlgebraicExpression("sqrt(-4x^2)")

    val polynomial = expression.reduceToPolynomial()

    // sqrt(-4x^2) is not valid since negative even roots result in imaginary results.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_squareRootOfXSquaredYSquared_returnsXyPolynomial() {
    val expression = parseAlgebraicExpression("√(x^2y^2)")

    val polynomial = expression.reduceToPolynomial()

    // √(x^2y^2) evaluates to xy (i.e. individual variable terms can be extracted and rooted).
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("xy")
  }

  @Test
  fun testReduce_squareTwoXSquared_returnsIrrationalCoefficientXPolynomial() {
    val expression = parseAlgebraicExpression("√(2x^2)")

    val polynomial = expression.reduceToPolynomial()

    // √(2x^2) evaluates to a polynomial with a decimal coefficient.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIrrationalThat().isWithin(1e-5).of(1.414213562)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().matches("1.414\\d+x")
  }

  @Test
  fun testReduce_sixteenXToTheFourth_raisedToOneFourth_returnsTwoXPolynomial() {
    val expression = parseAlgebraicExpression("((2x)^4)^(1/4)")

    val polynomial = expression.reduceToPolynomial()

    // ((2x)^4)^(1/4)=2x (demonstrates root-based operations with exponentiation).
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("2x")
  }

  @Test
  fun testReduce_negativeSixteenXToTheFourth_raisedToOneFourth_returnsNullPolynomial() {
    val expression = parseAlgebraicExpression("(-16x^4)^(1/4)")

    val polynomial = expression.reduceToPolynomial()

    // (-16x^4)^(1/4) is not valid since negative even roots result in imaginary results.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_negativeTwentySevenYCubed_raisedToOneThird_returnsNegativeThreeXPolynomial() {
    val expression = parseAlgebraicExpression("(-27y^3)^(1/3)")

    val polynomial = expression.reduceToPolynomial()

    // (-27y^3)^(1/3)=-3y (shows that odd roots can accept real-valued negative radicands).
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-3)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("-3y")
  }

  @Test
  fun testReduce_xSquared_raisedToOneHalf_returnsXPolynomial() {
    val expression = parseAlgebraicExpression("(x^2)^(1/2)")

    val polynomial = expression.reduceToPolynomial()

    // (x^2)^(1/2) simplifies to just 'x'.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x")
  }

  @Test
  fun testReduce_xToTheOneHalf_squared_returnsXPolynomial() {
    val expression = parseAlgebraicExpression("(x^(1/2))^2")

    val polynomial = expression.reduceToPolynomial()

    // This doesn't currently evaluate correctly due to a limitation in the reduction algorithm (it
    // can't represent sub-polynomials with fractional powers).
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_xCubed_raisedToOneThird_returnsXPolynomial() {
    val expression = parseAlgebraicExpression("(x^3)^(1/3)")

    val polynomial = expression.reduceToPolynomial()

    // (x^3)^(1/3) simplifies to just 'x'.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x")
  }

  @Test
  fun testReduce_xCubed_raisedToTwoThirds_returnsXSquaredPolynomial() {
    val expression = parseAlgebraicExpression("(x^3)^(2/3)")

    val polynomial = expression.reduceToPolynomial()

    // (x^3)^(2/3) simplifies to 'x^2'.
    assertThat(polynomial).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2")
  }

  @Test
  fun testReduce_xToTheOneThird_cubed_returnsXPolynomial() {
    val expression = parseAlgebraicExpression("(x^(1/3))^3")

    val polynomial = expression.reduceToPolynomial()

    // This doesn't currently evaluate correctly due to a limitation in the reduction algorithm (it
    // can't represent sub-polynomials with fractional powers).
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_xPlusOne_squared_returnsXSquaredPlusTwoXPlusOnePolynomial() {
    val expression = parseAlgebraicExpression("(x+1)^2")

    val polynomial = expression.reduceToPolynomial()

    // (x+1)^2=x^2+2x+1 (simple binomial multiplication).
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(3)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
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
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^2 + 2x + 1")
  }

  @Test
  fun testReduce_xPlusOne_cubed_returnsXCubedPlusThreeXSquaredPlusThreeXPlusOnePolynomial() {
    val expression = parseAlgebraicExpression("(x+1)^3")

    val polynomial = expression.reduceToPolynomial()

    // (x+1)^3=x^3+3x^2+3x+1 (simple binomial multiplication per Pascal's triangle).
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(4)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(3)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(3)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(2).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(3)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(3).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^3 + 3x^2 + 3x + 1")
  }

  @Test
  fun testReduce_xMinusYCubed_returnsXCubedMinusThreeXSqYPlusThreeXYSqMinusYCubedPolynomial() {
    val expression = parseAlgebraicExpression("(x-y)^3")

    val polynomial = expression.reduceToPolynomial()

    // (x-y)^3=x^3-3x^2y+3xy^2-y^3 (show that exponentiation works with double variable terms, too).
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(4)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(3)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-3)
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
      term(2).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(3)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(3).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(-1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(3)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^3 - 3x^2y + 3xy^2 - y^3")
  }

  @Test
  fun testReduce_xSquaredPlusTwoXPlusOne_raisedToOneHalf_returnsNullPolynomial() {
    val expression = parseAlgebraicExpression("(x^2+2x+1)^(1/2)")

    val polynomial = expression.reduceToPolynomial()

    // While (x^2+2x+1)^(1/2) can technically be factored to (x+1), the system doesn't yet support
    // factoring polynomials via roots.
    assertThat(polynomial).isNotValidPolynomial()
  }

  @Test
  fun testReduce_xRaisedToTwoPlusTwo_returnsXToTheFourthPolynomial() {
    val expression = parseAlgebraicExpression("x^(2+2)")

    val polynomial = expression.reduceToPolynomial()

    // x^(2+2)=x^4 (the exponent is evaluated).
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(1)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(4)
        }
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^4")
  }

  @Test
  fun testReduce_xRaisedToTwoMinusTwo_returnsOnePolynomial() {
    val expression = parseAlgebraicExpression("x^(2-2)")

    val polynomial = expression.reduceToPolynomial()

    // x^(2-2)=1 (since 2-2 evaluates to 0, and x^0 is 1).
    assertThat(polynomial).isConstantThat().isIntegerThat().isEqualTo(1)
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("1")
  }

  @Test
  fun testReduce_moreComplexArithmeticExpression_returnsCorrectlyComputedCoefficientsPolynomial() {
    val expression = parseAlgebraicExpression("133+3.14*x/(11-15)^2")

    val polynomial = expression.reduceToPolynomial()

    // 133+3.14*x/(11-15)^2 simplifies to 0.19625x+133.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(2)
      term(0).apply {
        hasCoefficientThat().isIrrationalThat().isWithin(1e-5).of(0.19625)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(1).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(133)
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("0.19625x + 133")
  }

  /*
   * Tests to verify that ordering matches https://en.wikipedia.org/wiki/Polynomial#Definition
   * (where multiple variables are sorted lexicographically).
   */

  @Test
  fun testReduce_xCubedPlusXSquaredPlusXPlusOne_returnsSameOrderPolynomial() {
    val expression = parseAlgebraicExpression("x^3+x^2+x+1")

    val polynomial = expression.reduceToPolynomial()

    // x^3+x^2+x+1 retains its order since higher power terms are ordered first.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(4)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(3)
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
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^3 + x^2 + x + 1")
  }

  @Test
  fun testReduce_onePlusXPlusXSquaredPlusXCubed_returnsXCubedPlusXSquaredPlusXPlusOnePolynomial() {
    val expression = parseAlgebraicExpression("1+x+x^2+x^3")

    val polynomial = expression.reduceToPolynomial()

    // 1+x+x^2+x^3 is reversed to x^3+x^2+x+1 since higher power terms are ordered first.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(4)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(3)
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
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("x^3 + x^2 + x + 1")
  }

  @Test
  fun testReduce_xyPlusXzPlusYz_returnsSameOrderPolynomial() {
    val expression = parseAlgebraicExpression("xy+xz+yz")

    val polynomial = expression.reduceToPolynomial()

    // xy+xz+yz retains its order since multivariable terms are ordered lexicographically.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(3)
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
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("z")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(2).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("xy + xz + yz")
  }

  @Test
  fun testReduce_zYPlusZxPlusYX_returnsXyPlusXzPlusYzPolynomial() {
    val expression = parseAlgebraicExpression("zy+zx+yx")

    val polynomial = expression.reduceToPolynomial()

    // zy+zx+yx is reversed in ordered and terms to be xy+xz+yz since multivariable terms are
    // ordered lexicographically.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(3)
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
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("z")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(2).apply {
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
    assertThat(polynomial).evaluatesToPlainTextThat().isEqualTo("xy + xz + yz")
  }

  @Test
  fun testReduce_complexMultiVariableOutOfOrderExpression_returnsCorrectlyOrderedPolynomial() {
    val expression = parseAlgebraicExpression("3+y+x+yx+x^2y+x^2y^2+y^2x")

    val polynomial = expression.reduceToPolynomial()

    // 3+y+x+yx+x^2y+x^2y^2+y^2x is sorted to: x^2y^2+x^2y+xy^2+xy+x+y+3 per term sorting rules.
    // ordered lexicographically.
    assertThat(polynomial).apply {
      hasTermCountThat().isEqualTo(7)
      term(0).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(2)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(1).apply {
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
      term(2).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(2)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
        variable(1).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(2)
        }
      }
      term(3).apply {
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
      term(4).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("x")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(5).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(1)
        hasVariableCountThat().isEqualTo(1)
        variable(0).apply {
          hasNameThat().isEqualTo("y")
          hasPowerThat().isEqualTo(1)
        }
      }
      term(6).apply {
        hasCoefficientThat().isIntegerThat().isEqualTo(3)
        hasVariableCountThat().isEqualTo(0)
      }
    }
    assertThat(polynomial)
      .evaluatesToPlainTextThat()
      .isEqualTo("x^2y^2 + x^2y + xy^2 + xy + x + y + 3")
  }

  @Test
  fun testEquals_twoPolynomial_twoPolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("2")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("2")

    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_zeroPolynomial_negativeZeroPolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("0")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("-0")

    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_twoPolynomial_negativeTwoPolynomial_areNotEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("2")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("-2")

    assertThat(polynomial1).isNotEqualTo(polynomial2)
  }

  @Test
  fun testEquals_onePlusTwoPolynomial_threePolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("1+2")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("3")

    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_threePolynomial_onePlusTwoPolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("3")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("1+2")

    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_oneMinusTwoPolynomial_negativeOnePolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("1-2")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("-1")

    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_twoTimesSixPolynomial_sixPolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("2*3")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("6")

    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_twoRaisedToThreePolynomial_eightPolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("2^3")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("8")

    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_xPolynomial_xPolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("x")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("x")

    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_xPolynomial_twoPolynomial_areNotEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("x")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("2")

    assertThat(polynomial1).isNotEqualTo(polynomial2)
  }

  @Test
  fun testEquals_onePlusXPolynomial_xPlusOnePolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("1+x")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("x+1")

    // Demonstrate that commutativity doesn't matter (for addition).
    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_xPlusYPolynomial_yPlusXPolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("x+y")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("y+x")

    // Commutativity doesn't change for variable ordering.
    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_oneMinusXPolynomial_xMinusOnePolynomial_areNotEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("1-x")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("x-1")

    // Subtraction is not commutative.
    assertThat(polynomial1).isNotEqualTo(polynomial2)
  }

  @Test
  fun testEquals_twoXPolynomial_xTimesTwoPolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("2x")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("x*2")

    // Demonstrate that commutativity doesn't matter (for multiplication).
    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_twoDividedByXPolynomial_xDividedByTwoPolynomial_areNotEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("2/x")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("x/2")

    // Division is not commutative.
    assertThat(polynomial1).isNotEqualTo(polynomial2)
  }

  @Test
  fun testEquals_xTimesQuantityXPlusOnePolynomial_xSquaredPlusXPolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("x(x+1)")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("x^2+x")

    // Multiplication is expanded.
    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_threeXSubTwoTimesSevenSubX_minusThreeXSqAddTwentyThreeXSubFourteen_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("(3x-2)(7-x)")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("-3x^2+23x-14")

    // Multiplication is expanded.
    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_quantityXPlusOneSquaredPolynomial_xSquaredPlusTwoXPlusOnePolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("(x+1)^2")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("x^2+2x+1")

    // Exponentiation is expanded.
    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_quantityXPlusOneDividedByTwoPolynomial_oneHalfXPlusOneHalfPolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("(x+1)/2")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("x/2+(1/2)")

    // Division distributes.
    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_squareRootOnePlusOnePolynomial_squareRootTwoPolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("sqrt(1+1)")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("sqrt(2)")

    // The two are equal after evaluation (to contrast with comparable operations).
    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_squareRootTwoPolynomial_squareRootThreePolynomial_areNotEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("sqrt(2)")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("sqrt(3)")

    // The evaluated constants are actually different.
    assertThat(polynomial1).isNotEqualTo(polynomial2)
  }

  @Test
  fun testEquals_squareRootTwoXSquaredPolynomial_twoXSquaredToOneHalfPolynomial_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("sqrt(2x^2)")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("(2x^2)^(1/2)")

    // sqrt() is the same as raising to 1/2.
    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  @Test
  fun testEquals_complexPolynomial_samePolynomialInDifferentOrder_areEqual() {
    val polynomial1 = parsePolynomialFromAlgebraicExpression("3+y+x+yx+x^2y+x^2y^2+y^2x")
    val polynomial2 = parsePolynomialFromAlgebraicExpression("xy+xy^2+x^2y+y^2x^2+3+x+y")

    // Order doesn't matter.
    assertThat(polynomial1).isEqualTo(polynomial2)
  }

  private fun parsePolynomialFromAlgebraicExpression(expression: String) =
    parseAlgebraicExpression(expression).reduceToPolynomial()

  private companion object {
    private fun parseAlgebraicExpression(
      expression: String,
      errorCheckingMode: ErrorCheckingMode = ALL_ERRORS
    ): MathExpression {
      return MathExpressionParser.parseAlgebraicExpression(
        expression, allowedVariables = listOf("x", "y", "z"), errorCheckingMode
      ).getExpectedSuccess()
    }

    private inline fun <reified T> MathParsingResult<T>.getExpectedSuccess(): T {
      assertThat(this).isInstanceOf(MathParsingResult.Success::class.java)
      return (this as MathParsingResult.Success<T>).result
    }
  }
}
